package ru.krymer.delivery.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R

val Roboto = FontFamily(
    Font(R.font.roboto_light, FontWeight.Light),
    Font(R.font.roboto_medium, FontWeight.Medium),
    Font(R.font.roboto_bold, FontWeight.Bold),
)

fun createTypography(fontFamily: FontFamily, fontSizeIndex: Int): Typography {
    val multiplier = when(fontSizeIndex) {
        0 -> -4
        1 -> -2
        2 -> 0
        3 -> +2
        4 -> +4
        else -> 0
    }
    return Typography(
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Light,
            fontSize = (16 + multiplier).sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Light,
            fontSize = (20 + multiplier).sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Light,
            fontSize = (18 + multiplier).sp,
            lineHeight = 26.sp,
            letterSpacing = 0.sp
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Light,
            fontSize = (22 + multiplier).sp,
            lineHeight = 30.sp,
            letterSpacing = 0.sp
        ),
        labelMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Light,
            fontSize = (24 + multiplier).sp,
            lineHeight = 30.sp,
            letterSpacing = 0.sp
        ),
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Light,
            fontSize = (26 + multiplier).sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Light,
            fontSize = (14 + multiplier).sp,
            lineHeight = 18.sp,
            letterSpacing = 0.sp
        ),
    )
}

val LocalTypographyProvider = staticCompositionLocalOf<Typography> {
    error("No Typography provided")
}