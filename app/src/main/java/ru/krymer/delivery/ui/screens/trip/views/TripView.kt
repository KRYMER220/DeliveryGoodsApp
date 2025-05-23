package ru.krymer.delivery.ui.screens.trip.views

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.ui.components.CommonAlertAddDialog
import ru.krymer.delivery.ui.components.CommonDeleteDialog
import ru.krymer.delivery.ui.navigation.NavigationTree
import ru.krymer.delivery.ui.screens.trip.models.TripAction
import ru.krymer.delivery.ui.screens.trip.models.TripEvent
import ru.krymer.delivery.ui.screens.trip.models.TripViewState
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.convertToTextDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TripView(
    navigateToPreviousScreen: () -> Unit = {},
    event: (TripEvent) -> Unit = {},
    state: TripViewState,
    user: UserModel,
    navigateTo: (String) -> Unit = {}
) {

    val lazyListState = rememberLazyListState()
    val trips = state.trips.collectAsState().value
    val coroutineScope = rememberCoroutineScope()
    var isFirstLoad by remember { mutableStateOf(true) }

    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            visibleItemsInfo.any { it.index == totalItems - 1 } && totalItems > 0
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            event(TripEvent.LoadMoreTrips)
        }
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            event(TripEvent.TripActionDefault)
        }
    }

    LaunchedEffect(trips) {
        if (trips.isNotEmpty() && isFirstLoad) {
            isFirstLoad = false
            coroutineScope.launch {
                delay(300)
                lazyListState.animateScrollToItem(1)
            }
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
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
                            navigateToPreviousScreen
                        })
                        .size(50.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.filter),
                    contentDescription = "sort",
                    modifier = Modifier
                        .clickable(onClick = {
                            event(TripEvent.OpenFilterTrip)
                        })
                        .size(50.dp)
                )
                if (user.isSysOrAdmin()) {
                    Image(
                        painter = painterResource(id = R.drawable.add),
                        contentDescription = "add trip",
                        modifier = Modifier
                            .clickable(onClick = {
                                event(TripEvent.ShowHideAddDialog)
                            })
                            .size(50.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (trips.isNotEmpty()) {
            items(items = trips, key = { it.id }) { trip ->
                TripItem(
                    trip = trip, updateTrip = {
                    event(TripEvent.ShowUpdateDialog(it))
                }, deleteTrip = {
                    event(TripEvent.ShowDeleteDialog(trip = it))
                }, openTrip = {
                    event(TripEvent.OpenTrip(it))
                }, user = user
                )
                Spacer(modifier = Modifier.height(3.dp))
            }
        } else {
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
        }
    }

    if (state.showDeleteDialog) {
        state.deleteTrip?.let {
            CommonDeleteDialog(
                itemName = it.nameRoute,
                isVisible = true,
                onDismiss = { event(TripEvent.DismissDeleteDialog) },
                onConfirm = { event(TripEvent.DeleteTrip) })
        }
    }

    if (state.isShowFilterDialog) {
        CommonAlertAddDialog(onDismiss = {
            event(TripEvent.OpenFilterTrip)
        }, content = {
            FilterView(changeFilterCourier = {
                event(TripEvent.ChangerCheckBoxFilterCourier(it))
            }, state = state)
        }, confirm = { event(TripEvent.SubmitFilter) })
    }



    if (state.stateAddDialog) {
        CommonAlertAddDialog(confirm = { event(TripEvent.SaveTrip) }, onDismiss = {
            event(TripEvent.ShowHideAddDialog)
        }, content = {
            AddTripView(
                state = state, event = event
            )
        })
    }

    if (state.showUpdateSheetDialog) {
        CommonAlertAddDialog(confirm = { event(TripEvent.UpdateTrip) }, onDismiss = {
            event(TripEvent.DismissUpdateDialog)
        }, content = {
            UpdateTripView(
                state = state, event = event
            )
        })
    }

    LaunchedEffect(key1 = state.tripAction) {
        when (state.tripAction) {
            TripAction.None -> {}
            is TripAction.OpenShops -> {
                navigateTo(NavigationTree.Shop.name)
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TripItem(
    trip: TripModel,
    updateTrip: (TripModel) -> Unit,
    deleteTrip: (TripModel) -> Unit, openTrip: (TripModel) -> Unit, user: UserModel
) {
    Box(
        modifier = Modifier
            .combinedClickable(onLongClick = { updateTrip(trip) }, onClick = { openTrip(trip) })
            .background(
                color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp)
            )
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    style = AppTheme.typography.titleMedium,
                    text = trip.nameRoute,
                    color = AppTheme.colors.onSecondary
                )
                Text(
                    style = AppTheme.typography.titleSmall,
                    text = convertToTextDate(trip.date),
                    color = AppTheme.colors.onSecondary
                )
                Text(
                    style = AppTheme.typography.titleSmall,
                    text = trip.nameCourier,
                    color = AppTheme.colors.onSecondary
                )
            }
            if (user.isSysOrAdmin()) {
                Image(
                    contentDescription = "delete trip",
                    painter = painterResource(id = R.drawable.delete),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = { deleteTrip(trip) })
                )
            }
        }
    }
}