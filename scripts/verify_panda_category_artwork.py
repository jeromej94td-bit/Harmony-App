#!/usr/bin/env python3
"""Regression check for the premium panda category artwork and animation."""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
VISUALS = ROOT / "app/src/main/java/com/example/ui/components/GameCategoryVisuals.kt"
REPAIR = ROOT / "scripts/repair_build_blockers.py"
DRAWABLES = ROOT / "app/src/main/res/drawable-nodpi"

errors: list[str] = []


def require(condition: bool, message: str) -> None:
    if not condition:
        errors.append(message)


visuals = VISUALS.read_text(encoding="utf-8")
repair = REPAIR.read_text(encoding="utf-8")

require((DRAWABLES / "panda_thinking_harmony.png").exists(), "missing premium thinking panda artwork")
require((DRAWABLES / "panda_never_harmony.png").exists(), "missing premium never panda artwork")

require('"wer" -> PandaArtworkIcon(' in visuals, 'wer category no longer uses PandaArtworkIcon')
require('drawableRes = R.drawable.panda_thinking_harmony' in visuals, 'wer category is not wired to panda_thinking_harmony')
require('"nie" -> PandaArtworkIcon(' in visuals, 'nie category no longer uses PandaArtworkIcon')
require('drawableRes = R.drawable.panda_never_harmony' in visuals, 'nie category is not wired to panda_never_harmony')

# Preserve the softer original motion visible in the approved screenshots.
require('durationMillis = 11_000' in visuals, 'premium panda slow tilt animation was changed')
require('durationMillis = 3_200' in visuals, 'premium panda breathing animation was changed')
require('durationMillis = 2_400' in visuals, 'premium panda glow animation was changed')

# CI/build repair must never silently downgrade the artwork again.
require('new = \'\'\'        "wer" -> PandaCategoryIcon' not in repair, 'build repair still downgrades panda artwork to Canvas icons')
require('panda_thinking_harmony' not in repair or 'PandaCategoryIcon(categoryId = "wer"' not in repair, 'build repair still contains the old panda downgrade rule')

if errors:
    for error in errors:
        print(f"::error::{error}")
    print(f"panda category artwork verification FAILED ({len(errors)} errors)")
    sys.exit(1)

print("panda category artwork verification PASSED")
