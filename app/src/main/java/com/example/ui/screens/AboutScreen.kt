package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.R
import com.example.data.repository.UpdateCheckStatus
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.NotoSansOriyaFontFamily

@Composable
fun AboutScreen(
    totalSongsCount: Int,
    updateCheckStatus: UpdateCheckStatus?,
    onCheckUpdates: () -> Unit,
    onSyncNow: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var showPrivacyDialog by remember { mutableStateOf(false) }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("about_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.content_desc_navigate_back)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = strings.about,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_odia_gita_icon),
                    contentDescription = stringResource(R.string.content_desc_app_logo),
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = strings.appTitle,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = NotoSansOriyaFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

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
                    Text(
                        text = strings.aboutAppDesc,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = strings.dataPreservationNote,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

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
                    Text(
                        text = "Baserow Table ID: ${BuildConfig.BASEROW_TABLE_ID}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "${strings.totalSongsLabel}: $totalSongsCount",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (updateCheckStatus != null) {
                        Text(
                            text = if (updateCheckStatus.hasUpdate) {
                                String.format(
                                    strings.updateAvailableDesc,
                                    updateCheckStatus.remoteCount,
                                    updateCheckStatus.localCount
                                )
                            } else {
                                strings.upToDateMsg
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (updateCheckStatus.hasUpdate) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondary
                            }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onCheckUpdates,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("about_check_updates_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.NewReleases, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.checkUpdates, textAlign = TextAlign.Center)
                        }
                        if (updateCheckStatus?.hasUpdate == true) {
                            OutlinedButton(
                                onClick = onSyncNow,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(strings.syncNow)
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = { showPrivacyDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("about_privacy_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Rounded.PrivacyTip, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.privacyPolicy)
            }
        }
    }
}
