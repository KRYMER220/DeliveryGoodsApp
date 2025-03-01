package ru.krymer.delivery.ui.screens.shop.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.utilModel.TypePayModel
import ru.krymer.delivery.data.model.utilModel.getRuStringByTypePay
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.screens.shop.ShopViewModel
import ru.krymer.delivery.ui.screens.shop.models.ShopViewState
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun AlertDialogAddSum(
    onAddSumTFC: (String) -> Unit, viewModel: ShopViewModel
) {
    var addSum by remember { mutableStateOf("") }
    Column {
        Spacer(modifier = Modifier.height(5.dp))
        CommonTextField(
            value = addSum,
            placeholder = "Добавочная сумма",
            onVC = { str ->
                addSum = str
                onAddSumTFC(str)
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
fun AlertDialogChangeArrears(
    onChangeArrearsTFC: (String) -> Unit
) {
    var arrears by remember { mutableStateOf("") }
    Column {
        Spacer(modifier = Modifier.height(5.dp))
        CommonTextField(
            value = arrears,
            placeholder = "Долг",
            onVC = { str ->
                arrears = str
                onChangeArrearsTFC(str)
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
fun AlertDialogChangeTypePay(
    viewState: ShopViewState,
    onChangeType: (TypePayModel) -> Unit,
    onChangeStateTypePay: (Boolean) -> Unit
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
                .clickable(onClick = { onChangeStateTypePay(true) }),
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
                onChangeStateTypePay(false)
            }) {
                val list = TypePayModel.entries.toTypedArray()
                list.forEach {
                    DropdownMenuItem(text = { Text(text = it.getRuStringByTypePay()) }, onClick = {
                        onChangeType(it)
                        onChangeStateTypePay(false)
                    })
                }
            }

        }
    }
}


@Composable
fun ConfirmView(onSubmit: () -> Unit, onDismiss: () -> Unit) {
    Column {
        Text(
            text = "Подтвердите действие",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = AppTheme.colors.onSecondary
        )
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onDismiss, colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.onSecondary
                )
            ) {
                Text(text = stringResource(id = R.string.close), color = AppTheme.colors.onPrimary)
            }
            Button(
                onClick = onSubmit, colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.onSecondary
                )
            ) {
                Text(text = stringResource(id = R.string.ok), color = AppTheme.colors.onPrimary)
            }
        }
    }
}