package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppLanguage
import com.example.data.local.LyricsFontStyle
import com.example.data.local.SongWithMeta
import com.example.data.local.ThemeMode
import com.example.data.local.UserPreferences
import com.example.data.local.UserPreferencesRepository
import com.example.data.remote.BaserowNetworkFactory
import com.example.data.repository.NetworkMonitor
import com.example.data.repository.SongRepository
import com.example.data.repository.SyncState
import com.example.data.repository.UpdateCheckStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SongSortOrder {
    NUMBER,
    ODIA_TITLE,
    SUBTITLE
}

data class CategoryGroup(
    val key: String,
    val label: String,
    val songs: List<SongWithMeta>
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val repository: SongRepository,
    private val preferencesRepository: UserPreferencesRepository,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesRepository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnlineFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), networkMonitor.isCurrentlyOnline())

    private val _sortOrder = MutableStateFlow(SongSortOrder.NUMBER)
    val sortOrder: StateFlow<SongSortOrder> = _sortOrder.asStateFlow()

    val allSongsRaw: StateFlow<List<SongWithMeta>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sortedSongs: StateFlow<List<SongWithMeta>> = combine(allSongsRaw, _sortOrder) { songs, order ->
        when (order) {
            SongSortOrder.NUMBER -> songs.sortedWith(compareBy({ it.songNumber }, { it.baserowId }))
            SongSortOrder.ODIA_TITLE -> songs.sortedBy { it.titleOdia }
            SongSortOrder.SUBTITLE -> songs.sortedBy { it.subTitle.lowercase() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<SongWithMeta>> = repository.favoriteSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSongs: StateFlow<List<SongWithMeta>> = repository.recentSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _syncState = MutableStateFlow<SyncState>(
        if (BaserowNetworkFactory.hasValidToken()) SyncState.Idle else SyncState.MissingToken
    )
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _updateCheckStatus = MutableStateFlow<UpdateCheckStatus?>(null)
    val updateCheckStatus: StateFlow<UpdateCheckStatus?> = _updateCheckStatus.asStateFlow()

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchIncludeLyrics = MutableStateFlow(false)
    val searchIncludeLyrics: StateFlow<Boolean> = _searchIncludeLyrics.asStateFlow()

    private val _onlineSearchResults = MutableStateFlow<List<SongWithMeta>?>(null)
    val onlineSearchResults: StateFlow<List<SongWithMeta>?> = _onlineSearchResults.asStateFlow()

    private val _isSearchingOnline = MutableStateFlow(false)
    val isSearchingOnline: StateFlow<Boolean> = _isSearchingOnline.asStateFlow()

    val searchResults: StateFlow<List<SongWithMeta>> = combine(
        _searchQuery,
        _searchIncludeLyrics,
        _onlineSearchResults
    ) { query, includeLyrics, onlineResults ->
        Triple(query.trim(), includeLyrics, onlineResults)
    }.flatMapLatest { (query, includeLyrics, onlineResults) ->
        if (query.isEmpty()) {
            flowOf(emptyList())
        } else if (onlineResults != null) {
            flowOf(onlineResults)
        } else {
            repository.searchOffline(query, includeLyrics)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hasBaserowCategories: StateFlow<Boolean> = allSongsRaw
        .combine(flowOf(Unit)) { songs, _ ->
            songs.any { !it.category.isNullOrBlank() }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private var hasAttemptedInitialSync = false

    init {
        viewModelScope.launch {
            repository.ensureSeedLoadedIfEmpty()
            // Auto-sync on initial launch or whenever connectivity returns if enabled
            combine(isOnline, preferences) { online, prefs ->
                online to prefs
            }.collect { (online, prefs) ->
                if (!BaserowNetworkFactory.hasValidToken()) {
                    _syncState.value = SyncState.MissingToken
                    return@collect
                }
                if (online && _syncState.value !is SyncState.Syncing) {
                    val now = System.currentTimeMillis()
                    val stale = now - prefs.lastSyncTimestamp > 6 * 60 * 60 * 1000L
                    val noCachedSongs = allSongsRaw.value.isEmpty()
                    if ((!hasAttemptedInitialSync && (noCachedSongs || prefs.autoSyncEnabled)) ||
                        (prefs.autoSyncEnabled && stale)
                    ) {
                        hasAttemptedInitialSync = true
                        syncSongs(silentIfCached = !noCachedSongs)
                    } else if (!hasAttemptedInitialSync) {
                        hasAttemptedInitialSync = true
                        checkForUpdatesSilently()
                    }
                }
            }
        }
    }

    fun observeSong(songId: Int): Flow<SongWithMeta?> = repository.observeSong(songId)

    fun syncSongs(silentIfCached: Boolean = false) {
        if (_syncState.value is SyncState.Syncing) return
        viewModelScope.launch {
            if (!BaserowNetworkFactory.hasValidToken()) {
                _syncState.value = SyncState.MissingToken
                return@launch
            }
            if (!silentIfCached || allSongsRaw.value.isEmpty()) {
                _syncState.value = SyncState.Syncing(0, 0)
            }
            val result = repository.syncFromBaserow { fetched, total ->
                _syncState.value = SyncState.Syncing(fetched, total)
            }
            _syncState.value = result
            if (result is SyncState.Success) {
                _updateCheckStatus.value = UpdateCheckStatus(
                    remoteCount = result.syncedCount,
                    localCount = result.syncedCount,
                    hasUpdate = false
                )
            }
        }
    }

    fun checkForUpdates(onResult: ((UpdateCheckStatus?) -> Unit)? = null) {
        viewModelScope.launch {
            val res = repository.checkForUpdates()
            val status = res.getOrNull()
            _updateCheckStatus.value = status
            onResult?.invoke(status)
        }
    }

    private suspend fun checkForUpdatesSilently() {
        repository.checkForUpdates().getOrNull()?.let { status ->
            _updateCheckStatus.value = status
        }
    }

    fun dismissUpdateBanner(remoteCount: Int) {
        viewModelScope.launch {
            preferencesRepository.dismissUpdateBanner(remoteCount)
        }
    }

    fun setSortOrder(order: SongSortOrder) {
        _sortOrder.value = order
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _onlineSearchResults.value = null
    }

    fun setSearchIncludeLyrics(include: Boolean) {
        _searchIncludeLyrics.value = include
        _onlineSearchResults.value = null
    }

    fun runOnlineSearch() {
        val q = _searchQuery.value.trim()
        if (q.isEmpty()) return
        viewModelScope.launch {
            _isSearchingOnline.value = true
            val result = repository.searchOnline(q)
            _isSearchingOnline.value = false
            result.onSuccess { list ->
                _onlineSearchResults.value = list
            }
        }
    }

    fun toggleFavorite(songId: Int) {
        viewModelScope.launch {
            repository.toggleFavorite(songId)
        }
    }

    fun setFavorite(songId: Int, favorite: Boolean) {
        viewModelScope.launch {
            repository.setFavorite(songId, favorite)
        }
    }

    fun recordSongViewed(songId: Int) {
        viewModelScope.launch {
            repository.recordSongViewed(songId)
        }
    }

    fun clearRecentHistory() {
        viewModelScope.launch {
            repository.clearRecentHistory()
        }
    }

    fun findSongByNumber(number: Int): SongWithMeta? {
        return allSongsRaw.value.firstOrNull { it.songNumber == number }
            ?: allSongsRaw.value.firstOrNull { it.baserowId == number }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { preferencesRepository.setLanguage(language) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }

    fun setLyricsFontSize(sizeSp: Int) {
        viewModelScope.launch { preferencesRepository.setLyricsFontSize(sizeSp) }
    }

    fun increaseFontSize() {
        val current = preferences.value.lyricsFontSizeSp
        setLyricsFontSize((current + 2).coerceAtMost(34))
    }

    fun decreaseFontSize() {
        val current = preferences.value.lyricsFontSizeSp
        setLyricsFontSize((current - 2).coerceAtLeast(14))
    }

    fun setLyricsFontStyle(style: LyricsFontStyle) {
        viewModelScope.launch { preferencesRepository.setLyricsFontStyle(style) }
    }

    fun toggleReadingMode() {
        val next = !preferences.value.readingMode
        viewModelScope.launch { preferencesRepository.setReadingMode(next) }
    }

    fun setAutoSyncEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setAutoSyncEnabled(enabled) }
    }

    fun clearOfflineCache() {
        viewModelScope.launch {
            repository.clearOfflineSongCache()
            preferencesRepository.updateSyncMetadata(0L, 0)
            _syncState.value = SyncState.Idle
        }
    }

    class Factory(
        private val repository: SongRepository,
        private val preferencesRepository: UserPreferencesRepository,
        private val networkMonitor: NetworkMonitor
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repository, preferencesRepository, networkMonitor) as T
        }
    }
}
