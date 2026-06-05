package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    primaryContainer = BrandPrimaryContainer,
    onPrimaryContainer = BrandOnPrimaryContainer,
    background = BrandBackground,
    onBackground = BrandOnBackground,
    surface = BrandSurface,
    onSurface = BrandOnSurface,
    surfaceVariant = BrandSurfaceVariant,
    onSurfaceVariant = BrandOnSurfaceVariant,
    outline = BrandOutline,
    outlineVariant = BrandOutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkBrandPrimary,
    primaryContainer = DarkBrandPrimaryContainer,
    onPrimaryContainer = DarkBrandOnPrimaryContainer,
    background = DarkBrandBackground,
    onBackground = DarkBrandOnBackground,
    surface = DarkBrandSurface,
    onSurface = DarkBrandOnSurface,
    surfaceVariant = DarkBrandSurfaceVariant,
    onSurfaceVariant = DarkBrandOnSurfaceVariant,
    outline = DarkBrandOutline,
    outlineVariant = DarkBrandOutlineVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
