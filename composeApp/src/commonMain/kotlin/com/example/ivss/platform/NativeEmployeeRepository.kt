package com.example.ivss.platform

import com.example.ivss.ui.home.ui.ActiveEmployeeItem

expect object NativeEmployeeRepository {
    fun readAllEmployees(): List<ActiveEmployeeItem>
}
