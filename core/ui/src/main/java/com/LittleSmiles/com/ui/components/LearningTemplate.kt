package com.LittleSmiles.com.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.LittleSmiles.com.core.domain.model.LearningActivityType
import com.LittleSmiles.com.core.util.SpeechPriority
import com.LittleSmiles.com.core.util.TtsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A standard UI wrapper for all learning activities.
 * Provides consistent layout, top bar, and celebration logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> LearningActivityTemplate(
    activity: LearningActivityType,
    items: List<T>,
    tts: TtsManager,
    onBack: () -> Unit,
    getDisplayName: (T) -> String,
    isChallengeMode: Boolean = true,
    content: @Composable (T?, (T) -> Unit) -> Unit
) {
    val title = activity.displayName
    val scope = rememberCoroutineScope()
    var targetItem by remember { mutableStateOf<T?>(null) }
    var showCelebration by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }
    var lastInteractionTime by remember { mutableLongStateOf(0L) }
    val throttleThreshold = 450L
    
    // Logic: Universal Challenge Loop
    LaunchedEffect(Unit) {
        if (isChallengeMode) {
            delay(1000)
            if (items.isNotEmpty()) {
                val nextTarget = items.random()
                targetItem = nextTarget
                tts.speak("Can you find the ${getDisplayName(nextTarget)}?", SpeechPriority.HIGH)
            }
        }
    }

    // Logic: Inactivity Hint Timer
    LaunchedEffect(targetItem, lastInteractionTime, showCelebration) {
        if (isChallengeMode && targetItem != null && !showCelebration) {
            showHint = false
            delay(10000) // 10 seconds of inactivity
            showHint = true
            val hintMessages = listOf(
                "Where is the ${getDisplayName(targetItem!!)} hiding?",
                "Can you find the ${getDisplayName(targetItem!!)}?",
                "Let's look for the ${getDisplayName(targetItem!!)}!"
            )
            tts.speak(hintMessages.random(), SpeechPriority.HIGH)
            delay(6000)
            showHint = false
        } else {
            showHint = false
        }
    }

    Scaffold(
        topBar = {
            LittleBudsTopAppBar(
                title = title,
                onBack = onBack
            )
        },
        containerColor = Color(0xFFF0FDFA)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    Column {
                        CompositionLocalProvider(
                            LocalTargetItemName provides (targetItem?.let { getDisplayName(it) }),
                            LocalShowHint provides showHint
                        ) {
                            content(targetItem) { selectedItem ->
                                val now = System.currentTimeMillis()
                                if (now - lastInteractionTime < throttleThreshold) return@content
                                lastInteractionTime = now
                                showHint = false

                                if (isChallengeMode && targetItem != null) {
                                    if (selectedItem == targetItem && !showCelebration) {
                                        showCelebration = true
                                        tts.speak("Fantastic! You found ${getDisplayName(selectedItem)}!", SpeechPriority.MEDIUM)
                                        
                                        if (items.isNotEmpty()) {
                                            val nextTarget = if (items.size > 1) {
                                                items.filter { it != targetItem }.random()
                                            } else {
                                                items.random()
                                            }
                                            targetItem = nextTarget
                                            scope.launch {
                                                delay(2500)
                                                if (targetItem == nextTarget) {
                                                    tts.speak("Now, can you find ${getDisplayName(nextTarget)}?", SpeechPriority.HIGH)
                                                }
                                            }
                                        }
                                    } else {
                                        val feedback = if (showCelebration) {
                                            getDisplayName(selectedItem)
                                        } else {
                                            "That is ${getDisplayName(selectedItem)}!"
                                        }
                                        tts.speak(feedback, SpeechPriority.LOW)
                                    }
                                } else {
                                    tts.speak(getDisplayName(selectedItem), SpeechPriority.LOW)
                                }
                            }
                        }
                    }
                }
            }

            SuccessCelebration(
                isVisible = showCelebration,
                onFinished = {
                    showCelebration = false
                }
            )
        }
    }
}
