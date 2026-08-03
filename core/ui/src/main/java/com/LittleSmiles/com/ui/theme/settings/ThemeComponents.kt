package com.LittleSmiles.com.ui.theme.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Neumorphic Colors
object NeuColors {
    val Background = Color(0xFF1B1F2B)
    val Surface = Color(0xFF23293A)
    val SurfaceHighlight = Color(0xFF31384D)
    val DeepShadow = Color.Black.copy(alpha = 0.55f)
    val LightShadow = Color.White.copy(alpha = 0.08f)
    
    val Blue = Color(0xFF5B7CFF)
    val Cyan = Color(0xFF38D6FF)
    val Purple = Color(0xFF8B6CFF)
    val Pink = Color(0xFFFF67C8)
}

fun Modifier.neumorphicSurface(
    borderRadius: Dp = 22.dp,
    elevation: Dp = 6.dp,
    isInset: Boolean = false,
    backgroundColor: Color = NeuColors.Surface
) = this.drawBehind {
    val shadowBlur = elevation.toPx()
    val shadowOffset = (elevation / 2).toPx()

    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.TRANSPARENT
        }

        if (!isInset) {
            // Raised Shadow - Bottom Right (Dark)
            paint.setShadowLayer(shadowBlur, shadowOffset, shadowOffset, NeuColors.DeepShadow.toArgb())
            canvas.nativeCanvas.drawRoundRect(
                0f, 0f, size.width, size.height,
                borderRadius.toPx(), borderRadius.toPx(),
                paint
            )

            // Raised Shadow - Top Left (Light)
            paint.setShadowLayer(shadowBlur, -shadowOffset, -shadowOffset, NeuColors.LightShadow.toArgb())
            canvas.nativeCanvas.drawRoundRect(
                0f, 0f, size.width, size.height,
                borderRadius.toPx(), borderRadius.toPx(),
                paint
            )
        } else {
            // Inset Shadow Simulation
            paint.setShadowLayer(shadowBlur, shadowOffset, shadowOffset, NeuColors.DeepShadow.toArgb())
            canvas.nativeCanvas.drawRoundRect(
                0f, 0f, size.width, size.height,
                borderRadius.toPx(), borderRadius.toPx(),
                paint
            )
        }
    }
}.background(
    if (isInset) Color(0xFF161A24) else backgroundColor, 
    RoundedCornerShape(borderRadius)
)

@Composable
fun NeumorphicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = NeuColors.Blue,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "scale")
    val elevation by animateFloatAsState(if (isPressed) 2f else 6f, label = "elevation")

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .neumorphicSurface(borderRadius = 22.dp, elevation = elevation.dp)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}

@Composable
fun NeumorphicSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    accentColor: Color = NeuColors.Cyan
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .neumorphicSurface(borderRadius = 10.dp, elevation = 3.dp, isInset = true),
        contentAlignment = Alignment.CenterStart
    ) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = accentColor,
                inactiveTrackColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        )
    }
}

@Composable
fun NeumorphicCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .neumorphicSurface(borderRadius = 28.dp, elevation = 8.dp)
            .padding(20.dp),
        content = content
    )
}
