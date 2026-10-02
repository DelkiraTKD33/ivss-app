package com.example.ivss.server.routes

import com.example.ivss.server.db.UsuariosTable
import com.example.ivss.server.model.*
import com.example.ivss.server.plugins.rolActual
import com.example.ivss.server.plugins.usernameActual
import com.example.ivss.server.security.JwtService
import com.example.ivss.server.security.PasswordUtil
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.authRoutes() {

    route("/auth") {

        // LOGIN público
        post("/login") {
            val req = call.receive<LoginRequest>()
            val row = transaction {
                UsuariosTable.selectAll().where { UsuariosTable.username eq req.username }
                    .firstOrNull()
            } ?: return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Credenciales inválidas"))

            if (!row[UsuariosTable.activo])
                return@post call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Usuario inactivo"))

            if (!PasswordUtil.verify(req.password, row[UsuariosTable.passwordHash]))
                return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Credenciales inválidas"))

            val rol = Rol.valueOf(row[UsuariosTable.rol])
            val token = JwtService.generateToken(
                username = row[UsuariosTable.username],
                rol = rol,
                cedula = row[UsuariosTable.cedula],
                servicio = row[UsuariosTable.servicio]
            )

            val usuario = Usuario(
                id = row[UsuariosTable.id].value,
                username = row[UsuariosTable.username],
                nombreCompleto = row[UsuariosTable.nombreCompleto],
                email = row[UsuariosTable.email],
                rol = rol,
                cedula = row[UsuariosTable.cedula],
                servicio = row[UsuariosTable.servicio],
                activo = row[UsuariosTable.activo]
            )

            call.respond(LoginResponse(token, usuario))
        }

        // Registro y gestión de usuarios (solo SUPER_USUARIO)
        authenticate("auth-jwt") {
            post("/register") {
                if (call.rolActual() != Rol.SUPER_USUARIO.name) {
                    return@post call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Solo el super usuario puede crear usuarios"))
                }

                val u = call.receive<Usuario>()
                if (u.password.isNullOrBlank())
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Password requerido"))

                val existe = transaction {
                    UsuariosTable.selectAll().where { UsuariosTable.username eq u.username }.any()
                }
                if (existe)
                    return@post call.respond(HttpStatusCode.Conflict, mapOf("error" to "Username ya existe"))

                val id = transaction {
                    UsuariosTable.insert {
                        it[username] = u.username
                        it[passwordHash] = PasswordUtil.hash(u.password)
                        it[nombreCompleto] = u.nombreCompleto
                        it[email] = u.email
                        it[rol] = u.rol.name
                        it[cedula] = u.cedula
                        it[servicio] = u.servicio
                        it[activo] = u.activo
                    } get UsuariosTable.id
                }.value

                call.respond(HttpStatusCode.Created, mapOf("id" to id))
            }

            get("/usuarios") {
                if (call.rolActual() != Rol.SUPER_USUARIO.name) {
                    return@get call.respond(HttpStatusCode.Forbidden)
                }
                val lista = transaction {
                    UsuariosTable.selectAll().map { row ->
                        Usuario(
                            id = row[UsuariosTable.id].value,
                            username = row[UsuariosTable.username],
                            nombreCompleto = row[UsuariosTable.nombreCompleto],
                            email = row[UsuariosTable.email],
                            rol = Rol.valueOf(row[UsuariosTable.rol]),
                            cedula = row[UsuariosTable.cedula],
                            servicio = row[UsuariosTable.servicio],
                            activo = row[UsuariosTable.activo]
                        )
                    }
                }
                call.respond(lista)
            }

            put("/usuarios/{id}/rol") {
                if (call.rolActual() != Rol.SUPER_USUARIO.name)
                    return@put call.respond(HttpStatusCode.Forbidden)

                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest)
                val nuevoRol = call.request.queryParameters["rol"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Falta parámetro rol"))

                try { Rol.valueOf(nuevoRol) } catch (e: Exception) {
                    return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Rol inválido"))
                }

                transaction {
                    UsuariosTable.update({ UsuariosTable.id eq id }) {
                        it[rol] = nuevoRol
                    }
                }
                call.respond(mapOf("ok" to true))
            }

            put("/usuarios/{id}/activo") {
                if (call.rolActual() != Rol.SUPER_USUARIO.name)
                    return@put call.respond(HttpStatusCode.Forbidden)

                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest)
                val activo = call.request.queryParameters["activo"]?.toBooleanStrictOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest)

                transaction {
                    UsuariosTable.update({ UsuariosTable.id eq id }) { it[UsuariosTable.activo] = activo }
                }
                call.respond(mapOf("ok" to true))
            }

            put("/usuarios/me/password") {
                val username = call.usernameActual()
                    ?: return@put call.respond(HttpStatusCode.Unauthorized)
                val nuevaPass = call.request.queryParameters["password"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest)

                transaction {
                    UsuariosTable.update({ UsuariosTable.username eq username }) {
                        it[passwordHash] = PasswordUtil.hash(nuevaPass)
                    }
                }
                call.respond(mapOf("ok" to true))
            }
        }
    }
}
