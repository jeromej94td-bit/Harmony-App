package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.HarmonyPink
import com.example.ui.theme.HarmonyPurple
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameCategoryVisual(
    categoryId: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    when (categoryId) {
        "wer" -> PandaArtworkIcon(
            drawableRes = R.drawable.panda_thinking_harmony,
            accent = accent,
            animationLabel = "thinking_panda",
            modifier = modifier
        )

        "nie" -> PandaArtworkIcon(
            drawableRes = R.drawable.panda_never_harmony,
            accent = accent,
            animationLabel = "never_panda",
            modifier = modifier
        )

        "tot" -> AnimatedHarmonyCards(accent = accent, modifier = modifier)
        "zeich" -> AnimatedPaletteCategoryIcon(accent = accent, modifier = modifier)
        "zust" -> AnimatedCategoryGlyph(
            primary = Icons.Default.CheckCircle,
            secondary = Icons.Default.Close,
            accent = accent,
            animationLabel = "agree_disagree",
            modifier = modifier
        )
        "lieber" -> AnimatedCategoryGlyph(
            primary = Icons.Default.Favorite,
            secondary = Icons.Default.Favorite,
            accent = accent,
            animationLabel = "preference_balance",
            modifier = modifier
        )
        "foto" -> AnimatedCategoryGlyph(
            primary = Icons.Default.CameraAlt,
            secondary = Icons.Default.FlashOn,
            accent = accent,
            animationLabel = "photo_flash",
            modifier = modifier
        )
        "tief" -> DeepConversationVisual(accent = accent, modifier = modifier)
        "reden" -> TalkBeforeVisual(accent = accent, modifier = modifier)
        else -> HarmonyCategoryIcon(categoryId = categoryId, accent = accent, modifier = modifier)
    }
}

@Composable
private fun AnimatedPaletteCategoryIcon(accent: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "animated_palette")
    val glow by transition.animateFloat(
        initialValue = 0.42f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "palette_glow"
    )
    val tilt by transition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(tween(3200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "palette_tilt"
    )
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .size(76.dp)
            .graphicsLayer {
                rotationZ = tilt
                scaleX = 0.98f + glow * 0.035f
                scaleY = 0.98f + glow * 0.035f
                shadowElevation = 12f
            }
            .clip(shape)
            .background(Brush.radialGradient(listOf(accent.copy(alpha = 0.76f), HarmonyPurple.copy(alpha = 0.72f), Color(0xFF16091F))))
            .border(
                1.5.dp,
                Brush.sweepGradient(listOf(Color(0xFFFFD166), Color(0xFF63E6BE), Color(0xFF6EA8FF), HarmonyPink, Color(0xFFFFD166))),
                shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.matchParentSize()) {
            val colors = listOf(Color(0xFFFFD166), Color(0xFF65E8B2), Color(0xFF79A8FF), HarmonyPink)
            val centers = listOf(
                Offset(size.width * 0.22f, size.height * 0.23f),
                Offset(size.width * 0.76f, size.height * 0.22f),
                Offset(size.width * 0.80f, size.height * 0.76f),
                Offset(size.width * 0.20f, size.height * 0.78f)
            )
            centers.forEachIndexed { index, center ->
                drawCircle(colors[index].copy(alpha = 0.38f + glow * 0.45f), radius = size.minDimension * (0.055f + glow * 0.012f), center = center)
            }
        }
        Icon(Icons.Default.Palette, contentDescription = null, tint = Color.White, modifier = Modifier.size(42.dp))
    }
}

