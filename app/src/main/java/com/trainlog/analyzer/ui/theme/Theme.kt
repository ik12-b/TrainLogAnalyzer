package com.trainlog.analyzer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ——— Palette: laboratory notebook (teal ink on warm paper) ———
val TealPrimary = Color(0xFF0F6B7A)
val TealDeep = Color(0xFF0A4A54)
val TealSoft = Color(0xFFD4EEF2)
val TealGlow = Color(0xFF1A8A9C)

val PaperBg = Color(0xFFF5F1E8)
val PaperSurface = Color(0xFFFFFDF9)
val PaperElevated = Color(0xFFFFFFFF)
val Ink = Color(0xFF142228)
val Muted = Color(0xFF5A6B73)
val Subtle = Color(0xFF8B989E)
val Border = Color(0xFFE0D8CC)
val BorderStrong = Color(0xFFC9BFAF)

val Ok = Color(0xFF1B7A45)
val OkSoft = Color(0xFFD8F3E4)
val Warn = Color(0xFFB45309)
val WarnSoft = Color(0xFFFDEBD3)
val Danger = Color(0xFFB42318)
val DangerSoft = Color(0xFFFCE4E2)
val AccentPurple = Color(0xFF6D28D9)
val AccentOrange = Color(0xFFEA580C)

val InkBg = Color(0xFF0B1C22)
val InkSurface = Color(0xFF122830)
val InkElevated = Color(0xFF1A3540)

private val LightColors = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color(0xFFF7FCFD),
    primaryContainer = TealSoft,
    onPrimaryContainer = TealDeep,
    secondary = Muted,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8EEF0),
    onSecondaryContainer = Ink,
    tertiary = Ok,
    onTertiary = Color.White,
    tertiaryContainer = OkSoft,
    onTertiaryContainer = Ok,
    background = PaperBg,
    onBackground = Ink,
    surface = PaperSurface,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEEE8DC),
    onSurfaceVariant = Muted,
    surfaceContainerHighest = PaperElevated,
    outline = Border,
    outlineVariant = Color(0xFFEDE6DA),
    error = Danger,
    onError = Color.White,
    errorContainer = DangerSoft,
    onErrorContainer = Danger
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF5EC4D4),
    onPrimary = InkBg,
    primaryContainer = Color(0xFF1A3F48),
    onPrimaryContainer = TealSoft,
    secondary = Color(0xFFA8B8C0),
    onSecondary = InkBg,
    secondaryContainer = Color(0xFF243840),
    onSecondaryContainer = Color(0xFFD0DCE0),
    tertiary = Color(0xFF5DDB9A),
    onTertiary = InkBg,
    tertiaryContainer = Color(0xFF1A3D2C),
    background = InkBg,
    onBackground = Color(0xFFE8F0F2),
    surface = InkSurface,
    onSurface = Color(0xFFE8F0F2),
    surfaceVariant = InkElevated,
    onSurfaceVariant = Color(0xFFA8B8C0),
    surfaceContainerHighest = Color(0xFF1F3A44),
    outline = Color(0xFF3A5058),
    outlineVariant = Color(0xFF2A4048),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

val TrainLogTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.6.sp
    )
)

val TrainLogShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TrainLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = TrainLogTypography,
        shapes = TrainLogShapes,
        content = content
    )
}
