package com.example.ivss.platform

import kotlinx.browser.document
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

actual class FileSaver {
    actual suspend fun guardar(nombreArchivo: String, bytes: ByteArray, mimeType: String): String {
        val blob = Blob(arrayOf(bytes), BlobPropertyBag(type = mimeType))
        val url = URL.createObjectURL(blob)
        val anchor = document.createElement("a") as HTMLAnchorElement
        anchor.href = url
        anchor.download = nombreArchivo
        anchor.click()
        URL.revokeObjectURL(url)
        return nombreArchivo
    }
}
