package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.TextDecrease
import androidx.compose.material.icons.rounded.TextIncrease
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.LyricsFontStyle
import com.example.data.local.SongWithMeta
import com.example.data.local.ThemeMode
import com.example.data.local.UserPreferences
import com.example.ui.theme.HeartPink
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.NotoSansOriyaFontFamily
import com.example.ui.theme.NotoSerifOriyaFontFamily
import com.example.ui.theme.ReadingInkDark
import com.example.ui.theme.ReadingInkLight
import com.example.ui.theme.ReadingPaperDark
import com.example.ui.theme.ReadingPaperLight
import kotlinx.coroutines.launch

@Composable
fun SongDetailScreen(
    song: SongWithMeta?,
    previousSongId: Int?,
    nextSongId: Int?,
    preferences: UserPreferences,
    onNavigateBack: () -> Unit,
    onNavigateToSong: (Int) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onIncreaseFontSize: () -> Unit,
    onDecreaseFontSize: () -> Unit,
    onToggleReadingMode: () -> Unit,
    onSongOpened: (Int) -> Unit
) {
    val strings = LocalAppStrings.current
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(song?.baserowId) {
        song?.baserowId?.let { onSongOpened(it) }
    }

    val isDark = when (preferences.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val backgroundColor = when {
        preferences.readingMode && isDark -> ReadingPaperDark
        preferences.readingMode && !isDark -> ReadingPaperLight
        else -> MaterialTheme.colorScheme.background
    }
    val textColor = when {
        preferences.readingMode && isDark -> ReadingInkDark
        preferences.readingMode && !isDark -> ReadingInkLight
        else -> MaterialTheme.colorScheme.onBackground
    }
    val lyricsFontFamily = when (preferences.lyricsFontStyle) {
        LyricsFontStyle.SANS_ORIYA -> NotoSansOriyaFontFamily
        LyricsFontStyle.SERIF_ORIYA -> NotoSerifOriyaFontFamily
    }

    Scaffold(
        containerColor = backgroundColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                color = if (preferences.readingMode) backgroundColor else MaterialTheme.colorScheme.surface,
                tonalElevation = if (preferences.readingMode) 0.dp else 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("detail_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.content_desc_navigate_back),
                                tint = textColor
                            )
                        }
                        if (song != null) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${strings.songNumberPrefix}${song.songNumber}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    if (song != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onDecreaseFontSize,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("detail_font_decrease")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.TextDecrease,
                                    contentDescription = "Decrease font size",
                                    tint = textColor
                                )
                            }
                            Text(
                                text = "${preferences.lyricsFontSizeSp}",
                                style = MaterialTheme.typography.labelMedium,
                                color = textColor.copy(alpha = 0.75f)
                            )
                            IconButton(
                                onClick = onIncreaseFontSize,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("detail_font_increase")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.TextIncrease,
                                    contentDescription = "Increase font size",
                                    tint = textColor
                                )
                            }
                            IconButton(
                                onClick = onToggleReadingMode,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("detail_reading_mode")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoStories,
                                    contentDescription = strings.readingMode,
                                    tint = if (preferences.readingMode) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        textColor
                                    }
                                )
                            }
                            IconButton(
                                onClick = { onToggleFavorite(song.baserowId) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("detail_favorite_button")
                            ) {
                                Icon(
                                    imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = stringResource(R.string.content_desc_favorite_toggle),
                                    tint = if (song.isFavorite) HeartPink else textColor
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (song != null) {
                Surface(
                    color = if (preferences.readingMode) backgroundColor else MaterialTheme.colorScheme.surface,
                    tonalElevation = if (preferences.readingMode) 0.dp else 4.dp
                ) {
                    Column(modifier = Modifier.navigationBarsPadding()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalButton(
                                onClick = { previousSongId?.let(onNavigateToSong) },
                                enabled = previousSongId != null,
                                modifier = Modifier.testTag("detail_prev_song"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(strings.previousSong)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        val fullText = buildString {
                                            append("${song.songNumber}. ${song.titleOdia}\n")
                                            if (song.subTitle.isNotBlank()) append("(${song.subTitle})\n")
                                            append("\n${song.lyrics}")
                                        }
                                        clipboardManager.setText(AnnotatedString(fullText))
                                        scope.launch {
                                            snackbarHostState.showSnackbar(strings.lyricsCopied)
                                        }
                                    },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("detail_copy_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ContentCopy,
                                        contentDescription = strings.copyLyrics,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val shareText = buildString {
                                            append("🎵 ${song.songNumber}. ${song.titleOdia}\n")
                                            if (song.subTitle.isNotBlank()) append("${song.subTitle}\n")
                                            append("\n${song.lyrics}\n\n— Odia Gita Bahi")
                                        }
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, song.titleOdia)
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                        }
                                        context.startActivity(
                                            Intent.createChooser(intent, strings.shareSong)
                                        )
                                    },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("detail_share_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Share,
                                        contentDescription = strings.shareSong,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            FilledTonalButton(
                                onClick = { nextSongId?.let(onNavigateToSong) },
                                enabled = nextSongId != null,
                                modifier = Modifier.testTag("detail_next_song"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Text(strings.nextSong)
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (song == null) {
                Text(
                    text = strings.noSearchResultsTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                    modifier = Modifier.padding(24.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 720.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(
                            horizontal = if (preferences.readingMode) 24.dp else 20.dp,
                            vertical = 18.dp
                        )
                ) {
                    if (preferences.readingMode) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = strings.readingModeOn,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = song.titleOdia,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = lyricsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = (preferences.lyricsFontSizeSp + 4).sp,
                            lineHeight = ((preferences.lyricsFontSizeSp + 4) * 1.45f).sp
                        ),
                        color = textColor
                    )

                    if (song.subTitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = song.subTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor.copy(alpha = 0.7f)
                        )
                    }

                    if (!song.category.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = song.category,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    val fontSize = preferences.lyricsFontSizeSp.sp
                    val lineHeight = (preferences.lyricsFontSizeSp * 1.65f).sp

                    Text(
                        text = song.lyrics,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = lyricsFontFamily,
                            fontSize = fontSize,
                            lineHeight = lineHeight
                        ),
                        color = textColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("song_lyrics_text")
                    )

                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }
}
