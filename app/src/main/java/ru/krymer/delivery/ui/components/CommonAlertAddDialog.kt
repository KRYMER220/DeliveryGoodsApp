package ru.krymer.delivery.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.theme.AppTheme

@ExperimentalFoundationApi
@Composable
fun CommonAlertAddDialog(
    onDismiss: () -> Unit,
    confirm: () -> Unit,
    otherFun: () -> Unit = {},
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onDismiss, indication = null, interactionSource = remember { MutableInteractionSource() })
                .padding(bottom = 15.dp, start = 10.dp, end = 10.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = 5.dp, end = 5.dp).clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {},
                shape = RoundedCornerShape(10.dp),
                colors = CardColors(
                    containerColor = AppTheme.colors.onPrimary,
                    contentColor = AppTheme.colors.onPrimary,
                    disabledContentColor = AppTheme.colors.onPrimary,
                    disabledContainerColor = AppTheme.colors.onPrimary
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        contentDescription = "submit",
                        painter = painterResource(id = R.drawable.submit),
                        modifier = Modifier
                            .size(60.dp)
                            .combinedClickable(onClick = confirm, onLongClick = otherFun)
                    )
                    content()
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }
        }
    }
}