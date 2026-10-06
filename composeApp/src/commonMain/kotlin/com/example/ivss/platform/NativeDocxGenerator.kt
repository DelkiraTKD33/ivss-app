package com.example.ivss.platform

expect object NativeDocxGenerator {
    fun generateForma1216Docx(
        userName: String,
        userNationalId: String,
        employerName: String,
        vacationName: String,
        usedDays: Int
    ): ByteArray
}
