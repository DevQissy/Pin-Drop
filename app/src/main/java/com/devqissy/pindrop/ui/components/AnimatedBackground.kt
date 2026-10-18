package com.devqissy.pindrop.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.devqissy.pindrop.data.model.FloatingOrb
import com.devqissy.pindrop.ui.theme.DarkBackground
import com.devqissy.pindrop.ui.theme.LightBackground
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

data class FloatingOrbWithAlpha(
    val radius: Float,
    val color: Color,
    val centerXFraction: Float,
    val centerYFraction: Float,
    val xFrequency: Float,
    val yFrequency: Float,
    val xAmplitude: Float,
    val yAmplitude: Float,
    val alpha: Float = 0.5f
)

@Composable
fun rememberFloatingOrbs(isDarkTheme: Boolean): List<Any> {
    return remember(isDarkTheme) {
        if (isDarkTheme) {
            listOf(
                FloatingOrb(180f, Color(0xFFE60023), 0.8f, 0.2f, 0.04f, 0.03f, 0.6f, 0.45f),
                FloatingOrb(220f, Color(0xFF8B0000), 0.2f, 0.75f, 0.05f, 0.04f, 0.5f, 0.55f),
                FloatingOrb(140f, Color(0xFFFF4D4D), 0.5f, 0.5f, 0.06f, 0.05f, 0.7f, 0.4f)
            )
        } else {
            listOf(
                FloatingOrbWithAlpha(160f, Color(0xFFE60023), 0.8f, 0.2f, 0.04f, 0.03f, 0.6f, 0.45f, 0.25f),
                FloatingOrbWithAlpha(200f, Color(0xFF9C27B0), 0.2f, 0.75f, 0.05f, 0.04f, 0.5f, 0.55f, 0.2f),
                FloatingOrbWithAlpha(130f, Color(0xFFFF4D4D), 0.5f, 0.5f, 0.06f, 0.05f, 0.7f, 0.4f, 0.22f)
            )
        }
    }
}

@Composable
fun AnimatedBackground(isDarkTheme: Boolean) {
    val orbs = rememberFloatingOrbs(isDarkTheme)
    val configuration = LocalConfiguration.current

    val infiniteTransition = rememberInfiniteTransition(label = "LiquidFluidTransition")

    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 25000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FluidTime"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) DarkBackground else LightBackground)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(75.dp)
        ) {
            orbs.forEach { orb ->
                when (orb) {
                    is FloatingOrb -> {
                        val bx = orb.centerXFraction * size.width
                        val by = orb.centerYFraction * size.height

                        val ox = orb.xAmplitude * size.width * sin(time * orb.xFrequency)
                        val oy = orb.yAmplitude * size.height * cos(time * orb.yFrequency)
                        val center = Offset(bx + ox, by + oy)

                        drawCircle(
                            color = orb.color.copy(alpha = 0.5f),
                            radius = orb.radius.dp.toPx(),
                            center = center
                        )
                    }
                    is FloatingOrbWithAlpha -> {
                        val bx = orb.centerXFraction * size.width
                        val by = orb.centerYFraction * size.height

                        val ox = orb.xAmplitude * size.width * sin(time * orb.xFrequency)
                        val oy = orb.yAmplitude * size.height * cos(time * orb.yFrequency)
                        val center = Offset(bx + ox, by + oy)

                        drawCircle(
                            color = orb.color.copy(alpha = orb.alpha),
                            radius = orb.radius.dp.toPx(),
                            center = center
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDarkTheme) {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.45f)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.3f)
                            )
                        )
                    }
                )
        )
    }
}