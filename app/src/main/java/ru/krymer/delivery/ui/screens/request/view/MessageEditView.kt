package ru.krymer.delivery.ui.screens.request.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.MessageModel
import ru.krymer.delivery.data.model.utilModel.Error
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.screens.shop.views.MessageItem
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun MessageEditView(
    changeTextMessage: (String) -> Unit,
    deleteMessage: (MessageModel) -> Unit,
    messages: List<MessageModel>,
    createMessage: () -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
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
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                textStyle = AppTheme.typography.titleMedium,
                isNotCenter = true,
                autoClearFocus = true
            )
            Spacer(modifier = Modifier.height(5.dp))
            Image(
                contentDescription = null,
                painter = painterResource(id = R.drawable.submit),
                modifier = Modifier
                    .size(50.dp)
                    .combinedClickable(onClick = {
                        text = ""
                        focusManager.clearFocus()
                        createMessage()
                    })
            )
        }

        if (messages.isNotEmpty()) {
            items(messages) { message ->
                MessageItem(messageModel = message, deleteMessage = deleteMessage)
            }
        }

        item { Spacer(modifier = Modifier.height(15.dp)) }
    }
}