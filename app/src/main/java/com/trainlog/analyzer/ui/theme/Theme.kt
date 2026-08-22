package com.trainlog.analyzer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TealPrimary = Color(0xFF1B5F73)
val PaperBg = Color(0xFFF3EFE6)
val PaperSurface = Color(0xFFFFFDF8)
val Ink = Color(0xFF1A2A32)
val Muted = Color(0xFF5C6B73)
val Subtle = Color(0xFF8A969C)
val Border = Color(0xFFD9D2C4)
val PrimarySoft = Color(0xFFD7E6EB)
val Ok = Color(0xFF2D8A4E)
val Warn = Color(0xFFB45309)
val Danger = Color(0xFFB42318)
val InkBg = Color(0xFF10242C)

private val LightColors = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color(0xFFF7F4EC),
    primaryContainer = PrimarySoft,
    onPrimaryContainer = TealPrimary,
    secondary = Muted,
    onSecondary = Color.White,
    tertiary = Ok,
    background = PaperBg,
    onBackground = Ink,
    surface = PaperSurface,
    onSurface = Ink,
    surfaceVariant = PrimarySoft,
    onSurfaceVariant = Muted,
    outline = Border,
    error = Danger
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7EB6C7),
    onPrimary = InkBg,
    primaryContainer = Color(0xFF1B3A45),
    onPrimaryContainer = Color(0xFFD7E6EB),
    secondary = Color(0xFFA0B0B8),
    background = InkBg,
    onBackground = Color(0xFFF3EFE6),
    surface = Color(0xFF162A32),
    onSurface = Color(0xFFF3EFE6),
    surfaceVariant = Color(0xFF1E3640),
    onSurfaceVariant = Color(0xFFA0B0B8),
    outline = Color(0xFF3A5058),
    error = Color(0xFFFFB4AB)
)

@Composable
fun TrainLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
