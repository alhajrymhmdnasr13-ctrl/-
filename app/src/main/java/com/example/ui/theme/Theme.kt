package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = TailorDarkBg,
    primaryContainer = GoldContainer,
    onPrimaryContainer = GoldText,
    secondary = NeonCyan,
    onSecondary = TailorDarkBg,
    secondaryContainer = TailorCardDark,
    onSecondaryContainer = TextPrimary,
    tertiary = NeonGreen,
    onTertiary = TailorDarkBg,
    background = TailorDarkBg,
    onBackground = TextPrimary,
    surface = TailorSurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = TailorCardDark,
    onSurfaceVariant = TextSecondary,
    outline = TailorCardBorder,
    error = ErrorRed
)

private val LightColorScheme = darkColorScheme( // Keep sophisticated tailor dark theme consistent
    primary = GoldPrimary,
    onPrimary = TailorDarkBg,
    primaryContainer = GoldContainer,
    onPrimaryContainer = GoldText,
    secondary = NeonCyan,
    onSecondary = TailorDarkBg,
    secondaryContainer = TailorCardDark,
    onSecondaryContainer = TextPrimary,
    tertiary = NeonGreen,
    onTertiary = TailorDarkBg,
    background = TailorDarkBg,
    onBackground = TextPrimary,
    surface = TailorSurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = TailorCardDark,
    onSurfaceVariant = TextSecondary,
    outline = TailorCardBorder,
    error = ErrorRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = TailorDarkBg.toArgb()
            window.navigationBarColor = TailorDarkBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
