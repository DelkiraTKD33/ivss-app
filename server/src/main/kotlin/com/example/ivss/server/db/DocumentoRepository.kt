package com.example.ivss.server.db

import com.example.ivss.server.model.Documento
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.time.LocalDateTime

object DocumentoRepository {

    fun registrar(
        cedula: String,
        tipo: String,
        archivo: File,
        periodo: String?,
        generadoPor: String
    ): Int {
        return transaction {
            DocumentosTable.insert {
                it[DocumentosTable.cedula] = cedula
                it[DocumentosTable.tipo] = tipo
                it[nombreArchivo] = archivo.name
                it[rutaArchivo] = archivo.absolutePath
                it[DocumentosTable.periodo] = periodo
                it[DocumentosTable.generadoPor] = generadoPor
                it[fechaGeneracion] = LocalDateTime.now().toString()
                it[tamanio] = archivo.length()
            } get DocumentosTable.id
        }.value
    }

    fun listarPorCedula(cedula: String): List<Documento> = transaction {
        DocumentosTable.selectAll()
            .where { DocumentosTable.cedula eq cedula }
            .orderBy(DocumentosTable.id, SortOrder.DESC)
            .map {
                Documento(
                    id = it[DocumentosTable.id].value,
                    cedula = it[DocumentosTable.cedula],
                    tipo = it[DocumentosTable.tipo],
                    nombreArchivo = it[DocumentosTable.nombreArchivo],
                    rutaArchivo = it[DocumentosTable.rutaArchivo],
                    periodo = it[DocumentosTable.periodo],
                    generadoPor = it[DocumentosTable.generadoPor],
                    fechaGeneracion = it[DocumentosTable.fechaGeneracion],
                    tamanio = it[DocumentosTable.tamanio]
                )
            }
    }

    fun obtenerPorId(id: Int): Documento? = transaction {
        DocumentosTable.selectAll()
            .where { DocumentosTable.id eq id }
            .map {
                Documento(
                    id = it[DocumentosTable.id].value,
                    cedula = it[DocumentosTable.cedula],
                    tipo = it[DocumentosTable.tipo],
                    nombreArchivo = it[DocumentosTable.nombreArchivo],
                    rutaArchivo = it[DocumentosTable.rutaArchivo],
                    periodo = it[DocumentosTable.periodo],
                    generadoPor = it[DocumentosTable.generadoPor],
                    fechaGeneracion = it[DocumentosTable.fechaGeneracion],
                    tamanio = it[DocumentosTable.tamanio]
                )
            }.firstOrNull()
    }
}
