package com.example.ivss.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta del Modo Oscuro con fondo estilo WhatsApp (#0B141A, #202C33) manteniendo el rojo institucional IVSS
private val DarkColorScheme = darkColorScheme(
    primary = IvssRedLight,
    onPrimary = Color.White,
    primaryContainer = IvssRedDark,
    onPrimaryContainer = Color.White,
    secondary = IvssRedLight,
    onSecondary = Color.White,
    background = Color(0xFF0B141A),         // Fondo ultranoche estilo WhatsApp
    surface = Color(0xFF202C33),            // Tarjetas elevadas estilo WhatsApp
    surfaceVariant = Color(0xFF2A3942),     // Contenedores secundarios estilo WhatsApp
    onSurface = Color(0xFFE9EDEF),          // Texto principal claro
    onSurfaceVariant = Color(0xFF8696A0),   // Texto secundario mudo
    outlineVariant = Color(0xFF222D34),     // Bordes y divisores
    onBackground = Color(0xFFE9EDEF)
)

private val LightColorScheme = lightColorScheme(
    primary = IvssRed,
    onPrimary = Color.White,
    primaryContainer = IvssRedLight,
    onPrimaryContainer = IvssRedDark,
    secondary = Color.Black,
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurface = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF64748B),
    outlineVariant = Color(0xFFE2E8F0),
    onBackground = Color(0xFF1E293B)
)

@Composable
fun IvssTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
