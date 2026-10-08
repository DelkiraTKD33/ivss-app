package com.example.ivss.server.plugins

import com.example.ivss.server.db.UsuariosTable
import com.example.ivss.server.model.Rol
import com.example.ivss.server.security.PasswordUtil
import com.example.ivss.server.services.ExcelImportService
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

fun Application.seedSuperUsuario() {
    // 1. Inicializar Super Usuario admin / admin
    transaction {
        val existe = UsuariosTable.selectAll()
            .where { UsuariosTable.username eq "admin" }
            .any()
        if (!existe) {
            UsuariosTable.insert {
                it[username] = "admin"
                it[passwordHash] = PasswordUtil.hash("admin")
                it[nombreCompleto] = "Super Usuario Administrador"
                it[email] = "admin@ivss.gob.ve"
                it[rol] = Rol.SUPER_USUARIO.name
                it[activo] = true
            }
            println("Super Usuario predeterminado inicializado: admin / admin")
        }
    }

    // 2. Importar automáticamente la base de datos de trabajadores activos (nomina_activos.xls)
    try {
        val externalFile = File("C:\\Users\\Delkira\\Downloads\\ivss db\\60207382-60209382..xls")
        val inputStream: InputStream? = when {
            externalFile.exists() -> FileInputStream(externalFile)
            else -> Application::class.java.getResourceAsStream("/data/nomina_activos.xls")
        }

        if (inputStream != null) {
            val totalImportados = ExcelImportService.importar(inputStream)
            println("✅ Base de Datos de Trabajadores Activos importada exitosamente. Total procesados: $totalImportados")
            inputStream.close()
        }
    } catch (e: Exception) {
        println("Aviso importando nómina automática: ${e.message}")
    }
}
