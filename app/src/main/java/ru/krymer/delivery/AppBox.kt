package ru.krymer.delivery


import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch
import ru.krymer.delivery.data.model.user.StatusModel
import ru.krymer.delivery.di.RetryWorkManager
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import javax.inject.Inject

@HiltAndroidApp
class AppBox : Application() {

    @Inject
    lateinit var retryWorkManager: RetryWorkManager


    override fun onCreate() {
        super.onCreate()
        retryWorkManager.scheduleRetryWork()
    }
}