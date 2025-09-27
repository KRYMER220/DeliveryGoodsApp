package ru.krymer.delivery.ui.screens.courier

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.krymer.delivery.ui.screens.courier.view.CourierView

@Composable
fun CourierScreen() {
    val viewModel = hiltViewModel<CourierViewModel>()
    CourierView(
        state = viewModel.viewState.collectAsState().value,
        event = viewModel::obtainEvent,
    )
}


