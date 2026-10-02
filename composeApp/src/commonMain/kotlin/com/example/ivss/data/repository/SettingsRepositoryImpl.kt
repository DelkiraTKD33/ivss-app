package com.example.ivss.data.repository

import com.example.ivss.domain.repository.SettingsRepository
import com.example.ivss.domain.repository.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsRepositoryImpl : SettingsRepository {

    private val _preferences = MutableStateFlow(UserPreferences())

    override fun getUserPreferences(): Flow<UserPreferences> = _preferences.asStateFlow()

    override suspend fun setBiometricsEnabled(enabled: Boolean) {
        _preferences.update { it.copy(biometricsEnabled = enabled) }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        _preferences.update { it.copy(notificationsEnabled = enabled) }
    }

    override suspend fun setDarkModeEnabled(enabled: Boolean) {
        _preferences.update { it.copy(darkModeEnabled = enabled) }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        if (currentPassword.isBlank()) {
            return Result.failure(Exception("Debe ingresar su contraseña actual."))
        }
        if (newPassword.length < 6) {
            return Result.failure(Exception("La nueva contraseña debe tener al menos 6 caracteres."))
        }
        return Result.success(Unit)
    }
}
