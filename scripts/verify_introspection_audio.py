#!/usr/bin/env python3
"""Verify that every introspection audio asset is a decodable, non-empty stream."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
RAW = ROOT / "app/src/main/res/raw"
FILES = ("introspection_animal.mp3", "introspection_color.mp3", "introspection_reveal.mp3", "introspection_water.mp3", "merlin_theme.mp3")
for name in FILES:
    path = RAW / name
    if not path.is_file() or path.stat().st_size == 0:
        raise SystemExit(f"{name}: missing or empty")
    result = subprocess.run(["ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "default=nw=1:nk=1", str(path)], text=True, capture_output=True)
    if result.returncode != 0 or not result.stdout.strip():
        raise SystemExit(f"{name}: not decodable ({result.stderr.strip()})")
    print(f"{name}: {float(result.stdout.strip()):.3f}s")
