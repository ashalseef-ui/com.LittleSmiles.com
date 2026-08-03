package com.LittleSmiles.com.ui.features.learning

import com.LittleSmiles.com.core.domain.repository.ContentRepository
import com.LittleSmiles.com.core.util.TtsManager
import com.LittleSmiles.com.core.base.BaseLearningViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ColorViewModel @Inject constructor(
    contentRepository: ContentRepository,
    ttsManager: TtsManager
) : BaseLearningViewModel(contentRepository, ttsManager)

@HiltViewModel
class ShapeViewModel @Inject constructor(
    contentRepository: ContentRepository,
    ttsManager: TtsManager
) : BaseLearningViewModel(contentRepository, ttsManager)

@HiltViewModel
class AnimalViewModel @Inject constructor(
    contentRepository: ContentRepository,
    ttsManager: TtsManager
) : BaseLearningViewModel(contentRepository, ttsManager)

@HiltViewModel
class RoutineViewModel @Inject constructor(
    contentRepository: ContentRepository,
    ttsManager: TtsManager
) : BaseLearningViewModel(contentRepository, ttsManager)

@HiltViewModel
class OppositeViewModel @Inject constructor(
    contentRepository: ContentRepository,
    ttsManager: TtsManager
) : BaseLearningViewModel(contentRepository, ttsManager)

@HiltViewModel
class BodyPartViewModel @Inject constructor(
    contentRepository: ContentRepository,
    ttsManager: TtsManager
) : BaseLearningViewModel(contentRepository, ttsManager)

@HiltViewModel
class EmotionViewModel @Inject constructor(
    contentRepository: ContentRepository,
    ttsManager: TtsManager
) : BaseLearningViewModel(contentRepository, ttsManager)
