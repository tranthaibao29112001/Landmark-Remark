package com.example.mapnote.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColors(
    background = Black,
    primary = DarkBlue,
    error = DarkRed,
    surface = LightBlack,
    onSecondary = Blue
)

private val LightColorScheme = lightColors(
    background = Color.White,
    primary = DarkBlue,
    error = LightRed,
    surface = Color.White,
    onSecondary = Blue

)

@Composable
fun MapNoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colors = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}