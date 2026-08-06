package com.LittleSmiles.com.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.LittleSmiles.com.ui.theme.*

@Composable
fun CrystalAmbientBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Reverse),
        label = "offset"
    )

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0284C7), SkyBluePrimary)))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Vivid moving orbs - Softened to prevent patches
            drawCircle(
                brush = Brush.radialGradient(listOf(PastelPink.copy(alpha = 0.25f), Color.Transparent)),
                radius = size.minDimension * 0.7f,
                center = Offset(size.width * (0.2f + 0.1f * animOffset), size.height * (0.3f - 0.1f * animOffset))
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(RainbowYellow.copy(alpha = 0.2f), Color.Transparent)),
                radius = size.minDimension * 0.6f,
                center = Offset(size.width * (0.8f - 0.2f * animOffset), size.height * (0.6f + 0.1f * animOffset))
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(RainbowBlue.copy(alpha = 0.25f), Color.Transparent)),
                radius = size.minDimension * 0.8f,
                center = Offset(size.width * (0.4f + 0.3f * animOffset), size.height * (0.8f - 0.2f * animOffset))
            )
        }
        
        // Drifting subtle clouds
        repeat(4) { i ->
            val duration = 30000 + (i * 5000)
            val delay = i * 2000
            FloatingCloud(duration, delay, offsetMultiplier = i)
        }
    }
}

@Composable
fun FloatingCloud(duration: Int, delay: Int, offsetMultiplier: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "cloud")
    val xProgress by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            tween(duration, delayMillis = delay, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "x"
    )
    
    val yOffset = (80 + (offsetMultiplier * 180)).dp
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = yOffset)
            .graphicsLayer { translationX = xProgress * size.width }
    ) {
        Icon(
            imageVector = Icons.Default.Cloud,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.15f),
            modifier = Modifier.size(140.dp + (offsetMultiplier * 40).dp)
        )
    }
}
