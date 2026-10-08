package com.example.ivss.platform

import com.example.ivss.ui.home.ui.ActiveEmployeeItem

actual object NativeEmployeeRepository {
    actual fun readAllEmployees(): List<ActiveEmployeeItem> {
        return listOf(
            ActiveEmployeeItem("V-17.062.973", "JENNIFFER", "HERNANDEZ RON", "MEDICO ADJUNTO I", "01/11/2019", "CIRUGIA"),
            ActiveEmployeeItem("V-18.765.432", "JUAN CARLOS", "PÉREZ RODRÍGUEZ", "ANALISTA TÉCNICO I", "01/03/2018", "ADMINISTRACIÓN"),
            ActiveEmployeeItem("V-12.345.678", "WILLIAMS", "GONZALEZ", "SUPERVISOR INMEDIATO", "15/05/2015", "RECURSOS HUMANOS"),
            ActiveEmployeeItem("V-14.890.123", "MAYARI", "SOJO", "COORDINADOR DE RRHH", "10/08/2016", "RECURSOS HUMANOS"),
            ActiveEmployeeItem("V-11.222.333", "JULIO", "AQUINO", "MÁXIMA AUTORIDAD - DIRECTOR", "01/01/2010", "DIRECCIÓN GENERAL")
        )
    }
}
