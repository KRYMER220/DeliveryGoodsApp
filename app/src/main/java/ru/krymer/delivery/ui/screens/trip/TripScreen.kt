package ru.krymer.delivery.ui.screens.trip

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.components.CommonAlertAddDialog
import ru.krymer.delivery.ui.components.CommonDeleteDialog
import ru.krymer.delivery.ui.navigation.NavigationTree
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.screens.trip.models.TripAction
import ru.krymer.delivery.ui.screens.trip.models.TripEvent
import ru.krymer.delivery.ui.screens.trip.views.AddTripView
import ru.krymer.delivery.ui.screens.trip.views.UpdateTripView
import ru.krymer.delivery.ui.screens.trip.views.TripView

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TripScreen(
    viewModel: TripViewModel, navController: NavController
) {
    val sharedViewModel = hiltViewModel<SharedViewModel>()
    val viewState = viewModel.viewState.collectAsState().value
    val trips = viewState.listTrip.collectAsState().value

    DisposableEffect(key1 = Unit) {
        onDispose {
            viewModel.obtainEvent(TripEvent.TripActionInvoked)
        }
    }

    Column(modifier = Modifier.padding(15.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.back_stack),
                contentDescription = "exit",
                modifier = Modifier
                    .clickable(onClick = {
                        viewModel.obtainEvent(TripEvent.TripActionInvoked)
                        navController.popBackStack()
                    })
                    .size(40.dp)
            )
            if (sharedViewModel.initSysAdm()) {
                Image(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = "add route",
                    modifier = Modifier
                        .clickable(onClick = {
                            viewModel.obtainEvent(TripEvent.ShowAddDialog)
                        })
                        .size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(15.dp))
        if (trips.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.Center),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            }
        } else {
            TripView(viewState = viewState, onItemLongClicked = {
                viewModel.obtainEvent(TripEvent.ShowChangeCourierDialog(it))
            }, onItemDelete = {
                viewModel.obtainEvent(
                    TripEvent.ShowDeleteDialog(trip = it)
                )
            }, onItemClick = {
                viewModel.obtainEvent(TripEvent.TripItemClicked(it))
            }, sharedViewModel = sharedViewModel)

        }
    }

    if (viewState.showDeleteDialog) {
        viewState.deleteTrip?.let {
            CommonDeleteDialog(itemName = it.nameRoute,
                isVisible = true,
                onDismiss = { viewModel.obtainEvent(TripEvent.DismissDeleteDialog) },
                onConfirm = { viewModel.obtainEvent(TripEvent.DeleteTrip) })
        }
    }



    if (viewState.showAddSheetDialog) {
        CommonAlertAddDialog(
            onConfirmation = { viewModel.obtainEvent(TripEvent.TripSaveAction) },
            onDismissRequest = {
                viewModel.obtainEvent(TripEvent.DismissAddDialog)
            },
            onConfirm = {},
            content = {
                AddTripView(
                    viewState = viewState, viewModel = viewModel
                )
            })
    }

    if (viewState.showUpdateSheetDialog) {
        CommonAlertAddDialog(
            onConfirmation = { viewModel.obtainEvent(TripEvent.TripUpdateAction) },
            onDismissRequest = {
                viewModel.obtainEvent(TripEvent.DismissUpdateDialog)
            },
            onConfirm = {},
            content = {
                UpdateTripView(
                    viewState = viewState, viewModel = viewModel
                )
            })
    }

    LaunchedEffect(key1 = viewState.tripAction) {
        when (viewState.tripAction) {
            TripAction.None -> {}
            is TripAction.OpenShops -> {
                navController.navigate(NavigationTree.Shop.name)
            }
        }
    }
}


