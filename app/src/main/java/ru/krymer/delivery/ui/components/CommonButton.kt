package ru.krymer.delivery.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun CommonButton(onClick: () -> Unit, text: String = "", modifier: Modifier = Modifier) {
    Button(
        onClick = onClick, colors = ButtonColors(
            containerColor = AppTheme.colors.onSecondary,
            contentColor = AppTheme.colors.onSecondary,
            disabledContainerColor = AppTheme.colors.onSecondary,
            disabledContentColor = AppTheme.colors.onSecondary,
        ), modifier = modifier
            .height(60.dp)
            .width(70.dp)
    ) { Text(text = text, color = AppTheme.colors.onPrimary) }
}