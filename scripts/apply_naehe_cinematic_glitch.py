from pathlib import Path

path = Path('app/src/main/java/com/example/ui/screens/QuizRunnerScreen.kt')
text = path.read_text(encoding='utf-8')

if 'private fun CinematicGlitchMaterialize(' in text:
    print('Cinematic Nähe glitch materialization already applied')
    raise SystemExit(0)

animated_start = text.index('@Composable\nprivate fun AnimatedQuestionCard(')
materialize_start = text.index('@Composable\nprivate fun MaterializeInEntrance(', animated_start)
quiz_start = text.index('@Composable\nfun QuizRunnerScreen(', materialize_start)

new_animated = r'''private val CINEMATIC_GLITCH_GLYPHS = charArrayOf('█', '▓', '▒', '░', '▌', '▐', '◆', '◇')

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

'''

new_materialize = r'''@Composable
private fun CinematicGlitchMaterialize(
    animationKey: Any,
    delayMillis: Int,
    totalDurationMillis: Int,
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
    val build = (p / 0.70f).coerceIn(0f, 1f)
    val reveal = ((p - 0.02f) / 0.24f).coerceIn(0f, 1f)
    val settle = ((p - 0.62f) / 0.38f).coerceIn(0f, 1f)
    val buildEase = CubicBezierEasing(0.12f, 0.86f, 0.20f, 1f).transform(build)
    val settleEase = FastOutSlowInEasing.transform(settle)
    val glitchAmount = ((1f - settleEase) * (0.78f + (1f - build) * 0.22f)).coerceIn(0f, 1f)

    val scale = if (p < 0.62f) {
        cinematicLerp(0.54f, 1.085f, buildEase)
    } else {
        cinematicLerp(1.085f, 1f, settleEase)
    }
    val rotationX = if (p < 0.62f) {
        cinematicLerp(29f, -4.5f, buildEase)
    } else {
        cinematicLerp(-4.5f, 0f, settleEase)
    }
    val rotationY = if (p < 0.62f) {
        cinematicLerp(-24f, 6f, buildEase)
    } else {
        cinematicLerp(6f, 0f, settleEase)
    }
    val rotationZ = sin(p * 31f) * 1.6f * glitchAmount
    val xJitter = sin(p * 69f) * 9f * glitchAmount
    val yLift = cinematicLerp(62f, 0f, reveal)

    Box(
        modifier = modifier
            .graphicsLayer {
                alpha = reveal
                scaleX = scale
                scaleY = scale
                this.rotationX = rotationX
                this.rotationY = rotationY
                this.rotationZ = rotationZ
                translationX = xJitter
                translationY = yLift
                cameraDistance = 28f * density
                shadowElevation = 14f * glitchAmount
            }
    ) {
        content(glitchAmount)

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
        ) {
            val fragmentFade = (1f - settleEase).coerceIn(0f, 1f)
            val columns = 9
            val rows = 6
            val cellW = size.width / columns
            val cellH = size.height / rows

            for (row in 0 until rows) {
                for (col in 0 until columns) {
                    val seed = ((row * 31 + col * 17) % 100) / 100f
                    val local = ((p - seed * 0.38f) / 0.34f).coerceIn(0f, 1f)
                    if (local < 0.995f) {
                        val directionX = ((col % 3) - 1).toFloat()
                        val directionY = ((row % 3) - 1).toFloat()
                        val driftX = directionX * size.width * 0.17f * (1f - local) +
                            sin((row * 11 + col * 7).toFloat() + p * 42f) * 18f * (1f - local)
                        val driftY = directionY * size.height * 0.28f * (1f - local)
                        val alpha = (1f - local) * fragmentFade * (0.28f + seed * 0.34f)
                        val fragmentColor = when ((row + col) % 4) {
                            0 -> Color(0xFF7CF7FF)
                            1 -> Color(0xFFFF63D6)
                            2 -> Color.White
                            else -> HarmonyPink
                        }
                        drawRect(
                            color = fragmentColor.copy(alpha = alpha),
                            topLeft = androidx.compose.ui.geometry.Offset(
                                x = col * cellW + driftX,
                                y = row * cellH + driftY
                            ),
                            size = androidx.compose.ui.geometry.Size(
                                width = cellW * (0.72f + 0.22f * local),
                                height = cellH * (0.58f + 0.34f * local)
                            )
                        )
                    }
                }
            }

            val sliceAlpha = glitchAmount * 0.22f
            repeat(12) { index ->
                val bandHeight = size.height / 17f
                val y = ((index * 1.47f + p * 7.5f) % 13f) / 13f * size.height
                val xShift = sin(index * 2.3f + p * 57f) * 44f * glitchAmount
                drawRect(
                    color = when (index % 3) {
                        0 -> Color(0xFF7CF7FF).copy(alpha = sliceAlpha)
                        1 -> Color(0xFFFF63D6).copy(alpha = sliceAlpha * 0.88f)
                        else -> Color.White.copy(alpha = sliceAlpha * 0.72f)
                    },
                    topLeft = androidx.compose.ui.geometry.Offset(xShift, y),
                    size = androidx.compose.ui.geometry.Size(size.width, bandHeight)
                )
            }

            var scanY = 0f
            while (scanY < size.height) {
                drawLine(
                    color = Color.White.copy(alpha = 0.055f * glitchAmount),
                    start = androidx.compose.ui.geometry.Offset(0f, scanY),
                    end = androidx.compose.ui.geometry.Offset(size.width, scanY),
                    strokeWidth = 1f
                )
                scanY += 7f
            }

            val sweepX = cinematicLerp(-size.width * 0.45f, size.width * 1.22f, build)
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF7CF7FF).copy(alpha = 0.10f * glitchAmount),
                        Color.White.copy(alpha = 0.24f * glitchAmount),
                        Color(0xFFFF63D6).copy(alpha = 0.11f * glitchAmount),
                        Color.Transparent
                    ),
                    start = androidx.compose.ui.geometry.Offset(sweepX - 170f, 0f),
                    end = androidx.compose.ui.geometry.Offset(sweepX + 190f, size.height)
                )
            )

            repeat(18) { index ->
                val sparkPhase = ((p * 2.2f + index * 0.071f) % 1f)
                val sparkAlpha = (1f - settleEase) * (1f - sparkPhase) * 0.70f
                val edgeX = if (index % 2 == 0) sparkPhase * size.width else size.width - sparkPhase * size.width
                val edgeY = ((index * 0.173f) % 1f) * size.height
                drawRect(
                    color = if (index % 3 == 0) {
                        Color(0xFF7CF7FF).copy(alpha = sparkAlpha)
                    } else {
                        Color.White.copy(alpha = sparkAlpha * 0.82f)
                    },
                    topLeft = androidx.compose.ui.geometry.Offset(edgeX, edgeY),
                    size = androidx.compose.ui.geometry.Size(3f + (index % 3) * 2f, 2f + (index % 2) * 2f)
                )
            }
        }
    }
}

'''

