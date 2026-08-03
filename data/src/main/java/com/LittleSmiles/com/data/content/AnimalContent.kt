package com.LittleSmiles.com.data.content

import com.LittleSmiles.com.core.domain.model.Animal
import com.LittleSmiles.com.core.util.LearningPalette

object AnimalContent {
    val all: List<Animal> by lazy {
        val colors = LearningPalette.getUniqueColors(8)
        listOf(
            Animal("Cow", "🐄", "Moo!", colors[0]),
            Animal("Dog", "🐶", "Woof! Woof!", colors[1]),
            Animal("Cat", "🐱", "Meow!", colors[2]),
            Animal("Lion", "🦁", "Roar!", colors[3]),
            Animal("Pig", "🐷", "Oink! Oink!", colors[4]),
            Animal("Sheep", "🐑", "Baa!", colors[5]),
            Animal("Duck", "🦆", "Quack!", colors[6]),
            Animal("Monkey", "🐒", "Ooh ooh aah aah!", colors[7])
        )
    }
}
