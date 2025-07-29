package ru.krymer.delivery.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun CommonInfoAlertDialog(
    onDismissRequest: () -> Unit, content: @Composable () -> Unit, modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardColors(
                containerColor = AppTheme.colors.onPrimary,
                contentColor = AppTheme.colors.onPrimary,
                disabledContentColor = AppTheme.colors.onPrimary,
                disabledContainerColor = AppTheme.colors.onPrimary
            )
        ) {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(5.dp)
            ) {
                content()
            }
        }
    }
}