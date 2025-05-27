package ru.krymer.delivery.ui.screens.product

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import ru.krymer.delivery.ui.screens.product.view.ProductView

@Composable
fun ProductScreen(
    popBackStack: () -> Unit
) {
    val viewModel = hiltViewModel<ProductViewModel>()
    ProductView(
        state = viewModel.viewState.collectAsState().value,
        event = viewModel::obtainEvent,
        popBackStack = popBackStack,
    )
}
