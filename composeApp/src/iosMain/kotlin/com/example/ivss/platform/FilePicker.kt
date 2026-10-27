package com.example.ivss.platform

actual class FilePicker {
    actual fun selectExcelFile(onFileSelected: (fileName: String) -> Unit) {
        onFileSelected("Relacion_Vacaciones_Nomina_IVSS.xlsx")
    }
}
