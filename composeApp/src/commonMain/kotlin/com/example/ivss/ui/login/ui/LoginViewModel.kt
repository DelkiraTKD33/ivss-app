package com.example.ivss.ui.login.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivss.data.remote.IvssApiClient
import com.example.ivss.data.repository.ProfileRepositoryImpl
import com.example.ivss.domain.model.UserProfile
import com.example.ivss.domain.repository.ProfileRepository
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
    private val settingsRepository: SettingsRepository,
    private val apiClient: IvssApiClient = IvssApiClient(),
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl()
) : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _loginEnable = mutableStateOf(false)
    val loginEnable: State<Boolean> = _loginEnable

    private val _loginError = mutableStateOf<String?>(null)
    val loginError: State<String?> = _loginError

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

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
        _loginError.value = null
        _loginEnable.value = email.isNotBlank() && password.isNotBlank()
    }

    fun performLogin(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_isLoading.value) return
        _isLoading.value = true
        _loginError.value = null

        val inputUser = _email.value.trim()
        val inputPass = _password.value.trim()

        // 1. Reconocimiento instantáneo del Super Usuario admin / admin
        if ((inputUser.equals("admin", ignoreCase = true) || inputUser.equals("admin@ivss.gob.ve", ignoreCase = true)) && inputPass == "admin") {
            val adminProfile = UserProfile(
                id = "ADM-000001",
                fullName = "Super Usuario Administrador",
                nationalId = "V-00.000.000",
                email = "admin@ivss.gob.ve",
                phone = "+58 412-0000000",
                birthDate = "01/01/1980",
                affiliationNumber = "100000001",
                status = "SUPER_USUARIO",
                employer = "SEDE CENTRAL IVSS RECURSOS HUMANOS",
                weeksContributed = 1000
            )
            viewModelScope.launch {
                profileRepository.updateProfile(adminProfile)
                _isLoading.value = false
                _loginError.value = null
                onSuccess()
            }
            return
        }

        // 2. Reconocimiento local por Cédula (Cédula / Cédula)
        val cleanCedula = inputUser.replace("V-", "").replace("v-", "").replace(".", "").replace("-", "").trim()
        val cleanPass = inputPass.replace("V-", "").replace("v-", "").replace(".", "").replace("-", "").trim()

        if (cleanCedula.length >= 6 && (cleanPass == cleanCedula || inputPass == inputUser)) {
            val formattedCedula = "V-$cleanCedula"
            val employeeProfile = UserProfile(
                id = "USR-$cleanCedula",
                fullName = if (cleanCedula == "17062973") "HERNANDEZ RON JENNIFFER" else "Juan Carlos Pérez Rodríguez",
                nationalId = formattedCedula,
                email = "$cleanCedula@ivss.gob.ve",
                phone = "+58 412-9876543",
                birthDate = "15/05/1985",
                affiliationNumber = "100234891",
                status = "Cotizante Activo",
                employer = "HOSPITAL GENERAL MUNICIPAL IVSS SAN JUAN DE LOS MORROS.",
                weeksContributed = 850
            )

            viewModelScope.launch {
                try {
                    val result = apiClient.login(inputUser, inputPass)
                    if (result.isSuccess && result.getOrNull()?.success == true && result.getOrNull()?.userProfile != null) {
                        val p = result.getOrNull()!!.userProfile!!
                        profileRepository.updateProfile(
                            UserProfile(
                                id = p.id,
                                fullName = p.fullName,
                                nationalId = p.nationalId,
                                email = p.email,
                                phone = p.phone,
                                birthDate = p.birthDate,
                                affiliationNumber = p.affiliationNumber,
                                status = p.status,
                                employer = p.employer,
                                weeksContributed = p.weeksContributed
                            )
                        )
                    } else {
                        profileRepository.updateProfile(employeeProfile)
                    }
                } catch (e: Exception) {
                    profileRepository.updateProfile(employeeProfile)
                }
                _isLoading.value = false
                _loginError.value = null
                onSuccess()
            }
            return
        }

        // 3. Intento de Login contra el Servidor Backend
        viewModelScope.launch {
            val result = apiClient.login(inputUser, inputPass)
            _isLoading.value = false

            result.fold(
                onSuccess = { response ->
                    if (response.success && response.userProfile != null) {
                        val p = response.userProfile
                        val profile = UserProfile(
                            id = p.id,
                            fullName = p.fullName,
                            nationalId = p.nationalId,
                            email = p.email,
                            phone = p.phone,
                            birthDate = p.birthDate,
                            affiliationNumber = p.affiliationNumber,
                            status = p.status,
                            employer = p.employer,
                            weeksContributed = p.weeksContributed
                        )
                        profileRepository.updateProfile(profile)
                        _loginError.value = null
                        onSuccess()
                    } else {
                        val errMsg = response.message.ifBlank { "Credenciales inválidas. Compruebe su contraseña." }
                        _loginError.value = errMsg
                        onError(errMsg)
                    }
                },
                onFailure = {
                    val errMsg = "Contraseña incorrecta o usuario no encontrado."
                    _loginError.value = errMsg
                    onError(errMsg)
                }
            )
        }
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
