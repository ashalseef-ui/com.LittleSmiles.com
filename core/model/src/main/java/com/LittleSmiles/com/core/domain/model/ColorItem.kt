package com.LittleSmiles.com.core.domain.model

/**
 * Data model for color learning items.
 */
data class ColorItem(
    override val name: String,
    val colorHex: Long,
    val emoji: String,
    override val backgroundColorHex: Long = colorHex
) : LearningItem
