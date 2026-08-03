package com.LittleSmiles.com.ui.features.learning.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.LittleSmiles.com.core.domain.model.BodyPart
import com.LittleSmiles.com.core.domain.model.LearningActivityType
import com.LittleSmiles.com.ui.components.LearningActivityTemplate
import com.LittleSmiles.com.ui.components.LearningGridContent
import com.LittleSmiles.com.ui.features.learning.BodyPartViewModel

@Composable
fun BodyPartScreen(
    onBack: () -> Unit,
    viewModel: BodyPartViewModel = hiltViewModel()
) {
    val bodyParts = remember { viewModel.contentRepository.getBodyParts() }

    LearningActivityTemplate(
        activity = LearningActivityType.BodyParts,
        items = bodyParts,
        tts = viewModel.ttsManager,
        onBack = onBack,
        getDisplayName = { it.name }
    ) { _, onItemClick ->
        LearningGridContent(
            items = bodyParts,
            onItemClick = onItemClick,
            cardContent = { part ->
                Text(part.emoji, fontSize = 60.sp)
            }
        )
    }
}
