package ru.krymer.delivery.ui.screens.trip

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.ui.screens.trip.views.TripView

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TripScreen(
    openTrip: (TripModel) -> Unit = {}, user: UserModel?
) {
    user?.let {
        val viewModel = hiltViewModel<TripViewModel>()
        TripView(
            user = user,
            state = viewModel.viewState.collectAsState().value,
            event = viewModel::obtainEvent,
            openTrip = {
                openTrip(it)
            })
    }
}


