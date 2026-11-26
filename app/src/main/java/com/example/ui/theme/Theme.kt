package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.data.local.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = PureWhite,
    primaryContainer = LavenderContainer,
    onPrimaryContainer = LavenderOnContainer,
    secondary = LightBlueSecondary,
    onSecondary = PureWhite,
    secondaryContainer = SkyBlueContainer,
    onSecondaryContainer = SkyBlueOnContainer,
    tertiary = PurpleVibrant,
    onTertiary = PureWhite,
    background = IceWhiteBackground,
    onBackground = InkDark,
    surface = PureWhite,
    onSurface = InkDark,
    surfaceVariant = TintedSurfaceVariant,
    onSurfaceVariant = InkMuted,
    outlineVariant = SoftBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPurplePrimary,
    onPrimary = DarkPurpleOnPrimary,
    primaryContainer = DarkPurpleContainer,
    onPrimaryContainer = DarkPurpleOnContainer,
    secondary = DarkSkySecondary,
    onSecondary = DarkSkyOnSecondary,
    secondaryContainer = DarkSkyContainer,
    onSecondaryContainer = DarkSkyOnContainer,
    tertiary = SkyBlueAccent,
    onTertiary = DarkSkyOnSecondary,
    background = MidnightBackground,
    onBackground = LightTextPrimary,
    surface = MidnightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = MidnightSurfaceVariant,
    onSurfaceVariant = LightTextMuted,
    outlineVariant = MidnightBorder
)

val OdiaGitaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun OdiaGitaBahiTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity
            activity?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                    !useDarkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = OdiaGitaTypography,
        shapes = OdiaGitaShapes,
        content = content
    )
}
