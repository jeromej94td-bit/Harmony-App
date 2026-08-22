#!/usr/bin/env python3
"""Audit Japanese against the locked canonical German visible-copy inventory."""
from __future__ import annotations

from collections import Counter
import json
from pathlib import Path

from audit_localization import extract_map, parse_map_of
from visible_copy_canonical import extract_placeholders

ROOT = Path(__file__).resolve().parents[1]
UI = ROOT / "app/src/main/java/com/example/ui"
INVENTORY = ROOT / "localization/visible-copy-inventory.de.json"
REPORT = ROOT / "localization/japanese-visible-copy-missing.json"


def _normalize_kotlin_literal(value: str) -> str:
    # Supplemental Kotlin maps must escape $ so source templates such as ${pics.size}
    # compile as literal catalog keys. The static audit compares runtime string values.
    return value.replace(r"\$", "$")


def load_japanese_catalog() -> dict[str, str]:
    catalog = extract_map(UI / "JapaneseContent.kt", "EXACT_JAPANESE_CONTENT")
    updates_path = UI / "LocalizationUpdates.kt"
    if updates_path.exists():
        catalog.update(
            parse_map_of(
                updates_path.read_text(encoding="utf-8"),
                "LOCALIZATION_UPDATES_JAPANESE",
            )
        )
    overrides = UI / "JapaneseVisibleCopyOverrides.kt"
    if overrides.exists():
        raw_overrides = extract_map(overrides, "JAPANESE_VISIBLE_COPY_OVERRIDES")
        catalog.update(
            {
                _normalize_kotlin_literal(key): _normalize_kotlin_literal(value)
                for key, value in raw_overrides.items()
            }
        )
    return catalog


def main() -> int:
    payload = json.loads(INVENTORY.read_text(encoding="utf-8"))
    units = [unit for unit in payload["units"] if unit.get("exemption") is None]
    canonical = {unit["german"]: unit for unit in units}
    japanese = load_japanese_catalog()

    missing = sorted(set(canonical) - set(japanese))
    placeholder_mismatches: list[dict[str, object]] = []
    for german in sorted(set(canonical) & set(japanese)):
        expected = tuple(canonical[german].get("placeholders", []))
        actual = extract_placeholders(japanese[german])
        if Counter(actual) != Counter(expected):
            placeholder_mismatches.append(
                {
                    "german": german,
                    "japanese": japanese[german],
                    "expected": list(expected),
                    "actual": list(actual),
                }
            )

    report = {
        "canonical_translatable_units": len(canonical),
        "japanese_catalog_keys": len(japanese),
        "covered_canonical_units": len(canonical) - len(missing),
        "missing_count": len(missing),
        "placeholder_mismatch_count": len(placeholder_mismatches),
        "missing": missing,
        "placeholder_mismatches": placeholder_mismatches,
    }
    REPORT.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    print(
        f"Japanese visible-copy coverage: {report['covered_canonical_units']}/"
        f"{report['canonical_translatable_units']} | missing={len(missing)} | "
        f"placeholder_mismatches={len(placeholder_mismatches)}"
    )
    if missing:
        print("MISSING=" + " | ".join(missing))
    for item in placeholder_mismatches:
        print(
            "PLACEHOLDER_MISMATCH="
            + item["german"]
            + " -> "
            + item["japanese"]
            + f" expected={item['expected']} actual={item['actual']}"
        )

    return 1 if missing or placeholder_mismatches else 0


if __name__ == "__main__":
    raise SystemExit(main())
