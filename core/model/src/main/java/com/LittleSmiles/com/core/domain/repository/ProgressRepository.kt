package com.LittleSmiles.com.core.domain.repository

import com.LittleSmiles.com.core.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {
    fun getUserProgress(userId: String): Flow<UserProgress?>
    
    suspend fun resetProgress(userId: String)
}
