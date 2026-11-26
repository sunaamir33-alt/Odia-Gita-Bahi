package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.NotoSansOriyaFontFamily
import com.example.ui.theme.PurpleDeep
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBlueAccent
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val scale = remember { Animatable(0.82f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 450)
        )
        delay(850)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PurpleDeep,
                        PurplePrimary,
                        SkyBlueAccent.copy(alpha = 0.85f)
                    )
                )
            )
            .statusBarsPadding()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value.coerceAtLeast(0.4f))
        ) {
            Surface(
                shape = RoundedCornerShape(30.dp),
                shadowElevation = 16.dp,
                color = PurpleDeep
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_odia_gita_icon),
                    contentDescription = stringResource(R.string.content_desc_app_logo),
                    modifier = Modifier
                        .size(124.dp)
                        .clip(RoundedCornerShape(30.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "ଓଡ଼ିଆ ଗୀତ ବହି",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = NotoSansOriyaFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = PureWhite,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "ODIA GITA BAHI",
                style = MaterialTheme.typography.titleSmall,
                color = PureWhite.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Digital Christian Song Book",
                style = MaterialTheme.typography.bodyMedium,
                color = PureWhite.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .size(width = 140.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = PureWhite,
                trackColor = PureWhite.copy(alpha = 0.28f)
            )
        }
    }
}
