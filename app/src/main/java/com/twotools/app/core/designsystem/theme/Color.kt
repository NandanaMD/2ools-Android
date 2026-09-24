package com.twotools.app.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// Clean Native Android Theme Tokens

// Dark Mode - Clean Slate & Obsidian Neutral
val DarkBackground = Color(0xFF0F1115)         // Deep clean neutral dark
val DarkSurface = Color(0xFF181B22)            // Clean elevated card surface
val DarkSurfaceVariant = Color(0xFF222730)     // Search bar, secondary containers, chips
val DarkSurfaceElevated = Color(0xFF2A303C)    // Elevated dialogs & floating elements
val DarkBorder = Color(0xFF2E3542)             // Subtle clean borders

// Dark Mode Accents (Material 3 Native Blue / Teal)
val NativeBlueDark = Color(0xFFA8C7FA)         // M3 Primary (Google native blue in dark mode)
val NativeBlueDarkContainer = Color(0xFF1E3A66) // Subtle blue container
val NativeBlueDarkOnContainer = Color(0xFFD3E3FD)
val NativeSecondaryDark = Color(0xFFBCC7D9)
val NativeTertiaryDark = Color(0xFF88D9C0)

// Text Shades Dark
val TextPrimaryDark = Color(0xFFF1F3F8)        // Crisp readable white
val TextSecondaryDark = Color(0xFF9CA3AF)      // Clean neutral secondary grey
val TextMutedDark = Color(0xFF6B7280)          // Muted grey

// Light Mode - Crisp Clean White & Soft Neutral
val LightBackground = Color(0xFFF8F9FC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEEF1F6)
val LightBorder = Color(0xFFE2E5EC)

// Light Mode Accents
val NativeBlueLight = Color(0xFF0B57D0)        // M3 Primary (Google native blue in light mode)
val NativeBlueLightContainer = Color(0xFFD3E3FD)
val NativeBlueLightOnContainer = Color(0xFF041E49)
val NativeSecondaryLight = Color(0xFF4A607A)
val NativeTertiaryLight = Color(0xFF006A57)

// Text Shades Light
val TextPrimaryLight = Color(0xFF191C20)
val TextSecondaryLight = Color(0xFF535F70)
val TextMutedLight = Color(0xFF74777F)

// Star / Accent colors
val StarAmber = Color(0xFFF59E0B)

// Clean Fallbacks for backward compatibility
val WarmOrange = StarAmber
val WarmOrangeLight = NativeBlueDark
val WarmOrangeDark = NativeBlueLight
val WarmOrangeContainer = NativeBlueDarkContainer
val BrandPlumBase = DarkSurfaceVariant
val BrandPlumDark = DarkBackground
val BrandPlumSurface = DarkSurface
val BrandPlumElevated = DarkSurfaceVariant
val BrandPlumBorder = DarkBorder

val Indigo600 = NativeBlueLight
val Indigo500 = NativeBlueDark
val Indigo400 = NativeBlueDark
val Cyan500 = NativeBlueDark
val Cyan400 = NativeBlueDark
val Emerald500 = NativeTertiaryDark
val Emerald400 = NativeTertiaryDark
val Amber500 = StarAmber
val Amber400 = StarAmber
val PureWhite = Color(0xFFFFFFFF)
