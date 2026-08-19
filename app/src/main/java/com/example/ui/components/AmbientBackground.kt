package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.ui.theme.HarmonyBg
import com.example.ui.theme.HarmonyBgGradientEnd
import com.example.ui.theme.HarmonyBgGradientMid
import com.example.ui.theme.HarmonyBgGradientStart
import com.example.ui.theme.HarmonyPink
import com.example.ui.theme.HarmonyPurple
import kotlin.math.sin
import kotlin.random.Random

private data class FloatingHeart(
    val xRatio: Float,
    val yRatio: Float,
    val speed: Float,
    val size: Float,
    val phase: Float,
    val alpha: Float,
    val color: Color,
    val outlined: Boolean
)

private data class Starlight(
    val xRatio: Float,
    val yRatio: Float,
    val speed: Float,
    val radius: Float,
    val phase: Float
)

@Composable
fun AmbientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val hearts = remember {
        val random = Random(2608)
        val heartColors = listOf(
            Color(0xFFFF3F91),
            Color(0xFFFF70C6),
            Color(0xFFFF4FB3),
            Color(0xFFE95CFF)
        )
        List(20) { index ->
            val onLeft = index % 2 == 0
            FloatingHeart(
                xRatio = if (onLeft) 0.018f + random.nextFloat() * 0.115f else 0.867f + random.nextFloat() * 0.115f,
                yRatio = random.nextFloat() * 1.08f,
                // Integer lap counts keep the global animation phase perfectly closed.
                speed = if (index % 5 == 0) 2f else 1f,
                size = 22f + random.nextFloat() * 26f,
                phase = random.nextFloat() * 6.28f,
                alpha = 0.42f + random.nextFloat() * 0.34f,
                color = heartColors[index % heartColors.size],
                outlined = index % 4 == 0
            )
        }
    }
    val starlights = remember {
        val random = Random(1104)
        List(34) {
            Starlight(
                xRatio = random.nextFloat(),
                yRatio = random.nextFloat(),
                speed = if (it % 7 == 0) 2f else 1f,
                radius = 0.7f + random.nextFloat() * 1.7f,
                phase = random.nextFloat() * 6.28f
            )
        }
    }

    val auroraTransition = rememberInfiniteTransition(label = "harmony_aurora")
    val ambientPhase by auroraTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(36000, easing = LinearEasing), RepeatMode.Restart),
        label = "ambient_particle_phase"
    )
    val auroraRotation by auroraTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(22000, easing = LinearEasing), RepeatMode.Restart),
        label = "aurora_rotation"
    )
    val counterRotation by auroraTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(31000, easing = LinearEasing), RepeatMode.Restart),
        label = "aurora_counter_rotation"
    )
    val auroraPulse by auroraTransition.animateFloat(
        initialValue = 0.70f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "aurora_pulse"
    )

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
            val phase = ambientPhase

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(HarmonyPurple.copy(alpha = 0.24f), Color.Transparent),
                    center = Offset(width * 0.84f, -height * 0.08f),
                    radius = width * 0.95f
                ),
                center = Offset(width * 0.84f, -height * 0.08f),
                radius = width * 0.95f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(HarmonyPink.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(-width * 0.05f, height * 0.08f),
                    radius = width * 0.82f
                ),
                center = Offset(-width * 0.05f, height * 0.08f),
                radius = width * 0.82f
            )

            val ribbonDrift = sin(phase * TWO_PI) * width * 0.05f
            val auroraRibbon = Path().apply {
                moveTo(-width * 0.18f, height * 0.16f)
                cubicTo(
                    width * 0.18f + ribbonDrift,
                    height * 0.02f,
                    width * 0.58f - ribbonDrift,
                    height * 0.32f,
                    width * 1.18f,
                    height * 0.12f
                )
            }
            drawPath(
                path = auroraRibbon,
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, Color(0xFF6BE7FF), Color(0xFFC36CFF), Color(0xFFFF5EAF), Color.Transparent)
                ),
                alpha = 0.075f * auroraPulse,
                style = Stroke(width = width * 0.16f, cap = StrokeCap.Round)
            )

            val portalCenter = Offset(width * 0.50f, height * 0.33f)
            val portalRadius = width * 0.29f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFB86CFF).copy(alpha = 0.18f * auroraPulse), Color.Transparent),
                    center = portalCenter,
                    radius = width * 0.46f
                ),
                center = portalCenter,
                radius = width * 0.46f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF130820).copy(alpha = 0.66f), Color.Transparent),
                    center = portalCenter,
                    radius = portalRadius * 0.80f
                ),
                center = portalCenter,
                radius = portalRadius * 0.80f
            )
            listOf(
                Triple(auroraRotation, 0.25f, 3.7f),
                Triple(counterRotation + 42f, 0.15f, 2.0f),
                Triple(auroraRotation + 96f, 0.09f, 1.4f)
            ).forEach { (rotation, alpha, strokeWidth) ->
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(Color(0xFF66E8FF), Color(0xFFB56BFF), Color(0xFFFF5EAF), Color(0xFFFF8A68), Color(0xFF66E8FF))
                    ),
                    startAngle = rotation,
                    sweepAngle = 232f,
                    useCenter = false,
                    topLeft = Offset(portalCenter.x - portalRadius, portalCenter.y - portalRadius),
                    size = Size(portalRadius * 2f, portalRadius * 2f),
                    alpha = alpha * auroraPulse,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            starlights.forEach { star ->
                val travel = (star.yRatio + phase * star.speed) % 1f
                val currentYRatio = 1.04f - travel * 1.08f
                val twinkle = 0.28f + ((sin(phase * TWO_PI + star.phase) + 1f) * 0.5f) * 0.58f
                val center = Offset(star.xRatio * width, currentYRatio * height)
                drawCircle(Color.White.copy(alpha = twinkle * 0.24f), radius = star.radius * 2.6f, center = center)
                drawCircle(Color.White.copy(alpha = twinkle), radius = star.radius, center = center)
            }

            hearts.forEach { heart ->
                val travel = (heart.yRatio + phase * heart.speed) % 1f
                // Each heart completes the whole journey. Its reset happens outside the viewport.
                val currentYRatio = 1.12f - travel * 1.24f
                val drift = sin(travel * TWO_PI + heart.phase) * width * 0.024f
                val center = Offset(heart.xRatio * width + drift, currentYRatio * height)
                val rotation = sin(travel * TWO_PI + heart.phase) * 13f
                drawHeartParticle(
                    center = center,
                    size = heart.size,
                    color = heart.color,
                    alpha = heart.alpha,
                    rotation = rotation,
                    outlined = heart.outlined
                )
            }
        }

        content()
    }
}

