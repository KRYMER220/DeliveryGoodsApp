package ru.krymer.delivery.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun CommonShowDeleteDialog(
    itemName: String,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {

    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Вы уверены, что хотите удалить $itemName ?",
                    modifier = Modifier.fillMaxWidth(),
                    color = AppTheme.colors.textColor
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.onSecondary
                        )

                    ) {
                        Text(text = "Нет", color = AppTheme.colors.onPrimary)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.onSecondary
                        )
                    ) {
                        Text(text = "Да", color = AppTheme.colors.onPrimary)
                    }
                }
            }, containerColor = AppTheme.colors.onPrimary
        )
    }
}