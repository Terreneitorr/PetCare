package com.tuapp.petcare.core.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tuapp.petcare.features.profile.domain.repositories.ProfileRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class ProfileSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val profileRepository: ProfileRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val profile = profileRepository.getProfile().firstOrNull()
                ?: return Result.success()

            // Sincroniza con timestamp actualizado
            val syncResult = profileRepository.syncProfileWithBackend(
                profile.copy(updatedAt = System.currentTimeMillis())
            )

            when {
                syncResult.isSuccess -> Result.success()
                runAttemptCount < 2  -> Result.retry()
                else                 -> Result.success()
            }
        } catch (e: Exception) {
            Result.success()
        }
    }

    companion object {
        const val WORK_NAME = "profile_sync_work"
    }
}