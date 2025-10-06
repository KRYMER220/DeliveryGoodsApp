package ru.krymer.delivery.ui.screens.product.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.utilModel.Error
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.screens.product.models.ProductViewState
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.startsWithDigit


@Composable
fun UpdateProductView(
    state: ProductViewState,
    changeName: (String) -> Unit,
    changePrice: (String) -> Unit,
    productAction: (Boolean) -> Unit
) {

    state.product?.let { product ->
        var name by remember { mutableStateOf(product.name) }
        var errorName by remember { mutableStateOf(Error()) }
        var price by remember { mutableStateOf("${product.price.toInt()}") }
        var errorPrice by remember { mutableStateOf(Error()) }
        var isActive by remember { mutableStateOf(product.isActive) }
        val errorEmpty = stringResource(R.string.empty_input)
        val errorNum = stringResource(R.string.error_num)


        Column {
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
                    .fillMaxWidth()
                    .height(60.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isActive) stringResource(R.string.hide) else stringResource(R.string.show),
                    color = AppTheme.colors.onSecondary
                )
                Checkbox(
                    checked = !isActive,
                    onCheckedChange = {
                        isActive = !isActive
                        productAction(!isActive)
                    }
                )
            }
        }
    }
}