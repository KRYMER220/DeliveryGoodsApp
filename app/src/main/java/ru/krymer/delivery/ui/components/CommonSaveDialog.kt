package ru.krymer.delivery.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonSaveDialog(
    dismiss: () -> Unit,
    content: @Composable () -> Unit,
    confirm: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
            onDismissRequest = dismiss,
            sheetState = sheetState,
            containerColor = AppTheme.colors.onPrimary
    ) {
        Column(
            modifier = Modifier
                .padding(end = 15.dp, start = 15.dp)
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                contentDescription = "submit",
                painter = painterResource(id = R.drawable.submit),
                modifier = Modifier
                    .size(50.dp)
                    .combinedClickable(onClick = confirm)
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}