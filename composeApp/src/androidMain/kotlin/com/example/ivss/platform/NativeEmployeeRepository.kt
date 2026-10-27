package com.example.ivss.platform

import com.example.ivss.domain.model.UserProfile
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
                            val fullUbi = row.getCell(2)?.toString()?.trim() ?: ""
                            val servicioVal = if (fullUbi.contains("-")) fullUbi.substringAfterLast("-").trim() else fullUbi

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

    actual fun findEmployeeByCedula(cedulaInput: String): UserProfile? {
        val cleanCedulaNum = cedulaInput.replace("V-", "").replace("v-", "")
            .replace(".", "").replace("-", "").replace("E7", "").replace("E8", "").trim()
        if (cleanCedulaNum.isBlank()) return null

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

                            val rowCleanCedula = rawCedulaString.replace("V-", "").replace("v-", "")
                                .replace(".", "").replace("-", "").replace("E7", "").replace("E8", "").trim()

                            if (rowCleanCedula == cleanCedulaNum) {
                                val apellido1 = row.getCell(5)?.toString()?.trim() ?: ""
                                val nombre1 = row.getCell(6)?.toString()?.trim() ?: ""
                                val cargoVal = row.getCell(4)?.toString()?.trim() ?: "CONTRATADO"
                                val fullUbi = row.getCell(2)?.toString()?.trim() ?: ""
                                val extractedServicio = when {
                                    fullUbi.contains("-") -> fullUbi.substringAfterLast("-").trim()
                                    fullUbi.startsWith("HOSPITAL", ignoreCase = true) -> ""
                                    else -> fullUbi
                                }

                                val diaVal = row.getCell(14)?.let {
                                    if (it.cellType == CellType.NUMERIC) it.numericCellValue.toInt() else it.toString().trim().toIntOrNull()
                                } ?: 1
                                val mesVal = row.getCell(15)?.let {
                                    if (it.cellType == CellType.NUMERIC) it.numericCellValue.toInt() else it.toString().trim().toIntOrNull()
                                } ?: 11
                                val anioVal = row.getCell(16)?.let {
                                    if (it.cellType == CellType.NUMERIC) it.numericCellValue.toInt() else it.toString().trim().toIntOrNull()
                                } ?: 2019

                                val formattedCedula = try {
                                    val num = cleanCedulaNum.toLong()
                                    val withDots = String.format("%,d", num).replace(',', '.')
                                    "V-$withDots"
                                } catch (e: Exception) {
                                    "V-$cleanCedulaNum"
                                }

                                val fullNombre = "$nombre1 $apellido1".trim().ifBlank { "TRABAJADOR IVSS" }

                                inputStream.close()
                                return UserProfile(
                                    id = "USR-$cleanCedulaNum",
                                    fullName = fullNombre,
                                    nationalId = formattedCedula,
                                    email = "",
                                    phone = "",
                                    birthDate = "",
                                    affiliationNumber = "100234891",
                                    status = "Cotizante Activo",
                                    servicio = extractedServicio,
                                    cargo = cargoVal,
                                    numeroCargo = "CONTRATADO",
                                    employer = "HOSPITAL GENERAL MUNICIPAL IVSS SAN JUAN DE LOS MORROS.",
                                    fechaIngreso = String.format("%02d/%02d/%d", diaVal, mesVal, anioVal),
                                    weeksContributed = 850
                                )
                            }
                        }
                    }
                }
                inputStream.close()
            } catch (e: Exception) {
                // Fallback
            }
        }
        return null
    }

    actual fun readAllVacationsForAdmin(): List<AdminVacationItem> {
        val list = mutableListOf<AdminVacationItem>()
        val context = AppContextHolder.context
        val externalFile = File("C:\\Users\\Delkira\\Desktop\\VACACIONES RRHH\\RELACION VACACIONES RRHH ACTUAL FEBRERO 2025.xlsx")

        val inputStream: InputStream? = when {
            context != null -> {
                try {
                    context.assets.open("vacaciones_rrhh.xlsx")
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
                    val sheetVac = if (wb.numberOfSheets > 1) wb.getSheetAt(1) else wb.getSheetAt(0)
                    if (sheetVac != null && sheetVac.lastRowNum >= 1) {
                        var idCounter = 1
                        for (r in 1..sheetVac.lastRowNum) {
                            val row = sheetVac.getRow(r) ?: continue
                            val cedCell = row.getCell(3) ?: continue

                            val rawCedStr = when (cedCell.cellType) {
                                CellType.NUMERIC -> cedCell.numericCellValue.toLong().toString()
                                CellType.STRING -> cedCell.stringCellValue.trim()
                                else -> cedCell.toString().trim()
                            }

                            val cleanCed = rawCedStr.replace("V-", "").replace(".", "").replace("-", "").trim()
                            if (cleanCed.isBlank() || !cleanCed.all { it.isDigit() }) continue

                            val formattedCed = try {
                                val num = cleanCed.toLong()
                                "V-" + String.format("%,d", num).replace(',', '.')
                            } catch (e: Exception) {
                                "V-$cleanCed"
                            }

                            val apellido = row.getCell(1)?.toString()?.trim() ?: ""
                            val nombre = row.getCell(2)?.toString()?.trim() ?: ""
                            val fullNombre = "$nombre $apellido".trim().ifBlank { "TRABAJADOR IVSS" }

                            val per1 = row.getCell(8)?.toString()?.trim() ?: ""
                            val fechas1 = row.getCell(9)?.toString()?.trim() ?: ""

                            val per2 = row.getCell(10)?.toString()?.trim() ?: ""
                            val fechas2 = row.getCell(11)?.toString()?.trim() ?: ""

                            if (per1.isNotBlank()) {
                                val isApproved = fechas1.isNotBlank()
                                list.add(
                                    AdminVacationItem(
                                        id = idCounter++,
                                        workerName = fullNombre,
                                        workerCedula = formattedCed,
                                        periodName = "Vacaciones Período $per1",
                                        status = if (isApproved) "Aprobada" else "En espera",
                                        usedDays = if (isApproved) 15 else 0,
                                        totalDays = 15,
                                        fileName = "Forma_12-16_Vacaciones_${per1.replace(" ", "_")}.docx"
                                    )
                                )
                            }

                            if (per2.isNotBlank()) {
                                val isApproved = fechas2.isNotBlank()
                                list.add(
                                    AdminVacationItem(
                                        id = idCounter++,
                                        workerName = fullNombre,
                                        workerCedula = formattedCed,
                                        periodName = "Vacaciones Período $per2",
                                        status = if (isApproved) "Aprobada" else "En espera",
                                        usedDays = if (isApproved) 15 else 0,
                                        totalDays = 15,
                                        fileName = "Forma_12-16_Vacaciones_${per2.replace(" ", "_")}.docx"
                                    )
                                )
                            }

                            // Período pendiente por defecto
                            list.add(
                                AdminVacationItem(
                                    id = idCounter++,
                                    workerName = fullNombre,
                                    workerCedula = formattedCed,
                                    periodName = "Vacaciones Período 2024 - 2025",
                                    status = "En espera",
                                    usedDays = 0,
                                    totalDays = 15,
                                    fileName = "Forma_12-16_Vacaciones_2024_2025.docx"
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
            AdminVacationItem(1, "FRANKLIN GARCIA", "V-4.688.627", "Vacaciones Período 2023 - 2024", "Aprobada", 15, 15, "Forma_12-16_Franklin_Garcia.docx"),
            AdminVacationItem(2, "JENNIFFER HERNANDEZ RON", "V-17.062.973", "Vacaciones Período 2024 - 2025", "En espera", 0, 15, "Forma_12-16_Jenniffer_Hernandez.docx"),
            AdminVacationItem(3, "WILLIANS MARTINEZ", "V-18.366.056", "Vacaciones Período 2020 - 2021", "Aprobada", 15, 15, "Forma_12-16_Willians_Martinez.docx"),
            AdminVacationItem(4, "SANTIAGO MORGADO", "V-28.176.135", "Vacaciones Período 2024 - 2025", "En espera", 0, 15, "Forma_12-16_Santiago_Morgado.docx"),
            AdminVacationItem(5, "ZAIDA PAZ", "V-9.446.776", "Vacaciones Período 2023 - 2024", "En espera", 0, 15, "Forma_12-16_Zaida_Paz.docx"),
            AdminVacationItem(6, "DIANA CARDOZO", "V-17.353.576", "Vacaciones Período 2022 - 2023", "Aprobada", 15, 15, "Forma_12-16_Diana_Cardozo.docx")
        )
    }
}
