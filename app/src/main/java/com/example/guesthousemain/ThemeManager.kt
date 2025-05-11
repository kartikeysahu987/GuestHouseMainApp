// ThemeManager.kt
package com.example.guesthousemain

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create DataStore instance
private val Context.dataStore by preferencesDataStore(name = "settings")
private val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")

// Local composition to access theme across the app
val LocalThemeManager = compositionLocalOf<ThemeManager> {
    error("ThemeManager not provided")
}

class ThemeManager(private val context: Context) {
    // Get the current theme preference as a Flow
    val isDarkThemeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_MODE_KEY] ?: false
        }

    // Update the theme preference
    suspend fun setDarkTheme(isDarkTheme: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE_KEY] = isDarkTheme
        }
    }
}