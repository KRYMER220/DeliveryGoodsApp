package ru.krymer.delivery


import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import ru.krymer.delivery.di.RetryWorkManager
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