package ru.krymer.delivery.ui.screens.analitic.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.screens.analitic.DateRangePickerModal
import ru.krymer.delivery.ui.screens.analitic.models.AnaliticAction
import ru.krymer.delivery.ui.screens.analitic.models.AnaliticEvent
import ru.krymer.delivery.ui.screens.analitic.models.AnaliticViewState
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.convertToTextDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnaliticView(
    state: AnaliticViewState,
    popBackStack: () -> Unit,
    event: (AnaliticEvent) -> Unit
) {
    val dateRange = state.dateRangeForSearch
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
                        when (state.analiticAction) {
                            AnaliticAction.None -> {
                                popBackStack()
                            }

                            else -> {
                                event(AnaliticEvent.AnaliticActionInvoked)
                            }
                        }
                    })
                    .size(60.dp)
            )

            when (state.analiticAction) {
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
                                when (state.analiticAction) {
                                    AnaliticAction.OpenAll -> {
                                        event(AnaliticEvent.ShowDatePicker)
                                    }

                                    AnaliticAction.OpenTrip -> {
                                        event(AnaliticEvent.ShowDatePicker)
                                    }

                                    AnaliticAction.OpenClient -> {
                                        event(AnaliticEvent.ShowDatePicker)
                                    }

                                    else -> {}

                                }
                            })
                            .size(60.dp)
                    )
                }
            }
        }
        when (state.analiticAction) {
            AnaliticAction.None -> {
                AnaliticMenu(onClickClient = {
                    event(AnaliticEvent.ClientInfoClickedToOpen)
                }, onClickAll = {
                    event(AnaliticEvent.AllInfoClickedToOpen)
                }, onClickTrip = {
                    event(AnaliticEvent.TripInfoClickedToOpen)
                }, onClickLogs = {
                    event(AnaliticEvent.ShowLogView)
                })
            }

            AnaliticAction.OpenAll -> {
                AnaliticFactoryView(state = state)
            }

            AnaliticAction.OpenClient -> {
                AnaliticClientView(state = state, event = event)
            }

            AnaliticAction.OpenTrip -> {
                AnaliticTripView(state = state, event = event)
            }

            AnaliticAction.OpenLog -> {
                LogView(state = state)
            }
        }
    }

    if (state.isShowDatePicker) {
        DateRangePickerModal(onDateRangeSelected = {
            event(AnaliticEvent.ChangeRangeDatePicker(it))
        }, onDismiss = {
            event(AnaliticEvent.DismissDatePicker)
        })
    }
}

@Composable
fun AnaliticMenu(
    onClickAll: () -> Unit,
    onClickTrip: () -> Unit,
    onClickClient: () -> Unit,
    onClickLogs: () -> Unit
) {
    Column(verticalArrangement = Arrangement.Center) {
        Button(
            onClick = onClickAll, shape = RoundedCornerShape(10.dp), modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.onSecondary
            )
        ) {
            Text(
                text = stringResource(R.string.all_info), style = AppTheme.typography.titleMedium, fontSize = 20.sp,
                color = AppTheme.colors.onPrimary
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onClickTrip, shape = RoundedCornerShape(10.dp), modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.onSecondary
            )
        ) {
            Text(
                text = stringResource(R.string.trip), style = AppTheme.typography.titleMedium, fontSize = 20.sp,
                color = AppTheme.colors.onPrimary
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onClickClient, shape = RoundedCornerShape(10.dp), modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.onSecondary
            )
        ) {
            Text(
                text = stringResource(R.string.shop), style = AppTheme.typography.titleMedium, fontSize = 20.sp,
                color = AppTheme.colors.onPrimary
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onClickLogs, shape = RoundedCornerShape(10.dp), modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.onSecondary
            )
        ) {
            Text(
                text = stringResource(R.string.logs), style = AppTheme.typography.titleMedium, fontSize = 20.sp,
                color = AppTheme.colors.onPrimary
            )
        }
    }
}