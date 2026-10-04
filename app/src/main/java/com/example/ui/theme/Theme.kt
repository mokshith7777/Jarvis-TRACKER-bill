package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisDarkColorScheme = darkColorScheme(
  primary = JarvisNeonRed,
  onPrimary = Color.White,
  primaryContainer = JarvisCyanContainer,
  onPrimaryContainer = Color(0xFFFFB3C1),
  secondary = JarvisNeonBlue,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFF06265C),
  onSecondaryContainer = Color(0xFFB9D7FF),
  tertiary = JarvisNeonGreen,
  onTertiary = Color(0xFF00150A),
  background = JarvisVoid,
  onBackground = TextPrimary,
  surface = JarvisDarkNavy,
  onSurface = TextPrimary,
  surfaceVariant = JarvisCard,
  onSurfaceVariant = TextSecondary,
  outline = JarvisCardBorder,
  outlineVariant = Color(0xFF24513A),
  error = JarvisNeonRed,
  onError = Color.White
)

private val JarvisLightColorScheme = lightColorScheme(
  primary = Color(0xFFD50032),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFFFD9DF),
  onPrimaryContainer = Color(0xFF3A000B),
  secondary = Color(0xFF1557C0),
  onSecondary = Color.White,
  tertiary = Color(0xFF008A45),
  background = Color(0xFFF7F9FC),
  onBackground = Color(0xFF0B1320),
  surface = Color.White,
  onSurface = Color(0xFF0B1320),
  surfaceVariant = Color(0xFFE9F5EF),
  onSurfaceVariant = Color(0xFF475569),
  outline = Color(0xFFB7C7BC),
  error = Color(0xFFBA1A1A)
)

@Composable
fun JarvisTheme(
  darkTheme: Boolean = true,
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) JarvisDarkColorScheme else JarvisLightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
