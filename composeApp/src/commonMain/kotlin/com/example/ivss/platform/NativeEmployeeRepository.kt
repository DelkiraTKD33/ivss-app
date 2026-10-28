package com.example.ivss.platform

import com.example.ivss.domain.model.UserProfile
import com.example.ivss.ui.home.ui.ActiveEmployeeItem

data class AdminVacationItem(
    val id: Int,
    val workerName: String,
    val workerCedula: String,
    val periodName: String,
    val status: String,
    val fechaReintegro: String = "Pendiente",
    val usedDays: Int,
    val totalDays: Int,
    val fileName: String
)

expect object NativeEmployeeRepository {
    fun readAllEmployees(): List<ActiveEmployeeItem>
    fun findEmployeeByCedula(cedulaInput: String): UserProfile?
    fun readAllVacationsForAdmin(): List<AdminVacationItem>
}
