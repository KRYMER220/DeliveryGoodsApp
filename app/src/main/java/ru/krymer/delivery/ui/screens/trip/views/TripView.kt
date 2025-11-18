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
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
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
    
    val trips = if (state.isInitialLoad) emptyList() else state.trips

    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) return@derivedStateOf false
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            visibleItemsInfo.any { it.index == totalItems - 1 }
        }
    }

    val showStickyHeader by remember {
        derivedStateOf {
            val firstIndex = lazyListState.firstVisibleItemIndex
            firstIndex > 1 || (firstIndex == 1 && lazyListState.firstVisibleItemScrollOffset > 0)
        }
    }

    LaunchedEffect(state.isInitialLoad) {
        if (state.isInitialLoad) {
            event(TripEvent.RefreshTrips)
        }
    }

    LaunchedEffect(shouldLoadMore, state.hasMore, state.isLoading) {
        if (shouldLoadMore && state.hasMore && !state.isLoading) {
            event(TripEvent.LoadMoreTrips)
        }
    }

    val currentTime = System.currentTimeMillis()
    val (futureTrips, pastTrips) = remember(trips, currentTime) {
        trips.partition { it.date > currentTime }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        .fillParentMaxHeight(0.43f)
                        .fillMaxWidth()
                )
            }

            item {
                HeaderContentTrip(
                    state = state,
                    user = user,
                    event = event,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(10.dp)
                )
            }

            when {
                trips.isEmpty() && state.isLoading -> {
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
                trips.isNotEmpty() -> {
                    if (futureTrips.isNotEmpty()) {
                        if (!state.lightVersion) {
                            item {
                                Text(
                                    text = "Предстоящие рейсы",
                                    style = AppTheme.typography.titleSmall,
                                    color = AppTheme.colors.onSecondary,
                                )
                            }
                        }

                        items(items = futureTrips, key = { it.id }) { trip ->
                            TripItem(
                                trip = trip,
                                updateTrip = {
                                    if (user.isSysOrAdmin()) event(TripEvent.ToggleUpdateDialog(it))
                                },
                                deleteTrip = {
                                    event(TripEvent.ToggleDeleteDialog(trip = it))
                                },
                                openTrip = openTrip,
                                user = user
                            )
                        }

                        if (pastTrips.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(5.dp))
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = AppTheme.colors.onSecondary.copy(alpha = 0.3f)
                                )
                                Spacer(modifier = Modifier.height(5.dp))
                            }
                        }
                    }

                    if (pastTrips.isNotEmpty()) {
                        if (!state.lightVersion) {
                            item {
                                Text(
                                    text = "Текущие и завершенные рейсы",
                                    color = AppTheme.colors.onSecondary,
                                    style = AppTheme.typography.titleSmall,
                                )
                            }
                        }

                        items(items = pastTrips, key = { it.id }) { trip ->
                            TripItem(
                                trip = trip,
                                updateTrip = {
                                    if (user.isSysOrAdmin()) event(TripEvent.ToggleUpdateDialog(it))
                                },
                                deleteTrip = {
                                    event(TripEvent.ToggleDeleteDialog(trip = it))
                                },
                                openTrip = openTrip,
                                user = user
                            )
                        }
                    }
                }
            }

        }
        if (showStickyHeader) {
            HeaderContentTrip(
                state = state,
                user = user,
                event = event,
                modifier = Modifier
                    .background(color = AppTheme.colors.onPrimary)
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(10.dp)
                    .pointerInput(Unit) {}
            )
        }
    }


    if (state.toggleDeleteTrip) {
        state.trip?.let {
            CommonDeleteDialog(
                itemName = it.nameRoute,
                isVisible = true,
                onDismiss = { event(TripEvent.ToggleDeleteDialog(null)) },
                onConfirm = { event(TripEvent.DeleteTrip) })
        }
    }

    if (state.isShowFilterDialog) {
        CommonAlertAddDialog(onDismiss = {
            event(TripEvent.ToggleFilterDialog)
        }, content = {
            FilterView(changeFilterCourier = {
                event(TripEvent.IsFilter(it))
            }, state = state, event = event)
        }, confirm = { event(TripEvent.SubmitFilter) })
    }



    if (state.toggleAddTrip) {
        CommonAlertAddDialog(confirm = { event(TripEvent.SaveTrip) }, onDismiss = {
            event(TripEvent.ToggleAddDialog)
        }, content = {
            AddTripView(
                state = state, event = event
            )
        })
    }

    if (state.toggleUpdateTrip) {
        CommonAlertAddDialog(confirm = { event(TripEvent.UpdateTrip) }, onDismiss = {
            event(TripEvent.ToggleUpdateDialog(null))
        }, content = {
            UpdateTripView(
                state = state, event = event
            )
        })
    }
}

@Composable
fun HeaderContentTrip(
    state: TripViewState, user: UserModel, event: (TripEvent) -> Unit, modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!state.lightVersion) {
            Image(
                painter = painterResource(id = R.drawable.filter),
                contentDescription = "sort",
                modifier = Modifier
                    .clickable(onClick = {
                        event(TripEvent.ToggleFilterDialog)
                    })
                    .size(60.dp)
            )
        }
        if (user.isSysOrAdmin()) {
            Image(
                painter = painterResource(id = R.drawable.add),
                contentDescription = "add trip",
                modifier = Modifier
                    .clickable(onClick = {
                        event(TripEvent.ToggleAddDialog)
                    })
                    .size(60.dp)
            )
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
                    .weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)
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
                    text = trip.nameCourier, style = AppTheme.typography.titleSmall,
                    color = AppTheme.colors.onSecondary
                )
                if (user.isSysOrAdmin()) {
                    Text(
                        text = "Гcм: " + trip.millage.toInt()
                            .toString() + "  ЗП: ${trip.salary.toInt()} / ${trip.salaryCourier.toInt()}",
                        style = AppTheme.typography.titleSmall,
                        color = AppTheme.colors.onSecondary
                    )
                }
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