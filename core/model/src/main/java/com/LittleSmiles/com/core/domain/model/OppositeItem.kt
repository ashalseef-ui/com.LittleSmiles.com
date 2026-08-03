package com.LittleSmiles.com.core.domain.model

/**
 * Data model for the opposites learning game.
 */
data class OppositeItem(
    override val name: String,
    val emoji: String,
    val pairId: Int,
    override val backgroundColorHex: Long
) : LearningItem
