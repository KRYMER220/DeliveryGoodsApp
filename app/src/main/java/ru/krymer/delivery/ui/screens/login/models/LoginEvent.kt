package ru.krymer.delivery.ui.screens.login.models

sealed class LoginEvent {
    data object ForgotAction : LoginEvent()
    data object LoginClicked : LoginEvent()
    data object ForgotClicked : LoginEvent()
    data object LoginAction : LoginEvent()
    data class EmailChanged(val value: String) : LoginEvent()
    data class PassChanged(val value: String) : LoginEvent()
}