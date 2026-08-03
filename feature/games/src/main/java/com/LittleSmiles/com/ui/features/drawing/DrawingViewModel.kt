package com.LittleSmiles.com.ui.features.drawing

import com.LittleSmiles.com.core.domain.repository.ContentRepository
import com.LittleSmiles.com.core.util.TtsManager
import com.LittleSmiles.com.core.base.BaseLearningViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DrawingViewModel @Inject constructor(
    contentRepository: ContentRepository,
    ttsManager: TtsManager
) : BaseLearningViewModel(contentRepository, ttsManager)
