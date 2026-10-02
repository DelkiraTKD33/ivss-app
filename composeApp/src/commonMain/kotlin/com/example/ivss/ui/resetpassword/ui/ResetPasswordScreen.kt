package com.example.ivss.ui.resetpassword.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.ivss.ui.components.*
import com.example.ivss.ui.login.ui.LoginScreen
import org.koin.compose.viewmodel.koinViewModel

class ResetPasswordScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel: ResetPasswordViewModel = koinViewModel()
        ResetPasswordScreenContent(viewModel)
    }
}

@Composable
fun ResetPasswordScreenContent(viewModel: ResetPasswordViewModel) {
    val newPassword = viewModel.newPassword.value
    val confirmPassword = viewModel.confirmPassword.value
    val isResetEnabled = viewModel.isResetEnabled.value
    val navigator = LocalNavigator.currentOrThrow

    Box(modifier = Modifier.fillMaxSize()) {
        BackButton(
            onClick = { navigator.pop() },
            modifier = Modifier.align(Alignment.TopStart)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AuthLogo()
            
            Spacer(modifier = Modifier.height(32.dp))

            AuthTitleGroup(
                title = "Nueva Contraseña",
                subtitle = "Crea una contraseña segura para tu cuenta"
            )

            Spacer(modifier = Modifier.height(32.dp))

            AuthTextField(
                value = newPassword,
                onValueChange = { viewModel.onPasswordChanged(it, confirmPassword) },
                label = "Nueva Contraseña",
                keyboardType = KeyboardType.Password,
                isPasswordField = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthTextField(
                value = confirmPassword,
                onValueChange = { viewModel.onPasswordChanged(newPassword, it) },
                label = "Confirmar Contraseña",
                keyboardType = KeyboardType.Password,
                isPasswordField = true
            )

            Spacer(modifier = Modifier.height(40.dp))

            AuthButton(
                text = "Actualizar Contraseña",
                enabled = isResetEnabled,
                onClick = { 
                    viewModel.resetPassword()
                    navigator.replaceAll(LoginScreen())
                }
            )
        }
    }
}
