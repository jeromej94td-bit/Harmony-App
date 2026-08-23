#!/usr/bin/env python3
"""Regression guard for the This-or-That transition.

The fast card shuffle may reveal the incoming pair, but once that pair is visible it must
not run the old closing/converge motion or replay the wind entrance after the index changes.
"""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
SCREEN = ROOT / "app/src/main/java/com/example/ui/screens/QuizRunnerScreen.kt"
text = SCREEN.read_text(encoding="utf-8")

errors: list[str] = []

def require(condition: bool, message: str) -> None:
    if not condition:
        errors.append(message)

require("skipNextTotEntrance" in text, "missing guard that skips the duplicate incoming-pair entrance")
require("return@LaunchedEffect" in text, "duplicate entrance guard does not exit the entrance animation")
require("topOffsetY.animateTo(52f" not in text, "legacy final top-card converge animation is still present")
require("bottomOffsetY.animateTo(-52f" not in text, "legacy final bottom-card converge animation is still present")
require("topTilt.animateTo(-3.6f" not in text, "legacy final top tilt animation is still present")
require("bottomTilt.animateTo(3.6f" not in text, "legacy final bottom tilt animation is still present")
require("oderScale.animateTo(0f" not in text, "legacy final 'oder' shrink animation is still present")
require("delay(360)" not in text, "legacy pause before advancing to the incoming pair is still present")

if errors:
    for error in errors:
        print(f"ERROR: {error}")
    print(f"tot final-transition verification FAILED ({len(errors)} errors)")
    sys.exit(1)

print("tot final-transition verification PASSED: no converge/re-entry after final flip")
