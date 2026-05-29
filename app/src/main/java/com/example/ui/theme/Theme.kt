package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light Theme = Sunrise Focus Mode
private val SunriseColorScheme = lightColorScheme(
    primary = LightCalmTeal,
    secondary = LightSoftSage,
    tertiary = LightSkyAccent,
    background = LightCleanBackground,
    surface = LightCleanSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary
)

// Dark Theme = Sunset / Bedtime Circadian Sleep Mode (Themed with "Sleek Interface" codes)
private val SunsetColorScheme = darkColorScheme(
    primary = DarkWarmAmber,             // `#D0BCFF` - Sleek lavender
    secondary = DarkTerracotta,          // `#381E72` - Sleek deep purple
    tertiary = DarkSoftGold,             // `#EADDFF` - Soft highlighted violet-white
    background = DarkCircadianBackground,// `#1C1B1F` - Deep obsidian background
    surface = DarkCircadianSurface,      // `#2B2930` - Elegant container surface
    outline = DarkBorderColor,           // `#49454F` - Sleek borders/dividers
    onPrimary = DarkTerracotta,          // On primary container color
    onSecondary = DarkSoftGold,
    onTertiary = DarkTerracotta,
    onBackground = DarkTextPrimary,      // `#E6E1E5` - Eye-safe warm text
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCircadianSurface,
    onSurfaceVariant = DarkTextPrimary
)

@Composable
fun SinaMindTheme(
    isSleepMode: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isSleepMode) {
        SunsetColorScheme
    } else {
        SunriseColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
