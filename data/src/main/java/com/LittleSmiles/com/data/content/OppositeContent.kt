package com.LittleSmiles.com.data.content

import com.LittleSmiles.com.core.domain.model.OppositeItem

object OppositeContent {
    val pairs: List<Pair<OppositeItem, OppositeItem>> = listOf(
        Pair(
            OppositeItem("Big", "🐘", 1, 0xFFF1F5F9L),
            OppositeItem("Small", "🐭", 1, 0xFFF1F5F9L)
        ),
        Pair(
            OppositeItem("Hot", "☀️", 2, 0xFFFFF7EDL),
            OppositeItem("Cold", "❄️", 2, 0xFFEFF6FFL)
        ),
        Pair(
            OppositeItem("Happy", "😊", 3, 0xFFFEFCE8L),
            OppositeItem("Sad", "😢", 3, 0xFFEFF6FFL)
        ),
        Pair(
            OppositeItem("Fast", "🐆", 4, 0xFFF0FDF4L),
            OppositeItem("Slow", "🐢", 4, 0xFFFDF2F8L)
        )
    )
}
