package com.LittleSmiles.com.ui.features.matching

import com.LittleSmiles.com.core.domain.repository.ContentRepository
import com.LittleSmiles.com.core.util.TtsManager
import com.LittleSmiles.com.core.base.BaseLearningViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MatchingViewModel @Inject constructor(
    contentRepository: ContentRepository,
    ttsManager: TtsManager
) : BaseLearningViewModel(contentRepository, ttsManager)
