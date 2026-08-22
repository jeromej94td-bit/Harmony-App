#!/usr/bin/env python3
from pathlib import Path

path = Path("app/src/main/java/com/example/ui/screens/QuizRunnerScreen.kt")
text = path.read_text(encoding="utf-8")

required = [
    "private fun CinematicGlitchMaterialize(",
    "private fun cinematicGlitchText(",
    "glitchAmount: Float = 0f",
    "totalDurationMillis = 2_800",
    "totalDurationMillis = 2_100",
    "delayMillis = 650 + optIdx * 170",
    "val isIntimacyPack = pack.id == \"naehe\" && pack.topic == \"sex\"",
    "Color(0xFF7CF7FF)",
    "Color(0xFFFF63D6)",
]

missing = [marker for marker in required if marker not in text]
if missing:
    raise SystemExit("Missing cinematic Nähe markers: " + ", ".join(missing))

if "private fun MaterializeInEntrance(" in text:
    raise SystemExit("Legacy simple MaterializeInEntrance must be removed")

if text.count('val isIntimacyPack = pack.id == "naehe" && pack.topic == "sex"') != 1:
    raise SystemExit("Cinematic animation must remain scoped exactly once to Nähe & Intimität")

print("Cinematic Nähe source invariants verified")
# trigger-main-apply
