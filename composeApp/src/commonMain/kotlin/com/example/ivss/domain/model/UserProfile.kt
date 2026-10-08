package com.example.ivss.domain.model

data class UserProfile(
    val id: String,
    val fullName: String,
    val nationalId: String,
    val email: String = "",
    val phone: String = "",
    val birthDate: String = "",
    val affiliationNumber: String = "100234891",
    val status: String = "Cotizante Activo",
    val servicio: String = "ADMINISTRACIÓN Y RRHH",
    val cargo: String = "CONTRATADO",
    val numeroCargo: String = "CONTRATADO",
    val employer: String = "HOSPITAL GENERAL MUNICIPAL IVSS SAN JUAN DE LOS MORROS.",
    val fechaIngreso: String = "01/11/2019",
    val weeksContributed: Int = 850,
    val profilePictureUrl: String? = null
) {
    val isSuperUser: Boolean
        get() = status.equals("SUPER_USUARIO", ignoreCase = true) ||
                id.equals("ADM-000001", ignoreCase = true) ||
                email.equals("admin@ivss.gob.ve", ignoreCase = true) ||
                fullName.contains("Super Usuario", ignoreCase = true)

    val canViewEmployeeList: Boolean
        get() = isSuperUser ||
                status.contains("SUPER_USUARIO", ignoreCase = true) ||
                status.contains("DIRECTOR", ignoreCase = true) ||
                status.contains("COORDINADOR", ignoreCase = true) ||
                status.contains("RRHH", ignoreCase = true) ||
                status.contains("ADMIN", ignoreCase = true) ||
                cargo.contains("DIRECTOR", ignoreCase = true) ||
                cargo.contains("COORDINADOR", ignoreCase = true) ||
                employer.contains("RECURSOS HUMANOS", ignoreCase = true) ||
                employer.contains("DIRECCIÓN GENERAL", ignoreCase = true)
}
