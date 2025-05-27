package ru.krymer.delivery.ui.screens.shop.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.screens.shop.models.ShopViewState
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun MillageAndInfoView(
    state: ShopViewState, onMillageTFC: (String) -> Unit
) {
    val millage = state.millage.collectAsState().value.toInt()
        var millageInput by remember { mutableStateOf(if (millage == 0) "" else "$millage") }
    val cash = state.cash.collectAsState().value.toInt()
    val noCash = state.noCash.collectAsState().value.toInt()
    val remains = state.remains.collectAsState().value.toInt()
    val allMoney = state.allMoney.collectAsState().value.toInt()
    val salary = state.salary.collectAsState().value.toInt()
    val salaryFix = state.salaryFix.collectAsState().value.toInt()

        Column {
            CommonTextField(
                value = millageInput,
                placeholder = stringResource(id = R.string.km_et),
                changerText = { newValue ->
                    millageInput = newValue
                    onMillageTFC(newValue)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                style = AppTheme.typography.titleMedium,
                text = "ГСМ: $millageInput км",
                fontSize = 20.sp,
                color = AppTheme.colors.onSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                style = AppTheme.typography.titleMedium,
                text = "Зарплата: $salary",
                fontSize = 20.sp,
                color = AppTheme.colors.onSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                style = AppTheme.typography.titleMedium,
                text = "Остаток: $remains",
                fontSize = 20.sp,
                color = AppTheme.colors.onSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                style = AppTheme.typography.titleMedium,
                text = "Общая: $allMoney",
                fontSize = 20.sp,
                color = AppTheme.colors.onSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                style = AppTheme.typography.titleMedium,
                text = "Нал: $cash",
                fontSize = 20.sp,
                color = AppTheme.colors.onSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                style = AppTheme.typography.titleMedium,
                text = "Без/нал: $noCash",
                fontSize = 20.sp,
                color = AppTheme.colors.onSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                style = AppTheme.typography.titleMedium,
                text = "Зарплата(фикс): $salaryFix",
                fontSize = 20.sp,
                color = AppTheme.colors.onSecondary
            )
        }
}



