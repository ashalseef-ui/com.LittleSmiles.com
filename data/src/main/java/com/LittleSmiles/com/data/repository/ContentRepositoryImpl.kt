package com.LittleSmiles.com.data.repository

import com.LittleSmiles.com.core.domain.model.Animal
import com.LittleSmiles.com.core.domain.model.BodyPart
import com.LittleSmiles.com.core.domain.model.ColorItem
import com.LittleSmiles.com.core.domain.model.Emotion
import com.LittleSmiles.com.core.domain.model.MatchItem
import com.LittleSmiles.com.core.domain.model.OppositeItem
import com.LittleSmiles.com.core.domain.model.Routine
import com.LittleSmiles.com.core.domain.model.Shape
import com.LittleSmiles.com.core.domain.model.TracingItem
import com.LittleSmiles.com.core.domain.repository.ContentRepository
import com.LittleSmiles.com.data.content.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepositoryImpl @Inject constructor() : ContentRepository {
    override fun getAnimals(): List<Animal> = AnimalContent.all
    override fun getShapes(): List<Shape> = ShapeContent.all
    override fun getColors(): List<ColorItem> = ColorContent.all
    override fun getEmotions(): List<Emotion> = EmotionContent.all
    override fun getRoutines(): List<Routine> = RoutineContent.all
    override fun getBodyParts(): List<BodyPart> = BodyPartContent.all
    override fun getOppositePairs(): List<Pair<OppositeItem, OppositeItem>> = OppositeContent.pairs
    override fun getMatchPools(): List<List<MatchItem>> = MatchContent.allPools
    override fun getTracingLetters(): List<TracingItem> = TracingContent.letters
    override fun getTracingNumbers(): List<TracingItem> = TracingContent.numbers
}
