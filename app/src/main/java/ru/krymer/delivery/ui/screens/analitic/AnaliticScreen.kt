package ru.krymer.delivery.ui.screens.analitic

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.screens.analitic.models.AnaliticAction
import ru.krymer.delivery.ui.screens.analitic.models.AnaliticEvent
import ru.krymer.delivery.ui.screens.analitic.view.AnaliticFactoryView
import ru.krymer.delivery.ui.screens.analitic.view.AnaliticClientView
import ru.krymer.delivery.ui.screens.analitic.view.AnaliticTripView
import ru.krymer.delivery.ui.screens.analitic.view.AnaliticView
import ru.krymer.delivery.ui.screens.analitic.view.LogView
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.convertToTextDate
import ru.krymer.delivery.utills.getCurrentDayRangeTimestamps

@ExperimentalMaterial3Api
@Composable
fun AnaliticScreen(
    viewModel: AnaliticViewModel, navController: NavController
) {
    val viewState = viewModel.viewState.collectAsState().value
    val dateRange = viewState.dateRangeForSearch
    Column(
        modifier = Modifier
            .padding(15.dp)
            .fillMaxSize()
    ) {
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
                        when (viewState.analiticAction) {
                            AnaliticAction.None -> {
                                navController.popBackStack()
                            }

                            else -> {
                                viewModel.obtainEvent(AnaliticEvent.AnaliticActionInvoked)
                            }
                        }
                    })
                    .size(40.dp)
            )

            when (viewState.analiticAction) {
                AnaliticAction.None -> {}
                AnaliticAction.OpenLog -> {}
                else -> {
                    Text(
                        text = convertToTextDate(dateRange.first) + " - " + convertToTextDate(
                            dateRange.second
                        ), color = AppTheme.colors.onSecondary, fontSize = 14.sp
                    )
                    Image(
                        painter = painterResource(id = R.drawable.date_range),
                        contentDescription = "date",
                        modifier = Modifier
                            .clickable(onClick = {
                                when (viewState.analiticAction) {
                                    AnaliticAction.OpenAll -> {
                                        viewModel.obtainEvent(AnaliticEvent.ShowDatePicker)
                                    }

                                    AnaliticAction.OpenTrip -> {
                                        viewModel.obtainEvent(AnaliticEvent.ShowDatePicker)
                                    }
                                    AnaliticAction.OpenClient -> {
                                        viewModel.obtainEvent(AnaliticEvent.ShowDatePicker)
                                    }

                                    else -> {}

                                }
                            })
                            .size(40.dp)
                    )
                }
            }
        }
        when (viewState.analiticAction) {
            AnaliticAction.None -> {
                AnaliticView(onClickClient = {
                    viewModel.obtainEvent(AnaliticEvent.ClientInfoClickedToOpen)
                }, onClickAll = {
                    viewModel.obtainEvent(AnaliticEvent.AllInfoClickedToOpen)
                }, onClickTrip = {
                    viewModel.obtainEvent(AnaliticEvent.TripInfoClickedToOpen)
                }, onClickLogs = {
                    viewModel.obtainEvent(AnaliticEvent.ShowLogView)
                })
            }

            AnaliticAction.OpenAll -> {
                AnaliticFactoryView(viewModel = viewModel)
            }

            AnaliticAction.OpenClient -> {
                AnaliticClientView(viewModel = viewModel)
            }

            AnaliticAction.OpenTrip -> {
                AnaliticTripView(viewModel = viewModel)
            }

            AnaliticAction.OpenLog -> {
                LogView(viewModel = viewModel)
            }
        }
    }

    if (viewState.isShowDatePicker) {
        DateRangePickerModal(onDateRangeSelected = {
            viewModel.obtainEvent(AnaliticEvent.ChangeRangeDatePicker(it))
        }, onDismiss = {
            viewModel.obtainEvent(AnaliticEvent.DismissDatePicker)
        })
    }
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
