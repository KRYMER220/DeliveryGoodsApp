package ru.krymer.delivery.ui.screens.login.models

enum class LoginSubState {
    SignIn, Forgot
}

sealed class LoginAction {
    data object OpenMenu : LoginAction()
    data object None : LoginAction()
}

data class LoginViewState(
    val loginSubState: LoginSubState = LoginSubState.SignIn,
    val emailValue: String = "",
    val passValue: String = "",
    val isLoginProgress: Boolean = false,
    val isForgotProgress: Boolean = false,
    val loginAction: LoginAction = LoginAction.None,
    val isErrorEmail: Boolean = false,
    val isErrorPass: Boolean = false,
    val valueErrorEmail: String = "",
    val valueErrorPass: String = "",
)

