package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.SongWithMeta
import com.example.data.repository.SyncState
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SongCard
import com.example.ui.theme.LocalAppStrings
import com.example.ui.viewmodel.SongSortOrder
import kotlinx.coroutines.launch

@Composable
fun AllSongsScreen(
    songs: List<SongWithMeta>,
    sortOrder: SongSortOrder,
    syncState: SyncState,
    onSelectSortOrder: (SongSortOrder) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSongDetail: (Int) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val quickJumpMarkers = remember(songs, sortOrder) {
        if (songs.size < 15) {
            emptyList()
        } else {
            when (sortOrder) {
                SongSortOrder.NUMBER -> {
                    val step = (songs.size / 6).coerceAtLeast(1)
                    songs.indices.step(step).map { idx ->
                        songs[idx].songNumber.toString() to idx
                    }
                }
                SongSortOrder.ODIA_TITLE -> {
                    val seen = linkedMapOf<String, Int>()
                    songs.forEachIndexed { idx, song ->
                        if (song.odiaInitial !in seen) seen[song.odiaInitial] = idx
                    }
                    seen.entries.take(10).map { it.key to it.value }
                }
                SongSortOrder.SUBTITLE -> {
                    val seen = linkedMapOf<String, Int>()
                    songs.forEachIndexed { idx, song ->
                        val ch = song.subTitle.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "#"
                        if (ch !in seen) seen[ch] = idx
                    }
                    seen.entries.take(10).map { it.key to it.value }
                }
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
        ) {
            // Top bar with search and sync
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.allSongs,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${songs.size} ${strings.totalSongsLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onOpenSearch,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("all_songs_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = strings.searchSongs,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onSyncNow,
                        enabled = syncState !is SyncState.Syncing,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("all_songs_sync_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Sync,
                            contentDescription = strings.syncNow,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            if (syncState is SyncState.Syncing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Sort Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortChip(
                    label = strings.sortByNumber,
                    selected = sortOrder == SongSortOrder.NUMBER,
                    onClick = { onSelectSortOrder(SongSortOrder.NUMBER) },
                    testTag = "sort_number"
                )
                SortChip(
                    label = strings.sortByOdiaTitle,
                    selected = sortOrder == SongSortOrder.ODIA_TITLE,
                    onClick = { onSelectSortOrder(SongSortOrder.ODIA_TITLE) },
                    testTag = "sort_odia"
                )
                SortChip(
                    label = strings.sortBySubtitle,
                    selected = sortOrder == SongSortOrder.SUBTITLE,
                    onClick = { onSelectSortOrder(SongSortOrder.SUBTITLE) },
                    testTag = "sort_subtitle"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (songs.isEmpty()) {
                val isMissingToken = syncState is SyncState.MissingToken
                EmptyStateView(
                    icon = Icons.Rounded.LibraryMusic,
                    title = if (isMissingToken) strings.missingTokenTitle else strings.noSongsYetTitle,
                    message = if (isMissingToken) strings.missingTokenMessage else strings.noSongsYetMessage,
                    actionLabel = if (isMissingToken) null else strings.syncNow,
                    onAction = if (isMissingToken) null else onSyncNow
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag("all_songs_list"),
                        contentPadding = PaddingValues(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = songs,
                            key = { it.baserowId }
                        ) { song ->
                            SongCard(
                                song = song,
                                onClick = { onOpenSongDetail(song.baserowId) },
                                onToggleFavorite = { onToggleFavorite(song.baserowId) }
                            )
                        }
                    }

                    // Fast-Scroll Quick Jump Scrubber
                    if (quickJumpMarkers.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(end = 6.dp, top = 12.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            quickJumpMarkers.forEach { (label, index) ->
                                Surface(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable {
                                            scope.launch {
                                                listState.animateScrollToItem(index)
                                            }
                                        },
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SortChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        modifier = Modifier.testTag(testTag),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
