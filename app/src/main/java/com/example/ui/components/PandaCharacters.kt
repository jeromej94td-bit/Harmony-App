package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private enum class PandaMood { HAPPY, THINKING, SURPRISED, SAD }

/**
 * A production vector animation rather than a looping clip. Every moving value is periodic,
 * so the first and last frame are identical and the 22-second cycle never visibly snaps.
 */
@Composable
fun PandaCategoryIcon(
    categoryId: String,
    accent: Color,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp
) {
    val transition = rememberInfiniteTransition(label = "panda_$categoryId")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(22_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "panda_phase_$categoryId"
    )

    Canvas(modifier = modifier.then(Modifier.size(size))) {
        val cycle = phase * TWO_PI_PANDA
        val blink = blinkAmount(phase)
        val center = Offset(this.size.width * 0.5f, this.size.height * 0.46f)
        val radius = this.size.minDimension * 0.29f

        when (categoryId) {
            "wer" -> {
                val direction = sin(cycle)
                drawThoughtBubbles(center, radius, accent, direction)
                drawPandaHead(
                    center = center.copy(y = center.y + 2f),
                    radius = radius,
                    bow = false,
                    mood = PandaMood.THINKING,
                    blink = blink,
                    tilt = direction * 4f
                )
                drawPaw(
                    center = Offset(center.x + direction * radius * 0.38f, center.y + radius * 0.72f),
                    radius = radius * 0.29f,
                    rotation = -18f + direction * 20f
                )
            }

            "tot" -> {
                val balance = sin(cycle)
                drawPandaHead(center, radius, bow = true, mood = PandaMood.SURPRISED, blink = blink)
                drawPaw(
                    center = Offset(center.x - radius * 1.03f, center.y + radius * (0.38f - balance * 0.38f)),
                    radius = radius * 0.28f,
                    rotation = 24f
                )
                drawPaw(
                    center = Offset(center.x + radius * 1.03f, center.y + radius * (0.38f + balance * 0.38f)),
                    radius = radius * 0.28f,
                    rotation = -24f
                )
                drawChoiceSpark(Offset(center.x - radius * 1.06f, center.y - radius * (0.10f + balance * 0.38f)), accent)
                drawChoiceSpark(Offset(center.x + radius * 1.06f, center.y - radius * (0.10f - balance * 0.38f)), Color(0xFFFF6FAF))
            }

            "zust" -> {
                val firstHalf = phase < 0.5f
                val localPhase = if (firstHalf) phase * 2f else (phase - 0.5f) * 2f
                val nod = if (firstHalf) sin(localPhase * TWO_PI_PANDA) else 0f
                val shake = if (firstHalf) 0f else sin(localPhase * TWO_PI_PANDA)
                drawPandaHead(
                    center = Offset(center.x + shake * radius * 0.16f, center.y + nod * radius * 0.13f),
                    radius = radius,
                    bow = false,
                    mood = if (firstHalf) PandaMood.HAPPY else PandaMood.SURPRISED,
                    blink = blink,
                    tilt = shake * 7f
                )
                drawApprovalMark(center, radius, accent, approved = firstHalf)
            }

            "zeich" -> {
                drawPandaHead(center, radius, bow = true, mood = PandaMood.HAPPY, blink = blink, tilt = sin(cycle) * 2f)
                drawTinyPencil(center, radius, accent, sin(cycle) * radius * 0.16f)
            }

            "nie" -> {
                val shy = (sin(cycle) + 1f) * 0.5f
                drawPandaHead(center.copy(y = center.y + shy * 2f), radius, bow = false, mood = PandaMood.THINKING, blink = blink)
                drawPaw(Offset(center.x - radius * 0.44f, center.y + radius * (0.62f - shy * 0.16f)), radius * 0.27f, 12f)
                drawPaw(Offset(center.x + radius * 0.44f, center.y + radius * (0.62f - shy * 0.16f)), radius * 0.27f, -12f)
            }

            "lieber" -> {
                drawPandaHead(center, radius, bow = true, mood = PandaMood.HAPPY, blink = blink, tilt = sin(cycle) * 3f)
                drawFloatingHeart(Offset(center.x + radius * 0.88f, center.y - radius * (0.72f + 0.10f * sin(cycle))), radius * 0.25f)
            }

            "foto" -> {
                drawPandaHead(center.copy(y = center.y - 2f), radius, bow = true, mood = PandaMood.HAPPY, blink = blink)
                drawCamera(center.copy(y = center.y + radius * 0.84f), radius, accent, flash = cos(cycle) > 0.94f)
            }

            "tief" -> {
                drawPandaHead(Offset(center.x - radius * 0.48f, center.y), radius * 0.76f, bow = false, mood = PandaMood.HAPPY, blink = blink, tilt = 4f)
                drawPandaHead(Offset(center.x + radius * 0.48f, center.y), radius * 0.76f, bow = true, mood = PandaMood.HAPPY, blink = blink, tilt = -4f)
                drawFloatingHeart(Offset(center.x, center.y - radius * (1.06f + sin(cycle) * 0.08f)), radius * 0.19f)
            }

            else -> {
                drawPandaHead(center, radius, bow = categoryId == "reden", mood = PandaMood.HAPPY, blink = blink, tilt = sin(cycle) * 2f)
                drawSpeechDots(Offset(center.x + radius * 0.92f, center.y - radius * 0.55f), radius, accent, phase)
            }
        }
    }
}

