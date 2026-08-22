#!/usr/bin/env python3
"""Audit every production-visible Harmony render occurrence for Japanese localization.

Unlike catalog coverage, this gate works at occurrence level: if the same German copy appears
in five source/render locations, all five locations are represented and must have both a
Japanese translation and a runtime localization route. Developer Studio is already excluded
by the canonical inventory.
"""
from __future__ import annotations

import argparse
from collections import Counter
import json
from pathlib import Path

from audit_japanese_visible_copy import load_japanese_catalog

ROOT = Path(__file__).resolve().parents[1]
INVENTORY_REL = Path("localization/visible-copy-inventory.de.json")
REPORT_REL = Path("localization/japanese-render-occurrences.json")

LOCALIZATION_TOKENS = (
    "tr(",
    "contentText(",
    "localizedContent(",
    "TranslationCatalog.",
    "LanguageManager.tr(",
    "IntrospectionStrings.",
)


def _line_window(source: str, line: int, radius: int = 5) -> str:
    lines = source.splitlines()
    if not lines:
        return ""
    index = max(0, line - 1)
    start = max(0, index - radius)
    end = min(len(lines), index + radius + 1)
    return "\n".join(lines[start:end])


def compose_occurrence_is_localized(source: str, line: int, german: str) -> bool:
    """Conservatively recognize an explicit runtime localization route near an occurrence."""
    window = _line_window(source, line)
    if german not in window:
        # The canonical scanner can normalize escapes/newlines; route recognition must remain
        # conservative rather than claiming a location is localized without source evidence.
        return False
    return any(token in window for token in LOCALIZATION_TOKENS)


def _source_text(root: Path, rel: str, cache: dict[str, str]) -> str:
    if rel not in cache:
        path = root / rel
        cache[rel] = path.read_text(encoding="utf-8", errors="replace") if path.exists() else ""
    return cache[rel]


def _occurrence_route_ok(root: Path, occ: dict, cache: dict[str, str]) -> tuple[bool, str]:
    if occ.get("exemption") == "brand":
        return True, "brand-exemption"

    presentation = occ.get("presentation")
    rel = occ.get("path", "")

    if presentation in {"product-content", "bundled-image-option"}:
        # These are product-data occurrences. They are rendered through Harmony's content
        # localization layer; catalog coverage is still checked separately per occurrence.
        return True, "content-catalog"

    if presentation == "introspection-string":
        source = _source_text(root, rel, cache)
        routed = "AppLanguage.JAPANESE" in source and "IntrospectionStrings" in source
        return routed, "introspection-locale-map" if routed else "unrouted-introspection"

    if presentation == "compose-ui":
        source = _source_text(root, rel, cache)
        routed = compose_occurrence_is_localized(source, int(occ.get("line", 0)), occ.get("german", ""))
        return routed, "compose-localization" if routed else "raw-compose"

    if presentation == "widget-notification":
        source = _source_text(root, rel, cache)
        window = _line_window(source, int(occ.get("line", 0)), radius=10)
        routed = any(token in window for token in ("localizedContent(", "TranslationCatalog.", "LanguageStore.get("))
        return routed, "widget-localization" if routed else "raw-widget"

    if presentation in {"android-string", "android-xml"}:
        # Internal Harmony language selection is independent from the Android system locale;
        # plain resources do not prove Japanese routing on their own.
        return False, "raw-android-resource"

    if presentation in {"bitmap-text", "svg-text"}:
        return False, "localized-asset-required"

    return False, f"unclassified:{presentation}"


def build_render_report(root: Path) -> dict:
    root = root.resolve()
    inventory_path = root / INVENTORY_REL
    payload = json.loads(inventory_path.read_text(encoding="utf-8"))
    japanese = load_japanese_catalog()
    cache: dict[str, str] = {}
    rows: list[dict] = []

    for index, occ in enumerate(payload.get("occurrences", []), start=1):
        german = occ.get("german", "")
        exemption = occ.get("exemption")
        japanese_text = german if exemption == "brand" else japanese.get(german)
        translated = exemption == "brand" or bool(japanese_text)
        routed, route = _occurrence_route_ok(root, occ, cache)
        rows.append(
            {
                "occurrence": index,
                "path": occ.get("path"),
                "line": occ.get("line"),
                "presentation": occ.get("presentation"),
                "german": german,
                "japanese": japanese_text,
                "exemption": exemption,
                "translated": translated,
                "routed": routed,
                "route": route,
                "ok": translated and routed,
            }
        )

    unresolved = [row for row in rows if not row["ok"]]
    missing_translation = [row for row in rows if not row["translated"]]
    unrouted = [row for row in rows if not row["routed"]]
    by_route = Counter(row["route"] for row in rows)
    return {
        "total_render_occurrences": len(rows),
        "localized_render_occurrences": len(rows) - len(unresolved),
        "unresolved_render_occurrences": len(unresolved),
        "missing_translation_occurrences": len(missing_translation),
        "unrouted_render_occurrences": len(unrouted),
        "routes": dict(sorted(by_route.items())),
        "occurrences": rows,
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=ROOT)
    parser.add_argument("--write", action="store_true")
    args = parser.parse_args()

    report = build_render_report(args.root)
    if args.write:
        target = args.root.resolve() / REPORT_REL
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    print(
        "Japanese render-occurrence coverage: "
        f"{report['localized_render_occurrences']}/{report['total_render_occurrences']} | "
        f"missing_translation_occurrences={report['missing_translation_occurrences']} | "
        f"unrouted_render_occurrences={report['unrouted_render_occurrences']}"
    )
    unresolved = [row for row in report["occurrences"] if not row["ok"]]
    for row in unresolved[:250]:
        reasons = []
        if not row["translated"]:
            reasons.append("missing-ja")
        if not row["routed"]:
            reasons.append(row["route"])
        print(
            "UNRESOLVED="
            f"#{row['occurrence']} {row['path']}:{row['line']} [{','.join(reasons)}] {row['german']}"
        )
    if len(unresolved) > 250:
        print(f"... {len(unresolved) - 250} more unresolved occurrences in {REPORT_REL}")
    return 1 if unresolved else 0


if __name__ == "__main__":
    raise SystemExit(main())
