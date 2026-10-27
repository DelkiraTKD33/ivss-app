package com.example.ivss.ui.vacations.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.ivss.platform.AdminVacationItem
import com.example.ivss.platform.toImageBitmap
import com.example.ivss.ui.components.BackButton
import org.koin.compose.viewmodel.koinViewModel

class VacationsScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel: VacationsViewModel = koinViewModel()
        VacationsContent(viewModel = viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VacationsContent(viewModel: VacationsViewModel) {
    val navigator = LocalNavigator.currentOrThrow
    val userProfile by viewModel.userProfile.collectAsState()
    val vacations by viewModel.vacations
    val adminVacations by viewModel.adminVacations
    val showAddDialog by viewModel.showAddDialog
    val downloadMessage by viewModel.downloadMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTab by remember { mutableStateOf("TODAS") }

    val filteredAdminVacations = remember(adminVacations, searchQuery, selectedFilterTab) {
        adminVacations.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                    item.workerName.contains(searchQuery, ignoreCase = true) ||
                    item.workerCedula.contains(searchQuery, ignoreCase = true) ||
                    item.periodName.contains(searchQuery, ignoreCase = true)

            val matchesTab = when (selectedFilterTab) {
                "EN DISFRUTE" -> item.status.equals("Aprobada", ignoreCase = true)
                "PENDIENTES" -> !item.status.equals("Aprobada", ignoreCase = true)
                else -> true
            }

            matchesSearch && matchesTab
        }
    }

    LaunchedEffect(downloadMessage) {
        downloadMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearDownloadMessage()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (userProfile?.canViewEmployeeList == true) "Control de Vacaciones" else "Mis Vacaciones", fontWeight = FontWeight.Bold) },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (userProfile?.canViewEmployeeList != true) {
                FloatingActionButton(
                    onClick = { viewModel.onAddVacationClick() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Canvas(modifier = Modifier.size(20.dp)) {
                        val strokeWidth = 2.5.dp.toPx()
                        val iconColor = Color.White
                        drawLine(
                            color = iconColor,
                            start = Offset(0f, size.height / 2),
                            end = Offset(size.width, size.height / 2),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = iconColor,
                            start = Offset(size.width / 2, 0f),
                            end = Offset(size.width / 2, size.height),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            if (userProfile?.canViewEmployeeList == true) {
                // VISTA DE CONTROL DE VACACIONES PARA ADMIN / SUPER USUARIO / RRHH / DIRECTOR
                Column(
                    modifier = Modifier
                        .widthIn(max = 750.dp)
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Control General de Vacaciones del Personal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Monitoreo de trabajadores en disfrute y períodos pendientes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Buscar por Nombre, Cédula o Período...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Filtros por Estado (Todas, En Disfrute, Pendientes)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilterTab == "TODAS",
                            onClick = { selectedFilterTab = "TODAS" },
                            label = { Text("Todas (${adminVacations.size})") },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = selectedFilterTab == "EN DISFRUTE",
                            onClick = { selectedFilterTab = "EN DISFRUTE" },
                            label = { Text("En Disfrute") },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = selectedFilterTab == "PENDIENTES",
                            onClick = { selectedFilterTab = "PENDIENTES" },
                            label = { Text("Pendientes") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredAdminVacations) { item ->
                            AdminVacationCard(
                                item = item,
                                onDownloadClick = {
                                    val vacItem = VacationItem(
                                        id = item.id,
                                        name = item.periodName,
                                        status = item.status,
                                        usedDays = item.usedDays,
                                        totalDays = item.totalDays,
                                        documentType = DocumentType.WORD,
                                        fileName = item.fileName
                                    )
                                    viewModel.downloadPdfDocument(vacItem)
                                }
                            )
                        }
                    }
                }
            } else {
                // VISTA PERSONAL DEL EMPLEADO REGULAR
                if (vacations.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tienes solicitudes de vacaciones",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .widthIn(max = 700.dp)
                                .fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = vacations,
                                key = { it.id }
                            ) { vacation ->
                                VacationCard(
                                    vacation = vacation,
                                    viewModel = viewModel,
                                    onCardClick = { navigator.push(DocumentDetailScreen(vacation = vacation)) },
                                    onDownloadClick = { viewModel.downloadPdfDocument(vacation) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddVacationDialog(
            onDismiss = { viewModel.onDismissAddDialog() },
            onConfirm = { name, days, docType -> viewModel.addVacation(name, days, docType) }
        )
    }
}

@Composable
fun AdminVacationCard(
    item: AdminVacationItem,
    onDownloadClick: () -> Unit
) {
    val isApproved = item.status.equals("Aprobada", ignoreCase = true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = item.workerName.take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.workerName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Cédula: ${item.workerCedula}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = item.periodName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Surface(
                    color = if (isApproved) Color(0xFFE8F5E9) else Color(0xFFFFF8E1),
                    border = BorderStroke(1.dp, if (isApproved) Color(0xFFA5D6A7) else Color(0xFFFFE082)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (isApproved) "En Disfrute (Aprobada)" else "Pendiente por Disfrutar",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            color = if (isApproved) Color(0xFF2E7D32) else Color(0xFFF57F17)
                        )
                    )
                }
            }

            Surface(
                onClick = onDownloadClick,
                modifier = Modifier.size(36.dp).clip(CircleShape),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        val strokeWidth = 2.dp.toPx()
                        val iconColor = Color.White
                        drawLine(
                            color = iconColor,
                            start = Offset(size.width / 2, 0f),
                            end = Offset(size.width / 2, size.height * 0.7f),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = iconColor,
                            start = Offset(size.width * 0.2f, size.height * 0.45f),
                            end = Offset(size.width / 2, size.height * 0.7f),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = iconColor,
                            start = Offset(size.width * 0.8f, size.height * 0.45f),
                            end = Offset(size.width / 2, size.height * 0.7f),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = iconColor,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddVacationDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, totalDays: Int, docType: DocumentType) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var daysText by remember { mutableStateOf("15") }
    var selectedDocType by remember { mutableStateOf(DocumentType.EXCEL) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nueva Solicitud de Vacaciones",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre o Período") },
                    placeholder = { Text("Ej: Vacaciones 2025") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = daysText,
                    onValueChange = { daysText = it.filter { char -> char.isDigit() } },
                    label = { Text("Días solicitados") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Formato de documento:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedDocType == DocumentType.EXCEL,
                            onClick = { selectedDocType = DocumentType.EXCEL },
                            label = { Text("Excel (.xlsx)") },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = selectedDocType == DocumentType.PDF,
                            onClick = { selectedDocType = DocumentType.PDF },
                            label = { Text("PDF (.pdf)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    FilterChip(
                        selected = selectedDocType == DocumentType.WORD,
                        onClick = { selectedDocType = DocumentType.WORD },
                        label = { Text("Word (.docx)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = daysText.toIntOrNull() ?: 15
                    onConfirm(name, days, selectedDocType)
                },
                enabled = name.isNotBlank()
            ) {
                Text("Solicitar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun VacationCard(
    vacation: VacationItem,
    viewModel: VacationsViewModel,
    onCardClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {}
) {
    Card(
        onClick = onCardClick,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(12.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val pixelSize = 6.dp.toPx()
                val pixelAlpha = 0.08f
                val pixelColor = Color(0xFF00ACC1)

                drawRect(
                    color = pixelColor,
                    topLeft = Offset(size.width - 24.dp.toPx(), 16.dp.toPx()),
                    size = Size(pixelSize, pixelSize),
                    alpha = pixelAlpha
                )
                drawRect(
                    color = pixelColor,
                    topLeft = Offset(size.width - 16.dp.toPx(), 26.dp.toPx()),
                    size = Size(pixelSize, pixelSize),
                    alpha = pixelAlpha
                )
                drawRect(
                    color = pixelColor,
                    topLeft = Offset(size.width - 24.dp.toPx(), 36.dp.toPx()),
                    size = Size(pixelSize, pixelSize),
                    alpha = pixelAlpha
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Vista previa real del documento creado
                DocumentPreviewThumbnail(vacation = vacation, viewModel = viewModel)

                // Columna de Contenido (Nombre, Nombre de archivo y Estado)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 2.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = vacation.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = vacation.fileName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Etiqueta de Estado
                    val isApproved = vacation.status == "Aprobada"
                    Surface(
                        color = if (isApproved) Color(0xFFE8F5E9) else Color(0xFFFFF8E1),
                        border = BorderStroke(1.dp, if (isApproved) Color(0xFFA5D6A7) else Color(0xFFFFE082)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = vacation.status,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = if (isApproved) Color(0xFF2E7D32) else Color(0xFFF57F17)
                            )
                        )
                    }
                }

                // Botón de Descarga directa en la tarjeta
                Surface(
                    onClick = onDownloadClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(16.dp)) {
                            val strokeWidth = 2.dp.toPx()
                            val iconColor = Color.White
                            drawLine(
                                color = iconColor,
                                start = Offset(size.width / 2, 0f),
                                end = Offset(size.width / 2, size.height * 0.7f),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = iconColor,
                                start = Offset(size.width * 0.2f, size.height * 0.45f),
                                end = Offset(size.width / 2, size.height * 0.7f),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = iconColor,
                                start = Offset(size.width * 0.8f, size.height * 0.45f),
                                end = Offset(size.width / 2, size.height * 0.7f),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = iconColor,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentPreviewThumbnail(
    vacation: VacationItem,
    viewModel: VacationsViewModel,
    modifier: Modifier = Modifier
) {
    val docType = vacation.documentType
    val accentColor = when (docType) {
        DocumentType.EXCEL -> Color(0xFF00C853)
        DocumentType.PDF -> Color(0xFFE53935)
        DocumentType.WORD -> Color(0xFF1E88E5)
    }

    val badgeLabel = when (docType) {
        DocumentType.EXCEL -> "X"
        DocumentType.PDF -> "PDF"
        DocumentType.WORD -> "W"
    }

    val titleText = vacation.name.uppercase()
    val userProfile by viewModel.userProfile.collectAsState()
    var previewBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(vacation.id, userProfile) {
        val result = viewModel.getDocumentPreviewImage(vacation)
        if (result.isSuccess && result.getOrNull() != null) {
            try {
                previewBitmap = result.getOrNull()!!.toImageBitmap()
            } catch (e: Exception) {
                previewBitmap = null
            }
        }
    }

    Surface(
        modifier = modifier
            .size(92.dp, 80.dp)
            .clip(RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFCFD8DC)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Área Superior: Muestra la Imagen Real del Documento
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (previewBitmap != null) {
                    Image(
                        bitmap = previewBitmap!!,
                        contentDescription = vacation.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(3.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "FORMA 12-16",
                                color = Color.White,
                                fontSize = 5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, Color.Black)
                                .padding(2.dp),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth(0.9f).height(2.dp).background(Color.DarkGray))
                            Box(modifier = Modifier.fillMaxWidth(0.7f).height(2.dp).background(Color.Gray))
                            Box(modifier = Modifier.fillMaxWidth(0.85f).height(2.dp).background(accentColor))
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFCFD8DC), thickness = 1.dp)

            // 2. Área Inferior: Franja con Icono, Título correspondiente y Esquina Doblada
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 4.dp, end = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(15.dp),
                        shape = RoundedCornerShape(3.dp),
                        color = accentColor
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = badgeLabel,
                                color = Color.White,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            color = Color(0xFF37474F)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Esquina Doblada
                Canvas(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(14.dp)
                ) {
                    val shadowFold = Path().apply {
                        moveTo(0f, size.height)
                        lineTo(size.width, 0f)
                        lineTo(size.width, size.height)
                        close()
                    }
                    drawPath(
                        path = shadowFold,
                        color = Color(0xFFB0BEC5)
                    )

                    val accentFold = Path().apply {
                        moveTo(size.width * 0.25f, size.height)
                        lineTo(size.width, size.height * 0.25f)
                        lineTo(size.width, size.height)
                        close()
                    }
                    drawPath(
                        path = accentFold,
                        color = accentColor
                    )
                }
            }
        }
    }
}
```

Now let's update `DocumentDetailScreen.kt` to center content with `widthIn(max = 750.dp)`:

Let's read `DocumentDetailScreen.kt` lines 120-150:
`composeApp/src/commonMain/kotlin/com/example/ivss/ui/vacations/ui/DocumentDetailScreen.kt`

Let's read `DocumentDetailScreen.kt` using `read_file`.   
`read_file(absolutePath = "C:/Users/Delkira/Downloads/IVSS/composeApp/src/commonMain/kotlin/com/example/ivss/ui/vacations/ui/DocumentDetailScreen.kt")`

Let's read `DocumentDetailScreen.kt`.               


Let's read `DocumentDetailScreen.kt` using `read_file`.   


           


                        
                       

                    

                    

                    

                    

                    

                   

                    

                    

                    

                   

                    

                    

                    

                    

                
                
            
        
    

    
        
            
            
        
    

    
        
            
            
        
    



    
        
        
        
        
    
        
            
            
                
            
            

            

            
        
    


    
    
    
    
    
    
        
            
            
        
        
            
            
                
                    
                    
                    
                    
                    
                
            
            
                
                    
                    
                    
                    
                
                
                    
                    
                    
                
            
        
        
            
            
            
            
        
    


    
    
    
    
    
    
        
            
            

        
            
            
                
                    
                    
                    
                    
                
                
                    
                    
                    
                
            
        

        
            
            
            
            
                
                
            
        
    


    
    
    
    
    
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
