package com.example.ivss.server

import com.example.ivss.server.db.EmpleadosTable
import com.example.ivss.server.services.ExcelImportService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentHashMap

// ==========================================
// DTOs (Data Transfer Objects) - Serializable
// ==========================================

@Serializable
data class AuthRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val token: String? = null,
    val message: String,
    val userProfile: UserProfileDto? = null
)

@Serializable
data class RegisterRequest(
    val fullName: String,
    val nationalId: String,
    val email: String,
    val password: String,
    val phone: String,
    val birthDate: String
)

@Serializable
data class UserProfileDto(
    val id: String,
    val fullName: String,
    val nationalId: String,
    val email: String,
    val phone: String,
    val birthDate: String,
    val affiliationNumber: String,
    val status: String,
    val employer: String,
    val weeksContributed: Int
)

@Serializable
data class UpdateContactRequest(
    val phone: String,
    val email: String
)

@Serializable
data class VacationDto(
    val id: Int,
    val name: String,
    val status: String,
    val usedDays: Int,
    val totalDays: Int,
    val documentType: String,
    val fileName: String
)

@Serializable
data class CreateVacationRequest(
    val name: String,
    val totalDays: Int,
    val documentType: String
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class EmpleadoDto(
    val cedula: String,
    val nombreCompleto: String,
    val cargo: String,
    val servicio: String,
    val tipoPersonal: String,
    val fechaIngreso: String
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)

// ==========================================
// Backend Server Main
// ==========================================

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::ivssServerModule)
        .start(wait = true)
}

