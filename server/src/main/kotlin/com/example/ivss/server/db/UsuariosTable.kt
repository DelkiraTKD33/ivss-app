package com.example.ivss.server.db

import org.jetbrains.exposed.dao.id.IntIdTable

object UsuariosTable : IntIdTable("usuarios") {
    val username = varchar("username", 50).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val nombreCompleto = varchar("nombre_completo", 150)
    val email = varchar("email", 150).nullable()
    val rol = varchar("rol", 30).index()
    val cedula = varchar("cedula", 20).nullable().index()
    val servicio = varchar("servicio", 10).nullable()
    val activo = bool("activo").default(true)
}
