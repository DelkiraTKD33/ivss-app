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
)
