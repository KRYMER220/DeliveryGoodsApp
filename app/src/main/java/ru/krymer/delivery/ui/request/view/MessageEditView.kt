package ru.krymer.delivery.ui.request.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.data.model.MessageModel
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.screens.shop.views.MessageItem
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun MessageEditView(
    changeTextMessage: (String) -> Unit,
    deleteMessage: (MessageModel) -> Unit,
    messages: List<MessageModel>
) {
    var text by remember { mutableStateOf("") }
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
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Sentences
                ),
                textStyle = AppTheme.typography.titleMedium,
                isNotCenter = true
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