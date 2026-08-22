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
    write_report,
    write_summary,
)

KOTLIN_PLACEHOLDER_RE = re.compile(
    r"\$\{[^{}]+\}|\$[A-Za-z_][A-Za-z0-9_.]*|\{[A-Za-z_][A-Za-z0-9_.]*\}|%(?:\d+\$)?0?\d*[sd]"
)
WORD_RE = re.compile(r"[^\W_]+(?:[’'\-‑][^\W_]+)*", re.UNICODE)
TECHNICAL_GENERATED_LITERAL_RE = re.compile(r"^[a-z0-9][a-z0-9_.:-]*$")
SNAKE_IDENTIFIER_RE = re.compile(r"^[A-Za-z][A-Za-z0-9]*(?:_[A-Za-z0-9${}.]+)+$")
LOWER_CAMEL_RE = re.compile(r"^[a-z][a-z0-9]*(?:[A-Z][A-Za-z0-9]*)+$")
LOWER_IDENTIFIER_RE = re.compile(r"^[a-zäöüß][a-z0-9äöüß.-]*$")
BASE64ISH_RE = re.compile(r"^[A-Za-z0-9+/=]+$")

LOCALE_ONLY_SOURCES = {
    "app/src/main/java/com/example/data/CuisinePackInstaller.kt",
}
PRECISE_SUPPLEMENT_SOURCES = {
    "app/src/main/java/com/example/data/DriveTotAssetInstaller.kt",
}
LOWERCASE_VISIBLE_ALLOW = {"oder"}
DEV_STUDIO_TEXTS = {
    "Entwickler Studio Öffnen",
    "Entwickler-Modus",
    "🛠️ Entwickler-Modus",
    "Spiele & Städte bearbeiten, Ordner reinladen, Bilder anpassen",
    "🛠️ Developer mode",
    "Open Developer Studio",
    "Edit games and destinations, import folders, adjust images",
    "Titel, Kategorie, Thema, Tags, Fragen...",
}
TECHNICAL_EXACT = {
    "image/*",
    "me",
    "them",
    "de",
    "id",
    "name",
    "title",
    "value",
    "caption",
    "cat",
    "type",
    "side",
    "count",
    "text",
    "audio",
    "voice",
    "avatars",
    "supabase",
    "disc",
    "pairIndex",
    "packId",
    "slotB",
    "templateA",
    "question_index",
    "category_id",
    "tag_color_hex",
}
CODE_FRAGMENT_TOKENS = (
    "LanguageManager.tr(",
    "appLanguage)}",
    "contentText(",
    "R.drawable.",
    "R.string.",
)
BRAND_EXACT = {"Harmony", "HARMONY"}


def extract_placeholders(text: str) -> tuple[str, ...]:
    return tuple(match.group(0) for match in KOTLIN_PLACEHOLDER_RE.finditer(text))


def canonical_word_count(text: str) -> int:
    clean = KOTLIN_PLACEHOLDER_RE.sub(" ", text)
    return len(WORD_RE.findall(clean))


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


def _is_base64_blob(text: str) -> bool:
    compact = text.strip()
    return len(compact) > 256 and " " not in compact[:200] and bool(BASE64ISH_RE.fullmatch(compact))


def _has_translatable_letters(text: str) -> bool:
    residual = KOTLIN_PLACEHOLDER_RE.sub("", text)
    residual = re.sub(r"[%0-9:./&+\-–—·↔„“'\"()\[\] ]+", "", residual)
    return bool(re.search(r"[^\W\d_]", residual, re.UNICODE))


def _looks_technical_identifier(text: str) -> bool:
    if text in LOWERCASE_VISIBLE_ALLOW:
        return False
    if text in TECHNICAL_EXACT:
        return True
    if SNAKE_IDENTIFIER_RE.fullmatch(text):
        return True
    if LOWER_CAMEL_RE.fullmatch(text):
        return True
    if LOWER_IDENTIFIER_RE.fullmatch(text):
        return True
    if "_" in text and not any(ch.isspace() for ch in text):
        return True
    return False


def _keep_occurrence(root: Path, occ: dict, comment_cache: dict[str, set[int]]) -> bool:
    rel = occ["path"]
    text = occ["german"].strip()

    if rel in LOCALE_ONLY_SOURCES:
        return False
    if text in DEV_STUDIO_TEXTS:
        return False
    if _is_base64_blob(text):
        return False
    if any(token in text for token in CODE_FRAGMENT_TOKENS):
        return False
    if not _has_translatable_letters(text):
        return False
    if _looks_technical_identifier(text):
        return False

    if rel.endswith("GeneratedHarmonyContent.kt"):
        lines = comment_cache.setdefault(rel, _comment_only_lines(root / rel))
        if occ["line"] in lines:
            return False
        if TECHNICAL_GENERATED_LITERAL_RE.fullmatch(text):
            return False

    if rel in PRECISE_SUPPLEMENT_SOURCES:
        return occ["presentation"] == "bundled-image-option"

    return True


def discover_repository(root: Path) -> InventoryReport:
    root = root.resolve()
    raw = discover_complete_repository(root)
    comment_cache: dict[str, set[int]] = {}
    occurrences: list[VisibleCopyOccurrence] = []
    for occ in raw.occurrences:
        if not _keep_occurrence(root, occ, comment_cache):
            continue
        text = occ["german"]
        exemption = occ.get("exemption") or ("brand" if text in BRAND_EXACT else None)
        occurrences.append(
            VisibleCopyOccurrence(
                path=occ["path"],
                line=occ["line"],
                presentation=occ["presentation"],
                german=text,
                placeholders=extract_placeholders(text),
                exemption=exemption,
            )
        )

    occurrences = _dedupe_occurrences(occurrences)
    units = _build_units(occurrences)
    metrics = {
        "unique_visible_units_total": len(units),
        "unique_translatable_units": sum(1 for unit in units if not unit["exemption"]),
        "exempt_visible_units": sum(1 for unit in units if unit["exemption"]),
        "visible_render_occurrences": len(occurrences),
        "german_word_count": sum(canonical_word_count(unit["german"]) for unit in units if not unit["exemption"]),
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
