package com.LittleSmiles.com.ui.features.matching

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.SoundEffectConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.LittleSmiles.com.core.domain.model.LearningActivityType
import com.LittleSmiles.com.core.domain.model.MatchItem
import com.LittleSmiles.com.core.util.SpeechPriority
import com.LittleSmiles.com.ui.components.LittleBudsTopAppBar
import com.LittleSmiles.com.ui.components.backgroundColor
import com.LittleSmiles.com.core.util.LearningPalette
import kotlinx.coroutines.delay

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
 * Interactive shadow matching game.
 * Refactored to match the unified modular UI style.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchingScreen(
    onBack: () -> Unit,
    viewModel: MatchingViewModel = hiltViewModel()
) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    val view = LocalView.current
    
    val contentRepository = viewModel.contentRepository
    val tts = viewModel.ttsManager

    val allPools = remember { contentRepository.getMatchPools() }

    var gameRound by remember { mutableIntStateOf(0) }
    
    // items with randomized unique backgrounds
    val items = remember(gameRound) {
        val rawItems = allPools.random().shuffled().take(4)
        val colors = LearningPalette.getUniqueColors(4)
        rawItems.mapIndexed { index, matchItem ->
            matchItem.copy(backgroundColorHex = colors[index])
        }
    }
    
    val shuffledShadows = remember(gameRound) { items.shuffled() }
    val matchedIds = remember(gameRound) { mutableStateListOf<Int>() }
    
    var draggingItemId by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    
    val itemPositions = remember { mutableStateMapOf<Int, Offset>() }
    val shadowPositions = remember { mutableStateMapOf<Int, Offset>() }
    var showTutorial by remember(gameRound) { mutableStateOf(true) }

    Scaffold(
        topBar = {
            LittleBudsTopAppBar(
                title = "Shadow Match",
                onBack = onBack
            )
        },
        containerColor = Color(0xFFF0F9FF)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (matchedIds.size == items.size) "Amazing! 🌟" else "Match the shadows!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items.forEach { item ->
                            val isMatched = matchedIds.contains(item.id)
                            DraggableItem(
                                item = item,
                                isMatched = isMatched,
                                isDragging = draggingItemId == item.id,
                                dragOffset = dragOffset,
                                onDragStart = { 
                                    draggingItemId = item.id
                                    dragOffset = Offset.Zero
                                    showTutorial = false
                                },
                                onDrag = { offset ->
                                    dragOffset += offset
                                    
                                    val currentAbsPos = (itemPositions[item.id] ?: Offset.Zero) + dragOffset
                                    val targetAbsPos = shadowPositions[item.id] ?: Offset.Zero
                                    
                                    if ((currentAbsPos - targetAbsPos).getDistance() < 120f) {
                                        matchedIds.add(item.id)
                                        draggingItemId = null
                                        dragOffset = Offset.Zero
                                        
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        
                                        // Reporting Success
                                        tts.speak("Good Job!", SpeechPriority.MEDIUM)
                                        
                                        if (matchedIds.size == items.size) {
                                            tts.speak("Fantastic! You matched them all!", SpeechPriority.MEDIUM)
                                        }
                                    }
                                },
                                onDragEnd = {
                                    if (draggingItemId != null) {
                                        // Reporting Attempt
                                        tts.speak("Try again!", SpeechPriority.LOW)
                                    }
                                    draggingItemId = null
                                    dragOffset = Offset.Zero
                                },
                                onPositioned = { itemPositions[item.id] = it }
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        shuffledShadows.forEach { shadow ->
                            ShadowTarget(
                                shadow = shadow,
                                isMatched = matchedIds.contains(shadow.id),
                                onPositioned = { shadowPositions[shadow.id] = it }
                            )
                        }
                    }
                }
                
                GameControls(
                    isComplete = matchedIds.size == items.size,
                    onNextRound = { gameRound++ }
                )
            }

            // Tutorial Overlay
            if (showTutorial && matchedIds.isEmpty() && itemPositions.isNotEmpty() && shadowPositions.isNotEmpty()) {
                val firstId = items.first().id
                val startPos = itemPositions[firstId]
                val endPos = shadowPositions[firstId]
                
                if (startPos != null && endPos != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        MatchingTutorial(startPos, endPos)
                    }
                }
            }
        }
    }
}

@Composable
fun MatchingTutorial(start: Offset, end: Offset) {
    val infiniteTransition = rememberInfiniteTransition(label = "matchingTutorial")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    val currentX = start.x + (end.x - start.x) * progress
    val currentY = start.y + (end.y - start.y) * progress

    val density = LocalDensity.current
    
    Icon(
        imageVector = Icons.Default.TouchApp,
        contentDescription = null,
        modifier = Modifier
            .offset { 
                IntOffset(
                    currentX.toInt() - 24.dp.toPx(density).toInt(), 
                    currentY.toInt() - 24.dp.toPx(density).toInt()
                ) 
            }
            .size(48.dp)
            .alpha(0.7f),
        tint = Color(0xFF3B82F6)
    )
}

// Extension to convert dp to px in offset
private fun Float.dpToPx(density: androidx.compose.ui.unit.Density): Float = this * density.density
private fun androidx.compose.ui.unit.Dp.toPx(density: androidx.compose.ui.unit.Density): Float = this.value * density.density

@Composable
fun DraggableItem(
    item: MatchItem,
    isMatched: Boolean,
    isDragging: Boolean,
    dragOffset: Offset,
    onDragStart: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onPositioned: (Offset) -> Unit
) {
    Surface(
        modifier = Modifier
            .size(115.dp)
            .onGloballyPositioned { 
                if (!isMatched && !isDragging) onPositioned(it.positionInWindow())
            }
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer {
                if (isDragging) {
                    translationX = dragOffset.x
                    translationY = dragOffset.y
                    scaleX = 1.2f
                    scaleY = 1.2f
                }
                alpha = if (isMatched) 0f else 1f
            }
            .pointerInput(isMatched) {
                if (!isMatched) {
                    detectDragGestures(
                        onDragStart = { onDragStart() },
                        onDrag = { change, amount ->
                            change.consume()
                            onDrag(amount)
                        },
                        onDragEnd = { onDragEnd() },
                        onDragCancel = { onDragEnd() }
                    )
                }
            },
        color = item.backgroundColor,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = if (isDragging) 8.dp else 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(item.emoji, fontSize = 62.sp)
        }
    }
}

@Composable
fun ShadowTarget(
    shadow: MatchItem,
    isMatched: Boolean,
    onPositioned: (Offset) -> Unit
) {
    Surface(
        modifier = Modifier
            .size(115.dp)
            .onGloballyPositioned { onPositioned(it.positionInWindow()) },
        color = if (isMatched) shadow.backgroundColor else Color.Black.copy(alpha = 0.05f),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = if (isMatched) 2.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isMatched) {
                Text(shadow.emoji, fontSize = 62.sp)
                Icon(
                    Icons.Default.Star, 
                    null, 
                    tint = Color(0xFFFACC15), 
                    modifier = Modifier.size(24.dp).align(Alignment.TopEnd).padding(4.dp)
                )
            } else {
                Text(
                    shadow.emoji, 
                    fontSize = 62.sp,
                    modifier = Modifier.graphicsLayer { alpha = 0.2f },
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun GameControls(isComplete: Boolean, onNextRound: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isComplete) {
            Button(
                onClick = onNextRound,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Next Round!", fontSize = 20.sp)
            }
        } else {
            OutlinedButton(
                onClick = onNextRound,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Reset / New Game")
            }
        }
    }
}