private fun blinkAmount(phase: Float): Float {
    val local = (phase * 4f) % 1f
    return if (local > 0.90f) sin(((local - 0.90f) / 0.10f) * PI).toFloat().coerceIn(0f, 1f) else 0f
}

private fun DrawScope.drawPandaHead(
    center: Offset,
    radius: Float,
    bow: Boolean,
    mood: PandaMood,
    blink: Float,
    tilt: Float = 0f
) {
    rotate(tilt, pivot = center) {
        val ink = Color(0xFF17121E)
        val white = Color(0xFFFFF9FC)
        val cheek = Color(0xFFFF87B8)
        drawCircle(ink, radius * 0.43f, Offset(center.x - radius * 0.69f, center.y - radius * 0.66f))
        drawCircle(ink, radius * 0.43f, Offset(center.x + radius * 0.69f, center.y - radius * 0.66f))
        drawCircle(Color(0xFFBC67FF).copy(alpha = 0.28f), radius * 1.13f, center)
        drawOval(white, topLeft = Offset(center.x - radius, center.y - radius * 0.91f), size = Size(radius * 2f, radius * 1.88f))

        listOf(-1f, 1f).forEach { side ->
            rotate(side * 18f, pivot = Offset(center.x + side * radius * 0.40f, center.y - radius * 0.12f)) {
                drawOval(
                    ink,
                    topLeft = Offset(center.x + side * radius * 0.40f - radius * 0.25f, center.y - radius * 0.45f),
                    size = Size(radius * 0.50f, radius * 0.70f)
                )
            }
            val eyeHeight = radius * (0.20f * (1f - blink)).coerceAtLeast(0.035f)
            drawOval(
                Color.White,
                topLeft = Offset(center.x + side * radius * 0.40f - radius * 0.07f, center.y - radius * 0.20f - eyeHeight / 2f),
                size = Size(radius * 0.14f, eyeHeight)
            )
        }

        drawOval(cheek.copy(alpha = 0.54f), Offset(center.x - radius * 0.78f, center.y + radius * 0.20f), Size(radius * 0.34f, radius * 0.17f))
        drawOval(cheek.copy(alpha = 0.54f), Offset(center.x + radius * 0.44f, center.y + radius * 0.20f), Size(radius * 0.34f, radius * 0.17f))
        drawOval(ink, Offset(center.x - radius * 0.11f, center.y + radius * 0.03f), Size(radius * 0.22f, radius * 0.16f))

        val mouth = Path().apply {
            moveTo(center.x, center.y + radius * 0.18f)
            when (mood) {
                PandaMood.HAPPY -> cubicTo(center.x - radius * 0.16f, center.y + radius * 0.40f, center.x - radius * 0.35f, center.y + radius * 0.19f, center.x - radius * 0.37f, center.y + radius * 0.13f)
                PandaMood.THINKING -> cubicTo(center.x + radius * 0.07f, center.y + radius * 0.24f, center.x + radius * 0.20f, center.y + radius * 0.21f, center.x + radius * 0.24f, center.y + radius * 0.18f)
                PandaMood.SURPRISED -> cubicTo(center.x + radius * 0.08f, center.y + radius * 0.14f, center.x + radius * 0.10f, center.y + radius * 0.36f, center.x, center.y + radius * 0.38f)
                PandaMood.SAD -> cubicTo(center.x - radius * 0.08f, center.y + radius * 0.14f, center.x - radius * 0.28f, center.y + radius * 0.38f, center.x - radius * 0.38f, center.y + radius * 0.38f)
            }
        }
        drawPath(mouth, ink, style = Stroke(width = (radius * 0.075f).coerceAtLeast(1.5f)))
        if (mood == PandaMood.HAPPY) {
            val mirror = Path().apply {
                moveTo(center.x, center.y + radius * 0.18f)
                cubicTo(center.x + radius * 0.16f, center.y + radius * 0.40f, center.x + radius * 0.35f, center.y + radius * 0.19f, center.x + radius * 0.37f, center.y + radius * 0.13f)
            }
            drawPath(mirror, ink, style = Stroke(width = (radius * 0.075f).coerceAtLeast(1.5f)))
        }
        if (mood == PandaMood.SAD) {
            val sadMirror = Path().apply {
                moveTo(center.x, center.y + radius * 0.18f)
                cubicTo(center.x + radius * 0.08f, center.y + radius * 0.14f, center.x + radius * 0.28f, center.y + radius * 0.38f, center.x + radius * 0.38f, center.y + radius * 0.38f)
            }
            drawPath(sadMirror, ink, style = Stroke(width = (radius * 0.075f).coerceAtLeast(1.5f)))
        }
        if (bow) drawBow(Offset(center.x + radius * 0.66f, center.y - radius * 0.77f), radius * 0.42f)
    }
}

