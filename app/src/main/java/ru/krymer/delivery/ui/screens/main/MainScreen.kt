package ru.krymer.delivery.ui.screens.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import ru.krymer.delivery.Screens
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.ui.screens.main.views.MainView
import ru.krymer.delivery.ui.screens.product.models.ProductEvent
import ru.krymer.delivery.ui.screens.shared.models.SharedEvents
import ru.krymer.delivery.ui.screens.shared.models.SharedViewState

@Composable
fun MenuScreen(
    navigateTo: (Screens) -> Unit, user: UserModel?, event: (SharedEvents) -> Unit
) {
    user?.let {
        MainView(navigateTo = {
            navigateTo(it)
        }, user = user, event = event)
    }
}