package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.ui.util.triggerMiniVibration
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.R
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.LinkEngine
import com.example.data.model.ProfileEntity
import com.example.data.model.QuestionPack
import com.example.ui.ActivePackRun
import com.example.ui.contentText
import com.example.ui.tr
import com.example.ui.components.CategoryTag
import com.example.ui.components.TotImageProvider
import com.example.ui.theme.HarmonyBg
import com.example.ui.theme.HarmonyGold
import com.example.ui.theme.HarmonyLine
import com.example.ui.theme.HarmonyMuted
import com.example.ui.theme.HarmonyPink
import com.example.ui.theme.HarmonyPinkSoft
import com.example.ui.theme.HarmonyPurple
import com.example.ui.theme.HarmonyPurpleLight
import com.example.ui.theme.HarmonySurface
import com.example.ui.theme.HarmonySurface2
import com.example.ui.theme.HarmonyText
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

private val QUESTION_FLOW_COLORS = listOf(
    Color(0xFFFFCB52),
    Color(0xFF3B8DFF),
    Color(0xFF3BD68A),
    Color(0xFF8B5CFF),
    Color(0xFFFF4FA3),
    Color(0xFFFF4D5F),
    Color(0xFFFFCB52)
)

private const val QUESTION_FLOW_TWO_PI = 6.2831855f
private const val PIZZA_BURGER_TENSION_QUESTION =
    "Was wäre für dich schlimmer: nie wieder Pizza oder nie wieder Burger?"

private fun compactPizzaBurgerQuestion(rawQuestion: String, localizedQuestion: String): String {
    if (rawQuestion != PIZZA_BURGER_TENSION_QUESTION) return localizedQuestion

    val colonIndex = listOf(
        localizedQuestion.indexOf(':'),
        localizedQuestion.indexOf('：')
    ).filter { it >= 0 }.minOrNull() ?: return localizedQuestion

    val questionMark = localizedQuestion.lastOrNull { it == '?' || it == '؟' || it == '？' } ?: '?'
    val stem = localizedQuestion
        .substring(0, colonIndex)
        .trim()
        .trimEnd('?', '؟', '？')
    return "$stem$questionMark"
}

private fun questionFlowColor(phase: Float): Color {
    val normalized = ((phase % 1f) + 1f) % 1f
    val scaled = normalized * (QUESTION_FLOW_COLORS.size - 1)
    val index = floor(scaled).toInt().coerceIn(0, QUESTION_FLOW_COLORS.size - 2)
    return lerp(
        QUESTION_FLOW_COLORS[index],
        QUESTION_FLOW_COLORS[index + 1],
        scaled - index
    )
}

@Composable
private fun QuestionColorFlowBackdrop(
    accent: Color,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "question_color_flow")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 30_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "question_color_phase"
    )
    val primary = questionFlowColor(phase)
    val secondary = questionFlowColor((phase + 0.23f) % 1f)
    val tertiary = questionFlowColor((phase + 0.58f) % 1f)
    val background = HarmonyBg

    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    lerp(background, primary, 0.48f),
                    lerp(background, secondary, 0.30f),
                    background,
                    lerp(background, tertiary, 0.20f)
                )
            )
        )

        val angle = phase * QUESTION_FLOW_TWO_PI
        val firstCenter = androidx.compose.ui.geometry.Offset(
            x = size.width * (0.24f + cos(angle) * 0.18f),
            y = size.height * (0.24f + sin(angle) * 0.09f)
        )
        val secondCenter = androidx.compose.ui.geometry.Offset(
            x = size.width * (0.78f + cos(angle + 2.2f) * 0.16f),
            y = size.height * (0.52f + sin(angle + 2.2f) * 0.16f)
        )
        val thirdCenter = androidx.compose.ui.geometry.Offset(
            x = size.width * (0.36f + cos(angle + 4.1f) * 0.20f),
            y = size.height * (0.82f + sin(angle + 4.1f) * 0.08f)
        )

        listOf(
            Triple(firstCenter, primary, size.minDimension * 0.72f),
            Triple(secondCenter, secondary, size.minDimension * 0.66f),
            Triple(thirdCenter, tertiary, size.minDimension * 0.58f)
        ).forEach { (center, color, radius) ->
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.34f), Color.Transparent),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
            drawCircle(
                color = color.copy(alpha = 0.15f),
                radius = radius * 0.62f,
                center = center,
                style = Stroke(width = 2.2f)
            )
        }

        repeat(18) { index ->
            val particleAngle = angle * (1f + (index % 3) * 0.13f) + index * 0.91f
            val orbitX = size.width * (0.12f + (index % 5) * 0.035f)
            val orbitY = size.height * (0.04f + (index % 4) * 0.018f)
            val baseX = size.width * ((index * 0.173f) % 1f)
            val baseY = size.height * (0.12f + ((index * 0.137f) % 0.78f))
            val pulse = (0.5f + 0.5f * sin(particleAngle * 1.7f)).coerceIn(0f, 1f)
            drawCircle(
                color = questionFlowColor((phase + index * 0.071f) % 1f)
                    .copy(alpha = 0.18f + pulse * 0.48f),
                radius = 1.8f + (index % 4) * 0.85f,
                center = androidx.compose.ui.geometry.Offset(
                    x = (baseX + cos(particleAngle) * orbitX).coerceIn(0f, size.width),
                    y = (baseY + sin(particleAngle) * orbitY).coerceIn(0f, size.height)
                )
            )
        }

        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFF120815).copy(alpha = 0.10f),
                    Color(0xFF09030D).copy(alpha = 0.34f),
                    Color(0xFF07020A).copy(alpha = 0.66f)
                )
            )
        )
        drawCircle(
            color = accent.copy(alpha = 0.10f),
            radius = size.width * 0.62f,
            center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.42f),
            style = Stroke(width = 3f)
        )
    }
}

