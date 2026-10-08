package com.example.ivss.platform

import com.example.ivss.domain.model.UserProfile
import com.example.ivss.ui.home.ui.ActiveEmployeeItem

expect object NativeEmployeeRepository {
    fun readAllEmployees(): List<ActiveEmployeeItem>
    fun findEmployeeByCedula(cedulaInput: String): UserProfile?
}
