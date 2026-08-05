package com.LittleSmiles.com.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.graphicsLayer
import com.LittleSmiles.com.core.domain.model.LearningItem

/**
 * CompositionLocals for passing hint/target state down to components
 * without modifying their signatures.
 */
val LocalTargetItemName = compositionLocalOf<String?> { null }
val LocalShowHint = compositionLocalOf { false }

/**
 * Extension to convert Domain color hex to Compose Color.
 */
val LearningItem.backgroundColor: Color get() = Color(this.backgroundColorHex.toInt())

/**
 * A unified card component used across all learning screens.
 * Uses a "Slot" for the main graphic content.
 */
@Composable
fun StandardLearningCard(
    item: LearningItem,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val targetName = LocalTargetItemName.current
    val showHint = LocalShowHint.current
    val isTarget = targetName != null && targetName == item.name
    
    val infiniteTransition = rememberInfiniteTransition(label = "hintPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    val animatedScale = if (showHint && isTarget) pulseScale else 1f

    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .clickable { onClick() },
        color = item.backgroundColor,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = item.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155)
            )
        }
    }
}

/**
 * A responsive grid that adjusts columns based on orientation.
 */
@Composable
fun <T : LearningItem> LearningGridContent(
    items: List<T>,
    onItemClick: (T) -> Unit,
    cardContent: @Composable (T) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items) { item ->
            StandardLearningCard(
                item = item,
                onClick = { onItemClick(item) }
            ) {
                cardContent(item)
            }
        }
    }
}

/**
 * A unified TopAppBar for the entire application.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LittleBudsTopAppBar(
    title: String = "",
    titleContent: (@Composable () -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    containerColor: Color = Color(0xFFF0F9FF)
) {
    CenterAlignedTopAppBar(
        title = {
            if (titleContent != null) {
                titleContent()
            } else {
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF334155)
                )
            }
        },
        navigationIcon = {
            if (navigationIcon != null) {
                navigationIcon()
            } else if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF0369A1)
                    )
                }
            }
        },
        actions = {
            if (actions != null) {
                actions()
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = containerColor
        )
    )
}
