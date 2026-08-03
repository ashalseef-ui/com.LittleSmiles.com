package com.LittleSmiles.com.core.domain.model

enum class ShapeType {
    SQUARE, CIRCLE, TRIANGLE, STAR, RECTANGLE, HEART, DIAMOND, OVAL
}

/**
 * Data model for the shapes learning screen.
 */
data class Shape(
    override val name: String,
    val colorHex: Long,
    val type: ShapeType,
    val sides: String,
    override val backgroundColorHex: Long
) : LearningItem
