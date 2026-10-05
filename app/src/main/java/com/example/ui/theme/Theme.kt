package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = OneUIBluePrimaryDark,
    onPrimary = Color(0xFF00227B),
    primaryContainer = OneUIBlueContainerDark,
    onPrimaryContainer = OneUIOnBlueContainerDark,
    secondary = Color(0xFFA5B4FC),
    onSecondary = Color(0xFF1E1B4B),
    background = OneUIGrayBgDark,
    onBackground = OneUITextPrimaryDark,
    surface = OneUISurfaceDark,
    onSurface = OneUITextPrimaryDark,
    surfaceVariant = OneUISurfaceContainerDark,
    onSurfaceVariant = OneUITextSecondaryDark,
    outline = Color(0xFF2E333D),
    error = OneUIError
)

private val LightColorScheme = lightColorScheme(
    primary = OneUIBluePrimary,
    onPrimary = Color.White,
    primaryContainer = OneUIBlueContainerLight,
    onPrimaryContainer = OneUIOnBlueContainerLight,
    secondary = Color(0xFF4F46E5),
    onSecondary = Color.White,
    background = OneUIGrayBgLight,
    onBackground = OneUITextPrimaryLight,
    surface = OneUISurfaceLight,
    onSurface = OneUITextPrimaryLight,
    surfaceVariant = OneUISurfaceContainerLight,
    onSurfaceVariant = OneUITextSecondaryLight,
    outline = Color(0xFFE2E6EF),
    error = OneUIError
)

@Composable
fun VlacularyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep distinctive One UI branding by default
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

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = VlacularyTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
