package com.LittleSmiles.com.core.domain.model

/**
 * Data model for the shadow matching game.
 */
data class MatchItem(
    val id: Int,
    val emoji: String,
    val colorHex: Long,
    override val name: String = "",
    override val backgroundColorHex: Long = 0L
) : LearningItem
