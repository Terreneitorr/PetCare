package com.tuapp.petcare.features.pets.presentation.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuapp.petcare.features.pets.domain.entities.Pet
import com.tuapp.petcare.features.pets.domain.usecases.AddPetUseCase
import com.tuapp.petcare.features.pets.presentation.screens.AddPetUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddPetViewModel @Inject constructor(
    private val addPetUseCase: AddPetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddPetUiState())
    val uiState = _uiState.asStateFlow()

    fun onNameChange(v: String)    = _uiState.update { it.copy(name = v, error = null) }
    fun onSpeciesChange(v: String) = _uiState.update { it.copy(species = v, showSpeciesDropdown = false) }
    fun onBreedChange(v: String)   = _uiState.update { it.copy(breed = v) }
    fun onBirthDateChange(v: String) = _uiState.update { it.copy(birthDate = v) }
    fun onToggleDropdown()         = _uiState.update { it.copy(showSpeciesDropdown = !it.showSpeciesDropdown) }

    // Se llama cuando el usuario toma/selecciona una foto
    fun onPhotoSelected(uri: Uri) =
        _uiState.update { it.copy(photoUri = uri.toString()) }

    fun onSavePet(ownerId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val state = _uiState.value
            val result = addPetUseCase(
                Pet(
                    id        = UUID.randomUUID().toString(),
                    name      = state.name,
                    species   = state.species,
                    breed     = state.breed,
                    birthDate = state.birthDate,
                    photoUri  = state.photoUri,
                    ownerId   = ownerId
                )
            )
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isSuccess = true) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }
}
