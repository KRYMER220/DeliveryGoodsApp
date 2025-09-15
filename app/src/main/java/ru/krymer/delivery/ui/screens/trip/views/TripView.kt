package ru.krymer.delivery.ui.screens.trip.views

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.ui.components.CommonAlertAddDialog
import ru.krymer.delivery.ui.components.CommonDeleteDialog
import ru.krymer.delivery.ui.screens.trip.models.TripEvent
import ru.krymer.delivery.ui.screens.trip.models.TripViewState
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.convertToTextDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TripView(
    event: (TripEvent) -> Unit = {},
    state: TripViewState,
    user: UserModel,
    openTrip: (TripModel) -> Unit = {}
) {
    val lazyListState = rememberLazyListState()
    val trips = state.trips
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
                if (!state.lightVersion) {
                    Image(
                        painter = painterResource(id = R.drawable.filter),
                        contentDescription = "sort",
                        modifier = Modifier
                            .clickable(onClick = {
                                event(TripEvent.OpenFilterTrip)
                            })
                            .size(60.dp)
                    )
                } else {
                    Spacer(modifier = Modifier)
                }
                if (user.isSysOrAdmin()) {
                    Image(
                        painter = painterResource(id = R.drawable.add),
                        contentDescription = "add trip",
                        modifier = Modifier
                            .clickable(onClick = {
                                event(TripEvent.ShowHideAddDialog)
                            })
                            .size(60.dp)
                    )
                }
            }
        }

        if (trips.isNotEmpty()) {
            items(items = trips) { trip ->
                TripItem(
                    trip = trip, updateTrip = {
                    if (user.isSysOrAdmin()) event(TripEvent.ShowUpdateDialog(it))
                }, deleteTrip = {
                    event(TripEvent.ShowDeleteDialog(trip = it))
                }, openTrip = {
                    event(TripEvent.SyncTrip(it))
                    openTrip(it)
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
                event(TripEvent.IsFilter(it))
            }, state = state, event = event)
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