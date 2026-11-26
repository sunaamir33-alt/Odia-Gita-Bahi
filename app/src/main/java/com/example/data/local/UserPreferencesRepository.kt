package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "odia_gita_prefs")

enum class AppLanguage {
    ODIA,
    ENGLISH
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class LyricsFontStyle {
    SANS_ORIYA,
    SERIF_ORIYA
}

data class UserPreferences(
    val language: AppLanguage = AppLanguage.ODIA,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val lyricsFontSizeSp: Int = 20,
    val lyricsFontStyle: LyricsFontStyle = LyricsFontStyle.SERIF_ORIYA,
    val readingMode: Boolean = false,
    val autoSyncEnabled: Boolean = true,
    val lastSyncTimestamp: Long = 0L,
    val lastRemoteCount: Int = 0,
    val dismissedUpdateCount: Int = 0
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LYRICS_FONT_SIZE = intPreferencesKey("lyrics_font_size")
        val LYRICS_FONT_STYLE = stringPreferencesKey("lyrics_font_style")
        val READING_MODE = booleanPreferencesKey("reading_mode")
        val AUTO_SYNC = booleanPreferencesKey("auto_sync")
        val LAST_SYNC_TS = longPreferencesKey("last_sync_ts")
        val LAST_REMOTE_COUNT = intPreferencesKey("last_remote_count")
        val DISMISSED_UPDATE_COUNT = intPreferencesKey("dismissed_update_count")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            language = prefs[Keys.LANGUAGE]?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() }
                ?: AppLanguage.ODIA,
            themeMode = prefs[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            lyricsFontSizeSp = (prefs[Keys.LYRICS_FONT_SIZE] ?: 20).coerceIn(14, 34),
            lyricsFontStyle = prefs[Keys.LYRICS_FONT_STYLE]?.let {
                runCatching { LyricsFontStyle.valueOf(it) }.getOrNull()
            } ?: LyricsFontStyle.SERIF_ORIYA,
            readingMode = prefs[Keys.READING_MODE] ?: false,
            autoSyncEnabled = prefs[Keys.AUTO_SYNC] ?: true,
            lastSyncTimestamp = prefs[Keys.LAST_SYNC_TS] ?: 0L,
            lastRemoteCount = prefs[Keys.LAST_REMOTE_COUNT] ?: 0,
            dismissedUpdateCount = prefs[Keys.DISMISSED_UPDATE_COUNT] ?: 0
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setLyricsFontSize(sizeSp: Int) {
        context.dataStore.edit { it[Keys.LYRICS_FONT_SIZE] = sizeSp.coerceIn(14, 34) }
    }

    suspend fun setLyricsFontStyle(style: LyricsFontStyle) {
        context.dataStore.edit { it[Keys.LYRICS_FONT_STYLE] = style.name }
    }

    suspend fun setReadingMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.READING_MODE] = enabled }
    }

    suspend fun setAutoSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_SYNC] = enabled }
    }

    suspend fun updateSyncMetadata(timestamp: Long, remoteCount: Int) {
        context.dataStore.edit {
            it[Keys.LAST_SYNC_TS] = timestamp
            it[Keys.LAST_REMOTE_COUNT] = remoteCount
        }
    }

    suspend fun dismissUpdateBanner(remoteCount: Int) {
        context.dataStore.edit { it[Keys.DISMISSED_UPDATE_COUNT] = remoteCount }
    }
}
