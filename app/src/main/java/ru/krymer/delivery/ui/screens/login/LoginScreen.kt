package ru.krymer.delivery.ui.screens.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.screens.login.models.LoginEvent
import ru.krymer.delivery.ui.screens.login.models.LoginSubState
import ru.krymer.delivery.ui.screens.login.views.SignInView
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel
) {
    val viewState by loginViewModel.viewState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    with(viewState) {
        LazyColumn(
            contentPadding = PaddingValues(start = 30.dp, end = 30.dp),
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 100.dp)
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = when (loginSubState) {
                            LoginSubState.SignIn -> stringResource(id = R.string.sign_in_title)
                            LoginSubState.Forgot -> stringResource(id = R.string.forgot_pass_title)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 24.sp,
                        color = AppTheme.colors.onSecondary
                    )
                }
            }

            item {
                Spacer(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .fillMaxWidth()
                )
            }

            item {
                when (loginSubState) {
                    LoginSubState.SignIn -> SignInView(viewState = this@with,
                        onEmailChange = {
                            loginViewModel.obtainEvent(LoginEvent.EmailChanged(it))
                        }, onPassChange = {
                            loginViewModel.obtainEvent(LoginEvent.PassChanged(it))
                        }, onSignIn = {
                            loginViewModel.obtainEvent(LoginEvent.LoginClicked)
                            keyboardController?.hide()
                        })

                    LoginSubState.Forgot -> {}
                }
            }
        }
    }
}


