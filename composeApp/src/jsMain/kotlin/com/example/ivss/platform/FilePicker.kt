package com.example.ivss.platform

import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement

actual class FilePicker {
    actual fun selectExcelFile(onFileSelected: (fileName: String) -> Unit) {
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = ".xlsx, .xls"
        input.style.display = "none"
        input.onchange = {
            val files = input.files
            if (files != null && files.length > 0) {
                val file = files.item(0)
                if (file != null) {
                    onFileSelected(file.name)
                }
            }
        }
        document.body?.appendChild(input)
        input.click()
        document.body?.removeChild(input)
    }
}
