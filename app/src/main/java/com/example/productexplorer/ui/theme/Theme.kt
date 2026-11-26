package com.example.productexplorer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Palette claire : on définit des rôles (primary, surface...), pas juste des couleurs
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006C4C),
    onPrimary = Color.White,
    secondary = Color(0xFF4F6357),
    background = Color(0xFFF7FBF6),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C1A)
)

// Palette sombre : mêmes rôles, couleurs adaptées au fond foncé
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7DDBB0),
    onPrimary = Color(0xFF003826),
    secondary = Color(0xFFB7CCBC),
    background = Color(0xFF101411),
    onBackground = Color(0xFFE0E4DF),
    surface = Color(0xFF1C211D),
    onSurface = Color(0xFFE0E4DF)
)

// Arrondis utilisés partout via MaterialTheme.shapes
private val ProductShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun ProductExplorerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // on choisit la palette selon le mode clair / sombre du système
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ProductTypography,
        shapes = ProductShapes,
        content = content
    )
}