private fun DrawScope.drawPaw(center: Offset, radius: Float, rotation: Float) {
    rotate(rotation, pivot = center) {
        drawOval(Color(0xFF17121E), Offset(center.x - radius, center.y - radius * 0.78f), Size(radius * 2f, radius * 1.56f))
        drawCircle(Color(0xFFFFA2C9), radius * 0.36f, center.copy(y = center.y + radius * 0.08f))
        drawCircle(Color(0xFFFFA2C9), radius * 0.15f, Offset(center.x - radius * 0.42f, center.y - radius * 0.34f))
        drawCircle(Color(0xFFFFA2C9), radius * 0.15f, Offset(center.x, center.y - radius * 0.47f))
        drawCircle(Color(0xFFFFA2C9), radius * 0.15f, Offset(center.x + radius * 0.42f, center.y - radius * 0.34f))
    }
}

private fun DrawScope.drawBow(center: Offset, size: Float) {
    val pink = Color(0xFFFF4FA3)
    val dark = Color(0xFFC72A79)
    val left = Path().apply {
        moveTo(center.x, center.y)
        cubicTo(center.x - size * 0.35f, center.y - size * 0.42f, center.x - size, center.y - size * 0.28f, center.x - size * 0.78f, center.y + size * 0.38f)
        cubicTo(center.x - size * 0.43f, center.y + size * 0.30f, center.x - size * 0.20f, center.y + size * 0.14f, center.x, center.y)
    }
    val right = Path().apply {
        moveTo(center.x, center.y)
        cubicTo(center.x + size * 0.35f, center.y - size * 0.42f, center.x + size, center.y - size * 0.28f, center.x + size * 0.78f, center.y + size * 0.38f)
        cubicTo(center.x + size * 0.43f, center.y + size * 0.30f, center.x + size * 0.20f, center.y + size * 0.14f, center.x, center.y)
    }
    drawPath(left, pink)
    drawPath(right, pink)
    drawCircle(dark, size * 0.26f, center)
    drawCircle(Color.White.copy(alpha = 0.55f), size * 0.08f, Offset(center.x - size * 0.08f, center.y - size * 0.08f))
}

private fun DrawScope.drawThoughtBubbles(center: Offset, radius: Float, accent: Color, direction: Float) {
    val side = if (direction >= 0f) 1f else -1f
    val base = Offset(center.x + side * radius * 0.92f, center.y - radius * 0.75f)
    drawCircle(accent.copy(alpha = 0.48f), radius * 0.10f, Offset(base.x - side * radius * 0.28f, base.y + radius * 0.34f))
    drawCircle(accent.copy(alpha = 0.62f), radius * 0.16f, Offset(base.x - side * radius * 0.08f, base.y + radius * 0.12f))
    drawCircle(accent.copy(alpha = 0.78f), radius * 0.27f, base)
}

