package com.inkdrop.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val SERVER_URL_KEY = stringPreferencesKey("server_url")
        const val DEFAULT_SERVER_URL = "https://your-server.com"
    }

    val serverUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SERVER_URL_KEY] ?: DEFAULT_SERVER_URL
    }

    suspend fun getServerUrl(): String {
        return context.dataStore.data.first()[SERVER_URL_KEY] ?: DEFAULT_SERVER_URL
    }

    suspend fun setServerUrl(url: String) {
        context.dataStore.edit { preferences ->
            // Store URL as-is, trailing slash will be handled by API clients
            preferences[SERVER_URL_KEY] = url.trim()
        }
    }

    suspend fun isServerConfigured(): Boolean {
        val url = getServerUrl()
        return url.isNotBlank() && url != DEFAULT_SERVER_URL
    }
}
