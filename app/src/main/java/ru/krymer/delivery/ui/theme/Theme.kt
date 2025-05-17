package ru.krymer.delivery.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.Typography
import androidx.compose.runtime.collectAsState
import ru.krymer.delivery.ui.screens.shared.SharedViewModel

@Composable
fun BoxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
    sharedViewModel: SharedViewModel
) {

    val colors = if (darkTheme) darkPalette else lightPalette
    val viewState = sharedViewModel.viewState.collectAsState().value
    val typography = TypographyMap[viewState.currentFont] ?: TypographyMap[0]!!

    CompositionLocalProvider(
        LocalColorProvider provides colors,
        LocalTypographyProvider provides typography,
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