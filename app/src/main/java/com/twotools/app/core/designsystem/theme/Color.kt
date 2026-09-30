package com.twotools.app.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// Dark Palette
val DarkBackground = Color(0xFF0F1115)
val DarkSurface = Color(0xFF181B22)
val DarkSurfaceVariant = Color(0xFF222730)
val DarkSurfaceElevated = Color(0xFF2A303C)
val DarkBorder = Color(0xFF2E3542)

val NativeBlueDark = Color(0xFFA8C7FA)
val NativeBlueDarkContainer = Color(0xFF1E3A66)
val NativeBlueDarkOnContainer = Color(0xFFD3E3FD)
val NativeSecondaryDark = Color(0xFFBCC7D9)
val NativeTertiaryDark = Color(0xFF88D9C0)

val TextPrimaryDark = Color(0xFFF1F3F8)
val TextSecondaryDark = Color(0xFF9CA3AF)
val TextMutedDark = Color(0xFF6B7280)

// Light Palette
val LightBackground = Color(0xFFF8F9FC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEEF1F6)
val LightBorder = Color(0xFFE2E5EC)

val NativeBlueLight = Color(0xFF0B57D0)
val NativeBlueLightContainer = Color(0xFFD3E3FD)
val NativeBlueLightOnContainer = Color(0xFF041E49)
val NativeSecondaryLight = Color(0xFF4A607A)
val NativeTertiaryLight = Color(0xFF006A57)

val TextPrimaryLight = Color(0xFF191C20)
val TextSecondaryLight = Color(0xFF535F70)
val TextMutedLight = Color(0xFF74777F)

val StarAmber = Color(0xFFF59E0B)

// Fallbacks for backward compatibility
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
