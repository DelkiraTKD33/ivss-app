package com.example.ivss.platform

import kotlinx.browser.document
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.set
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

actual class FileSaver {
    actual suspend fun guardar(nombreArchivo: String, bytes: ByteArray, mimeType: String): String {
        val uint8Array = Uint8Array(bytes.size)
        bytes.forEachIndexed { index, byte ->
            uint8Array[index] = byte
        }
        val blob = Blob(arrayOf(uint8Array), BlobPropertyBag(type = mimeType))

        val url = URL.createObjectURL(blob)
        val anchor = document.createElement("a") as HTMLAnchorElement
        anchor.href = url
        anchor.download = nombreArchivo
        anchor.style.display = "none"
        document.body?.appendChild(anchor)
        anchor.click()
        document.body?.removeChild(anchor)
        URL.revokeObjectURL(url)

        return nombreArchivo
    }
}
