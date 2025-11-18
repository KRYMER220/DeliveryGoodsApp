package ru.krymer.delivery.ui.screens.product.view

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
import ru.krymer.delivery.utills.startsWithDigit

@Composable
fun AddProductView(
    changeName: (String) -> Unit,
    changePrice: (String) -> Unit
) {

    var name by remember { mutableStateOf("") }
    var errorName by remember { mutableStateOf(Error()) }

    var price by remember { mutableStateOf("") }
    var errorPrice by remember { mutableStateOf(Error()) }
    val errorEmpty = stringResource(R.string.empty_input)
    val errorNum = stringResource(R.string.error_num)

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        CommonTextField(
            isError = errorName.visible,
            errorValue = errorName.error,
            value = name,
            placeholder = stringResource(
                id = R.string.name
            ),
            changerText = { str ->
                name = str
                errorName = when {
                    str == "" -> Error(visible = true, error = errorEmpty)
                    else -> {
                        changeName(str)
                        Error()
                    }
                }
            },
            infoValue = stringResource(R.string.name),
            modifier = Modifier
                .fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)

        )
        CommonTextField(
            isError = errorPrice.visible,
            errorValue = errorPrice.error,
            value = price,
            placeholder = stringResource(
                id = R.string.price
            ),
            infoValue = stringResource(R.string.price),
            changerText = { str ->
                price = str
                errorPrice = when {
                    str == "" -> Error(visible = true, error = errorEmpty)
                    !startsWithDigit(str) -> Error(visible = true, error = errorNum)
                    else -> {
                        changePrice(str)
                        Error()
                    }
                }

            },
            modifier = Modifier
                .fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}