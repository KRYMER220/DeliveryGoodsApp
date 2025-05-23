package ru.krymer.delivery.ui.theme

import androidx.compose.ui.graphics.Color

data class Colors(
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val secondaryVariant: Color,
    val background: Color,
    val surface: Color,
    val error: Color,
    val onPrimary: Color,
    val onSecondary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onError: Color,
    val textColor: Color,
    val isLight: Boolean,
    val isChanged: Color,
)

val lightPalette = Colors(
    primary = Color(0xFF6200EE),
    primaryVariant = Color(0xFF3700B3),
    secondary = Color(0x33A6A6A6),
    secondaryVariant = Color(0xFF6C6C6C),
    background = Color.White,
    surface = Color.White,
    error = Color(0xFFFF0000),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.White,
    isLight = true,
    textColor = Color.Black,
    isChanged = Color(0x4DFF0000)
)

val darkPalette = Colors(
    primary = Color(0xFFBB86FC),
    primaryVariant = Color(0xFF3700B3),
    secondary = Color(0x33E4E4E4),
    secondaryVariant = Color(0xFF605C5C),
    background = Color.Black,
    surface = Color(0xFF121212),
    error = Color(0xFFFF002B),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.Black,
    isLight = false,
    textColor = Color.White,
    isChanged = Color(0x4DFF0000)
)