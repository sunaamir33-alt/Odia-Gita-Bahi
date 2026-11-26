package com.example.data.repository

import android.content.Context
import com.example.BuildConfig
import com.example.data.local.FavoriteEntity
import com.example.data.local.RecentSongEntity
import com.example.data.local.SongDao
import com.example.data.local.SongEntity
import com.example.data.local.SongWithMeta
import com.example.data.local.UserPreferencesRepository
import com.example.data.remote.BaserowApiService
import com.example.data.remote.BaserowNetworkFactory
import com.example.data.remote.BaserowPageResponse
import com.example.data.remote.BaserowRowMapper
import com.example.data.remote.BaserowSongMapped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

sealed class SyncState {
    data object Idle : SyncState()
    data class Syncing(val fetchedSoFar: Int = 0, val totalExpected: Int = 0) : SyncState()
    data class Success(val syncedCount: Int, val timestamp: Long) : SyncState()
    data object MissingToken : SyncState()
    data class Error(val message: String) : SyncState()
}

data class UpdateCheckStatus(
    val remoteCount: Int,
    val localCount: Int,
    val hasUpdate: Boolean
)

class SongRepository(
    private val context: Context,
    private val songDao: SongDao,
    private val apiService: BaserowApiService,
    private val preferencesRepository: UserPreferencesRepository
) {
    val allSongs: Flow<List<SongWithMeta>> = songDao.observeAllSongs()
    val favoriteSongs: Flow<List<SongWithMeta>> = songDao.observeFavoriteSongs()
    val recentSongs: Flow<List<SongWithMeta>> = songDao.observeRecentSongs()

    suspend fun ensureSeedLoadedIfEmpty() = withContext(Dispatchers.IO) {
        if (songDao.getSongCount() > 0) return@withContext
        runCatching {
            val json = context.assets.open("baserow_songs_seed.json")
                .bufferedReader()
                .use { it.readText() }
            val adapter = BaserowNetworkFactory.createMoshi()
                .adapter(BaserowPageResponse::class.java)
            val parsed = adapter.fromJson(json) ?: return@runCatching
            val entities = parsed.results
                .mapNotNull { BaserowRowMapper.mapRow(it) }
                .map { it.toEntity() }
                .distinctBy { it.baserowId }
            if (entities.isNotEmpty()) {
                songDao.upsertSongs(entities)
            }
        }
    }

    fun observeSong(baserowId: Int): Flow<SongWithMeta?> = songDao.observeSongById(baserowId)

    fun searchOffline(query: String, includeLyrics: Boolean): Flow<List<SongWithMeta>> {
        val cleaned = query.trim().removePrefix("#")
        return songDao.searchSongsOffline(
            query = cleaned,
            exactNum = cleaned,
            includeLyrics = includeLyrics
        )
    }

    suspend fun searchOnline(query: String): Result<List<SongWithMeta>> = withContext(Dispatchers.IO) {
        if (!BaserowNetworkFactory.hasValidToken()) {
            return@withContext Result.failure(IllegalStateException("MISSING_TOKEN"))
        }
        return@withContext try {
            val response = apiService.searchSongs(
                tableId = BuildConfig.BASEROW_TABLE_ID,
                userFieldNames = true,
                query = query.trim()
            )
            val mapped = response.results.mapNotNull { BaserowRowMapper.mapRow(it) }
            val entities = mapped.map { it.toEntity() }
            if (entities.isNotEmpty()) {
                songDao.upsertSongs(entities)
            }
            val resultList = entities.map { entity ->
                val isFav = songDao.isFavorite(entity.baserowId)
                val recent = songDao.getRecentEntry(entity.baserowId)
                SongWithMeta(
                    baserowId = entity.baserowId,
                    songNumber = entity.songNumber,
                    orderRaw = entity.orderRaw,
                    titleOdia = entity.titleOdia,
                    subTitle = entity.subTitle,
                    lyrics = entity.lyrics,
                    category = entity.category,
                    odiaInitial = entity.odiaInitial,
                    isFavorite = isFav,
                    lastViewedAt = recent?.viewedAt,
                    viewCount = recent?.viewCount
                )
            }
            Result.success(resultList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncFromBaserow(
        onProgress: (fetched: Int, total: Int) -> Unit = { _, _ -> }
    ): SyncState = withContext(Dispatchers.IO) {
        if (!BaserowNetworkFactory.hasValidToken()) {
            return@withContext SyncState.MissingToken
        }
        try {
            val collected = mutableListOf<SongEntity>()
            var page = 1
            var hasNext = true
            var totalCount = 0

            while (hasNext && page <= 50) {
                val response = apiService.getSongsPage(
                    tableId = BuildConfig.BASEROW_TABLE_ID,
                    userFieldNames = true,
                    page = page,
                    size = 200
                )
                if (page == 1) {
                    totalCount = response.count
                }
                val pageItems = response.results
                    .mapNotNull { BaserowRowMapper.mapRow(it) }
                    .map { it.toEntity() }
                collected.addAll(pageItems)
                onProgress(collected.size, totalCount)

                hasNext = response.next != null && response.results.isNotEmpty()
                page++
            }

            if (collected.isNotEmpty()) {
                val distinctSongs = collected.distinctBy { it.baserowId }
                songDao.syncAllSongsAtomically(distinctSongs)
                val now = System.currentTimeMillis()
                preferencesRepository.updateSyncMetadata(now, totalCount.coerceAtLeast(distinctSongs.size))
                SyncState.Success(syncedCount = distinctSongs.size, timestamp = now)
            } else {
                val now = System.currentTimeMillis()
                preferencesRepository.updateSyncMetadata(now, 0)
                SyncState.Success(syncedCount = 0, timestamp = now)
            }
        } catch (e: HttpException) {
            when (e.code()) {
                401, 403 -> SyncState.Error("Baserow authentication failed (HTTP ${e.code()}). Please verify your read-only BASEROW_API_TOKEN.")
                404 -> SyncState.Error("Baserow table ${BuildConfig.BASEROW_TABLE_ID} was not found (HTTP 404).")
                else -> SyncState.Error("Baserow server error (HTTP ${e.code()}).")
            }
        } catch (e: IOException) {
            SyncState.Error("Network unavailable. Offline mode active.")
        } catch (e: Exception) {
            SyncState.Error(e.localizedMessage ?: "Failed to sync songs from Baserow.")
        }
    }

    suspend fun checkForUpdates(): Result<UpdateCheckStatus> = withContext(Dispatchers.IO) {
        if (!BaserowNetworkFactory.hasValidToken()) {
            return@withContext Result.failure(IllegalStateException("MISSING_TOKEN"))
        }
        return@withContext try {
            val response = apiService.getSongsPage(
                tableId = BuildConfig.BASEROW_TABLE_ID,
                userFieldNames = true,
                page = 1,
                size = 1
            )
            val localCount = songDao.getSongCount()
            val remoteCount = response.count
            Result.success(
                UpdateCheckStatus(
                    remoteCount = remoteCount,
                    localCount = localCount,
                    hasUpdate = remoteCount > 0 && remoteCount != localCount
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(songId: Int) = withContext(Dispatchers.IO) {
        if (songDao.isFavorite(songId)) {
            songDao.removeFavorite(songId)
        } else {
            songDao.addFavorite(FavoriteEntity(songId = songId))
        }
    }

    suspend fun setFavorite(songId: Int, favorite: Boolean) = withContext(Dispatchers.IO) {
        if (favorite) {
            songDao.addFavorite(FavoriteEntity(songId = songId))
        } else {
            songDao.removeFavorite(songId)
        }
    }

    suspend fun recordSongViewed(songId: Int) = withContext(Dispatchers.IO) {
        val existing = songDao.getRecentEntry(songId)
        val count = (existing?.viewCount ?: 0) + 1
        songDao.upsertRecent(
            RecentSongEntity(
                songId = songId,
                viewedAt = System.currentTimeMillis(),
                viewCount = count
            )
        )
    }

    suspend fun clearRecentHistory() = withContext(Dispatchers.IO) {
        songDao.clearRecentHistory()
    }

    suspend fun clearOfflineSongCache() = withContext(Dispatchers.IO) {
        songDao.clearAllCachedSongs()
    }

    private fun BaserowSongMapped.toEntity(): SongEntity {
        return SongEntity(
            baserowId = baserowId,
            songNumber = songNumber,
            orderRaw = orderRaw,
            titleOdia = titleOdia,
            subTitle = subTitle,
            lyrics = lyrics,
            category = category,
            odiaInitial = extractOdiaInitial(titleOdia)
        )
    }

    companion object {
        fun extractOdiaInitial(title: String): String {
            val trimmed = title.trim()
            for (ch in trimmed) {
                if (ch in '\u0B00'..'\u0B7F') {
                    return ch.toString()
                }
                if (ch.isLetter()) {
                    return ch.uppercaseChar().toString()
                }
            }
            return "#"
        }
    }
}
