package com.example.ivss.server.db

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

object EmpleadosTable : IntIdTable("empleados") {
    val codigoUbi = varchar("codigo_ubi", 20).default("")
    val servicio = varchar("servicio", 10).default("")
    val descripcionUbi = varchar("descripcion_ubi", 250).default("")
    val codigoCargo = varchar("codigo_cargo", 20).default("")
    val nombreCargo = varchar("nombre_cargo", 200).default("")
    val apellido1 = varchar("apellido1", 100).default("")
    val nombre1 = varchar("nombre1", 100).default("")
    val cedula = varchar("cedula", 20).uniqueIndex()
    val estado = varchar("estado", 50).default("")
    val centro = varchar("centro", 50).default("")
    val tipoEmp = varchar("tipo_emp", 10).default("")
    val descripcionTE = varchar("descripcion_te", 100).default("")
    val fechaIngreso = date("fecha_ingreso")
    val dia = integer("dia").nullable()
    val mes = integer("mes").nullable()
    val anio = integer("anio").nullable()
    val cargoTabulador = varchar("cargo_tabulador", 20).nullable()
    val grado = varchar("grado", 20).nullable()
    val paso = varchar("paso", 20).nullable()
    val turno = varchar("turno", 20).nullable()
    val conceptos = text("conceptos").default("{}")
}
