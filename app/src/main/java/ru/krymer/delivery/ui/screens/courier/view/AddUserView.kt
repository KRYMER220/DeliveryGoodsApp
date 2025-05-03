package ru.krymer.delivery.ui.screens.courier.view

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
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.isValidEmail
import ru.krymer.delivery.utills.startsWithDigit

@Composable
fun BottomSheetDialogAddUser(
    viewState: CourierViewState,
    changeName: (String) -> Unit,
    changeEmail: (String) -> Unit,
    changePass: (String) -> Unit
) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var errorName by remember { mutableStateOf(Error()) }
    var errorEmail by remember { mutableStateOf(Error()) }
    var errorPass by remember { mutableStateOf(Error()) }

    Column {
        CommonTextField(
            isError = errorName.visible,
            errorValue = errorName.error,
            value = name,
            placeholder = stringResource(
                id = R.string.name_user
            ),
            onVC = {
                name = it
                errorName = when {
                    it == "" -> Error(visible = true, error = Constants.EMPTY.EMPTY_FIELD)
                    else -> {
                        changeName(it)
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
            isError = errorEmail.visible,
            errorValue = errorEmail.error,
            value = email,
            placeholder = stringResource(
                id = R.string.email_hint
            ),
            onVC = {
                email = it
                errorEmail = when {
                    it == "" -> Error(visible = true, error = Constants.EMPTY.EMPTY_FIELD)
                    !isValidEmail(it) -> Error(visible = true, error = Constants.ERROR.EMAIL_INVALID)
                    else -> {
                        changeEmail(it)
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
            isError = errorPass.visible,
            errorValue = errorPass.error,
            value = pass,
            placeholder = stringResource(
                id = R.string.pass_hint
            ),
            onVC = {

                pass = it
                errorPass = when {
                    it == "" -> Error(visible = true, error = Constants.EMPTY.EMPTY_FIELD)
                    it.length < 8 -> Error(visible = true, error = Constants.ERROR.PASS_INVALID)
                    else -> {
                        changePass(it)
                        Error()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )
    }
}