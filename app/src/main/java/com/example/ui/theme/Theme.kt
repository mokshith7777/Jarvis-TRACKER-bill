package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisDarkColorScheme = darkColorScheme(
  primary = JarvisCyan,
  onPrimary = Color(0xFF001F24),
  primaryContainer = JarvisCyanContainer,
  onPrimaryContainer = Color(0xFF70F5FF),
  secondary = JarvisHoloBlue,
  onSecondary = Color(0xFF001E28),
  secondaryContainer = Color(0xFF004D61),
  onSecondaryContainer = Color(0xFFBBE9FF),
  tertiary = JarvisGold,
  onTertiary = Color(0xFF241A00),
  background = JarvisVoid,
  onBackground = TextPrimary,
  surface = JarvisDarkNavy,
  onSurface = TextPrimary,
  surfaceVariant = JarvisCard,
  onSurfaceVariant = TextSecondary,
  outline = JarvisCardBorder,
  outlineVariant = Color(0xFF1E355B),
  error = JarvisRed,
  onError = Color.White
)

private val JarvisLightColorScheme = lightColorScheme(
  primary = Color(0xFF006877),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFA1EFFF),
  onPrimaryContainer = Color(0xFF001F25),
  secondary = Color(0xFF00657E),
  onSecondary = Color.White,
  tertiary = Color(0xFF7D5700),
  background = Color(0xFFF6F8FC),
  onBackground = Color(0xFF0B1320),
  surface = Color.White,
  onSurface = Color(0xFF0B1320),
  surfaceVariant = Color(0xFFE2E8F0),
  onSurfaceVariant = Color(0xFF475569),
  outline = Color(0xFFCBD5E1),
  error = Color(0xFFBA1A1A)
)

@Composable
fun JarvisTheme(
  darkTheme: Boolean = true, // Default to Jarvis futuristic HUD dark theme
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) JarvisDarkColorScheme else JarvisLightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
