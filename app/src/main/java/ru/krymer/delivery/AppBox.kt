package ru.krymer.delivery


import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch
import ru.krymer.delivery.data.model.user.StatusModel
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import javax.inject.Inject

@HiltAndroidApp
class AppBox : Application(), LifecycleObserver {
    @Inject
    lateinit var appLifecycleObserver: AppLifecycleObserver

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
    }
}

class AppLifecycleObserver @Inject constructor(
    private val viewModel: SharedViewModel
) : DefaultLifecycleObserver {

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        viewModel.viewModelScope.launch {
            viewModel.updateUserStatus(StatusModel.OFFLINE)
        }
    }
}