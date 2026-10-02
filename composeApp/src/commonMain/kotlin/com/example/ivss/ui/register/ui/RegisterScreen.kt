package com.example.ivss.ui.register.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.ivss.ui.components.*
import com.example.ivss.ui.home.ui.HomeScreen
import org.koin.compose.viewmodel.koinViewModel

class RegisterScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel: RegisterViewModel = koinViewModel()
        RegisterScreenContent(viewModel)
    }
}

@Composable
fun RegisterScreenContent(viewModel: RegisterViewModel) {
    val navigator = LocalNavigator.currentOrThrow

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AuthLogo()
            Spacer(modifier = Modifier.height(32.dp))

            AuthTitleGroup(
                title = "Crear Cuenta",
                subtitle = "Regístrate para empezar"
            )

            Spacer(modifier = Modifier.height(32.dp))

            RegisterForm(
                viewModel = viewModel,
                onBack = { navigator.pop() },
                onRegisterClick = { navigator.replaceAll(HomeScreen()) }
            )
        }
    }
}

@Composable
private fun RegisterForm(
    viewModel: RegisterViewModel,
    onBack: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val email = viewModel.email.value
    val password = viewModel.password.value
    val repeatPassword = viewModel.repeatPassword.value
    val registerEnable = viewModel.registerEnable.value

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AuthTextField(
            value = email,
            onValueChange = { viewModel.onRegisterChanged(it, password, repeatPassword) },
            label = "Correo electrónico",
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = password,
            onValueChange = { viewModel.onRegisterChanged(email, it, repeatPassword) },
            label = "Contraseña",
            keyboardType = KeyboardType.Password,
            isPasswordField = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = repeatPassword,
            onValueChange = { viewModel.onRegisterChanged(email, password, it) },
            label = "Confirmar Contraseña",
            keyboardType = KeyboardType.Password,
            isPasswordField = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        AuthClickableLabel(
            text = "¿Ya tienes cuenta? Inicia sesión",
            onClick = onBack,
            modifier = Modifier.align(Alignment.End).padding(end = 8.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        AuthButton(
            text = "Registrarse",
            enabled = registerEnable,
            onClick = onRegisterClick
        )
    }
}
