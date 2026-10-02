package com.example.ivss.ui.forgotpassword.ui

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
import org.koin.compose.viewmodel.koinViewModel

class ForgotPasswordScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel: ForgotPasswordViewModel = koinViewModel()
        ForgotPasswordScreenContent(viewModel)
    }
}

@Composable
fun ForgotPasswordScreenContent(viewModel: ForgotPasswordViewModel) {
    val email = viewModel.email.value
    val isSendEnabled = viewModel.isSendEnabled.value
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
                title = "Recuperar Contraseña",
                subtitle = "Introduce tu correo para recibir instrucciones"
            )

            Spacer(modifier = Modifier.height(32.dp))

            AuthTextField(
                value = email,
                onValueChange = { viewModel.onEmailChanged(it) },
                label = "Correo electrónico",
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(40.dp))

            AuthButton(
                text = "Enviar instrucciones",
                enabled = isSendEnabled,
                onClick = { viewModel.sendRecoveryEmail() }
            )
        }
    }
}
