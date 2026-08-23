#!/usr/bin/env python3
"""Regression guard for a rotation-axis-only post-shuffle settle."""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
SCREEN = ROOT / "app/src/main/java/com/example/ui/screens/QuizRunnerScreen.kt"
text = SCREEN.read_text(encoding="utf-8")

start = text.find("        if (skipNextTotEntrance) {")
end = text.find("            return@LaunchedEffect", start)
settle = text[start:end] if start >= 0 and end > start else ""

checks = {
    "settle block found": bool(settle),
    "settle uses only shuffle rotation axis": "topTilt.animateTo(" not in settle and "bottomTilt.animateTo(" not in settle,
    "first rotational overshoot": "topFlip.animateTo(2.0f, tween(120" in settle and "bottomFlip.animateTo(-2.0f, tween(120" in settle,
    "counter swing": "topFlip.animateTo(-1.0f, tween(150" in settle and "bottomFlip.animateTo(1.0f, tween(150" in settle,
    "small final swing": "topFlip.animateTo(0.35f, tween(130" in settle and "bottomFlip.animateTo(-0.35f, tween(130" in settle,
    "damped return": "topFlip.animateTo(0f, tween(180" in settle and "bottomFlip.animateTo(0f, tween(180" in settle,
    "clicks enabled before settle": text.find("isAnimating = false") < text.find("tot_settle_wobble") if "tot_settle_wobble" in text else False,
    "legacy converge remains absent": "topOffsetY.animateTo(52f" not in text and "bottomOffsetY.animateTo(-52f" not in text,
}

failed = [name for name, ok in checks.items() if not ok]
if failed:
    for name in failed:
        print(f"ERROR: {name}")
    print(f"tot settle wobble verification FAILED ({len(failed)} checks)")
    sys.exit(1)

print("tot settle wobble verification PASSED: rotation-axis-only settle")
