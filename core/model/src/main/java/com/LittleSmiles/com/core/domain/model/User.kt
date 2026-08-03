package com.LittleSmiles.com.core.domain.model

import java.util.concurrent.TimeUnit

data class User(
    val uid: String,
    val email: String,
    val deviceId: String?,
    val trialStartDate: Long?,
    val isPremium: Boolean,
    val minutesUsedToday: Long
) {
    /**
     * Calculates how many days are left in the 30-day trial.
     * Returns 0 if the trial has expired or not started.
     */
    val trialDaysLeft: Long
        get() {
            val start = trialStartDate ?: return 0
            val now = System.currentTimeMillis()
            val diff = now - start
            // If clock is moved back (diff < 0), we treat it as Day 1 (30 left) 
            // but never more than 30. This prevents extending trial via clock hacking.
            if (diff < 0) return 30 
            val daysPassed = TimeUnit.MILLISECONDS.toDays(diff)
            return (30 - daysPassed).coerceIn(0, 30)
        }

    /**
     * Helper to check if the trial is still active.
     */
    val isTrialActive: Boolean get() = trialDaysLeft > 0

    /**
     * Helper to check if the trial has ever been started.
     */
    val hasTrialStarted: Boolean get() = trialStartDate != null
}
