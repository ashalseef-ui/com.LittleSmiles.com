package com.LittleSmiles.com.features.matching

import androidx.compose.ui.graphics.Color

/**
 * Data model for the shadow matching game.
 */
data class MatchItem(
    val id: Int,
    val emoji: String,
    val color: Color
)
