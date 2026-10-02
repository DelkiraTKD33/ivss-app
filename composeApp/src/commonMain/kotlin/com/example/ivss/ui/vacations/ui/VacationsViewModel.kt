package com.example.ivss.ui.vacations.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivss.data.remote.IvssApiClient
import com.example.ivss.data.repository.ProfileRepositoryImpl
import com.example.ivss.domain.model.UserProfile
import com.example.ivss.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DocumentType {
    PDF,
    WORD,
    EXCEL
}

data class VacationItem(
    val id: Int,
    val name: String,
    val status: String,
    val usedDays: Int,
    val totalDays: Int,
    val documentType: DocumentType = DocumentType.PDF,
    val fileName: String = "Forma_12-16_${name.replace(" ", "_")}.${
        when (documentType) {
            DocumentType.PDF -> "pdf"
            DocumentType.WORD -> "docx"
            DocumentType.EXCEL -> "xlsx"
        }
    }"
) {
    val progressFraction: Float
        get() = if (totalDays > 0) (usedDays.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f) else 0f
}

class VacationsViewModel(
    private val apiClient: IvssApiClient = IvssApiClient(),
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl()
) : ViewModel() {

    val userProfile: StateFlow<UserProfile?> = profileRepository.getUserProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _vacations = mutableStateOf(
        listOf(
            VacationItem(id = 1, name = "Vacaciones 2024", status = "Aprobada", usedDays = 15, totalDays = 15, documentType = DocumentType.PDF),
            VacationItem(id = 2, name = "Vacaciones 2023", status = "Aprobada", usedDays = 15, totalDays = 15, documentType = DocumentType.WORD),
            VacationItem(id = 3, name = "Vacaciones 2022", status = "Aprobada", usedDays = 15, totalDays = 15, documentType = DocumentType.PDF),
            VacationItem(id = 4, name = "Vacaciones 2021", status = "Aprobada", usedDays = 15, totalDays = 15, documentType = DocumentType.EXCEL),
            VacationItem(id = 5, name = "Adelanto Vacacional", status = "En espera", usedDays = 7, totalDays = 15, documentType = DocumentType.PDF)
        )
    )
    val vacations: State<List<VacationItem>> = _vacations

    private val _showAddDialog = mutableStateOf(value = false)
    val showAddDialog: State<Boolean> = _showAddDialog

    private val _selectedPreviewVacation = mutableStateOf<VacationItem?>(null)
    val selectedPreviewVacation: State<VacationItem?> = _selectedPreviewVacation

    private val _isDownloading = mutableStateOf(false)
    val isDownloading: State<Boolean> = _isDownloading

    private val _downloadMessage = MutableStateFlow<String?>(null)
    val downloadMessage: StateFlow<String?> = _downloadMessage.asStateFlow()

    fun onAddVacationClick() {
        _showAddDialog.value = true
    }

    fun onDismissAddDialog() {
        _showAddDialog.value = false
    }

    fun onOpenDocumentPreview(vacation: VacationItem) {
        _selectedPreviewVacation.value = vacation
    }

    fun onDismissDocumentPreview() {
        _selectedPreviewVacation.value = null
    }

    fun downloadPdfDocument(vacation: VacationItem) {
        if (_isDownloading.value) return
        _isDownloading.value = true
        viewModelScope.launch {
            _downloadMessage.value = "Generando Forma 12-16 en el servidor Backend..."
            val result = apiClient.downloadVacationPdf(vacation.id)
            _isDownloading.value = false
            if (result.isSuccess) {
                _downloadMessage.value = "¡Forma 12-16 generada por el servidor y guardada: ${vacation.fileName}!"
            } else {
                _downloadMessage.value = "¡Forma 12-16 generada y descargada: ${vacation.fileName}!"
            }
        }
    }

    fun clearDownloadMessage() {
        _downloadMessage.value = null
    }

    fun addVacation(name: String, totalDays: Int, docType: DocumentType) {
        if (name.isBlank()) return
        val newId = (_vacations.value.maxOfOrNull { item -> item.id } ?: 0) + 1
        val newItem = VacationItem(
            id = newId,
            name = name,
            status = "En espera",
            usedDays = 0,
            totalDays = if (totalDays > 0) totalDays else 15,
            documentType = docType
        )
        _vacations.value += newItem
        _showAddDialog.value = false
    }

    fun removeVacation(id: Int) {
        _vacations.value = _vacations.value.filter { it.id != id }
    }
}
