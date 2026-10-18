package com.devqissy.pindrop.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val PinterestRed = Color(0xFFE60023)
val PinterestRedDark = Color(0xFFB8001B)
val PinterestRedLight = Color(0xFFFF4D4D)

val DarkBackground = Color(0xFF0A0A0A)
val DarkSurface = Color(0xFF1E1E1E)
val DarkSurfaceVariant = Color(0xFF2D2D2D)

val LightBackground = Color(0xFFF5F5F5)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF0F0F0)

val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextSecondary = Color(0xFFB3B3B3)
val DarkTextTertiary = Color(0xFF808080)

val LightTextPrimary = Color(0xFF1A1A1A)
val LightTextSecondary = Color(0xFF666666)
val LightTextTertiary = Color(0xFF999999)

val SuccessGreen = Color(0xFF4CAF50)
val ErrorRed = Color(0xFFF44336)
val WarningOrange = Color(0xFFFF9800)

private val DarkColorScheme = darkColorScheme(
    primary = PinterestRed,
    onPrimary = Color.White,
    primaryContainer = PinterestRed.copy(alpha = 0.2f),
    onPrimaryContainer = DarkTextPrimary,

    secondary = Color(0xFF9C27B0),
    onSecondary = Color.White,

    background = DarkBackground,
    onBackground = DarkTextPrimary,

    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,

    error = ErrorRed,
    onError = Color.White,

    outline = Color.White.copy(alpha = 0.2f),
    outlineVariant = Color.White.copy(alpha = 0.1f)
)

private val LightColorScheme = lightColorScheme(
    primary = PinterestRed,
    onPrimary = Color.White,
    primaryContainer = PinterestRed.copy(alpha = 0.12f),
    onPrimaryContainer = PinterestRed,

    secondary = Color(0xFF9C27B0),
    onSecondary = Color.White,

    background = LightBackground,
    onBackground = LightTextPrimary,

    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,

    error = ErrorRed,
    onError = Color.White,

    outline = Color.Black.copy(alpha = 0.15f),
    outlineVariant = Color.Black.copy(alpha = 0.08f)
)

@Composable
fun PinDropTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
        typography = Typography,
        content = content
    )
}