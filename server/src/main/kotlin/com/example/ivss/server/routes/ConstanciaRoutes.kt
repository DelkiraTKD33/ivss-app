package com.example.ivss.server.routes

import com.example.ivss.server.db.DocumentoRepository
import com.example.ivss.server.model.DatosConstancia
import com.example.ivss.server.model.Rol
import com.example.ivss.server.plugins.cedulaActual
import com.example.ivss.server.plugins.rolActual
import com.example.ivss.server.plugins.usernameActual
import com.example.ivss.server.services.FileStorageService
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
            if (!call.puedeGenerarConstancia())
                return@post call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Sin permisos"))

            val datos = call.receive<DatosConstancia>()
            val pdf = PdfService.generarConstancia(datos)

            val archivo = FileStorageService.guardarDocumento(
                cedula = datos.cedula,
                tipo = "PDF",
                bytes = pdf,
                periodo = datos.periodo
            )

            val id = DocumentoRepository.registrar(
                cedula = datos.cedula,
                tipo = "PDF",
                archivo = archivo,
                periodo = datos.periodo,
                generadoPor = call.usernameActual() ?: "desconocido"
            )

            call.response.header(
                HttpHeaders.ContentDisposition,
                "attachment; filename=\"${archivo.name}\""
            )
            call.response.header("X-Documento-Id", id.toString())
            call.respondBytes(pdf, ContentType.Application.Pdf, HttpStatusCode.OK)
        }

        post("/constancia/word") {
            if (!call.puedeGenerarConstancia())
                return@post call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Sin permisos"))

            val datos = call.receive<DatosConstancia>()
            val docx = WordService.generarConstancia(datos)

            val archivo = FileStorageService.guardarDocumento(
                cedula = datos.cedula,
                tipo = "WORD",
                bytes = docx,
                periodo = datos.periodo
            )

            val id = DocumentoRepository.registrar(
                cedula = datos.cedula,
                tipo = "WORD",
                archivo = archivo,
                periodo = datos.periodo,
                generadoPor = call.usernameActual() ?: "desconocido"
            )

            call.response.header(
                HttpHeaders.ContentDisposition,
                "attachment; filename=\"${archivo.name}\""
            )
            call.response.header("X-Documento-Id", id.toString())
            call.respondBytes(
                docx,
                ContentType.parse("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
                HttpStatusCode.OK
            )
        }

        get("/constancia/lista/{cedula}") {
            val cedula = call.parameters["cedula"]
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val rol = call.rolActual()
            if (rol == Rol.EMPLEADO.name && call.cedulaActual() != cedula) {
                return@get call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Sin permisos"))
            }

            val lista = DocumentoRepository.listarPorCedula(cedula)
            call.respond(lista)
        }

        get("/constancia/descargar/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val documento = DocumentoRepository.obtenerPorId(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Documento no encontrado"))

            val rol = call.rolActual()
            if (rol == Rol.EMPLEADO.name && call.cedulaActual() != documento.cedula) {
                return@get call.respond(HttpStatusCode.Forbidden)
            }

            val bytes = FileStorageService.leerDocumento(documento.rutaArchivo)
                ?: return@get call.respond(HttpStatusCode.Gone, mapOf("error" to "Archivo ya no existe en disco"))

            val contentType = if (documento.tipo == "PDF")
                ContentType.Application.Pdf
            else
                ContentType.parse("application/vnd.openxmlformats-officedocument.wordprocessingml.document")

            call.response.header(
                HttpHeaders.ContentDisposition,
                "attachment; filename=\"${documento.nombreArchivo}\""
            )
            call.respondBytes(bytes, contentType, HttpStatusCode.OK)
        }

        delete("/constancia/{id}") {
            val rol = call.rolActual()
            if (rol !in listOf(Rol.SUPER_USUARIO.name, Rol.DIRECTOR.name)) {
                return@delete call.respond(HttpStatusCode.Forbidden)
            }

            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val doc = DocumentoRepository.obtenerPorId(id)
                ?: return@delete call.respond(HttpStatusCode.NotFound)

            FileStorageService.eliminarDocumento(doc.rutaArchivo)
            call.respond(mapOf("ok" to true))
        }
    }
}

private fun ApplicationCall.puedeGenerarConstancia(): Boolean {
    val rol = rolActual() ?: return true
    return rol in listOf(Rol.COORDINADOR.name, Rol.DIRECTOR.name, Rol.SUPER_USUARIO.name, Rol.EMPLEADO.name)
}
