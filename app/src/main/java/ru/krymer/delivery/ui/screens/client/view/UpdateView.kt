package ru.krymer.delivery.ui.screens.client.view

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.data.model.utilModel.Error
import ru.krymer.delivery.ui.components.CommonTextField
import ru.krymer.delivery.ui.screens.client.models.ClientEvent
import ru.krymer.delivery.ui.screens.client.models.ClientViewState
import ru.krymer.delivery.ui.theme.AppTheme
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.SignedNumberWithComma
import ru.krymer.delivery.utills.isValidCords
import ru.krymer.delivery.utills.isValidPhone
import ru.krymer.delivery.utills.startsWithDigit

@Composable
fun UpdateClientView(
    viewState: ClientViewState,
    user: UserModel,
    event: (ClientEvent) -> Unit
) {
    var name by remember { mutableStateOf(viewState.name) }
    var arrears by remember { mutableStateOf(viewState.arrears) }
    var phone by remember { mutableStateOf(viewState.phone) }
    var cords by remember { mutableStateOf(viewState.cords) }
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
                        event(ClientEvent.ChangeNameClient(str))
                        Error()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
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
                        event(ClientEvent.ChangeArrearsClient(str))
                        Error()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
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
                        event(ClientEvent.ChangePhoneClient(str))
                        Error()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
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
                        event(ClientEvent.ChangeCordClient(str))
                        Error()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = SignedNumberWithComma(),
            isError = errorCords.visible,
            errorValue = errorCords.error
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (user.isModOrAdminOrSys()) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable {
                            event(ClientEvent.DropDownMenuState(true))
                        }, verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = viewState.selectedRoute!!.name,
                        modifier = Modifier.padding(start = 15.dp),
                        color = AppTheme.colors.onSecondary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 15.dp)
                    )
                    DropdownMenu(expanded = viewState.dropDownState, onDismissRequest = {
                        event(ClientEvent.DropDownMenuState(false))
                    }) {
                        val list = viewState.listRoute.collectAsState().value
                        list.forEach {
                            DropdownMenuItem(text = { Text(text = it.name) }, onClick = {
                                event(
                                    ClientEvent.SelectedItemMenu(
                                        it
                                    )
                                )
                                event(
                                    ClientEvent.DropDownMenuState(
                                        false
                                    )
                                )
                            })
                        }
                    }

                }
            }
        }
    }
}