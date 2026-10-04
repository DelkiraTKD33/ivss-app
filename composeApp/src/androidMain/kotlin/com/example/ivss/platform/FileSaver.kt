package com.example.ivss.platform

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File

actual class FileSaver {
    actual suspend fun guardar(nombreArchivo: String, bytes: ByteArray, mimeType: String): String {
        val context = AppContextHolder.context

        val resultPath: String = if (context != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ivss")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("No se pudo crear el archivo mediante MediaStore")

            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(bytes)
                stream.flush()
            } ?: error("No se pudo abrir el stream de salida")

            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)

            "Descargas/ivss/$nombreArchivo"
        } else {
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "ivss"
            )
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, nombreArchivo)
            file.outputStream().use { stream ->
                stream.write(bytes)
                stream.flush()
            }
            file.absolutePath
        }

        // Mostrar notificación en la barra de estado del sistema Android mediante DownloadManager
        if (context != null) {
            try {
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                val fileInDownloads = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "ivss/$nombreArchivo"
                )
                if (downloadManager != null && fileInDownloads.exists()) {
                    downloadManager.addCompletedDownload(
                        nombreArchivo,
                        "Autorización de Vacaciones Forma 12-16 - IVSS",
                        true,
                        mimeType,
                        fileInDownloads.absolutePath,
                        bytes.size.toLong(),
                        true
                    )
                }
            } catch (e: Exception) {
                // Notificación de respaldo
            }
        }

        return resultPath
    }
}

object AppContextHolder {
    var context: android.content.Context? = null
        set(value) {
            field = value?.applicationContext
        }
}
