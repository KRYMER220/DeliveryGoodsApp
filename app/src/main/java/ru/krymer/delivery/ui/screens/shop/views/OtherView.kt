package ru.krymer.delivery.ui.screens.shop.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.data.model.utilModel.Error
import ru.krymer.delivery.data.model.utilModel.TypePayModel
import ru.krymer.delivery.data.model.utilModel.getRuStringByTypePay
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.screens.shop.models.ShopViewState
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.startsWithDigit

@Composable
fun ChangeAddSumView(
    changeAddSum: (String) -> Unit
) {
    var addSum by remember { mutableStateOf("") }
    var errorAddSum by remember { mutableStateOf(Error()) }
    Column {
        Spacer(modifier = Modifier.height(5.dp))
        CommonTextField(
            value = addSum,
            placeholder = "Добавочная сумма",
            onVC = { str ->
                addSum = str
                errorAddSum = when {
                    str == "" -> Error(visible = true, error = Constants.EMPTY.EMPTY_FIELD)
                    !startsWithDigit(str) -> Error(visible = true, error = Constants.ERROR.ERROR_NUMBER_INPUT)
                    else -> {
                        changeAddSum(str)
                        Error()
                    }
                }

            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
            ),
            textStyle = TextStyle(textAlign = TextAlign.Center)
        )
        Spacer(modifier = Modifier.height(5.dp))
    }
}

@Composable
fun ChangeArrearsView(
    changeArrears: (String) -> Unit
) {
    var arrears by remember { mutableStateOf("") }
    var errorArrears by remember { mutableStateOf(Error()) }
    Column {
        Spacer(modifier = Modifier.height(5.dp))
        CommonTextField(
            value = arrears,
            placeholder = "Долг",
            onVC = { str ->
                arrears = str
                errorArrears = when {
                    str == "" -> Error(visible = true, error = Constants.EMPTY.EMPTY_FIELD)
                    !startsWithDigit(str) -> Error(visible = true, error = Constants.ERROR.ERROR_NUMBER_INPUT)
                    else -> {
                        changeArrears(str)
                        Error()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
            ),
            textStyle = TextStyle(textAlign = TextAlign.Center)
        )
        Spacer(modifier = Modifier.height(5.dp))
    }
}

@Composable
fun ChangeTypePayView(
    viewState: ShopViewState,
    changeTypePay: (TypePayModel) -> Unit,
    changeStateChangerTypePay: (Boolean) -> Unit
) {
    val type = viewState.typePay.collectAsState().value.getRuStringByTypePay()
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .background(AppTheme.colors.secondary)
                .fillMaxWidth()
                .height(60.dp)
                .clickable(onClick = { changeStateChangerTypePay(true) }),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = type,
                modifier = Modifier.padding(start = 15.dp),
                color = AppTheme.colors.onPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.padding(end = 15.dp)
            )
            DropdownMenu(expanded = viewState.isShowDropDownTypePay, onDismissRequest = {
                changeStateChangerTypePay(false)
            }) {
                val list = TypePayModel.entries.toTypedArray()
                list.forEach {
                    DropdownMenuItem(text = { Text(text = it.getRuStringByTypePay()) }, onClick = {
                        changeTypePay(it)
                        changeStateChangerTypePay(false)
                    })
                }
            }

        }
    }
}
