package com.example.ivss.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class ArchivoDescargado(
    val nombre: String,
    val bytes: ByteArray,
    val mimeType: String
)

@Serializable
data class DocumentoResumen(
    val id: Int? = null,
    val cedula: String,
    val tipo: String,
    val nombreArchivo: String,
    val rutaArchivo: String,
    val periodo: String? = null,
    val generadoPor: String,
    val fechaGeneracion: String,
    val tamanio: Long
)

class DocumentoApi(
    private val client: HttpClient,
    private val baseUrl: String = "http://10.0.2.2:8080/api"
) {

    /**
     * Solicita y descarga el archivo PDF o Word desde el servidor Ktor.
     */
    suspend fun descargarVacacion(id: Int, isPdf: Boolean = true): ArchivoDescargado {
        val endpoint = if (isPdf) "$baseUrl/vacations/$id/pdf" else "$baseUrl/vacations/$id/docx"
        val response: HttpResponse = client.get(endpoint)
        if (!response.status.isSuccess()) {
            error("Error ${response.status.value}: ${response.bodyAsText()}")
        }
        val bytes = response.body<ByteArray>()
        val defaultName = if (isPdf) "Solicitud_Vacaciones.pdf" else "Solicitud_Vacaciones.docx"
        val mime = if (isPdf) "application/pdf" else "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        val nombre = extraerNombreArchivo(response, defaultName)
        return ArchivoDescargado(nombre, bytes, mime)
    }

    private fun extraerNombreArchivo(response: HttpResponse, fallback: String): String {
        val disposition = response.headers[HttpHeaders.ContentDisposition] ?: return fallback
        val regex = Regex("filename=\"?([^\"]+)\"?")
        return regex.find(disposition)?.groupValues?.get(1) ?: fallback
    }
}
