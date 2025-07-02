package ru.krymer.delivery.ui.screens.shared.models

sealed class SharedEvents {
    data object ClearToken: SharedEvents()
    data object LogOut: SharedEvents()
    data class DeleteMessage(val id: Long): SharedEvents()
}