package ru.krymer.delivery.ui.screens.client

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.ui.screens.client.view.ClientView

@Composable
fun ClientScreen(
    user: UserModel?, route: RouteModel, routes: List<RouteModel>
) {
    user?.let {
        val viewModel = hiltViewModel<ClientViewModel>()
        LaunchedEffect(Unit) {
            viewModel.getClients(route = route)
            viewModel.saveListRoute(list = routes)
        }
        ClientView(
            user = user,
            event = viewModel::obtainEvent,
            state = viewModel.viewState.collectAsState().value
        )
    }
}




