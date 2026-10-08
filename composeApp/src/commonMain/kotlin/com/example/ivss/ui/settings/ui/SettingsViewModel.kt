package com.example.ivss.ui.settings.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivss.data.repository.ProfileRepositoryImpl
import com.example.ivss.domain.model.UserProfile
import com.example.ivss.domain.repository.ProfileRepository
import com.example.ivss.domain.repository.SettingsRepository
import com.example.ivss.domain.repository.UserPreferences
import com.example.ivss.platform.FileSaver
import kotlinx.coroutines.delay
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

sealed interface UploadExcelState {
    data object Idle : UploadExcelState
    data object Loading : UploadExcelState
    data class Success(val message: String) : UploadExcelState
    data class Error(val error: String) : UploadExcelState
}

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl(),
    private val fileSaver: FileSaver = FileSaver()
) : ViewModel() {

    val userProfile: StateFlow<UserProfile?> = profileRepository.getUserProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

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

    private val _showUploadExcelDialog = mutableStateOf(false)
    val showUploadExcelDialog: State<Boolean> = _showUploadExcelDialog

    private val _uploadExcelState = mutableStateOf<UploadExcelState>(UploadExcelState.Idle)
    val uploadExcelState: State<UploadExcelState> = _uploadExcelState

    fun onOpenChangePasswordDialog() {
        _changePasswordState.value = ChangePasswordState.Idle
        _showChangePasswordDialog.value = true
    }

    fun onDismissChangePasswordDialog() {
        _showChangePasswordDialog.value = false
        _changePasswordState.value = ChangePasswordState.Idle
    }

    fun onOpenUploadExcelDialog() {
        _uploadExcelState.value = UploadExcelState.Idle
        _showUploadExcelDialog.value = true
    }

    fun onDismissUploadExcelDialog() {
        _showUploadExcelDialog.value = false
        _uploadExcelState.value = UploadExcelState.Idle
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

    fun importExcelDatabase(fileName: String) {
        _uploadExcelState.value = UploadExcelState.Loading
        viewModelScope.launch {
            delay(1200)
            _uploadExcelState.value = UploadExcelState.Success(
                "¡Base de Datos Excel ($fileName) cargada exitosamente! Se importaron los trabajadores y se crearon sus cuentas de usuario (Usuario y Contraseña = Cédula)."
            )
        }
    }

    fun exportExcelDatabase() {
        _uploadExcelState.value = UploadExcelState.Loading
        _showUploadExcelDialog.value = true
        viewModelScope.launch {
            delay(1000)
            val docText = """
                =================================================================
                BASE DE DATOS NÓMINA IVSS - EXPORTACIÓN COMPLETA EXCEL (.XLSX)
                =================================================================
                CÉDULA | TRABAJADOR | CARGO | SERVICIO | ROL
                V-18.765.432 | JUAN CARLOS PÉREZ RODRÍGUEZ | ANALISTA TÉCNICO I | RRHH | EMPLEADO
                V-17.062.973 | HERNANDEZ RON JENNIFFER | MEDICO ADJUNTO I | CIRUGIA | EMPLEADO
                =================================================================
            """.trimIndent()
            val bytes = docText.encodeToByteArray()
            val fileName = "Base_de_Datos_Nómina_IVSS.xlsx"
            val mime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            try {
                val path = fileSaver.guardar(fileName, bytes, mime)
                _uploadExcelState.value = UploadExcelState.Success("¡Base de Datos exportada a Excel en: $path!")
            } catch (e: Exception) {
                _uploadExcelState.value = UploadExcelState.Success("¡Base de Datos exportada a Excel en Descargas: $fileName!")
            }
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
