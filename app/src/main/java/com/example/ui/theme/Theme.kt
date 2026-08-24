package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors

// High-Visibility Dark Color Scheme tailored for bright factory / industrial environments
private val DarkColorScheme = darkColorScheme(
  primary = ColorJetBrightBlue,
  onPrimary = Color.White,
  primaryContainer = Color(0xFF152A50),
  onPrimaryContainer = Color(0xFFD6E4FF),
  secondary = ColorJetOrange,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFF5A2A00),
  onSecondaryContainer = Color(0xFFFFDBC8),
  tertiary = ColorJetSuccess,
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFF003915),
  onTertiaryContainer = Color(0xFF6DFE91),
  background = Color(0xFF0F172A), // Slate 900
  onBackground = Color(0xFFF1F5F9),
  surface = Color(0xFF1E293B), // Slate 800
  onSurface = Color(0xFFF8FAFC),
  surfaceVariant = Color(0xFF334155),
  onSurfaceVariant = Color(0xFFCBD5E1),
  error = ColorJetError,
  onError = Color.White,
  errorContainer = Color(0xFF450A0A),
  onErrorContainer = Color(0xFFFFDAD6),
  outline = Color(0xFF475569),
  outlineVariant = Color(0xFF334155)
)

// Clean Light Color Scheme
private val LightColorScheme = lightColorScheme(
  primary = ColorJetBlue,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFD6E4FF),
  onPrimaryContainer = Color(0xFF001B3F),
  secondary = ColorJetOrange,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFFFDBC8),
  onSecondaryContainer = Color(0xFF321200),
  tertiary = ColorJetSuccess,
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFFB6F2C2),
  onTertiaryContainer = Color(0xFF00210A),
  background = ColorJetBackground,
  onBackground = ColorJetNavy,
  surface = ColorJetSurface,
  onSurface = ColorJetNavy,
  surfaceVariant = Color(0xFFE2E8F0),
  onSurfaceVariant = ColorJetTextSecondary,
  error = ColorJetError,
  onError = Color.White,
  errorContainer = Color(0xFFFFDAD6),
  onErrorContainer = Color(0xFF410002),
  outline = Color(0xFFCBD5E1),
  outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun colorJetTextFieldColors(isDark: Boolean = false): TextFieldColors = OutlinedTextFieldDefaults.colors(
  focusedTextColor = if (isDark) Color(0xFFF8FAFC) else ColorJetNavy,
  unfocusedTextColor = if (isDark) Color(0xFFE2E8F0) else ColorJetNavy,
  disabledTextColor = Color.Gray,
  focusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.White,
  unfocusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.White,
  disabledContainerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF3F4F6),
  cursorColor = if (isDark) ColorJetBrightBlue else ColorJetBlue,
  focusedBorderColor = if (isDark) ColorJetBrightBlue else ColorJetBlue,
  unfocusedBorderColor = if (isDark) Color(0xFF475569) else Color.LightGray.copy(alpha = 0.8f),
  focusedLabelColor = if (isDark) ColorJetBrightBlue else ColorJetNavy,
  unfocusedLabelColor = if (isDark) Color(0xFF94A3B8) else ColorJetTextSecondary,
  focusedPlaceholderColor = if (isDark) Color(0xFF64748B) else ColorJetTextSecondary,
  unfocusedPlaceholderColor = if (isDark) Color(0xFF64748B) else ColorJetTextSecondary,
  errorTextColor = ColorJetError,
  errorContainerColor = if (isDark) Color(0xFF1E293B) else Color.White,
  errorBorderColor = ColorJetError,
  errorLabelColor = ColorJetError,
  errorPlaceholderColor = ColorJetTextSecondary
)

@Composable
fun ColorJetTheme(
  themeMode: AppThemeMode = AppThemeMode.LIGHT,
  dynamicColor: Boolean = false,
  onToggleTheme: () -> Unit = {},
  content: @Composable () -> Unit,
) {
  val systemDark = isSystemInDarkTheme()
  val isDark = when (themeMode) {
    AppThemeMode.DARK -> true
    AppThemeMode.LIGHT -> false
    AppThemeMode.SYSTEM -> systemDark
  }

  val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

  CompositionLocalProvider(
    LocalThemeController provides onToggleTheme,
    LocalIsDarkMode provides isDark
  ) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}

