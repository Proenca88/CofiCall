package com.example.coficall.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CoficabYellow,
    onPrimary = CoficabBlue,
    primaryContainer = DarkCard,
    onPrimaryContainer = NeutralOffWhite,
    secondary = CoficabYellow,
    onSecondary = CoficabBlue,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = NeutralOffWhite,
    tertiary = OnlineGreen,
    background = DarkBackground,
    onBackground = NeutralOffWhite,
    surface = DarkSurface,
    onSurface = NeutralOffWhite,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = NeutralMedGrey,
    outline = DarkCard,
    error = ErrorRed,
)

private val LightColorScheme = lightColorScheme(
    primary = CoficabRoyalBlue,
    onPrimary = NeutralWhite,
    primaryContainer = LightBluePillBg,
    onPrimaryContainer = CoficabRoyalBlue,
    secondary = CoficabRoyalBlue,
    onSecondary = NeutralWhite,
    secondaryContainer = NeutralOffWhite,
    onSecondaryContainer = NeutralCharcoal,
    tertiary = OnlineGreen,
    background = NeutralOffWhite,
    onBackground = NeutralCharcoal,
    surface = NeutralWhite,
    onSurface = NeutralCharcoal,
    surfaceVariant = LightGrayBg,
    onSurfaceVariant = NeutralDarkGrey,
    outline = LightGrayBorder,
    error = ErrorRed,
)

@Composable
fun CofiCallTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
