package com.tuapp.petcare.features.pets.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.tuapp.petcare.core.workers.DataBackupWorker
import com.tuapp.petcare.features.pets.domain.usecases.DeletePetUseCase
import com.tuapp.petcare.features.pets.domain.usecases.GetPetsUseCase
import com.tuapp.petcare.features.pets.presentation.screens.BackupStatus
import com.tuapp.petcare.features.pets.presentation.screens.PetListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetListViewModel @Inject constructor(
    private val getPetsUseCase: GetPetsUseCase,
    private val deletePetUseCase: DeletePetUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPets()
        observeBackupWorker()
    }

    private fun loadPets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getPetsUseCase()
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { pets ->
                    _uiState.update { it.copy(isLoading = false, pets = pets, error = null) }
                }
        }
    }

    // ── Observa el DataBackupWorker en tiempo real ────────────────────────────
    private fun observeBackupWorker() {
        viewModelScope.launch {
            WorkManager.getInstance(context)
                .getWorkInfosForUniqueWorkFlow(DataBackupWorker.WORK_NAME)
                .catch { }
                .collect { workInfos ->
                    val status = when (workInfos.firstOrNull()?.state) {
                        WorkInfo.State.RUNNING   -> BackupStatus.SYNCING
                        WorkInfo.State.ENQUEUED  -> BackupStatus.SYNCING
                        WorkInfo.State.SUCCEEDED -> BackupStatus.SUCCESS
                        else                     -> BackupStatus.IDLE
                    }
                    _uiState.update { it.copy(backupStatus = status) }
                }
        }
    }

    fun onDeletePet(petId: String) {
        viewModelScope.launch { deletePetUseCase(petId) }
    }
}