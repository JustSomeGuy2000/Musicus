package jehr.projects.musicus.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Purple40,
    secondary = greyD4,
    tertiary = greyD3,
    inversePrimary = white
)

private val LightColorScheme = lightColorScheme(
    primary = Purple80,
    secondary = greyD2,
    tertiary = white,
    inversePrimary = black

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

data class ColourScheme(val background: Color, val foreground: Color, val text: Color)

val lightColourScheme = ColourScheme(greyD2, white, black)
val darkColourScheme = ColourScheme(greyD4, greyD3, white)
var colourScheme = lightColourScheme

@Composable
fun MusicusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}