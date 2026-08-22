#!/usr/bin/env python3
"""Complete Harmony visible-copy inventory, including non-Compose render APIs."""
from __future__ import annotations

import argparse
from collections import Counter
from dataclasses import asdict
import json
from pathlib import Path
import re

from visible_copy_inventory import (
    InventoryReport,
    VisibleCopyOccurrence,
    _build_units,
    _dedupe_occurrences,
    _decode_kotlin_string,
    _iter_kotlin_strings,
    _line_number,
    discover_repository as discover_core_repository,
    extract_placeholders,
    load_json,
    normalize_visible_text,
    word_count,
    write_report,
    write_summary,
)

NON_COMPOSE_RENDER_CALLS = (
    "setTextViewText",
    "setContentTitle",
    "setContentText",
    "setSubText",
    "setTicker",
)


def _scan_non_compose_render_calls(root: Path) -> list[VisibleCopyOccurrence]:
    out: list[VisibleCopyOccurrence] = []
    source_root = root / "app/src/main/java"
    if not source_root.exists():
        return out
    policy = load_json(root / "scripts/visible_copy_policy.json")
    excluded = policy.get("excluded_exact_visible_text", [])
    internal = policy.get("internal_exact_literals", [])
    brands = set(policy.get("brand_literals", []))

    for path in sorted(source_root.rglob("*.kt")):
        rel = path.relative_to(root).as_posix()
        if "DevStudioScreen.kt" in rel or "/data/Dev" in rel or "DeveloperDataManager.kt" in rel:
            continue
        source = path.read_text(encoding="utf-8", errors="replace")
        for value, start, end in _iter_kotlin_strings(source):
            before = source[max(0, start - 360):start]
            if not any(call in before for call in NON_COMPOSE_RENDER_CALLS):
                continue
            nearest = max((before.rfind(call) for call in NON_COMPOSE_RENDER_CALLS), default=-1)
            if nearest < 0 or len(before) - nearest > 300:
                continue
            text = normalize_visible_text(value)
            if not text or text in excluded or text in internal:
                continue
            if not re.search(r"[^\W\d_]", text, re.UNICODE):
                continue
            out.append(
                VisibleCopyOccurrence(
                    path=rel,
                    line=_line_number(source, start),
                    presentation="widget-notification",
                    german=text,
                    placeholders=extract_placeholders(text),
                    exemption="brand" if text in brands else None,
                )
            )
    return out


def discover_repository(root: Path) -> InventoryReport:
    root = root.resolve()
    base = discover_core_repository(root)
    occurrences = [
        VisibleCopyOccurrence(
            path=occ["path"],
            line=occ["line"],
            presentation=occ["presentation"],
            german=occ["german"],
            placeholders=tuple(occ.get("placeholders", [])),
            exemption=occ.get("exemption"),
        )
        for occ in base.occurrences
    ]
    occurrences.extend(_scan_non_compose_render_calls(root))
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
            expected_inventory = Path(tmp) / "inventory.json"
            expected_summary = Path(tmp) / "summary.md"
            write_report(report, expected_inventory)
            write_summary(report, expected_summary)
            stale = []
            if not inventory.exists() or inventory.read_bytes() != expected_inventory.read_bytes():
                stale.append(str(inventory.relative_to(root)))
            if not summary.exists() or summary.read_bytes() != expected_summary.read_bytes():
                stale.append(str(summary.relative_to(root)))
            if stale:
                print("Visible-copy inventory is stale or missing: " + ", ".join(stale))
                return 1

    print(json.dumps(report.metrics, ensure_ascii=False, sort_keys=True))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
