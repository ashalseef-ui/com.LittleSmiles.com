package com.LittleSmiles.com.ui.features.drawing

import android.graphics.Bitmap
import android.graphics.Paint
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import java.io.OutputStream
import java.util.Random
import kotlin.math.roundToInt

enum class BrushType {
    SOLID, RAINBOW, GLITTER
}

data class PathData(
    val path: Path,
    val color: Color,
    val brushType: BrushType = BrushType.SOLID,
    val strokeWidth: Float = 15f,
    val alpha: Float = 1f
)

sealed class ColoringTemplate(val name: String) {
    object None : ColoringTemplate("None")
    object Star : ColoringTemplate("Star")
    object Heart : ColoringTemplate("Heart")
    object Face : ColoringTemplate("Face")
    object House : ColoringTemplate("House")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    var currentColor by remember { mutableStateOf(Color(0xFFFF0000)) }
    var currentBrushType by remember { mutableStateOf(BrushType.SOLID) }
    var strokeWidth by remember { mutableFloatStateOf(20f) }
    var currentAlpha by remember { mutableFloatStateOf(1f) }
    var selectedTemplate by remember { mutableStateOf<ColoringTemplate>(ColoringTemplate.None) }
    
    val paths = remember { mutableStateListOf<PathData>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var cursorPosition by remember { mutableStateOf<Offset?>(null) }

    val solidColors = listOf(
        Color(0xFFFF0000), Color(0xFFFFCC00), Color(0xFF00FF00), 
        Color(0xFF00CCFF), Color(0xFFFF00FF), Color(0xFF000000)
    )

    val glitterPalettes = listOf(
        listOf(Color(0xFFFFD700), Color(0xFFFF8C00), Color(0xFFFF0000)),
        listOf(Color(0xFF00FFFF), Color(0xFF1E90FF), Color(0xFF0000FF)),
        listOf(Color(0xFFEE82EE), Color(0xFFDA70D6), Color(0xFF8B008B))
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Magic Markers", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF0F9FF)
                ),
                modifier = Modifier.height(48.dp)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            // Drawing Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(renderEffect = null) // Layering optimization
                    .pointerInput(currentBrushType, currentColor, strokeWidth) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                cursorPosition = offset
                                currentPath = Path().apply { moveTo(offset.x, offset.y) }
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                cursorPosition = change.position
                                currentPath?.lineTo(change.position.x, change.position.y)
                                // Triggering redraw by updating a simple state if needed, 
                                // but recomposing the whole screen for every point is heavy.
                                // In production, we'd use a background bitmap for old paths.
                            },
                            onDragEnd = {
                                cursorPosition = null
                                currentPath?.let {
                                    paths.add(PathData(it, currentColor, currentBrushType, strokeWidth, currentAlpha))
                                }
                                currentPath = null
                            }
                        )
                    }
            ) {
                drawColoringTemplate(selectedTemplate)
                paths.forEach { drawPathData(it) }
                currentPath?.let {
                    drawPathData(PathData(it, currentColor, currentBrushType, strokeWidth, currentAlpha))
                }
            }

            // 3D Marker Cursor
            cursorPosition?.let { pos ->
                val markerHeightPx = with(density) { 120.dp.toPx() }
                val markerWidthPx = with(density) { 40.dp.toPx() }
                
                Box(
                    modifier = Modifier
                        .offset { 
                            IntOffset(
                                (pos.x - markerWidthPx / 2).roundToInt(), 
                                (pos.y - markerHeightPx).roundToInt()
                            ) 
                        }
                        .zIndex(100f)
                ) {
                    RealisticMarker(
                        color = currentColor,
                        brushType = currentBrushType,
                        modifier = Modifier.size(width = 40.dp, height = 120.dp),
                        tiltAngle = -10f,
                        alpha = currentAlpha
                    )
                }
            }

            // Top Rail - Marker Selection
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(8.dp)
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF1F5F9).copy(alpha = 0.8f))
                    .padding(horizontal = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                solidColors.forEach { color ->
                    MarkerItem(color, BrushType.SOLID, (currentBrushType == BrushType.SOLID && currentColor == color), 30.dp, 70.dp) {
                        currentColor = color
                        currentBrushType = BrushType.SOLID
                    }
                }
                MarkerItem(Color.White, BrushType.RAINBOW, (currentBrushType == BrushType.RAINBOW), 30.dp, 70.dp) {
                    currentBrushType = BrushType.RAINBOW
                }
                glitterPalettes.forEach { palette ->
                    val baseColor = palette[0]
                    MarkerItem(baseColor, BrushType.GLITTER, (currentBrushType == BrushType.GLITTER && currentColor == baseColor), 30.dp, 70.dp) {
                        currentColor = baseColor
                        currentBrushType = BrushType.GLITTER
                    }
                }
            }

            // Left Rail - Character Selection
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(8.dp)
                    .width(60.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF1F5F9).copy(alpha = 0.8f))
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TemplateButton(Icons.Default.Delete, selectedTemplate == ColoringTemplate.None) {
                    selectedTemplate = ColoringTemplate.None
                }
                TemplateButton(Icons.Default.Star, selectedTemplate == ColoringTemplate.Star) {
                    selectedTemplate = ColoringTemplate.Star
                }
                TemplateButton(Icons.Default.Favorite, selectedTemplate == ColoringTemplate.Heart) {
                    selectedTemplate = ColoringTemplate.Heart
                }
                TemplateButton(Icons.Default.Face, selectedTemplate == ColoringTemplate.Face) {
                    selectedTemplate = ColoringTemplate.Face
                }
                TemplateButton(Icons.Default.Home, selectedTemplate == ColoringTemplate.House) {
                    selectedTemplate = ColoringTemplate.House
                }
            }

            // Bottom Rail - Size and Tools
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
                    .fillMaxWidth()
                    .height(70.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF1F5F9).copy(alpha = 0.8f))
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { paths.clear() },
                    modifier = Modifier.size(52.dp).background(Color.White, CircleShape).border(1.dp, Color(0xFFE2E8F0), CircleShape)
                ) {
                    Icon(Icons.Default.Delete, "Clear", tint = Color.Red)
                }

                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Slider(
                        value = strokeWidth,
                        onValueChange = { strokeWidth = it },
                        valueRange = 5f..120f,
                        modifier = Modifier.height(24.dp)
                    )
                    Slider(
                        value = currentAlpha,
                        onValueChange = { currentAlpha = it },
                        valueRange = 0.1f..1f,
                        modifier = Modifier.height(24.dp)
                    )
                }

                IconButton(
                    onClick = { saveCanvasToGallery(context, paths) },
                    modifier = Modifier.size(52.dp).background(Color.White, CircleShape).border(1.dp, Color(0xFFE2E8F0), CircleShape)
                ) {
                    Icon(Icons.Default.Image, "Save", tint = Color(0xFF0EA5E9))
                }
            }
        }
    }
}

