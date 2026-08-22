#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PATH = ROOT / "app/src/main/java/com/example/ui/screens/QuizRunnerScreen.kt"
text = PATH.read_text(encoding="utf-8")

replacements = [
    (
        "import kotlinx.coroutines.delay\n",
        "",
    ),
    (
        "    var isAnimating by remember { mutableStateOf(false) }\n"
        "    var topShuffleKey by remember(firstText, secondText) { mutableStateOf(firstText) }\n",
        "    var isAnimating by remember { mutableStateOf(false) }\n"
        "    var skipNextTotEntrance by remember { mutableStateOf(false) }\n"
        "    var topShuffleKey by remember(firstText, secondText) { mutableStateOf(firstText) }\n",
    ),
    (
        "    LaunchedEffect(firstText, secondText) {\n"
        "        topOffsetY.snapTo(-windDistancePx)\n",
        "    LaunchedEffect(firstText, secondText) {\n"
        "        if (skipNextTotEntrance) {\n"
        "            topOffsetY.snapTo(0f)\n"
        "            bottomOffsetY.snapTo(0f)\n"
        "            topTilt.snapTo(0f)\n"
        "            bottomTilt.snapTo(0f)\n"
        "            oderScale.snapTo(1f)\n"
        "            topFlip.snapTo(0f)\n"
        "            bottomFlip.snapTo(0f)\n"
        "            skipNextTotEntrance = false\n"
        "            return@LaunchedEffect\n"
        "        }\n"
        "\n"
        "        topOffsetY.snapTo(-windDistancePx)\n",
    ),
    (
        "            coroutineScope {\n"
        "                launch { topOffsetY.animateTo(52f, tween(540, easing = CubicBezierEasing(0.16f, 0.78f, 0.2f, 1f))) }\n"
        "                launch { bottomOffsetY.animateTo(-52f, tween(540, easing = CubicBezierEasing(0.16f, 0.78f, 0.2f, 1f))) }\n"
        "                launch { topTilt.animateTo(-3.6f, tween(540, easing = FastOutSlowInEasing)) }\n"
        "                launch { bottomTilt.animateTo(3.6f, tween(540, easing = FastOutSlowInEasing)) }\n"
        "                launch { oderScale.animateTo(0f, tween(260, easing = FastOutSlowInEasing)) }\n"
        "            }\n"
        "            delay(360)\n"
        "            onPick(option)\n"
        "            topShuffleKey = firstText\n"
        "            bottomShuffleKey = secondText\n"
        "            isAnimating = false\n",
        "            // The shuffle already ends on the incoming pair. Keep it in place.\n"
        "            // Do not converge the cards or replay the wind entrance after the index update.\n"
        "            skipNextTotEntrance = true\n"
        "            onPick(option)\n"
        "            isAnimating = false\n",
    ),
]

for index, (old, new) in enumerate(replacements, start=1):
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"replacement {index} expected exactly once, found {count}")
    text = text.replace(old, new, 1)

PATH.write_text(text, encoding="utf-8")
print("Applied minimal Das-oder-das final-transition patch")
