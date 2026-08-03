package com.LittleSmiles.com.data.content

import com.LittleSmiles.com.core.domain.model.BodyPart
import com.LittleSmiles.com.core.util.LearningPalette

object BodyPartContent {
    val all: List<BodyPart> by lazy {
        val colors = LearningPalette.getUniqueColors(12)
        listOf(
            BodyPart("Head", "👦", colors[0]),
            BodyPart("Eyes", "👀", colors[1]),
            BodyPart("Ears", "👂", colors[2]),
            BodyPart("Nose", "👃", colors[3]),
            BodyPart("Mouth", "👄", colors[4]),
            BodyPart("Hands", "🖐️", colors[5]),
            BodyPart("Feet", "🦶", colors[6]),
            BodyPart("Arms", "💪", colors[7]),
            BodyPart("Legs", "🦵", colors[8]),
            BodyPart("Shoulders", "👔", colors[9]),
            BodyPart("Knees", "👖", colors[10]),
            BodyPart("Toes", "👣", colors[11])
        )
    }
}
