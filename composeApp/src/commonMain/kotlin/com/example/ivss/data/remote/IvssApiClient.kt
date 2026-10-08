package com.example.ivss.data.remote

import com.example.ivss.getPlatform
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// DTOs para comunicación cliente-servidor

@Serializable
data class AuthRequestDto(val email: String, val password: String)

@Serializable
data class AuthResponseDto(
    val success: Boolean,
    val token: String? = null,
    val message: String,
    val userProfile: UserProfileRemoteDto? = null
)

@Serializable
data class UserProfileRemoteDto(
    val id: String,
    val fullName: String,
    val nationalId: String,
    val email: String,
    val phone: String,
    val birthDate: String,
    val affiliationNumber: String,
    val status: String,
    val employer: String,
    val weeksContributed: Int,
    val servicio: String = ""
)

@Serializable
data class EmpleadoRemoteDto(
    val cedula: String,
    val nombreCompleto: String,
    val cargo: String,
    val servicio: String,
    val tipoPersonal: String,
    val fechaIngreso: String
)

@Serializable
data class UpdateContactRequestDto(
    val phone: String,
    val email: String,
    val birthDate: String = "",
    val servicio: String = ""
)

@Serializable
data class VacationRemoteDto(
    val id: Int,
    val name: String,
    val status: String,
    val usedDays: Int,
    val totalDays: Int,
    val documentType: String,
    val fileName: String,
)

@Serializable
data class CreateVacationRequestDto(val name: String, val totalDays: Int, val documentType: String)

@Serializable
data class ChangePasswordRequestDto(val currentPassword: String, val newPassword: String)

@Serializable
data class ApiServerResponse<T>(val success: Boolean, val message: String, val data: T? = null)

class IvssApiClient {

    // URL base según la plataforma (localhost en Web/Desktop, 10.0.2.2 en Android Emulator)
    private val baseUrl: String
        get() = if (getPlatform().name.contains("Android", ignoreCase = true)) {
            "http://10.0.2.2:8080/api"
        } else {
            "http://localhost:8080/api"
        }

    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResponseDto> = runCatching {
        client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(AuthRequestDto(email, password))
        }.body()
    }

    suspend fun getProfile(): Result<UserProfileRemoteDto> = runCatching {
        val response: ApiServerResponse<UserProfileRemoteDto> = client.get("$baseUrl/profile").body()
        response.data ?: throw Exception(response.message)
    }

    suspend fun getEmployees(): Result<List<EmpleadoRemoteDto>> = runCatching {
        val response: ApiServerResponse<List<EmpleadoRemoteDto>> = client.get("$baseUrl/employees").body()
        response.data ?: emptyList()
    }

    suspend fun updateContact(phone: String, email: String, birthDate: String = "", servicio: String = ""): Result<UserProfileRemoteDto> = runCatching {
        val response: ApiServerResponse<UserProfileRemoteDto> = client.put("$baseUrl/profile/contact") {
            contentType(ContentType.Application.Json)
            setBody(UpdateContactRequestDto(phone, email, birthDate, servicio))
        }.body()
        response.data ?: throw Exception(response.message)
    }

    suspend fun getVacations(): Result<List<VacationRemoteDto>> = runCatching {
        val response: ApiServerResponse<List<VacationRemoteDto>> = client.get("$baseUrl/vacations").body()
        response.data ?: emptyList()
    }

    suspend fun createVacation(name: String, totalDays: Int, docType: String): Result<VacationRemoteDto> = runCatching {
        val response: ApiServerResponse<VacationRemoteDto> = client.post("$baseUrl/vacations") {
            contentType(ContentType.Application.Json)
            setBody(CreateVacationRequestDto(name, totalDays, docType))
        }.body()
        response.data ?: throw Exception(response.message)
    }

    suspend fun downloadVacationPdf(vacationId: Int): Result<ByteArray> = runCatching {
        client.get("$baseUrl/vacations/$vacationId/pdf").body()
    }

    suspend fun downloadVacationDocx(vacationId: Int): Result<ByteArray> = runCatching {
        client.get("$baseUrl/vacations/$vacationId/docx").body()
    }

    suspend fun fetchPreviewImage(cedula: String, nombre: String, periodo: String): Result<ByteArray> = runCatching {
        client.post("$baseUrl/constancia/preview-imagen") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "cedula" to cedula,
                    "apellidosNombres" to nombre,
                    "denominacionCargo" to "ANALISTA TÉCNICO I",
                    "numeroCargo" to "00101",
                    "fechaIngreso" to "2019-11-01",
                    "codigoOrigenServicio" to "60209382 - 31",
                    "unidadServicio" to "ADMINISTRACIÓN Y RRHH",
                    "lugar" to "SAN JUAN DE LOS MORROS",
                    "horario" to "ASISTENCIAL",
                    "fechaDesde" to "2025-10-15",
                    "fechaHasta" to "2025-11-17",
                    "periodo" to periodo,
                    "numeroDias" to 15,
                    "fechaReintegro" to "2025-11-18",
                    "observaciones" to "Solicitud aprobada y registrada en el sistema IVSS.",
                    "nota" to "EL TRABAJADOR SOLICITÓ DICHAS VACACIONES CON EXPOSICIÓN DE MOTIVOS.",
                    "supervisorInmediato" to "DR. WILLIAMS GONZALEZ",
                    "coordinadorRRHH" to "LCDA. MAYARI SOJO",
                    "maximaAutoridad" to "DR. JULIO AQUINO"
                )
            )
        }.body()
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = runCatching {
        val response: ApiServerResponse<Unit> = client.put("$baseUrl/settings/password") {
            contentType(ContentType.Application.Json)
            setBody(ChangePasswordRequestDto(currentPassword, newPassword))
        }.body()
        if (!response.success) throw Exception(response.message)
    }
}
