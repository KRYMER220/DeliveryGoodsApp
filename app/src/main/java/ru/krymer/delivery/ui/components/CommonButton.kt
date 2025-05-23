package ru.krymer.delivery.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun CommonButton(onClick: () -> Unit, text: String = "", fontSize: TextUnit = 16.sp) {
    Button(
        onClick = onClick, colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.colors.secondary,
            disabledContainerColor = AppTheme.colors.secondaryVariant,
            disabledContentColor = AppTheme.colors.secondaryVariant,
        ), modifier = Modifier
            .height(60.dp)
            .width(70.dp)
    ) { Text(text = text, color = AppTheme.colors.onSecondary, fontSize = fontSize, style = AppTheme.typography.titleLarge) }
}