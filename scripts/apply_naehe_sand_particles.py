#!/usr/bin/env python3
from pathlib import Path

path = Path("app/src/main/java/com/example/ui/screens/QuizRunnerScreen.kt")
text = path.read_text(encoding="utf-8")

if "private fun CinematicSandMaterialize(" not in text:
    start = text.index("@Composable\nprivate fun CinematicGlitchMaterialize(")
    end = text.index("\n@Composable\nfun QuizRunnerScreen(", start)

    replacement = r'''@Composable
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
'''

    text = text[:start] + replacement + text[end:]

text = text.replace("CinematicGlitchMaterialize(", "CinematicSandMaterialize(")

text = text.replace(
    """                                    totalDurationMillis = 2_800,\n                                    shape = RoundedCornerShape(24.dp),""",
    """                                    totalDurationMillis = 3_200,\n                                    particleCount = 3_000,\n                                    accentColor = HarmonyPink,\n                                    flowDirection = 1f,\n                                    shape = RoundedCornerShape(24.dp),"""
)

text = text.replace(
    """                                        delayMillis = 650 + optIdx * 170,\n                                        totalDurationMillis = 2_100,\n                                        shape = RoundedCornerShape(18.dp),""",
    """                                        delayMillis = 760 + optIdx * 210,\n                                        totalDurationMillis = 2_400,\n                                        particleCount = 1_000,\n                                        accentColor = optionAccentColor(optIdx + 1),\n                                        flowDirection = if (optIdx % 2 == 0) 1f else -1f,\n                                        shape = RoundedCornerShape(18.dp),"""
)

if "private fun optionAccentColor(number: Int): Color" not in text:
    marker = "\n@Composable\nfun QuizOptionButton("
    helper = '''\nprivate fun optionAccentColor(number: Int): Color = when (number) {\n    1 -> Color(0xFF4AA8FF)\n    2 -> Color(0xFFFFC857)\n    3 -> Color(0xFF4ED69A)\n    4 -> Color(0xFFA978FF)\n    else -> Color(0xFFFF6B9D)\n}\n'''
    text = text.replace(marker, helper + marker, 1)

old_accent = '''    val optionAccent = when (number) {\n        1 -> Color(0xFF4AA8FF)\n        2 -> Color(0xFFFFC857)\n        3 -> Color(0xFF4ED69A)\n        4 -> Color(0xFFA978FF)\n        else -> Color(0xFFFF6B9D)\n    }'''
text = text.replace(old_accent, "    val optionAccent = optionAccentColor(number)")

path.write_text(text, encoding="utf-8")
print("Applied Nähe micro-sand particle materialization")
