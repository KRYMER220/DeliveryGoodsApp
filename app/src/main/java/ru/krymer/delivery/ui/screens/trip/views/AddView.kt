package ru.krymer.delivery.ui.screens.trip.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.components.CustomCircularProgressIndicator
import ru.krymer.delivery.ui.components.GenericDropdown
import ru.krymer.delivery.ui.screens.trip.models.TripEvent
import ru.krymer.delivery.ui.screens.trip.models.TripViewState
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.convertToTextDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripView(
    state: TripViewState, event: (TripEvent) -> Unit
) {
    var toggleMenuRoute by remember { mutableStateOf(false) }
    var toggleMenuCourier by remember { mutableStateOf(false) }
    var toggleDatePicker by remember { mutableStateOf(false) }
    val routes = state.listRoute
    val couriers = state.listCourier
    if (routes.isNotEmpty() && couriers.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .height(60.dp)
                    .background(
                        color = AppTheme.colors.secondary, shape = RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        toggleDatePicker = true
                    }) {
                Text(
                    text = convertToTextDate(state.currentDate),
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .align(Alignment.Center),
                    color = AppTheme.colors.onSecondary,
                    style = AppTheme.typography.titleMedium
                )

                if (toggleDatePicker) {
                    val datePickerState = rememberDatePickerState()
                    DatePickerDialog(onDismissRequest = {
                        toggleDatePicker = false
                    }, confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                event(TripEvent.ChangeDate(it))
                                toggleDatePicker = false
                            }
                        }) {
                            Text(
                                stringResource(id = R.string.ok),
                                style = AppTheme.typography.titleMedium
                            )
                        }
                    }, dismissButton = {
                        TextButton(onClick = {
                            toggleDatePicker = false
                        }) {
                            Text(stringResource(id = R.string.close))
                        }
                    }) {
                        DatePicker(state = datePickerState)
                    }
                }
            }

            GenericDropdown(
                selectedItem = state.currentRoute,
                items = routes,
                expanded = toggleMenuRoute,
                onExpandedChange = { expanded ->
                    toggleMenuRoute = !toggleMenuRoute
                },
                itemLabel = { it.name },
                placeholder = stringResource(R.string.not_select_route),
                onItemSelected = { selectedRoute ->
                    event(TripEvent.SelectRoute(selectedRoute))
                }
            )

            GenericDropdown(
                selectedItem = state.currentCourier,
                items = state.listCourier,
                expanded = toggleMenuCourier,
                onExpandedChange = { expanded ->
                    toggleMenuCourier = !toggleMenuCourier
                },
                itemLabel = { it.name },
                placeholder = stringResource(R.string.not_select_courier),
                onItemSelected = { selectedCourier ->
                    event(TripEvent.SelectCourier(selectedCourier))
                }
            )
        }
    } else {
        CustomCircularProgressIndicator()
    }
}
