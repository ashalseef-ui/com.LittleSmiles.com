package com.LittleSmiles.com.core.domain.model

/**
 * Data model for daily routines.
 */
data class Routine(
    val action: String,
    val emoji: String,
    val sequence: Int,
    val description: String,
    override val backgroundColorHex: Long = 0L
) : LearningItem {
    override val name: String get() = action
}
