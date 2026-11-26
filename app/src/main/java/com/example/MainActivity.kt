package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.navigation.AppRoutes
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AllSongsScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RecentSongsScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SongDetailScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.OdiaGitaBahiTheme
import com.example.ui.theme.stringsFor
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        val app = application as OdiaGitaBahiApp
        MainViewModel.Factory(
            repository = app.container.songRepository,
            preferencesRepository = app.container.preferencesRepository,
            networkMonitor = app.container.networkMonitor
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OdiaGitaBahiRoot(viewModel = viewModel)
        }
    }
}

private data class TopNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun OdiaGitaBahiRoot(viewModel: MainViewModel) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val allSongs by viewModel.sortedSongs.collectAsStateWithLifecycle()
    val allSongsRaw by viewModel.allSongsRaw.collectAsStateWithLifecycle()
    val favoriteSongs by viewModel.favoriteSongs.collectAsStateWithLifecycle()
    val recentSongs by viewModel.recentSongs.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val updateCheckStatus by viewModel.updateCheckStatus.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val hasBaserowCategories by viewModel.hasBaserowCategories.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchIncludeLyrics by viewModel.searchIncludeLyrics.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearchingOnline by viewModel.isSearchingOnline.collectAsStateWithLifecycle()

    val appStrings = stringsFor(preferences.language)

    OdiaGitaBahiTheme(themeMode = preferences.themeMode) {
        CompositionLocalProvider(LocalAppStrings provides appStrings) {
            val navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route

            val topNavItems = listOf(
                TopNavItem(AppRoutes.HOME, appStrings.home, Icons.Rounded.Home, Icons.Outlined.Home, "nav_home"),
                TopNavItem(AppRoutes.ALL_SONGS, appStrings.allSongs, Icons.Rounded.LibraryMusic, Icons.Outlined.LibraryMusic, "nav_all_songs"),
                TopNavItem(AppRoutes.CATEGORIES, appStrings.categories, Icons.Rounded.Category, Icons.Outlined.Category, "nav_categories"),
                TopNavItem(AppRoutes.FAVORITES, appStrings.favorites, Icons.Rounded.Favorite, Icons.Outlined.FavoriteBorder, "nav_favorites"),
                TopNavItem(AppRoutes.SETTINGS, appStrings.settings, Icons.Rounded.Settings, Icons.Outlined.Settings, "nav_settings")
            )
            val showTopLevelNav = currentRoute in topNavItems.map { it.route }

            val navigateTopLevel: (String) -> Unit = { route ->
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavigationRail = maxWidth >= 600.dp

                Row(modifier = Modifier.fillMaxSize()) {
                    if (showTopLevelNav && useNavigationRail) {
                        NavigationRail(
                            modifier = Modifier.fillMaxHeight(),
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            topNavItems.forEach { item ->
                                val selected = currentRoute == item.route
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTopLevel(item.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.label,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }

                    Scaffold(
                        modifier = Modifier.weight(1f),
                        contentWindowInsets = if (showTopLevelNav) {
                            WindowInsets.safeDrawing
                        } else {
                            WindowInsets(0, 0, 0, 0)
                        },
                        bottomBar = {
                            if (showTopLevelNav && !useNavigationRail) {
                                NavigationBar(
                                    windowInsets = WindowInsets.navigationBars,
                                    containerColor = MaterialTheme.colorScheme.surface
                                ) {
                                    topNavItems.forEach { item ->
                                        val selected = currentRoute == item.route
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = { navigateTopLevel(item.route) },
                                            icon = {
                                                Icon(
                                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                    contentDescription = item.label
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = item.label,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            },
                                            modifier = Modifier.testTag(item.testTag)
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = AppRoutes.SPLASH,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            composable(AppRoutes.SPLASH) {
                                SplashScreen(
                                    onSplashFinished = {
                                        navController.navigate(AppRoutes.HOME) {
                                            popUpTo(AppRoutes.SPLASH) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable(AppRoutes.HOME) {
                                HomeScreen(
                                    totalSongsCount = allSongsRaw.size,
                                    favoritesCount = favoriteSongs.size,
                                    recentSongs = recentSongs,
                                    isOnline = isOnline,
                                    syncState = syncState,
                                    updateCheckStatus = updateCheckStatus,
                                    preferences = preferences,
                                    onOpenSearch = { navController.navigate(AppRoutes.SEARCH) },
                                    onOpenAllSongs = { navigateTopLevel(AppRoutes.ALL_SONGS) },
                                    onOpenCategories = { navigateTopLevel(AppRoutes.CATEGORIES) },
                                    onOpenFavorites = { navigateTopLevel(AppRoutes.FAVORITES) },
                                    onOpenRecent = { navController.navigate(AppRoutes.RECENT) },
                                    onOpenSettings = { navigateTopLevel(AppRoutes.SETTINGS) },
                                    onOpenAbout = { navController.navigate(AppRoutes.ABOUT) },
                                    onOpenSongDetail = { songId ->
                                        navController.navigate(AppRoutes.songDetailRoute(songId))
                                    },
                                    onJumpToSongNumber = { number ->
                                        val match = viewModel.findSongByNumber(number)
                                        if (match != null) {
                                            navController.navigate(AppRoutes.songDetailRoute(match.baserowId))
                                            true
                                        } else {
                                            false
                                        }
                                    },
                                    onToggleFavorite = viewModel::toggleFavorite,
                                    onSyncNow = { viewModel.syncSongs(silentIfCached = false) },
                                    onDismissUpdateBanner = viewModel::dismissUpdateBanner
                                )
                            }

                            composable(AppRoutes.ALL_SONGS) {
                                AllSongsScreen(
                                    songs = allSongs,
                                    sortOrder = sortOrder,
                                    syncState = syncState,
                                    onSelectSortOrder = viewModel::setSortOrder,
                                    onOpenSearch = { navController.navigate(AppRoutes.SEARCH) },
                                    onOpenSongDetail = { songId ->
                                        navController.navigate(AppRoutes.songDetailRoute(songId))
                                    },
                                    onToggleFavorite = viewModel::toggleFavorite,
                                    onSyncNow = { viewModel.syncSongs(silentIfCached = false) }
                                )
                            }

                            composable(AppRoutes.CATEGORIES) {
                                CategoriesScreen(
                                    songs = allSongsRaw,
                                    hasBaserowCategories = hasBaserowCategories,
                                    onOpenSongDetail = { songId ->
                                        navController.navigate(AppRoutes.songDetailRoute(songId))
                                    },
                                    onToggleFavorite = viewModel::toggleFavorite,
                                    onSyncNow = { viewModel.syncSongs(silentIfCached = false) }
                                )
                            }

                            composable(AppRoutes.SEARCH) {
                                SearchScreen(
                                    query = searchQuery,
                                    includeLyrics = searchIncludeLyrics,
                                    results = searchResults,
                                    isOnline = isOnline,
                                    isSearchingOnline = isSearchingOnline,
                                    onQueryChange = viewModel::updateSearchQuery,
                                    onToggleIncludeLyrics = viewModel::setSearchIncludeLyrics,
                                    onRunOnlineSearch = viewModel::runOnlineSearch,
                                    onOpenSongDetail = { songId ->
                                        navController.navigate(AppRoutes.songDetailRoute(songId))
                                    },
                                    onToggleFavorite = viewModel::toggleFavorite,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            composable(
                                route = AppRoutes.SONG_DETAIL,
                                arguments = listOf(navArgument("songId") { type = NavType.IntType })
                            ) { entry ->
                                val songId = entry.arguments?.getInt("songId") ?: -1
                                val song by viewModel.observeSong(songId)
                                    .collectAsStateWithLifecycle(initialValue = allSongsRaw.firstOrNull { it.baserowId == songId })

                                val orderedSongs = allSongsRaw
                                val currentIdx = orderedSongs.indexOfFirst { it.baserowId == songId }
                                val prevId = if (currentIdx > 0) orderedSongs[currentIdx - 1].baserowId else null
                                val nextId = if (currentIdx in 0 until orderedSongs.lastIndex) {
                                    orderedSongs[currentIdx + 1].baserowId
                                } else null

                                SongDetailScreen(
                                    song = song,
                                    previousSongId = prevId,
                                    nextSongId = nextId,
                                    preferences = preferences,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToSong = { targetId ->
                                        navController.navigate(AppRoutes.songDetailRoute(targetId)) {
                                            popUpTo(AppRoutes.SONG_DETAIL) { inclusive = true }
                                        }
                                    },
                                    onToggleFavorite = viewModel::toggleFavorite,
                                    onIncreaseFontSize = viewModel::increaseFontSize,
                                    onDecreaseFontSize = viewModel::decreaseFontSize,
                                    onToggleReadingMode = viewModel::toggleReadingMode,
                                    onSongOpened = viewModel::recordSongViewed
                                )
                            }

                            composable(AppRoutes.FAVORITES) {
                                FavoritesScreen(
                                    favoriteSongs = favoriteSongs,
                                    onOpenSongDetail = { songId ->
                                        navController.navigate(AppRoutes.songDetailRoute(songId))
                                    },
                                    onSetFavorite = viewModel::setFavorite
                                )
                            }

                            composable(AppRoutes.RECENT) {
                                RecentSongsScreen(
                                    recentSongs = recentSongs,
                                    onOpenSongDetail = { songId ->
                                        navController.navigate(AppRoutes.songDetailRoute(songId))
                                    },
                                    onToggleFavorite = viewModel::toggleFavorite,
                                    onClearHistory = viewModel::clearRecentHistory,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            composable(AppRoutes.SETTINGS) {
                                SettingsScreen(
                                    preferences = preferences,
                                    totalCachedSongs = allSongsRaw.size,
                                    syncState = syncState,
                                    onSetLanguage = viewModel::setLanguage,
                                    onSetThemeMode = viewModel::setThemeMode,
                                    onSetFontSize = viewModel::setLyricsFontSize,
                                    onSetFontStyle = viewModel::setLyricsFontStyle,
                                    onToggleReadingMode = viewModel::toggleReadingMode,
                                    onSetAutoSync = viewModel::setAutoSyncEnabled,
                                    onSyncNow = { viewModel.syncSongs(silentIfCached = false) },
                                    onOpenAbout = { navController.navigate(AppRoutes.ABOUT) }
                                )
                            }

                            composable(AppRoutes.ABOUT) {
                                AboutScreen(
                                    totalSongsCount = allSongsRaw.size,
                                    updateCheckStatus = updateCheckStatus,
                                    onCheckUpdates = { viewModel.checkForUpdates() },
                                    onSyncNow = { viewModel.syncSongs(silentIfCached = false) },
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
