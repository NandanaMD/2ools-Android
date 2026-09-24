package com.twotools.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "twools_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        private val FAVORITE_TOOLS_KEY = stringSetPreferencesKey("favorite_tools")
        private val RECENT_TOOLS_KEY = stringPreferencesKey("recent_tools_csv")
    }

    val favoriteToolsFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[FAVORITE_TOOLS_KEY] ?: setOf("images_to_pdf", "image_compressor", "qr_scanner", "percentage_calc")
    }

    suspend fun toggleFavorite(toolId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[FAVORITE_TOOLS_KEY]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(toolId)) {
                current.remove(toolId)
            } else {
                current.add(toolId)
            }
            preferences[FAVORITE_TOOLS_KEY] = current
        }
    }

    val recentToolsFlow: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val raw = preferences[RECENT_TOOLS_KEY] ?: ""
        if (raw.isBlank()) emptyList() else raw.split(",").filter { it.isNotBlank() }
    }

    suspend fun recordToolUsage(toolId: String) {
        context.dataStore.edit { preferences ->
            val raw = preferences[RECENT_TOOLS_KEY] ?: ""
            val current = raw.split(",").filter { it.isNotBlank() }.toMutableList()
            current.remove(toolId)
            current.add(0, toolId)
            val trimmed = current.take(10).joinToString(",")
            preferences[RECENT_TOOLS_KEY] = trimmed
        }
    }
}
