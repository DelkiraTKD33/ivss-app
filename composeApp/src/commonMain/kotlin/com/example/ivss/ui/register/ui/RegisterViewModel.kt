package com.example.ivss.ui.register.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class RegisterViewModel : ViewModel() {
    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _repeatPassword = mutableStateOf("")
    val repeatPassword: State<String> = _repeatPassword

    private val _registerEnable = mutableStateOf(false)
    val registerEnable: State<Boolean> = _registerEnable

    fun onRegisterChanged(email: String, password: String, repeatPassword: String) {
        _email.value = email
        _password.value = password
        _repeatPassword.value = repeatPassword
        _registerEnable.value = isValidEmail(email) && 
                              isValidPassword(password) && 
                              password == repeatPassword
    }

    private fun isValidEmail(email: String): Boolean = 
        email.isNotEmpty() && "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex().matches(email)

    private fun isValidPassword(password: String): Boolean = password.length >= 8
}
