package ru.krymer.delivery.ui.screens.shop.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
    viewState: ShopViewState, onMillageTFC: (String) -> Unit
) {
    if (viewState.isDataShopForCourierLoad) {
        val millage = viewState.millage.collectAsState().value
        var millageInput by remember { mutableStateOf(if (millage == 0.0) "" else "$millage") }
        val cash = viewState.cash.collectAsState().value
        val noCash = viewState.noCash.collectAsState().value
        val remains = viewState.remains.collectAsState().value
        val allMoney = viewState.allMoney.collectAsState().value
        val salary = viewState.salary.collectAsState().value

        Column {
            CommonTextField(
                value = millageInput,
                placeholder = stringResource(id = R.string.km_et),
                onVC = { newValue ->
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
                style = MaterialTheme.typography.labelSmall,
                text = "ГСМ: $millageInput км",
                fontSize = 20.sp,
                color = AppTheme.colors.onSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                style = MaterialTheme.typography.labelSmall,
                text = "Зарплата: $salary",
                fontSize = 20.sp,
                color = AppTheme.colors.onSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                style = MaterialTheme.typography.labelSmall,
                text = "Зарплата(фикс): ${viewState.currentTrip?.salary}",
                fontSize = 20.sp,
                color = AppTheme.colors.onSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                style = MaterialTheme.typography.labelSmall, text = "Нал: $cash",
                fontSize = 20.sp,
                color = AppTheme.colors.onSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                style = MaterialTheme.typography.labelSmall,
                text = "Без/нал: $noCash",
                fontSize = 20.sp,
                color = AppTheme.colors.onSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                style = MaterialTheme.typography.labelSmall,
                text = "Общая: $allMoney",
                fontSize = 20.sp,
                color = AppTheme.colors.onSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                style = MaterialTheme.typography.labelSmall,
                text = "Остаток: $remains",
                fontSize = 20.sp,
                color = AppTheme.colors.onSecondary
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.Center),
                strokeWidth = 2.dp,
                color = AppTheme.colors.onSecondary
            )
        }
    }
}



