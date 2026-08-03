package com.LittleSmiles.com.data.content

import com.LittleSmiles.com.core.domain.model.Shape
import com.LittleSmiles.com.core.domain.model.ShapeType
import com.LittleSmiles.com.core.util.LearningPalette

object ShapeContent {
    val all: List<Shape> by lazy {
        val bgColors = LearningPalette.getUniqueColors(8)
        listOf(
            Shape("Square", 0xFFEF4444L, ShapeType.SQUARE, "4 Sides", bgColors[0]),
            Shape("Circle", 0xFF3B82F6L, ShapeType.CIRCLE, "Round", bgColors[1]),
            Shape("Triangle", 0xFF22C55EL, ShapeType.TRIANGLE, "3 Sides", bgColors[2]),
            Shape("Star", 0xFFF59E0BL, ShapeType.STAR, "Shiny", bgColors[3]),
            Shape("Rectangle", 0xFF8B5CF6L, ShapeType.RECTANGLE, "4 Sides", bgColors[4]),
            Shape("Heart", 0xFFEC4899L, ShapeType.HEART, "Love", bgColors[5]),
            Shape("Diamond", 0xFF06B6D4L, ShapeType.DIAMOND, "4 Points", bgColors[6]),
            Shape("Oval", 0xFFF97316L, ShapeType.OVAL, "Stretched", bgColors[7])
        )
    }
}
