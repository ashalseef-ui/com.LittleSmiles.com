package com.LittleSmiles.com.ui.features.learning.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.LittleSmiles.com.core.domain.model.Routine
import com.LittleSmiles.com.core.domain.model.LearningActivityType
import com.LittleSmiles.com.core.util.SpeechPriority
import com.LittleSmiles.com.ui.components.LearningActivityTemplate
import com.LittleSmiles.com.ui.features.learning.RoutineViewModel

@Composable
fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context.findActivity()
        if (activity == null) {
            onDispose {}
        } else {
            val originalOrientation = activity.requestedOrientation
            activity.requestedOrientation = orientation
            onDispose {
                activity.requestedOrientation = originalOrientation
            }
        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * RoutineScreen refactored to use the LearningActivityTemplate.
 */
@Composable
fun RoutineScreen(
    onBack: () -> Unit,
    viewModel: RoutineViewModel = hiltViewModel()
) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    val contentRepository = viewModel.contentRepository
    val tts = viewModel.ttsManager

    val routines = remember { contentRepository.getRoutines() }

    if (routines.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No routines available", fontSize = 20.sp, color = Color.Gray)
        }
        return
    }

    var currentStep by remember { mutableIntStateOf(0) }
    var isFinishing by remember { mutableStateOf(false) }
    var lastActionTime by remember { mutableLongStateOf(0L) }
    val throttleThreshold = 500L

    val currentRoutine = routines[currentStep]

    LearningActivityTemplate(
        activity = LearningActivityType.Routines,
        items = routines,
        tts = tts,
        onBack = onBack,
        getDisplayName = { it.action },
        isChallengeMode = false
    ) { _, onItemClick ->
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step Indicator
            Text(
                text = "Step ${currentRoutine.sequence} of ${routines.size}",
                fontSize = 18.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = Color(0xFFBE185D)
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Interactive Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                color = Color.White,
                shape = RoundedCornerShape(48.dp),
                shadowElevation = 8.dp,
                onClick = { onItemClick(currentRoutine) }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(currentRoutine.emoji, fontSize = 120.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = currentRoutine.action,
                        fontSize = 36.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                        color = Color(0xFF831843)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = currentRoutine.description,
                        fontSize = 20.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = Color(0xFF9D174D)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Navigation Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { 
                        val now = System.currentTimeMillis()
                        if (now - lastActionTime < throttleThreshold) return@Button
                        lastActionTime = now
                        if (currentStep > 0) currentStep-- 
                    },
                    enabled = currentStep > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF472B6))
                ) {
                    Text("Previous")
                }

                IconButton(
                    onClick = {
                        val now = System.currentTimeMillis()
                        if (now - lastActionTime < throttleThreshold) return@IconButton
                        lastActionTime = now
                        tts.speak(currentRoutine.description, SpeechPriority.MEDIUM)
                    },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Speak",
                        tint = Color(0xFFDB2777),
                        modifier = Modifier.size(64.dp)
                    )
                }

                Button(
                    onClick = { 
                        val now = System.currentTimeMillis()
                        if (now - lastActionTime < throttleThreshold) return@Button
                        lastActionTime = now

                        if (currentStep < routines.size - 1) {
                            currentStep++
                        } else if (!isFinishing) {
                            isFinishing = true
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB2777))
                ) {
                    Text(if (currentStep == routines.size - 1) "Finish!" else "Next")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Visual Sequence Bar
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(routines) { item ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (item.sequence <= currentRoutine.sequence) Color(0xFFF472B6) else Color(0xFFFCE7F3),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(item.emoji, fontSize = 20.sp)
                    }
                }
            }
        }
    }
}
