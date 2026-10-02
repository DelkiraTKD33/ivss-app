package com.example.ivss.server.plugins

import com.example.ivss.server.security.JwtService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureSecurity() {
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JwtService.verifier)
            realm = "IVSS"
            validate { credential ->
                val username = credential.payload.subject
                val rol = credential.payload.getClaim("rol").asString()
                if (username != null && rol != null) {
                    JWTPrincipal(credential.payload)
                } else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Token inválido o expirado"))
            }
        }
    }
}

fun ApplicationCall.rolActual(): String? =
    principal<JWTPrincipal>()?.payload?.getClaim("rol")?.asString()

fun ApplicationCall.cedulaActual(): String? =
    principal<JWTPrincipal>()?.payload?.getClaim("cedula")?.asString()

fun ApplicationCall.servicioActual(): String? =
    principal<JWTPrincipal>()?.payload?.getClaim("servicio")?.asString()

fun ApplicationCall.usernameActual(): String? =
    principal<JWTPrincipal>()?.payload?.subject
