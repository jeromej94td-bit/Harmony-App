#!/usr/bin/env python3
"""Regression guard for the subtle post-shuffle 3D settle motion."""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
SCREEN = ROOT / "app/src/main/java/com/example/ui/screens/QuizRunnerScreen.kt"
text = SCREEN.read_text(encoding="utf-8")

checks = {
    "settle marker": "tot_settle_wobble" in text,
    "subtle Z tilt": "topTilt.animateTo(0.9f" in text and "bottomTilt.animateTo(-0.9f" in text,
    "subtle Y depth": "topFlip.animateTo(2.4f" in text and "bottomFlip.animateTo(-2.4f" in text,
    "damped return": "topTilt.animateTo(0f, tween(180" in text and "topFlip.animateTo(0f, tween(180" in text,
    "clicks enabled before settle": text.find("isAnimating = false") < text.find("tot_settle_wobble") if "tot_settle_wobble" in text else False,
    "legacy converge remains absent": "topOffsetY.animateTo(52f" not in text and "bottomOffsetY.animateTo(-52f" not in text,
}

failed = [name for name, ok in checks.items() if not ok]
if failed:
    for name in failed:
        print(f"ERROR: {name}")
    print(f"tot settle wobble verification FAILED ({len(failed)} checks)")
    sys.exit(1)

print("tot settle wobble verification PASSED")
