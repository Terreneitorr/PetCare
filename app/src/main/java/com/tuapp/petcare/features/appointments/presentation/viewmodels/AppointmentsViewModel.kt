package com.tuapp.petcare.features.appointments.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuapp.petcare.core.services.AppointmentCountdownService
import com.tuapp.petcare.features.appointments.domain.entities.Appointment
import com.tuapp.petcare.features.appointments.domain.usecases.AddAppointmentUseCase
import com.tuapp.petcare.features.appointments.domain.usecases.CompleteAppointmentUseCase
import com.tuapp.petcare.features.appointments.domain.usecases.DeleteAppointmentUseCase
import com.tuapp.petcare.features.appointments.domain.usecases.GetAppointmentsByPetUseCase
import com.tuapp.petcare.features.appointments.presentation.screens.AddAppointmentUiState
import com.tuapp.petcare.features.appointments.presentation.screens.AppointmentsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppointmentsViewModel @Inject constructor(
    private val getAppointmentsByPetUseCase: GetAppointmentsByPetUseCase,
    private val addAppointmentUseCase: AddAppointmentUseCase,
    private val deleteAppointmentUseCase: DeleteAppointmentUseCase,
    private val completeAppointmentUseCase: CompleteAppointmentUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppointmentsUiState())
    val uiState = _uiState.asStateFlow()

    private val _addState = MutableStateFlow(AddAppointmentUiState())
    val addState = _addState.asStateFlow()

    fun loadAppointments(petId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getAppointmentsByPetUseCase(petId)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { list ->
                    _uiState.update { it.copy(isLoading = false, appointments = list) }
                    // Solo gestiona el Foreground Service — la notificación
                    // la maneja AlarmManager, no este collect
                    val hayProximas = list.any {
                        !it.isCompleted &&
                                it.dateTimeMillis > System.currentTimeMillis() &&
                                it.dateTimeMillis < System.currentTimeMillis() + 24 * 60 * 60 * 1000L
                    }
                    if (hayProximas) {
                        AppointmentCountdownService.startService(context)
                    } else {
                        AppointmentCountdownService.stopService(context)
                    }
                }
        }
    }

    fun onTitleChange(v: String)        = _addState.update { it.copy(title = v, error = null) }
    fun onDescriptionChange(v: String)  = _addState.update { it.copy(description = v) }
    fun onVetChange(v: String)          = _addState.update { it.copy(veterinarian = v) }
    fun onDateTimeMillisChange(v: Long) = _addState.update { it.copy(dateTimeMillis = v) }
    fun onDateTextChange(v: String)     = _addState.update { it.copy(dateText = v) }
    fun onHourTextChange(v: String)     = _addState.update { it.copy(hourText = v) }
    fun onMinuteTextChange(v: String)   = _addState.update { it.copy(minuteText = v) }
    fun onAmPmToggle() = _addState.update {
        it.copy(amPm = if (it.amPm == "AM") "PM" else "AM")
    }

    fun onSaveAppointment(petId: String, petName: String) {
        viewModelScope.launch {
            _addState.update { it.copy(isLoading = true, error = null) }
            val s = _addState.value
            val result = addAppointmentUseCase(
                Appointment(
                    id             = "",
                    petId          = petId,
                    petName        = petName,
                    title          = s.title,
                    description    = s.description,
                    veterinarian   = s.veterinarian,
                    dateTimeMillis = s.dateTimeMillis
                )
            )
            result.fold(
                onSuccess = {
                    _addState.update { it.copy(isLoading = false, isSuccess = true) }
                    // La alarma ya se programó en AppointmentsRepositoryImpl
                    // No necesitamos hacer nada más aquí
                },
                onFailure = { e ->
                    _addState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun onDeleteAppointment(id: String) {
        viewModelScope.launch { deleteAppointmentUseCase(id) }
    }

    fun onCompleteAppointment(id: String) {
        viewModelScope.launch { completeAppointmentUseCase(id) }
    }

    fun resetAddSuccess() = _addState.update { it.copy(isSuccess = false) }

    override fun onCleared() {
        super.onCleared()
        AppointmentCountdownService.stopService(context)
    }
}