package com.example.ivss.server.services

import com.example.ivss.server.db.EmpleadosTable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.InputStream
import java.time.LocalDate
import java.time.ZoneId

object ExcelImportService {

    fun importar(input: InputStream): Int {
        var insertados = 0
        WorkbookFactory.create(input).use { wb ->
            val sheet = wb.getSheetAt(0)
            if (sheet == null || sheet.lastRowNum < 1) return 0

            val dataRow = sheet.getRow(1) ?: sheet.getRow(0)

            // Construir mapa: índice -> nombre columna
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
            val iFechaIng = idx("fecha_ingreso") ?: 12
            val iDia = idx("DIA") ?: 13
            val iMes = idx("MES") ?: 14
            val iAnio = idx("AÑO") ?: 15
            val iCargoTab = idx("cargo_tabulador") ?: 16
            val iGrado = idx("grado") ?: 17
            val iPaso = idx("paso") ?: 18
            val iTurno = idx("turno") ?: 19

            val conceptosCols = columnas.filter { it.value.startsWith("C") && it.value.length in 4..5 }

            transaction {
                val startRow = if (sheet.lastRowNum >= 2) 2 else 1
                for (r in startRow..sheet.lastRowNum) {
                    val row = sheet.getRow(r) ?: continue
                    val cedulaCell = row.getCell(iCedula)
                    val cedula = cedulaCell?.toString()?.trim() ?: ""
                    if (cedula.isEmpty()) continue

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

                    val fechaIngresoVal = try {
                        val cell = row.getCell(iFechaIng)
                        when {
                            cell == null -> LocalDate.now()
                            cell.cellType == CellType.NUMERIC ->
                                cell.dateCellValue.toInstant()
                                    .atZone(ZoneId.systemDefault()).toLocalDate()
                            else -> LocalDate.parse(cell.toString().trim())
                        }
                    } catch (e: Exception) {
                        LocalDate.now()
                    }

                    try {
                        EmpleadosTable.insert {
                            it[codigoUbi] = row.getCell(iCodigoUbi)?.toString() ?: ""
                            it[servicio] = row.getCell(iServicio)?.toString() ?: ""
                            it[descripcionUbi] = row.getCell(iDescUbi)?.toString() ?: ""
                            it[codigoCargo] = row.getCell(iCodCargo)?.toString() ?: ""
                            it[nombreCargo] = row.getCell(iNomCargo)?.toString() ?: ""
                            it[apellido1] = row.getCell(iApellido)?.toString() ?: ""
                            it[nombre1] = row.getCell(iNombre)?.toString() ?: ""
                            it[EmpleadosTable.cedula] = cedula
                            it[estado] = row.getCell(iEstado)?.toString() ?: ""
                            it[centro] = row.getCell(iCentro)?.toString() ?: ""
                            it[tipoEmp] = row.getCell(iTipoEmp)?.toString() ?: ""
                            it[descripcionTE] = row.getCell(iDescTE)?.toString() ?: ""
                            it[EmpleadosTable.fechaIngreso] = fechaIngresoVal
                            it[dia] = row.getCell(iDia)?.numericCellValue?.toInt()
                            it[mes] = row.getCell(iMes)?.numericCellValue?.toInt()
                            it[anio] = row.getCell(iAnio)?.numericCellValue?.toInt()
                            it[cargoTabulador] = row.getCell(iCargoTab)?.toString()
                            it[grado] = row.getCell(iGrado)?.toString()
                            it[paso] = row.getCell(iPaso)?.toString()
                            it[turno] = row.getCell(iTurno)?.toString()
                            it[conceptos] = Json.encodeToString(
                                MapSerializer(String.serializer(), Double.serializer()),
                                conceptosMap
                            )
                        }
                        insertados++
                    } catch (e: Exception) {
                        println("Aviso importando cédula $cedula: ${e.message}")
                    }
                }
            }
        }
        return insertados
    }
}