private fun DrawScope.drawChoiceSpark(center: Offset, color: Color) {
    drawCircle(color.copy(alpha = 0.20f), 10f, center)
    drawCircle(color, 3.2f, center)
    drawLine(color.copy(alpha = 0.75f), Offset(center.x - 6f, center.y), Offset(center.x + 6f, center.y), 1.5f)
    drawLine(color.copy(alpha = 0.75f), Offset(center.x, center.y - 6f), Offset(center.x, center.y + 6f), 1.5f)
}

private fun DrawScope.drawApprovalMark(center: Offset, radius: Float, accent: Color, approved: Boolean) {
    val markCenter = Offset(center.x + radius * 0.92f, center.y - radius * 0.68f)
    drawCircle((if (approved) accent else Color(0xFFFF648A)).copy(alpha = 0.26f), radius * 0.32f, markCenter)
    if (approved) {
        drawLine(Color.White, Offset(markCenter.x - radius * 0.12f, markCenter.y), Offset(markCenter.x - radius * 0.02f, markCenter.y + radius * 0.11f), radius * 0.07f)
        drawLine(Color.White, Offset(markCenter.x - radius * 0.02f, markCenter.y + radius * 0.11f), Offset(markCenter.x + radius * 0.16f, markCenter.y - radius * 0.12f), radius * 0.07f)
    } else {
        drawLine(Color.White, Offset(markCenter.x - radius * 0.12f, markCenter.y - radius * 0.12f), Offset(markCenter.x + radius * 0.12f, markCenter.y + radius * 0.12f), radius * 0.07f)
        drawLine(Color.White, Offset(markCenter.x + radius * 0.12f, markCenter.y - radius * 0.12f), Offset(markCenter.x - radius * 0.12f, markCenter.y + radius * 0.12f), radius * 0.07f)
    }
}

private fun DrawScope.drawTinyPencil(center: Offset, radius: Float, accent: Color, drift: Float) {
    val start = Offset(center.x - radius * 0.68f + drift, center.y + radius * 0.75f)
    val end = Offset(center.x + radius * 0.62f + drift, center.y + radius * 0.40f)
    drawLine(accent, start, end, radius * 0.16f)
    drawLine(Color(0xFFFFD46B), start, end, radius * 0.08f)
}

private fun DrawScope.drawFloatingHeart(center: Offset, size: Float) {
    val path = Path().apply {
        moveTo(center.x, center.y + size * 0.72f)
        cubicTo(center.x - size * 1.20f, center.y, center.x - size * 0.74f, center.y - size * 0.82f, center.x, center.y - size * 0.24f)
        cubicTo(center.x + size * 0.74f, center.y - size * 0.82f, center.x + size * 1.20f, center.y, center.x, center.y + size * 0.72f)
    }
    drawPath(path, Color(0xFFFF4F9F))
}

private fun DrawScope.drawCamera(center: Offset, radius: Float, accent: Color, flash: Boolean) {
    drawRoundRect(
        color = Color(0xFF21162B),
        topLeft = Offset(center.x - radius * 0.62f, center.y - radius * 0.30f),
        size = Size(radius * 1.24f, radius * 0.64f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.12f)
    )
    drawCircle(accent, radius * 0.22f, center)
    drawCircle(Color.White, radius * 0.09f, center)
    if (flash) drawCircle(Color.White.copy(alpha = 0.74f), radius * 0.46f, Offset(center.x + radius * 0.48f, center.y - radius * 0.38f))
}

private fun DrawScope.drawSpeechDots(center: Offset, radius: Float, accent: Color, phase: Float) {
    repeat(3) { index ->
        val pulse = 0.55f + 0.45f * sin((phase + index / 3f) * TWO_PI_PANDA)
        drawCircle(accent.copy(alpha = pulse.coerceIn(0.18f, 1f)), radius * (0.10f + pulse * 0.025f), Offset(center.x + index * radius * 0.27f, center.y))
    }
}

private const val TWO_PI_PANDA = 6.2831855f

