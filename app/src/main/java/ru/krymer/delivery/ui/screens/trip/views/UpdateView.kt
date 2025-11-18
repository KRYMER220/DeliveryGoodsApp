package ru.krymer.delivery.ui.screens.trip.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.components.CustomCircularProgressIndicator
import ru.krymer.delivery.ui.components.GenericDropdown
import ru.krymer.delivery.ui.screens.trip.models.TripEvent
import ru.krymer.delivery.ui.screens.trip.models.TripViewState
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.convertToTextDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateTripView(
    state: TripViewState,
    event: (TripEvent) -> Unit
) {
    val routes = state.listRoute
    val couriers = state.listCourier
    var toggleMenuRoute by remember { mutableStateOf(false) }
    var toggleMenuCourier by remember { mutableStateOf(false) }
    var toggleDatePicker by remember { mutableStateOf(false) }

    if (routes.isEmpty() || couriers.isEmpty() || state.currentRoute == null || state.currentCourier == null) {
        CustomCircularProgressIndicator()
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        DatePickerField(
            date = state.currentDate,
            onDateClick = { toggleDatePicker = true },
            showPicker = toggleDatePicker,
            onDismiss = { toggleDatePicker = false },
            onDateSelected = { date ->
                event(TripEvent.ChangeDate(date))
                toggleDatePicker = false
            }
        )

        GenericDropdown(
            selectedItem = state.currentRoute,
            items = routes,
            expanded = toggleMenuRoute,
            onExpandedChange = { toggleMenuRoute = !toggleMenuRoute },
            itemLabel = { it.name },
            placeholder = stringResource(R.string.not_select_route),
            onItemSelected = { event(TripEvent.SelectRoute(it)) }
        )

        GenericDropdown(
            selectedItem = state.currentCourier,
            items = couriers,
            expanded = toggleMenuCourier,
            onExpandedChange = { toggleMenuCourier = !toggleMenuCourier },
            itemLabel = { it.name },
            placeholder = stringResource(R.string.not_select_courier),
            onItemSelected = { event(TripEvent.SelectCourier(it)) }
        )

        Spacer(modifier = Modifier.height(10.dp))
        CommonTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.salary,
            placeholder = stringResource(R.string.salary),
            changerText = { event(TripEvent.ChangeSalaryTrip(it)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    date: Long,
    onDateClick: () -> Unit,
    showPicker: Boolean,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .height(60.dp)
            .background(
                color = AppTheme.colors.secondary,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onDateClick)
    ) {
        Text(
            text = convertToTextDate(date),
            modifier = Modifier
                .padding(start = 15.dp)
                .align(Alignment.Center),
            color = AppTheme.colors.onSecondary,
            style = AppTheme.typography.titleMedium
        )

        if (showPicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date)
            DatePickerDialog(
                onDismissRequest = onDismiss,
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let(onDateSelected)
                    }) {
                        Text(
                            stringResource(id = R.string.ok),
                            style = AppTheme.typography.titleMedium
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(id = R.string.close))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}