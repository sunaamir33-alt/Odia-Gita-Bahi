package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.SongWithMeta
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SongCard
import com.example.ui.theme.LocalAppStrings
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(
    favoriteSongs: List<SongWithMeta>,
    onOpenSongDetail: (Int) -> Unit,
    onSetFavorite: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = strings.favorites,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${favoriteSongs.size} ${strings.totalSongsLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (favoriteSongs.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Rounded.FavoriteBorder,
                        title = strings.noFavoritesTitle,
                        message = strings.noFavoritesMessage
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("favorites_list"),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(favoriteSongs, key = { it.baserowId }) { song ->
                            SongCard(
                                song = song,
                                onClick = { onOpenSongDetail(song.baserowId) },
                                onToggleFavorite = {
                                    onSetFavorite(song.baserowId, false)
                                    scope.launch {
                                        val res = snackbarHostState.showSnackbar(
                                            message = strings.removedFromFavorites,
                                            actionLabel = strings.undo,
                                            duration = SnackbarDuration.Short
                                        )
                                        if (res == SnackbarResult.ActionPerformed) {
                                            onSetFavorite(song.baserowId, true)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
