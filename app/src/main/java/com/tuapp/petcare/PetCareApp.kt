package com.tuapp.petcare

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.tuapp.petcare.core.workers.AlertCheckWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class PetCareApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // WorkManager se inicializa automáticamente con workManagerConfiguration
        // Programamos el worker después de que Hilt haya inyectado todo
        scheduleAlertCheck()
    }

    private fun scheduleAlertCheck() {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val alertRequest = PeriodicWorkRequestBuilder<AlertCheckWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .addTag(AlertCheckWorker.WORK_NAME)
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                AlertCheckWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                alertRequest
            )
        } catch (e: Exception) {
            android.util.Log.e("PetCareApp", "Error scheduling AlertCheckWorker: ${e.message}")
        }
    }
}