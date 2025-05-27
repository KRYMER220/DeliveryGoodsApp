package ru.krymer.delivery.di

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@HiltWorker
class RetryFailedRequestsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val retryManager: RetryManager
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            retryManager.retryFailedRequests()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

@Singleton
class RetryWorkManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleRetryWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest = PeriodicWorkRequestBuilder<RetryFailedRequestsWorker>(
            15,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "retry_failed_requests_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun cancelRetryWork() {
        WorkManager.getInstance(context).cancelUniqueWork("retry_failed_requests_work")
    }
}