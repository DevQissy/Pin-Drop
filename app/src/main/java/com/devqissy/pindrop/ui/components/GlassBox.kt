package com.devqissy.pindrop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GlassBox(
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    backgroundColor: Color? = null,
    content: @Composable () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    val finalBorderColor = remember(borderColor, isDarkTheme) {
        borderColor ?: if (isDarkTheme) {
            Color.White.copy(alpha = 0.14f)
        } else {
            Color.Black.copy(alpha = 0.1f)
        }
    }

    val finalBackgroundColor = remember(backgroundColor, isDarkTheme) {
        backgroundColor ?: if (isDarkTheme) {
            Color.White.copy(alpha = 0.05f)
        } else {
            Color.Black.copy(alpha = 0.03f)
        }
    }

    val glassBorderBrush = remember(finalBorderColor) {
        Brush.linearGradient(
            colors = listOf(
                finalBorderColor,
                finalBorderColor.copy(alpha = 0.01f)
            )
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(finalBackgroundColor)
            .border(1.dp, glassBorderBrush, RoundedCornerShape(24.dp))
    ) {
        content()
    }
}