package ru.krymer.delivery.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.screens.login.models.LoginEvent
import ru.krymer.delivery.ui.screens.login.models.LoginSubState
import ru.krymer.delivery.ui.screens.login.views.SignInView
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun LoginScreen(
    viewModel: LoginViewModel
) {
    val viewState by viewModel.viewState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    with(viewState) {
        Box(
            contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize().padding(10.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = when (loginSubState) {
                        LoginSubState.SignIn -> stringResource(id = R.string.sign_in_title)
                        LoginSubState.Forgot -> stringResource(id = R.string.forgot_pass_title)
                    },
                    style = AppTheme.typography.labelLarge,
                    color = AppTheme.colors.onSecondary
                )
                when (loginSubState) {
                    LoginSubState.SignIn -> SignInView(viewState = this@with,
                        changeEmail = {
                            viewModel.obtainEvent(LoginEvent.EmailChanged(it))
                        }, changePass = {
                            viewModel.obtainEvent(LoginEvent.PassChanged(it))
                        }, onSignIn = {
                            viewModel.obtainEvent(LoginEvent.LoginClicked)
                            keyboardController?.hide()
                        })

                    LoginSubState.Forgot -> {}
                }
            }
        }
    }
}


