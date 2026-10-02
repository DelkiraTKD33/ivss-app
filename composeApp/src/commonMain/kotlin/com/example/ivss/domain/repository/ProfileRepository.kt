package com.example.ivss.domain.repository

import com.example.ivss.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getUserProfile(): Flow<UserProfile>
    suspend fun updateProfile(updatedProfile: UserProfile): Boolean
}
