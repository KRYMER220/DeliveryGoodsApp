package ru.krymer.delivery.ui.screens.shared.models

import kotlinx.coroutines.flow.MutableStateFlow
import ru.krymer.delivery.data.model.FactoryModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.data.model.utilModel.MessageModel

data class SharedViewState(
    val user: UserModel? = null,
    val factory: FactoryModel? = null,
    val isUserBlocked: Boolean = false,
    val listMessage: List<MessageModel> = listOf(),
    val isShowSettings: Boolean = false,
    val isShowPassChanger: Boolean = false,
    val lightVersion: Boolean = false,
    val fontSizeIndex: Int = 2
)