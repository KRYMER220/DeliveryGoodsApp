package ru.krymer.delivery.ui.screens.client

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.delay
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.ui.screens.client.models.ClientEvent
import ru.krymer.delivery.ui.screens.client.view.ClientView

@Composable
fun ClientScreen(
    user: UserModel?, route: RouteModel, routes: List<RouteModel>
) {
    val viewModel = hiltViewModel<ClientViewModel>()
    user?.let {

        LaunchedEffect(route, routes) {
            delay(200)
            viewModel.obtainEvent(ClientEvent.Initialize(route = route, routes = routes))
        }

        ClientView(
            user = user,
            event = { viewModel.obtainEvent(it) },
            state = viewModel.viewState.collectAsState().value
        )
    }
}




