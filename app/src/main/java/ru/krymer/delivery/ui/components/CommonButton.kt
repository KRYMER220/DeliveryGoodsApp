package ru.krymer.delivery.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun CommonButton(onClick: () -> Unit, text: String = "") {
    Button(
        onClick = onClick, colors = ButtonColors(
            containerColor = AppTheme.colors.onSecondary,
            contentColor = AppTheme.colors.onSecondary,
            disabledContainerColor = AppTheme.colors.onSecondary,
            disabledContentColor = AppTheme.colors.onSecondary
        )
    ) { Text(text = text, color = AppTheme.colors.onPrimary) }
}