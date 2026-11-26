package com.example.ui.navigation

object AppRoutes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val ALL_SONGS = "all_songs"
    const val CATEGORIES = "categories"
    const val SEARCH = "search"
    const val SONG_DETAIL = "song_detail/{songId}"
    const val FAVORITES = "favorites"
    const val RECENT = "recent"
    const val SETTINGS = "settings"
    const val ABOUT = "about"

    fun songDetailRoute(songId: Int): String = "song_detail/$songId"
}
