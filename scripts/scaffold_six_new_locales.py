#!/usr/bin/env python3
"""Verify that the six production locale scaffolds already exist.

The original bootstrap mutations have been committed to the branch. Keeping this
step verification-only prevents CI from regenerating or overwriting production
locale support code on every pull-request run.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
UI = ROOT / "app/src/main/java/com/example/ui"

REQUIRED = [
    UI / "Language.kt",
    UI / "TranslationCatalog.kt",
    UI / "GeneratedLocaleSupport.kt",
    UI / "DutchContent.kt",
    UI / "SwedishContent.kt",
    UI / "IcelandicContent.kt",
    UI / "KoreanContent.kt",
    UI / "ChineseSimplifiedContent.kt",
    UI / "ChineseTraditionalContent.kt",
    UI / "introspection/IntrospectionStrings.kt",
]

missing = [str(path.relative_to(ROOT)) for path in REQUIRED if not path.exists()]
if missing:
    raise RuntimeError("Missing six-locale scaffold files: " + ", ".join(missing))

print("Six-locale scaffolding already present")
