package ru.krymer.delivery.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun ConfirmView(onSubmit: () -> Unit, onDismiss: () -> Unit) {
    Column {
        Text(
            text = "Подтвердите действие",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = AppTheme.colors.onSecondary
        )
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onDismiss, colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.onSecondary
                )
            ) {
                Text(text = stringResource(id = R.string.close), color = AppTheme.colors.onPrimary)
            }
            Button(
                onClick = onSubmit, colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.onSecondary
                )
            ) {
                Text(text = stringResource(id = R.string.ok), color = AppTheme.colors.onPrimary)
            }
        }
    }
}