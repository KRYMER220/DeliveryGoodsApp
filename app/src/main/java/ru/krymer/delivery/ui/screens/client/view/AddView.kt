package ru.krymer.delivery.ui.screens.client.view

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
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.SignedNumberWithComma
import ru.krymer.delivery.utills.isValidCords
import ru.krymer.delivery.utills.isValidPhone
import ru.krymer.delivery.utills.startsWithDigit

@Composable
fun AddClientView(
    changeName: (String) -> Unit,
    changeArrears: (String) -> Unit,
    changePhone: (String) -> Unit,
    changeCords: (String) -> Unit,
) {

    var name by remember { mutableStateOf("") }
    var arrears by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var cords by remember { mutableStateOf("") }
    var errorName by remember { mutableStateOf(Error()) }
    var errorArrears by remember { mutableStateOf(Error()) }
    var errorPhone by remember { mutableStateOf(Error()) }
    var errorCords by remember { mutableStateOf(Error()) }

    Column {
        CommonTextField(
            value = name,
            placeholder = stringResource(
                id = R.string.name
            ),
            changerText = { str ->
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
                .fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = errorName.visible,
            errorValue = errorName.error
        )
        Spacer(modifier = Modifier.height(10.dp))
        CommonTextField(
            value = arrears,
            placeholder = stringResource(
                id = R.string.arrears
            ),
            changerText = { str ->
                arrears = str
                errorArrears = when {
                    !startsWithDigit(str) -> Error(visible = true, error = Constants.ERROR.ERROR_NUMBER_INPUT)
                    else -> {
                        changeArrears(str)
                        Error()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = errorArrears.visible,
            errorValue = errorArrears.error
        )
        Spacer(modifier = Modifier.height(10.dp))
        CommonTextField(
            value = phone,
            placeholder = stringResource(
                id = R.string.phone
            ),
            changerText = { str ->
                phone = str
                errorPhone = when {
                    !isValidPhone(str) -> Error(visible = true, error = Constants.ERROR.PHONE)
                    else -> {
                        changePhone(str)
                        Error()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = errorPhone.visible,
            errorValue = errorPhone.error
        )
        Spacer(modifier = Modifier.height(10.dp))
        CommonTextField(
            value = cords,
            placeholder = stringResource(
                id = R.string.cords
            ),
            changerText = { str ->
                cords = str
                errorCords = when {
                    !isValidCords(str) -> Error(visible = true, error = Constants.ERROR.CORD)
                    else -> {
                        changeCords(str)
                        Error()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            visualTransformation = SignedNumberWithComma(),
            isError = errorCords.visible,
            errorValue = errorCords.error
        )
    }
}