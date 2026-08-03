package com.LittleSmiles.com.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.LittleSmiles.com.core.domain.model.LearningActivityType

/**
 * Data model for main menu items, now powered by the Activity Registry.
 */
data class MenuData(
    val activity: LearningActivityType,
    val color: Color
) {
    val title: String get() = activity.displayName
    val route: String get() = activity.route
    val emoji: String get() = activity.iconEmoji
    val icon: ImageVector get() = ActivityIconRegistry.getIconForActivity(activity.id)
    val isFree: Boolean get() = activity.isFree
}
