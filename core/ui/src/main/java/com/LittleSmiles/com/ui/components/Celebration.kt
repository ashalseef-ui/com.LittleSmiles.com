package com.LittleSmiles.com.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * A simplified reusable component that shows a celebration (Emoji + Text)
 * triggered by a success event.
 * Granular rewards and sticker-specific celebrations have been removed.
 */
@Composable
fun SuccessCelebration(
    isVisible: Boolean,
    onFinished: () -> Unit,
    message: String = "Great Job!"
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn(initialScale = 0.5f),
        exit = fadeOut() + scaleOut(targetScale = 1.2f),
        modifier = Modifier.pointerInput(Unit) {} 
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🌟",
                    fontSize = 120.sp
                )
                Text(
                    text = message,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFBBF24),
                    lineHeight = 56.sp
                )
            }
        }
    }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(1500)
            onFinished()
        }
    }
}
