package ru.krymer.delivery.ui.screens.analitic

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.screens.analitic.view.AnaliticView
import ru.krymer.delivery.utills.getCurrentDayRangeTimestamps

@ExperimentalMaterial3Api
@Composable
fun AnaliticScreen(
    popBackStack: () -> Unit
) {
    val viewModel = hiltViewModel<AnaliticViewModel>()
    AnaliticView(state = viewModel.viewState.collectAsState().value, popBackStack = popBackStack, event = viewModel::obtainEvent)
}

@ExperimentalMaterial3Api
@Composable
fun DateRangePickerModal(
    onDateRangeSelected: (Pair<Long, Long>) -> Unit, onDismiss: () -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState()

    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = {
        TextButton(onClick = {
            if (dateRangePickerState.selectedStartDateMillis != null && dateRangePickerState.selectedEndDateMillis != null) {
                onDateRangeSelected(
                    Pair(
                        first = dateRangePickerState.selectedStartDateMillis
                            ?: getCurrentDayRangeTimestamps().first,
                        second = dateRangePickerState.selectedEndDateMillis
                            ?: getCurrentDayRangeTimestamps().second
                    )
                )
                onDismiss()
            }
        }) {
            Text(stringResource(id = R.string.ok))
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text(stringResource(id = R.string.close))
        }
    }) {
        DateRangePicker(
            state = dateRangePickerState,
            title = {
                Text(
                    text = stringResource(R.string.range_select)
                )
            },
            showModeToggle = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp)
        )
    }
}
