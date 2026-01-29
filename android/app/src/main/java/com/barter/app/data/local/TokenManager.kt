package com.barter.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "barter_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = longPreferencesKey("user_id")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val NICKNAME_KEY = stringPreferencesKey("nickname")
        private val AVATAR_KEY = stringPreferencesKey("avatar")
    }

    val token: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }

    val userId: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[USER_ID_KEY]
    }

    val username: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USERNAME_KEY]
    }

    val nickname: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[NICKNAME_KEY]
    }

    val avatar: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[AVATAR_KEY]
    }

    suspend fun saveAuthData(
        token: String,
        userId: Long,
        username: String,
        nickname: String?,
        avatar: String?
    ) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = userId
            prefs[USERNAME_KEY] = username
            nickname?.let { prefs[NICKNAME_KEY] = it }
            avatar?.let { prefs[AVATAR_KEY] = it }
        }
    }

    suspend fun updateNickname(nickname: String) {
        context.dataStore.edit { prefs ->
            prefs[NICKNAME_KEY] = nickname
        }
    }

    suspend fun updateAvatar(avatar: String) {
        context.dataStore.edit { prefs ->
            prefs[AVATAR_KEY] = avatar
        }
    }

    suspend fun clearAuthData() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
