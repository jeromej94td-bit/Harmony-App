#!/usr/bin/env python3
"""Regression check for the original premium panda category artwork and animation.

The panda PNGs are approved Harmony assets. Runtime copies intentionally live in the
standard Android drawable directory because external project/export tooling such as
Google AI Studio can omit drawable-nodpi binary resources while still retaining the
Compose container, which results in the black empty tile seen in the app.
"""
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
VISUALS = ROOT / "app/src/main/java/com/example/ui/components/GameCategoryVisuals.kt"
REPAIR = ROOT / "scripts/repair_build_blockers.py"
DRAWABLE = ROOT / "app/src/main/res/drawable"
LEGACY_NODPI = ROOT / "app/src/main/res/drawable-nodpi"

# Git blob IDs of the approved original panda artwork. Moving the files must never
# change their bytes or appearance.
APPROVED_PANDA_BLOBS = {
    "panda_thinking_harmony.png": "4989d7b9b76d34a2204bfb8e3d91e4c46207e198",
    "panda_never_harmony.png": "f39cf088d38a88db17bf5f724a3752795bc9cb6d",
}

errors: list[str] = []


def require(condition: bool, message: str) -> None:
    if not condition:
        errors.append(message)


def git_blob_sha(path: Path) -> str | None:
    try:
        return subprocess.run(
            ["git", "hash-object", str(path.relative_to(ROOT))],
            cwd=ROOT,
            check=True,
            capture_output=True,
            text=True,
        ).stdout.strip()
    except (OSError, subprocess.CalledProcessError):
        return None


visuals = VISUALS.read_text(encoding="utf-8")
repair = REPAIR.read_text(encoding="utf-8")

# The approved original PNG bytes must be available as ordinary Android drawables.
for filename, expected_blob in APPROVED_PANDA_BLOBS.items():
    asset = DRAWABLE / filename
    require(asset.exists(), f"missing approved panda runtime artwork: {filename}")
    if asset.exists():
        actual_blob = git_blob_sha(asset)
        require(actual_blob is not None, f"could not verify Git blob for {filename}")
        require(
            actual_blob == expected_blob,
            f"approved panda artwork changed: {filename} (expected {expected_blob}, got {actual_blob})",
        )

    # Do not move runtime rendering back to nodpi-only resources. That path caused the
    # exported/AI-Studio build to retain the dark frame while losing the actual panda.
    require(
        not (LEGACY_NODPI / filename).exists(),
        f"legacy drawable-nodpi panda resource restored: {filename}; keep runtime asset in drawable",
    )

# Keep the exact original artwork wiring/resource names.
require('"wer" -> PandaArtworkIcon(' in visuals, 'wer category no longer uses PandaArtworkIcon')
require('drawableRes = R.drawable.panda_thinking_harmony' in visuals, 'wer category is not wired to panda_thinking_harmony')
require('animationLabel = "thinking_panda"' in visuals, 'thinking panda animation label changed')
require('"nie" -> PandaArtworkIcon(' in visuals, 'nie category no longer uses PandaArtworkIcon')
require('drawableRes = R.drawable.panda_never_harmony' in visuals, 'nie category is not wired to panda_never_harmony')
require('animationLabel = "never_panda"' in visuals, 'never panda animation label changed')

# Preserve the approved slow tilt + breathing + glow motion exactly.
for snippet, message in (
    ('initialValue = -1.6f', 'premium panda tilt start changed'),
    ('targetValue = 1.6f', 'premium panda tilt end changed'),
    ('durationMillis = 11_000', 'premium panda slow tilt duration changed'),
    ('initialValue = 0.985f', 'premium panda breathing minimum changed'),
    ('targetValue = 1.025f', 'premium panda breathing maximum changed'),
    ('durationMillis = 3_200', 'premium panda breathing duration changed'),
    ('initialValue = 0.44f', 'premium panda glow minimum changed'),
    ('targetValue = 0.88f', 'premium panda glow maximum changed'),
    ('durationMillis = 2_400', 'premium panda glow duration changed'),
    ('rotationZ = tilt', 'premium panda tilt application changed'),
    ('scaleX = breathe', 'premium panda horizontal breathing changed'),
    ('scaleY = breathe', 'premium panda vertical breathing changed'),
    ('.size(76.dp)', 'premium panda outer size changed'),
    ('.clip(RoundedCornerShape(23.dp))', 'premium panda corner shape changed'),
    ('.background(Color(0xFF15091E))', 'premium panda background changed'),
    ('width = 1.4.dp', 'premium panda border width changed'),
    ('contentScale = ContentScale.Crop', 'premium panda image crop behavior changed'),
    ('modifier = Modifier.size(74.dp)', 'premium panda artwork size changed'),
):
    require(snippet in visuals, message)

# CI/build repair must never silently downgrade the artwork again.
require('new = \'\'\'        "wer" -> PandaCategoryIcon' not in repair, 'build repair still downgrades panda artwork to Canvas icons')
require('panda_thinking_harmony' not in repair or 'PandaCategoryIcon(categoryId = "wer"' not in repair, 'build repair still contains the old panda downgrade rule')

if errors:
    for error in errors:
        print(f"::error::{error}")
    print(f"panda category artwork verification FAILED ({len(errors)} errors)")
    sys.exit(1)

print("panda category artwork verification PASSED: original drawable assets and animations are locked")
