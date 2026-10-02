package com.example.ivss.platform

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File

actual class FileSaver {
    actual suspend fun guardar(nombreArchivo: String, bytes: ByteArray, mimeType: String): String {
        val context = AppContextHolder.context

        return if (context != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ivss")
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("No se pudo crear el archivo mediante MediaStore")
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
            uri.toString()
        } else {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ivss")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, nombreArchivo)
            file.writeBytes(bytes)
            file.absolutePath
        }
    }
}

object AppContextHolder {
    var context: android.content.Context? = null
        set(value) {
            field = value?.applicationContext
        }
}
