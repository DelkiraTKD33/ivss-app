package com.example.ivss.platform

actual object NativeDocxGenerator {
    actual fun generateForma1216Docx(
        userName: String,
        userNationalId: String,
        employerName: String,
        vacationName: String,
        usedDays: Int
    ): ByteArray {
        val text = "INSTITUTO VENEZOLANO DE LOS SEGUROS SOCIALES\nFORMA 12-16: AUTORIZACIÓN DE VACACIONES\nTRABAJADOR: $userName\nCÉDULA: $userNationalId\nDE: $employerName\nSOLICITUD: $vacationName ($usedDays DÍAS)"
        return text.encodeToByteArray()
    }
}
