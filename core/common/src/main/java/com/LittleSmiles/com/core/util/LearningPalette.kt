package com.LittleSmiles.com.core.util

object LearningPalette {
    val PastelRed = 0xFFFEE2E2L
    val PastelOrange = 0xFFFFEDD5L
    val PastelYellow = 0xFFFEF3C7L
    val PastelGreen = 0xFFDCFCE7L
    val PastelEmerald = 0xFFD1FAE5L
    val PastelTeal = 0xFFCCFBF1L
    val PastelCyan = 0xFFCFFAFEL
    val PastelSky = 0xFFE0F2FEL
    val PastelBlue = 0xFFDBEAFEL
    val PastelIndigo = 0xFFE0E7FFL
    val PastelViolet = 0xFFEDE9FEL
    val PastelPurple = 0xFFF3E8FFL
    val PastelFuchsia = 0xFFFAE8FFL
    val PastelPink = 0xFFFCE7F3L
    val PastelRose = 0xFFFFE4E6L
    val PastelSlate = 0xFFF1F5F9L
    
    val AllPastels = listOf(
        PastelRed, PastelOrange, PastelYellow, PastelGreen, 
        PastelEmerald, PastelTeal, PastelCyan, PastelSky, 
        PastelBlue, PastelIndigo, PastelViolet, PastelPurple, 
        PastelFuchsia, PastelPink, PastelRose, PastelSlate
    )
    
    /**
     * Helper to get a list of unique colors from the palette as Long hex values.
     */
    fun getUniqueColors(count: Int): List<Long> {
        return AllPastels.shuffled().take(count)
    }
}
