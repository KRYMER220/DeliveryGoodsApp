package ru.krymer.delivery.ui.screens.shop.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.data.model.MessageModel
import ru.krymer.delivery.data.model.utilModel.Error
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.screens.shop.models.ShopViewState
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.startsWithDigit

@Composable
fun ChangeAddSumView(
    changeAddSum: (String) -> Unit
) {
    var addSum by remember { mutableStateOf("") }
    var errorAddSum by remember { mutableStateOf(Error()) }
    Column {
        Spacer(modifier = Modifier.height(5.dp))
        CommonTextField(
            value = addSum,
            placeholder = "Добавочная сумма",
            changerText = { str ->
                addSum = str
                errorAddSum = when {
                    str == "" -> Error(visible = true, error = Constants.EMPTY.EMPTY_FIELD)
                    !startsWithDigit(str) -> Error(visible = true, error = Constants.ERROR.ERROR_NUMBER_INPUT)
                    else -> {
                        changeAddSum(str)
                        Error()
                    }
                }

            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
            ),
            textStyle = TextStyle(textAlign = TextAlign.Center)
        )
        Spacer(modifier = Modifier.height(5.dp))
    }
}

@Composable
fun ChangeArrearsView(
    changeArrears: (String) -> Unit
) {
    var arrears by remember { mutableStateOf("") }
    var errorArrears by remember { mutableStateOf(Error()) }
    Column {
        Spacer(modifier = Modifier.height(5.dp))
        CommonTextField(
            value = arrears,
            placeholder = "Долг",
            changerText = { str ->
                arrears = str
                errorArrears = when {
                    str == "" -> Error(visible = true, error = Constants.EMPTY.EMPTY_FIELD)
                    !startsWithDigit(str) -> Error(visible = true, error = Constants.ERROR.ERROR_NUMBER_INPUT)
                    else -> {
                        changeArrears(str)
                        Error()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
            ),
            textStyle = TextStyle(textAlign = TextAlign.Center)
        )
        Spacer(modifier = Modifier.height(5.dp))
    }
}

@Composable
fun MessageTextView(
    changeTextMessage: (String) -> Unit, deleteMessage: (MessageModel) -> Unit, state: ShopViewState
) {
    var text by remember { mutableStateOf("") }
    val messages = state.messages
    LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        item {
            Spacer(modifier = Modifier.height(5.dp))
            CommonTextField(
                value = text,
                placeholder = "Сообщение",
                changerText = { str ->
                    text = str
                    changeTextMessage(str)
                },
                modifier = Modifier.fillMaxSize(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                ),
                textStyle = TextStyle(textAlign = TextAlign.Start)
            )
            Spacer(modifier = Modifier.height(5.dp))
        }

        if (messages.isNotEmpty()) {
            items(messages) { message ->
                MessageItem(messageModel = message, deleteMessage = deleteMessage)
            }
        }

        item { Spacer(modifier = Modifier.height(15.dp)) }
    }
}