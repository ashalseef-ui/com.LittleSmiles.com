package com.LittleSmiles.com.ui.features.tracing

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.roundToInt
import com.LittleSmiles.com.core.domain.model.LearningActivityType
import com.LittleSmiles.com.core.domain.model.TracingItem
import com.LittleSmiles.com.core.util.SpeechPriority
import com.LittleSmiles.com.ui.components.LittleBudsTopAppBar
import com.LittleSmiles.com.ui.components.SuccessCelebration
import com.LittleSmiles.com.ui.theme.*

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

enum class TracingMode { ABCs, Numbers, Random }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracingScreen(
    mode: String,
    onBack: () -> Unit,
    viewModel: TracingViewModel = hiltViewModel()
) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    
    val contentRepository = viewModel.contentRepository
    val tts = viewModel.ttsManager

    var currentMode by remember { 
        mutableStateOf(if (mode == "123") TracingMode.Numbers else TracingMode.ABCs) 
    }
    
    val alphabetItems = remember { contentRepository.getTracingLetters() }
    val numberItems = remember { contentRepository.getTracingNumbers() }
    
    val items = remember(currentMode) {
        when (currentMode) {
            TracingMode.ABCs -> alphabetItems
            TracingMode.Numbers -> numberItems
            TracingMode.Random -> (alphabetItems + numberItems).shuffled()
        }
    }
    
    var currentIndex by remember(currentMode) { mutableIntStateOf(0) }
    val currentItem = items[currentIndex]
    
    val currentPhonics = currentItem.phonicWord to currentItem.emoji

    val path = remember(currentIndex, currentMode) { Path() }
    val userStrokes = remember(currentIndex, currentMode) { mutableStateListOf<List<Offset>>() }
    val rainbowColors = listOf(
        Color(0xFFE11D48), Color(0xFFFB923C), Color(0xFFFACC15),
        Color(0xFF4ADE80), Color(0xFF60A5FA), Color(0xFFA855F7)
    )
    
    var drawTrigger by remember { mutableIntStateOf(0) }
    var isMatched by remember(currentIndex, currentMode) { mutableStateOf(false) }
    var showTryAgain by remember(currentIndex, currentMode) { mutableStateOf(false) }
    var showTutorial by remember(currentIndex, currentMode) { mutableStateOf(true) }
    var showCelebration by remember { mutableStateOf(false) }
    
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    val letterMask = remember(currentItem, canvasSize) {
        if (canvasSize.width <= 0 || canvasSize.height <= 0) return@remember null
        val bitmap = Bitmap.createBitmap(canvasSize.width, canvasSize.height, Bitmap.Config.ALPHA_8)
        val canvas = AndroidCanvas(bitmap)
        val paint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = with(density) { 488.sp.toPx() }
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val xPos = canvas.width / 2f
        val yPos = (canvas.height / 2f - (paint.descent() + paint.ascent()) / 2f)
        canvas.drawText(currentItem.char, xPos, yPos, paint)
        bitmap
    }

    fun checkTracing(): Boolean {
        val mask = letterMask ?: return false
        val allPoints = userStrokes.flatten()
        if (allPoints.isEmpty()) return false
        var pointsInside = 0
        val sampleRate = 5
        val pointsToCheck = allPoints.filterIndexed { index, _ -> index % sampleRate == 0 }
        if (pointsToCheck.isEmpty()) return false
        for (point in pointsToCheck) {
            val x = point.x.toInt()
            val y = point.y.toInt()
            if (x in 0 until mask.width && y in 0 until mask.height) {
                val pixel = mask.getPixel(x, y)
                if (android.graphics.Color.alpha(pixel) > 0) pointsInside++
            }
        }
        return (pointsInside.toFloat() / pointsToCheck.size) > 0.75f && pointsToCheck.size > 20
    }

    Scaffold(
        topBar = {
            LittleBudsTopAppBar(
                onBack = onBack,
                titleContent = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 20.dp, top = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ModeButton("ABCs", currentMode == TracingMode.ABCs) { currentMode = TracingMode.ABCs }
                        ModeButton("123s", currentMode == TracingMode.Numbers) { currentMode = TracingMode.Numbers }
                        ModeButton("Random", currentMode == TracingMode.Random) { currentMode = TracingMode.Random }
                    }
                }
            )
        },
        containerColor = Color(0xFFEFF6FF)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FeedbackHeader(isMatched, showTryAgain)
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(32.dp))
                        .onSizeChanged { canvasSize = it }
                        .pointerInput(currentIndex, isMatched, currentMode) {
                            if (!isMatched) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        path.moveTo(offset.x, offset.y)
                                        userStrokes.add(listOf(offset))
                                        drawTrigger++; showTryAgain = false; showTutorial = false
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        path.lineTo(change.position.x, change.position.y)
                                        val lastStroke = userStrokes.lastOrNull()?.toMutableList() ?: mutableListOf()
                                        lastStroke.add(change.position)
                                        if (userStrokes.isNotEmpty()) {
                                            userStrokes[userStrokes.size - 1] = lastStroke
                                        } else {
                                            userStrokes.add(lastStroke)
                                        }
                                        drawTrigger++
                                    }
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (!isMatched) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val paint = Paint().apply {
                                color = android.graphics.Color.LTGRAY
                                textSize = 488.sp.toPx()
                                typeface = Typeface.DEFAULT_BOLD
                                textAlign = Paint.Align.CENTER
                                isAntiAlias = true
                            }
                            
                            val outlinePaint = Paint().apply {
                                color = android.graphics.Color.DKGRAY
                                textSize = 488.sp.toPx()
                                typeface = Typeface.DEFAULT_BOLD
                                textAlign = Paint.Align.CENTER
                                style = Paint.Style.STROKE
                                strokeWidth = 3f
                                isAntiAlias = true
                            }

                            val xPos = size.width / 2f
                            val yPos = (size.height / 2f - (paint.descent() + paint.ascent()) / 2f)
                            
                            drawContext.canvas.nativeCanvas.drawText(currentItem.char, xPos, yPos, outlinePaint)
                            drawContext.canvas.nativeCanvas.drawText(currentItem.char, xPos, yPos, paint)
                        }
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawTrigger
                        if (!isMatched) {
                            var pointIndex = 0
                            userStrokes.forEach { stroke ->
                                for (i in 0 until stroke.size - 1) {
                                    val colorIndex = (pointIndex / 5) % rainbowColors.size
                                    drawLine(
                                        color = rainbowColors[colorIndex],
                                        start = stroke[i],
                                        end = stroke[i + 1],
                                        strokeWidth = 60f,
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                    pointIndex++
                                }
                                pointIndex++
                            }
                        } else {
                            drawPath(
                                path = path,
                                color = Color(0xFF22C55E),
                                style = Stroke(width = 60f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                            )
                        }
                    }

                    if (isMatched) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = currentPhonics.second, fontSize = 120.sp)
                            Text(
                                text = "${currentItem.char} is for ${currentPhonics.first}!",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SkyBlueDark,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (showTutorial && !isMatched) {
                        TracingTutorialAnimation(canvasSize, currentItem.char)
                    }
                }
                
                TracingControls(
                    isMatched = isMatched,
                    onClear = {
                        path.reset(); userStrokes.clear(); drawTrigger++; showTryAgain = false; showTutorial = true
                    },
                    onSkip = {
                        currentIndex = (currentIndex + 1) % items.size
                    },
                    onDone = {
                        if (checkTracing()) {
                            isMatched = true
                            showCelebration = true
                            tts.speak("${currentItem.char} is for ${currentPhonics.first}!", SpeechPriority.MEDIUM)
                        } else {
                            showTryAgain = true
                            tts.speak("Try again, you can do it!", SpeechPriority.LOW)
                        }
                    },
                    onNext = {
                        currentIndex = (currentIndex + 1) % items.size
                    }
                )
            }

            SuccessCelebration(
                isVisible = showCelebration,
                onFinished = { showCelebration = false }
            )
        }
    }
}