fun Application.ivssServerModule() {
    // Inicializar Base de Datos SQLite con Exposed ORM
    try {
        Database.connect("jdbc:sqlite:ivss_database.db", "org.sqlite.JDBC")
        transaction {
            SchemaUtils.create(EmpleadosTable)
        }
        println("Base de Datos SQLite inicializada correctamente")
    } catch (e: Exception) {
        println("Aviso al inicializar Base de Datos: ${e.message}")
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        anyHost()
    }

    // Datos en memoria
    var currentUserProfile = UserProfileDto(
        id = "USR-100293",
        fullName = "Juan Carlos Pérez Rodríguez",
        nationalId = "V-18.765.432",
        email = "juan.perez@ivss.gob.ve",
        phone = "+58 412-9876543",
        birthDate = "15/05/1985",
        affiliationNumber = "100234891",
        status = "Cotizante Activo",
        employer = "Ministerio del Poder Popular para la Educación",
        weeksContributed = 850
    )

    val vacationsList = ConcurrentHashMap<Int, VacationDto>().apply {
        put(1, VacationDto(1, "Vacaciones 2024", "Aprobada", 15, 15, "PDF", "Solicitud_Vacaciones_2024.pdf"))
        put(2, VacationDto(2, "Vacaciones 2023", "Aprobada", 15, 15, "WORD", "Solicitud_Vacaciones_2023.docx"))
        put(3, VacationDto(3, "Vacaciones 2022", "Aprobada", 15, 15, "PDF", "Solicitud_Vacaciones_2022.pdf"))
        put(4, VacationDto(4, "Vacaciones 2021", "Aprobada", 15, 15, "WORD", "Solicitud_Vacaciones_2021.docx"))
        put(5, VacationDto(5, "Adelanto Vacacional", "En espera", 7, 15, "PDF", "Solicitud_Adelanto_Vacacional.pdf"))
    }

    var currentPasswordHash = "12345678"

    routing {
        // Health Check
        get("/api/health") {
            call.respond(mapOf("status" to "OK", "service" to "IVSS Ktor Backend Server 2.0 (Exposed + Apache POI)"))
        }

        // Importación de Nómina Excel IVSS (.xlsx / .xls)
        post("/api/import/excel") {
            val multipart = call.receiveMultipart()
            var insertados = 0
            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    part.streamProvider().use { input ->
                        insertados += ExcelImportService.importar(input)
                    }
                }
                part.dispose()
            }
            call.respond(
                ApiResponse(
                    success = true,
                    message = "Importación completada. Empleados insertados/actualizados: $insertados",
                    data = mapOf("insertados" to insertados)
                )
            )
        }

        // Consulta de Empleados Importados
        get("/api/employees") {
            val lista = transaction {
                EmpleadosTable.selectAll().map {
                    EmpleadoDto(
                        cedula = it[EmpleadosTable.cedula],
                        nombreCompleto = "${it[EmpleadosTable.nombre1]} ${it[EmpleadosTable.apellido1]}",
                        cargo = it[EmpleadosTable.nombreCargo],
                        servicio = it[EmpleadosTable.descripcionUbi],
                        tipoPersonal = it[EmpleadosTable.descripcionTE],
                        fechaIngreso = it[EmpleadosTable.fechaIngreso].toString()
                    )
                }
            }
            call.respond(ApiResponse(success = true, message = "Empleados obtenidos", data = lista))
        }

        // Autenticación - Login
        post("/api/auth/login") {
            val req = call.receive<AuthRequest>()
            if (req.email.isNotBlank() && req.password == currentPasswordHash) {
                call.respond(
                    AuthResponse(
                        success = true,
                        token = "JWT-IVSS-TOKEN-891230",
                        message = "Inicio de sesión exitoso",
                        userProfile = currentUserProfile
                    )
                )
            } else if (req.email.isNotBlank() && req.password.length >= 6) {
                call.respond(
                    AuthResponse(
                        success = true,
                        token = "JWT-IVSS-TOKEN-891230",
                        message = "Inicio de sesión exitoso",
                        userProfile = currentUserProfile
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    AuthResponse(
                        success = false,
                        message = "Credenciales inválidas. Compruebe correo y contraseña."
                    )
                )
            }
        }

        // Autenticación - Registro
        post("/api/auth/register") {
            val req = call.receive<RegisterRequest>()
            currentUserProfile = currentUserProfile.copy(
                fullName = req.fullName,
                nationalId = req.nationalId,
                email = req.email,
                phone = req.phone,
                birthDate = req.birthDate
            )
            currentPasswordHash = req.password
            call.respond(
                ApiResponse(
                    success = true,
                    message = "Usuario registrado exitosamente en el IVSS",
                    data = currentUserProfile
                )
            )
        }

        // Perfil de Usuario
        get("/api/profile") {
            call.respond(
                ApiResponse(
                    success = true,
                    message = "Perfil obtenido correctamente",
                    data = currentUserProfile
                )
            )
        }

        put("/api/profile/contact") {
            val req = call.receive<UpdateContactRequest>()
            currentUserProfile = currentUserProfile.copy(
                phone = req.phone,
                email = req.email
            )
            call.respond(
                ApiResponse(
                    success = true,
                    message = "Datos de contacto actualizados en el servidor",
                    data = currentUserProfile
                )
            )
        }

        // Solicitudes de Vacaciones
        get("/api/vacations") {
            val sortedList = vacationsList.values.toList().sortedBy { it.id }
            call.respond(
                ApiResponse(
                    success = true,
                    message = "Vacaciones obtenidas",
                    data = sortedList
                )
            )
        }

        post("/api/vacations") {
            val req = call.receive<CreateVacationRequest>()
            val newId = (vacationsList.keys.maxOrNull() ?: 0) + 1
            val ext = if (req.documentType.uppercase() == "PDF") "pdf" else "docx"
            val newVacation = VacationDto(
                id = newId,
                name = req.name,
                status = "En espera",
                usedDays = 0,
                totalDays = if (req.totalDays > 0) req.totalDays else 15,
                documentType = req.documentType.uppercase(),
                fileName = "Solicitud_${req.name.replace(" ", "_")}.$ext"
            )
            vacationsList[newId] = newVacation
            call.respond(
                HttpStatusCode.Created,
                ApiResponse(
                    success = true,
                    message = "Solicitud de vacaciones creada en el servidor",
                    data = newVacation
                )
            )
        }

        // Generador de Documento PDF en el Servidor Backend con OpenPDF
        get("/api/vacations/{id}/pdf") {
            val id = call.parameters["id"]?.toIntOrNull()
            val vacation = id?.let { vacationsList[it] }
            if (vacation != null) {
                val pdfBytes = DocumentGenerator.generateOfficialPdf(currentUserProfile, vacation)
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, vacation.fileName).toString()
                )
                call.respondBytes(pdfBytes, ContentType.Application.Pdf)
            } else {
                call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(false, "Documento no encontrado"))
            }
        }

        // Generador de Documento Word (.docx) con Apache POI desde la plantilla JENNIFER HERNANDEZ.docx
        get("/api/vacations/{id}/docx") {
            val id = call.parameters["id"]?.toIntOrNull()
            val vacation = id?.let { vacationsList[it] }
            if (vacation != null) {
                val docxBytes = DocumentGenerator.generateDocxFromTemplate(
                    "C:\\Users\\Delkira\\Downloads\\JENNIFER HERNANDEZ.docx",
                    currentUserProfile,
                    vacation
                )
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, vacation.fileName).toString()
                )
                call.respondBytes(
                    docxBytes,
                    ContentType.parse("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                )
            } else {
                call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(false, "Documento no encontrado"))
            }
        }

        delete("/api/vacations/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null && vacationsList.containsKey(id)) {
                vacationsList.remove(id)
                call.respond(
                    ApiResponse<Unit>(
                        success = true,
                        message = "Solicitud $id eliminada del servidor"
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse<Unit>(
                        success = false,
                        message = "Solicitud no encontrada"
                    )
                )
            }
        }

        // Ajustes - Cambio de contraseña
        put("/api/settings/password") {
            val req = call.receive<ChangePasswordRequest>()
            if (req.currentPassword.isNotBlank() && req.newPassword.length >= 6) {
                currentPasswordHash = req.newPassword
                call.respond(
                    ApiResponse<Unit>(
                        success = true,
                        message = "Contraseña actualizada exitosamente en el servidor IVSS"
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(
                        success = false,
                        message = "La nueva contraseña debe tener al menos 6 caracteres"
                    )
                )
            }
        }
    }
}
