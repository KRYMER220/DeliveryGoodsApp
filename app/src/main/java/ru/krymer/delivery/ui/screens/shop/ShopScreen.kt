package ru.krymer.delivery.ui.screens.shop

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.delay
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent
import ru.krymer.delivery.ui.screens.shop.views.ShopView

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShopScreen(user: UserModel?, trip: TripModel, routeToRequest: (List<ShopModel>, ShopModel) -> Unit) {
    user?.let {
        val viewModel = hiltViewModel<ShopViewModel>()

        LaunchedEffect(trip) {
            delay(200)
            viewModel.obtainEvent(ShopEvent.Initialize(trip = trip))
        }

        ShopView(
            state = viewModel.viewState.collectAsState().value,
            event = viewModel::obtainEvent,
            user = it,
            routeToRequest = routeToRequest
        )
    }
}

