private val CINEMATIC_GLITCH_GLYPHS = charArrayOf('█', '▓', '▒', '░', '▌', '▐', '◆', '◇')

private fun cinematicGlitchText(text: String, amount: Float): String {
    val strength = amount.coerceIn(0f, 1f)
    if (strength < 0.035f) return text
    val phase = (strength * 29f).toInt()
    return buildString(text.length) {
        text.forEachIndexed { index, char ->
            if (char.isWhitespace()) {
                append(char)
            } else {
                val gate = ((index * 37 + phase * 19) % 100) / 100f
                if (gate < strength * 0.62f) {
                    append(CINEMATIC_GLITCH_GLYPHS[(index + phase) % CINEMATIC_GLITCH_GLYPHS.size])
                } else {
                    append(char)
                }
            }
        }
    }
}

private fun cinematicLerp(start: Float, end: Float, fraction: Float): Float =
    start + (end - start) * fraction.coerceIn(0f, 1f)

@Composable
private fun AnimatedQuestionCard(
    question: String,
    glitchAmount: Float = 0f,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "question_spotlight")
    val shimmer by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4_800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "question_spotlight_shimmer"
    )
    val glow by transition.animateFloat(
        initialValue = 0.54f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "question_spotlight_glow"
    )
    val shape = RoundedCornerShape(24.dp)
    val glitch = glitchAmount.coerceIn(0f, 1f)
    val displayedQuestion = cinematicGlitchText(question, glitch)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { shadowElevation = 8f + glow * 12f }
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        HarmonyPurple.copy(alpha = 0.74f),
                        HarmonyPink.copy(alpha = 0.40f),
                        HarmonySurface2.copy(alpha = 0.98f),
                        HarmonyPurple.copy(alpha = 0.58f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(shimmer * 260f, 0f),
                    end = androidx.compose.ui.geometry.Offset(620f + shimmer * 180f, 440f)
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.sweepGradient(
                    listOf(
                        HarmonyPink.copy(alpha = glow),
                        Color.White.copy(alpha = glow * 0.90f),
                        HarmonyPurpleLight.copy(alpha = glow),
                        HarmonyPink.copy(alpha = glow)
                    )
                ),
                shape = shape
            )
            .padding(horizontal = 19.dp, vertical = 21.dp)
    ) {
        if (glitch > 0.025f) {
            Text(
                text = displayedQuestion,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF7CF7FF).copy(alpha = glitch * 0.48f),
                lineHeight = 31.sp,
                modifier = Modifier.graphicsLayer {
                    translationX = 11f * glitch + sin(glitch * 41f) * 5f
                    translationY = sin(glitch * 27f) * 3f
                }
            )
            Text(
                text = displayedQuestion,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFF63D6).copy(alpha = glitch * 0.42f),
                lineHeight = 31.sp,
                modifier = Modifier.graphicsLayer {
                    translationX = -10f * glitch + sin(glitch * 33f) * 4f
                    translationY = -sin(glitch * 23f) * 2.5f
                }
            )
        }
        Text(
            text = displayedQuestion,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            lineHeight = 31.sp,
            modifier = Modifier.graphicsLayer {
                translationX = sin(glitch * 52f) * 4.5f * glitch
                translationY = sin(glitch * 37f) * 1.8f * glitch
            }
        )
    }
}

