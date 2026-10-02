package com.example.ivss.server.db

import org.jetbrains.exposed.dao.id.IntIdTable

object DocumentosTable : IntIdTable("documentos") {
    val cedula = varchar("cedula", 20).index()
    val tipo = varchar("tipo", 10)
    val nombreArchivo = varchar("nombre_archivo", 255)
    val rutaArchivo = varchar("ruta_archivo", 500)
    val periodo = varchar("periodo", 20).nullable()
    val generadoPor = varchar("generado_por", 100)
    val fechaGeneracion = varchar("fecha_generacion", 30)
    val tamanio = long("tamanio")
}
