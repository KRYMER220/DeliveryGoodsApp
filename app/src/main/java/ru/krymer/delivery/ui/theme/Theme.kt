package ru.krymer.delivery.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import ru.krymer.delivery.ui.screens.shared.models.SharedViewState

@Composable
fun BoxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
    state: SharedViewState
) {
    var typography = remember { mutableStateOf(TypographyMap[0]) }
    val colors = if (darkTheme) darkPalette else lightPalette
    LaunchedEffect(key1 = state.currentFont) {
        typography = mutableStateOf(TypographyMap[state.currentFont.value])
    }

    CompositionLocalProvider(
        LocalColorProvider provides colors,
        LocalTypographyProvider provides typography.value!!,
        content = content
    )
}

object AppTheme {
    val colors: Colors
        @Composable
        @ReadOnlyComposable
        get() = LocalColorProvider.current
    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypographyProvider.current
}

val LocalColorProvider = staticCompositionLocalOf<Colors> {
    error("No default colors provided")
}