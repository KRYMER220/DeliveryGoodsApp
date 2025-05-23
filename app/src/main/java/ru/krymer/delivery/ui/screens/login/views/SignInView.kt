package ru.krymer.delivery.ui.screens.login.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.data.model.utilModel.Error
import ru.krymer.delivery.ui.components.AuthField
import ru.krymer.delivery.ui.screens.login.models.LoginViewState
import ru.krymer.delivery.ui.screens.main.views.CustomButton
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.isValidEmail

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignInView(
    viewState: LoginViewState,
    changeEmail: (String) -> Unit,
    changePass: (String) -> Unit,
    onSignIn: () -> Unit
) {
    var email by remember {
        mutableStateOf(viewState.emailValue)
    }

    var pass by remember {
        mutableStateOf(viewState.passValue)
    }
    var errorEmail by remember { mutableStateOf(Error()) }
    var errorPass by remember { mutableStateOf(Error()) }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        AuthField(
            value = email,
            placeholder = stringResource(id = R.string.email_hint),
            changerText = {
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
            isError = errorEmail.visible,
            errorValue = errorEmail.error,
            enabled = !viewState.isLoginProgress,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        AuthField(
            value = pass,
            placeholder = stringResource(id = R.string.pass_hint),
            changerText = {
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
            isError = errorPass.visible,
            errorValue = errorPass.error,
            enabled = !viewState.isLoginProgress,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
        )
        Spacer(modifier = Modifier.height(5.dp))
        CustomButton(routeTo = onSignIn, buttonName = stringResource(id = R.string.sign_in))
        Spacer(modifier = Modifier.height(20.dp))
    }
}