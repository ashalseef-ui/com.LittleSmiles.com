package com.LittleSmiles.com.ui.features.learning.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.LittleSmiles.com.core.domain.model.Animal
import com.LittleSmiles.com.core.domain.model.LearningActivityType
import com.LittleSmiles.com.ui.components.LearningActivityTemplate
import com.LittleSmiles.com.ui.components.LearningGridContent
import com.LittleSmiles.com.ui.features.learning.AnimalViewModel

@Composable
fun AnimalScreen(
    onBack: () -> Unit,
    viewModel: AnimalViewModel = hiltViewModel()
) {
    val animals = remember { viewModel.contentRepository.getAnimals() }

    LearningActivityTemplate(
        activity = LearningActivityType.Animals,
        items = animals,
        tts = viewModel.ttsManager,
        onBack = onBack,
        getDisplayName = { it.name }
    ) { _, onItemClick ->
        LearningGridContent(
            items = animals,
            onItemClick = onItemClick,
            cardContent = { animal ->
                Text(animal.emoji, fontSize = 60.sp)
            }
        )
    }
}