@Composable
private fun DeepConversationVisual(accent: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "deep_conversation_premium")
    val motion by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "deep_conversation_exchange"
    )
    val glow by transition.animateFloat(
        initialValue = 0.46f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "deep_conversation_glow"
    )
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .size(76.dp)
            .graphicsLayer {
                scaleX = 0.99f + glow * 0.018f
                scaleY = 0.99f + glow * 0.018f
                shadowElevation = 13f
            }
            .clip(shape)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF6B8CFF).copy(alpha = 0.66f), accent.copy(alpha = 0.62f), HarmonyPurple.copy(alpha = 0.78f), Color(0xFF120719))
                )
            )
            .border(
                1.5.dp,
                Brush.sweepGradient(listOf(Color(0xFF7DE7FF), Color.White.copy(alpha = glow), HarmonyPink, accent, Color(0xFF7DE7FF))),
                shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.matchParentSize()) {
            val strokeWidth = 1.15.dp.toPx()
            val corner = CornerRadius(9.dp.toPx(), 9.dp.toPx())
            val floatPx = motion * 1.7.dp.toPx()

            val leftTop = Offset(size.width * 0.10f, size.height * 0.20f + floatPx)
            val leftSize = Size(size.width * 0.48f, size.height * 0.34f)
            val leftBrush = Brush.linearGradient(
                colors = listOf(Color(0xFF68D8FF).copy(alpha = 0.92f), Color(0xFF796BFF).copy(alpha = 0.94f)),
                start = leftTop,
                end = Offset(leftTop.x + leftSize.width, leftTop.y + leftSize.height)
            )
            drawRoundRect(leftBrush, leftTop, leftSize, corner)
            drawRoundRect(Color.White.copy(alpha = 0.66f + glow * 0.20f), leftTop, leftSize, corner, style = Stroke(strokeWidth))
            val leftTail = Path().apply {
                moveTo(leftTop.x + leftSize.width * 0.72f, leftTop.y + leftSize.height - strokeWidth)
                lineTo(leftTop.x + leftSize.width * 0.86f, leftTop.y + leftSize.height + 7.dp.toPx())
                lineTo(leftTop.x + leftSize.width * 0.91f, leftTop.y + leftSize.height - strokeWidth)
                close()
            }
            drawPath(leftTail, leftBrush)

            val rightTop = Offset(size.width * 0.42f, size.height * 0.48f - floatPx)
            val rightSize = Size(size.width * 0.48f, size.height * 0.31f)
            val rightBrush = Brush.linearGradient(
                colors = listOf(HarmonyPink.copy(alpha = 0.94f), accent.copy(alpha = 0.94f)),
                start = rightTop,
                end = Offset(rightTop.x + rightSize.width, rightTop.y + rightSize.height)
            )
            drawRoundRect(rightBrush, rightTop, rightSize, corner)
            drawRoundRect(Color.White.copy(alpha = 0.66f + glow * 0.20f), rightTop, rightSize, corner, style = Stroke(strokeWidth))
            val rightTail = Path().apply {
                moveTo(rightTop.x + rightSize.width * 0.10f, rightTop.y + rightSize.height - strokeWidth)
                lineTo(rightTop.x + rightSize.width * 0.02f, rightTop.y + rightSize.height + 6.dp.toPx())
                lineTo(rightTop.x + rightSize.width * 0.27f, rightTop.y + rightSize.height - strokeWidth)
                close()
            }
            drawPath(rightTail, rightBrush)

            repeat(3) { index ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.76f + glow * 0.18f),
                    radius = 1.55.dp.toPx(),
                    center = Offset(leftTop.x + 11.dp.toPx() + index * 7.dp.toPx(), leftTop.y + leftSize.height * 0.50f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.76f + glow * 0.18f),
                    radius = 1.45.dp.toPx(),
                    center = Offset(rightTop.x + 10.dp.toPx() + index * 7.dp.toPx(), rightTop.y + rightSize.height * 0.50f)
                )
            }
        }
        Box(
            modifier = Modifier
                .offset(y = 1.dp)
                .size(21.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color.White, HarmonyPink.copy(alpha = 0.92f))))
                .border(1.dp, Color.White.copy(alpha = glow), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFD82D72), modifier = Modifier.size(12.dp))
        }
    }
}

