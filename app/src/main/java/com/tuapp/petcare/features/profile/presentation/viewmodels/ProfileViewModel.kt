package com.tuapp.petcare.features.profile.presentation.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.tuapp.petcare.core.workers.ProfileSyncWorker
import com.tuapp.petcare.features.auth.domain.repositories.AuthRepository
import com.tuapp.petcare.features.profile.domain.entities.Profile
import com.tuapp.petcare.features.profile.domain.usecases.GetProfileUseCase
import com.tuapp.petcare.features.profile.domain.usecases.SaveProfileUseCase
import com.tuapp.petcare.features.profile.presentation.screens.EditProfileUiState
import com.tuapp.petcare.features.profile.presentation.screens.ProfileUiState
import com.tuapp.petcare.features.profile.presentation.screens.SyncStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val saveProfileUseCase: SaveProfileUseCase,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileUiState())
    val profileState = _profileState.asStateFlow()

    private val _editState = MutableStateFlow(EditProfileUiState())
    val editState = _editState.asStateFlow()

    private val _logoutState = MutableStateFlow(false)
    val logoutState = _logoutState.asStateFlow()

    init {
        loadProfile()
        observeWorkManager()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _profileState.update { it.copy(isLoading = true) }
            getProfileUseCase()
                .catch { e -> _profileState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { profile ->
                    _profileState.update { it.copy(isLoading = false, profile = profile) }
                    profile?.let { p ->
                        _editState.update {
                            it.copy(name = p.name, phone = p.phone, city = p.city, photoUri = p.photoUri)
                        }
                    }
                }
        }
    }

    private fun observeWorkManager() {
        viewModelScope.launch {
            WorkManager.getInstance(context)
                .getWorkInfosForUniqueWorkFlow(ProfileSyncWorker.WORK_NAME)
                .catch { }
                .collect { workInfos ->
                    val status = when (workInfos.firstOrNull()?.state) {
                        WorkInfo.State.RUNNING   -> SyncStatus.SYNCING
                        WorkInfo.State.SUCCEEDED -> SyncStatus.SUCCESS
                        WorkInfo.State.FAILED    -> SyncStatus.IDLE
                        WorkInfo.State.ENQUEUED  -> SyncStatus.SYNCING
                        WorkInfo.State.BLOCKED   -> SyncStatus.SYNCING
                        else                     -> SyncStatus.IDLE
                    }
                    _profileState.update { it.copy(syncStatus = status) }
                }
        }
    }

    fun onNameChange(v: String)  = _editState.update { it.copy(name = v, error = null) }
    fun onPhoneChange(v: String) = _editState.update { it.copy(phone = v) }
    fun onCityChange(v: String)  = _editState.update { it.copy(city = v) }

    fun onPhotoSelected(uri: Uri) {
        viewModelScope.launch {
            try {
                val fileName = "profile_${System.currentTimeMillis()}.jpg"
                val destFile = File(context.filesDir, fileName)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output -> input.copyTo(output) }
                }
                _editState.update { it.copy(photoUri = destFile.absolutePath) }
            } catch (e: Exception) {
                _editState.update { it.copy(photoUri = uri.toString()) }
            }
        }
    }

    fun onSaveProfile(userId: String, email: String) {
        viewModelScope.launch {
            _editState.update { it.copy(isLoading = true, error = null) }
            val s = _editState.value
            val result = saveProfileUseCase(
                Profile(
                    id = userId, name = s.name, email = email,
                    phone = s.phone, city = s.city, photoUri = s.photoUri
                )
            )
            result.fold(
                onSuccess = {
                    _editState.update { it.copy(isLoading = false, isSuccess = true) }
                    scheduleSyncWork()
                },
                onFailure = { e ->
                    _editState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    private fun scheduleSyncWork() {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(ProfileSyncWorker.WORK_NAME)
        val syncRequest = OneTimeWorkRequestBuilder<ProfileSyncWorker>().build()
        workManager.enqueueUniqueWork(
            ProfileSyncWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    // ── Cerrar sesión ─────────────────────────────────────────────────────────
    fun onLogout() {
        viewModelScope.launch {
            authRepository.clearSession()  // Borra el token del DataStore
            _logoutState.update { true }   // Notifica a la UI que debe navegar al login
        }
    }

    fun resetLogout() = _logoutState.update { false }

    fun resetEditSuccess() = _editState.update { it.copy(isSuccess = false) }
}