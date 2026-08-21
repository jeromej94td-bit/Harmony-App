#!/usr/bin/env python3
"""Materialize the six custom_logo image files already embedded in Kotlin.

This keeps Google AI Studio self-contained: cloning the repository gives it
real image files at the exact paths declared by harmony-export-manifest.json.
"""

from __future__ import annotations

import base64
import io
import re
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
DATA_DIR = ROOT / "app" / "src" / "main" / "java" / "com" / "example" / "data"

ASSETS = [
    ("GeneratedHarmonyNewPicGameImage0.kt", "images/custom_logo/pair-001/a/1000110101.png", "PNG"),
    ("GeneratedHarmonyNewPicGameImage1.kt", "images/custom_logo/pair-001/b/1000110102.png", "PNG"),
    ("GeneratedHarmonyNewPicGameImage2.kt", "images/custom_logo/pair-002/a/1000110103.png", "PNG"),
    ("GeneratedHarmonyNewPicGameImage3.kt", "images/custom_logo/pair-002/b/1000110104.png", "PNG"),
    ("GeneratedHarmonyNewPicGameImage4.kt", "images/custom_logo/pair-003/a/1000110105.png", "PNG"),
    ("GeneratedHarmonyNewPicGameImage5.kt", "images/custom_logo/pair-003/b/1000110111.jpg", "JPEG"),
]

APPEND_RE = re.compile(r'append\("([^\"]*)"\)')


def embedded_bytes(source_name: str) -> bytes:
    source = (DATA_DIR / source_name).read_text(encoding="utf-8")
    chunks = APPEND_RE.findall(source)
    if not chunks:
        raise RuntimeError(f"No Base64 append chunks found in {source_name}")

    payload = "".join(chunks)
    payload = re.sub(r"\s+", "", payload)
    payload += "=" * (-len(payload) % 4)
    raw = base64.b64decode(payload, validate=False)
    if not raw:
        raise RuntimeError(f"Decoded image is empty: {source_name}")
    return raw


def materialize(source_name: str, target_name: str, target_format: str) -> None:
    raw = embedded_bytes(source_name)
    target = ROOT / target_name
    target.parent.mkdir(parents=True, exist_ok=True)

    with Image.open(io.BytesIO(raw)) as image:
        image.load()
        if target_format == "JPEG" and image.format == "JPEG":
            target.write_bytes(raw)
        else:
            converted = image.convert("RGBA") if image.mode in {"RGBA", "LA"} else image.convert("RGB")
            converted.save(target, format=target_format, optimize=True)

    with Image.open(target) as verification_image:
        verification_image.verify()

    if not target.exists() or target.stat().st_size == 0:
        raise RuntimeError(f"Failed to materialize {target_name}")
    print(f"materialized {target_name} ({target.stat().st_size} bytes)")


def main() -> None:
    for source_name, target_name, target_format in ASSETS:
        materialize(source_name, target_name, target_format)


if __name__ == "__main__":
    main()
