package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors

private val DarkColorScheme = darkColorScheme(
  primary = ColorJetBlue,
  secondary = ColorJetOrange,
  tertiary = ColorJetSuccess,
  background = Color(0xFF121212),
  surface = Color(0xFF121212),
  onPrimary = Color.White,
  onSecondary = Color.White,
  onTertiary = Color.White,
  onBackground = Color.White,
  onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
  primary = ColorJetBlue,
  secondary = ColorJetOrange,
  tertiary = ColorJetSuccess,
  background = ColorJetBackground,
  surface = ColorJetSurface,
  onPrimary = Color.White,
  onSecondary = Color.White,
  onTertiary = Color.White,
  onBackground = ColorJetNavy,
  onSurface = ColorJetNavy,
)

@Composable
fun colorJetTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
  focusedTextColor = ColorJetNavy,
  unfocusedTextColor = ColorJetNavy,
  disabledTextColor = Color.Gray,
  focusedContainerColor = Color.White,
  unfocusedContainerColor = Color.White,
  disabledContainerColor = Color(0xFFF3F4F6),
  cursorColor = ColorJetBlue,
  focusedBorderColor = ColorJetBlue,
  unfocusedBorderColor = Color.LightGray.copy(alpha = 0.8f),
  focusedLabelColor = ColorJetNavy,
  unfocusedLabelColor = ColorJetTextSecondary,
  focusedPlaceholderColor = ColorJetTextSecondary,
  unfocusedPlaceholderColor = ColorJetTextSecondary,
  errorTextColor = ColorJetError,
  errorContainerColor = Color.White,
  errorBorderColor = ColorJetError,
  errorLabelColor = ColorJetError,
  errorPlaceholderColor = ColorJetTextSecondary
)

@Composable
fun ColorJetTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