@Composable
fun TracingTutorialAnimation(size: IntSize, char: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "fingerTutorial")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(3000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "progress"
    )

    if (size.width > 0) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        
        val pos = when (char) {
            "O", "0", "C", "Q" -> { 
                val radius = 180f
                Offset(
                    centerX + (Math.cos(progress * 2 * Math.PI) * radius).toFloat(),
                    centerY + (Math.sin(progress * 2 * Math.PI) * radius).toFloat()
                )
            }
            "1", "I", "l", "7" -> {
                Offset(centerX, centerY - 220f + (440f * progress))
            }
            "A", "V", "M", "W", "N" -> {
                val xOffset = -180f + (360f * progress)
                val yOffset = if (progress < 0.5f) -220f + (440f * progress * 2) else 220f - (440f * (progress - 0.5f) * 2)
                Offset(centerX + xOffset, centerY + yOffset)
            }
            "H", "E", "F", "L" -> {
                if (progress < 0.4f) {
                    Offset(centerX - 120f, centerY - 220f + (440f * (progress / 0.4f)))
                } else {
                    Offset(centerX - 120f + (240f * ((progress - 0.4f) / 0.6f)), centerY)
                }
            }
            "X", "Y" -> {
                val x = -150f + (300f * progress)
                val y = -200f + (400f * progress)
                Offset(centerX + x, centerY + y)
            }
            else -> {
                // S-curve for others
                val xOffset = (Math.sin(progress * 2 * Math.PI) * 120f).toFloat()
                Offset(centerX + xOffset, centerY - 220f + (440f * progress))
            }
        }

        Icon(
            imageVector = Icons.Default.TouchApp,
            contentDescription = null,
            modifier = Modifier
                .offset {
                    IntOffset(
                        (pos.x - 24.dp.toPx()).roundToInt(),
                        (pos.y - 24.dp.toPx()).roundToInt()
                    )
                }
                .size(48.dp)
                .alpha(0.7f),
            tint = Color(0xFF3B82F6)
        )
    }
}

@Composable
fun ModeButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
            contentColor = if (isSelected) Color.White else Color.DarkGray
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.height(48.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FeedbackHeader(isMatched: Boolean, showTryAgain: Boolean) {
    if (isMatched) {
        Text("Great Job! 👏🌟", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF22C55E))
    } else if (showTryAgain) {
        Text("Stay inside the lines! ✍️", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
    } else {
        Text("Trace exactly on the item!", fontSize = 20.sp, color = Color(0xFF1E40AF))
    }
}

@Composable
fun TracingControls(isMatched: Boolean, onClear: () -> Unit, onSkip: () -> Unit, onDone: () -> Unit, onNext: () -> Unit) {
    Row(modifier = Modifier.padding(vertical = 24.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        if (isMatched) {
            Button(onClick = onNext, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)), modifier = Modifier.fillMaxWidth(0.9f).height(64.dp)) {
                Text("Next Item", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Button(onClick = onClear, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray), modifier = Modifier.weight(1f).height(64.dp)) {
                Text("Clear", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(onClick = onSkip, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF60A5FA)), modifier = Modifier.weight(1f).height(64.dp)) {
                Text("Skip", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(onClick = onDone, modifier = Modifier.weight(1f).height(64.dp)) {
                Text("Done!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
