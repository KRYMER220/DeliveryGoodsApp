package ru.krymer.delivery.ui.screens.shared.models

sealed class SharedEvents {
    data object ClearToken: SharedEvents()
    data object LogOut: SharedEvents()
    data class DeleteMessage(val id: Long): SharedEvents()
    data object OpenHideSettingsApp: SharedEvents()
    data object ChangeSettings: SharedEvents()
    data class ChangeFontSizeIndex(val index: Int) : SharedEvents()
}