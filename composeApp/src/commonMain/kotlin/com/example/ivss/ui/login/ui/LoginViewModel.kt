package com.example.ivss.ui.login.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivss.domain.repository.SettingsRepository
import com.example.ivss.domain.repository.UserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface BiometricState {
    data object Idle : BiometricState
    data object Authenticating : BiometricState
    data object Success : BiometricState
    data class Error(val message: String) : BiometricState
}

class LoginViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _loginEnable = mutableStateOf(false)
    val loginEnable: State<Boolean> = _loginEnable

    val userPreferences: StateFlow<UserPreferences> = settingsRepository.getUserPreferences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    private val _showBiometricDialog = mutableStateOf(false)
    val showBiometricDialog: State<Boolean> = _showBiometricDialog

    private val _biometricState = mutableStateOf<BiometricState>(BiometricState.Idle)
    val biometricState: State<BiometricState> = _biometricState

    fun onLoginChanged(email: String, password: String) {
        _email.value = email
        _password.value = password
        _loginEnable.value = email.isNotBlank() && password.isNotBlank()
    }

    fun onOpenBiometricDialog() {
        _biometricState.value = BiometricState.Idle
        _showBiometricDialog.value = true
    }

    fun onDismissBiometricDialog() {
        _showBiometricDialog.value = false
        _biometricState.value = BiometricState.Idle
    }

    fun authenticateWithBiometrics(onSuccess: () -> Unit) {
        _biometricState.value = BiometricState.Authenticating
        viewModelScope.launch {
            delay(1200)
            _biometricState.value = BiometricState.Success
            delay(400)
            _showBiometricDialog.value = false
            onSuccess()
        }
    }
}
