package com.example.ivss.server.plugins

import com.example.ivss.server.db.UsuariosTable
import com.example.ivss.server.model.Rol
import com.example.ivss.server.security.PasswordUtil
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.seedSuperUsuario() {
    transaction {
        val existe = UsuariosTable.selectAll()
            .where { UsuariosTable.rol eq Rol.SUPER_USUARIO.name }
            .any()
        if (!existe) {
            UsuariosTable.insert {
                it[username] = "admin"
                it[passwordHash] = PasswordUtil.hash("admin123")
                it[nombreCompleto] = "Administrador del Sistema"
                it[email] = "admin@ivss.gob.ve"
                it[rol] = Rol.SUPER_USUARIO.name
                it[activo] = true
            }
            println("Super usuario predeterminado inicializado: admin / admin123")
        }
    }
}
