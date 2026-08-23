#!/usr/bin/env python3
"""Regression tests for the generic production-locale generator."""
from pathlib import Path
import importlib.util
import sys

ROOT = Path(__file__).resolve().parents[1]
GENERATOR = ROOT / "scripts/generate_production_locale_catalogs.py"

failed = False

def fail(message: str) -> None:
    global failed
    failed = True
    print(f"::error::{message}")

if not GENERATOR.exists():
    fail("generic production locale generator is missing")
else:
    spec = importlib.util.spec_from_file_location("production_generator", GENERATOR)
    module = importlib.util.module_from_spec(spec)
    assert spec and spec.loader
    spec.loader.exec_module(module)

    sample = r"$name · ${profile.partnerName} · {count} · %1$s · %d"
    protected, placeholders = module.protect(sample)
    if any(ch.isalpha() for ch in protected if ch not in sample.replace("$name", "").replace("${profile.partnerName}", "").replace("{count}", "").replace("%1$s", "").replace("%d", "")):
        fail("placeholder protection token contains alphabetic characters and can be transliterated")
    if any(token in protected for token in ("$name", "${profile.partnerName}", "{count}", "%1$s", "%d")):
        fail("not all dynamic placeholders are protected before translation")
    if module.restore(protected, placeholders) != sample:
        fail("placeholder protect/restore round-trip changed the source")

    newline_key = r"first\nsecond"
    if module.kotlin_escape(newline_key) != newline_key:
        fail("generator double-escapes stable Kotlin newline sequences")

    escaped = module.kotlin_escape(r"${profile.partnerName}")
    if r"\\$" in escaped:
        fail("generator creates double-escaped Kotlin dollar placeholders")

if failed:
    print("production locale generator regression FAILED")
    sys.exit(1)
print("production locale generator regression PASSED")
