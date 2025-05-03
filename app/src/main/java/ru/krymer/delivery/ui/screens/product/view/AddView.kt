package ru.krymer.delivery.ui.screens.product.view

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
import ru.krymer.delivery.ui.screens.product.models.ProductViewState
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.startsWithDigit

@Composable
fun AddProductView(
    changeName: (String) -> Unit,
    changePrice: (String) -> Unit
) {

    var name by remember { mutableStateOf(Constants.EMPTY.EMPTY_STRING) }
    var errorName by remember { mutableStateOf(Error()) }
    var price by remember { mutableStateOf(Constants.EMPTY.EMPTY_STRING) }
    var errorPrice by remember { mutableStateOf(Error()) }

    Column {
        CommonTextField(
            isError = errorName.visible,
            errorValue = errorName.error,
            value = name,
            placeholder = stringResource(
                id = R.string.name
            ),
            onVC = { str ->
                name = str
                errorName = when {
                    str == "" -> Error(visible = true, error = Constants.EMPTY.EMPTY_FIELD)
                    else -> {
                        changeName(str)
                        Error()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)

        )
        Spacer(modifier = Modifier.height(10.dp))
        CommonTextField(
            isError = errorPrice.visible,
            errorValue = errorPrice.error,
            value = price,
            placeholder = stringResource(
                id = R.string.price
            ),
            onVC = { str ->
                price = str
                errorPrice = when {
                    str == "" -> Error(visible = true, error = Constants.EMPTY.EMPTY_FIELD)
                    !startsWithDigit(str) -> Error(visible = true, error = Constants.ERROR.ERROR_NUMBER_INPUT)
                    else -> {
                        changePrice(str)
                        Error()
                    }
                }

            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}