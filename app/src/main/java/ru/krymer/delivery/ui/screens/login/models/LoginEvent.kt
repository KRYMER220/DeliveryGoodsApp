package ru.krymer.delivery.ui.screens.login.models

sealed class LoginEvent {
    data object ForgotAction : LoginEvent()
    data object SignIn : LoginEvent()
    data object ForgotClicked : LoginEvent()
    data object LoginAction : LoginEvent()
    data class ChangeEmail(val value: String) : LoginEvent()
    data class ChangePassword(val value: String) : LoginEvent()
}