text = text[:animated_start] + new_animated + new_materialize + text[quiz_start:]

old_question = r'''                            if (isIntimacyPack) {
                                MaterializeInEntrance(
                                    animationKey = questionAnimationKey,
                                    delayMillis = 0,
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    AnimatedQuestionCard(question = contentText(q?.q ?: ""))
                                }
                            } else {
                                AnimatedQuestionCard(question = contentText(q?.q ?: ""))
                            }
'''
new_question = r'''                            if (isIntimacyPack) {
                                CinematicGlitchMaterialize(
                                    animationKey = questionAnimationKey,
                                    delayMillis = 0,
                                    totalDurationMillis = 2_800,
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) { glitchAmount ->
                                    AnimatedQuestionCard(
                                        question = contentText(q?.q ?: ""),
                                        glitchAmount = glitchAmount
                                    )
                                }
                            } else {
                                AnimatedQuestionCard(question = contentText(q?.q ?: ""))
                            }
'''
if old_question not in text:
    raise SystemExit('question block not found')
text = text.replace(old_question, new_question, 1)

old_option_lambda = r'''                                val optionButton: @Composable () -> Unit = {
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
                                        }
                                    )
                                }
'''
new_option_lambda = r'''                                val optionButton: @Composable (Float) -> Unit = { glitchAmount ->
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
'''
if old_option_lambda not in text:
    raise SystemExit('option lambda block not found')
text = text.replace(old_option_lambda, new_option_lambda, 1)

old_option_animation = r'''                                if (isIntimacyPack) {
                                    MaterializeInEntrance(
                                        animationKey = "${pack.id}_${activeRun.currentIndex}_option_$optIdx",
                                        delayMillis = 130 + optIdx * 65,
                                        shape = RoundedCornerShape(18.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 11.dp)
                                    ) {
                                        optionButton()
                                    }
                                } else {
'''
new_option_animation = r'''                                if (isIntimacyPack) {
                                    CinematicGlitchMaterialize(
                                        animationKey = "${pack.id}_${activeRun.currentIndex}_option_$optIdx",
                                        delayMillis = 650 + optIdx * 170,
                                        totalDurationMillis = 2_100,
                                        shape = RoundedCornerShape(18.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 11.dp)
                                    ) { glitchAmount ->
                                        optionButton(glitchAmount)
                                    }
                                } else {
'''
if old_option_animation not in text:
    raise SystemExit('option animation block not found')
text = text.replace(old_option_animation, new_option_animation, 1)

option_start = text.index('@Composable\nfun QuizOptionButton(')
tot_start = text.index('@Composable\nfun TotCardPairView(', option_start)
new_option_function = r'''@Composable
fun QuizOptionButton(
    number: Int,
    text: String,
    isSelected: Boolean,
    isOwn: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glitchAmount: Float = 0f
) {
    val optionAccent = when (number) {
        1 -> Color(0xFF4AA8FF)
        2 -> Color(0xFFFFC857)
        3 -> Color(0xFF4ED69A)
        4 -> Color(0xFFA978FF)
        else -> Color(0xFFFF6B9D)
    }
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

'''
text = text[:option_start] + new_option_function + text[tot_start:]

required = [
    'private fun CinematicGlitchMaterialize(',
    'private fun cinematicGlitchText(',
    'glitchAmount: Float = 0f',
    'totalDurationMillis = 2_800',
    'totalDurationMillis = 2_100',
    'delayMillis = 650 + optIdx * 170',
]
missing = [marker for marker in required if marker not in text]
if missing:
    raise SystemExit('post-patch markers missing: ' + ', '.join(missing))
if 'private fun MaterializeInEntrance(' in text:
    raise SystemExit('legacy MaterializeInEntrance still present')

path.write_text(text, encoding='utf-8')
print('Applied cinematic Nähe glitch materialization')
