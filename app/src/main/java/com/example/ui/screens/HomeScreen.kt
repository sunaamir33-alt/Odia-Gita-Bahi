package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.SongWithMeta
import com.example.data.local.UserPreferences
import com.example.data.repository.SyncState
import com.example.data.repository.UpdateCheckStatus
import com.example.ui.components.ConnectivityStatusPill
import com.example.ui.components.SongCard
import com.example.ui.theme.HeartPink
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.NotoSansOriyaFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.PurpleDeep
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SyncGreen

@Composable
fun HomeScreen(
    totalSongsCount: Int,
    favoritesCount: Int,
    recentSongs: List<SongWithMeta>,
    isOnline: Boolean,
    syncState: SyncState,
    updateCheckStatus: UpdateCheckStatus?,
    preferences: UserPreferences,
    onOpenSearch: () -> Unit,
    onOpenAllSongs: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenRecent: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenSongDetail: (Int) -> Unit,
    onJumpToSongNumber: (Int) -> Boolean,
    onToggleFavorite: (Int) -> Unit,
    onSyncNow: () -> Unit,
    onDismissUpdateBanner: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var jumpNumberInput by remember { mutableStateOf("") }
    var jumpNotFoundMessage by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Brand Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_odia_gita_icon),
                            contentDescription = stringResource(R.string.content_desc_app_logo),
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.appTitle,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = NotoSansOriyaFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = strings.appSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ConnectivityStatusPill(isOnline = isOnline, syncState = syncState)
                        IconButton(
                            onClick = onOpenAbout,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("home_about_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = strings.about,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Search Bar Trigger
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(onClick = onOpenSearch)
                        .testTag("home_search_bar")
                        .minimumInteractiveComponentSize(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(18.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = strings.searchSongs,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = strings.searchHint,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Hero Counter & Offline Sync Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PurpleDeep, PurplePrimary)
                            )
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_home_hero_banner),
                        contentDescription = stringResource(R.string.content_desc_hero_banner),
                        modifier = Modifier
                            .matchParentSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.42f
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        PurpleDeep.copy(alpha = 0.88f),
                                        PurplePrimary.copy(alpha = 0.45f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = strings.totalSongsLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PureWhite.copy(alpha = 0.86f)
                                )
                                Text(
                                    text = totalSongsCount.toString(),
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontFamily = PlusJakartaFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = PureWhite
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = PureWhite.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = if (totalSongsCount > 0) strings.offlineReady else strings.offlineLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PureWhite,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onOpenAllSongs,
                                modifier = Modifier.testTag("hero_all_songs_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PureWhite,
                                    contentColor = PurpleDeep
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LibraryMusic,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.allSongs)
                            }

                            OutlinedButton(
                                onClick = onSyncNow,
                                enabled = syncState !is SyncState.Syncing,
                                modifier = Modifier.testTag("hero_sync_button"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = PureWhite
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    PureWhite.copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                if (syncState is SyncState.Syncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = PureWhite,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Sync,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.syncNow)
                            }
                        }
                    }
                }
            }

            // Missing Token (when empty) or Sync Error Notice
            if ((syncState is SyncState.MissingToken && totalSongsCount == 0) || syncState is SyncState.Error) {
                item {
                    val isMissingToken = syncState is SyncState.MissingToken
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WarningAmber,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isMissingToken) strings.missingTokenTitle else "Sync Notice",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = if (isMissingToken) {
                                        strings.missingTokenMessage
                                    } else {
                                        (syncState as SyncState.Error).message
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // App Update Notification Banner
            if (updateCheckStatus != null &&
                updateCheckStatus.hasUpdate &&
                updateCheckStatus.remoteCount != preferences.dismissedUpdateCount
            ) {
                item {
                    AnimatedVisibility(visible = true) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.NewReleases,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = strings.updateAvailableTitle,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = String.format(
                                        strings.updateAvailableDesc,
                                        updateCheckStatus.remoteCount,
                                        updateCheckStatus.localCount
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { onDismissUpdateBanner(updateCheckStatus.remoteCount) }) {
                                        Text(strings.close)
                                    }
                                    TextButton(onClick = onSyncNow) {
                                        Text(strings.syncNow)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Jump by Song Number
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                                RoundedCornerShape(18.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Text(
                            text = strings.jumpToSongNumber,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = jumpNumberInput,
                                onValueChange = { input ->
                                    jumpNumberInput = input.filter { it.isDigit() }.take(4)
                                    jumpNotFoundMessage = false
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("jump_song_number_input"),
                                placeholder = { Text("1 - ${totalSongsCount.coerceAtLeast(255)}") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Go
                                ),
                                keyboardActions = KeyboardActions(
                                    onGo = {
                                        val num = jumpNumberInput.toIntOrNull()
                                        if (num == null || !onJumpToSongNumber(num)) {
                                            jumpNotFoundMessage = true
                                        }
                                    }
                                ),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                            Button(
                                onClick = {
                                    val num = jumpNumberInput.toIntOrNull()
                                    if (num == null || !onJumpToSongNumber(num)) {
                                        jumpNotFoundMessage = true
                                    }
                                },
                                modifier = Modifier
                                    .height(54.dp)
                                    .testTag("jump_song_go_button"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(strings.goButton)
                            }
                        }
                        if (jumpNotFoundMessage) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = strings.noSearchResultsTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Dashboard Feature Grid (2-column cards)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashboardTileCard(
                            title = strings.allSongs,
                            subtitle = "$totalSongsCount",
                            icon = Icons.Rounded.LibraryMusic,
                            accentColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                            onClick = onOpenAllSongs,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tile_all_songs")
                        )
                        DashboardTileCard(
                            title = strings.favorites,
                            subtitle = "$favoritesCount",
                            icon = Icons.Rounded.Favorite,
                            accentColor = HeartPink,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            onClick = onOpenFavorites,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tile_favorites")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashboardTileCard(
                            title = strings.categories,
                            subtitle = strings.byOdiaLetter,
                            icon = Icons.Rounded.Category,
                            accentColor = MaterialTheme.colorScheme.secondary,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            onClick = onOpenCategories,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tile_categories")
                        )
                        DashboardTileCard(
                            title = strings.recent,
                            subtitle = "${recentSongs.size}",
                            icon = Icons.Rounded.History,
                            accentColor = MaterialTheme.colorScheme.tertiary,
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                            onClick = onOpenRecent,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tile_recent")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashboardTileCard(
                            title = strings.downloadOffline,
                            subtitle = if (totalSongsCount > 0) strings.offlineReady else strings.syncNow,
                            icon = Icons.Rounded.CloudDownload,
                            accentColor = SyncGreen,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            onClick = onSyncNow,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tile_offline_sync")
                        )
                        DashboardTileCard(
                            title = strings.settings,
                            subtitle = strings.languageLabel,
                            icon = Icons.Rounded.Settings,
                            accentColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tile_settings")
                        )
                    }
                }
            }

            // Recent Preview Strip
            if (recentSongs.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.recent,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            TextButton(onClick = onOpenRecent) {
                                Text(strings.goButton)
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null
                                )
                            }
                        }

                        recentSongs.take(3).forEach { song ->
                            SongCard(
                                song = song,
                                onClick = { onOpenSongDetail(song.baserowId) },
                                onToggleFavorite = { onToggleFavorite(song.baserowId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardTileCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = NotoSansOriyaFontFamily,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
