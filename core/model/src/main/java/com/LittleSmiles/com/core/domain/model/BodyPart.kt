package com.LittleSmiles.com.core.domain.model

/**
 * Data model for body part learning items.
 */
data class BodyPart(
    override val name: String,
    val emoji: String,
    override val backgroundColorHex: Long
) : LearningItem
