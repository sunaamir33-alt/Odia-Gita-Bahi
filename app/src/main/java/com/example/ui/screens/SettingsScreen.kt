package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.local.AppLanguage
import com.example.data.local.LyricsFontStyle
import com.example.data.local.ThemeMode
import com.example.data.local.UserPreferences
import com.example.data.repository.SyncState
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.NotoSansOriyaFontFamily
import com.example.ui.theme.NotoSerifOriyaFontFamily
import java.text.DateFormat
import java.util.Date

@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    totalCachedSongs: Int,
    syncState: SyncState,
    onSetLanguage: (AppLanguage) -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetFontSize: (Int) -> Unit,
    onSetFontStyle: (LyricsFontStyle) -> Unit,
    onToggleReadingMode: () -> Unit,
    onSetAutoSync: (Boolean) -> Unit,
    onSyncNow: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var showPrivacyDialog by remember { mutableStateOf(false) }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }

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
            item {
                Text(
                    text = strings.settings,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Language Selector (Odia / English)
            item {
                SettingsCard(title = strings.languageLabel, icon = Icons.Rounded.Language) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = preferences.language == AppLanguage.ODIA,
                            onClick = { onSetLanguage(AppLanguage.ODIA) },
                            label = { Text("ଓଡ଼ିଆ (Odia)") },
                            modifier = Modifier.testTag("settings_lang_odia")
                        )
                        FilterChip(
                            selected = preferences.language == AppLanguage.ENGLISH,
                            onClick = { onSetLanguage(AppLanguage.ENGLISH) },
                            label = { Text("English") },
                            modifier = Modifier.testTag("settings_lang_english")
                        )
                    }
                }
            }

            // Theme Mode (System / Light / Dark)
            item {
                SettingsCard(title = strings.themeLabel, icon = Icons.Rounded.DarkMode) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = preferences.themeMode == ThemeMode.SYSTEM,
                            onClick = { onSetThemeMode(ThemeMode.SYSTEM) },
                            label = { Text(strings.themeSystem) },
                            modifier = Modifier.testTag("settings_theme_system")
                        )
                        FilterChip(
                            selected = preferences.themeMode == ThemeMode.LIGHT,
                            onClick = { onSetThemeMode(ThemeMode.LIGHT) },
                            label = { Text(strings.themeLight) },
                            modifier = Modifier.testTag("settings_theme_light")
                        )
                        FilterChip(
                            selected = preferences.themeMode == ThemeMode.DARK,
                            onClick = { onSetThemeMode(ThemeMode.DARK) },
                            label = { Text(strings.themeDark) },
                            modifier = Modifier.testTag("settings_theme_dark")
                        )
                    }
                }
            }

            // Odia Typography & Font Size
            item {
                SettingsCard(title = strings.fontStyleLabel, icon = Icons.Rounded.FontDownload) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = preferences.lyricsFontStyle == LyricsFontStyle.SERIF_ORIYA,
                                onClick = { onSetFontStyle(LyricsFontStyle.SERIF_ORIYA) },
                                label = { Text(strings.fontSerif) }
                            )
                            FilterChip(
                                selected = preferences.lyricsFontStyle == LyricsFontStyle.SANS_ORIYA,
                                onClick = { onSetFontStyle(LyricsFontStyle.SANS_ORIYA) },
                                label = { Text(strings.fontSans) }
                            )
                        }

                        Text(
                            text = "${strings.fontSize}: ${preferences.lyricsFontSizeSp}sp",
                            style = MaterialTheme.typography.labelLarge
                        )

                        Slider(
                            value = preferences.lyricsFontSizeSp.toFloat(),
                            onValueChange = { onSetFontSize(it.toInt()) },
                            valueRange = 14f..34f,
                            steps = 9,
                            modifier = Modifier.testTag("settings_font_slider")
                        )

                        val previewFamily = if (preferences.lyricsFontStyle == LyricsFontStyle.SERIF_ORIYA) {
                            NotoSerifOriyaFontFamily
                        } else {
                            NotoSansOriyaFontFamily
                        }
                        Text(
                            text = "ଆଉ ରାତ୍ରି ନାହିଁ ଆଉ କ୍ଲାନ୍ତ ନାହିଁ\nବିଜୟୀ ଯୀଶୁ ଉଠିଛି ହସି",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = previewFamily,
                                fontSize = preferences.lyricsFontSizeSp.sp,
                                lineHeight = (preferences.lyricsFontSizeSp * 1.55f).sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Reading Mode & Auto-Sync Toggles
            item {
                SettingsCard(title = strings.readingMode, icon = Icons.Rounded.AutoStories) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.readingMode,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = preferences.readingMode,
                                onCheckedChange = { onToggleReadingMode() },
                                modifier = Modifier.testTag("settings_reading_mode_switch")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.autoSyncLabel,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = strings.autoSyncDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = preferences.autoSyncEnabled,
                                onCheckedChange = onSetAutoSync,
                                modifier = Modifier.testTag("settings_auto_sync_switch")
                            )
                        }
                    }
                }
            }

            // Baserow Database Status & Offline Sync
            item {
                SettingsCard(title = strings.databaseStatusLabel, icon = Icons.Rounded.CloudDone) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Baserow Table ID: ${BuildConfig.BASEROW_TABLE_ID}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${strings.totalSongsLabel}: $totalCachedSongs",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        val syncText = if (preferences.lastSyncTimestamp > 0L) {
                            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                                .format(Date(preferences.lastSyncTimestamp))
                        } else {
                            strings.neverSynced
                        }
                        Text(
                            text = "${strings.lastSyncedLabel}: $syncText",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (syncState is SyncState.MissingToken) {
                            Text(
                                text = strings.missingTokenMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = onSyncNow,
                            enabled = syncState !is SyncState.Syncing && syncState !is SyncState.MissingToken,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("settings_sync_now_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Sync,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.syncNow)
                        }
                    }
                }
            }

            // About & Privacy Policy
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenAbout,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_about_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.Info, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.about)
                    }
                    OutlinedButton(
                        onClick = { showPrivacyDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_privacy_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.PrivacyTip, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.privacyPolicy)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                    RoundedCornerShape(20.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}
