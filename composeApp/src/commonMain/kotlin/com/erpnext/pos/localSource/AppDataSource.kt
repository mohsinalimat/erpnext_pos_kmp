package com.erpnext.pos.localSource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val AUTH_TOKEN = stringPreferencesKey("auth_token")
    private val AUTH_REFRESH_TOKEN = stringPreferencesKey("auth_refresh_token")

    val refreshToken : Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[AUTH_REFRESH_TOKEN]
        }

    val authToken: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[AUTH_TOKEN]
        }

    suspend fun saveRefreshToken(refreshToken: String?) {
        dataStore.edit { preferences ->
            preferences[AUTH_REFRESH_TOKEN] = refreshToken ?: ""
        }
    }

    suspend fun saveAuthToken(token: String?) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = token ?: ""
        }
    }
}