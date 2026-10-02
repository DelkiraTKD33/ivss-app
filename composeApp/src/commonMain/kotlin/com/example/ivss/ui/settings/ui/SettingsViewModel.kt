package com.example.ivss.ui.settings.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivss.domain.repository.SettingsRepository
import com.example.ivss.domain.repository.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ChangePasswordState {
    data object Idle : ChangePasswordState
    data object Loading : ChangePasswordState
    data class Success(val message: String) : ChangePasswordState
    data class Error(val error: String) : ChangePasswordState
}

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = settingsRepository.getUserPreferences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    private val _showChangePasswordDialog = mutableStateOf(false)
    val showChangePasswordDialog: State<Boolean> = _showChangePasswordDialog

    private val _changePasswordState = mutableStateOf<ChangePasswordState>(ChangePasswordState.Idle)
    val changePasswordState: State<ChangePasswordState> = _changePasswordState

    fun onOpenChangePasswordDialog() {
        _changePasswordState.value = ChangePasswordState.Idle
        _showChangePasswordDialog.value = true
    }

    fun onDismissChangePasswordDialog() {
        _showChangePasswordDialog.value = false
        _changePasswordState.value = ChangePasswordState.Idle
    }

    fun toggleBiometrics(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBiometricsEnabled(enabled)
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkModeEnabled(enabled)
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        _changePasswordState.value = ChangePasswordState.Loading
        viewModelScope.launch {
            val result = settingsRepository.changePassword(currentPassword, newPassword)
            result.fold(
                onSuccess = {
                    _changePasswordState.value = ChangePasswordState.Success("¡Contraseña actualizada exitosamente!")
                },
                onFailure = { exception ->
                    _changePasswordState.value = ChangePasswordState.Error(
                        exception.message ?: "Ocurrió un error al cambiar la contraseña."
                    )
                }
            )
        }
    }
}
