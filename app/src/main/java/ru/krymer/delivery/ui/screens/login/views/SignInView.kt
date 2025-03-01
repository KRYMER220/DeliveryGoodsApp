package ru.krymer.delivery.ui.screens.login.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.components.AuthFields
import ru.krymer.delivery.ui.screens.login.models.LoginViewState
import ru.krymer.delivery.ui.theme.AppTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignInView(
    viewState: LoginViewState,
    onEmailTFC: (String) -> Unit,
    onPassTFC: (String) -> Unit,
    onAuthClick: () -> Unit,
    onForgotClick: () -> Unit
) {
    Column {
        AuthFields(
            value = viewState.emailValue,
            placeholder = stringResource(id = R.string.email_hint),
            onVC = onEmailTFC,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            isError = viewState.isErrorEmail,
            errorValue = viewState.valueErrorEmail,
            enabled = !viewState.isLoginProgress,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            autofillTypes = listOf(AutofillType.EmailAddress)
        )
        Spacer(modifier = Modifier.padding(top = 10.dp))

        AuthFields(
            value = viewState.passValue,
            placeholder = stringResource(id = R.string.pass_hint),
            onVC = onPassTFC,
            isError = viewState.isErrorPass,
            errorValue = viewState.valueErrorPass,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            enabled = !viewState.isLoginProgress,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            autofillTypes = listOf(AutofillType.Password)
        )
        Spacer(modifier = Modifier.padding(top = 10.dp))

        Button(
            onClick = onAuthClick, shape = RoundedCornerShape(10.dp), modifier = Modifier
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

        Spacer(modifier = Modifier.height(30.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(id = R.string.forgot_action),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clickable(onClick = onForgotClick),
                color = AppTheme.colors.onSecondary
            )
        }
    }
}