@Composable
private fun CinematicSandMaterialize(
    animationKey: Any,
    delayMillis: Int,
    totalDurationMillis: Int,
    particleCount: Int,
    accentColor: Color,
    flowDirection: Float,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier,
    content: @Composable (Float) -> Unit
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(animationKey) {
        progress.snapTo(0f)
        if (delayMillis > 0) delay(delayMillis.toLong())
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = totalDurationMillis, easing = LinearEasing)
        )
    }

    val p = progress.value.coerceIn(0f, 1f)
    val contentAlpha = ((p - 0.72f) / 0.28f).coerceIn(0f, 1f)
    val settle = FastOutSlowInEasing.transform(contentAlpha)
    val glitchPulse = (0.5f + 0.5f * sin(p * 83f)).coerceIn(0f, 1f)
    val glitchAmount = ((1f - settle) * (0.28f + glitchPulse * 0.34f)).coerceIn(0f, 0.62f)

    Box(
        modifier = modifier.graphicsLayer {
            rotationY = flowDirection * 2.8f * (1f - settle)
            rotationX = -1.8f * (1f - settle)
            scaleX = 0.992f + settle * 0.008f
            scaleY = 0.992f + settle * 0.008f
            cameraDistance = 34f * density
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = contentAlpha
                    translationX = sin(p * 69f) * 1.8f * (1f - settle)
                }
        ) {
            content(glitchAmount)
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            if (p >= 0.995f) return@Canvas

            fun hash01(index: Int, salt: Int): Float {
                var x = index * 0x45D9F3B + salt * 0x119DE1F3
                x = x xor (x ushr 16)
                x *= 0x45D9F3B
                x = x xor (x ushr 16)
                return (x and 0x7FFFFFFF) / 2147483647f
            }

            fun smoothstep(value: Float): Float {
                val t = value.coerceIn(0f, 1f)
                return t * t * (3f - 2f * t)
            }

            val fadeToSurface = (1f - contentAlpha * 0.94f).coerceIn(0f, 1f)
            val width = size.width.coerceAtLeast(1f)
            val height = size.height.coerceAtLeast(1f)
            val direction = if (flowDirection >= 0f) 1f else -1f

            repeat(particleCount) { index ->
                val h1 = hash01(index, 1)
                val h2 = hash01(index, 2)
                val h3 = hash01(index, 3)
                val h4 = hash01(index, 4)
                val h5 = hash01(index, 5)
                val h6 = hash01(index, 6)
                val h7 = hash01(index, 7)
                val h8 = hash01(index, 8)

                val borderParticle = h8 > 0.86f
                val targetX: Float
                val targetY: Float
                if (borderParticle) {
                    when ((h7 * 4f).toInt().coerceIn(0, 3)) {
                        0 -> {
                            targetX = h1 * width
                            targetY = 1.1f + h2 * 1.8f
                        }
                        1 -> {
                            targetX = h1 * width
                            targetY = height - 1.1f - h2 * 1.8f
                        }
                        2 -> {
                            targetX = 1.1f + h1 * 1.8f
                            targetY = h2 * height
                        }
                        else -> {
                            targetX = width - 1.1f - h1 * 1.8f
                            targetY = h2 * height
                        }
                    }
                } else {
                    targetX = h1 * width
                    targetY = h2 * height
                }

                val localDelay = h3 * 0.43f
                val localRaw = ((p - localDelay) / (0.79f - localDelay).coerceAtLeast(0.14f)).coerceIn(0f, 1f)
                val local = smoothstep(localRaw)
                if (local <= 0f) return@repeat

                val mostlyMainSide = if (h4 > 0.91f) -direction else direction
                val startX = if (mostlyMainSide > 0f) {
                    -width * (0.16f + h5 * 0.78f)
                } else {
                    width * (1.16f + h5 * 0.78f)
                }
                val startY = targetY + (h6 - 0.5f) * height * 1.45f

                val inv = 1f - local
                val turbulence = sin(index * 0.173f + p * 37f + h4 * 6.2831855f)
                val crossTurbulence = cos(index * 0.117f + p * 29f + h5 * 6.2831855f)
                val streamArc = sin(local * 3.1415927f + h6 * 6.2831855f)

                var x = cinematicLerp(startX, targetX, local)
                var y = cinematicLerp(startY, targetY, local)
                x += turbulence * width * (0.035f + h7 * 0.055f) * inv
                y += crossTurbulence * height * (0.045f + h8 * 0.075f) * inv
                y += streamArc * height * 0.13f * inv * direction

                val microGlitchGate = hash01(index, 10)
                if (microGlitchGate > 0.965f && p in 0.34f..0.84f) {
                    x += sin(p * 151f + index) * (4f + h5 * 12f)
                }

                val gradientMix = (targetX / width).coerceIn(0f, 1f)
                val baseColor = lerp(accentColor, HarmonyPurple, 0.22f + gradientMix * 0.48f)
                val particleColor = when {
                    h7 > 0.975f -> Color.White
                    h7 > 0.942f -> Color(0xFF7CF7FF)
                    h7 > 0.915f -> Color(0xFFFF63D6)
                    else -> baseColor
                }

                val arrivalBrightness = 0.30f + local * 0.70f
                val alpha = (fadeToSurface * arrivalBrightness * (0.30f + h4 * 0.68f)).coerceIn(0f, 1f)
                val particleRadius = 0.65f + hash01(index, 5) * 1.35f

                if (local < 0.985f && index % 5 == 0) {
                    val trailX = x - (targetX - startX) * 0.010f * inv
                    val trailY = y - (targetY - startY) * 0.010f * inv
                    drawCircle(
                        color = particleColor.copy(alpha = alpha * 0.18f),
                        radius = particleRadius * 0.72f,
                        center = androidx.compose.ui.geometry.Offset(trailX, trailY)
                    )
                }

                drawCircle(
                    color = particleColor.copy(alpha = alpha),
                    radius = particleRadius,
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )

                if (index % 149 == 0 && p in 0.38f..0.88f) {
                    val streak = 3f + h6 * 8f
                    drawLine(
                        color = particleColor.copy(alpha = alpha * 0.38f),
                        start = androidx.compose.ui.geometry.Offset(x - streak * direction, y),
                        end = androidx.compose.ui.geometry.Offset(x + streak * 0.35f * direction, y),
                        strokeWidth = 0.8f
                    )
                }
            }

            if (p in 0.48f..0.90f) {
                val wave = ((p - 0.48f) / 0.42f).coerceIn(0f, 1f)
                val waveX = if (direction > 0f) {
                    cinematicLerp(-width * 0.12f, width * 1.08f, wave)
                } else {
                    cinematicLerp(width * 1.12f, -width * 0.08f, wave)
                }
                drawLine(
                    color = Color.White.copy(alpha = (1f - contentAlpha) * 0.10f),
                    start = androidx.compose.ui.geometry.Offset(waveX, 0f),
                    end = androidx.compose.ui.geometry.Offset(waveX, height),
                    strokeWidth = 0.7f
                )
            }
        }
    }
}

