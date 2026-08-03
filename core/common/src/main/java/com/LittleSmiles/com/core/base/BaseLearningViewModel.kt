package com.LittleSmiles.com.core.base

import androidx.lifecycle.ViewModel
import com.LittleSmiles.com.core.domain.repository.ContentRepository
import com.LittleSmiles.com.core.util.TtsManager

/**
 * Base ViewModel for all learning-related features.
 * Provides common dependencies.
 */
abstract class BaseLearningViewModel(
    val contentRepository: ContentRepository,
    val ttsManager: TtsManager
) : ViewModel()
