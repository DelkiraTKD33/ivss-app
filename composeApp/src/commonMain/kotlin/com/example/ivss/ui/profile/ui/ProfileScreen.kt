package com.example.ivss.ui.profile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.ivss.domain.model.UserProfile
import com.example.ivss.ui.components.BackButton
import ivss.composeapp.generated.resources.Res
import ivss.composeapp.generated.resources.usuario
import ivss.composeapp.generated.resources.vacaciones
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

class ProfileScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel: ProfileViewModel = koinViewModel()
        ProfileContent(viewModel = viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(viewModel: ProfileViewModel) {
    val navigator = LocalNavigator.currentOrThrow
    val uiState by viewModel.uiState.collectAsState()
    val isDownloading by viewModel.isDownloading
    val userMessage by viewModel.userMessage.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Perfil de Usuario", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    BackButton(onClick = { navigator.pop() })
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is ProfileUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is ProfileUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                is ProfileUiState.Success -> {
                    val user = state.user
                    val cargoDisplay = user.cargo.ifBlank { "CONTRATADO" }
                    val numCargoDisplay = if (user.numeroCargo.isBlank() || user.numeroCargo == "0" || user.numeroCargo == "0.0") "CONTRATADO" else user.numeroCargo

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Tarjeta Encabezado del Usuario con el Servicio en el Badge
                        ProfileHeaderCard(user = user)

                        // Información Personal en blanco por defecto con Lápiz para Editar/Rellenar
                        ProfileSectionCard(
                            title = "Información Personal",
                            items = listOf(
                                ProfileInfoItem("Correo Electrónico", user.email),
                                ProfileInfoItem("Teléfono", user.phone),
                                ProfileInfoItem("Fecha de Nacimiento", user.birthDate),
                                ProfileInfoItem("Servicio / Departamento", user.servicio)
                            ),
                            onEditClick = { showEditDialog = true }
                        )

                        // Información del Cargo (Solo datos del cargo)
                        ProfileSectionCard(
                            title = "Información del Cargo",
                            items = listOf(
                                ProfileInfoItem("Cargo del Trabajador", cargoDisplay),
                                ProfileInfoItem("Nº de Cargo", numCargoDisplay),
                                ProfileInfoItem("Fecha de Ingreso", user.fechaIngreso.ifBlank { "01/11/2019" }),
                                ProfileInfoItem("Patrono / Empresa", user.employer)
                            )
                        )

                        // Botón de Acción para Descarga de Constancia
                        OutlinedButton(
                            onClick = { viewModel.downloadConstancia() },
                            enabled = !isDownloading,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isDownloading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text("Descargar Constancia de Cotizaciones")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (showEditDialog) {
                        EditProfileDialog(
                            user = user,
                            onDismiss = { showEditDialog = false },
                            onConfirm = { phone, email, birthDate, servicio ->
                                viewModel.updateProfileInfo(phone, email, birthDate, servicio)
                                showEditDialog = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeaderCard(user: UserProfile) {
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(Res.drawable.usuario),
                        contentDescription = "Avatar de usuario",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nombre Completo
            Text(
                text = user.fullName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Cédula
            Text(
                text = "C.I. ${user.nationalId}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            )

            // Badge del Servicio del Trabajador (solo visible si posee servicio asignado)
            if (user.servicio.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFE8F5E9),
                    border = BorderStroke(1.dp, Color(0xFFA5D6A7)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = user.servicio,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

data class ProfileInfoItem(val label: String, val value: String)

@Composable
fun ProfileSectionCard(
    title: String,
    items: List<ProfileInfoItem>,
    onEditClick: (() -> Unit)? = null
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                if (onEditClick != null) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Canvas(modifier = Modifier.size(16.dp)) {
                            val strokeWidth = 2.dp.toPx()
                            val color = Color(0xFFD32F2F)
                            val path = Path().apply {
                                moveTo(size.width * 0.7f, 0f)
                                lineTo(size.width, size.height * 0.3f)
                                lineTo(size.width * 0.3f, size.height)
                                lineTo(0f, size.height)
                                lineTo(0f, size.height * 0.7f)
                                close()
                            }
                            drawPath(
                                path = path,
                                color = color,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            items.forEach { item ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = item.value,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (item.value.isBlank()) Color.Gray else MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    user: UserProfile,
    onDismiss: () -> Unit,
    onConfirm: (phone: String, email: String, birthDate: String, servicio: String) -> Unit
) {
    var email by remember { mutableStateOf(user.email) }
    var phone by remember { mutableStateOf(user.phone) }
    var birthDate by remember { mutableStateOf(user.birthDate) }
    var servicio by remember { mutableStateOf(user.servicio) }

    var showDatePicker by remember { mutableStateOf(false) }
    var expandedDropdown by remember { mutableStateOf(false) }

    val serviciosList = listOf(
        "RECURSOS HUMANOS",
        "ADMINISTRACIÓN",
        "DIRECCIÓN",
        "ENFERMERÍA",
        "MEDICINA INTERNA",
        "UCI",
        "PEDIATRÍA",
        "CIRUGÍA",
        "EMERGENCIA",
        "TRAUMATOLOGÍA",
        "LABORATORIO",
        "FARMACIA",
        "ODONTOLOGÍA",
        "SERVICIOS GENERALES"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Información Personal", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico") },
                    placeholder = { Text("Ej: usuario@correo.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono de Contacto") },
                    placeholder = { Text("Ej: +58 412-1234567") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Campo Fecha de Nacimiento con Selector de Calendario
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = { birthDate = it },
                        readOnly = true,
                        label = { Text("Fecha de Nacimiento") },
                        placeholder = { Text("Toca para abrir calendario") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    painter = painterResource(Res.drawable.vacaciones),
                                    contentDescription = "Abrir Calendario",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                    )
                }

                // Selector Dropdown de Servicio / Departamento
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = servicio,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Servicio / Departamento") },
                        placeholder = { Text("Seleccionar servicio") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        serviciosList.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item, fontWeight = FontWeight.SemiBold) },
                                onClick = {
                                    servicio = item
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(phone, email, birthDate, servicio) },
                enabled = email.isNotBlank() || phone.isNotBlank() || birthDate.isNotBlank() || servicio.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val totalDays = millis / 86400000L
                            val daysSinceEpoch = totalDays.toInt()
                            var days = daysSinceEpoch + 719468
                            val era = (if (days >= 0) days else days - 146096) / 146097
                            val doe = days - era * 146097
                            val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
                            val y = yoe + era * 400
                            val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
                            val mp = (5 * doy + 2) / 153
                            val d = doy - (153 * mp + 2) / 5 + 1
                            val m = mp + if (mp < 10) 3 else -9
                            val year = y + if (m <= 2) 1 else 0

                            val dStr = if (d < 10) "0$d" else "$d"
                            val mStr = if (m < 10) "0$m" else "$m"
                            birthDate = "$dStr/$mStr/$year"
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
