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

sealed class AuthAction {
    data object Authorized : AuthAction()
    data object Unauthorized : AuthAction()
    data object None : AuthAction()
}

data class SharedViewState(
    val authAction: AuthAction = AuthAction.None,
    val user: MutableStateFlow<UserModel?> = MutableStateFlow(null),
    val factory: FactoryModel? = null,
    val isUserBlocked: Boolean = false,
    val navController: NavController? = null,
    val currentNavRoute: String? = null,
    val routeList: List<RouteModel> = mutableListOf(),

    val currentRoute: RouteModel? = null,
    val currentTrip: TripModel? = null, val currentShop: ShopModel? = null,

    val listMessage: MutableStateFlow<List<MessageModel>> = MutableStateFlow(listOf()),
    val currentFont: MutableStateFlow<Int> = MutableStateFlow(0),
    val stack: Bundle? = null,
)