package com.LittleSmiles.com.ui.features.learning.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.LittleSmiles.com.core.domain.model.OppositeItem
import com.LittleSmiles.com.core.domain.model.LearningActivityType
import com.LittleSmiles.com.ui.components.LearningActivityTemplate
import com.LittleSmiles.com.ui.components.backgroundColor
import com.LittleSmiles.com.ui.features.learning.OppositeViewModel

/**
 * OppositeScreen refactored to use the LearningActivityTemplate.
 */
@Composable
fun OppositeScreen(
    onBack: () -> Unit,
    viewModel: OppositeViewModel = hiltViewModel()
) {
    val contentRepository = viewModel.contentRepository
    val tts = viewModel.ttsManager

    val pairs = remember { contentRepository.getOppositePairs() }

    // Flattening items for the universal challenge logic
    val allItems = remember(pairs) { pairs.flatMap { listOf(it.first, it.second) } }
    
    // We'll track the current pair index locally to maintain the "Next Pair" functionality
    var currentPairIndex by remember { mutableIntStateOf(0) }

    LearningActivityTemplate(
        activity = LearningActivityType.Opposites,
        items = allItems,
        tts = tts,
        onBack = onBack,
        getDisplayName = { it.name },
        isChallengeMode = false
    ) { _, onItemClick ->
        val (left, right) = pairs[currentPairIndex]
        
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OppositeCard(left, Modifier.weight(1f)) {
                    onItemClick(left)
                }
                OppositeCard(right, Modifier.weight(1f)) {
                    onItemClick(right)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    currentPairIndex = (currentPairIndex + 1) % pairs.size
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B)),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Next Pair", fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun OppositeCard(item: OppositeItem, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .fillMaxHeight(0.8f)
            .clickable { onClick() },
        color = item.backgroundColor,
        shape = RoundedCornerShape(32.dp),
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(2.dp, Color.LightGray)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(item.emoji, fontSize = if (item.name == "Big") 100.sp else 60.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = item.name,
                fontSize = 24.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                color = Color(0xFF1E293B)
            )
        }
    }
}
