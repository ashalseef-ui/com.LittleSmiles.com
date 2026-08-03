package com.LittleSmiles.com.core.domain.model

/**
 * Data model for tracing activities (letters and numbers).
 */
data class TracingItem(
    val char: String,
    val phonicWord: String,
    val emoji: String,
    override val name: String = char,
    override val backgroundColorHex: Long = 0L
) : LearningItem
