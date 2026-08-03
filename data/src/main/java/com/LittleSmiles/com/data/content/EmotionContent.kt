package com.LittleSmiles.com.data.content

import com.LittleSmiles.com.core.domain.model.Emotion
import com.LittleSmiles.com.core.util.LearningPalette

object EmotionContent {
    val all: List<Emotion> by lazy {
        val bgColors = LearningPalette.getUniqueColors(6)
        listOf(
            Emotion("Happy", "😊", 0xFFFACC15L, "We smile when we are happy!", bgColors[0]),
            Emotion("Sad", "😢", 0xFF60A5FAL, "It's okay to cry when we feel sad.", bgColors[1]),
            Emotion("Angry", "😡", 0xFFF87171L, "Take a deep breath when you feel angry.", bgColors[2]),
            Emotion("Surprised", "😯", 0xFFA855F7L, "Wow! Something unexpected happened!", bgColors[3]),
            Emotion("Silly", "🤪", 0xFF4ADE80L, "Let's make a funny face!", bgColors[4]),
            Emotion("Sleepy", "😴", 0xFF94A3B8L, "Time to rest our eyes.", bgColors[5])
        )
    }
}