@Composable
fun TemplateButton(icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(52.dp)
            .background(if (isSelected) Color(0xFFCBD5E1) else Color.White, CircleShape)
            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) Color(0xFF64748B) else Color(0xFFE2E8F0), CircleShape)
    ) {
        Icon(icon, null, tint = if (isSelected) Color(0xFF1E293B) else Color(0xFF94A3B8))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawColoringTemplate(template: ColoringTemplate) {
    val center = Offset(size.width / 2, size.height / 2)
    val outlineColor = Color.LightGray.copy(alpha = 0.5f)
    val stroke = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    when (template) {
        is ColoringTemplate.Star -> {
            val starPath = Path().apply {
                val outerRadius = size.width * 0.35f
                val innerRadius = outerRadius * 0.4f
                for (i in 0 until 10) {
                    val angle = Math.toRadians(i * 36.0 - 90.0)
                    val radius = if (i % 2 == 0) outerRadius else innerRadius
                    val x = center.x + radius * Math.cos(angle).toFloat()
                    val y = center.y + radius * Math.sin(angle).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(starPath, outlineColor, style = stroke)
        }
        is ColoringTemplate.Heart -> {
            val heartPath = Path().apply {
                val w = size.width * 0.6f
                val h = size.width * 0.6f
                val x = center.x - w / 2
                val y = center.y - h / 2
                moveTo(x + w / 2, y + h * 0.25f)
                cubicTo(x + w * 0.2f, y, x, y + h * 0.5f, x + w / 2, y + h)
                cubicTo(x + w, y + h * 0.5f, x + w * 0.8f, y, x + w / 2, y + h * 0.25f)
            }
            drawPath(heartPath, outlineColor, style = stroke)
        }
        is ColoringTemplate.Face -> {
            drawCircle(outlineColor, radius = size.width * 0.3f, center = center, style = stroke)
            drawCircle(outlineColor, radius = 10f, center = Offset(center.x - 50f, center.y - 40f))
            drawCircle(outlineColor, radius = 10f, center = Offset(center.x + 50f, center.y - 40f))
            val mouthPath = Path().apply {
                val r = size.width * 0.15f
                moveTo(center.x - r, center.y + 40f)
                quadraticBezierTo(center.x, center.y + 120f, center.x + r, center.y + 40f)
            }
            drawPath(mouthPath, outlineColor, style = stroke)
        }
        is ColoringTemplate.House -> {
            val hPath = Path().apply {
                val w = size.width * 0.5f
                val h = size.width * 0.5f
                val x = center.x - w / 2
                val y = center.y - h / 4
                moveTo(x, y)
                lineTo(x + w, y)
                lineTo(x + w, y + h)
                lineTo(x, y + h)
                close()
                moveTo(x - 20f, y)
                lineTo(center.x, y - h * 0.6f)
                lineTo(x + w + 20f, y)
                close()
            }
            drawPath(hPath, outlineColor, style = stroke)
        }
        else -> {}
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPathData(data: PathData) {
    val brush = when (data.brushType) {
        BrushType.SOLID -> Brush.linearGradient(listOf(data.color, data.color))
        BrushType.RAINBOW -> Brush.linearGradient(
            listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
        )
        BrushType.GLITTER -> {
            Brush.linearGradient(
                0.0f to data.color, 0.3f to Color.White, 0.5f to data.color, 
                0.7f to Color.White.copy(alpha = 0.8f), 1.0f to data.color
            )
        }
    }
    drawPath(
        path = data.path, 
        brush = brush, 
        alpha = data.alpha,
        style = Stroke(width = data.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

@Composable
fun MarkerItem(color: Color, brushType: BrushType, isSelected: Boolean, width: androidx.compose.ui.unit.Dp = 40.dp, height: androidx.compose.ui.unit.Dp = 100.dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(width = width, height = height).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        RealisticMarker(color = color, brushType = brushType, isSelected = isSelected, modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun RealisticMarker(color: Color, brushType: BrushType, modifier: Modifier = Modifier, isSelected: Boolean = false, tiltAngle: Float = 0f, alpha: Float = 1f) {
    val bodyBrush = when (brushType) {
        BrushType.RAINBOW -> Brush.linearGradient(listOf(Color.Red, Color.Yellow, Color.Green, Color.Blue, Color.Magenta))
        BrushType.GLITTER -> Brush.linearGradient(listOf(color.copy(alpha = 0.8f), Color.White, color, color.copy(alpha = 0.6f)))
        else -> Brush.linearGradient(listOf(color.copy(alpha = 0.8f), color, color.copy(alpha = 0.6f)))
    }
    Canvas(modifier = modifier.graphicsLayer(rotationZ = tiltAngle).padding(if (isSelected) 0.dp else 2.dp)) {
        val w = size.width
        val h = size.height
        val bodyH = h * 0.65f
        val shoulderH = h * 0.08f
        val gripH = h * 0.17f
        drawRoundRect(brush = bodyBrush, size = Size(w, bodyH), cornerRadius = CornerRadius(8f, 8f))
        val shoulderPath = Path().apply {
            moveTo(0f, bodyH)
            lineTo(w, bodyH)
            lineTo(w * 0.85f, bodyH + shoulderH)
            lineTo(w * 0.15f, bodyH + shoulderH)
            close()
        }
        drawPath(shoulderPath, Color(0xFF222222))
        drawRect(color = Color.Black, topLeft = Offset(w * 0.15f, bodyH + shoulderH), size = Size(w * 0.7f, gripH))
        val tipPath = Path().apply {
            moveTo(w * 0.25f, bodyH + shoulderH + gripH)
            lineTo(w * 0.75f, bodyH + shoulderH + gripH)
            lineTo(w * 0.5f, h)
            close()
        }
        drawPath(tipPath, if (brushType == BrushType.RAINBOW) Color.Black else color.copy(alpha = alpha))
        if (brushType == BrushType.GLITTER) {
            val random = Random(color.toArgb().toLong())
            repeat(15) { drawCircle(Color.White.copy(alpha = 0.9f), radius = 2f, center = Offset(random.nextFloat() * w, random.nextFloat() * bodyH)) }
        }
        drawRect(Color.White.copy(alpha = 0.25f), topLeft = Offset(w * 0.15f, 0f), size = Size(w * 0.25f, bodyH))
        if (isSelected) {
            drawRoundRect(Color.White.copy(alpha = 0.5f), size = size, style = Stroke(width = 4f), cornerRadius = CornerRadius(10f, 10f))
        }
    }
}

private fun saveCanvasToGallery(context: android.content.Context, paths: List<PathData>) {
    try {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        val paint = Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE; strokeJoin = Paint.Join.ROUND; strokeCap = Paint.Cap.ROUND }
        val random = Random()
        paths.forEach { data ->
            paint.strokeWidth = data.strokeWidth
            when (data.brushType) {
                BrushType.RAINBOW -> paint.color = android.graphics.Color.HSVToColor(floatArrayOf(random.nextFloat() * 360, 1f, 1f))
                BrushType.GLITTER -> { paint.color = data.color.toArgb(); paint.strokeWidth = data.strokeWidth + 10f }
                else -> paint.color = data.color.toArgb()
            }
            paint.alpha = (data.alpha * 255).toInt()
            canvas.drawPath(data.path.asAndroidPath(), paint)
        }
        val filename = "LittleSmiles_Art_${System.currentTimeMillis()}.png"
        val values = android.content.ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LittleSmiles")
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            val out: OutputStream? = context.contentResolver.openOutputStream(it)
            out?.let { stream -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream); stream.close(); Toast.makeText(context, "Saved to Gallery!", Toast.LENGTH_SHORT).show() }
        } ?: throw Exception("Could not create MediaStore entry")
    } catch (e: Exception) { Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_LONG).show() }
}
