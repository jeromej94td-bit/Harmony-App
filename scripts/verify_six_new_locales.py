#!/usr/bin/env python3
"""Regression checks for the six new production locales."""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
UI = ROOT / "app/src/main/java/com/example/ui"
sys.path.insert(0, str(ROOT / "scripts"))

from generate_six_new_locale_catalogs import kotlin_escape  # noqa: E402

language = (UI / "Language.kt").read_text(encoding="utf-8")
catalog = (UI / "TranslationCatalog.kt").read_text(encoding="utf-8")

LOCALES = {
    "DUTCH": ("nl", "DutchContent.kt", "EXACT_DUTCH_CONTENT", "localizeDutchDynamicContent"),
    "SWEDISH": ("sv", "SwedishContent.kt", "EXACT_SWEDISH_CONTENT", "localizeSwedishDynamicContent"),
    "ICELANDIC": ("is", "IcelandicContent.kt", "EXACT_ICELANDIC_CONTENT", "localizeIcelandicDynamicContent"),
    "KOREAN": ("ko", "KoreanContent.kt", "EXACT_KOREAN_CONTENT", "localizeKoreanDynamicContent"),
    "CHINESE_SIMPLIFIED": ("zh-CN", "ChineseSimplifiedContent.kt", "EXACT_CHINESE_SIMPLIFIED_CONTENT", "localizeChineseSimplifiedDynamicContent"),
    "CHINESE_TRADITIONAL": ("zh-TW", "ChineseTraditionalContent.kt", "EXACT_CHINESE_TRADITIONAL_CONTENT", "localizeChineseTraditionalDynamicContent"),
}

failed = False

def fail(message: str) -> None:
    global failed
    failed = True
    print(f"::error::{message}")

# The audit parser treats the canonical Kotlin \n escape as part of the stable key.
# Re-escaping it to \\n changes the generated key and drops multiline strings from coverage.
newline_key = r"first\nsecond"
if kotlin_escape(newline_key) != newline_key:
    fail("locale generator double-escapes stable Kotlin newline sequences")

for enum_name, (code, filename, exact_name, dynamic_name) in LOCALES.items():
    if f'{enum_name}("{code}"' not in language:
        fail(f"AppLanguage entry missing: {enum_name} ({code})")
    path = UI / filename
    if not path.exists():
        fail(f"locale source missing: {filename}")
        continue
    text = path.read_text(encoding="utf-8")
    if exact_name not in text:
        fail(f"exact catalog missing in {filename}: {exact_name}")
    if dynamic_name not in text:
        fail(f"dynamic localization helper missing in {filename}: {dynamic_name}")
    if r"\\$" in text:
        fail(f"double-escaped Kotlin dollar placeholder in {filename}")

    if enum_name not in catalog:
        fail(f"TranslationCatalog route missing: {enum_name}")
    if exact_name not in catalog:
        fail(f"TranslationCatalog exact map missing: {exact_name}")
    if dynamic_name not in catalog:
        fail(f"TranslationCatalog dynamic route missing: {dynamic_name}")

if failed:
    print("six-locale readiness FAILED")
    sys.exit(1)
print("six-locale readiness PASSED")
