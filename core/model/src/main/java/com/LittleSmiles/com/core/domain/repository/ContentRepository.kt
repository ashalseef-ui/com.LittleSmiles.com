package com.LittleSmiles.com.core.domain.repository

import com.LittleSmiles.com.core.domain.model.*

/**
 * Strongly typed repository for educational content.
 */
interface ContentRepository {
    fun getAnimals(): List<Animal>
    fun getShapes(): List<Shape>
    fun getColors(): List<ColorItem>
    fun getEmotions(): List<Emotion>
    fun getRoutines(): List<Routine>
    fun getBodyParts(): List<BodyPart>
    fun getOppositePairs(): List<Pair<OppositeItem, OppositeItem>>
    fun getMatchPools(): List<List<MatchItem>>
    fun getTracingLetters(): List<TracingItem>
    fun getTracingNumbers(): List<TracingItem>
}
