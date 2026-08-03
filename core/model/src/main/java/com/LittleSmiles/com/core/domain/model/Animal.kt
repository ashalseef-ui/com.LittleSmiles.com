package com.LittleSmiles.com.core.domain.model

/**
 * Data model for the animal sounds screen.
 */
data class Animal(
    override val name: String,
    val emoji: String,
    val soundDescription: String,
    override val backgroundColorHex: Long
) : LearningItem
