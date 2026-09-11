package com.kharcha.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// --- Warm brand palette ---
private val Brown = Color(0xFF6D4C41)
private val BrownDark = Color(0xFF4E342E)
private val Coral = Color(0xFFFF7043)

private val LightColors = lightColorScheme(
    primary = Brown,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBCF),
    onPrimaryContainer = Color(0xFF2C160D),
    secondary = Color(0xFF8D6E63),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E0D8),
    onSecondaryContainer = Color(0xFF2C160D),
    tertiary = Coral,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDBCF),
    onTertiaryContainer = Color(0xFF3A0B00),
    background = Color(0xFFFFF8F5),
    onBackground = Color(0xFF211A17),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF211A17),
    surfaceVariant = Color(0xFFF3E7E1),
    onSurfaceVariant = Color(0xFF52443D),
    outline = Color(0xFF857269)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE7BDAD),
    onPrimary = Color(0xFF422B22),
    primaryContainer = Color(0xFF5A4034),
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = Color(0xFFD7C2B8),
    onSecondary = Color(0xFF3B2C25),
    secondaryContainer = Color(0xFF52433B),
    onSecondaryContainer = Color(0xFFF3E0D8),
    tertiary = Color(0xFFFFB59D),
    onTertiary = Color(0xFF5C1900),
    tertiaryContainer = Color(0xFF7D2E11),
    onTertiaryContainer = Color(0xFFFFDBCF),
    background = Color(0xFF1B1210),
    onBackground = Color(0xFFEDE0DA),
    surface = Color(0xFF241A17),
    onSurface = Color(0xFFEDE0DA),
    surfaceVariant = Color(0xFF52443D),
    onSurfaceVariant = Color(0xFFD7C2B8),
    outline = Color(0xFF9F8D84)
)

private val KharchaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun KharchaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = KharchaShapes,
        content = content
    )
}
