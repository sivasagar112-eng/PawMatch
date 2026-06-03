package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Premium Clean Light Scheme as the core design aesthetic
private val PawMatchLightScheme = lightColorScheme(
    primary = Terracotta,
    onPrimary = Color.White,
    primaryContainer = TerracottaLight,
    onPrimaryContainer = WarmBrown,
    secondary = GoldAccent,
    onSecondary = Color.White,
    secondaryContainer = GoldLight,
    onSecondaryContainer = WarmBrown,
    tertiary = WarmBrown,
    onTertiary = Color.White,
    background = WarmCream,
    onBackground = WarmBrown,
    surface = Color.White,
    onSurface = WarmBrown,
    surfaceVariant = Soapstone,
    onSurfaceVariant = SoftBrown,
    outline = SoftBrown,
    outlineVariant = Soapstone
)

// Dark Scheme for optional dark mode support keeping the warm premium palette
private val PawMatchDarkScheme = darkColorScheme(
    primary = Terracotta,
    onPrimary = Color.White,
    primaryContainer = WarmBrown,
    onPrimaryContainer = WarmCream,
    secondary = GoldAccent,
    onSecondary = WarmBrown,
    tertiary = GoldLight,
    onTertiary = Color.White,
    background = Color(0xFF1E140F),
    onBackground = WarmCream,
    surface = Color(0xFF281C16),
    onSurface = WarmCream,
    surfaceVariant = Color(0xFF33251E),
    onSurfaceVariant = SoftBrown,
    outline = SoftBrown,
    outlineVariant = Color(0xFF33251E)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Enforce our custom premium light warm cream theme for perfect visual contrast
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = PawMatchLightScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
