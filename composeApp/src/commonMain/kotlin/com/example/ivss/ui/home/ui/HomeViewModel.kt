package com.example.ivss.ui.home.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivss.data.remote.IvssApiClient
import com.example.ivss.data.repository.ProfileRepositoryImpl
import com.example.ivss.domain.model.UserProfile
import com.example.ivss.domain.repository.ProfileRepository
import com.example.ivss.platform.NativeEmployeeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ActiveEmployeeItem(
    val cedula: String,
    val nombre: String,
    val apellido: String,
    val cargo: String,
    val fechaIngreso: String,
    val servicio: String
)

class HomeViewModel(
    private val apiClient: IvssApiClient = IvssApiClient(),
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl()
) : ViewModel() {

    val userProfile: StateFlow<UserProfile?> = profileRepository.getUserProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _activeEmployees = mutableStateOf<List<ActiveEmployeeItem>>(emptyList())
    val activeEmployees: State<List<ActiveEmployeeItem>> = _activeEmployees

    private val _isLoadingEmployees = mutableStateOf(false)
    val isLoadingEmployees: State<Boolean> = _isLoadingEmployees

    init {
        loadEmployees()
    }

    fun loadEmployees() {
        _isLoadingEmployees.value = true
        viewModelScope.launch {
            val result = apiClient.getEmployees()
            if (result.isSuccess && result.getOrNull() != null && result.getOrNull()!!.isNotEmpty()) {
                val list = result.getOrNull()!!.map { emp ->
                    val names = emp.nombreCompleto.split(" ")
                    val first = names.firstOrNull() ?: emp.nombreCompleto
                    val last = if (names.size > 1) names.subList(1, names.size).joinToString(" ") else ""
                    ActiveEmployeeItem(
                        cedula = emp.cedula,
                        nombre = first,
                        apellido = last,
                        cargo = emp.cargo.ifBlank { "MÉDICO / TRABAJADOR IVSS" },
                        fechaIngreso = emp.fechaIngreso,
                        servicio = emp.servicio
                    )
                }
                _activeEmployees.value = list
            } else {
                // Carga la lista completa de todos los trabajadores desde la base de datos Excel
                _activeEmployees.value = NativeEmployeeRepository.readAllEmployees()
            }
            _isLoadingEmployees.value = false
        }
    }
}
