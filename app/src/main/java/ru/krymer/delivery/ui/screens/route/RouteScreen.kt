package ru.krymer.delivery.ui.screens.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.ui.screens.route.views.RouteView

@Composable
fun RouteScreen(user: UserModel?, navigateTo: (String) -> Unit, popBackStack: () -> Unit) {
    user?.let {
        val viewModel = hiltViewModel<RouteViewModel>()
        RouteView(
            state = viewModel.viewState.collectAsState().value,
            event = viewModel::obtainEvent,
            user = user,
            navigateTo = navigateTo,
            popBackStack = popBackStack
        )
    }
}



