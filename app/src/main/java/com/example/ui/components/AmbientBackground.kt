package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.example.ui.theme.HarmonyBg
import com.example.ui.theme.HarmonyBgGradientEnd
import com.example.ui.theme.HarmonyBgGradientMid
import com.example.ui.theme.HarmonyBgGradientStart
import com.example.ui.theme.HarmonyPink
import com.example.ui.theme.HarmonyPurple
import kotlin.random.Random

private data class Particle(
    var xRatio: Float,
    var yRatio: Float,
    var speed: Float,
    var size: Float,
    var glyph: String,
    var phase: Float,
    var alpha: Float
)

@Composable
fun AmbientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val particles = remember {
        val glyphs = listOf("💖", "💕", "💗", "💞", "✨")
        List(12) { i ->
            Particle(
                xRatio = Random.nextFloat(),
                yRatio = Random.nextFloat(),
                speed = 0.0004f + Random.nextFloat() * 0.0006f,
                size = 32f + Random.nextFloat() * 24f,
                glyph = glyphs[i % glyphs.size],
                phase = Random.nextFloat() * 6.28f,
                alpha = 0.035f + Random.nextFloat() * 0.055f
            )
        }
    }

    val animTime = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animTime.animateTo(
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(100000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        HarmonyBgGradientStart,
                        HarmonyBgGradientMid,
                        HarmonyBg,
                        HarmonyBgGradientEnd
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val t = animTime.value

            // Draw radial glows
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(HarmonyPurple.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(width * 0.8f, -height * 0.1f),
                    radius = width * 0.9f
                ),
                center = Offset(width * 0.8f, -height * 0.1f),
                radius = width * 0.9f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(HarmonyPink.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = width * 0.8f
                ),
                center = Offset(0f, 0f),
                radius = width * 0.8f
            )

            // Draw floating particles
            particles.forEach { p ->
                p.yRatio -= p.speed
                if (p.yRatio < -0.05f) {
                    p.yRatio = 1.05f
                    p.xRatio = Random.nextFloat()
                }

                val currentX = (p.xRatio + kotlin.math.sin(t * 0.05f + p.phase) * 0.04f) * width
                val currentY = p.yRatio * height

                if (p.xRatio < 0.16f || p.xRatio > 0.84f) {
                    drawIntoCanvas { canvas ->
                        val paint = android.graphics.Paint().apply {
                            textSize = p.size
                            color = Color.White.toArgb()
                            alpha = (p.alpha * 255).toInt().coerceIn(0, 255)
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        canvas.nativeCanvas.drawText(p.glyph, currentX, currentY, paint)
                    }
                }
            }
        }

        content()
    }
}
