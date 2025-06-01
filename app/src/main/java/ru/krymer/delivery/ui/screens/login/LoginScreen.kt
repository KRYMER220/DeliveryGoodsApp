package ru.krymer.delivery.ui.screens.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import ru.krymer.delivery.ui.screens.login.views.LoginView

@Composable
fun LoginScreen() {
    val viewModel = hiltViewModel<LoginViewModel>()
    LoginView(state = viewModel.viewState.collectAsState().value, onEvent = viewModel::obtainEvent, )
}


