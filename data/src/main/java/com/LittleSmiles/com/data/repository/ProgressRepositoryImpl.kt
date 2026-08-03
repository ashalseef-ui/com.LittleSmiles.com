package com.LittleSmiles.com.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.LittleSmiles.com.core.domain.model.*
import com.LittleSmiles.com.core.domain.repository.ProgressRepository
import com.LittleSmiles.com.data.util.ProgressSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_progress")

class ProgressRepositoryImpl(private val context: Context) : ProgressRepository {

    private val serializer = ProgressSerializer()
    private val PROGRESS_KEY = stringPreferencesKey("progress_data")

    override fun getUserProgress(userId: String): Flow<UserProgress?> {
        return context.dataStore.data.map { preferences ->
            serializer.deserialize(preferences[PROGRESS_KEY], userId)
        }
    }

    override suspend fun resetProgress(userId: String) {
        context.dataStore.edit { preferences -> preferences.remove(PROGRESS_KEY) }
    }
}
