#!/usr/bin/env python3
"""Content audit for the 41 additional Harmony production locales."""
from collections import Counter
from pathlib import Path
import re
import sys

import audit_localization as audit
from production_locale_registry import NEW_LOCALES

ROOT = Path(__file__).resolve().parents[1]
UI = ROOT / "app/src/main/java/com/example/ui"
PLACEHOLDER_RE = re.compile(r'(\$\{[^}]+\}|\$[A-Za-z_][A-Za-z0-9_]*|\{[^}]+\}|%\d*\$?[a-zA-Z])')


def placeholders(text: str) -> Counter[str]:
    return Counter(PLACEHOLDER_RE.findall(text))


def fail(message: str) -> None:
    print(f"::error::{message}")


def main() -> int:
    canonical_all = audit.extract_map(UI / "EnglishContent.kt", "EXACT_ENGLISH_CONTENT")
    internal = getattr(audit, "INTERNAL_ONLY_KEYS", set())
    canonical = {
        key: value for key, value in canonical_all.items()
        if key not in audit.DEV_ONLY_KEYS and key not in internal and "Entwickler" not in key
    }
    if not canonical:
        fail("Could not parse canonical customer catalog")
        return 2

    print(f"Canonical customer catalog: {len(canonical)} keys")
    failed = False
    for item in NEW_LOCALES:
        path = UI / item["filename"]
        if not path.exists():
            fail(f"{item['code']}: missing catalog file {item['filename']}")
            failed = True
            continue
        catalog = audit.extract_map(path, item["exact"])
        missing = sorted(set(canonical) - set(catalog))
        if missing:
            fail(f"{item['code']}: missing {len(missing)} / {len(canonical)} customer keys")
            failed = True
            continue
        bad_placeholders = []
        leftovers = []
        blank = []
        for source in canonical:
            translated = catalog.get(source, "")
            if not translated.strip():
                blank.append(source)
            if placeholders(source) != placeholders(translated):
                bad_placeholders.append(source)
            if "HARMONYPLACEHOLDER" in translated.upper():
                leftovers.append(source)
        if blank:
            fail(f"{item['code']}: {len(blank)} blank translations")
            failed = True
        if bad_placeholders:
            fail(f"{item['code']}: {len(bad_placeholders)} placeholder mismatches")
            print("PLACEHOLDER_MISMATCH=" + " | ".join(bad_placeholders[:20]))
            failed = True
        if leftovers:
            fail(f"{item['code']}: unrestored translation placeholders remain")
            failed = True
        if not (blank or bad_placeholders or leftovers):
            print(f"{item['code']}: {len(catalog)} keys, coverage and placeholders OK")

    if failed:
        print("41-locale content audit FAILED")
        return 1
    print("41-locale content audit PASSED")
    return 0


if __name__ == "__main__":
    sys.exit(main())