private const val TWO_PI = 6.2831855f

private fun DrawScope.drawHeartParticle(
    center: Offset,
    size: Float,
    color: Color,
    alpha: Float,
    rotation: Float,
    outlined: Boolean
) {
    val heart = Path().apply {
        moveTo(center.x, center.y - size * 0.22f)
        cubicTo(
            center.x - size * 0.62f,
            center.y - size * 0.78f,
            center.x - size,
            center.y + size * 0.06f,
            center.x,
            center.y + size * 0.88f
        )
        cubicTo(
            center.x + size,
            center.y + size * 0.06f,
            center.x + size * 0.62f,
            center.y - size * 0.78f,
            center.x,
            center.y - size * 0.22f
        )
        close()
    }
    rotate(rotation, pivot = center) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = alpha * 0.28f), Color.Transparent),
                center = center,
                radius = size * 1.65f
            ),
            radius = size * 1.65f,
            center = center
        )
        if (outlined) {
            drawPath(
                path = heart,
                color = color.copy(alpha = alpha * 0.26f),
                style = Stroke(width = size * 0.30f, cap = StrokeCap.Round)
            )
            drawPath(
                path = heart,
                color = color.copy(alpha = alpha),
                style = Stroke(width = (size * 0.105f).coerceAtLeast(2f), cap = StrokeCap.Round)
            )
        } else {
            drawPath(path = heart, color = color.copy(alpha = alpha * 0.90f))
            drawPath(
                path = heart,
                color = Color.White.copy(alpha = alpha * 0.72f),
                style = Stroke(width = (size * 0.06f).coerceAtLeast(1.5f), cap = StrokeCap.Round)
            )
        }
    }
}
