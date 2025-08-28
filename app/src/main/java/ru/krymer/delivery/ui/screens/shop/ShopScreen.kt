package ru.krymer.delivery.ui.screens.shop

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.ui.screens.shop.views.ShopView

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShopScreen(user: UserModel?, trip: TripModel) {
    user?.let {
        val viewModel = hiltViewModel<ShopViewModel>()
        LaunchedEffect(Unit) {
            viewModel.initData(trip = trip)
        }
        ShopView(
            state = viewModel.viewState.collectAsState().value,
            event = viewModel::obtainEvent,
            user = it
        )
    }
}

















