package ru.krymer.delivery.ui.screens.shop.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent
import ru.krymer.delivery.ui.screens.shop.models.ShopViewState
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun MillageView(
    state: ShopViewState, onMillageTFC: (String) -> Unit, event: (ShopEvent) -> Unit
) {
    val millage = state.millage.toInt().toString()
    var millageInput by remember { mutableStateOf("") }
    val cash = state.cash.toInt()
    val noCash = state.noCash.toInt()
    val remains = state.remains.toInt()
    val allMoney = state.allMoney.toInt()
    val salary = state.salary.toInt()
    val salaryFix = state.salaryFix.toInt()

    LaunchedEffect(millage) {
        millageInput = if (millage == "0") "" else millage
    }

    Column(verticalArrangement = Arrangement.spacedBy(5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            style = AppTheme.typography.titleMedium,
            text = "${stringResource(R.string.box_office)}: $allMoney",
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.titleMedium,
            text = "${stringResource(R.string.cash)}: $cash",
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.titleMedium,
            text = "${stringResource(R.string.noCash)}: $noCash",
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.titleMedium,
            text = "${stringResource(R.string.salary)}: $salaryFix / $salary",
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.titleMedium,
            text = "${stringResource(R.string.remains)}: $remains",
            color = AppTheme.colors.onSecondary
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            CommonTextField(
                value = millageInput,
                placeholder = stringResource(id = R.string.km_et),
                changerText = { newValue ->
                    millageInput = newValue
                    onMillageTFC(newValue)
                },
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxWidth()
                    .height(60.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Image(
                contentDescription = null,
                painter = painterResource(id = R.drawable.submit),
                modifier = Modifier
                    .weight(0.3f)
                    .size(50.dp)
                    .combinedClickable(onClick = { event(ShopEvent.MillageSaveAction) })
            )
        }
    }
}



