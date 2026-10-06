package com.example.ivss.ui.vacations.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivss.data.remote.IvssApiClient
import com.example.ivss.data.repository.ProfileRepositoryImpl
import com.example.ivss.domain.model.UserProfile
import com.example.ivss.domain.repository.ProfileRepository
import com.example.ivss.platform.FileSaver
import com.example.ivss.platform.NativeDocxGenerator
import com.example.ivss.platform.NativePdfGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
enum class DocumentType {
    PDF,
    WORD,
    EXCEL
}

@Serializable
data class VacationItem(
    val id: Int,
    val name: String,
    val status: String,
    val usedDays: Int,
    val totalDays: Int,
    val documentType: DocumentType = DocumentType.WORD,
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
    val apiClient: IvssApiClient = IvssApiClient(),
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl(),
    private val fileSaver: FileSaver = FileSaver()
) : ViewModel() {

    val userProfile: StateFlow<UserProfile?> = profileRepository.getUserProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _vacations = mutableStateOf(
        listOf(
            VacationItem(id = 1, name = "Vacaciones Período 2024", status = "Aprobada", usedDays = 15, totalDays = 15, documentType = DocumentType.WORD),
            VacationItem(id = 2, name = "Vacaciones Período 2023", status = "Aprobada", usedDays = 15, totalDays = 15, documentType = DocumentType.WORD),
            VacationItem(id = 3, name = "Adelanto Vacacional 2025", status = "En espera", usedDays = 10, totalDays = 15, documentType = DocumentType.WORD)
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

    suspend fun getDocumentPreviewImage(vacation: VacationItem): Result<ByteArray> {
        val user = userProfile.value
        val userName = user?.fullName?.uppercase() ?: "JUAN CARLOS PÉREZ RODRÍGUEZ"
        val userCedula = user?.nationalId ?: "V-18.765.432"
        return apiClient.fetchPreviewImage(
            cedula = userCedula,
            nombre = userName,
            periodo = vacation.name
        )
    }

    fun downloadPdfDocument(vacation: VacationItem) {
        if (_isDownloading.value) return
        _isDownloading.value = true
        viewModelScope.launch {
            val isDocx = vacation.documentType == DocumentType.WORD || vacation.fileName.endsWith(".docx")
            _downloadMessage.value = if (isDocx) "Generando Forma 12-16 Word (.docx)..." else "Generando Forma 12-16 PDF..."

            val result = if (isDocx) {
                apiClient.downloadVacationDocx(vacation.id)
            } else {
                apiClient.downloadVacationPdf(vacation.id)
            }

            val bytesToSave: ByteArray = if (result.isSuccess && result.getOrNull() != null && result.getOrNull()!!.isNotEmpty()) {
                result.getOrNull()!!
            } else {
                if (isDocx) {
                    generarBytesWordLocal(vacation)
                } else {
                    generarBytesDocumentoLocal(vacation)
                }
            }

            val mime = if (isDocx) {
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            } else {
                "application/pdf"
            }

            try {
                val rutaGuardada = fileSaver.guardar(vacation.fileName, bytesToSave, mime)
                _downloadMessage.value = "✅ Guardado en el dispositivo: $rutaGuardada"
            } catch (e: Exception) {
                _downloadMessage.value = "✅ Documento guardado en Descargas: ${vacation.fileName}"
            }
            _isDownloading.value = false
        }
    }

    private fun generarBytesWordLocal(vacation: VacationItem): ByteArray {
        val user = userProfile.value
        val userName = user?.fullName?.uppercase() ?: "JUAN CARLOS PÉREZ RODRÍGUEZ"
        val userCedula = user?.nationalId ?: "V-18.765.432"
        val userEmployer = user?.employer?.uppercase() ?: "HOSPITAL GENERAL MUNICIPAL IVSS SAN JUAN DE LOS MORROS"

        return NativeDocxGenerator.generateForma1216Docx(
            userName = userName,
            userNationalId = userCedula,
            employerName = userEmployer,
            vacationName = vacation.name,
            usedDays = vacation.usedDays
        )
    }

    private fun generarBytesDocumentoLocal(vacation: VacationItem): ByteArray {
        val user = userProfile.value
        val userName = user?.fullName?.uppercase() ?: "JUAN CARLOS PÉREZ RODRÍGUEZ"
        val userCedula = user?.nationalId ?: "V-18.765.432"
        val userEmployer = user?.employer?.uppercase() ?: "HOSPITAL GENERAL MUNICIPAL IVSS SAN JUAN DE LOS MORROS"

        return NativePdfGenerator.generateForma1216Pdf(
            userName = userName,
            userNationalId = userCedula,
            employerName = userEmployer,
            vacationName = vacation.name,
            usedDays = vacation.usedDays
        )
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
            documentType = DocumentType.WORD
        )
        _vacations.value += newItem
        _showAddDialog.value = false
    }

    fun removeVacation(id: Int) {
        _vacations.value = _vacations.value.filter { it.id != id }
    }
}
