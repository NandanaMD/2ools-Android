package com.twotools.app.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NativeBlueDark,
    onPrimary = Color(0xFF002F6C),
    primaryContainer = NativeBlueDarkContainer,
    onPrimaryContainer = NativeBlueDarkOnContainer,
    secondary = NativeSecondaryDark,
    onSecondary = Color(0xFF1C3140),
    secondaryContainer = Color(0xFF2E3844),
    onSecondaryContainer = Color(0xFFD4E3F5),
    tertiary = NativeTertiaryDark,
    onTertiary = Color(0xFF00382E),
    tertiaryContainer = Color(0xFF1B4E43),
    onTertiaryContainer = Color(0xFFA5F2D9),
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkBorder,
    outlineVariant = Color(0xFF363C48)
)

private val LightColorScheme = lightColorScheme(
    primary = NativeBlueLight,
    onPrimary = Color.White,
    primaryContainer = NativeBlueLightContainer,
    onPrimaryContainer = NativeBlueLightOnContainer,
    secondary = NativeSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDE3ED),
    onSecondaryContainer = Color(0xFF141D28),
    tertiary = NativeTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFA5F2D9),
    onTertiaryContainer = Color(0xFF00201A),
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorder,
    outlineVariant = Color(0xFFD4D8E2)
)

@Composable
fun TwoolsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Clean & native: Enable dynamic color on Android 12+ (Material You)
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TwoolsTypography,
        content = content
    )
}
