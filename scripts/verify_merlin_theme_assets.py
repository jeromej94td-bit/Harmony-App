#!/usr/bin/env python3
"""Verify that the split Merlin assets reconstruct the exact bundled OGG."""

from __future__ import annotations

import base64
import hashlib
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
CHUNK_DIR = REPO_ROOT / "app/src/main/assets/introspection"
RAW_THEME = REPO_ROOT / "app/src/main/res/raw/merlin_theme.ogg"
EXPECTED_CHUNKS = [f"merlin_theme_{index:02d}.b64" for index in range(1, 13)]
EXPECTED_SIZE = 134_361
EXPECTED_SHA256 = "43a81cfc7254d69dce6027d7fadbdef32f3ce70c27d7d99507a20fe02127de24"


def reconstruct() -> bytes:
    actual_chunks = sorted(path.name for path in CHUNK_DIR.glob("merlin_theme_*.b64"))
    if actual_chunks != EXPECTED_CHUNKS:
        raise AssertionError(
            f"unexpected Merlin chunks: expected {EXPECTED_CHUNKS}, got {actual_chunks}"
        )

    encoded = "".join(
        (CHUNK_DIR / chunk_name).read_text(encoding="ascii")
        for chunk_name in EXPECTED_CHUNKS
    )
    decoded = base64.b64decode("".join(encoded.split()), validate=True)
    if len(decoded) != EXPECTED_SIZE:
        raise AssertionError(
            f"wrong Merlin size: expected {EXPECTED_SIZE}, got {len(decoded)}"
        )

    digest = hashlib.sha256(decoded).hexdigest()
    if digest != EXPECTED_SHA256:
        raise AssertionError(
            f"wrong Merlin SHA-256: expected {EXPECTED_SHA256}, got {digest}"
        )
    return decoded


def main() -> None:
    reconstructed = reconstruct()
    if not RAW_THEME.is_file():
        raise AssertionError(f"generated Merlin resource is missing: {RAW_THEME}")
    if RAW_THEME.read_bytes() != reconstructed:
        raise AssertionError("generated Merlin resource differs from the verified chunks")
    print(
        f"Merlin theme verified: {len(reconstructed)} bytes, "
        f"sha256={EXPECTED_SHA256}"
    )


if __name__ == "__main__":
    main()
