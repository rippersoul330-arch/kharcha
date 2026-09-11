package com.kharcha.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BrandBrown = Color(0xFF6D4C41)
private val BrandCoral = Color(0xFFFF7043)
private val BrandTeal = Color(0xFF26A69A)

private val LightColors = lightColorScheme(
    primary = BrandBrown,
    secondary = BrandCoral,
    tertiary = BrandTeal
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD7B7A6),
    secondary = BrandCoral,
    tertiary = BrandTeal
)

@Composable
fun KharchaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
