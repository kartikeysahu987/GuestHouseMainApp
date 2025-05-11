package com.example.guesthousemain.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.State


private val LightColors = lightColorScheme(
//    primary = Color(0xFF6200EE),
//    onPrimary = Color.White,
//    secondary = Color(0xFF03DAC6)
    primary = Color(0xFF3871E0),         // Deeper blue for light mode
    secondary = Color(0xFF7C4DFF),       // Purple accent
    background = Color.White,
    surface = Color(0xFFF8F8F8),         // Slightly off-white for cards
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

private val DarkColors = darkColorScheme(
//    primary = Color(0xFFBB86FC),
//    onPrimary = Color.Black,
//    secondary = Color(0xFF03DAC6)
    primary = Color(0xFF6F9AE8),         // A more vibrant blue
    secondary = Color(0xFF9D84FF),       // Purple accent
    background = Color(0xFF121212),      // Dark background
    surface = Color(0xFF242424),         // Slightly lighter card background for dark mode
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun GuestHouseMainTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }
    val animatedColorScheme = animateColorSchemeAsState(targetColorScheme = colorScheme)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
private fun animateColorSchemeAsState(
    targetColorScheme: ColorScheme
): State<ColorScheme> {
    val animationSpec = tween<Color>(durationMillis = 600)

    val primary = animateColorAsState(targetValue = targetColorScheme.primary, animationSpec = animationSpec)
    val onPrimary = animateColorAsState(targetValue = targetColorScheme.onPrimary, animationSpec = animationSpec)
    val secondary = animateColorAsState(targetValue = targetColorScheme.secondary, animationSpec = animationSpec)
    val onSecondary = animateColorAsState(targetValue = targetColorScheme.onSecondary, animationSpec = animationSpec)
    val background = animateColorAsState(targetValue = targetColorScheme.background, animationSpec = animationSpec)
    val onBackground = animateColorAsState(targetValue = targetColorScheme.onBackground, animationSpec = animationSpec)
    val surface = animateColorAsState(targetValue = targetColorScheme.surface, animationSpec = animationSpec)
    val onSurface = animateColorAsState(targetValue = targetColorScheme.onSurface, animationSpec = animationSpec)

    return remember(
        primary.value,
        onPrimary.value,
        secondary.value,
        onSecondary.value,
        background.value,
        onBackground.value,
        surface.value,
        onSurface.value
    ) {
        mutableStateOf(
            ColorScheme(
                primary = primary.value,
                onPrimary = onPrimary.value,
                secondary = secondary.value,
                onSecondary = onSecondary.value,
                background = background.value,
                onBackground = onBackground.value,
                surface = surface.value,
                onSurface = onSurface.value,
                // Copy other colors from targetColorScheme
                tertiary = targetColorScheme.tertiary,
                onTertiary = targetColorScheme.onTertiary,
                // ... and so on for all required ColorScheme properties
                primaryContainer = targetColorScheme.primaryContainer,
                onPrimaryContainer = targetColorScheme.onPrimaryContainer,
                secondaryContainer = targetColorScheme.secondaryContainer,
                onSecondaryContainer = targetColorScheme.onSecondaryContainer,
                tertiaryContainer = targetColorScheme.tertiaryContainer,
                onTertiaryContainer = targetColorScheme.onTertiaryContainer,
                error = targetColorScheme.error,
                onError = targetColorScheme.onError,
                errorContainer = targetColorScheme.errorContainer,
                onErrorContainer = targetColorScheme.onErrorContainer,
                surfaceVariant = targetColorScheme.surfaceVariant,
                onSurfaceVariant = targetColorScheme.onSurfaceVariant,
                outline = targetColorScheme.outline,
                outlineVariant = targetColorScheme.outlineVariant,
                scrim = targetColorScheme.scrim,
                inverseSurface = targetColorScheme.inverseSurface,
                inverseOnSurface = targetColorScheme.inverseOnSurface,
                inversePrimary = targetColorScheme.inversePrimary,
                surfaceTint = targetColorScheme.surfaceTint,
                surfaceBright = targetColorScheme.surfaceBright,
                surfaceContainer = targetColorScheme.surfaceContainer,
                surfaceContainerHigh = targetColorScheme.surfaceContainerHigh,
                surfaceContainerHighest = targetColorScheme.surfaceContainerHighest,
                surfaceContainerLow = targetColorScheme.surfaceContainerLow,
                surfaceContainerLowest = targetColorScheme.surfaceContainerLowest,
                surfaceDim = targetColorScheme.surfaceDim
            )
        )
    }
}