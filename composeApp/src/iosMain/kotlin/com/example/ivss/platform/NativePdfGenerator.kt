package com.example.ivss.platform

actual object NativePdfGenerator {
    actual fun generateForma1216Pdf(
        userName: String,
        userNationalId: String,
        employerName: String,
        vacationName: String,
        usedDays: Int
    ): ByteArray {
        val docText = """
            =================================================================
            INSTITUTO VENEZOLANO DE LOS SEGUROS SOCIALES
            FORMA 12-16: AUTORIZACIÓN DE VACACIONES
            =================================================================
            TRABAJADOR: $userName
            CÉDULA: $userNationalId
            DE: $employerName
            SOLICITUD: $vacationName ($usedDays DÍAS HÁBILES)
            =================================================================
        """.trimIndent()
        return docText.encodeToByteArray()
    }
}
