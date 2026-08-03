package com.LittleSmiles.com.ui.features.learning.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.LittleSmiles.com.core.domain.model.ColorItem
import com.LittleSmiles.com.core.domain.model.LearningActivityType
import com.LittleSmiles.com.ui.components.LearningActivityTemplate
import com.LittleSmiles.com.ui.components.LearningGridContent
import com.LittleSmiles.com.ui.features.learning.ColorViewModel

@Composable
fun ColorScreen(
    onBack: () -> Unit,
    viewModel: ColorViewModel = hiltViewModel()
) {
    val colors = remember { viewModel.contentRepository.getColors() }

    LearningActivityTemplate(
        activity = LearningActivityType.Colors,
        items = colors,
        tts = viewModel.ttsManager,
        onBack = onBack,
        getDisplayName = { it.name }
    ) { _, onItemClick ->
        LearningGridContent(
            items = colors,
            onItemClick = onItemClick,
            cardContent = { colorItem ->
                Text(colorItem.emoji, fontSize = 48.sp)
            }
        )
    }
}
