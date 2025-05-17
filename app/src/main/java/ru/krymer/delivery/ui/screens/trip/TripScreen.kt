package ru.krymer.delivery.ui.screens.trip

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.components.CommonAlertAddDialog
import ru.krymer.delivery.ui.components.CommonDeleteDialog
import ru.krymer.delivery.ui.navigation.NavigationTree
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.screens.trip.models.TripAction
import ru.krymer.delivery.ui.screens.trip.models.TripEvent
import ru.krymer.delivery.ui.screens.trip.views.AddTripView
import ru.krymer.delivery.ui.screens.trip.views.FilterView
import ru.krymer.delivery.ui.screens.trip.views.TripItem
import ru.krymer.delivery.ui.screens.trip.views.UpdateTripView

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TripScreen(
    viewModel: TripViewModel, navController: NavController
) {
    val sharedViewModel = hiltViewModel<SharedViewModel>()
    val viewState = viewModel.viewState.collectAsState().value
    val trips = viewState.trips.collectAsState().value
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(key1 = Unit) {
        onDispose {
            viewModel.obtainEvent(TripEvent.TripActionInvoked)
        }
    }

    LaunchedEffect(key1 = trips) {
        coroutineScope.launch {
            delay(300)
            lazyListState.animateScrollToItem(1)
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize().padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        item {
            Spacer(
                modifier = Modifier
                    .fillParentMaxHeight(0.7f)
                    .fillMaxWidth()
            )
        }
        item {
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
                        .size(50.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.filter),
                    contentDescription = "sort",
                    modifier = Modifier
                        .clickable(onClick = {
                            viewModel.obtainEvent(TripEvent.SwitcherFilterDialog)
                        })
                        .size(50.dp)
                )
                if (sharedViewModel.initSysAdm()) {
                    Image(
                        painter = painterResource(id = R.drawable.add),
                        contentDescription = "add route",
                        modifier = Modifier
                            .clickable(onClick = {
                                viewModel.obtainEvent(TripEvent.ShowAddDialog)
                            })
                            .size(50.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (trips.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.Center),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                }
            }
        } else {

            items(viewState.trips.value) { trip ->
                TripItem(
                    trip = trip,
                    updateTrip = {
                        viewModel.obtainEvent(TripEvent.ShowChangeCourierDialog(it))
                    },
                    deleteTrip = {
                        viewModel.obtainEvent(TripEvent.ShowDeleteDialog(trip = it))
                    },
                    routeToTrip = {
                        viewModel.obtainEvent(TripEvent.TripItemClicked(it))
                    },
                    sharedViewModel = sharedViewModel
                )
                Spacer(modifier = Modifier.height(3.dp))
            }
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

    if (viewState.isShowFilterDialog) {
        CommonAlertAddDialog(
            onDismiss = {
                viewModel.obtainEvent(TripEvent.SwitcherFilterDialog)
            },
            content = {
                FilterView(changeFilterCourier = {
                    viewModel.obtainEvent(TripEvent.ChangerCheckBoxFilterCourier(it))
                }, viewState = viewState)
            },
            confirm = { viewModel.obtainEvent(TripEvent.SubmitFilter) }
        )
    }



    if (viewState.showAddSheetDialog) {
        CommonAlertAddDialog(
            confirm = { viewModel.obtainEvent(TripEvent.TripSaveAction) },
            onDismiss = {
                viewModel.obtainEvent(TripEvent.DismissAddDialog)
            },
            content = {
                AddTripView(
                    viewState = viewState, viewModel = viewModel
                )
            })
    }

    if (viewState.showUpdateSheetDialog) {
        CommonAlertAddDialog(
            confirm = { viewModel.obtainEvent(TripEvent.TripUpdateAction) },
            onDismiss = {
                viewModel.obtainEvent(TripEvent.DismissUpdateDialog)
            },
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


