package com.example.ivss.server.routes

import com.example.ivss.server.db.EmpleadosTable
import com.example.ivss.server.model.Rol
import com.example.ivss.server.plugins.cedulaActual
import com.example.ivss.server.plugins.rolActual
import com.example.ivss.server.plugins.servicioActual
import com.example.ivss.server.plugins.usernameActual
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.protectedRoutes() {
    authenticate("auth-jwt") {

        get("/me") {
            call.respond(mapOf(
                "username" to call.usernameActual(),
                "rol" to call.rolActual(),
                "cedula" to call.cedulaActual(),
                "servicio" to call.servicioActual()
            ))
        }

        // EMPLEADO: ver su propio perfil y vacaciones
        route("/empleado") {
            get("/perfil") {
                val cedula = call.cedulaActual()
                    ?: return@get call.respond(HttpStatusCode.Forbidden, mapOf("error" to "No vinculado a una cédula"))
                val emp = transaction {
                    EmpleadosTable.selectAll().where { EmpleadosTable.cedula eq cedula }
                        .map { row ->
                            mapOf(
                                "cedula" to row[EmpleadosTable.cedula],
                                "nombre" to "${row[EmpleadosTable.nombre1]} ${row[EmpleadosTable.apellido1]}",
                                "cargo" to row[EmpleadosTable.nombreCargo],
                                "servicio" to row[EmpleadosTable.descripcionUbi],
                                "fechaIngreso" to row[EmpleadosTable.fechaIngreso].toString()
                            )
                        }.firstOrNull()
                }
                if (emp != null) {
                    call.respond(emp)
                } else {
                    call.respond(mapOf("cedula" to cedula))
                }
            }

            get("/vacaciones") {
                val cedula = call.cedulaActual()
                    ?: return@get call.respond(HttpStatusCode.Forbidden)
                call.respond(mapOf("cedula" to cedula, "diasValidos" to 15, "estado" to "Aprobada"))
            }

            post("/solicitar-vacaciones") {
                val cedula = call.cedulaActual()
                    ?: return@post call.respond(HttpStatusCode.Forbidden)
                call.respond(HttpStatusCode.Created, mapOf("ok" to true, "cedula" to cedula))
            }
        }

        // COORDINADOR, DIRECTOR y SUPER_USUARIO
        route("/gestion") {
            get("/empleados") {
                val rol = call.rolActual()
                if (rol !in listOf(Rol.COORDINADOR.name, Rol.DIRECTOR.name, Rol.SUPER_USUARIO.name)) {
                    return@get call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Sin permisos"))
                }

                val servicioFiltro = if (rol == Rol.COORDINADOR.name) call.servicioActual() else null
                val lista = transaction {
                    var query = EmpleadosTable.selectAll()
                    if (!servicioFiltro.isNullOrBlank()) {
                        query = query.where { EmpleadosTable.servicio eq servicioFiltro }
                    }
                    query.map { row ->
                        mapOf(
                            "cedula" to row[EmpleadosTable.cedula],
                            "nombre" to "${row[EmpleadosTable.nombre1]} ${row[EmpleadosTable.apellido1]}",
                            "cargo" to row[EmpleadosTable.nombreCargo],
                            "servicio" to row[EmpleadosTable.descripcionUbi]
                        )
                    }
                }

                call.respond(mapOf(
                    "rol" to rol,
                    "servicioFiltro" to servicioFiltro,
                    "empleados" to lista
                ))
            }

            get("/vacaciones/pendientes") {
                val rol = call.rolActual()
                if (rol !in listOf(Rol.COORDINADOR.name, Rol.DIRECTOR.name, Rol.SUPER_USUARIO.name)) {
                    return@get call.respond(HttpStatusCode.Forbidden)
                }
                call.respond(emptyList<Any>())
            }

            post("/vacaciones/{id}/aprobar") {
                if (!call.puedeAprobar())
                    return@post call.respond(HttpStatusCode.Forbidden)
                call.respond(mapOf("ok" to true, "mensaje" to "Solicitud aprobada"))
            }

            post("/vacaciones/{id}/rechazar") {
                if (!call.puedeAprobar())
                    return@post call.respond(HttpStatusCode.Forbidden)
                call.respond(mapOf("ok" to true, "mensaje" to "Solicitud rechazada"))
            }
        }

        // DIRECTOR y SUPER_USUARIO
        route("/director") {
            get("/reportes/vacaciones") {
                if (!call.esDirectorOSuper())
                    return@get call.respond(HttpStatusCode.Forbidden)
                call.respond(mapOf("status" to "OK", "reporte" to "Resumen ejecutivo de vacaciones"))
            }

            get("/servicios") {
                if (!call.esDirectorOSuper())
                    return@get call.respond(HttpStatusCode.Forbidden)
                call.respond(listOf("CIRUGIA", "ADMINISTRACION", "EMERGENCIA", "CONSULTA EXTERNA"))
            }
        }

        // SUPER_USUARIO
        route("/admin") {
            post("/reset-db") {
                if (call.rolActual() != Rol.SUPER_USUARIO.name)
                    return@post call.respond(HttpStatusCode.Forbidden)
                call.respond(mapOf("ok" to true, "mensaje" to "Base de datos reiniciada"))
            }
        }
    }
}

private fun ApplicationCall.puedeAprobar(): Boolean {
    val rol = rolActual()
    return rol in listOf(Rol.COORDINADOR.name, Rol.DIRECTOR.name, Rol.SUPER_USUARIO.name)
}

private fun ApplicationCall.esDirectorOSuper(): Boolean {
    val rol = rolActual()
    return rol in listOf(Rol.DIRECTOR.name, Rol.SUPER_USUARIO.name)
}
