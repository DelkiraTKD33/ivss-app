package com.example.ivss.domain.repository

import kotlinx.coroutines.flow.Flow

data class UserPreferences(
    val biometricsEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false
)

interface SettingsRepository {
    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun setBiometricsEnabled(enabled: Boolean)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setDarkModeEnabled(enabled: Boolean)
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>
}
