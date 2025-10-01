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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.ui.screens.trip.models.TripEvent
import ru.krymer.delivery.ui.screens.trip.models.TripViewState
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun FilterView(
    changeFilterCourier: (Boolean) -> Unit,
    state: TripViewState,
    event: (TripEvent) -> Unit
) {

    var toggleMenuRoute by remember { mutableStateOf(false) }
    var toggleMenuCourier by remember { mutableStateOf(false) }

    var isFilter by remember { mutableStateOf(state.isFilter) }
    var isSorted by remember { mutableStateOf(state.sort) }
    val routes = state.listRoute
    val couriers = state.listCourier

    var route = state.currentRoute
    var user = state.currentCourier

    LaunchedEffect(route, user) {
        route = state.currentRoute
        user = state.currentCourier
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                modifier = Modifier.size(60.dp),
                checked = isFilter,
                onCheckedChange = {
                    isFilter = !isFilter
                    changeFilterCourier(!isFilter)
                },
                colors = CheckboxColors(
                    checkedCheckmarkColor = AppTheme.colors.onSecondary,
                    uncheckedCheckmarkColor = AppTheme.colors.secondary,
                    checkedBoxColor = Color.Transparent,
                    uncheckedBoxColor = Color.Transparent,
                    disabledCheckedBoxColor = Color.Transparent,
                    disabledUncheckedBoxColor = Color.Transparent,
                    disabledIndeterminateBoxColor = Color.Transparent,
                    checkedBorderColor = AppTheme.colors.onSecondary,
                    uncheckedBorderColor = AppTheme.colors.onSecondary,
                    disabledBorderColor = Color.Transparent,
                    disabledUncheckedBorderColor = Color.Transparent,
                    disabledIndeterminateBorderColor = Color.Transparent
                )
            )
            Text(
                text = "Включить фильтрацию",
                color = AppTheme.colors.onSecondary,
                style = AppTheme.typography.titleMedium
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                modifier = Modifier.size(60.dp),
                checked = isSorted,
                onCheckedChange = {
                    isSorted = !isSorted
                    event(TripEvent.ChangeSort(!isSorted))
                },
                colors = CheckboxColors(
                    checkedCheckmarkColor = AppTheme.colors.onSecondary,
                    uncheckedCheckmarkColor = AppTheme.colors.secondary,
                    checkedBoxColor = Color.Transparent,
                    uncheckedBoxColor = Color.Transparent,
                    disabledCheckedBoxColor = Color.Transparent,
                    disabledUncheckedBoxColor = Color.Transparent,
                    disabledIndeterminateBoxColor = Color.Transparent,
                    checkedBorderColor = AppTheme.colors.onSecondary,
                    uncheckedBorderColor = AppTheme.colors.onSecondary,
                    disabledBorderColor = Color.Transparent,
                    disabledUncheckedBorderColor = Color.Transparent,
                    disabledIndeterminateBorderColor = Color.Transparent
                )
            )
            Text(
                text = "Сортировка: " + if (isSorted) "по возрастанию" else "по убыванию",
                color = AppTheme.colors.onSecondary,
                style = AppTheme.typography.titleMedium
            )
        }
        route?.let {
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
                            toggleMenuRoute = true
                        }, verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = route?.name ?: "Маршрут не выбран",
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

                    DropdownMenu(expanded = toggleMenuRoute, onDismissRequest = {
                        toggleMenuRoute = false
                    }) {
                        DropdownMenuItem(text = {
                            Text(text = "Не выбран", style = AppTheme.typography.titleSmall)
                        }, onClick = {
                            event(TripEvent.ChangeRouteFilter(null))
                            toggleMenuRoute = false
                        })
                        routes.forEach {
                            DropdownMenuItem(text = {
                                Text(
                                    text = it.name,
                                    style = AppTheme.typography.titleSmall
                                )
                            }, onClick = {
                                event(TripEvent.ChangeRouteFilter(it))
                                toggleMenuRoute = false
                            })
                        }
                    }
                }
            }
        }
        user?.let {
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
                            toggleMenuCourier = true
                        }, verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user?.name ?: "Курьер не выбран",
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
                    DropdownMenu(
                        expanded = toggleMenuCourier,
                        onDismissRequest = {
                            toggleMenuCourier = false
                        }) {
                        DropdownMenuItem(text = {
                            Text(text = "Не выбран", style = AppTheme.typography.titleSmall)
                        }, onClick = {
                            event(TripEvent.ChangeCourierFilter(null))
                            toggleMenuCourier = false
                        })
                        couriers.forEach {
                            DropdownMenuItem(text = {
                                Text(
                                    text = it.name,
                                    style = AppTheme.typography.titleSmall
                                )
                            }, onClick = {
                                event(TripEvent.ChangeCourierFilter(it))
                                toggleMenuCourier = false
                            })
                        }
                    }
                }
            }
        }

    }


}