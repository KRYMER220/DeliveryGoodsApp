package ru.krymer.delivery.ui.screens.trip

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.ui.screens.trip.views.TripView

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TripScreen(
    openTrip: (TripModel) -> Unit = {},
    user: UserModel?
) {
    if (user == null) return

    val viewModel = hiltViewModel<TripViewModel>()
    val state by viewModel.viewState.collectAsState()
    
    TripView(
        user = user,
        state = state,
        event = viewModel::obtainEvent,
        openTrip = openTrip
    )
}


