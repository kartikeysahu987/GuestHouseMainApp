package com.example.guesthousemain.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Color Palette: A harmonious, accessible set of colors.
// Primary: Deep Blue for main actions and headers.
// Secondary: Amber for emphasis and indicators.
// Tertiary (Accent): Cyan for highlights.
// Background: Light Grey to ensure readability.
private val PrimaryColor = Color(0xFF0D47A1)       // Deep Blue
private val SecondaryColor = Color(0xFFFFA000)     // Amber
private val AccentColor = Color(0xFF00BCD4)        // Cyan
private val BackgroundColor = Color(0xFFF5F5F5)    // Light Grey
private val SurfaceColor = Color(0xFFFFFFFF)       // White

// Text colors for high contrast.
private val OnPrimaryColor = Color.White
private val OnSecondaryColor = Color.Black
private val OnBackgroundColor = Color.Black
private val OnSurfaceColor = Color.Black

// Define a custom light color scheme.
private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    tertiary = AccentColor,
    background = BackgroundColor,
    surface = SurfaceColor,
    onPrimary = OnPrimaryColor,
    onSecondary = OnSecondaryColor,
    onBackground = OnBackgroundColor,
    onSurface = OnSurfaceColor
)

// Typography: Modern and clean fonts with clear hierarchy.
private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 14.sp
    )
)

// Custom theme applying the color scheme and typography.
@Composable
fun MyAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}
