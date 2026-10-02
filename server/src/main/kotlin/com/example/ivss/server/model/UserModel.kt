package com.example.ivss.server.model

import kotlinx.serialization.Serializable

@Serializable
enum class Rol {
    SUPER_USUARIO,
    DIRECTOR,
    COORDINADOR,
    EMPLEADO
}

@Serializable
data class Usuario(
    val id: Int? = null,
    val username: String,
    val password: String? = null,
    val nombreCompleto: String,
    val email: String? = null,
    val rol: Rol,
    val cedula: String? = null,
    val servicio: String? = null,
    val activo: Boolean = true
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val usuario: Usuario
)
