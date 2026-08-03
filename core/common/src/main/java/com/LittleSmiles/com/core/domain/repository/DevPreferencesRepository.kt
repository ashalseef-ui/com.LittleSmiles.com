package com.LittleSmiles.com.core.domain.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.devDataStore: DataStore<Preferences> by preferencesDataStore(name = "dev_prefs")

class DevPreferencesRepository(private val context: Context) {

    private val SPEECH_RATE = floatPreferencesKey("speech_rate")
    private val VOICE_PITCH = floatPreferencesKey("voice_pitch")
    private val SELECTED_VOICE_NAME = stringPreferencesKey("selected_voice_name")

    val speechRate: Flow<Float> = context.devDataStore.data
        .map { preferences ->
            preferences[SPEECH_RATE] ?: 1.0f
        }

    val voicePitch: Flow<Float> = context.devDataStore.data
        .map { preferences ->
            preferences[VOICE_PITCH] ?: 1.0f
        }

    val selectedVoiceName: Flow<String> = context.devDataStore.data
        .map { preferences ->
            preferences[SELECTED_VOICE_NAME] ?: ""
        }

    suspend fun setSpeechRate(rate: Float) {
        context.devDataStore.edit { preferences ->
            preferences[SPEECH_RATE] = rate
        }
    }

    suspend fun setVoicePitch(pitch: Float) {
        context.devDataStore.edit { preferences ->
            preferences[VOICE_PITCH] = pitch
        }
    }

    suspend fun setSelectedVoiceName(name: String) {
        context.devDataStore.edit { preferences ->
            preferences[SELECTED_VOICE_NAME] = name
        }
    }
}