@Composable
private fun TalkBeforeVisual(accent: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "talk_before_premium")
    val clockPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(12_000, easing = LinearEasing), RepeatMode.Restart),
        label = "talk_before_clock"
    )
    val motion by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "talk_before_conversation"
    )
    val glow by transition.animateFloat(
        initialValue = 0.44f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "talk_before_glow"
    )
    val gold = Color(0xFFFFD36B)
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .size(76.dp)
            .graphicsLayer {
                scaleX = 0.99f + glow * 0.018f
                scaleY = 0.99f + glow * 0.018f
                shadowElevation = 13f
            }
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(gold.copy(alpha = 0.64f), Color(0xFFE876A8).copy(alpha = 0.66f), HarmonyPurple.copy(alpha = 0.82f), Color(0xFF14091D))
                )
            )
            .border(
                1.5.dp,
                Brush.sweepGradient(listOf(gold, Color.White.copy(alpha = glow), HarmonyPink, accent, gold)),
                shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.matchParentSize()) {
            val clockCenter = Offset(size.width * 0.32f, size.height * 0.40f)
            val radius = size.minDimension * 0.205f
            drawCircle(gold.copy(alpha = 0.16f + glow * 0.18f), radius * 1.28f, clockCenter)
            drawCircle(Color(0xFF24132F).copy(alpha = 0.90f), radius, clockCenter)
            drawCircle(gold.copy(alpha = 0.88f), radius, clockCenter, style = Stroke(1.6.dp.toPx()))
            repeat(4) { index ->
                val angle = index * (Math.PI / 2.0)
                val inner = radius * 0.73f
                val outer = radius * 0.88f
                drawLine(
                    color = Color.White.copy(alpha = 0.82f),
                    start = Offset(clockCenter.x + cos(angle).toFloat() * inner, clockCenter.y + sin(angle).toFloat() * inner),
                    end = Offset(clockCenter.x + cos(angle).toFloat() * outer, clockCenter.y + sin(angle).toFloat() * outer),
                    strokeWidth = 1.1.dp.toPx()
                )
            }
            val handAngle = clockPhase * (Math.PI * 2.0) - Math.PI / 2.0
            drawLine(
                color = gold,
                start = clockCenter,
                end = Offset(clockCenter.x + cos(handAngle).toFloat() * radius * 0.70f, clockCenter.y + sin(handAngle).toFloat() * radius * 0.70f),
                strokeWidth = 1.7.dp.toPx()
            )
            drawLine(
                color = Color.White,
                start = clockCenter,
                end = Offset(clockCenter.x + radius * 0.45f, clockCenter.y - radius * 0.18f),
                strokeWidth = 1.6.dp.toPx()
            )
            drawCircle(Color.White, 1.8.dp.toPx(), clockCenter)

            val bubbleTop = Offset(size.width * 0.45f, size.height * 0.38f + motion * 1.8.dp.toPx())
            val bubbleSize = Size(size.width * 0.43f, size.height * 0.31f)
            val bubbleBrush = Brush.linearGradient(
                listOf(HarmonyPink.copy(alpha = 0.96f), accent.copy(alpha = 0.94f)),
                start = bubbleTop,
                end = Offset(bubbleTop.x + bubbleSize.width, bubbleTop.y + bubbleSize.height)
            )
            drawRoundRect(bubbleBrush, bubbleTop, bubbleSize, CornerRadius(9.dp.toPx(), 9.dp.toPx()))
            drawRoundRect(
                Color.White.copy(alpha = 0.68f + glow * 0.20f),
                bubbleTop,
                bubbleSize,
                CornerRadius(9.dp.toPx(), 9.dp.toPx()),
                style = Stroke(1.15.dp.toPx())
            )
            val tail = Path().apply {
                moveTo(bubbleTop.x + bubbleSize.width * 0.18f, bubbleTop.y + bubbleSize.height - 1.dp.toPx())
                lineTo(bubbleTop.x + bubbleSize.width * 0.03f, bubbleTop.y + bubbleSize.height + 6.dp.toPx())
                lineTo(bubbleTop.x + bubbleSize.width * 0.37f, bubbleTop.y + bubbleSize.height - 1.dp.toPx())
                close()
            }
            drawPath(tail, bubbleBrush)
            repeat(3) { index ->
                drawCircle(
                    Color.White.copy(alpha = 0.78f + glow * 0.18f),
                    1.55.dp.toPx(),
                    Offset(bubbleTop.x + 9.dp.toPx() + index * 7.dp.toPx(), bubbleTop.y + bubbleSize.height * 0.49f)
                )
            }
        }
    }
}

