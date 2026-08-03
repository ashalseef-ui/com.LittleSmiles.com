package com.LittleSmiles.com.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * "Modern Sticker-Craft" Design System.
 * This provides a playful, high-contrast "cut-out" look inspired by stickers.
 */

@Immutable
data class StickerCraftColors(
    val border: Color = Color(0xFF1F2937), // Dark Slate/Black
    val shadow: Color = Color(0xFF1F2937),
    val paperWhite: Color = Color.White,
    val success: Color = SuccessGreen,
    val error: Color = ErrorRed,
    val palettes: List<Color> = listOf(
        RainbowRed, RainbowOrange, RainbowYellow, RainbowGreen, RainbowBlue, RainbowViolet
    )
)

@Immutable
data class StickerCraftShapes(
    val cornerRadius: Dp = 16.dp,
    val borderWidth: Dp = 2.5.dp,
    val shadowOffset: Dp = 5.dp
)

val LocalStickerColors = staticCompositionLocalOf { StickerCraftColors() }
val LocalStickerShapes = staticCompositionLocalOf { StickerCraftShapes() }

object StickerTheme {
    val colors: StickerCraftColors
        @Composable
        @ReadOnlyComposable
        get() = LocalStickerColors.current

    val shapes: StickerCraftShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalStickerShapes.current
}

/**
 * Reusable Modifier to apply the "Modern Sticker-Craft" look to any component.
 */
fun Modifier.stickerStyle(
    backgroundColor: Color = Color.White,
    borderColor: Color = Color(0xFF1F2937),
    borderWidth: Dp = 2.5.dp,
    shadowOffset: Dp = 5.dp,
    cornerRadius: Dp = 16.dp
): Modifier = this
    .drawBehind {
        val shadowPx = shadowOffset.toPx()
        val cornerPx = cornerRadius.toPx()
        // Draw the hard offset shadow (the "sticker depth")
        drawRoundRect(
            color = borderColor,
            topLeft = Offset(shadowPx, shadowPx),
            size = size,
            cornerRadius = CornerRadius(cornerPx, cornerPx)
        )
    }
    .background(backgroundColor, RoundedCornerShape(cornerRadius))
    .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))
    .padding(2.dp) // Subtle internal breathing room

@Composable
fun StickerCraftTheme(
    colors: StickerCraftColors = StickerCraftColors(),
    shapes: StickerCraftShapes = StickerCraftShapes(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalStickerColors provides colors,
        LocalStickerShapes provides shapes
    ) {
        content()
    }
}
