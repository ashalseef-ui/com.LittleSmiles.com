package com.LittleSmiles.com.data.content

import com.LittleSmiles.com.core.domain.model.Routine

object RoutineContent {
    val all: List<Routine> = listOf(
        Routine("Wake Up", "☀️", 1, "Good morning! Time to wake up."),
        Routine("Brush Teeth", "🪥", 2, "Brush brush brush! Make them shine."),
        Routine("Wash Face", "🧼", 3, "Splash! Now we are fresh."),
        Routine("Get Dressed", "👕", 4, "Put on your favorite clothes."),
        Routine("Eat Breakfast", "🥣", 5, "Yummy! Let's get energy for the day."),
        Routine("Play Time", "🧸", 6, "Now we are ready to play!")
    )
}
