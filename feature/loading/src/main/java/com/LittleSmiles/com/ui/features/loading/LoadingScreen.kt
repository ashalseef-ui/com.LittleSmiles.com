package com.LittleSmiles.com.ui.features.loading

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LittleSmiles.com.core.ui.R

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Integration of the same App Logo for visual continuity
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(24.dp))
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Production Tip: Use Lottie for high-quality kid-friendly animations.
            // When you have a JSON asset, uncomment below and remove BouncingBallsAnimation()
            /*
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_animation))
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(150.dp)
            )
            */
            
            BouncingBallsAnimation()
            
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Little Buds Academy",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 42.sp
                )
            )
            Text(
                text = "Loading Fun...",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 28.sp
                )
            )
        }
    }
}

@Composable
fun BouncingBallsAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "bouncing")
    
    val animationColors = listOf(
        Color(0xFF42A5F5), // Blue
        Color(0xFF66BB6A), // Green
        Color(0xFFFFA726), // Orange
        Color(0xFFAB47BC)  // Purple
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        animationColors.forEachIndexed { index, color ->
            val delay = index * 150
            val yOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -30f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600, delayMillis = delay, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "yOffset"
            )

            Canvas(
                modifier = Modifier
                    .size(20.dp)
                    .offset { IntOffset(x = 0, y = yOffset.dp.roundToPx()) }
            ) {
                drawCircle(color = color)
            }
        }
    }
}
