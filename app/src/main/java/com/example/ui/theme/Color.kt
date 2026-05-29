package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Let's establish highly specialized neuroscience color tokens matching the Sleek Theme layout

// 1. Sleek Sunrise Focus Palette (Clean focus mode)
val LightCalmTeal = Color(0xFF6750A4)         // Sleek Indigo primary
val LightSoftSage = Color(0xFF625B71)         // Sleek Charcoal secondary
val LightCleanBackground = Color(0xFFFEF7FF)  // Clean pale lavender-tinted background
val LightCleanSurface = Color(0xFFFFFFFF)     // Clean card surface
val LightSkyAccent = Color(0xFF7D5260)        // Subtle rose/sky accent
val LightTextPrimary = Color(0xFF1D1B20)      // Deep charcoal text

// 2. Sleek Sunset Sleep / Bedtime Circadian Palette (Melanopsin Protective Warm Lavender & Obsidian)
// Precise names mapped to Theme.kt
val DarkSleekLavender = Color(0xFFD0BCFF)     // Sleek glowing lavender
val DarkSleekDeepPurple = Color(0xFF381E72)   // Deep circadian protective slate purple
val DarkSleekEaddff = Color(0xFFEADDFF)       // Soft energetic lavender
val DarkSleekBackground = Color(0xFF1C1B1F)   // Deep obsidian black
val DarkSleekSurface = Color(0xFF2B2930)      // Sleek dark container cards
val DarkSleekBorder = Color(0xFF49454F)       // Modern sleek borderline
val DarkSleekTextHome = Color(0xFFE6E1E5)     // Soft primary text
val DarkSleekTextQuiet = Color(0xFFCAC4D0)    // Secondary grey text

// Retro-compatible aliases of the old design
val DarkWarmAmber = DarkSleekLavender
val DarkTerracotta = DarkSleekDeepPurple
val DarkCircadianBackground = DarkSleekBackground
val DarkCircadianSurface = DarkSleekSurface
val DarkSoftGold = DarkSleekEaddff
val DarkTextPrimary = DarkSleekTextHome
val DarkBorderColor = DarkSleekBorder
