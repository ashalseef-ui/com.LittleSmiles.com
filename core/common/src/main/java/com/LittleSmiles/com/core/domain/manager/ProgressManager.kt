package com.LittleSmiles.com.core.domain.manager

import com.LittleSmiles.com.core.domain.model.*
import com.LittleSmiles.com.core.domain.repository.ProgressRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

/**
 * Orchestrates educational progress logic.
 * Granular tracking and reward logic have been removed.
 */
class ProgressManager(
    val repository: ProgressRepository,
    private val scope: CoroutineScope,
    private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = kotlinx.coroutines.Dispatchers.IO
) {
    fun resetSessionState() {}

    suspend fun resetAllProgress(userId: String) {
        withContext(ioDispatcher) {
            repository.resetProgress(userId)
        }
    }

    // Stub methods for compatibility
    fun startSession(activity: LearningActivityType) {}
    fun endSession(userId: String) {}
    fun onCorrectAnswer(userId: String, activity: LearningActivityType) {}
    fun onAttempt(userId: String, activity: LearningActivityType) {}
    suspend fun resetChildProgress(userId: String, childId: String) { resetAllProgress(userId) }
}
