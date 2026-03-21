package com.tuapp.petcare.features.medical.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuapp.petcare.features.medical.domain.entities.Vaccine
import com.tuapp.petcare.features.medical.domain.usecases.AddVaccineUseCase
import com.tuapp.petcare.features.medical.domain.usecases.DeleteVaccineUseCase
import com.tuapp.petcare.features.medical.domain.usecases.GetVaccinesUseCase
import com.tuapp.petcare.features.medical.presentation.screens.AddVaccineUiState
import com.tuapp.petcare.features.medical.presentation.screens.MedHistoryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedHistoryViewModel @Inject constructor(
    private val getVaccinesUseCase: GetVaccinesUseCase,
    private val addVaccineUseCase: AddVaccineUseCase,
    private val deleteVaccineUseCase: DeleteVaccineUseCase
) : ViewModel() {

    private val _historyState = MutableStateFlow(MedHistoryUiState())
    val historyState = _historyState.asStateFlow()

    private val _addState = MutableStateFlow(AddVaccineUiState())
    val addState = _addState.asStateFlow()

    // Guarda el petId actual para recargar después de eliminar
    private var currentPetId: String = ""

    fun loadVaccines(petId: String) {
        currentPetId = petId
        viewModelScope.launch {
            _historyState.update { it.copy(isLoading = true) }
            getVaccinesUseCase(petId)
                .catch { e ->
                    _historyState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { vaccines ->
                    _historyState.update { it.copy(isLoading = false, vaccines = vaccines) }
                }
        }
    }

    fun onNameChange(v: String)         = _addState.update { it.copy(name = v, error = null) }
    fun onDateChange(v: String)         = _addState.update { it.copy(date = v) }
    fun onNextDoseDateChange(v: String) = _addState.update { it.copy(nextDoseDate = v) }
    fun onVetChange(v: String)          = _addState.update { it.copy(veterinarian = v) }
    fun onNotesChange(v: String)        = _addState.update { it.copy(notes = v) }

    fun onQrScanned(qrContent: String) {
        val parts = qrContent.split("|")
        _addState.update {
            it.copy(
                name         = parts.getOrElse(0) { "" },
                date         = parts.getOrElse(1) { "" },
                nextDoseDate = parts.getOrElse(2) { "" },
                veterinarian = parts.getOrElse(3) { "" }
            )
        }
    }

    fun onSaveVaccine(petId: String) {
        viewModelScope.launch {
            _addState.update { it.copy(isLoading = true, error = null) }
            val s = _addState.value
            val result = addVaccineUseCase(
                Vaccine(
                    id           = "",
                    petId        = petId,
                    name         = s.name,
                    date         = s.date,
                    nextDoseDate = s.nextDoseDate,
                    veterinarian = s.veterinarian,
                    notes        = s.notes
                )
            )
            result.fold(
                onSuccess = { _addState.update { it.copy(isLoading = false, isSuccess = true) } },
                onFailure = { e -> _addState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    // ── NUEVO: eliminar vacuna ─────────────────────────────────────────────
    fun onDeleteVaccine(vaccineId: String) {
        viewModelScope.launch {
            deleteVaccineUseCase(vaccineId)
            // Room actualiza el Flow automáticamente, no necesita recarga manual
        }
    }
}