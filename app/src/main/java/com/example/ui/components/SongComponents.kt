package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.SongWithMeta
import com.example.data.repository.SyncState
import com.example.ui.theme.HeartPink
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.NotoSansOriyaFontFamily
import com.example.ui.theme.OfflineAmber
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.SyncGreen

@Composable
fun SongNumberPill(
    songNumber: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 50.dp, height = 42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = songNumber.toString(),
            color = MaterialTheme.colorScheme.onPrimary,
            fontFamily = PlusJakartaFontFamily,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
fun SongCard(
    song: SongWithMeta,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    trailingCaption: String? = null
) {
    val heartColor by animateColorAsState(
        targetValue = if (song.isFavorite) HeartPink else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "heartColor"
    )
    val heartScale by animateFloatAsState(
        targetValue = if (song.isFavorite) 1.15f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "heartScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("song_card_${song.baserowId}")
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SongNumberPill(songNumber = song.songNumber)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.titleOdia,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = NotoSansOriyaFontFamily,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (song.subTitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = song.subTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!trailingCaption.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = trailingCaption,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("favorite_toggle_${song.baserowId}")
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = stringResource(R.string.content_desc_favorite_toggle),
                    tint = heartColor,
                    modifier = Modifier.scale(heartScale)
                )
            }
        }
    }
}

@Composable
fun ConnectivityStatusPill(
    isOnline: Boolean,
    syncState: SyncState,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val (bg, fg, icon, label) = when {
        syncState is SyncState.Syncing -> Quad(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            Icons.Rounded.Sync,
            if (syncState.totalExpected > 0) "${syncState.fetchedSoFar}/${syncState.totalExpected}" else strings.syncingSongs
        )
        isOnline -> Quad(
            SyncGreen.copy(alpha = 0.14f),
            SyncGreen,
            Icons.Filled.CloudDone,
            strings.onlineLabel
        )
        else -> Quad(
            OfflineAmber.copy(alpha = 0.14f),
            OfflineAmber,
            Icons.Filled.CloudOff,
            strings.offlineLabel
        )
    }

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = bg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = fg,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (!actionLabel.isNullOrBlank() && onAction != null) {
            Spacer(modifier = Modifier.height(18.dp))
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onAction)
                    .minimumInteractiveComponentSize(),
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    val strings = LocalAppStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.privacyPolicy,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Odia Gita Bahi (ଓଡ଼ିଆ ଗୀତ ବହି) respects your privacy.",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "• No Ads & No Tracking: This app does not display advertisements, track your personal identity, or sell analytics data.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Local Offline Storage: Downloaded songs, favorite songs, recently viewed history, font size, and theme preferences are stored locally on your device using Android Room and DataStore.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Baserow Sync: When internet is available, the app performs read-only GET requests to Baserow (api.baserow.io) to fetch updated Christian songs without modifying or deleting your original Baserow data.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Permissions: Only standard INTERNET and ACCESS_NETWORK_STATE permissions are used.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.close)
            }
        }
    )
}
