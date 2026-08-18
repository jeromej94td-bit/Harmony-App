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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
                alpha = 0.09f + Random.nextFloat() * 0.11f
            )
        }
    }

    val animTime = remember { Animatable(0f) }
    val auroraTransition = rememberInfiniteTransition(label = "harmony_aurora")
    val auroraRotation by auroraTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(22000, easing = LinearEasing), RepeatMode.Restart),
        label = "aurora_rotation"
    )
    val auroraPulse by auroraTransition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "aurora_pulse"
    )

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

            // Native Compose counterpart of harmony_aurora_glass.svg.
            // It keeps the same effect animated on Android even when SVG SMIL is unavailable.
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFB86CFF).copy(alpha = 0.16f * auroraPulse), Color.Transparent),
                    center = Offset(width * 0.5f, height * 0.33f),
                    radius = width * 0.44f
                ),
                center = Offset(width * 0.5f, height * 0.33f),
                radius = width * 0.44f
            )
            val portalCenter = Offset(width * 0.5f, height * 0.33f)
            val portalRadius = width * 0.29f
            listOf(0f to 0.24f, 46f to 0.14f, 92f to 0.08f).forEach { (offset, alpha) ->
                drawArc(
                    brush = Brush.sweepGradient(listOf(Color(0xFF66E8FF), Color(0xFFB56BFF), Color(0xFFFF5EAF), Color(0xFF66E8FF))),
                    startAngle = auroraRotation + offset,
                    sweepAngle = 230f,
                    useCenter = false,
                    topLeft = Offset(portalCenter.x - portalRadius, portalCenter.y - portalRadius),
                    size = androidx.compose.ui.geometry.Size(portalRadius * 2f, portalRadius * 2f),
                    alpha = alpha * auroraPulse,
                    style = Stroke(width = if (offset == 0f) 3.5f else 1.5f, cap = StrokeCap.Round)
                )
            }

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
