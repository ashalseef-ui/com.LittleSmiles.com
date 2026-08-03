package com.LittleSmiles.com.core.domain.model

/**
 * Centralized registry for all learning activities in the app.
 * Each entry provides a persistent ID for tracking, a display name, and its navigation route.
 * 
 * This is the source of truth for activity metadata.
 */
sealed class LearningActivityType(
    val id: String,
    val displayName: String,
    val route: String,
    val iconEmoji: String,
    val isFree: Boolean = false
) {
    object Tracing : LearningActivityType("tracing", "Tracing", "tracing", "✍️", isFree = true)
    object Colors : LearningActivityType("colors", "Color Fun!", "colors", "🌈", isFree = true)
    object Shapes : LearningActivityType("shapes", "Shape Fun!", "shapes", "🟦", isFree = true)
    object Animals : LearningActivityType("animals", "Animal Friends", "animals", "🦁")
    object Routines : LearningActivityType("routines", "My Daily Routine", "routines", "⏰")
    object Opposites : LearningActivityType("opposites", "Opposites", "opposites", "↔️")
    object BodyParts : LearningActivityType("body_parts", "Body Parts", "body_parts", "👶")
    object Emotions : LearningActivityType("emotions", "How Do I Feel?", "emotions", "😊")
    object Matching : LearningActivityType("matching", "Shadow Match", "matching", "🧩")
    object Drawing : LearningActivityType("drawing", "Magic Markers", "drawing", "✍️")
    
    // Tracing activities
    object Letters : LearningActivityType("tracing_ABC", "Letters", "letters", "🅰️", isFree = true)
    object Numbers : LearningActivityType("tracing_123", "Numbers", "numbers", "1️⃣", isFree = true)
    
    companion object {
        /**
         * List of all learning activities.
         * Using listOfNotNull to protect against potential initialization race conditions
         * that can occur with companion objects in sealed classes.
         */
        val all: List<LearningActivityType>
            get() = listOfNotNull(
                Tracing, Colors, Shapes, Animals, Routines,
                Opposites, BodyParts, Emotions, Matching,
                Drawing, Letters, Numbers
            )

        /**
         * Safely finds an activity by its persistent ID.
         * Returns null if the ID is unrecognized or null.
         */
        fun fromId(id: String?): LearningActivityType? {
            if (id == null) return null
            return all.firstOrNull { it.id == id }
        }
    }
}
