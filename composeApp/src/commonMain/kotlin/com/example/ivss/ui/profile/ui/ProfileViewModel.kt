package com.example.ivss.ui.profile.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivss.domain.model.UserProfile
import com.example.ivss.domain.repository.ProfileRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val user: UserProfile) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

class ProfileViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _isDownloading = mutableStateOf(false)
    val isDownloading: State<Boolean> = _isDownloading

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    val uiState: StateFlow<ProfileUiState> = profileRepository.getUserProfile()
        .map { profile -> ProfileUiState.Success(profile) as ProfileUiState }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileUiState.Loading
        )

    fun updateProfileInfo(newPhone: String, newEmail: String) {
        val currentState = uiState.value
        if (currentState is ProfileUiState.Success) {
            val updated = currentState.user.copy(phone = newPhone, email = newEmail)
            viewModelScope.launch {
                val success = profileRepository.updateProfile(updated)
                if (success) {
                    _userMessage.value = "Datos de contacto actualizados correctamente"
                }
            }
        }
    }

    fun downloadConstancia() {
        if (_isDownloading.value) return
        _isDownloading.value = true
        viewModelScope.launch {
            delay(1500)
            _isDownloading.value = false
            _userMessage.value = "Constancia de Cotizaciones descargada con éxito"
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
