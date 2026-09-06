from pathlib import Path
import base64
import hashlib

ROOT = Path('.')
models_path = ROOT / 'app/src/main/java/com/example/data/model/Models.kt'
runner_path = ROOT / 'app/src/main/java/com/example/ui/screens/QuizRunnerScreen.kt'
asset_path = ROOT / 'app/src/main/res/drawable-nodpi/egg_cooking_guide.webp'

# Rebuild the generated artwork without relying on binary GitHub content writes.
expected_hashes = [
    '215871df58eaa756dba67a21fa0bb416abc1d51240c9f4153342ee5a26ad6424',
    'dcb044857ee8be62b77cefdc53620651d980c8c56363d381760d59ea769196d2',
    'a76286769ab566a8b3574fa749901e64f772e239dfa83413c58bb6f2843f325f',
    'd8708247bd88d1ae5b72e40565f2cebf284144e021a5e60500071a8361451392',
    '97a44edf8775cd875dc98c2f4fdccd7ec2bffbf3ded659838346302570e84043',
    'd8e59ce9481ea5b8f2f9f000fee958903e474aae5ef765ac82cc70e521a6baaf',
    '1777bda2dea59045d78b2ed5a8b7b6e2538016ddac9b3b50027ee55eefcbb719',
]
parts = []
for index in range(6):
    parts.append((ROOT / f'scripts/egg_asset_chunks/part{index:02d}.txt').read_text(encoding='utf-8').strip())
parts.append(bytes.fromhex((ROOT / 'scripts/egg_asset_chunks/part06.hex.txt').read_text(encoding='utf-8').strip()).decode('utf-8'))
for index, part in enumerate(parts):
    digest = hashlib.sha256(part.encode('utf-8')).hexdigest()
    print(f'chunk {index}: len={len(part)} first_equals={part.find("=")} sha256={digest}')
    if digest != expected_hashes[index]:
        raise RuntimeError(f'egg artwork chunk {index} checksum mismatch')
asset_bytes = base64.b64decode(''.join(parts), validate=True)
if len(asset_bytes) < 20_000:
    raise RuntimeError(f'egg artwork unexpectedly small: {len(asset_bytes)} bytes')
asset_path.parent.mkdir(parents=True, exist_ok=True)
asset_path.write_bytes(asset_bytes)

models = models_path.read_text(encoding='utf-8')
if 'Wie möchtest du dein Ei am liebsten?' not in models:
    old_question = '''                Question(
                    "Wer entscheidet bei euch schneller, was bestellt wird?",
                    listOf(
                        "{user}",
                        "{partner}",
                        "Wir brauchen ewig",
                        "Wir bestellen einfach beides"
                    )
                )
'''
    new_question = old_question.rstrip('\n') + ''',
                Question(
                    "Wie möchtest du dein Ei am liebsten?",
                    listOf(
                        "4 Minuten – Sehr flüssig",
                        "5 Minuten – Flüssig",
                        "6 Minuten – Weich & cremig",
                        "7 Minuten – Weiches Eigelb",
                        "8 Minuten – Cremiges Eigelb",
                        "9 Minuten – Fast fest",
                        "10 Minuten – Fest",
                        "11 Minuten – Ziemlich fest",
                        "12 Minuten – Sehr fest",
                        "13 Minuten – Trocken",
                        "14 Minuten – Sehr trocken",
                        "15 Minuten – Übergart"
                    )
                )
'''
    if old_question not in models:
        raise RuntimeError('could not find Essen & Genuss third question anchor')
    models = models.replace(old_question, new_question, 1)
    models_path.write_text(models, encoding='utf-8')

runner = runner_path.read_text(encoding='utf-8')

def add_import(anchor: str, addition: str) -> None:
    global runner
    if addition.strip() not in runner:
        if anchor not in runner:
            raise RuntimeError(f'missing import anchor: {anchor!r}')
        runner = runner.replace(anchor, anchor + addition, 1)

add_import('import androidx.compose.foundation.layout.Arrangement\n', 'import androidx.compose.foundation.layout.aspectRatio\n')
add_import('import androidx.compose.ui.graphics.Color\n', 'import androidx.compose.ui.graphics.ImageBitmap\nimport androidx.compose.ui.graphics.TransformOrigin\n')
add_import('import androidx.compose.ui.res.stringResource\n', 'import androidx.compose.ui.res.imageResource\n')
add_import('import androidx.compose.ui.unit.dp\n', 'import androidx.compose.ui.unit.IntOffset\nimport androidx.compose.ui.unit.IntSize\n')
add_import('import kotlin.math.sin\n', 'import kotlin.math.roundToInt\n')

if 'val isEggCookingQuestion = pack.id == "essenreden" && activeRun.currentIndex == 3' not in runner:
    anchor = '                        val isIntimacyPack = pack.id == "naehe" && pack.topic == "sex"\n'
    if anchor not in runner:
        raise RuntimeError('missing intimacy pack anchor')
    runner = runner.replace(
        anchor,
        anchor + '                        val isEggCookingQuestion = pack.id == "essenreden" && activeRun.currentIndex == 3\n',
        1,
    )

