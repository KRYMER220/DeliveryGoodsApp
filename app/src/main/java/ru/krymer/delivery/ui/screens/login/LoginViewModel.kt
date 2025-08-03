package ru.krymer.delivery.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.krymer.delivery.common.EventHandler
import ru.krymer.delivery.data.api.UserApi
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.data.request.SignInRequest
<<<<<<< HEAD
<<<<<<< HEAD
import ru.krymer.delivery.di.SecureDataStore
import ru.krymer.delivery.di.putBoolean
=======
>>>>>>> parent of 359f480 (fix)
=======
>>>>>>> parent of 359f480 (fix)
import ru.krymer.delivery.ui.screens.login.models.LoginEvent
import ru.krymer.delivery.ui.screens.login.models.LoginSubState
import ru.krymer.delivery.ui.screens.login.models.LoginViewState
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.utills.Constants
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userApi: UserApi,
<<<<<<< HEAD
<<<<<<< HEAD
    private val secureDataStore: SecureDataStore,
    private val sharedViewModel: SharedViewModel,
=======
    private val tokenManager: TokenManager,
    private val sharedViewModel: SharedViewModel
>>>>>>> parent of 359f480 (fix)
=======
    private val tokenManager: TokenManager,
    private val sharedViewModel: SharedViewModel
>>>>>>> parent of 359f480 (fix)
) : ViewModel(), EventHandler<LoginEvent> {

    private val _viewState = MutableStateFlow(LoginViewState())
    val viewState = _viewState.asStateFlow()

    private fun updateViewState(update: (LoginViewState) -> LoginViewState) {
        _viewState.update { update(it) }
    }

    private fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (e: CancellationException) {
                sharedViewModel.message(Constants.ERROR.CANCEL_OPERATION, type = TypeMessageModel.ERROR)
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            }
        }
    }

    override fun obtainEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.ChangeEmail -> emailChanged(event.value)
            is LoginEvent.ChangePassword -> passChanged(event.value)
            is LoginEvent.SignIn -> signIn()
            is LoginEvent.ForgotAction -> TODO("forgotAction()")
            is LoginEvent.ForgotClicked -> TODO("forgotClicked()")
            is LoginEvent.LoginAction -> loginAction()

        }
    }

    private fun loginAction() {
        updateViewState { it.copy(loginSubState = LoginSubState.SignIn) }
    }

    private fun signIn() {
        launchCoroutine {
            updateViewState { it.copy(isLoginProgress = true) }
            val email = viewState.value.emailValue
            val pass = viewState.value.passValue
            val signInRequest = SignInRequest(email = email, password = pass)
            val tokenResponse = withContext(Dispatchers.IO) {
                userApi.signIn(signInRequest)
            }
            if (tokenResponse.success) {
                val tokens = tokenResponse.obj
                if (tokens != null) {
<<<<<<< HEAD
                    secureDataStore.putString(Constants.TOKEN.ACCESS, tokens.accessToken)
                    secureDataStore.putBoolean(Constants.KEYS.AUTH, true)
=======
                    tokenManager.saveAccessToken(tokens.accessToken)
<<<<<<< HEAD
>>>>>>> parent of 359f480 (fix)
=======
>>>>>>> parent of 359f480 (fix)
                    updateViewState { it.copy(isLoginProgress = false) }
                    sharedViewModel.initAuth()
                } else {
                    updateViewState { it.copy(isLoginProgress = false) }
                    sharedViewModel.message(Constants.ERROR.AUTH, type = TypeMessageModel.ERROR)
                }
            } else {
                updateViewState { it.copy(isLoginProgress = false) }
                sharedViewModel.message(tokenResponse.message, type = TypeMessageModel.ERROR)
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