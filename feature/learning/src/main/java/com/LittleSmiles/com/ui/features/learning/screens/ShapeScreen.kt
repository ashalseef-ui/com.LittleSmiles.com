package com.LittleSmiles.com.ui.features.learning.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.LittleSmiles.com.core.domain.model.LearningActivityType
import com.LittleSmiles.com.core.domain.model.Shape
import com.LittleSmiles.com.core.domain.model.ShapeType
import com.LittleSmiles.com.ui.components.LearningActivityTemplate
import com.LittleSmiles.com.ui.components.LearningGridContent
import com.LittleSmiles.com.ui.features.learning.ShapeViewModel

@Composable
fun ShapeScreen(
    onBack: () -> Unit,
    viewModel: ShapeViewModel = hiltViewModel()
) {
    val shapes = remember { viewModel.contentRepository.getShapes() }

    LearningActivityTemplate(
        activity = LearningActivityType.Shapes,
        items = shapes,
        tts = viewModel.ttsManager,
        onBack = onBack,
        getDisplayName = { it.name }
    ) { _, onItemClick ->
        LearningGridContent(
            items = shapes,
            onItemClick = onItemClick,
            cardContent = { shape ->
                val shapePath = remember(shape.type) { getPathForShape(shape.type) }
                val shapeColor = Color(shape.colorHex.toInt())
                
                Canvas(modifier = Modifier.size(60.dp)) {
                    val scaleX = size.width / 100f
                    val scaleY = size.height / 100f
                    drawContext.canvas.save()
                    drawContext.canvas.scale(scaleX, scaleY)
                    drawPath(path = shapePath, color = shapeColor, style = Fill)
                    drawContext.canvas.restore()
                }
            }
        )
    }
}

private fun getPathForShape(type: ShapeType): Path {
    return Path().apply {
        when (type) {
            ShapeType.SQUARE -> {
                moveTo(0f, 0f)
                lineTo(100f, 0f)
                lineTo(100f, 100f)
                lineTo(0f, 100f)
                close()
            }
            ShapeType.CIRCLE -> {
                addOval(Rect(0f, 0f, 100f, 100f))
            }
            ShapeType.TRIANGLE -> {
                moveTo(50f, 0f)
                lineTo(100f, 100f)
                lineTo(0f, 100f)
                close()
            }
            ShapeType.STAR -> {
                moveTo(50f, 0f)
                lineTo(63f, 38f)
                lineTo(100f, 38f)
                lineTo(70f, 61f)
                lineTo(82f, 100f)
                lineTo(50f, 75f)
                lineTo(18f, 100f)
                lineTo(30f, 61f)
                lineTo(0f, 38f)
                lineTo(37f, 38f)
                close()
            }
            ShapeType.RECTANGLE -> {
                moveTo(0f, 25f)
                lineTo(100f, 25f)
                lineTo(100f, 75f)
                lineTo(0f, 75f)
                close()
            }
            ShapeType.HEART -> {
                moveTo(50f, 30f)
                cubicTo(50f, 10f, 90f, 10f, 90f, 40f)
                cubicTo(90f, 70f, 50f, 95f, 50f, 95f)
                cubicTo(50f, 95f, 10f, 70f, 10f, 40f)
                cubicTo(10f, 10f, 50f, 10f, 50f, 30f)
                close()
            }
            ShapeType.DIAMOND -> {
                moveTo(50f, 0f)
                lineTo(100f, 50f)
                lineTo(50f, 100f)
                lineTo(0f, 50f)
                close()
            }
            ShapeType.OVAL -> {
                addOval(Rect(10f, 25f, 90f, 75f))
            }
        }
    }
}
