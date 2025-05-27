package ru.krymer.delivery.ui.screens.client

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.ui.screens.client.view.ClientView

@Composable
fun ClientShopScreen(
    user: UserModel?, popBackStack: () -> Unit
) {
    user?.let {
        val viewModel = hiltViewModel<ClientViewModel>()
        ClientView(
            user = user,
            event = viewModel::obtainEvent,
            popBackStack = popBackStack,
            state = viewModel.viewState.collectAsState().value
        )
    }
}




