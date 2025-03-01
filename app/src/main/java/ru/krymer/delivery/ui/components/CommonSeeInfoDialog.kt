package ru.krymer.delivery.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
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
                Spacer(modifier = Modifier.height(10.dp))
                content()
            }
        }
    }
}
