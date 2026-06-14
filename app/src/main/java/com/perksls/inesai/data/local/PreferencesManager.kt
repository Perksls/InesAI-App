package com.perksls.inesai.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "inesai_settings")

/**
 * Apenas configurações de comportamento da app.
 * Providers, API Keys e modelos vivem em AppDatabase (tabela providers).
 */
class PreferencesManager(private val context: Context) {

    companion object {
        private val FALLBACK_ENABLED = booleanPreferencesKey("fallback_enabled")
        private val CONTEXT_WINDOW   = intPreferencesKey("context_window")
        private val TEMPERATURE      = stringPreferencesKey("temperature")
        private val SYSTEM_PROMPT    = stringPreferencesKey("system_prompt")
        private val THEME            = stringPreferencesKey("theme")
    }

    val fallbackEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[FALLBACK_ENABLED] ?: false
    }

    val contextWindow: Flow<Int> = context.dataStore.data.map {
        it[CONTEXT_WINDOW] ?: 10
    }

    val temperature: Flow<Double> = context.dataStore.data.map {
        (it[TEMPERATURE] ?: "0.7").toDoubleOrNull() ?: 0.7
    }

    val systemPrompt: Flow<String> = context.dataStore.data.map {
        it[SYSTEM_PROMPT] ?: "You are a helpful assistant."
    }

    val theme: Flow<String> = context.dataStore.data.map {
        it[THEME] ?: "system"
    }

    suspend fun saveFallbackEnabled(enabled: Boolean) {
        context.dataStore.edit { it[FALLBACK_ENABLED] = enabled }
    }

    suspend fun saveContextWindow(window: Int) {
        context.dataStore.edit { it[CONTEXT_WINDOW] = window }
    }

    suspend fun saveTemperature(temp: Double) {
        context.dataStore.edit { it[TEMPERATURE] = temp.toString() }
    }

    suspend fun saveSystemPrompt(prompt: String) {
        context.dataStore.edit { it[SYSTEM_PROMPT] = prompt }
    }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { it[THEME] = theme }
    }
}
