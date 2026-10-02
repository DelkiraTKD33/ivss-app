package com.example.ivss.server.routes

import com.example.ivss.server.model.DatosConstancia
import com.example.ivss.server.model.Rol
import com.example.ivss.server.plugins.rolActual
import com.example.ivss.server.services.PdfService
import com.example.ivss.server.services.WordService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.constanciaRoutes() {
    authenticate("auth-jwt") {

        post("/constancia/preview") {
            val datos = call.receive<DatosConstancia>()
            call.respond(datos)
        }

        post("/constancia/pdf") {
            if (!call.puedeGenerarConstancia()) {
                return@post call.respond(HttpStatusCode.Forbidden)
            }
            val datos = call.receive<DatosConstancia>()
            val pdf = PdfService.generarConstancia(datos)
            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "constancia_${datos.cedula}.pdf").toString()
            )
            call.respondBytes(pdf, ContentType.Application.Pdf, HttpStatusCode.OK)
        }

        post("/constancia/word") {
            if (!call.puedeGenerarConstancia()) {
                return@post call.respond(HttpStatusCode.Forbidden)
            }
            val datos = call.receive<DatosConstancia>()
            val docx = WordService.generarConstancia(datos)
            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "constancia_${datos.cedula}.docx").toString()
            )
            call.respondBytes(
                docx,
                ContentType.parse("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
                HttpStatusCode.OK
            )
        }
    }
}

private fun ApplicationCall.puedeGenerarConstancia(): Boolean {
    val rol = rolActual() ?: return true
    return rol in listOf(Rol.COORDINADOR.name, Rol.DIRECTOR.name, Rol.SUPER_USUARIO.name, Rol.EMPLEADO.name)
}
