package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = HomeTealPrimaryDark,
    onPrimary = HomeTealOnPrimaryDark,
    primaryContainer = HomeTealContainerDark,
    onPrimaryContainer = HomeTealOnContainerDark,
    secondary = HomeAmberSecondaryDark,
    secondaryContainer = HomeAmberContainerDark,
    onSecondaryContainer = HomeAmberOnContainerDark,
    error = HomeUrgentCoralDark,
    errorContainer = HomeUrgentContainerDark,
    onErrorContainer = HomeUrgentOnContainerDark,
    background = HomeBackgroundDark,
    surface = HomeSurfaceDark,
    surfaceVariant = HomeSurfaceVariantDark,
    onBackground = HomeOnSurfaceDark,
    onSurface = HomeOnSurfaceDark,
    outline = HomeOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = HomeTealPrimary,
    onPrimary = HomeTealOnPrimary,
    primaryContainer = HomeTealContainer,
    onPrimaryContainer = HomeTealOnContainer,
    secondary = HomeAmberSecondary,
    secondaryContainer = HomeAmberContainer,
    onSecondaryContainer = HomeAmberOnContainer,
    error = HomeUrgentCoral,
    errorContainer = HomeUrgentContainer,
    onErrorContainer = HomeUrgentOnContainer,
    background = HomeBackgroundLight,
    surface = HomeSurfaceLight,
    surfaceVariant = HomeSurfaceVariantLight,
    onBackground = HomeOnSurfaceLight,
    onSurface = HomeOnSurfaceLight,
    outline = HomeOutlineLight
)

private val HighContrastColorScheme = darkColorScheme(
    primary = HighContrastPrimary,
    onPrimary = HighContrastOnPrimary,
    primaryContainer = HighContrastSurface,
    onPrimaryContainer = HighContrastPrimary,
    secondary = HighContrastPrimary,
    error = HighContrastUrgent,
    background = HighContrastBackground,
    surface = HighContrastSurface,
    onBackground = HighContrastOnSurface,
    onSurface = HighContrastOnSurface
)

@Composable
fun HomeNestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        highContrast -> HighContrastColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
