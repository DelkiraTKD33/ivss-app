package com.example.ivss.data.remote

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
data class AuthResponseDto(val success: Boolean, val token: String? = null, val message: String)

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
)

@Serializable
data class UpdateContactRequestDto(val phone: String, val email: String)

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

    // URL base del servidor Ktor Backend
    private val baseUrl = "http://10.0.2.2:8080/api"

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

    suspend fun updateContact(phone: String, email: String): Result<UserProfileRemoteDto> = runCatching {
        val response: ApiServerResponse<UserProfileRemoteDto> = client.put("$baseUrl/profile/contact") {
            contentType(ContentType.Application.Json)
            setBody(UpdateContactRequestDto(phone, email))
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

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = runCatching {
        val response: ApiServerResponse<Unit> = client.put("$baseUrl/settings/password") {
            contentType(ContentType.Application.Json)
            setBody(ChangePasswordRequestDto(currentPassword, newPassword))
        }.body()
        if (!response.success) throw Exception(response.message)
    }
}
