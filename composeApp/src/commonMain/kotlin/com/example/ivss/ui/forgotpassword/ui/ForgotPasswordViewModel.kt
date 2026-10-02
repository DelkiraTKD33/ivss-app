package com.example.ivss.ui.forgotpassword.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ForgotPasswordViewModel : ViewModel() {
    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _isSendEnabled = mutableStateOf(false)
    val isSendEnabled: State<Boolean> = _isSendEnabled

    fun onEmailChanged(email: String) {
        _email.value = email
        _isSendEnabled.value = isValidEmail(email)
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex().matches(email)
    }

    fun sendRecoveryEmail() {
        // TODO: Implement recovery email logic
        println("Sending recovery email to: ${_email.value}")
    }
}
