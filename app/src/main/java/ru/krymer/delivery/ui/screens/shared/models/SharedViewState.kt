package ru.krymer.delivery.ui.screens.shared.models

import android.os.Bundle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import ru.krymer.delivery.data.model.FactoryModel
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.data.model.utilModel.MessageModel

data class SharedViewState(
    val user: MutableStateFlow<UserModel?> = MutableStateFlow(null),
    val factory: FactoryModel? = null,
    val isUserBlocked: Boolean = false,
    val listMessage: MutableStateFlow<List<MessageModel>> = MutableStateFlow(listOf()),
    val isShowSettings: MutableStateFlow<Boolean> = MutableStateFlow(false),
    val lightVersion: Boolean = false,
    val fontSizeIndex: Int = 2
)