package com.example.ivss.ui.vacations.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
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
    val vacations by viewModel.vacations
    val showAddDialog by viewModel.showAddDialog
    val downloadMessage by viewModel.downloadMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(downloadMessage) {
        downloadMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearDownloadMessage()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis Vacaciones", fontWeight = FontWeight.Bold) },
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
            FloatingActionButton(
                onClick = { viewModel.onAddVacationClick() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                // Cruz sola en el FAB
                Canvas(modifier = Modifier.size(20.dp)) {
                    val strokeWidth = 2.5.dp.toPx()
                    val iconColor = Color.White
                    // Línea horizontal
                    drawLine(
                        color = iconColor,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                    // Línea vertical
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
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = vacations,
                        key = { it.id }
                    ) { vacation ->
                        VacationCard(
                            vacation = vacation,
                            onCardClick = { navigator.push(DocumentDetailScreen(vacation = vacation)) },
                            onDownloadClick = { viewModel.downloadPdfDocument(vacation) }
                        )
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
            // Fondo decorativo sutil con efecto de cuadritos al borde derecho
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
                // 1. Vista previa dinámica del archivo con contenido de tabla y esquina doblada
                DocumentPreviewThumbnail(vacation = vacation)

                // 2. Columna de Contenido (Nombre, Nombre de archivo y Estado)
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
                            // Flecha hacia abajo
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
                            // Barra de piso
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
            // 1. Área Superior: Vista Previa de la Rejilla / Contenido de Tabla
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Encabezado del documento
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .background(accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "FORMA 12-16",
                            color = Color.White,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Rejilla/Líneas de celdas dibujadas con Canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val rowCount = 6
                        val colCount = 5
                        val rowHeight = size.height / rowCount
                        val colWidth = size.width / colCount
                        val gridColor = Color(0xFFE0E0E0)

                        for (i in 1..rowCount) {
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, i * rowHeight),
                                end = Offset(size.width, i * rowHeight),
                                strokeWidth = 1f
                            )
                        }

                        for (j in 1..colCount) {
                            drawLine(
                                color = gridColor,
                                start = Offset(j * colWidth, 0f),
                                end = Offset(j * colWidth, size.height),
                                strokeWidth = 1f
                            )
                        }

                        // Celdas simuladas con datos
                        val cellDataColor = Color(0xFF78909C)
                        drawRect(
                            color = cellDataColor,
                            topLeft = Offset(colWidth * 0.1f, rowHeight * 0.2f),
                            size = Size(colWidth * 0.8f, rowHeight * 0.6f)
                        )
                        drawRect(
                            color = cellDataColor,
                            topLeft = Offset(colWidth * 1.1f, rowHeight * 1.2f),
                            size = Size(colWidth * 0.8f, rowHeight * 0.6f)
                        )
                        drawRect(
                            color = cellDataColor,
                            topLeft = Offset(colWidth * 2.1f, rowHeight * 2.2f),
                            size = Size(colWidth * 0.8f, rowHeight * 0.6f)
                        )
                        drawRect(
                            color = cellDataColor,
                            topLeft = Offset(colWidth * 3.1f, rowHeight * 3.2f),
                            size = Size(colWidth * 0.8f, rowHeight * 0.6f)
                        )
                        drawRect(
                            color = cellDataColor,
                            topLeft = Offset(colWidth * 0.1f, rowHeight * 4.2f),
                            size = Size(colWidth * 0.8f, rowHeight * 0.6f)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFCFD8DC), thickness = 1.dp)

            // 2. Área Inferior: Franja con Icono, Título correspondiente y Esquina Doblada
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .background(Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 4.dp, end = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Badge del tipo de archivo (Icono verde X / PDF / DOCX)
                    Surface(
                        modifier = Modifier.size(16.dp),
                        shape = RoundedCornerShape(3.dp),
                        color = accentColor
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = badgeLabel,
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Título correspondiente del documento
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.5.sp,
                            color = Color(0xFF37474F)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 3. Esquina Doblada en la parte inferior derecha (Dog-Ear Fold)
                Canvas(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(16.dp)
                ) {
                    // Sombra interior doblada
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

                    // Triángulo exterior del doblez (Verde/Rojo/Azul)
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
