#!/usr/bin/env python3
"""Regression check for the original premium panda category artwork and animation.

The two panda PNGs and their established Compose motion are approved Harmony assets.
This guard intentionally fails if an automated repair/export/AI edit replaces the artwork,
changes the wiring, alters the original tilt/breathe/glow behavior, or moves runtime rendering
back to the drawable-nodpi-only path that Google AI Studio can omit from generated projects.
"""
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
VISUALS = ROOT / "app/src/main/java/com/example/ui/components/GameCategoryVisuals.kt"
REPAIR = ROOT / "scripts/repair_build_blockers.py"
DRAWABLE_NODPI = ROOT / "app/src/main/res/drawable-nodpi"
DRAWABLE = ROOT / "app/src/main/res/drawable"

# Git blob IDs of the approved original panda artwork restored in PR #39.
APPROVED_PANDA_BLOBS = {
    "panda_thinking_harmony.png": "4989d7b9b76d34a2204bfb8e3d91e4c46207e198",
    "panda_never_harmony.png": "f39cf088d38a88db17bf5f724a3752795bc9cb6d",
}

# Runtime aliases use the exact same Git blobs, but live in the standard drawable folder.
# This avoids Google AI Studio/export pipelines silently dropping drawable-nodpi binary assets.
STUDIO_PANDA_ALIASES = {
    "panda_thinking_harmony_studio.png": APPROVED_PANDA_BLOBS["panda_thinking_harmony.png"],
    "panda_never_harmony_studio.png": APPROVED_PANDA_BLOBS["panda_never_harmony.png"],
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


def verify_blob(asset: Path, expected_blob: str, label: str) -> None:
    require(asset.exists(), f"missing {label}: {asset.name}")
    if asset.exists():
        actual_blob = git_blob_sha(asset)
        require(actual_blob is not None, f"could not verify Git blob for {asset.name}")
        require(
            actual_blob == expected_blob,
            f"{label} changed: {asset.name} (expected {expected_blob}, got {actual_blob})",
        )


visuals = VISUALS.read_text(encoding="utf-8")
repair = REPAIR.read_text(encoding="utf-8")

# Keep the archival/restored originals untouched.
for filename, expected_blob in APPROVED_PANDA_BLOBS.items():
    verify_blob(DRAWABLE_NODPI / filename, expected_blob, "approved panda artwork")

# The standard-drawable runtime copies must be byte-for-byte identical to the approved originals.
for filename, expected_blob in STUDIO_PANDA_ALIASES.items():
    verify_blob(DRAWABLE / filename, expected_blob, "AI Studio-safe panda runtime artwork")

# Keep the original artwork wiring, but render from the AI-Studio-safe standard drawable aliases.
require('"wer" -> PandaArtworkIcon(' in visuals, 'wer category no longer uses PandaArtworkIcon')
require(
    'drawableRes = R.drawable.panda_thinking_harmony_studio' in visuals,
    'wer category is not wired to the AI Studio-safe original thinking panda',
)
require('animationLabel = "thinking_panda"' in visuals, 'thinking panda animation label changed')
require('"nie" -> PandaArtworkIcon(' in visuals, 'nie category no longer uses PandaArtworkIcon')
require(
    'drawableRes = R.drawable.panda_never_harmony_studio' in visuals,
    'nie category is not wired to the AI Studio-safe original never panda',
)
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

print("panda category artwork verification PASSED: original assets, AI Studio-safe runtime copies and animations are locked")