if 'EggCookingQuestionGrid(' not in runner.split('// Standard Quiz Mode', 1)[1]:
    old_question_render = '''                            if (isIntimacyPack) {
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
                                        question = contentText(q?.q ?: ""),
                                        glitchAmount = glitchAmount
                                    )
                                }
                            } else {
                                AnimatedQuestionCard(question = contentText(q?.q ?: ""))
                            }
'''
    new_question_render = '''                            if (isEggCookingQuestion) {
                                EggCookingQuestionGrid(
                                    animationKey = questionAnimationKey,
                                    question = contentText(q?.q ?: ""),
                                    options = q?.options ?: emptyList(),
                                    selectedAnswer = selectedAns,
                                    onPick = { answer ->
                                        triggerMiniVibration(context, 40L)
                                        onPickAnswer(answer)
                                    }
                                )
                            } else if (isIntimacyPack) {
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
                                        question = contentText(q?.q ?: ""),
                                        glitchAmount = glitchAmount
                                    )
                                }
                            } else {
                                AnimatedQuestionCard(question = contentText(q?.q ?: ""))
                            }
'''
    if old_question_render not in runner:
        raise RuntimeError('could not find standard question renderer')
    runner = runner.replace(old_question_render, new_question_render, 1)

if 'if (!isEggCookingQuestion) {' not in runner:
    options_anchor = '                            val rawOptions = q?.options ?: emptyList()\n'
    if options_anchor not in runner:
        raise RuntimeError('missing raw options anchor')
    runner = runner.replace(
        options_anchor,
        '                            if (!isEggCookingQuestion) {\n' + options_anchor,
        1,
    )
    tail = '''                                        modifier = Modifier.padding(bottom = 11.dp)
                                    )
                                }
                            }
                        }
                    }
'''
    replacement = '''                                        modifier = Modifier.padding(bottom = 11.dp)
                                    )
                                }
                            }
                            }
                        }
                    }
'''
    if tail not in runner:
        raise RuntimeError('missing standard options tail anchor')
    runner = runner.replace(tail, replacement, 1)

if 'private fun EggCookingQuestionGrid(' not in runner:
    marker = 'private fun optionAccentColor(number: Int): Color = when (number) {'
    if marker not in runner:
        raise RuntimeError('missing optionAccentColor marker')
    helper = r'''
private data class EggGuideCrop(
    val offset: IntOffset,
    val size: IntSize
)

private fun eggCookingGuideCrop(index: Int): EggGuideCrop {
    val columns = arrayOf(20 to 138, 144 to 270, 275 to 395)
    val rows = arrayOf(149 to 292, 296 to 436, 441 to 576, 581 to 713)
    val safeIndex = index.coerceIn(0, 11)
    val column = safeIndex % 3
    val row = safeIndex / 3
    val (left, right) = columns[column]
    val (top, bottom) = rows[row]
    return EggGuideCrop(
        offset = IntOffset(left, top),
        size = IntSize(right - left, bottom - top)
    )
}

@Composable
private fun EggCookingQuestionGrid(
    animationKey: Any,
    question: String,
    options: List<String>,
    selectedAnswer: String?,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val guide = ImageBitmap.imageResource(R.drawable.egg_cooking_guide)
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        HarmonyPurple.copy(alpha = 0.42f),
                        HarmonyPink.copy(alpha = 0.16f),
                        HarmonySurface2,
                        HarmonyBg
                    )
                )
            )
            .border(1.4.dp, HarmonyPink.copy(alpha = 0.48f), shape)
            .padding(horizontal = 12.dp, vertical = 16.dp)
            .testTag("egg_cooking_question")
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(HarmonyPink.copy(alpha = 0.86f), HarmonyPurple.copy(alpha = 0.92f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🍳", fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.height(9.dp))
            Text(
                text = question,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = HarmonyText,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tr("Tippe auf deine liebste Garstufe", "Tap your preferred doneness"),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = HarmonyMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))

            options.take(12).chunked(3).forEachIndexed { row, rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    rowOptions.forEachIndexed { column, option ->
                        val index = row * 3 + column
                        EggCookingOptionCard(
                            animationKey = "${animationKey}_$index",
                            index = index,
                            option = option,
                            guide = guide,
                            selected = selectedAnswer == option,
                            onClick = { onPick(option) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if (row < 3) Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun EggCookingOptionCard(
    animationKey: Any,
    index: Int,
    option: String,
    guide: ImageBitmap,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reveal = remember(animationKey) { Animatable(0f) }
    val density = LocalDensity.current.density
    val row = index / 3
    val column = index % 3
    val delayMillis = row * 420L + column * 110L
    val crop = eggCookingGuideCrop(index)
    val shape = RoundedCornerShape(18.dp)

    LaunchedEffect(animationKey) {
        reveal.snapTo(0f)
        delay(delayMillis)
        reveal.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 430, easing = FastOutSlowInEasing)
        )
    }

    val progress = reveal.value.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .aspectRatio(0.82f)
            .graphicsLayer {
                alpha = progress
                rotationY = -82f * (1f - progress)
                translationX = -18f * (1f - progress)
                scaleX = 0.94f + progress * 0.06f
                scaleY = 0.96f + progress * 0.04f
                transformOrigin = TransformOrigin(0f, 0.5f)
                cameraDistance = 26f * density
            }
            .clip(shape)
            .background(HarmonySurface)
            .border(
                width = if (selected) 2.2.dp else 1.dp,
                color = if (selected) HarmonyPink else Color.White.copy(alpha = 0.18f),
                shape = shape
            )
            .clickable(enabled = progress > 0.86f, onClick = onClick)
            .testTag("egg_cooking_option_$index")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawImage(
                image = guide,
                srcOffset = crop.offset,
                srcSize = crop.size,
                dstOffset = IntOffset.Zero,
                dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt())
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            HarmonyPurple.copy(alpha = if (selected) 0.08f else 0.02f)
                        )
                    )
                )
        )

        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(7.dp)
                    .size(23.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(HarmonyPink, HarmonyPurple))),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✓", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

'''
    runner = runner.replace(marker, helper + marker, 1)

runner_path.write_text(runner, encoding='utf-8')
print(f'egg cooking game applied; artwork={len(asset_bytes)} bytes')
