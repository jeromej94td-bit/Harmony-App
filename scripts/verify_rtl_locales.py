#!/usr/bin/env python3
"""Verify RTL metadata for Arabic, Hebrew, Persian and Urdu."""
from pathlib import Path
import sys

from production_locale_registry import RTL_CODES

ROOT = Path(__file__).resolve().parents[1]
LANGUAGE = ROOT / "app/src/main/java/com/example/ui/Language.kt"
MANIFEST = ROOT / "app/src/main/AndroidManifest.xml"

failed = False

def fail(message: str) -> None:
    global failed
    failed = True
    print(f"::error::{message}")

language = LANGUAGE.read_text(encoding="utf-8")
manifest = MANIFEST.read_text(encoding="utf-8")

if 'android:supportsRtl="true"' not in manifest:
    fail("AndroidManifest.xml does not enable RTL")
if "val isRtl: Boolean" not in language:
    fail("AppLanguage.isRtl metadata is missing")
else:
    expected = ", ".join(f'"{code}"' for code in sorted(RTL_CODES))
    if f"setOf({expected})" not in language:
        fail(f"AppLanguage RTL set is not exactly: {expected}")

if failed:
    print("RTL locale verification FAILED")
    sys.exit(1)
print("RTL locale verification PASSED")
