package com.example.ivss.ui.resetpassword.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ResetPasswordViewModel : ViewModel() {
    private val _newPassword = mutableStateOf("")
    val newPassword: State<String> = _newPassword

    private val _confirmPassword = mutableStateOf("")
    val confirmPassword: State<String> = _confirmPassword

    private val _isResetEnabled = mutableStateOf(false)
    val isResetEnabled: State<Boolean> = _isResetEnabled

    fun onPasswordChanged(password: String, confirm: String) {
        _newPassword.value = password
        _confirmPassword.value = confirm
        _isResetEnabled.value = isValidPassword(password) && password == confirm
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    fun resetPassword() {
        // TODO: Implement password reset logic
        println("Resetting password...")
    }
}
