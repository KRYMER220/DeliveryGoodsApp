package ru.krymer.delivery.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun KeyBoardDialog(
    onDismissRequest: () -> Unit,
    setNumber: (Int) -> Unit,
    modifier: Modifier = Modifier,
    value: String = "0",
    text: String = ""
) {

    var count by remember {
        mutableStateOf(value)
    }

    Dialog(onDismissRequest = onDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onDismissRequest, indication = null, interactionSource = remember { MutableInteractionSource() })
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp).clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {},
                shape = RoundedCornerShape(16.dp),
                colors = CardColors(
                    containerColor = AppTheme.colors.onPrimary,
                    contentColor = AppTheme.colors.onPrimary,
                    disabledContentColor = AppTheme.colors.onPrimary,
                    disabledContainerColor = AppTheme.colors.onPrimary
                )
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = text,
                        textAlign = TextAlign.Center,
                        color = AppTheme.colors.onSecondary,
                        style = AppTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Row {
                        CommonButton(onClick = { setNumber(1) }, text = "1", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(2.dp))
                        CommonButton(onClick = { setNumber(2) }, text = "2", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(2.dp))
                        CommonButton(onClick = { setNumber(3) }, text = "3", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row {
                        CommonButton(onClick = { setNumber(4) }, text = "4", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(2.dp))
                        CommonButton(onClick = { setNumber(5) }, text = "5", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(2.dp))
                        CommonButton(onClick = { setNumber(6) }, text = "6", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Row {
                        CommonButton(onClick = { setNumber(7) }, text = "7", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(2.dp))
                        CommonButton(onClick = { setNumber(8) }, text = "8", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(2.dp))
                        CommonButton(onClick = { setNumber(9) }, text = "9", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Row {
                        CommonButton(onClick = { setNumber(0) }, text = "0", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(7.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CommonTextField(
                            modifier = Modifier.weight(0.5f),
                            value = count,
                            placeholder = "",
                            changerText = { newValue ->
                                count = newValue
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                            ),
                            textStyle = AppTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Image(
                            painter = painterResource(R.drawable.submit),
                            contentDescription = null,
                            modifier = Modifier
                                .background(AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp))
                                .size(50.dp)
                                .weight(0.5f)
                                .clickable(onClick = { setNumber(count.toInt()) })
                        )
                    }
                }
            }
        }

    }
}