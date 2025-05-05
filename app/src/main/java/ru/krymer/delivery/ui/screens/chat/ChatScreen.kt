package ru.krymer.delivery.ui.screens.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.user.MessageModel
import ru.krymer.delivery.data.model.utilModel.Error
import ru.krymer.delivery.ui.components.CommonButton
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.convertToTextDate
import ru.krymer.delivery.utills.startsWithDigit

@Composable
fun ChatScreen(viewModel: ChatViewModel, navController: NavHostController) {
    val viewState = viewModel.viewState.collectAsState().value
    val chat by viewModel.chatMessages.collectAsState()
    var message by remember { mutableStateOf(Constants.EMPTY.EMPTY_STRING) }
    Column(modifier = Modifier.padding(15.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.back_stack),
                contentDescription = "exit",
                modifier = Modifier
                    .clickable(onClick = {
                        navController.popBackStack()
                    })
                    .size(40.dp)
            )
        }
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            CommonTextField(
                isError = false,
                errorValue = "",
                value = message,
                placeholder = "Ваше сообщение",
                onVC = { str ->
                    message = str
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.height(60.dp)
            )
            CommonButton(onClick = {
                if (message.isNotEmpty()) {
                    viewModel.obtainEvent(ChatEvent.SendMessage(message = message))
                }
            }, text = "Отправить", modifier = Modifier.fillMaxWidth())
        }
        Spacer(modifier = Modifier.height(15.dp))
        if (chat.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.Center),
                    strokeWidth = 2.dp,
                    color = AppTheme.colors.onSecondary
                )
            }
        } else {
            ChatView(chat = chat)
        }
    }
}

@Composable
fun ChatView(chat: List<MessageModel>) {
    LazyColumn {
        itemsIndexed(chat) { index, model ->
            MessageItem(model = model)
            Spacer(modifier = Modifier.padding(bottom = 10.dp))
        }
    }
}

@Composable
fun MessageItem(model: MessageModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = model.nameUser, modifier = Modifier.weight(0.5f), fontSize = 14.sp)
            Text(text = convertToTextDate(model.date), modifier = Modifier.weight(0.5f), fontSize = 14.sp)
        }
        Text(text = model.message, fontSize = 14.sp)
    }
}