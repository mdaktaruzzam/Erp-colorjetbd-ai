package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class AppThemeMode {
  LIGHT,
  DARK,
  SYSTEM
}

object ThemePreferences {
  private const val PREFS_NAME = "colorjet_theme_prefs"
  private const val KEY_THEME_MODE = "key_app_theme_mode"

  private val _themeMode = MutableStateFlow(AppThemeMode.LIGHT)
  val themeMode: StateFlow<AppThemeMode> = _themeMode

  fun init(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val savedMode = prefs.getString(KEY_THEME_MODE, AppThemeMode.LIGHT.name)
    _themeMode.value = try {
      AppThemeMode.valueOf(savedMode ?: AppThemeMode.LIGHT.name)
    } catch (e: Exception) {
      AppThemeMode.LIGHT
    }
  }

  fun setThemeMode(context: Context, mode: AppThemeMode) {
    _themeMode.value = mode
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
  }

  fun toggleTheme(context: Context) {
    val newMode = if (_themeMode.value == AppThemeMode.DARK) AppThemeMode.LIGHT else AppThemeMode.DARK
    setThemeMode(context, newMode)
  }
}

val LocalThemeController = compositionLocalOf<() -> Unit> { {} }
val LocalIsDarkMode = compositionLocalOf { false }
