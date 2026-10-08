package com.example.ivss.platform

import com.example.ivss.ui.home.ui.ActiveEmployeeItem
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

actual object NativeEmployeeRepository {
    actual fun readAllEmployees(): List<ActiveEmployeeItem> {
        val list = mutableListOf<ActiveEmployeeItem>()
        val context = AppContextHolder.context
        val externalFile = File("C:\\Users\\Delkira\\Downloads\\ivss db\\60207382-60209382..xls")

        val inputStream: InputStream? = when {
            context != null -> {
                try {
                    context.assets.open("nomina_activos.xls")
                } catch (e: Exception) {
                    if (externalFile.exists()) FileInputStream(externalFile) else null
                }
            }
            externalFile.exists() -> FileInputStream(externalFile)
            else -> null
        }

        if (inputStream != null) {
            try {
                WorkbookFactory.create(inputStream).use { wb ->
                    val sheet = wb.getSheetAt(0)
                    if (sheet != null && sheet.lastRowNum >= 1) {
                        for (r in 1..sheet.lastRowNum) {
                            val row = sheet.getRow(r) ?: continue
                            val cedulaCell = row.getCell(7) ?: continue

                            val rawCedulaString = when (cedulaCell.cellType) {
                                CellType.NUMERIC -> cedulaCell.numericCellValue.toLong().toString()
                                CellType.STRING -> cedulaCell.stringCellValue.trim()
                                else -> cedulaCell.toString().trim()
                            }

                            val cleanCedulaNum = rawCedulaString.replace("V-", "").replace("v-", "")
                                .replace(".", "").replace("-", "").replace("E7", "").replace("E8", "").trim()
                            if (cleanCedulaNum.isBlank() || !cleanCedulaNum.all { it.isDigit() }) continue

                            val formattedCedula = try {
                                val num = cleanCedulaNum.toLong()
                                val withDots = String.format("%,d", num).replace(',', '.')
                                "V-$withDots"
                            } catch (e: Exception) {
                                "V-$cleanCedulaNum"
                            }

                            val apellido1 = row.getCell(5)?.toString()?.trim() ?: ""
                            val nombre1 = row.getCell(6)?.toString()?.trim() ?: ""
                            val cargoVal = row.getCell(4)?.toString()?.trim() ?: "MÉDICO / TRABAJADOR IVSS"
                            val servicioVal = row.getCell(2)?.toString()?.trim() ?: row.getCell(1)?.toString()?.trim() ?: "GENERAL"

                            // Leer Fecha de Ingreso directamente desde Columnas 14 (Día), 15 (Mes), 16 (Año)
                            val diaVal = row.getCell(14)?.let {
                                if (it.cellType == CellType.NUMERIC) it.numericCellValue.toInt() else it.toString().trim().toIntOrNull()
                            } ?: 1
                            val mesVal = row.getCell(15)?.let {
                                if (it.cellType == CellType.NUMERIC) it.numericCellValue.toInt() else it.toString().trim().toIntOrNull()
                            } ?: 11
                            val anioVal = row.getCell(16)?.let {
                                if (it.cellType == CellType.NUMERIC) it.numericCellValue.toInt() else it.toString().trim().toIntOrNull()
                            } ?: 2019

                            val fechaIngresoVal = String.format("%02d/%02d/%d", diaVal, mesVal, anioVal)

                            list.add(
                                ActiveEmployeeItem(
                                    cedula = formattedCedula,
                                    nombre = nombre1.ifBlank { "TRABAJADOR" },
                                    apellido = apellido1.ifBlank { "IVSS" },
                                    cargo = cargoVal,
                                    fechaIngreso = fechaIngresoVal,
                                    servicio = servicioVal
                                )
                            )
                        }
                    }
                }
                inputStream.close()
            } catch (e: Exception) {
                // Fallback
            }
        }

        return if (list.isNotEmpty()) list else listOf(
            ActiveEmployeeItem("V-17.062.973", "JENNIFFER", "HERNANDEZ RON", "MEDICO ADJUNTO I", "01/11/2019", "CIRUGIA"),
            ActiveEmployeeItem("V-18.765.432", "JUAN CARLOS", "PÉREZ RODRÍGUEZ", "ANALISTA TÉCNICO I", "01/03/2018", "ADMINISTRACIÓN"),
            ActiveEmployeeItem("V-12.345.678", "WILLIAMS", "GONZALEZ", "SUPERVISOR INMEDIATO", "15/05/2015", "RECURSOS HUMANOS"),
            ActiveEmployeeItem("V-14.890.123", "MAYARI", "SOJO", "COORDINADOR DE RRHH", "10/08/2016", "RECURSOS HUMANOS"),
            ActiveEmployeeItem("V-11.222.333", "JULIO", "AQUINO", "MÁXIMA AUTORIDAD - DIRECTOR", "01/01/2010", "DIRECCIÓN GENERAL")
        )
    }
}
