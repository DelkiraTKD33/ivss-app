package com.example.ivss.domain.model

data class UserProfile(
    val id: String,
    val fullName: String,
    val nationalId: String,
    val email: String,
    val phone: String,
    val birthDate: String,
    val affiliationNumber: String,
    val status: String,
    val employer: String,
    val weeksContributed: Int,
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
                employer.contains("RECURSOS HUMANOS", ignoreCase = true) ||
                employer.contains("DIRECCIÓN GENERAL", ignoreCase = true)
}
