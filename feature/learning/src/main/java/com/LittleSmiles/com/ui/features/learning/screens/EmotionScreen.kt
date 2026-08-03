package com.LittleSmiles.com.ui.features.learning.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.LittleSmiles.com.core.domain.model.Emotion
import com.LittleSmiles.com.core.domain.model.LearningActivityType
import com.LittleSmiles.com.ui.components.LearningActivityTemplate
import com.LittleSmiles.com.ui.components.LearningGridContent
import com.LittleSmiles.com.ui.features.learning.EmotionViewModel

@Composable
fun EmotionScreen(
    onBack: () -> Unit,
    viewModel: EmotionViewModel = hiltViewModel()
) {
    val emotions = remember { viewModel.contentRepository.getEmotions() }

    var selectedEmotion by remember { mutableStateOf<Emotion?>(null) }

    LearningActivityTemplate(
        activity = LearningActivityType.Emotions,
        items = emotions,
        tts = viewModel.ttsManager,
        onBack = onBack,
        getDisplayName = { it.name }
    ) { _, onItemClick ->
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                LearningGridContent(
                    items = emotions,
                    onItemClick = { emotion ->
                        selectedEmotion = emotion
                        onItemClick(emotion)
                    },
                    cardContent = { emotion ->
                        Text(emotion.emoji, fontSize = 60.sp)
                    }
                )
            }

            // Descriptive Feedback Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                selectedEmotion?.let {
                    Text(
                        text = it.description,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF431407),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
