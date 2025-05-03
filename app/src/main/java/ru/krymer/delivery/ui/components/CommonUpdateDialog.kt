package ru.krymer.delivery.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonUpdateDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
    onConfirm: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = AppTheme.colors.onPrimary
        ) {
            Column(
                modifier = Modifier
                    .padding(end = 15.dp, start = 15.dp)
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onDismiss, colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.onSecondary
                        ),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.close),
                            fontSize = 16.sp,
                            color = AppTheme.colors.onPrimary
                        )
                    }
                    Button(
                        onClick = onConfirm, colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.onSecondary
                        ),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.save),
                            fontSize = 16.sp,
                            color = AppTheme.colors.onPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                content()
            }
        }
    }
}