@Composable
fun PandaReactionStage(
    isMatch: Boolean,
    reactionKey: Int,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(reactionKey, isMatch) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = if (isMatch) 1_650 else 1_300,
                easing = FastOutSlowInEasing
            )
        )
    }

    Canvas(modifier = modifier) {
        val p = progress.value
        val baseRadius = size.minDimension * 0.16f
        if (isMatch) {
            val meet = (p / 0.62f).coerceIn(0f, 1f)
            val settle = ((p - 0.62f) / 0.38f).coerceIn(0f, 1f)
            val jump = sin(meet * PI).toFloat() * size.height * 0.13f
            val leftX = size.width * (0.21f + meet * 0.22f - settle * 0.05f)
            val rightX = size.width * (0.79f - meet * 0.22f + settle * 0.05f)
            val bodyY = size.height * 0.64f - jump

            drawPandaBody(
                center = Offset(leftX, bodyY),
                radius = baseRadius,
                bow = false,
                faceDirection = 1f,
                reach = meet
            )
            drawPandaBody(
                center = Offset(rightX, bodyY),
                radius = baseRadius,
                bow = true,
                faceDirection = -1f,
                reach = meet
            )

            if (p in 0.42f..0.86f) {
                val burst = sin(((p - 0.42f) / 0.44f) * PI).toFloat().coerceIn(0f, 1f)
                val impact = Offset(size.width * 0.5f, bodyY - baseRadius * 0.45f)
                repeat(8) { index ->
                    val angle = index * (TWO_PI_PANDA / 8f)
                    val inner = baseRadius * 0.25f
                    val outer = baseRadius * (0.52f + burst * 0.58f)
                    drawLine(
                        color = if (index % 2 == 0) Color(0xFFFFD56A) else Color(0xFFFF5AAA),
                        start = Offset(impact.x + cos(angle) * inner, impact.y + sin(angle) * inner),
                        end = Offset(impact.x + cos(angle) * outer, impact.y + sin(angle) * outer),
                        strokeWidth = 4f * burst
                    )
                }
            }
        } else {
            val scale = 0.72f + p * 0.34f
            val center = Offset(size.width * 0.5f, size.height * (0.66f - p * 0.06f))
            drawPandaBody(
                center = center,
                radius = baseRadius * scale,
                bow = false,
                faceDirection = 0f,
                reach = 0f,
                sad = true
            )
            val tearY = center.y - baseRadius * scale * 1.02f + p * baseRadius * 0.72f
            drawOval(
                color = Color(0xFF65D7FF).copy(alpha = 0.78f),
                topLeft = Offset(center.x + baseRadius * scale * 0.38f, tearY),
                size = Size(baseRadius * 0.12f, baseRadius * 0.22f)
            )
        }
    }
}

private fun DrawScope.drawPandaBody(
    center: Offset,
    radius: Float,
    bow: Boolean,
    faceDirection: Float,
    reach: Float,
    sad: Boolean = false
) {
    val ink = Color(0xFF17121E)
    val bodyCenter = center
    drawOval(
        color = ink,
        topLeft = Offset(bodyCenter.x - radius * 0.72f, bodyCenter.y - radius * 0.35f),
        size = Size(radius * 1.44f, radius * 1.70f)
    )
    drawOval(
        color = Color(0xFFFFF9FC),
        topLeft = Offset(bodyCenter.x - radius * 0.45f, bodyCenter.y - radius * 0.12f),
        size = Size(radius * 0.90f, radius * 1.02f)
    )
    val headCenter = Offset(bodyCenter.x, bodyCenter.y - radius * 0.82f)
    drawPandaHead(
        center = headCenter,
        radius = radius * 0.82f,
        bow = bow,
        mood = if (sad) PandaMood.SAD else PandaMood.HAPPY,
        blink = 0f,
        tilt = faceDirection * 4f
    )
    if (faceDirection != 0f) {
        val pawX = bodyCenter.x + faceDirection * radius * (0.70f + reach * 0.62f)
        val pawY = bodyCenter.y - radius * (0.20f + reach * 0.78f)
        drawLine(
            color = ink,
            start = Offset(bodyCenter.x + faceDirection * radius * 0.42f, bodyCenter.y + radius * 0.10f),
            end = Offset(pawX, pawY),
            strokeWidth = radius * 0.34f
        )
        drawPaw(Offset(pawX, pawY), radius * 0.25f, -faceDirection * 16f)
    }
}
