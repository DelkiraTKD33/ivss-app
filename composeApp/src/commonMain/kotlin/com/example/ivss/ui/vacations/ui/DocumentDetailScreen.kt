package com.example.ivss.ui.vacations.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.ivss.domain.model.UserProfile
import com.example.ivss.platform.toImageBitmap
import com.example.ivss.ui.components.BackButton
import ivss.composeapp.generated.resources.Res
import ivss.composeapp.generated.resources.ivss_logo
import org.jetbrains.compose.resources.painterResource
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
                title = { Text("Documento Forma 12-16", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    BackButton(onClick = { navigator.pop() })
                },
                actions = {
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (previewBitmap != null) {
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
                } else {
                    OfficialForma1216View(vacation = vacation, userProfile = userProfile)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun OfficialForma1216View(
    vacation: VacationItem,
    userProfile: UserProfile?
) {
    val userName = userProfile?.fullName?.uppercase() ?: "HERNANDEZ RON JENNIFFER"
    val userNationalId = userProfile?.nationalId ?: "V- 17.062.973"
    val employerName = userProfile?.employer?.uppercase() ?: "HOSPITAL GENERAL MUNICIPAL IVSS SAN JUAN DE LOS MORROS."

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        border = BorderStroke(1.5.dp, Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Encabezado Institucional
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(Res.drawable.ivss_logo),
                        contentDescription = null,
                        modifier = Modifier.size(44.dp)
                    )
                    Column {
                        Text(
                            text = "MINISTERIO DEL PODER POPULAR PARA EL PROCESO SOCIAL DE TRABAJO",
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "INSTITUTO VENEZOLANO DE LOS SEGUROS SOCIALES",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                        Text(
                            text = "DIRECCIÓN GENERAL DE RECURSOS HUMANOS Y ADMINISTRACIÓN DE PERSONAL",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                Text(
                    text = "Forma: 12-16",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // Banner Título Oficial con Cuadro de Fecha
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AUTORIZACIÓN DE VACACIONES",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(2.dp),
                        border = BorderStroke(1.dp, Color.Black)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "FECHA DE ELABORACIÓN:\n DÍA: 24 | MES: 09 | AÑO: 2025 ",
                                color = Color.Black,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Nº: 025",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // PARA / DE
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Black)
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "PARA:  DIRECCIÓN GENERAL DE RECURSOS HUMANOS Y ADMINISTRACIÓN DE PERSONAL",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "DE:  $employerName",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // TABLA DE DATOS DEL TRABAJADOR O TRABAJADORA
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Black)
            ) {
                // Header Bar de Tabla
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0E0E0))
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DATOS DEL TRABAJADOR O TRABAJADORA",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                HorizontalDivider(color = Color.Black, thickness = 1.dp)

                // Fila 1: Nombre y Cédula
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .border(0.5.dp, Color.Black)
                            .padding(6.dp)
                    ) {
                        Text("APELLIDOS Y NOMBRES", fontSize = 7.sp, color = Color.DarkGray)
                        Text(userName, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    }
                    Column(
                        modifier = Modifier
                            .weight(0.8f)
                            .border(0.5.dp, Color.Black)
                            .padding(6.dp)
                    ) {
                        Text("CÉDULA DE IDENTIDAD Nº", fontSize = 7.sp, color = Color.DarkGray)
                        Text(userNationalId, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    }
                }

                // Fila 2: Cargo, Nº Cargo, Fecha Ingreso, Cod Origen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .border(0.5.dp, Color.Black)
                            .padding(6.dp)
                    ) {
                        Text("DENOMINACIÓN DEL CARGO", fontSize = 7.sp, color = Color.DarkGray)
                        Text("MEDICO ADJUNTO I", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Column(
                        modifier = Modifier
                            .weight(0.5f)
                            .border(0.5.dp, Color.Black)
                            .padding(6.dp)
                    ) {
                        Text("Nº DE CARGO", fontSize = 7.sp, color = Color.DarkGray)
                        Text("00101", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Column(
                        modifier = Modifier
                            .weight(0.8f)
                            .border(0.5.dp, Color.Black)
                            .padding(6.dp)
                    ) {
                        Text("FECHA DE INGRESO (D/M/A)", fontSize = 7.sp, color = Color.DarkGray)
                        Text("01 / 11 / 2019", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Column(
                        modifier = Modifier
                            .weight(0.8f)
                            .border(0.5.dp, Color.Black)
                            .padding(6.dp)
                    ) {
                        Text("COD. ORIGEN Y SERV.", fontSize = 7.sp, color = Color.DarkGray)
                        Text("60209382 - 31", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }

                // Fila 3: Unidad, Lugar, Horario, Días Semana
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .border(0.5.dp, Color.Black)
                            .padding(6.dp)
                    ) {
                        Text("UNIDAD O SERVICIO", fontSize = 7.sp, color = Color.DarkGray)
                        Text("CIRUGIA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Column(
                        modifier = Modifier
                            .weight(0.6f)
                            .border(0.5.dp, Color.Black)
                            .padding(6.dp)
                    ) {
                        Text("LUGAR", fontSize = 7.sp, color = Color.DarkGray)
                        Text("S.J. M", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Column(
                        modifier = Modifier
                            .weight(0.8f)
                            .border(0.5.dp, Color.Black)
                            .padding(6.dp)
                    ) {
                        Text("HORARIO", fontSize = 7.sp, color = Color.DarkGray)
                        Text("ASISTENCIAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Column(
                        modifier = Modifier
                            .weight(0.6f)
                            .border(0.5.dp, Color.Black)
                            .padding(6.dp)
                    ) {
                        Text("DÍAS A LA SEMANA", fontSize = 7.sp, color = Color.DarkGray)
                        Text("5", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }

                // Texto toma vacaciones
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "EL TRABAJADOR O TRABAJADORA, TOMARÁ SUS VACACIONES EN EL LAPSO SEÑALADO",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                HorizontalDivider(color = Color.Black, thickness = 1.dp)

                // Fila Lapso de Disfrute
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .border(0.5.dp, Color.Black)
                            .padding(6.dp)
                    ) {
                        Text("DESDE (D/M/A)", fontSize = 7.sp, color = Color.DarkGray)
                        Text("15 / 10 / 2025", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .border(0.5.dp, Color.Black)
                            .padding(6.dp)
                    ) {
                        Text("HASTA (D/M/A)", fontSize = 7.sp, color = Color.DarkGray)
                        Text("17 / 11 / 2025", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Column(
                        modifier = Modifier
                            .weight(0.8f)
                            .border(0.5.dp, Color.Black)
                            .padding(6.dp)
                    ) {
                        Text("PERIODO", fontSize = 7.sp, color = Color.DarkGray)
                        Text("2023 - 2024", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Column(
                        modifier = Modifier
                            .weight(0.8f)
                            .border(0.5.dp, Color.Black)
                            .padding(6.dp)
                    ) {
                        Text("Nº DE DÍAS", fontSize = 7.sp, color = Color.DarkGray)
                        Text("${vacation.usedDays} DÍAS", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                    }
                }

                // Reintegro
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "HÁBILES, DEBERÁ REINTEGRARSE EL DÍA: 18 / 11 / 2025",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            // OBSERVACIONES
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Black)
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text("OBSERVACIONES:", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("NOTA:", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(
                    text = "EL TRABAJADOR SOLICITO DICHO VACACIONES CON EXPOSICION DE MOTIVO. CORRESPONDIENTES AL PERIODO 2023-2024.",
                    fontSize = 8.5.sp,
                    color = Color.DarkGray
                )
            }

            // FIRMAS Y SELLOS (4 Casillas 2x2)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Black)
            ) {
                Row(modifier = Modifier.fillMaxWidth().height(60.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(0.5.dp, Color.Black)
                            .padding(4.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DR. WILLIAMS GONZALEZ", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("NOMBRE Y APELLIDO / FIRMA Y SELLO", fontSize = 6.5.sp, color = Color.Gray)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(0.5.dp, Color.Black)
                            .padding(4.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("LCDA. MAYARI SOJO", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("NOMBRE Y APELLIDO / FIRMA Y SELLO", fontSize = 6.5.sp, color = Color.Gray)
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().height(60.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(0.5.dp, Color.Black)
                            .padding(4.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DR. JULIO AQUINO", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("NOMBRE Y APELLIDO / FIRMA Y SELLO", fontSize = 6.5.sp, color = Color.Gray)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(0.5.dp, Color.Black)
                            .padding(4.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = userName,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color.Black
                            )
                            Text("NOMBRE Y APELLIDO / FIRMA", fontSize = 6.5.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
