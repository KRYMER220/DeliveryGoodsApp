package ru.krymer.delivery.ui.screens.courier.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.utilModel.Error
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.screens.courier.models.CourierViewState
import ru.krymer.delivery.utills.startsWithDigit

@Composable
fun UpdateSettingsView(
    viewState: CourierViewState,
    changeSalary: (String) -> Unit,
    changePriceMillage: (String) -> Unit,
) {
    viewState.factory?.let { factory ->
        var salary by remember { mutableStateOf("${factory.salary.toInt()}") }
        var priceMillage by remember { mutableStateOf("${factory.priceMillage}") }
        var errorSalary by remember { mutableStateOf(Error()) }
        var errorPrice by remember { mutableStateOf(Error()) }
        val errorEmpty = stringResource(R.string.empty_input)
        val errorNum = stringResource(R.string.error_num)

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            CommonTextField(
                value =  salary,
                placeholder = stringResource(
                    id = R.string.salary
                ),
                infoValue = stringResource(R.string.salary),
                changerText = {
                    salary = it
                    errorSalary = when {
                        it == "" -> Error(visible = true, error = errorEmpty)
                        !startsWithDigit(it) -> Error(visible = true, error = errorNum)
                        else -> {
                            changeSalary(it)
                            Error()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = errorSalary.visible,
                errorValue = errorSalary.error
            )

            CommonTextField(
                value = priceMillage,
                placeholder = stringResource(
                    id = R.string.km_price
                ),
                infoValue = stringResource(R.string.km_price),
                changerText = {
                    priceMillage = it
                    errorPrice = when {
                        it == "" -> Error(visible = true, error = errorEmpty)
                        !startsWithDigit(it) -> Error(visible = true, error = errorNum)
                        else -> {
                            changePriceMillage(it)
                            Error()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = errorPrice.visible,
                errorValue = errorPrice.error
            )
        }
    }
}
