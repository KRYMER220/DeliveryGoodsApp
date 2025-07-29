package ru.krymer.delivery.ui.screens.shop.views

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent
import ru.krymer.delivery.ui.screens.shop.models.ShopViewState
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun MillageAndInfoView(
    state: ShopViewState, onMillageTFC: (String) -> Unit, event: (ShopEvent) -> Unit
) {
    val millage = state.millage.collectAsState().value.toInt().toString()
    var millageInput by remember { mutableStateOf("") }
    val cash = state.cash.collectAsState().value.toInt()
    val noCash = state.noCash.collectAsState().value.toInt()
    val remains = state.remains.collectAsState().value.toInt()
    val allMoney = state.allMoney.collectAsState().value.toInt()
    val salary = state.salary.collectAsState().value.toInt()
    val salaryFix = state.salaryFix.collectAsState().value.toInt()

    LaunchedEffect(millage) {
        millageInput = if (millage == "0") "" else millage
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            style = AppTheme.typography.titleMedium,
            text = "Касса: $allMoney",
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.titleMedium,
            text = "Нал: $cash",
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.titleMedium,
            text = "Без/нал: $noCash",
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.titleMedium,
            text = "Зарплата(фикс): $salaryFix",
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.titleMedium,
            text = "Зарплата: $salary",
            color = AppTheme.colors.onSecondary
        )
        Text(
            style = AppTheme.typography.titleMedium,
            text = "Остаток: $remains",
            color = AppTheme.colors.onSecondary
        )
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
        Image(
            contentDescription = "submit",
            painter = painterResource(id = R.drawable.submit),
            modifier = Modifier
                .size(50.dp)
                .combinedClickable(onClick = { event(ShopEvent.MillageSaveAction) })
        )
    }
}



