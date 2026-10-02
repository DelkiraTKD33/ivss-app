package com.example.ivss

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.example.ivss.di.appModule
import com.example.ivss.domain.repository.SettingsRepository
import com.example.ivss.domain.repository.UserPreferences
import com.example.ivss.ui.login.ui.LoginScreen
import com.example.ivss.ui.theme.IvssTheme
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.mp.KoinPlatform

@Composable
@Preview
fun App() {
    val koinContextExists = remember { KoinPlatform.getKoinOrNull() != null }

    if (koinContextExists) {
        AppContent()
    } else {
        KoinApplication(application = {
            modules(appModule)
        }) {
            AppContent()
        }
    }
}

@Composable
fun AppContent() {
    val settingsRepository: SettingsRepository = koinInject()
    val userPreferences by settingsRepository.getUserPreferences().collectAsState(initial = UserPreferences())

    IvssTheme(darkTheme = userPreferences.darkModeEnabled) {
        Navigator(LoginScreen()) { navigator ->
            SlideTransition(navigator)
        }
    }
}
