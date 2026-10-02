package com.example.ivss.platform

actual class FileSaver {
    actual suspend fun guardar(nombreArchivo: String, bytes: ByteArray, mimeType: String): String {
        return nombreArchivo
    }
}