@Composable
private fun AnimatedCategoryGlyph(
    primary: ImageVector,
    secondary: ImageVector,
    accent: Color,
    animationLabel: String,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = animationLabel)
    val motion by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "${animationLabel}_motion"
    )
    val glow by transition.animateFloat(
        initialValue = 0.38f,
        targetValue = 0.96f,
        animationSpec = infiniteRepeatable(tween(1900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "${animationLabel}_glow"
    )
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .size(76.dp)
            .graphicsLayer {
                scaleX = 0.985f + glow * 0.025f
                scaleY = 0.985f + glow * 0.025f
                shadowElevation = 10f
            }
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.16f), accent.copy(alpha = 0.72f), HarmonyPurple.copy(alpha = 0.82f), Color(0xFF15091E))
                )
            )
            .border(
                1.4.dp,
                Brush.sweepGradient(listOf(accent.copy(alpha = glow), Color.White.copy(alpha = glow), HarmonyPink.copy(alpha = glow), accent.copy(alpha = glow))),
                shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .offset(x = (-10).dp, y = (motion * -3f).dp)
                .graphicsLayer { rotationZ = motion * 5f }
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accent.copy(alpha = 0.36f + glow * 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(primary, contentDescription = null, tint = Color.White, modifier = Modifier.size(27.dp))
        }
        Box(
            Modifier
                .offset(x = 18.dp, y = (13f + motion * 3f).dp)
                .graphicsLayer { rotationZ = motion * -7f }
                .size(31.dp)
                .clip(CircleShape)
                .background(HarmonyPink.copy(alpha = 0.68f + glow * 0.20f))
                .border(1.dp, Color.White.copy(alpha = glow * 0.76f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(secondary, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun PandaArtworkIcon(
    @DrawableRes drawableRes: Int,
    accent: Color,
    animationLabel: String,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = animationLabel)
    val tilt by transition.animateFloat(
        initialValue = -1.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "${animationLabel}_tilt"
    )
    val breathe by transition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3_200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "${animationLabel}_breathe"
    )
    val glow by transition.animateFloat(
        initialValue = 0.44f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "${animationLabel}_glow"
    )

    Box(
        modifier = modifier
            .size(76.dp)
            .graphicsLayer {
                rotationZ = tilt
                scaleX = breathe
                scaleY = breathe
                shadowElevation = 8f
            }
            .clip(RoundedCornerShape(23.dp))
            .background(Color(0xFF15091E))
            .border(
                width = 1.4.dp,
                brush = Brush.sweepGradient(
                    listOf(
                        accent.copy(alpha = glow),
                        Color.White.copy(alpha = glow * 0.72f),
                        HarmonyPink.copy(alpha = glow),
                        accent.copy(alpha = glow)
                    )
                ),
                shape = RoundedCornerShape(23.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(drawableRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(74.dp)
        )
    }
}

@Composable
private fun AnimatedHarmonyCards(
    accent: Color,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density
    val flip = remember { Animatable(0f) }
    var pairIndex by remember { mutableIntStateOf(0) }
    val pairs = remember {
        listOf(
            Icons.Default.Restaurant to Icons.Default.LocalCafe,
            Icons.Default.LocationCity to Icons.Default.Flight,
            Icons.Default.Movie to Icons.Default.Palette
        )
    }
    val floatTransition = rememberInfiniteTransition(label = "harmony_cards_float")
    val floatPhase by floatTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3_200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "harmony_cards_float_phase"
    )
    val glow by floatTransition.animateFloat(
        initialValue = 0.50f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "harmony_cards_glow"
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(3_400)
            flip.animateTo(90f, tween(520, easing = FastOutSlowInEasing))
            pairIndex = (pairIndex + 1) % pairs.size
            flip.snapTo(-90f)
            flip.animateTo(0f, tween(520, easing = FastOutSlowInEasing))
        }
    }

    val pair = pairs[pairIndex]
    Box(
        modifier = modifier
            .size(width = 84.dp, height = 76.dp)
            .graphicsLayer {
                translationY = floatPhase * 2.5f * density
                scaleX = 0.99f + glow * 0.015f
                scaleY = 0.99f + glow * 0.015f
            },
        contentAlignment = Alignment.Center
    ) {
        HarmonyFlipCard(
            icon = pair.first,
            accent = accent,
            rotationY = flip.value,
            rotationZ = -10f + floatPhase * 2f,
            glow = glow,
            modifier = Modifier.offset(x = (-15).dp, y = 3.dp)
        )
        HarmonyFlipCard(
            icon = pair.second,
            accent = HarmonyPink,
            rotationY = -flip.value,
            rotationZ = 10f - floatPhase * 2f,
            glow = glow,
            modifier = Modifier.offset(x = 15.dp, y = (-3).dp)
        )
    }
}

@Composable
private fun HarmonyFlipCard(
    icon: ImageVector,
    accent: Color,
    rotationY: Float,
    rotationZ: Float,
    glow: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .size(width = 42.dp, height = 57.dp)
            .graphicsLayer {
                this.rotationY = rotationY
                this.rotationZ = rotationZ
                cameraDistance = 14f * density
                shadowElevation = 9f * density
            }
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.22f),
                        accent.copy(alpha = 0.88f),
                        HarmonyPurple.copy(alpha = 0.92f),
                        Color(0xFF170A21)
                    )
                )
            )
            .border(
                1.4.dp,
                Brush.sweepGradient(
                    listOf(
                        Color.White.copy(alpha = 0.92f),
                        accent.copy(alpha = glow),
                        HarmonyPink.copy(alpha = glow),
                        Color.White.copy(alpha = 0.92f)
                    )
                ),
                shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(23.dp)
        )
    }
}
