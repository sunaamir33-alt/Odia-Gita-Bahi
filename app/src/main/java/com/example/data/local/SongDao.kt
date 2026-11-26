package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Query(
        """
        SELECT s.baserowId, s.songNumber, s.orderRaw, s.titleOdia, s.subTitle, s.lyrics,
               s.category, s.odiaInitial,
               CASE WHEN f.songId IS NOT NULL THEN 1 ELSE 0 END AS isFavorite,
               r.viewedAt AS lastViewedAt,
               r.viewCount AS viewCount
        FROM songs s
        LEFT JOIN favorites f ON s.baserowId = f.songId
        LEFT JOIN recent_songs r ON s.baserowId = r.songId
        ORDER BY s.songNumber ASC, s.baserowId ASC
        """
    )
    fun observeAllSongs(): Flow<List<SongWithMeta>>

    @Query(
        """
        SELECT s.baserowId, s.songNumber, s.orderRaw, s.titleOdia, s.subTitle, s.lyrics,
               s.category, s.odiaInitial,
               CASE WHEN f.songId IS NOT NULL THEN 1 ELSE 0 END AS isFavorite,
               r.viewedAt AS lastViewedAt,
               r.viewCount AS viewCount
        FROM songs s
        INNER JOIN favorites f ON s.baserowId = f.songId
        LEFT JOIN recent_songs r ON s.baserowId = r.songId
        ORDER BY f.addedAt DESC
        """
    )
    fun observeFavoriteSongs(): Flow<List<SongWithMeta>>

    @Query(
        """
        SELECT s.baserowId, s.songNumber, s.orderRaw, s.titleOdia, s.subTitle, s.lyrics,
               s.category, s.odiaInitial,
               CASE WHEN f.songId IS NOT NULL THEN 1 ELSE 0 END AS isFavorite,
               r.viewedAt AS lastViewedAt,
               r.viewCount AS viewCount
        FROM songs s
        INNER JOIN recent_songs r ON s.baserowId = r.songId
        LEFT JOIN favorites f ON s.baserowId = f.songId
        ORDER BY r.viewedAt DESC
        LIMIT 50
        """
    )
    fun observeRecentSongs(): Flow<List<SongWithMeta>>

    @Query(
        """
        SELECT s.baserowId, s.songNumber, s.orderRaw, s.titleOdia, s.subTitle, s.lyrics,
               s.category, s.odiaInitial,
               CASE WHEN f.songId IS NOT NULL THEN 1 ELSE 0 END AS isFavorite,
               r.viewedAt AS lastViewedAt,
               r.viewCount AS viewCount
        FROM songs s
        LEFT JOIN favorites f ON s.baserowId = f.songId
        LEFT JOIN recent_songs r ON s.baserowId = r.songId
        WHERE s.baserowId = :baserowId
        LIMIT 1
        """
    )
    fun observeSongById(baserowId: Int): Flow<SongWithMeta?>

    @Query(
        """
        SELECT s.baserowId, s.songNumber, s.orderRaw, s.titleOdia, s.subTitle, s.lyrics,
               s.category, s.odiaInitial,
               CASE WHEN f.songId IS NOT NULL THEN 1 ELSE 0 END AS isFavorite,
               r.viewedAt AS lastViewedAt,
               r.viewCount AS viewCount
        FROM songs s
        LEFT JOIN favorites f ON s.baserowId = f.songId
        LEFT JOIN recent_songs r ON s.baserowId = r.songId
        WHERE CAST(s.songNumber AS TEXT) = :exactNum
           OR s.titleOdia LIKE '%' || :query || '%'
           OR s.subTitle LIKE '%' || :query || '%'
           OR (:includeLyrics = 1 AND s.lyrics LIKE '%' || :query || '%')
        ORDER BY
           CASE WHEN CAST(s.songNumber AS TEXT) = :exactNum THEN 0 ELSE 1 END,
           s.songNumber ASC
        """
    )
    fun searchSongsOffline(query: String, exactNum: String, includeLyrics: Boolean): Flow<List<SongWithMeta>>

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSongs(songs: List<SongEntity>)

    @Query("DELETE FROM songs WHERE baserowId NOT IN (:validIds)")
    suspend fun deleteSongsNotIn(validIds: List<Int>)

    @Query("DELETE FROM songs")
    suspend fun clearAllCachedSongs()

    @Transaction
    suspend fun syncAllSongsAtomically(songs: List<SongEntity>) {
        if (songs.isEmpty()) return
        upsertSongs(songs)
        deleteSongsNotIn(songs.map { it.baserowId })
    }

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    suspend fun isFavorite(songId: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE songId = :songId")
    suspend fun removeFavorite(songId: Int)

    @Query("SELECT * FROM recent_songs WHERE songId = :songId LIMIT 1")
    suspend fun getRecentEntry(songId: Int): RecentSongEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecent(recent: RecentSongEntity)

    @Query("DELETE FROM recent_songs")
    suspend fun clearRecentHistory()
}
