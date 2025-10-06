package ru.krymer.delivery.ui.screens.trip.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.components.GenericDropdown
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
                text = stringResource(R.string.filter),
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
                text = "${stringResource(R.string.sort)}: " + if (isSorted) stringResource(R.string.asc) else stringResource(
                    R.string.desc
                ),
                color = AppTheme.colors.onSecondary,
                style = AppTheme.typography.titleMedium
            )
        }
        GenericDropdown(
            selectedItem = route,
            items = routes,
            expanded = toggleMenuRoute,
            onExpandedChange = {
                toggleMenuRoute = !toggleMenuRoute
            },
            itemLabel = { it.name },
            onItemSelected = {
                event(TripEvent.ChangeRouteFilter(it))
            })
        GenericDropdown(
            selectedItem = user,
            items = couriers,
            expanded = toggleMenuCourier,
            onExpandedChange = {
                toggleMenuCourier = !toggleMenuCourier
            },
            itemLabel = { it.name },
            onItemSelected = {
                event(TripEvent.ChangeCourierFilter(it))
            })
    }
}