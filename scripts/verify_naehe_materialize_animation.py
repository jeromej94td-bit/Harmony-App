#!/usr/bin/env python3
from pathlib import Path

path = Path("app/src/main/java/com/example/ui/screens/QuizRunnerScreen.kt")
text = path.read_text(encoding="utf-8")

required = [
    'private fun MaterializeInEntrance(',
    'val isIntimacyPack = pack.id == "naehe" && pack.topic == "sex"',
    'animationKey = questionAnimationKey',
    'delayMillis = 130 + optIdx * 65',
    'shape = RoundedCornerShape(24.dp)',
    'shape = RoundedCornerShape(18.dp)',
]

missing = [marker for marker in required if marker not in text]
if missing:
    raise SystemExit("Missing Nähe materialize markers: " + ", ".join(missing))

if text.count('val isIntimacyPack = pack.id == "naehe" && pack.topic == "sex"') != 1:
    raise SystemExit("Nähe animation guard must occur exactly once")

if 'if (isIntimacyPack) {' not in text or '} else {\n                                AnimatedQuestionCard' not in text:
    raise SystemExit("Question animation must preserve the existing UI path for all other quiz packs")

if 'if (isIntimacyPack) {' not in text or 'modifier = Modifier.padding(bottom = 11.dp)' not in text:
    raise SystemExit("Option animation must preserve the existing UI path for all other quiz packs")

print("Nähe materialize source invariants verified")
