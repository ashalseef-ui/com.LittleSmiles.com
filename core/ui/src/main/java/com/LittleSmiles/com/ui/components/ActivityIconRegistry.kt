package com.LittleSmiles.com.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Central registry that maps LearningActivityType IDs to polished Material Rounded icons.
 */
object ActivityIconRegistry {
    fun getIconForActivity(id: String): ImageVector {
        return when (id) {
            "tracing" -> Icons.Rounded.Gesture
            "colors" -> Icons.Rounded.Palette
            "shapes" -> Icons.Rounded.Category
            "animals" -> Icons.Rounded.Pets
            "routines" -> Icons.Rounded.WbSunny
            "opposites" -> Icons.AutoMirrored.Rounded.CompareArrows
            "body_parts" -> Icons.Rounded.Face
            "emotions" -> Icons.Rounded.Mood
            "matching" -> Icons.Rounded.Extension
            "drawing" -> Icons.Rounded.Brush
            "tracing_ABC" -> Icons.Rounded.TextFields
            "tracing_123" -> Icons.Rounded.Pin
            else -> Icons.Rounded.PlayCircle
        }
    }
}
