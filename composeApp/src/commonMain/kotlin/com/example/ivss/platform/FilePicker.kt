package com.example.ivss.platform

expect class FilePicker() {
    /**
     * Abre el explorador de archivos nativo del dispositivo para seleccionar un archivo Excel (.xlsx, .xls)
     */
    fun selectExcelFile(onFileSelected: (fileName: String) -> Unit)
}
