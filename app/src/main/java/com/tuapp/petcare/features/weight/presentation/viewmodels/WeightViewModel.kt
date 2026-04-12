package com.tuapp.petcare.features.weight.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuapp.petcare.features.weight.domain.entities.WeightRecord
import com.tuapp.petcare.features.weight.domain.usecases.AddWeightRecordUseCase
import com.tuapp.petcare.features.weight.domain.usecases.DeleteWeightRecordUseCase
import com.tuapp.petcare.features.weight.domain.usecases.GetWeightRecordsUseCase
import com.tuapp.petcare.features.weight.presentation.screens.AddWeightUiState
import com.tuapp.petcare.features.weight.presentation.screens.WeightUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeightViewModel @Inject constructor(
    private val getWeightRecordsUseCase: GetWeightRecordsUseCase,
    private val addWeightRecordUseCase: AddWeightRecordUseCase,
    private val deleteWeightRecordUseCase: DeleteWeightRecordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeightUiState())
    val uiState = _uiState.asStateFlow()

    private val _addState = MutableStateFlow(AddWeightUiState())
    val addState = _addState.asStateFlow()

    fun loadRecords(petId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getWeightRecordsUseCase(petId)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { list -> _uiState.update { it.copy(isLoading = false, records = list) } }
        }
    }

    fun onWeightTextChange(v: String) = _addState.update { it.copy(weightText = v, error = null) }
    fun onNotesChange(v: String)      = _addState.update { it.copy(notes = v) }

    fun onSaveRecord(petId: String, petName: String) {
        viewModelScope.launch {
            _addState.update { it.copy(isLoading = true, error = null) }
            val s = _addState.value
            val weight = s.weightText.toFloatOrNull()
            if (weight == null || weight <= 0) {
                _addState.update { it.copy(isLoading = false, error = "Ingresa un peso válido") }
                return@launch
            }
            val result = addWeightRecordUseCase(
                WeightRecord(
                    id = "", petId = petId, petName = petName,
                    weightKg = weight, notes = s.notes
                )
            )
            result.fold(
                onSuccess = { _addState.update { it.copy(isLoading = false, isSuccess = true) } },
                onFailure = { e -> _addState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    fun onDeleteRecord(id: String) {
        viewModelScope.launch { deleteWeightRecordUseCase(id) }
    }

    fun resetSuccess() = _addState.update { it.copy(isSuccess = false) }
}