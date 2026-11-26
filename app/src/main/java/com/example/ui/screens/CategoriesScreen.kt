package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.SongWithMeta
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SongCard
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.NotoSansOriyaFontFamily
import com.example.ui.viewmodel.CategoryGroup

@Composable
fun CategoriesScreen(
    songs: List<SongWithMeta>,
    hasBaserowCategories: Boolean,
    onOpenSongDetail: (Int) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedGroup by remember { mutableStateOf<CategoryGroup?>(null) }

    BackHandler(enabled = selectedGroup != null) {
        selectedGroup = null
    }

    val baserowGroups = remember(songs, hasBaserowCategories) {
        if (!hasBaserowCategories) {
            emptyList()
        } else {
            songs.groupBy { it.category?.takeIf { c -> c.isNotBlank() } ?: "General" }
                .entries
                .sortedBy { it.key }
                .map { CategoryGroup(key = "cat_${it.key}", label = it.key, songs = it.value) }
        }
    }

    val odiaLetterGroups = remember(songs) {
        songs.groupBy { it.odiaInitial }
            .entries
            .sortedBy { it.key }
            .map { CategoryGroup(key = "letter_${it.key}", label = it.key, songs = it.value) }
    }

    val numberRangeGroups = remember(songs) {
        if (songs.isEmpty()) {
            emptyList()
        } else {
            val maxNum = songs.maxOf { it.songNumber }.coerceAtLeast(1)
            val ranges = mutableListOf<CategoryGroup>()
            var start = 1
            while (start <= maxNum) {
                val end = start + 49
                val inRange = songs.filter { it.songNumber in start..end }
                if (inRange.isNotEmpty()) {
                    ranges.add(
                        CategoryGroup(
                            key = "range_${start}_$end",
                            label = "$start – ${inRange.maxOf { it.songNumber }}",
                            songs = inRange
                        )
                    )
                }
                start += 50
            }
            ranges
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
            if (selectedGroup != null) {
                val group = selectedGroup!!
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { selectedGroup = null },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_navigate_back)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = group.label,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = NotoSansOriyaFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${group.songs.size} ${strings.totalSongsLabel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(group.songs, key = { it.baserowId }) { song ->
                        SongCard(
                            song = song,
                            onClick = { onOpenSongDetail(song.baserowId) },
                            onToggleFavorite = { onToggleFavorite(song.baserowId) }
                        )
                    }
                }
            } else {
                Text(
                    text = strings.categories,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )

                if (songs.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Rounded.Category,
                        title = strings.noSongsYetTitle,
                        message = strings.noSongsYetMessage,
                        actionLabel = strings.syncNow,
                        onAction = onSyncNow
                    )
                } else {
                    val tabs = buildList {
                        if (hasBaserowCategories) add(strings.byBaserowCategory)
                        add(strings.byOdiaLetter)
                        add(strings.byNumberRange)
                    }
                    val safeTab = selectedTab.coerceIn(0, tabs.lastIndex)

                    TabRow(
                        selectedTabIndex = safeTab,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = safeTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }

                    if (!hasBaserowCategories) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.noCategoryColumnNote,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    val activeGroups = when {
                        hasBaserowCategories && safeTab == 0 -> baserowGroups
                        (hasBaserowCategories && safeTab == 1) || (!hasBaserowCategories && safeTab == 0) -> odiaLetterGroups
                        else -> numberRangeGroups
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 148.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("categories_grid"),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(activeGroups, key = { it.key }) { group ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { selectedGroup = group }
                                    .testTag("category_card_${group.key}"),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                                            RoundedCornerShape(18.dp)
                                        )
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = group.label.take(3),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = NotoSansOriyaFontFamily,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = group.label,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = NotoSansOriyaFontFamily
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${group.songs.size}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
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
