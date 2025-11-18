package ru.krymer.delivery.ui.screens.trip.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

    val onSecondaryColor = AppTheme.colors.onSecondary
    val secondaryColor = AppTheme.colors.secondary
    
    val checkboxColors = remember(onSecondaryColor, secondaryColor) {
        CheckboxColors(
            checkedCheckmarkColor = onSecondaryColor,
            uncheckedCheckmarkColor = secondaryColor,
            checkedBoxColor = Color.Transparent,
            uncheckedBoxColor = Color.Transparent,
            disabledCheckedBoxColor = Color.Transparent,
            disabledUncheckedBoxColor = Color.Transparent,
            disabledIndeterminateBoxColor = Color.Transparent,
            checkedBorderColor = onSecondaryColor,
            uncheckedBorderColor = onSecondaryColor,
            disabledBorderColor = Color.Transparent,
            disabledUncheckedBorderColor = Color.Transparent,
            disabledIndeterminateBorderColor = Color.Transparent
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        FilterCheckboxRow(
            checked = state.isFilter,
            label = stringResource(R.string.filter),
            onCheckedChange = { changeFilterCourier(it) },
            checkboxColors = checkboxColors
        )

        FilterCheckboxRow(
            checked = state.sort,
            label = "${stringResource(R.string.sort)}: ${
                if (state.sort) stringResource(R.string.asc) else stringResource(R.string.desc)
            }",
            onCheckedChange = { event(TripEvent.ChangeSort(it)) },
            checkboxColors = checkboxColors
        )

        GenericDropdown(
            selectedItem = state.filterRouteId?.let { routeId ->
                state.listRoute.firstOrNull { it.id == routeId }
            },
            items = state.listRoute,
            expanded = toggleMenuRoute,
            onExpandedChange = { toggleMenuRoute = !toggleMenuRoute },
            itemLabel = { it.name },
            onItemSelected = { event(TripEvent.ChangeRouteFilter(it)) }
        )

        GenericDropdown(
            selectedItem = state.filterUid?.let { uid ->
                state.listCourier.firstOrNull { it.id == uid }
            },
            items = state.listCourier,
            expanded = toggleMenuCourier,
            onExpandedChange = { toggleMenuCourier = !toggleMenuCourier },
            itemLabel = { it.name },
            onItemSelected = { event(TripEvent.ChangeCourierFilter(it)) }
        )
    }
}

@Composable
private fun FilterCheckboxRow(
    checked: Boolean,
    label: String,
    onCheckedChange: (Boolean) -> Unit,
    checkboxColors: CheckboxColors
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            modifier = Modifier.size(60.dp),
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = checkboxColors
        )
        Text(
            text = label,
            color = AppTheme.colors.onSecondary,
            style = AppTheme.typography.titleMedium
        )
    }
}