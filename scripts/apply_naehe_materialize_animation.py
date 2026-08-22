#!/usr/bin/env python3
from pathlib import Path

path = Path("app/src/main/java/com/example/ui/screens/QuizRunnerScreen.kt")
text = path.read_text(encoding="utf-8")

replacements = [
    (
        "import kotlinx.coroutines.coroutineScope\nimport kotlinx.coroutines.launch\n",
        "import kotlinx.coroutines.coroutineScope\nimport kotlinx.coroutines.delay\nimport kotlinx.coroutines.launch\n",
    ),
    (
        "@Composable\nfun QuizRunnerScreen(\n",
        """@Composable
private fun MaterializeInEntrance(
    animationKey: Any,
    delayMillis: Int,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.84f) }
    val translationY = remember { Animatable(26f) }
    val shimmerAlpha = remember { Animatable(0.72f) }
    val shimmerShift = remember { Animatable(-160f) }

    LaunchedEffect(animationKey) {
        alpha.snapTo(0f)
        scale.snapTo(0.84f)
        translationY.snapTo(26f)
        shimmerAlpha.snapTo(0.72f)
        shimmerShift.snapTo(-160f)

        if (delayMillis > 0) delay(delayMillis.toLong())

        coroutineScope {
            launch {
                alpha.animateTo(1f, tween(durationMillis = 280, easing = FastOutSlowInEasing))
            }
            launch {
                scale.animateTo(
                    1.03f,
                    tween(durationMillis = 250, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f))
                )
                scale.animateTo(1f, tween(durationMillis = 150, easing = FastOutSlowInEasing))
            }
            launch {
                translationY.animateTo(
                    0f,
                    tween(durationMillis = 340, easing = CubicBezierEasing(0.16f, 0.9f, 0.25f, 1f))
                )
            }
            launch {
                shimmerShift.animateTo(240f, tween(durationMillis = 420, easing = LinearEasing))
            }
            launch {
                shimmerAlpha.animateTo(0f, tween(durationMillis = 360, easing = FastOutSlowInEasing))
            }
        }
    }

    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha.value
            scaleX = scale.value
            scaleY = scale.value
            this.translationY = translationY.value
        }
    ) {
        content()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = shimmerAlpha.value * 0.24f),
                            HarmonyPink.copy(alpha = shimmerAlpha.value * 0.16f),
                            Color(0xFF7CF7FF).copy(alpha = shimmerAlpha.value * 0.22f),
                            Color.Transparent
                        ),
                        start = androidx.compose.ui.geometry.Offset(shimmerShift.value, 0f),
                        end = androidx.compose.ui.geometry.Offset(shimmerShift.value + 280f, 260f)
                    )
                )
        )
    }
}

@Composable
fun QuizRunnerScreen(
""",
    ),
    (
        """                        val q = pack.questions.getOrNull(activeRun.currentIndex)
                        val selectedAns = activeRun.currentAnswers[activeRun.currentIndex]
                        val scrollState = rememberScrollState()
""",
        """                        val q = pack.questions.getOrNull(activeRun.currentIndex)
                        val selectedAns = activeRun.currentAnswers[activeRun.currentIndex]
                        val scrollState = rememberScrollState()
                        val isIntimacyPack = pack.id == \"naehe\" && pack.topic == \"sex\"
                        val questionAnimationKey = \"${pack.id}_${activeRun.currentIndex}_question\"
""",
    ),
    (
        "                            AnimatedQuestionCard(question = contentText(q?.q ?: \"\"))\n",
        """                            if (isIntimacyPack) {
                                MaterializeInEntrance(
                                    animationKey = questionAnimationKey,
                                    delayMillis = 0,
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    AnimatedQuestionCard(question = contentText(q?.q ?: \"\"))
                                }
                            } else {
                                AnimatedQuestionCard(question = contentText(q?.q ?: \"\"))
                            }
""",
    ),
    (
        """                                QuizOptionButton(
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
""",
        """                                val optionButton: @Composable () -> Unit = {
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

                                if (isIntimacyPack) {
                                    MaterializeInEntrance(
                                        animationKey = \"${pack.id}_${activeRun.currentIndex}_option_$optIdx\",
                                        delayMillis = 130 + optIdx * 65,
                                        shape = RoundedCornerShape(18.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 11.dp)
                                    ) {
                                        optionButton()
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
""",
    ),
]

for index, (old, new) in enumerate(replacements, start=1):
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"replacement {index} expected exactly once, found {count}")
    text = text.replace(old, new, 1)

path.write_text(text, encoding="utf-8")
print("Applied Nähe & Intimität materialize animation")
