package com.example.ivss.platform

expect class FileSaver() {
    /**
     * Guarda bytes en el sistema de archivos del dispositivo y devuelve la ruta final.
     * - Android: Guarda en la carpeta Downloads/ivss del almacenamiento público.
     * - iOS: Guarda en el directorio Documents del contenedor de la app.
     */
    suspend fun guardar(nombreArchivo: String, bytes: ByteArray, mimeType: String): String
}
