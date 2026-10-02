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

@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        val settingsRepository: SettingsRepository = koinInject()
        val userPreferences by settingsRepository.getUserPreferences().collectAsState(initial = UserPreferences())

        IvssTheme(darkTheme = userPreferences.darkModeEnabled) {
            Navigator(LoginScreen()) { navigator ->
                SlideTransition(navigator)
            }
        }
    }
}
