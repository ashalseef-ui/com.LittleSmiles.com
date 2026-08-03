package com.LittleSmiles.com.ui.features.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LittleSmiles.com.ui.theme.stickerStyle

/**
 * Reusable "Sticker-Craft" Card that replaces standard Surface/Card components.
 */
@Composable
fun StickerCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .stickerStyle(backgroundColor = backgroundColor)
            .padding(16.dp),
        content = content
    )
}

/**
 * A standardized header for sections within the Parental Hub.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF475569) // LabelBlue
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            color = color,
            letterSpacing = 0.5.sp
        ),
        modifier = modifier.padding(vertical = 8.dp)
    )
}

/**
 * A Sticker-styled button for consistent actions.
 */
@Composable
fun StickerButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFFFB923C), // SliderOrange
    contentColor: Color = Color.White
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent, // FIX: Transparent surface to let sticker background show
        modifier = modifier.stickerStyle(
            backgroundColor = containerColor,
            shadowOffset = 3.dp // Smaller shadow for buttons
        )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = contentColor
                )
            )
        }
    }
}
