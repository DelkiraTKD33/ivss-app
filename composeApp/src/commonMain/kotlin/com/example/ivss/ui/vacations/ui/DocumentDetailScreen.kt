package com.example.ivss.ui.vacations.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.ivss.platform.toImageBitmap
import com.example.ivss.ui.components.BackButton
import org.koin.compose.viewmodel.koinViewModel

class DocumentDetailScreen(private val vacation: VacationItem) : Screen {
    @Composable
    override fun Content() {
        val viewModel: VacationsViewModel = koinViewModel()
        DocumentDetailContent(vacation = vacation, viewModel = viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailContent(
    vacation: VacationItem,
    viewModel: VacationsViewModel
) {
    val navigator = LocalNavigator.currentOrThrow
    val userProfile by viewModel.userProfile.collectAsState()
    val isDownloading by viewModel.isDownloading
    val downloadMessage by viewModel.downloadMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var previewBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoadingPreview by remember { mutableStateOf(true) }

    LaunchedEffect(vacation.id, userProfile) {
        isLoadingPreview = true
        val result = viewModel.getDocumentPreviewImage(vacation)
        if (result.isSuccess && result.getOrNull() != null) {
            try {
                previewBitmap = result.getOrNull()!!.toImageBitmap()
            } catch (e: Exception) {
                previewBitmap = null
            }
        }
        isLoadingPreview = false
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
                title = { Text("Vista de Documento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    BackButton(onClick = { navigator.pop() })
                },
                actions = {
                    // ÚNICA Opción de Descargar en la barra superior
                    IconButton(
                        onClick = { viewModel.downloadPdfDocument(vacation) },
                        enabled = !isDownloading
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Canvas(modifier = Modifier.size(20.dp)) {
                                val strokeWidth = 2.5.dp.toPx()
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            if (isLoadingPreview) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Text("Cargando vista previa del documento...", style = MaterialTheme.typography.bodyMedium)
                }
            } else if (previewBitmap != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Muestra ÚNICAMENTE la imagen renderizada del documento
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Image(
                            bitmap = previewBitmap!!,
                            contentDescription = "Vista Previa de la Forma 12-16",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.FillWidth
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Text(
                            text = "Forma 12-16: Autorización de Vacaciones IVSS",
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
