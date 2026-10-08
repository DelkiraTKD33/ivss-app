package com.example.ivss.server.services

import com.example.ivss.server.db.EmpleadosTable
import com.example.ivss.server.db.UsuariosTable
import com.example.ivss.server.security.PasswordUtil
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.InputStream
import java.time.LocalDate

object ExcelImportService {

    /**
     * Importa la base de datos desde un archivo Excel (.xlsx / .xls),
     * registra la nómina en EmpleadosTable y CREA AUTOMÁTICAMENTE
     * las cuentas de usuario en UsuariosTable con el usuario y la contraseña
     * configurados como la CÉDULA del trabajador.
     */
    fun importar(input: InputStream): Int {
        var insertados = 0
        WorkbookFactory.create(input).use { wb ->
            val sheet = wb.getSheetAt(0)
            if (sheet == null || sheet.lastRowNum < 1) return 0

            val dataRow = sheet.getRow(1) ?: sheet.getRow(0)

            val columnas = mutableMapOf<Int, String>()
            val maxCol = dataRow.lastCellNum.toInt().coerceAtLeast(0)
            for (c in 0 until maxCol) {
                val cell = dataRow.getCell(c) ?: continue
                val nombre = cell.toString().trim()
                if (nombre.isNotEmpty()) columnas[c] = nombre
            }

            fun idx(nombre: String) = columnas.entries.firstOrNull { it.value.equals(nombre, ignoreCase = true) }?.key

            val iCodigoUbi = idx("codigoUbi") ?: 0
            val iServicio = idx("SERVICIO") ?: 1
            val iDescUbi = idx("descripcionUbi") ?: 2
            val iCodCargo = idx("codigo_cargo") ?: 3
            val iNomCargo = idx("nombre_cargo") ?: 4
            val iApellido = idx("apellido1") ?: 5
            val iNombre = idx("nombre1") ?: 6
            val iCedula = idx("cedula") ?: 7
            val iEstado = idx("ESTADO") ?: 8
            val iCentro = idx("CENTRO") ?: 9
            val iTipoEmp = idx("tipo_emp") ?: 10
            val iDescTE = idx("descripcionTE") ?: 11
            val iDia = idx("DIA") ?: 14
            val iMes = idx("MES") ?: 15
            val iAnio = idx("AÑO") ?: 16
            val iCargoTab = idx("cargo_tabulador") ?: 17
            val iGrado = idx("grado") ?: 18
            val iPaso = idx("paso") ?: 19
            val iTurno = idx("turno") ?: 20

            val conceptosCols = columnas.filter { it.value.startsWith("C") && it.value.length in 4..5 }

            transaction {
                val startRow = if (sheet.lastRowNum >= 2) 2 else 1
                for (r in startRow..sheet.lastRowNum) {
                    val row = sheet.getRow(r) ?: continue
                    val cedulaCell = row.getCell(iCedula) ?: continue

                    val rawCedulaString = when (cedulaCell.cellType) {
                        CellType.NUMERIC -> cedulaCell.numericCellValue.toLong().toString()
                        CellType.STRING -> cedulaCell.stringCellValue.trim()
                        else -> cedulaCell.toString().trim()
                    }

                    val cleanCedula = rawCedulaString.replace("V-", "").replace("v-", "")
                        .replace(".", "").replace("-", "").replace("E7", "").replace("E8", "").trim()
                    if (cleanCedula.isBlank() || !cleanCedula.all { it.isDigit() }) continue

                    val formattedCedula = try {
                        val num = cleanCedula.toLong()
                        val withDots = String.format("%,d", num).replace(',', '.')
                        "V-$withDots"
                    } catch (e: Exception) {
                        "V-$cleanCedula"
                    }

                    val nombre1Val = row.getCell(iNombre)?.toString()?.trim() ?: ""
                    val apellido1Val = row.getCell(iApellido)?.toString()?.trim() ?: ""
                    val nombreCompletoVal = "$nombre1Val $apellido1Val".trim().ifBlank { "TRABAJADOR IVSS" }

                    val conceptosMap = mutableMapOf<String, Double>()
                    conceptosCols.forEach { (colIdx, nombreCol) ->
                        val v = row.getCell(colIdx)?.let { cell ->
                            when (cell.cellType) {
                                CellType.NUMERIC -> cell.numericCellValue
                                CellType.STRING -> cell.stringCellValue.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                        } ?: 0.0
                        if (v != 0.0) conceptosMap[nombreCol] = v
                    }

                    val diaVal = row.getCell(iDia)?.let {
                        if (it.cellType == CellType.NUMERIC) it.numericCellValue.toInt() else it.toString().trim().toIntOrNull()
                    } ?: 1
                    val mesVal = row.getCell(iMes)?.let {
                        if (it.cellType == CellType.NUMERIC) it.numericCellValue.toInt() else it.toString().trim().toIntOrNull()
                    } ?: 11
                    val anioVal = row.getCell(iAnio)?.let {
                        if (it.cellType == CellType.NUMERIC) it.numericCellValue.toInt() else it.toString().trim().toIntOrNull()
                    } ?: 2019

                    val fechaIngresoVal = try {
                        LocalDate.of(anioVal, mesVal, diaVal)
                    } catch (e: Exception) {
                        LocalDate.of(2019, 11, 1)
                    }

                    try {
                        // 1. Guardar en la tabla de Empleados
                        EmpleadosTable.insert {
                            it[codigoUbi] = row.getCell(iCodigoUbi)?.toString() ?: ""
                            it[servicio] = row.getCell(iServicio)?.toString() ?: ""
                            it[descripcionUbi] = row.getCell(iDescUbi)?.toString() ?: ""
                            it[codigoCargo] = row.getCell(iCodCargo)?.toString() ?: ""
                            it[nombreCargo] = row.getCell(iNomCargo)?.toString() ?: ""
                            it[apellido1] = apellido1Val
                            it[nombre1] = nombre1Val
                            it[EmpleadosTable.cedula] = formattedCedula
                            it[estado] = row.getCell(iEstado)?.toString() ?: ""
                            it[centro] = row.getCell(iCentro)?.toString() ?: ""
                            it[tipoEmp] = row.getCell(iTipoEmp)?.toString() ?: ""
                            it[descripcionTE] = row.getCell(iDescTE)?.toString() ?: ""
                            it[EmpleadosTable.fechaIngreso] = fechaIngresoVal
                            it[dia] = diaVal
                            it[mes] = mesVal
                            it[anio] = anioVal
                            it[cargoTabulador] = row.getCell(iCargoTab)?.toString()
                            it[grado] = row.getCell(iGrado)?.toString()
                            it[paso] = row.getCell(iPaso)?.toString()
                            it[turno] = row.getCell(iTurno)?.toString()
                            it[conceptos] = Json.encodeToString(
                                MapSerializer(String.serializer(), Double.serializer()),
                                conceptosMap
                            )
                        }

                        // 2. CREACIÓN AUTOMÁTICA DEL USUARIO (Usuario = Cédula | Contraseña = Cédula)
                        val userExists = UsuariosTable.selectAll().where { UsuariosTable.username eq formattedCedula }.count() > 0
                        if (!userExists) {
                            val passwordEncrypted = PasswordUtil.hash(cleanCedula)
                            UsuariosTable.insert {
                                it[username] = formattedCedula
                                it[passwordHash] = passwordEncrypted
                                it[nombreCompleto] = nombreCompletoVal
                                it[email] = "$cleanCedula@ivss.gob.ve"
                                it[rol] = "EMPLEADO"
                                it[UsuariosTable.cedula] = formattedCedula
                                it[servicio] = row.getCell(iServicio)?.toString() ?: ""
                                it[activo] = true
                            }
                        }

                        insertados++
                    } catch (e: Exception) {
                        println("Aviso importando cédula $formattedCedula: ${e.message}")
                    }
                }
            }
        }
        return insertados
    }
}
