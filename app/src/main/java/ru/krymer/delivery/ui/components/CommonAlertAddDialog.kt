package ru.krymer.delivery.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.theme.AppTheme

@ExperimentalFoundationApi
@Composable
fun CommonAlertAddDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardColors(
                containerColor = AppTheme.colors.onPrimary,
                contentColor = AppTheme.colors.onPrimary,
                disabledContentColor = AppTheme.colors.onPrimary,
                disabledContainerColor = AppTheme.colors.onPrimary
            )
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .height(50.dp)
                            .padding(start = 5.dp)
                            .background(
                                color = AppTheme.colors.onSecondary,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .combinedClickable(onClick = {
                                onDismissRequest()
                            }, onLongClick = {})
                    ) {
                        Text(
                            text = stringResource(id = R.string.close),
                            fontSize = 16.sp,
                            color = AppTheme.colors.onPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(
                                    Alignment.Center
                                )
                                .padding(5.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .height(50.dp)
                            .padding(end = 5.dp)
                            .background(
                                color = AppTheme.colors.onSecondary,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .combinedClickable(onClick = {
                                onConfirmation()
                            }, onLongClick = {
                                onConfirm()
                            })
                    ) {
                        Text(
                            text = stringResource(id = R.string.save),
                            fontSize = 16.sp,
                            color = AppTheme.colors.onPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(
                                    Alignment.Center
                                )
                                .padding(5.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(5.dp))
                content()
            }
        }
    }
}