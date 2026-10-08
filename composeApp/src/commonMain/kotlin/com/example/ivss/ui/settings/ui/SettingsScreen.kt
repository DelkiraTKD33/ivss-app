package com.example.ivss.ui.settings.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.ivss.ui.components.BackButton
import ivss.composeapp.generated.resources.Res
import ivss.composeapp.generated.resources.opcion
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

class SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel: SettingsViewModel = koinViewModel()
        SettingsContent(viewModel = viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(viewModel: SettingsViewModel) {
    val navigator = LocalNavigator.currentOrThrow
    val preferences by viewModel.preferences.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val showChangePasswordDialog by viewModel.showChangePasswordDialog
    val changePasswordState by viewModel.changePasswordState

    val showUploadExcelDialog by viewModel.showUploadExcelDialog
    val uploadExcelState by viewModel.uploadExcelState

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Configuración", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    BackButton(onClick = { navigator.pop() })
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sección: Administración y Nómina (EXCLUSIVA PARA SUPER USUARIO / ADMIN)
                if (userProfile?.isSuperUser == true) {
                    SettingsSection(title = "Administración y Nómina (Super Usuario)") {
                        SettingActionItem(
                            title = "Cargar Nómina / Base de Datos Excel",
                            subtitle = "Importa archivo .xlsx / .xls y genera usuarios automáticamente (Usuario y Clave = Cédula)",
                            icon = Res.drawable.opcion,
                            onClick = { viewModel.onOpenUploadExcelDialog() }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        SettingActionItem(
                            title = "Exportar Base de Datos a Excel",
                            subtitle = "Descarga la base de datos completa de trabajadores en formato .xlsx",
                            icon = Res.drawable.opcion,
                            onClick = { viewModel.exportExcelDatabase() }
                        )
                    }
                }

                // Sección: Cuenta y Seguridad
                SettingsSection(title = "Cuenta y Seguridad") {
                    SettingActionItem(
                        title = "Cambiar Contraseña",
                        subtitle = "Actualiza tu clave de acceso de forma segura",
                        icon = Res.drawable.opcion,
                        onClick = { viewModel.onOpenChangePasswordDialog() }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    SettingSwitchItem(
                        title = "Acceso con Huella / Biometría",
                        subtitle = "Inicia sesión rápidamente en este dispositivo",
                        checked = preferences.biometricsEnabled,
                        onCheckedChange = { viewModel.toggleBiometrics(it) }
                    )
                }

                // Sección: Notificaciones y Preferencias
                SettingsSection(title = "Notificaciones y Preferencias") {
                    SettingSwitchItem(
                        title = "Notificaciones Push",
                        subtitle = "Alertas de solicitudes de vacaciones y cotizaciones",
                        checked = preferences.notificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications(it) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    SettingSwitchItem(
                        title = "Modo Oscuro",
                        subtitle = "Ajusta la apariencia del tema de la aplicación",
                        checked = preferences.darkModeEnabled,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                }

                // Sección: Soporte e Información
                SettingsSection(title = "Soporte e Información") {
                    SettingActionItem(
                        title = "Preguntas Frecuentes",
                        subtitle = "Consulta dudas sobre el IVSS y trámites",
                        onClick = { /* TODO */ }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    SettingActionItem(
                        title = "Términos y Condiciones",
                        subtitle = "Políticas de privacidad y uso de datos",
                        onClick = { /* TODO */ }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    SettingActionItem(
                        title = "Acerca de IVSS App",
                        subtitle = "Versión 1.0.0 (Construcción 2024)",
                        onClick = { /* TODO */ }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            state = changePasswordState,
            onDismiss = { viewModel.onDismissChangePasswordDialog() },
            onConfirm = { currentPass, newPass -> viewModel.changePassword(currentPass, newPass) }
        )
    }

    if (showUploadExcelDialog) {
        UploadExcelDialog(
            state = uploadExcelState,
            onDismiss = { viewModel.onDismissUploadExcelDialog() },
            onConfirmUpload = { fileName -> viewModel.importExcelDatabase(fileName) }
        )
    }
}

@Composable
fun UploadExcelDialog(
    state: UploadExcelState,
    onDismiss: () -> Unit,
    onConfirmUpload: (fileName: String) -> Unit
) {
    var selectedFileName by remember { mutableStateOf("Nomina_Trabajadores_IVSS.xlsx") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Gestión de Nómina Excel (Super Usuario)", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state is UploadExcelState.Success) {
                    Text(
                        text = state.message,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                } else {
                    Text(
                        text = "Selecciona un archivo de Excel (.xlsx / .xls) con la nómina de trabajadores del centro asistencial.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = selectedFileName,
                        onValueChange = { selectedFileName = it },
                        label = { Text("Nombre del archivo Excel") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "💡 Creación Automática de Cuentas:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Al procesar la nómina, cada trabajador tendrá asignado como Usuario y Contraseña inicial su número de Cédula.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    if (state is UploadExcelState.Error) {
                        Text(
                            text = state.error,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (state is UploadExcelState.Success) {
                Button(onClick = onDismiss) {
                    Text("Aceptar")
                }
            } else {
                Button(
                    onClick = { onConfirmUpload(selectedFileName) },
                    enabled = selectedFileName.isNotBlank() && state !is UploadExcelState.Loading
                ) {
                    if (state is UploadExcelState.Loading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Text("Procesando...")
                        }
                    } else {
                        Text("Cargar e Importar")
                    }
                }
            }
        },
        dismissButton = {
            if (state !is UploadExcelState.Success) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            content()
        }
    }
}

@Composable
fun SettingActionItem(
    title: String,
    subtitle: String,
    icon: DrawableResource? = null,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (icon != null) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = if (isDanger) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = if (isDanger) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
            }
        }
        Text(
            text = "›",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun ChangePasswordDialog(
    state: ChangePasswordState,
    onDismiss: () -> Unit,
    onConfirm: (currentPass: String, newPass: String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Cambiar Contraseña", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (state is ChangePasswordState.Success) {
                    Text(
                        text = state.message,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                } else {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Contraseña Actual") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Nueva Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmar Nueva Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (localError != null) {
                        Text(
                            text = localError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    if (state is ChangePasswordState.Error) {
                        Text(
                            text = state.error,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (state is ChangePasswordState.Success) {
                Button(onClick = onDismiss) {
                    Text("Aceptar")
                }
            } else {
                Button(
                    onClick = {
                        if (newPassword != confirmPassword) {
                            localError = "Las contraseñas no coinciden."
                        } else {
                            localError = null
                            onConfirm(currentPassword, newPassword)
                        }
                    },
                    enabled = currentPassword.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank() && state !is ChangePasswordState.Loading
                ) {
                    if (state is ChangePasswordState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Actualizar")
                    }
                }
            }
        },
        dismissButton = {
            if (state !is ChangePasswordState.Success) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}
