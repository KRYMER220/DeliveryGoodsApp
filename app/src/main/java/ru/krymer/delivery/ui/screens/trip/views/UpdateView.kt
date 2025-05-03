package ru.krymer.delivery.ui.screens.trip.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.screens.trip.TripViewModel
import ru.krymer.delivery.ui.screens.trip.models.TripEvent
import ru.krymer.delivery.ui.screens.trip.models.TripViewState
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.convertToTextDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateTripView(
    viewState: TripViewState,
    viewModel: TripViewModel
) {
    val routes = viewState.listRoute.collectAsState().value
    val couriers = viewState.listCourier.collectAsState().value
    if (routes.isNotEmpty() && couriers.isNotEmpty()) {
        viewState.currentRoute?.let {  r ->
            viewState.currentCourier?.let { c ->
                Column {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .height(60.dp)
                        .background(
                            color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            viewModel.obtainEvent(TripEvent.ChangeDropDownStateDatePicker(true))
                        }) {
                        Text(
                            text = convertToTextDate(viewState.currentDate),
                            modifier = Modifier
                                .padding(start = 15.dp)
                                .align(Alignment.Center),
                            color = AppTheme.colors.onSecondary
                        )
                        if (viewState.dropDownStateDatePicker) {
                            val datePickerState = rememberDatePickerState()
                            DatePickerDialog(onDismissRequest = {
                                viewModel.obtainEvent(
                                    TripEvent.ChangeDropDownStateDatePicker(
                                        false
                                    )
                                )
                            }, confirmButton = {
                                TextButton(onClick = {
                                    viewModel.obtainEvent(TripEvent.ChangeDate(datePickerState.selectedDateMillis!!))
                                    viewModel.obtainEvent(
                                        TripEvent.ChangeDropDownStateDatePicker(
                                            false
                                        )
                                    )
                                }) {
                                    Text(stringResource(id = R.string.ok))
                                }

                            }, dismissButton = {
                                TextButton(onClick = {
                                    viewModel.obtainEvent(
                                        TripEvent.ChangeDropDownStateDatePicker(
                                            false
                                        )
                                    )
                                }) {
                                    Text(stringResource(id = R.string.close))
                                }
                            }) {
                                DatePicker(state = datePickerState)
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .background(
                                color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clickable {
                                    viewModel.obtainEvent(TripEvent.ChangeDropDownStateTrip(true))
                                }, verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = r.name,
                                modifier = Modifier.padding(start = 15.dp),
                                color = AppTheme.colors.onSecondary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 15.dp)
                            )
                            DropdownMenu(expanded = viewState.dropDownStateTrips, onDismissRequest = {
                                viewModel.obtainEvent(TripEvent.ChangeDropDownStateTrip(false))
                            }) {
                                routes.forEach {
                                    DropdownMenuItem(text = { Text(text = it.name) }, onClick = {
                                        viewModel.obtainEvent(
                                            TripEvent.SelectDropDownRoute(it)
                                        )
                                        viewModel.obtainEvent(TripEvent.ChangeDropDownStateTrip(false))
                                    })
                                }
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .background(
                                color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clickable {
                                    viewModel.obtainEvent(TripEvent.ChangeDropDownStateCourier(true))
                                }, verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = c.name,
                                modifier = Modifier.padding(start = 15.dp),
                                color = AppTheme.colors.onSecondary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 15.dp)
                            )
                            DropdownMenu(expanded = viewState.dropDownStateCourier,
                                onDismissRequest = {
                                    viewModel.obtainEvent(TripEvent.ChangeDropDownStateCourier(false))
                                }) {
                                couriers.forEach {
                                    DropdownMenuItem(text = { Text(text = it.name) }, onClick = {
                                        viewModel.obtainEvent(
                                            TripEvent.SelectDropDownCourier(it)
                                        )
                                        viewModel.obtainEvent(TripEvent.ChangeDropDownStateCourier(false))
                                    })
                                }
                            }
                        }
                    }

                }
            }
        }

    } else {
        Box(modifier = Modifier.fillMaxWidth()) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.Center),
                strokeWidth = 2.dp,
                color = AppTheme.colors.onSecondary
            )
        }
    }

}