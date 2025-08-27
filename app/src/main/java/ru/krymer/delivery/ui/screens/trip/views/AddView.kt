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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.screens.trip.models.TripEvent
import ru.krymer.delivery.ui.screens.trip.models.TripViewState
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.convertToTextDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripView(
    state: TripViewState, event: (TripEvent) -> Unit
) {
    val routes = state.listRoute.collectAsState().value
    val couriers = state.listCourier.collectAsState().value
    if (routes.isNotEmpty() && couriers.isNotEmpty()) {
        Column(modifier = Modifier.padding(5.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .height(60.dp)
                    .background(
                        color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        event(TripEvent.OpenHideDatePickerForAddTrip)
                    }) {
                Text(
                    text = convertToTextDate(state.currentDate),
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .align(Alignment.Center),
                    color = AppTheme.colors.onSecondary,
                    style = AppTheme.typography.titleMedium
                )

                if (state.dropDownStateDatePicker) {
                    val datePickerState = rememberDatePickerState()
                    DatePickerDialog(onDismissRequest = {
                        event(
                            TripEvent.OpenHideDatePickerForAddTrip
                        )
                    }, confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                event(TripEvent.ChangeDate(it))
                                event(
                                    TripEvent.OpenHideDatePickerForAddTrip
                                )
                            }
                        }) {
                            Text(
                                stringResource(id = R.string.ok),
                                style = AppTheme.typography.titleMedium
                            )
                        }
                    }, dismissButton = {
                        TextButton(onClick = {
                            event(
                                TripEvent.OpenHideDatePickerForAddTrip
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
                            event(TripEvent.OpenHideDropDownMenuWithRoutes)
                        }, verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.currentRoute?.name ?: "Маршрут не выбран",
                        modifier = Modifier.padding(start = 15.dp),
                        color = AppTheme.colors.onSecondary,
                        style = AppTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 15.dp)
                    )
                    DropdownMenu(expanded = state.dropDownStateRoutes, onDismissRequest = {
                        event(TripEvent.OpenHideDropDownMenuWithRoutes)
                    }) {
                        DropdownMenuItem(text = {
                            Text(text = "Не выбран", style = AppTheme.typography.titleSmall)
                        }, onClick = {
                            event(TripEvent.SelectRoute(null))
                            event(TripEvent.OpenHideDropDownMenuWithRoutes)
                        })
                        routes.forEach {
                            DropdownMenuItem(text = { Text(text = it.name,
                                style = AppTheme.typography.titleSmall) }, onClick = {
                                event(TripEvent.SelectRoute(it))
                                event(TripEvent.OpenHideDropDownMenuWithRoutes)
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
                            event(TripEvent.OpenHideDropDownMenuWithCouriers)
                        }, verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.currentCourier?.name ?: "Курьер не выбран",
                        modifier = Modifier.padding(start = 15.dp),
                        color = AppTheme.colors.onSecondary,
                        style = AppTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 15.dp)
                    )
                    DropdownMenu(expanded = state.dropDownStateCourier,
                        onDismissRequest = {
                            event(TripEvent.OpenHideDropDownMenuWithCouriers)
                        }) {
                        DropdownMenuItem(text = {
                            Text(text = "Не выбран", style = AppTheme.typography.titleSmall)
                        }, onClick = {
                            event(TripEvent.SelectCourier(null))
                            event(TripEvent.OpenHideDropDownMenuWithCouriers)
                        })
                        val list = state.listCourier.collectAsState().value
                        list.forEach {
                            DropdownMenuItem(text = { Text(text = it.name,
                                style = AppTheme.typography.titleSmall) }, onClick = {
                                event(TripEvent.SelectCourier(it))
                                event(TripEvent.OpenHideDropDownMenuWithCouriers)
                            })
                        }
                    }
                }
            }

        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .background(
                    color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp)
                )
        ) {
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