package ru.krymer.delivery.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.krymer.delivery.common.EventHandler
import ru.krymer.delivery.data.TokenManager
import ru.krymer.delivery.data.api.UserApi
import ru.krymer.delivery.data.request.SignInRequest
import ru.krymer.delivery.ui.screens.login.models.LoginEvent
import ru.krymer.delivery.ui.screens.login.models.LoginSubState
import ru.krymer.delivery.ui.screens.login.models.LoginViewState
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.utills.Constants
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userApi: UserApi,
    private val tokenManager: TokenManager,
    private val sharedViewModel: SharedViewModel
) : ViewModel(), EventHandler<LoginEvent> {

    private val _viewState = MutableStateFlow(LoginViewState())
    val viewState: StateFlow<LoginViewState> = _viewState

    private fun updateViewState(update: (LoginViewState) -> LoginViewState) {
        _viewState.update { update(it) }
    }

    override fun obtainEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> emailChanged(event.value)
            is LoginEvent.PassChanged -> passChanged(event.value)
            is LoginEvent.LoginClicked -> loginClicked()
            is LoginEvent.ForgotAction -> forgotAction()
            is LoginEvent.ForgotClicked -> TODO("forgotClicked()")
            is LoginEvent.LoginAction -> loginAction()

        }
    }

    private fun loginAction() {
        updateViewState { it.copy(loginSubState = LoginSubState.SignIn) }
    }

    private fun forgotAction() {
        //updateViewState { it.copy(loginSubState = LoginSubState.Forgot) }
    }

    private fun loginClicked() {
        _viewState.value = viewState.value.copy(isLoginProgress = true)
        viewModelScope.launch(Dispatchers.IO) {
            val email = viewState.value.emailValue
            val pass = viewState.value.passValue
            try {
                val signInRequest = SignInRequest(email = email, password = pass)
                val tokenResponse = withContext(Dispatchers.IO) {
                    userApi.signInUser(signInRequest)
                }
                if (tokenResponse.success) {
                    val tokens = tokenResponse.obj
                    if (tokens != null) {
                        tokenManager.saveAccessToken(tokens.accessToken)
                        sharedViewModel.authorized(isSignIn = true)
                    }
                } else {
                    sharedViewModel.message(tokenResponse.message)
                }
            } catch (e: Exception) {
                sharedViewModel.message(message = e.message ?: Constants.ERROR.GENERAL_ERROR)
            } finally {
                updateViewState { it.copy(isLoginProgress = false) }
            }
        }
    }

    private fun passChanged(value: String) {
        updateViewState { it.copy(passValue = value) }
    }

    private fun emailChanged(value: String) {
        updateViewState { it.copy(emailValue = value) }
    }

}