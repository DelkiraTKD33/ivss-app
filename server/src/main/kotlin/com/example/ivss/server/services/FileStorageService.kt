package com.example.ivss.server.services

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object FileStorageService {

    private val BASE_DIR = File(System.getProperty("user.home"), "ivss-documentos")
        .also { if (!it.exists()) it.mkdirs() }

    private val TIMESTAMP_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    fun guardarDocumento(
        cedula: String,
        tipo: String,
        bytes: ByteArray,
        periodo: String? = null
    ): File {
        val carpetaCedula = File(BASE_DIR, cedula.replace("-", "").trim())
        if (!carpetaCedula.exists()) carpetaCedula.mkdirs()

        val extension = if (tipo.uppercase() == "PDF") "pdf" else "docx"
        val timestamp = LocalDateTime.now().format(TIMESTAMP_FMT)
        val sufijoPeriodo = if (periodo.isNullOrBlank()) "" else "_${periodo.replace("/", "-")}"
        val nombre = "constancia_${cedula.replace("-", "").trim()}_${timestamp}$sufijoPeriodo.$extension"

        val archivo = File(carpetaCedula, nombre)
        archivo.writeBytes(bytes)

        println("Documento guardado en almacenamiento: ${archivo.absolutePath}")
        return archivo
    }

    fun leerDocumento(ruta: String): ByteArray? {
        val archivo = File(ruta)
        return if (archivo.exists()) archivo.readBytes() else null
    }

    fun eliminarDocumento(ruta: String): Boolean {
        val archivo = File(ruta)
        return if (archivo.exists()) archivo.delete() else false
    }

    fun getBaseDir(): File = BASE_DIR
}
