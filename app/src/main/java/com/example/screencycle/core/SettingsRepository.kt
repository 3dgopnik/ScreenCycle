package com.example.screencycle.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SettingsRepository(private val context: Context) {
    private val dataStore: DataStore<Preferences> = context.dataStore

    suspend fun setGameMinutes(minutes: Int) {
        dataStore.edit { it[GAME_MINUTES] = minutes }
    }

    suspend fun getGameMinutes(): Int =
        dataStore.data.map { it[GAME_MINUTES] ?: 30 }.first()

    suspend fun setRestMinutes(minutes: Int) {
        dataStore.edit { it[REST_MINUTES] = minutes }
    }

    suspend fun getRestMinutes(): Int =
        dataStore.data.map { it[REST_MINUTES] ?: 30 }.first()

    suspend fun setBlockedPackages(packages: Set<String>) {
        dataStore.edit { it[BLOCKED_PACKAGES] = packages }
    }

    suspend fun getBlockedPackages(): Set<String> =
        dataStore.data.map { it[BLOCKED_PACKAGES] ?: emptySet() }.first()

    companion object {
        private val Context.dataStore by preferencesDataStore(name = "settings")
        private val GAME_MINUTES = intPreferencesKey("game_minutes")
        private val REST_MINUTES = intPreferencesKey("rest_minutes")
        private val BLOCKED_PACKAGES = stringSetPreferencesKey("blocked_packages")
    }
}
