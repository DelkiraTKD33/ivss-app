package com.example.ivss.platform

expect object NativePdfGenerator {
    fun generateForma1216Pdf(
        userName: String,
        userNationalId: String,
        employerName: String,
        vacationName: String,
        usedDays: Int
    ): ByteArray
}
