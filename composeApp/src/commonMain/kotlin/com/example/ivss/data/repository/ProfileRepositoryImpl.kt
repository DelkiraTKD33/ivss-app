package com.example.ivss.data.repository

import com.example.ivss.domain.model.UserProfile
import com.example.ivss.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileRepositoryImpl : ProfileRepository {

    private val _userProfile = MutableStateFlow(
        UserProfile(
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
    )

    override fun getUserProfile(): Flow<UserProfile> = _userProfile.asStateFlow()

    override suspend fun updateProfile(updatedProfile: UserProfile): Boolean {
        _userProfile.value = updatedProfile
        return true
    }
}
