package com.example.ivss.ui.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.ivss.ui.login.ui.LoginScreen
import com.example.ivss.ui.profile.ui.ProfileScreen
import com.example.ivss.ui.settings.ui.SettingsScreen
import com.example.ivss.ui.vacations.ui.VacationsScreen
import ivss.composeapp.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        HomeScreenContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent() {
    val navigator = LocalNavigator.currentOrThrow
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "IVSS Menú",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Inicio") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { 
                        Icon(
                            painter = painterResource(Res.drawable.home), 
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        ) 
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Vacaciones") },
                    selected = false,
                    onClick = { 
                        scope.launch { 
                            drawerState.close()
                            navigator.push(VacationsScreen())
                        } 
                    },
                    icon = { 
                        Icon(
                            painter = painterResource(Res.drawable.vacaciones),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Perfil") },
                    selected = false,
                    onClick = { 
                        scope.launch { 
                            drawerState.close()
                            navigator.push(ProfileScreen())
                        } 
                    },
                    icon = { 
                        Icon(
                            painter = painterResource(Res.drawable.usuario), 
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        ) 
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Configuración") },
                    selected = false,
                    onClick = { 
                        scope.launch { 
                            drawerState.close()
                            navigator.push(SettingsScreen())
                        } 
                    },
                    icon = { 
                        Icon(
                            painter = painterResource(Res.drawable.opcion), 
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        ) 
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(
                    label = { Text("Cerrar Sesión") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navigator.replaceAll(LoginScreen())
                        }
                    },
                    icon = { 
                        Icon(
                            painter = painterResource(Res.drawable.cerrar_sesion), 
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        ) 
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("IVSS Inicio", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                painter = painterResource(Res.drawable.menu), 
                                contentDescription = "Menú",
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "¡Bienvenido de nuevo!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Has iniciado sesión correctamente en IVSS.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        }
    }
}