@Composable
fun QuizRunnerScreen(
    activeRun: ActivePackRun,
    profile: ProfileEntity,
    isExitConfirmOpen: Boolean,
    isOwnAnswerDialogOpen: Boolean,
    appLanguage: String,
    onPickAnswer: (String) -> Unit,
    onPickTot: (String) -> Unit,
    onNextStep: () -> Unit,
    onAskExit: () -> Unit,
    onCloseExitConfirm: () -> Unit,
    onCloseRunner: () -> Unit,
    onOpenOwnAnswerDialog: (Int?, String?) -> Unit,
    onCloseOwnAnswerDialog: () -> Unit,
    onSaveOwnAnswer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pack = activeRun.pack
    val totalLen = if (pack.type == "tot") pack.pairs.size else pack.questions.size

    val category = com.example.data.model.HarmonyPacksData.CATEGORIES.find { it.id == pack.cat }
    val catColor = category?.tagColorHex?.let { Color(it) } ?: HarmonyPink

    val animatedCatColor by androidx.compose.animation.animateColorAsState(
        targetValue = catColor,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 800, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "runnerCatColor"
    )

    LaunchedEffect(activeRun.isFinished) {
        if (activeRun.isFinished) {
            triggerMiniVibration(context, durationMs = 70L)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("quiz_runner_screen"),
        color = HarmonyBg
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HarmonyBg)
        ) {
            QuestionColorFlowBackdrop(
                accent = animatedCatColor,
                modifier = Modifier.fillMaxSize()
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Runner Top Bar
                if (pack.type == "tot" && !activeRun.isFinished) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onAskExit,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f))
                                .testTag("runner_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = tr("Zurück", "Back"),
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = tr("Das oder das?", "This or That?"),
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(130.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color.White.copy(alpha = 0.35f))
                            )
                        }

                        Box(modifier = Modifier.size(36.dp))
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onAskExit,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .testTag("runner_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = tr("Zurück", "Back"),
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Progress Track
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                        ) {
                            val fraction = if (activeRun.isFinished) 1f else ((activeRun.currentIndex + 1).toFloat() / totalLen).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Brush.horizontalGradient(listOf(animatedCatColor, animatedCatColor.copy(alpha = 0.75f))))
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = if (activeRun.isFinished) "$totalLen/$totalLen" else "${activeRun.currentIndex + 1}/$totalLen",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                // Runner Body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(
                            start = if (pack.type == "tot") 8.dp else 22.dp,
                            end = if (pack.type == "tot") 8.dp else 22.dp,
                            bottom = if (pack.type == "tot") 12.dp else 0.dp
                        )
                ) {
                    if (activeRun.isFinished) {
                        if (pack.type == "tot") {
                            TotResultsView(
                                pack = pack,
                                activeRun = activeRun,
                                profile = profile,
                                onClose = onAskExit
                            )
                        } else {
                            // Standard Finished Screen
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "💞", fontSize = 56.sp)
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = tr("Fertig!", "Done!"),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = tr("Deine Antworten sind gespeichert. Sobald ${profile.partnerName} das Paket beendet, werden beide Antworten gemeinsam sichtbar.", "Your answers are saved. Once ${profile.partnerName} finishes the pack, you will see both answers together."),
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    } else if (pack.type == "tot") {
                        // This or That Mode
                        val pair = pack.pairs.getOrNull(activeRun.currentIndex) ?: ("" to "")
                        val selectedAns = activeRun.currentAnswers[activeRun.currentIndex]
                        val caption = LinkEngine.captionFor(pack.id, activeRun.currentIndex)

                        Column(modifier = Modifier.fillMaxSize()) {
                            if (!caption.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White.copy(alpha = 0.12f))
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = contentText(caption),
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            TotCardPairView(
                                firstText = pair.first,
                                secondText = pair.second,
                                packPairs = pack.pairs,
                                selectedAns = selectedAns,
                                onPick = { chosen ->
                                    onPickTot(chosen)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else if (pack.type == "disc") {
                        // Discussion Mode
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(vertical = 12.dp)
                        ) {
                            CategoryTag(tag = contentText(pack.tags.firstOrNull() ?: "reden"))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = contentText(pack.title),
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            pack.questions.forEachIndexed { qIdx, question ->
                                val mineAns = activeRun.currentAnswers[qIdx] ?: question.defaultMine

                                Column(modifier = Modifier.padding(bottom = 18.dp)) {
                                    Text(
                                        text = "${qIdx + 1}. ${contentText(question.q)}",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        lineHeight = 20.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    if (mineAns != null) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    Brush.horizontalGradient(
                                                        listOf(HarmonyPink.copy(alpha = 0.16f), HarmonyPurple.copy(alpha = 0.14f))
                                                    )
                                                )
                                                .border(1.dp, HarmonyPink.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                                .padding(11.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .background(Brush.linearGradient(listOf(HarmonyPink, HarmonyPinkSoft))),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = profile.userName.take(1),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(9.dp))
                                            Text(
                                                text = contentText(mineAns),
                                                fontSize = 13.sp,
                                                color = Color.White,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White.copy(alpha = 0.03f))
                                                .border(1.dp, Color.White.copy(alpha = 0.13f), RoundedCornerShape(12.dp))
                                                .clickable { onOpenOwnAnswerDialog(qIdx, "disc") }
                                                .padding(11.dp)
                                        ) {
                                            Text(
                                                text = tr("✎ Tippe, um zu antworten", "✎ Tap to answer"),
                                                fontSize = 12.5.sp,
                                                color = HarmonyMuted
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(7.dp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.05f))
                                            .border(1.dp, HarmonyLine, RoundedCornerShape(12.dp))
                                            .padding(11.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(Brush.linearGradient(listOf(HarmonyPurple, HarmonyPurpleLight))),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = profile.partnerName.take(1),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(9.dp))
                                        Text(
                                            text = tr("Verbinde dich mit ${profile.partnerName}, um die Antwort zu sehen", "Connect with ${profile.partnerName} to see the answer"),
                                            fontSize = 13.sp,
                                            color = HarmonyMuted
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Standard Quiz Mode
                        val q = pack.questions.getOrNull(activeRun.currentIndex)
                        val selectedAns = activeRun.currentAnswers[activeRun.currentIndex]
                        val scrollState = rememberScrollState()
                        val isIntimacyPack = pack.id == "naehe" && pack.topic == "sex"
                        val questionAnimationKey = "${pack.id}_${activeRun.currentIndex}_question"

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.Center
                        ) {
                            CategoryTag(tag = contentText(pack.tags.firstOrNull() ?: "unterhaltung"))
                            Spacer(modifier = Modifier.height(14.dp))

                            if (isIntimacyPack) {
                                CinematicSandMaterialize(
                                    animationKey = questionAnimationKey,
                                    delayMillis = 0,
                                    totalDurationMillis = 1_900,
                                    particleCount = 3_000,
                                    accentColor = HarmonyPink,
                                    flowDirection = 1f,
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) { glitchAmount ->
                                    AnimatedQuestionCard(
                                        question = compactPizzaBurgerQuestion(
                                            rawQuestion = q?.q ?: "",
                                            localizedQuestion = contentText(q?.q ?: "")
                                        ),
                                        glitchAmount = glitchAmount
                                    )
                                }
                            } else {
                                AnimatedQuestionCard(
                                    question = compactPizzaBurgerQuestion(
                                        rawQuestion = q?.q ?: "",
                                        localizedQuestion = contentText(q?.q ?: "")
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(26.dp))

                            val isPizzaBurgerTensionQuestion = q?.q == PIZZA_BURGER_TENSION_QUESTION
                            val rawOptions = q?.options ?: emptyList()
                            val processedOptions = rawOptions.map {
                                it.replace("{user}", profile.userName).replace("{partner}", profile.partnerName)
                            }
                            val isNie = pack.cat == "nie"
                            val fallbackText = if (isNie) tr("Überspringen", "Skip") else tr("Schreibe deine eigene Antwort", "Write your own answer")
                            val options = if (isPizzaBurgerTensionQuestion) {
                                processedOptions.take(2)
                            } else {
                                processedOptions + fallbackText
                            }

                            options.forEachIndexed { optIdx, optText ->
                                val isOwn = !isPizzaBurgerTensionQuestion && optIdx == options.size - 1
                                val isSelected = if (isOwn) {
                                    selectedAns != null && selectedAns !in (q?.options ?: emptyList())
                                } else {
                                    selectedAns == optText
                                }

                                val optionButton: @Composable (Float) -> Unit = { glitchAmount ->
                                    QuizOptionButton(
                                        number = optIdx + 1,
                                        text = if (isSelected && isOwn) contentText(selectedAns ?: optText) else contentText(optText),
                                        isSelected = isSelected,
                                        isOwn = isOwn,
                                        onClick = {
                                            triggerMiniVibration(context, 40L)
                                            if (isOwn) {
                                                onOpenOwnAnswerDialog(activeRun.currentIndex, null)
                                            } else {
                                                onPickAnswer(optText)
                                            }
                                        },
                                        glitchAmount = glitchAmount
                                    )
                                }

                                if (isIntimacyPack) {
                                    CinematicSandMaterialize(
                                        animationKey = "${pack.id}_${activeRun.currentIndex}_option_$optIdx",
                                        delayMillis = 760 + optIdx * 500,
                                        totalDurationMillis = 2_400,
                                        particleCount = 1_000,
                                        accentColor = optionAccentColor(optIdx + 1),
                                        flowDirection = if (optIdx % 2 == 0) 1f else -1f,
                                        shape = RoundedCornerShape(18.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 11.dp)
                                    ) { glitchAmount ->
                                        optionButton(glitchAmount)
                                    }
                                } else {
                                    QuizOptionButton(
                                        number = optIdx + 1,
                                        text = if (isSelected && isOwn) contentText(selectedAns ?: optText) else contentText(optText),
                                        isSelected = isSelected,
                                        isOwn = isOwn,
                                        onClick = {
                                            triggerMiniVibration(context, 40L)
                                            if (isOwn) {
                                                onOpenOwnAnswerDialog(activeRun.currentIndex, null)
                                            } else {
                                                onPickAnswer(optText)
                                            }
                                        },
                                        modifier = Modifier.padding(bottom = 11.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Runner Footer
                if (activeRun.isFinished || pack.type == "disc") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (activeRun.isFinished) {
                            Button(
                                onClick = onCloseRunner,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("finish_runner_button"),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = HarmonyPink)
                            ) {
                                Text(text = tr("Zurück", "Back"), fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else if (pack.type == "disc") {
                            Button(
                                onClick = onCloseRunner,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("finish_disc_button"),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = HarmonyPink)
                            ) {
                                Text(text = tr("Fertig", "Done"), fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Exit Confirm Dialog
            if (isExitConfirmOpen) {
                Dialog(onDismissRequest = onCloseExitConfirm) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Brush.verticalGradient(listOf(HarmonySurface2, HarmonySurface)))
                            .border(1.dp, HarmonyLine, RoundedCornerShape(24.dp))
                            .padding(22.dp)
                    ) {
                        Column {
                            Text(
                                text = tr("Quiz verlassen?", "Leave quiz?"),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarmonyText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = tr("Möchtest du das Quiz wirklich verlassen? Dein bisheriger Fortschritt bleibt gespeichert.", "Are you sure you want to leave? Your progress will be saved."),
                                fontSize = 13.sp,
                                color = HarmonyMuted,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onCloseExitConfirm,
                                    modifier = Modifier.weight(1f),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f))
                                ) {
                                    Text(text = tr("Weiter spielen", "Keep playing"), color = HarmonyText)
                                }
                                Button(
                                    onClick = onCloseRunner,
                                    modifier = Modifier.weight(1f),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = HarmonyPink)
                                ) {
                                    Text(text = tr("Quiz verlassen", "Leave quiz"), color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Own Answer Dialog
            if (isOwnAnswerDialogOpen) {
                var textInput by remember { mutableStateOf("") }
                Dialog(onDismissRequest = onCloseOwnAnswerDialog) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Brush.verticalGradient(listOf(HarmonySurface2, HarmonySurface)))
                            .border(1.dp, HarmonyLine, RoundedCornerShape(24.dp))
                            .padding(22.dp)
                    ) {
                        Column {
                            Text(
                                text = tr("Deine eigene Antwort", "Your own answer"),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarmonyText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = tr("Schreib frei, was dir wirklich dazu einfällt.", "Write what truly comes to mind."),
                                fontSize = 13.sp,
                                color = HarmonyMuted
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                placeholder = { Text(tr("Deine Antwort...", "Your answer..."), color = HarmonyMuted) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("own_answer_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = HarmonyPink,
                                    unfocusedBorderColor = HarmonyLine,
                                    focusedTextColor = HarmonyText,
                                    unfocusedTextColor = HarmonyText
                                )
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onCloseOwnAnswerDialog,
                                    modifier = Modifier.weight(1f),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f))
                                ) {
                                    Text(text = tr("Abbrechen", "Cancel"), color = HarmonyText)
                                }
                                Button(
                                    onClick = {
                                        triggerMiniVibration(context, 40L)
                                        onSaveOwnAnswer(textInput)
                                    },
                                    enabled = textInput.isNotBlank(),
                                    modifier = Modifier.weight(1f),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = HarmonyPink)
                                ) {
                                    Text(text = tr("Übernehmen", "Save"), color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun optionAccentColor(number: Int): Color = when (number) {
    1 -> Color(0xFF4AA8FF)
    2 -> Color(0xFFFFC857)
    3 -> Color(0xFF4ED69A)
    4 -> Color(0xFFA978FF)
    else -> Color(0xFFFF6B9D)
}

@Composable
fun QuizOptionButton(
    number: Int,
    text: String,
    isSelected: Boolean,
    isOwn: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glitchAmount: Float = 0f
) {
    val optionAccent = optionAccentColor(number)
    val optionLabel = ('A'.code + number - 1).toChar().toString()
    val transition = rememberInfiniteTransition(label = "quiz_option_color_$number")
    val glow by transition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.86f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1_900 + number * 230,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "quiz_option_glow_$number"
    )
    val shape = RoundedCornerShape(18.dp)
    val glitch = glitchAmount.coerceIn(0f, 1f)
    val displayedText = cinematicGlitchText(text, glitch)
    val displayedLabel = if (glitch > 0.42f && ((number * 7 + (glitch * 23f).toInt()) % 3 != 0)) {
        CINEMATIC_GLITCH_GLYPHS[(number + (glitch * 17f).toInt()) % CINEMATIC_GLITCH_GLYPHS.size].toString()
    } else {
        optionLabel
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { shadowElevation = if (isSelected) 14f else 4f + glow * 4f }
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        optionAccent.copy(alpha = if (isSelected) 0.46f else 0.16f + glow * 0.10f),
                        HarmonySurface2.copy(alpha = 0.94f),
                        lerp(optionAccent, HarmonyPurple, 0.48f)
                            .copy(alpha = if (isSelected) 0.42f else 0.13f + glow * 0.08f)
                    )
                )
            )
            .border(
                width = if (isSelected) 2.dp else 1.3.dp,
                color = optionAccent.copy(alpha = if (isSelected) 1f else 0.36f + glow * 0.40f),
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(15.dp)
            .testTag("quiz_option_$number")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(27.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                optionAccent,
                                lerp(optionAccent, HarmonyPurple, 0.42f)
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.50f + glow * 0.30f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (glitch > 0.03f) {
                    Text(
                        text = displayedLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF7CF7FF).copy(alpha = glitch * 0.55f),
                        modifier = Modifier.graphicsLayer { translationX = 4.5f * glitch }
                    )
                    Text(
                        text = displayedLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFF63D6).copy(alpha = glitch * 0.46f),
                        modifier = Modifier.graphicsLayer { translationX = -4f * glitch }
                    )
                }
                Text(
                    text = displayedLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.graphicsLayer {
                        translationX = sin(glitch * 43f) * 2f * glitch
                    }
                )
            }
            Spacer(modifier = Modifier.width(13.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (glitch > 0.025f) {
                    Text(
                        text = displayedText,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF7CF7FF).copy(alpha = glitch * 0.43f),
                        fontStyle = if (isOwn) FontStyle.Italic else FontStyle.Normal,
                        lineHeight = 19.sp,
                        modifier = Modifier.graphicsLayer {
                            translationX = 8f * glitch + sin(glitch * 39f) * 3f
                            translationY = sin(glitch * 24f) * 2f
                        }
                    )
                    Text(
                        text = displayedText,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFF63D6).copy(alpha = glitch * 0.37f),
                        fontStyle = if (isOwn) FontStyle.Italic else FontStyle.Normal,
                        lineHeight = 19.sp,
                        modifier = Modifier.graphicsLayer {
                            translationX = -7f * glitch + sin(glitch * 31f) * 2.5f
                            translationY = -sin(glitch * 28f) * 1.8f
                        }
                    )
                }
                Text(
                    text = displayedText,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isOwn && !isSelected) Color.White.copy(alpha = 0.72f) else Color.White,
                    fontStyle = if (isOwn) FontStyle.Italic else FontStyle.Normal,
                    lineHeight = 19.sp,
                    modifier = Modifier.graphicsLayer {
                        translationX = sin(glitch * 51f) * 3.5f * glitch
                        translationY = sin(glitch * 34f) * 1.4f * glitch
                    }
                )
            }
        }
    }
}

@Composable
fun TotCardPairView(
    firstText: String,
    secondText: String,
    packPairs: List<Pair<String, String>>,
    selectedAns: String?,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val oderText = stringResource(R.string.oder_text)
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val windDistancePx = with(density) { configuration.screenHeightDp.dp.toPx() * 0.42f }
    val topOffsetY = remember { Animatable(0f) }
    val bottomOffsetY = remember { Animatable(0f) }
    val topTilt = remember { Animatable(0f) }
    val bottomTilt = remember { Animatable(0f) }
    val topFlip = remember { Animatable(0f) }
    val bottomFlip = remember { Animatable(0f) }
    val oderScale = remember { Animatable(1f) }

    var isAnimating by remember { mutableStateOf(false) }
    var skipNextTotEntrance by remember { mutableStateOf(false) }
    var topShuffleKey by remember(firstText, secondText) { mutableStateOf(firstText) }
    var bottomShuffleKey by remember(firstText, secondText) { mutableStateOf(secondText) }

    LaunchedEffect(firstText, secondText) {
        if (skipNextTotEntrance) {
            topOffsetY.snapTo(0f)
            bottomOffsetY.snapTo(0f)
            topTilt.snapTo(0f)
            bottomTilt.snapTo(0f)
            oderScale.snapTo(1f)
            topFlip.snapTo(0f)
            bottomFlip.snapTo(0f)
            skipNextTotEntrance = false

            // Continue the final shuffle momentum on the same rotationY axis only.
            // No extra Z tilt or positional wobble: just a small, damped rotational settle.
            coroutineScope {
                launch { topFlip.animateTo(2.0f, tween(120, easing = FastOutSlowInEasing)) }
                launch { bottomFlip.animateTo(-2.0f, tween(120, easing = FastOutSlowInEasing)) }
            }
            coroutineScope {
                launch { topFlip.animateTo(-1.0f, tween(150, easing = FastOutSlowInEasing)) }
                launch { bottomFlip.animateTo(1.0f, tween(150, easing = FastOutSlowInEasing)) }
            }
            coroutineScope {
                launch { topFlip.animateTo(0.35f, tween(130, easing = FastOutSlowInEasing)) }
                launch { bottomFlip.animateTo(-0.35f, tween(130, easing = FastOutSlowInEasing)) }
            }
            coroutineScope {
                launch { topFlip.animateTo(0f, tween(180, easing = FastOutSlowInEasing)) }
                launch { bottomFlip.animateTo(0f, tween(180, easing = FastOutSlowInEasing)) }
            }
            return@LaunchedEffect
        }

        topOffsetY.snapTo(-windDistancePx)
        bottomOffsetY.snapTo(windDistancePx)
        topTilt.snapTo(0f)
        bottomTilt.snapTo(0f)
        oderScale.snapTo(1f)
        topFlip.snapTo(-28f)
        bottomFlip.snapTo(28f)
        coroutineScope {
            launch { topOffsetY.animateTo(0f, tween(760, easing = CubicBezierEasing(0.12f, 0.72f, 0.16f, 1f))) }
            launch { bottomOffsetY.animateTo(0f, tween(820, easing = CubicBezierEasing(0.12f, 0.72f, 0.16f, 1f))) }
            launch { topFlip.animateTo(0f, tween(760, easing = FastOutSlowInEasing)) }
            launch { bottomFlip.animateTo(0f, tween(820, easing = FastOutSlowInEasing)) }
        }
    }

    fun handlePick(option: String) {
        if (isAnimating) return
        isAnimating = true
        triggerMiniVibration(context, 40L)

        scope.launch {
            buildTotShuffleFrames(packPairs, firstText to secondText, 4).forEachIndexed { index, key ->
                coroutineScope {
                    launch { topFlip.animateTo(90f, tween(90, easing = FastOutSlowInEasing)) }
                    launch { bottomFlip.animateTo(-90f, tween(90, easing = FastOutSlowInEasing)) }
                }
                if (index % 2 == 0) topShuffleKey = key else bottomShuffleKey = key
                topFlip.snapTo(-90f)
                bottomFlip.snapTo(90f)
                coroutineScope {
                    launch { topFlip.animateTo(0f, tween(115, easing = FastOutSlowInEasing)) }
                    launch { bottomFlip.animateTo(0f, tween(115, easing = FastOutSlowInEasing)) }
                }
            }
            // The shuffle already ends on the incoming pair. Keep it in place.
            // Do not converge the cards or replay the wind entrance after the index update.
            skipNextTotEntrance = true
            onPick(option)
            isAnimating = false
            // tot_settle_wobble: the incoming pair is already clickable while its tiny
            // inertial settle runs in the keyed LaunchedEffect above.
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Card (tilted -3.2f)
            TotStyledCard(
                text = contentText(firstText),
                assetKey = topShuffleKey,
                tagAlignment = Alignment.TopStart,
                isSelected = selectedAns == firstText,
                rotationAngle = -3.2f + topTilt.value,
                onClick = { handlePick(firstText) },
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        translationY = topOffsetY.value
                        rotationY = topFlip.value
                        cameraDistance = 14f * density.density
                    }
            )

            // Bottom Card (tilted +3.2f)
            TotStyledCard(
                text = contentText(secondText),
                assetKey = bottomShuffleKey,
                tagAlignment = Alignment.BottomStart,
                isSelected = selectedAns == secondText,
                rotationAngle = 3.2f + bottomTilt.value,
                onClick = { handlePick(secondText) },
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        translationY = bottomOffsetY.value
                        rotationY = bottomFlip.value
                        cameraDistance = 14f * density.density
                    }
            )
        }

        // Central "oder" badge
        Box(
            modifier = Modifier
                .size(50.dp)
                .graphicsLayer {
                    scaleX = oderScale.value
                    scaleY = oderScale.value
                }
                .clip(CircleShape)
                .background(Color.White)
                .border(1.5.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contentText(oderText),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF231127)
            )
        }
    }
}

@Composable
fun TotStyledCard(
    text: String,
    tagAlignment: Alignment,
    isSelected: Boolean,
    rotationAngle: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    assetKey: String = text
) {
    val context = LocalContext.current
    val imageUrl = remember(text, assetKey, TotImageProvider.version) {
        TotImageProvider.getImageUrl(assetKey = assetKey, legacyAssetKey = text)
    }
    val imageRequest = remember(imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { rotationZ = rotationAngle }
            .clip(RoundedCornerShape(26.dp))
            .border(
                width = 3.dp,
                color = Color.White,
                shape = RoundedCornerShape(26.dp)
            )
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = imageRequest,
            contentDescription = text,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black.copy(alpha = 0.15f),
                            0.5f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.25f)
                        )
                    )
                )
        )

        // Destination Tag Pill
        if (com.example.data.DevAssetStore.isUserFacingLabel(text)) {
            Box(
                modifier = Modifier
                    .align(tagAlignment)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = contentText(text),
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TotResultsView(
    pack: QuestionPack,
    activeRun: ActivePackRun,
    profile: ProfileEntity,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = tr("Zurück", "Back"),
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = pack.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "•••",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (selectedTab == 0) Brush.horizontalGradient(listOf(HarmonyPink, HarmonyPurple))
                        else Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.08f)))
                    )
                    .clickable { selectedTab = 0 }
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = tr("Ergebnisse", "Results"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (selectedTab == 1) Brush.horizontalGradient(listOf(HarmonyPink, HarmonyPurple))
                        else Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.08f)))
                    )
                    .clickable { selectedTab = 1 }
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "💬", fontSize = 13.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = tr("Diskussion", "Discussion"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (selectedTab == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "85%",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = HarmonyPinkSoft
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tr("Antwortähnlichkeit", "Answer similarity"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarmonyPink
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            pack.pairs.forEachIndexed { index, pair ->
                val myAns = activeRun.currentAnswers[index]

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(1.dp, HarmonyLine, RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SideBySideTotCard(
                                text = contentText(pair.first),
                                assetKey = pair.first,
                                isSelected = myAns == pair.first,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(210.dp)
                            )

                            SideBySideTotCard(
                                text = contentText(pair.second),
                                assetKey = pair.second,
                                isSelected = myAns == pair.second,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(210.dp)
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 20.dp)
            ) {
                Text(
                    text = tr("Diskutiert eure Antworten", "Discuss your answers"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tr("Habt ihr überraschende Unterschiede entdeckt? Sprecht darüber, warum euch bestimmte Optionen besser gefallen!", "Did you discover surprising differences? Talk about why you prefer certain options!"),
                    fontSize = 13.5.sp,
                    color = HarmonyMuted,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun SideBySideTotCard(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    assetKey: String = text
) {
    val context = LocalContext.current
    val imageUrl = remember(text, assetKey, TotImageProvider.version) {
        TotImageProvider.getImageUrl(assetKey = assetKey, legacyAssetKey = text)
    }
    val imageRequest = remember(imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build()
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) HarmonyPink else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        AsyncImage(
            model = imageRequest,
            contentDescription = text,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.35f to Color.Black.copy(alpha = 0.15f),
                            1.0f to Color.Black.copy(alpha = 0.88f)
                        )
                    )
                )
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(HarmonyPink),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
            }
        }

        if (com.example.data.DevAssetStore.isUserFacingLabel(text)) {
            Text(
                text = contentText(text),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 16.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            )
        }
    }
}
