package com.example.ivss.server.db

import org.jetbrains.exposed.dao.id.IntIdTable

object VacacionesTable : IntIdTable("vacaciones_registradas") {
    val cedula = varchar("cedula", 20).index()
    val nombrePeriodo = varchar("nombre_periodo", 100)
    val anioInicio = integer("anio_inicio")
    val anioFin = integer("anio_fin")
    val estado = varchar("estado", 30) // "Aprobada", "En espera", "Pendiente"
    val diasUsados = integer("dias_usados").default(0)
    val diasTotales = integer("dias_totales").default(15)
    val fechaDesde = varchar("fecha_desde", 30).nullable()
    val fechaHasta = varchar("fecha_hasta", 30).nullable()
    val tipoDocumento = varchar("tipo_documento", 10).default("WORD")
    val nombreArchivo = varchar("nombre_archivo", 255)
}
