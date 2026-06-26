package com.example.obsidianclone.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

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


private val ObsidianColorScheme = darkColorScheme(
    background = Color.hsv(0f, 0f, .1f),
    surface = Color.hsv(0f, 0f, .2f),
    surfaceVariant = Color.hsv(0f, 0f, .3f),
    primary = Color.White,
//    = Color(0xFFFF562E),
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun ObsidianCloneTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ObsidianColorScheme,
        typography = Typography,
        content = content
    )
}