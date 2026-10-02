package com.example.ivss.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.*

actual class FileSaver {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun guardar(nombreArchivo: String, bytes: ByteArray, mimeType: String): String {
        val documents = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory, NSUserDomainMask, true
        ).first() as String

        val ruta = "$documents/$nombreArchivo"
        val data = bytes.toNSData()
        data.writeToFile(ruta, atomically = true)
        return ruta
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
}
