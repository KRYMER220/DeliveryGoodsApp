package ru.krymer.delivery.ui.screens.login.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.components.AuthField
import ru.krymer.delivery.ui.screens.login.models.LoginViewState

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignInView(
    viewState: LoginViewState,
    onEmailChange: (String) -> Unit,
    onPassChange: (String) -> Unit,
    onSignIn: () -> Unit
) {
    var email by remember {
        mutableStateOf(viewState.emailValue)
    }

    var password by remember {
        mutableStateOf(viewState.passValue)
    }

    Column {
        AuthField(
            value = email,
            placeholder = stringResource(id = R.string.email_hint),
            onVC = {
                email = it
                onEmailChange(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            isError = viewState.isErrorEmail,
            errorValue = viewState.valueErrorEmail,
            enabled = !viewState.isLoginProgress,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        Spacer(modifier = Modifier.padding(top = 10.dp))

        AuthField(
            value = password,
            placeholder = stringResource(id = R.string.pass_hint),
            onVC = {
                password = it
                onPassChange(it)
            },
            isError = viewState.isErrorPass,
            errorValue = viewState.valueErrorPass,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            enabled = !viewState.isLoginProgress,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
        )
        Spacer(modifier = Modifier.padding(top = 10.dp))

        Button(
            onClick = onSignIn, shape = RoundedCornerShape(10.dp), modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            enabled = !viewState.isLoginProgress,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black
            )
        ) {
            if (viewState.isLoginProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Text(
                    text = stringResource(id = R.string.sign_in), style = TextStyle(
                        color = Color.White
                    ), fontSize = 20.sp
                )
            }
        }
    }
}