package ru.krymer.delivery.ui.screens.trip.views

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
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.ui.screens.trip.models.TripViewState
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun FilterView(changeFilterCourier: (Boolean) -> Unit, state: TripViewState) {

    var isFilterByCourier by remember { mutableStateOf(state.checkBoxIsFilterCourier) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                modifier = Modifier.size(60.dp),
                checked = isFilterByCourier,
                onCheckedChange = {
                    isFilterByCourier = !isFilterByCourier
                    changeFilterCourier(isFilterByCourier)
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
            Text(text = "Курьер", color = AppTheme.colors.onSecondary, style = AppTheme.typography.titleMedium)
        }

    }
}