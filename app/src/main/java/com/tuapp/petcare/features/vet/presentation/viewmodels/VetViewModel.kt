package com.tuapp.petcare.features.vet.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.tuapp.petcare.core.workers.MedicineNotifyWorker
import com.tuapp.petcare.features.vet.domain.entities.Medicine
import com.tuapp.petcare.features.vet.domain.usecases.AddMedicineUseCase
import com.tuapp.petcare.features.vet.domain.usecases.DeleteMedicineUseCase
import com.tuapp.petcare.features.vet.domain.usecases.GetMedicinesUseCase
import com.tuapp.petcare.features.vet.domain.usecases.UpdateMedicineQuantityUseCase
import com.tuapp.petcare.features.vet.presentation.screens.AddMedicineUiState
import com.tuapp.petcare.features.vet.presentation.screens.NotifyStatus
import com.tuapp.petcare.features.vet.presentation.screens.VetUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VetViewModel @Inject constructor(
    private val getMedicinesUseCase: GetMedicinesUseCase,
    private val addMedicineUseCase: AddMedicineUseCase,
    private val deleteMedicineUseCase: DeleteMedicineUseCase,
    private val updateMedicineQuantityUseCase: UpdateMedicineQuantityUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(VetUiState())
    val uiState = _uiState.asStateFlow()

    private val _addState = MutableStateFlow(AddMedicineUiState())
    val addState = _addState.asStateFlow()

    init {
        loadMedicines()
    }

    private fun loadMedicines() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getMedicinesUseCase()
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { list -> _uiState.update { it.copy(isLoading = false, medicines = list) } }
        }
    }

    fun onNameChange(v: String)        = _addState.update { it.copy(name = v, error = null) }
    fun onQuantityChange(v: String)    = _addState.update { it.copy(quantity = v.filter { it.isDigit() }, error = null) }
    fun onUnitChange(v: String)        = _addState.update { it.copy(unit = v, showUnitDropdown = false) }
    fun onExpiryDateChange(v: String)  = _addState.update { it.copy(expiryDate = v) }
    fun onDescriptionChange(v: String) = _addState.update { it.copy(description = v) }
    fun onToggleUnitDropdown()         = _addState.update { it.copy(showUnitDropdown = !it.showUnitDropdown) }

    fun onSaveMedicine() {
        viewModelScope.launch {
            _addState.update { it.copy(isLoading = true, error = null) }
            val s = _addState.value

            // Valida nombre
            if (s.name.isBlank()) {
                _addState.update { it.copy(isLoading = false, error = "El nombre es obligatorio") }
                return@launch
            }

            // Valida cantidad
            val qty = s.quantity.toIntOrNull()
            if (qty == null || qty <= 0) {
                _addState.update {
                    it.copy(
                        isLoading = false,
                        error = "Ingresa una cantidad válida (número mayor a 0)"
                    )
                }
                return@launch
            }

            val result = addMedicineUseCase(
                Medicine(
                    id          = "",
                    name        = s.name,
                    quantity    = qty,
                    unit        = s.unit,
                    expiryDate  = s.expiryDate,
                    description = s.description
                )
            )
            result.fold(
                onSuccess = {
                    _addState.update { it.copy(isLoading = false, isSuccess = true) }
                    observeNotifyWorker(s.name)
                },
                onFailure = { e ->
                    _addState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    // Observa el WorkManager del medicamento en tiempo real
    private fun observeNotifyWorker(medicineName: String) {
        viewModelScope.launch {
            WorkManager.getInstance(context)
                .getWorkInfosByTagFlow(MedicineNotifyWorker.WORK_NAME)
                .catch { }
                .collect { workInfos ->
                    val status = when (workInfos.firstOrNull()?.state) {
                        WorkInfo.State.RUNNING   -> NotifyStatus.NOTIFYING
                        WorkInfo.State.ENQUEUED  -> NotifyStatus.NOTIFYING
                        WorkInfo.State.SUCCEEDED -> NotifyStatus.SENT
                        else                     -> NotifyStatus.IDLE
                    }
                    _uiState.update { it.copy(notifyStatus = status) }
                }
        }
    }

    fun onDeleteMedicine(id: String) {
        viewModelScope.launch { deleteMedicineUseCase(id) }
    }

    fun onUpdateQuantity(id: String, quantity: Int) {
        viewModelScope.launch { updateMedicineQuantityUseCase(id, quantity) }
    }

    fun resetAddSuccess() = _addState.update { it.copy(isSuccess = false) }
}