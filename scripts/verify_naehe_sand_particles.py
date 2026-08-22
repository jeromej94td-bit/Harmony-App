#!/usr/bin/env python3
from pathlib import Path

# Final verification trigger: production source is unchanged; this reruns source invariants + Kotlin compile.
path = Path("app/src/main/java/com/example/ui/screens/QuizRunnerScreen.kt")
text = path.read_text(encoding="utf-8")

required = [
    "private fun CinematicSandMaterialize(",
    "particleCount: Int",
    "particleCount = 3_000",
    "particleCount = 1_000",
    "totalDurationMillis = 3_200",
    "totalDurationMillis = 2_400",
    "val contentAlpha = ((p - 0.72f) / 0.28f).coerceIn(0f, 1f)",
    "val particleRadius = 0.65f + hash01(index, 5) * 1.35f",
    "flowDirection = if (optIdx % 2 == 0) 1f else -1f",
    "val isIntimacyPack = pack.id == \"naehe\" && pack.topic == \"sex\"",
]

missing = [marker for marker in required if marker not in text]
if missing:
    raise SystemExit("Missing micro-particle materialization markers: " + ", ".join(missing))

for forbidden in [
    "private fun CinematicGlitchMaterialize(",
    "val columns = 9",
    "val rows = 6",
    "cellW * (0.72f",
]:
    if forbidden in text:
        raise SystemExit("Legacy coarse block-glitch marker still present: " + forbidden)

if text.count('val isIntimacyPack = pack.id == "naehe" && pack.topic == "sex"') != 1:
    raise SystemExit("Sand-particle animation must remain scoped exactly once to Nähe & Intimität")

print("Nähe micro-sand particle source invariants verified")
