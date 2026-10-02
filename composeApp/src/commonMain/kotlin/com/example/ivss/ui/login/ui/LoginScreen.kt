package com.example.ivss.ui.login.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.ivss.ui.components.*
import com.example.ivss.ui.forgotpassword.ui.ForgotPasswordScreen
import com.example.ivss.ui.home.ui.HomeScreen
import com.example.ivss.ui.register.ui.RegisterScreen
import ivss.composeapp.generated.resources.Res
import ivss.composeapp.generated.resources.usuario
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

class LoginScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel: LoginViewModel = koinViewModel()
        LoginScreenContent(viewModel)
    }
}

@Composable
fun LoginScreenContent(viewModel: LoginViewModel) {
    val navigator = LocalNavigator.currentOrThrow
    val preferences by viewModel.userPreferences.collectAsState()
    val showBiometricDialog by viewModel.showBiometricDialog
    val biometricState by viewModel.biometricState

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
            title = "Bienvenido",
            subtitle = "Inicia sesión para continuar"
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        LoginForm(
            viewModel = viewModel,
            onForgotPasswordClick = { navigator.push(ForgotPasswordScreen()) },
            onLoginClick = { navigator.replaceAll(HomeScreen()) }
        )

        // Botón de Inicio con Huella / Biometría
        if (preferences.biometricsEnabled) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { viewModel.onOpenBiometricDialog() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.usuario),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Iniciar sesión con huella",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        AuthFooterAction(
            instruction = "¿No tienes una cuenta? ",
            actionText = "Regístrate aquí",
            onActionClick = { navigator.push(RegisterScreen()) }
        )
    }

    if (showBiometricDialog) {
        BiometricAuthDialog(
            state = biometricState,
            onDismiss = { viewModel.onDismissBiometricDialog() },
            onAuthenticate = {
                viewModel.authenticateWithBiometrics {
                    navigator.replaceAll(HomeScreen())
                }
            }
        )
    }
}

@Composable
private fun LoginForm(
    viewModel: LoginViewModel,
    onForgotPasswordClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    val email = viewModel.email.value
    val password = viewModel.password.value
    val loginEnable = viewModel.loginEnable.value

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AuthTextField(
            value = email,
            onValueChange = { viewModel.onLoginChanged(it, password) },
            label = "Correo electrónico",
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = password,
            onValueChange = { viewModel.onLoginChanged(email, it) },
            label = "Contraseña",
            keyboardType = KeyboardType.Password,
            isPasswordField = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        AuthClickableLabel(
            text = "¿Olvidaste tu contraseña?",
            onClick = onForgotPasswordClick,
            modifier = Modifier.align(Alignment.End).padding(end = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        AuthButton(
            text = "Entrar",
            enabled = loginEnable,
            onClick = onLoginClick
        )
    }
}

@Composable
fun BiometricAuthDialog(
    state: BiometricState,
    onDismiss: () -> Unit,
    onAuthenticate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Autenticación Biométrica", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.usuario),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = when (state) {
                        is BiometricState.Idle -> "Coloque su huella dactilar en el sensor para verificar su identidad."
                        is BiometricState.Authenticating -> "Verificando huella dactilar..."
                        is BiometricState.Success -> "¡Identidad verificada exitosamente!"
                        is BiometricState.Error -> state.message
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp
                )

                if (state is BiometricState.Authenticating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            if (state is BiometricState.Idle) {
                Button(onClick = onAuthenticate) {
                    Text("Verificar Huella")
                }
            }
        },
        dismissButton = {
            if (state !is BiometricState.Authenticating && state !is BiometricState.Success) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}
