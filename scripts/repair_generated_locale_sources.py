#!/usr/bin/env python3
"""Normalize generated Kotlin locale sources deterministically."""
from pathlib import Path
import re

from production_locale_registry import NEW_LOCALES

ROOT = Path(__file__).resolve().parents[1]
UI = ROOT / "app/src/main/java/com/example/ui"

for item in NEW_LOCALES:
    path = UI / item["filename"]
    if not path.exists():
        continue
    text = path.read_text(encoding="utf-8")
    # Any run of two or more literal backslashes before '$' must become the one
    # valid Kotlin dollar escape. Placeholders are protected before translation,
    # so variable identifiers themselves must never be rewritten.
    text = re.sub(r'\\{2,}\$', r'\\$', text)
    path.write_text(text, encoding="utf-8")

print("Generated production locale sources normalized")
