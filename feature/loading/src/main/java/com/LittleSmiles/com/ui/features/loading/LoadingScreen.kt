package com.LittleSmiles.com.ui.features.loading

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LittleSmiles.com.core.ui.R
import com.LittleSmiles.com.ui.components.CrystalAmbientBackground
import com.LittleSmiles.com.ui.theme.*

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Shared Crystal Ambient Background
        CrystalAmbientBackground()

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Pure Floating Logo
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .graphicsLayer { shadowElevation = 16f },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(36.dp))
                )
            }
            
            Spacer(modifier = Modifier.height(64.dp))

            // Branding with the new Crystal style
            val brandName = "Little Buds Academy"
            brandName.split(" ").chunked(2).forEach { lineWords ->
                Row(horizontalArrangement = Arrangement.Center) {
                    lineWords.forEach { word ->
                        Text(
                            text = word,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 2.sp,
                            modifier = Modifier.graphicsLayer { 
                                shadowElevation = 12f
                            }
                        )
                        if (word != lineWords.last()) Spacer(Modifier.width(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Magic is Loading...",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(48.dp))
            
            CrystalLoadingDots()
        }
    }
}

@Composable
fun CrystalLoadingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    val dotColors = listOf(
        RainbowRed, 
        RainbowYellow,
        RainbowBlue
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        dotColors.forEachIndexed { index, color ->
            val delay = index * 200
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600, delayMillis = delay, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600, delayMillis = delay, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha"
            )

            Surface(
                modifier = Modifier
                    .size(12.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    },
                color = color,
                shape = CircleShape,
                shadowElevation = 8.dp
            ) {}
        }
    }
}

