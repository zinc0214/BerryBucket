package com.zinc.datastore.login

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceDataStoreModule @Inject constructor(@ApplicationContext context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "login")
    private val loginDataStore = context.dataStore

    private val accessTokenKey = stringPreferencesKey("accessToken")
    private val refreshTokenKey = stringPreferencesKey("refreshToken")
    private val loginedEmailKey = stringPreferencesKey("loginedEmailKey")
    private val loginedEmailUidKey = stringPreferencesKey("loginedEmailUidKey")
    private val userIdKey = stringPreferencesKey("userIdKey")

    val loadAccessToken: Flow<String> = loginDataStore.data
        .map { preferences ->
            preferences[accessTokenKey] ?: ""
        }

    suspend fun setAccessToken(accessToken: String) {
        loginDataStore.edit { preferences ->
            preferences[accessTokenKey] = accessToken
        }
    }

    val loadRefreshToken: Flow<String> = loginDataStore.data
        .map { preferences ->
            preferences[refreshTokenKey] ?: ""
        }

    suspend fun setRefreshToken(refreshToken: String) {
        loginDataStore.edit { preferences ->
            preferences[refreshTokenKey] = refreshToken
        }
    }

    val loadLoginedEmail: Flow<String> = loginDataStore.data.map { preferences ->
        preferences[loginedEmailKey] ?: ""
    }

    val loadLoginedEmailUid: Flow<String> = loginDataStore.data.map { preferences ->
        preferences[loginedEmailUidKey] ?: ""
    }

    suspend fun setLoginEmail(email: String) {
        loginDataStore.edit { preferences ->
            preferences[loginedEmailKey] = email
        }
    }

    suspend fun clearLoginEmail() {
        loginDataStore.edit { preferences ->
            preferences.clear()
        }
    }


    suspend fun setLoginEmailUid(uid: String) {
        loginDataStore.edit { preferences ->
            preferences[loginedEmailUidKey] = uid
        }
    }


    val loadUserIdKey: Flow<String> = loginDataStore.data.map { preferences ->
        preferences[userIdKey] ?: ""
    }

    suspend fun setUserIdKey(id: String) {
        loginDataStore.edit { preferences ->
            preferences[userIdKey] = id
        }
    }

}