package com.LittleSmiles.com.core.domain.model

/**
 * Data model for the emotions learning screen.
 */
data class Emotion(
    override val name: String,
    val emoji: String,
    val colorHex: Long,
    val description: String,
    override val backgroundColorHex: Long
) : LearningItem
