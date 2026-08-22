#!/usr/bin/env python3
"""Quality-filtered canonical German visible-copy inventory for Harmony."""
from __future__ import annotations

import argparse
from collections import Counter
from dataclasses import asdict
import json
from pathlib import Path
import re

from visible_copy_complete import discover_repository as discover_complete_repository
from visible_copy_inventory import (
    InventoryReport,
    VisibleCopyOccurrence,
    _build_units,
    _dedupe_occurrences,
    word_count,
    write_report,
    write_summary,
)

TECHNICAL_GENERATED_LITERAL_RE = re.compile(r"^[a-z0-9][a-z0-9_.:-]*$")
LOCALE_ONLY_SOURCES = {
    "app/src/main/java/com/example/data/CuisinePackInstaller.kt",
}
PRECISE_SUPPLEMENT_SOURCES = {
    "app/src/main/java/com/example/data/DriveTotAssetInstaller.kt",
}


def _comment_only_lines(path: Path) -> set[int]:
    if not path.exists() or path.suffix != ".kt":
        return set()
    lines = path.read_text(encoding="utf-8", errors="replace").splitlines()
    comment_lines: set[int] = set()
    in_block = False
    for number, line in enumerate(lines, start=1):
        stripped = line.lstrip()
        if in_block:
            comment_lines.add(number)
            if "*/" in stripped:
                in_block = False
            continue
        if stripped.startswith("//"):
            comment_lines.add(number)
            continue
        if stripped.startswith("/*") or stripped.startswith("/**") or stripped.startswith("*"):
            comment_lines.add(number)
            if "*/" not in stripped:
                in_block = True
    return comment_lines


def _keep_occurrence(root: Path, occ: dict, comment_cache: dict[str, set[int]]) -> bool:
    rel = occ["path"]
    if rel in LOCALE_ONLY_SOURCES:
        return False

    if rel.endswith("GeneratedHarmonyContent.kt"):
        lines = comment_cache.setdefault(rel, _comment_only_lines(root / rel))
        if occ["line"] in lines:
            return False
        if occ["presentation"] == "product-content" and TECHNICAL_GENERATED_LITERAL_RE.fullmatch(occ["german"]):
            return False

    if rel in PRECISE_SUPPLEMENT_SOURCES and occ["presentation"] == "compose-ui":
        # DriveTotAssetInstaller is inventoried by its explicit option-map parser.
        # Generic proximity scanning around comments/map names is intentionally discarded.
        return False

    return True


def discover_repository(root: Path) -> InventoryReport:
    root = root.resolve()
    raw = discover_complete_repository(root)
    comment_cache: dict[str, set[int]] = {}
    occurrences = [
        VisibleCopyOccurrence(
            path=occ["path"],
            line=occ["line"],
            presentation=occ["presentation"],
            german=occ["german"],
            placeholders=tuple(occ.get("placeholders", [])),
            exemption=occ.get("exemption"),
        )
        for occ in raw.occurrences
        if _keep_occurrence(root, occ, comment_cache)
    ]
    occurrences = _dedupe_occurrences(occurrences)
    units = _build_units(occurrences)
    metrics = {
        "unique_visible_units_total": len(units),
        "unique_translatable_units": sum(1 for unit in units if not unit["exemption"]),
        "exempt_visible_units": sum(1 for unit in units if unit["exemption"]),
        "visible_render_occurrences": len(occurrences),
        "german_word_count": sum(word_count(unit["german"]) for unit in units if not unit["exemption"]),
    }
    breakdown = dict(sorted(Counter(occ.presentation for occ in occurrences).items()))
    return InventoryReport(
        units=units,
        occurrences=[asdict(occ) | {"placeholders": list(occ.placeholders)} for occ in occurrences],
        metrics=metrics,
        breakdown=breakdown,
    )


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument("--write", action="store_true")
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    root = args.root.resolve()
    report = discover_repository(root)
    inventory = root / "localization/visible-copy-inventory.de.json"
    summary = root / "localization/visible-copy-inventory-summary.md"

    if args.write:
        write_report(report, inventory)
        write_summary(report, summary)

    if args.check:
        import tempfile
        with tempfile.TemporaryDirectory() as tmp:
            inv = Path(tmp) / "inventory.json"
            summary_tmp = Path(tmp) / "summary.md"
            write_report(report, inv)
            write_summary(report, summary_tmp)
            stale = []
            if not inventory.exists() or inventory.read_bytes() != inv.read_bytes():
                stale.append(str(inventory.relative_to(root)))
            if not summary.exists() or summary.read_bytes() != summary_tmp.read_bytes():
                stale.append(str(summary.relative_to(root)))
            if stale:
                print("Visible-copy inventory is stale or missing: " + ", ".join(stale))
                return 1

    print(json.dumps(report.metrics, ensure_ascii=False, sort_keys=True))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
