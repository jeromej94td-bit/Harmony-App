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
    # PicShare wraps app-owned widget copy in this locale-aware helper before passing it
    # to RemoteViews. It still represents one concrete visible widget occurrence.
    "appText",
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


def _extract_map_labels(source: str, map_name: str) -> list[tuple[str, int]]:
    marker = re.search(rf"\b{re.escape(map_name)}\b[^=]*=\s*linkedMapOf\s*\(", source)
    if not marker:
        return []
    pos = marker.end()
    depth = 1
    in_string = False
    escaped = False
    end = pos
    while end < len(source) and depth:
        ch = source[end]
        if in_string:
            if escaped:
                escaped = False
            elif ch == "\\":
                escaped = True
            elif ch == '"':
                in_string = False
        else:
            if ch == '"':
                in_string = True
            elif ch == '(':
                depth += 1
            elif ch == ')':
                depth -= 1
        end += 1
    body = source[pos:end - 1]
    pattern = re.compile(r'^\s*"((?:\\.|[^"\\])*)"\s+to\s+"(?:\\.|[^"\\])*",?\s*(?://.*)?$', re.MULTILINE)
    result: list[tuple[str, int]] = []
    for match in pattern.finditer(body):
        absolute = pos + match.start(1)
        result.append((_decode_kotlin_string(match.group(1), False), absolute))
    return result


def _scan_bundled_option_labels(root: Path) -> list[VisibleCopyOccurrence]:
    path = root / "app/src/main/java/com/example/data/DriveTotAssetInstaller.kt"
    if not path.exists():
        return []
    source = path.read_text(encoding="utf-8", errors="replace")
    policy = load_json(root / "scripts/visible_copy_policy.json")
    brands = set(policy.get("brand_literals", []))
    rel = path.relative_to(root).as_posix()
    out: list[VisibleCopyOccurrence] = []
    seen_labels: set[str] = set()
    for map_name in ("driveOptionToFile", "brandOptionToFile"):
        for text, offset in _extract_map_labels(source, map_name):
            normalized = normalize_visible_text(text)
            if not normalized or normalized in seen_labels:
                continue
            seen_labels.add(normalized)
            out.append(
                VisibleCopyOccurrence(
                    path=rel,
                    line=_line_number(source, offset),
                    presentation="bundled-image-option",
                    german=normalized,
                    placeholders=extract_placeholders(normalized),
                    exemption="brand" if normalized in brands else None,
                )
            )
    return out


def _scan_cuisine_pack_labels(root: Path) -> list[VisibleCopyOccurrence]:
    path = root / "app/src/main/java/com/example/data/CuisinePackInstaller.kt"
    if not path.exists():
        return []
    source = path.read_text(encoding="utf-8", errors="replace")
    rel = path.relative_to(root).as_posix()
    out: list[VisibleCopyOccurrence] = []

    for match in re.finditer(r'\btitle\s*=\s*"((?:\\.|[^"\\])*)"', source):
        text = normalize_visible_text(_decode_kotlin_string(match.group(1), False))
        if text:
            out.append(
                VisibleCopyOccurrence(
                    path=rel,
                    line=_line_number(source, match.start(1)),
                    presentation="product-content",
                    german=text,
                    placeholders=extract_placeholders(text),
                )
            )

    for list_match in re.finditer(r'\bpairs\s*=\s*listOf\s*\(', source):
        pos = list_match.end()
        depth = 1
        in_string = False
        escaped = False
        end = pos
        while end < len(source) and depth:
            ch = source[end]
            if in_string:
                if escaped:
                    escaped = False
                elif ch == "\\":
                    escaped = True
                elif ch == '"':
                    in_string = False
            else:
                if ch == '"':
                    in_string = True
                elif ch == '(':
                    depth += 1
                elif ch == ')':
                    depth -= 1
            end += 1
        body = source[pos:end - 1]
        pair_re = re.compile(r'"((?:\\.|[^"\\])*)"\s+to\s+"((?:\\.|[^"\\])*)"')
        for match in pair_re.finditer(body):
            for group_index in (1, 2):
                text = normalize_visible_text(_decode_kotlin_string(match.group(group_index), False))
                if not text:
                    continue
                out.append(
                    VisibleCopyOccurrence(
                        path=rel,
                        line=_line_number(source, pos + match.start(group_index)),
                        presentation="product-content",
                        german=text,
                        placeholders=extract_placeholders(text),
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
        if not occ["path"].endswith("CuisinePackInstaller.kt")
    ]
    occurrences.extend(_scan_cuisine_pack_labels(root))
    occurrences.extend(_scan_non_compose_render_calls(root))
    occurrences.extend(_scan_bundled_option_labels(root))
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
