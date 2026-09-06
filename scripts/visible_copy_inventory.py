#!/usr/bin/env python3
"""Deterministic inventory of Harmony-owned German copy visible in production UI."""
from __future__ import annotations

import argparse
from collections import Counter, defaultdict
from dataclasses import asdict, dataclass
import fnmatch
import hashlib
import json
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

SCRIPT_DIR = Path(__file__).resolve().parent
DEFAULT_POLICY = SCRIPT_DIR / "visible_copy_policy.json"
DEFAULT_ASSETS = SCRIPT_DIR / "visible_copy_asset_registry.json"
DEFAULT_ROUTES = SCRIPT_DIR / "visible_copy_routes.json"

PLACEHOLDER_RE = re.compile(
    r"\$\{[^{}]+\}|\{[A-Za-z_][A-Za-z0-9_.]*\}|%(?:\d+\$)?[sd]"
)
WORD_RE = re.compile(r"[^\W_]+(?:[’'\-‑][^\W_]+)*", re.UNICODE)
LETTER_RE = re.compile(r"[^\W\d_]", re.UNICODE)

VISIBLE_CONTEXT_RE = re.compile(
    r"""(?ix)
    \b(
        Text|BasicText|AnnotatedString|Snackbar|Toast|AlertDialog|
        Question|Moment|Pack|Game|Result
    )\s*\(
    |
    \b(
        text|label|title|subtitle|description|placeholder|contentDescription|
        message|question|prompt|answer|option|options|instruction|headline|
        supportingText|eyebrow|caption|name
    )\s*=
    """
)

PRODUCT_DATA_BASENAMES = {
    "GeneratedHarmonyContent.kt",
    "CuisinePackInstaller.kt",
}

SKIP_KOTLIN_BASENAMES = {
    "TranslationCatalog.kt",
    "LocalizationUpdates.kt",
    "Language.kt",
    "GeneratedLocaleSupport.kt",
}

FILELIKE_RE = re.compile(
    r"""(?ix)
    ^(?:[A-Za-z0-9_-]+/)+[A-Za-z0-9_.-]+$
    |
    \.(?:png|jpe?g|webp|svg|mp3|wav|ogg|json|kt|xml|zip|b64)$
    """
)


@dataclass(frozen=True)
class VisibleCopyOccurrence:
    path: str
    line: int
    presentation: str
    german: str
    placeholders: tuple[str, ...]
    exemption: str | None = None


@dataclass
class InventoryReport:
    units: list[dict]
    occurrences: list[dict]
    metrics: dict[str, int]
    breakdown: dict[str, int] | None = None


def load_json(path: Path) -> dict:
    if not path.exists():
        return {}
    return json.loads(path.read_text(encoding="utf-8"))


def normalize_visible_text(text: str) -> str:
    text = text.replace("\r\n", "\n").replace("\r", "\n").strip()
    lines = [re.sub(r"[ \t\f\v]+", " ", line).strip() for line in text.split("\n")]
    while lines and not lines[0]:
        lines.pop(0)
    while lines and not lines[-1]:
        lines.pop()
    return "\n".join(lines)


def extract_placeholders(text: str) -> tuple[str, ...]:
    return tuple(match.group(0) for match in PLACEHOLDER_RE.finditer(text))


def word_count(text: str) -> int:
    clean = PLACEHOLDER_RE.sub(" ", text)
    return len(WORD_RE.findall(clean))


def _line_number(source: str, offset: int) -> int:
    return source.count("\n", 0, offset) + 1


def _is_excluded_path(rel: str, policy: dict) -> bool:
    if Path(rel).name in SKIP_KOTLIN_BASENAMES:
        return True
    if rel.startswith("app/src/main/java/com/example/ui/") and (
        rel.endswith("Content.kt") or rel.endswith("Overrides.kt")
    ):
        return True
    return any(fnmatch.fnmatch(rel, pattern) for pattern in policy.get("exclude_path_globs", []))


def _decode_kotlin_string(raw: str, triple: bool) -> str:
    if triple:
        return raw
    replacements = {
        r"\\n": "\n",
        r"\\t": "\t",
        r'\\"': '"',
        r"\\'": "'",
        r"\\$": "$",
        r"\\\\": "\\",
    }
    for old, new in replacements.items():
        raw = raw.replace(old, new)
    return raw


def _iter_kotlin_strings(source: str):
    i = 0
    n = len(source)
    while i < n:
        if source.startswith('"""', i):
            start = i
            end = source.find('"""', i + 3)
            if end == -1:
                return
            raw = source[i + 3:end]
            yield _decode_kotlin_string(raw, True), start, end + 3
            i = end + 3
            continue
        if source[i] == '"':
            start = i
            i += 1
            buf: list[str] = []
            escaped = False
            while i < n:
                ch = source[i]
                if escaped:
                    buf.append("\\" + ch)
                    escaped = False
                    i += 1
                    continue
                if ch == "\\":
                    escaped = True
                    i += 1
                    continue
                if ch == '"':
                    end = i + 1
                    yield _decode_kotlin_string("".join(buf), False), start, end
                    i = end
                    break
                buf.append(ch)
                i += 1
            else:
                return
            continue
        i += 1


def _human_text(
    text: str,
    *,
    policy: dict,
    visible_context: bool,
    product_data: bool,
) -> tuple[bool, str | None]:
    text = normalize_visible_text(text)
    if not text or text in policy.get("excluded_exact_visible_text", []):
        return False, None
    if text in policy.get("internal_exact_literals", []):
        return False, None
    exemption = "brand" if text in policy.get("brand_literals", []) else None
    if exemption:
        return True, exemption
    if not LETTER_RE.search(text):
        return False, None
    if len(text) > 4000:
        return False, None
    if FILELIKE_RE.search(text) or text.startswith(("http://", "https://", "content://", "file://")):
        return False, None
    if any(token in text for token in ("package com.", "androidx.", "io.github.", "SELECT ", "INSERT ", "UPDATE ")):
        return False, None
    if not visible_context and not product_data:
        return False, None
    if product_data and not visible_context:
        for pattern in policy.get("technical_literal_patterns", []):
            if re.search(pattern, text):
                return False, None
    return True, None


def _add_occurrence(
    target: list[VisibleCopyOccurrence],
    *,
    path: str,
    line: int,
    presentation: str,
    german: str,
    policy: dict,
    visible_context: bool = True,
    product_data: bool = False,
    exemption: str | None = None,
) -> None:
    german = normalize_visible_text(german)
    ok, inferred = _human_text(
        german,
        policy=policy,
        visible_context=visible_context,
        product_data=product_data,
    )
    if not ok:
        return
    target.append(
        VisibleCopyOccurrence(
            path=path,
            line=line,
            presentation=presentation,
            german=german,
            placeholders=extract_placeholders(german),
            exemption=exemption or inferred,
        )
    )


def _extract_introspection_german(
    rel: str, source: str, policy: dict
) -> list[VisibleCopyOccurrence]:
    out: list[VisibleCopyOccurrence] = []
    marker = source.find("private val germanStrings")
    if marker < 0:
        return out
    start = source.find("mapOf(", marker)
    if start < 0:
        return out
    end_marker = source.find("\n    )", start)
    body_end = end_marker if end_marker >= 0 else len(source)
    body = source[start:body_end]
    pattern = re.compile(
        r'IntrospectionStringKey\.[A-Z0-9_]+\s+to\s+"((?:\\.|[^"\\])*)"'
    )
    for match in pattern.finditer(body):
        raw = _decode_kotlin_string(match.group(1), False)
        absolute = start + match.start(1)
        _add_occurrence(
            out,
            path=rel,
            line=_line_number(source, absolute),
            presentation="introspection-string",
            german=raw,
            policy=policy,
        )
    return out


def _scan_kotlin(path: Path, root: Path, policy: dict) -> list[VisibleCopyOccurrence]:
    rel = path.relative_to(root).as_posix()
    if _is_excluded_path(rel, policy):
        return []
    source = path.read_text(encoding="utf-8", errors="replace")
    if rel.endswith("/introspection/IntrospectionStrings.kt"):
        return _extract_introspection_german(rel, source, policy)

    out: list[VisibleCopyOccurrence] = []
    product_data = path.name in PRODUCT_DATA_BASENAMES or rel.endswith("/data/GeneratedHarmonyContent.kt")
    for value, start, end in _iter_kotlin_strings(source):
        before = source[max(0, start - 240):start]
        after = source[end:min(len(source), end + 120)]
        context = before + after
        visible_context = bool(VISIBLE_CONTEXT_RE.search(context))
        if product_data and not visible_context:
            visible_context = any(
                token in before[-180:]
                for token in ("Question", "EitherOr", "Pair", "Pack", "Category", "Moment", "listOf(", "title", "description")
            )
        if not visible_context and rel.endswith("MainActivity.kt"):
            visible_context = bool(re.search(r"(Text|label|title|contentDescription)\s*=?\s*$", before[-120:]))
        _add_occurrence(
            out,
            path=rel,
            line=_line_number(source, start),
            presentation="product-content" if product_data else "compose-ui",
            german=value,
            policy=policy,
            visible_context=visible_context,
            product_data=product_data,
        )
    return out


def _strip_xml_namespace(tag: str) -> str:
    return tag.rsplit("}", 1)[-1]


def _scan_android_xml(path: Path, root: Path, policy: dict) -> list[VisibleCopyOccurrence]:
    rel = path.relative_to(root).as_posix()
    if _is_excluded_path(rel, policy):
        return []
    out: list[VisibleCopyOccurrence] = []
    try:
        tree = ET.parse(path)
    except ET.ParseError:
        return out
    xml_root = tree.getroot()
    if path.name == "strings.xml" or "/values" in rel:
        for elem in xml_root.iter():
            if _strip_xml_namespace(elem.tag) in {"string", "item"}:
                text = "".join(elem.itertext())
                _add_occurrence(
                    out,
                    path=rel,
                    line=0,
                    presentation="android-string",
                    german=text,
                    policy=policy,
                )
    visible_attrs = {"text", "hint", "contentDescription", "title", "summary"}
    for elem in xml_root.iter():
        for attr, value in elem.attrib.items():
            name = _strip_xml_namespace(attr)
            if name not in visible_attrs or value.startswith("@"):
                continue
            _add_occurrence(
                out,
                path=rel,
                line=0,
                presentation="android-xml",
                german=value,
                policy=policy,
            )
    return out


def _scan_svg(path: Path, root: Path, policy: dict) -> list[VisibleCopyOccurrence]:
    rel = path.relative_to(root).as_posix()
    out: list[VisibleCopyOccurrence] = []
    try:
        tree = ET.parse(path)
    except ET.ParseError:
        return out
    for elem in tree.getroot().iter():
        if _strip_xml_namespace(elem.tag) not in {"text", "tspan"}:
            continue
        text = "".join(elem.itertext())
        _add_occurrence(
            out,
            path=rel,
            line=0,
            presentation="svg-text",
            german=text,
            policy=policy,
        )
    return out


def _asset_registry_occurrences(root: Path, policy: dict) -> list[VisibleCopyOccurrence]:
    registry_path = root / "scripts/visible_copy_asset_registry.json"
    registry = load_json(registry_path)
    out: list[VisibleCopyOccurrence] = []
    for entry in registry.get("text_bearing_assets", []):
        rel = entry["path"]
        for text in entry.get("german_text", []):
            _add_occurrence(
                out,
                path=rel,
                line=0,
                presentation="bitmap-text" if Path(rel).suffix.lower() != ".svg" else "svg-text",
                german=text,
                policy=policy,
            )
    for entry in registry.get("brand_only_assets", []):
        rel = entry["path"]
        for text in entry.get("visible_text", []):
            text = normalize_visible_text(text)
            if not text:
                continue
            out.append(
                VisibleCopyOccurrence(
                    path=rel,
                    line=0,
                    presentation="brand-asset",
                    german=text,
                    placeholders=extract_placeholders(text),
                    exemption="brand",
                )
            )
    return out


def _dedupe_occurrences(occurrences: list[VisibleCopyOccurrence]) -> list[VisibleCopyOccurrence]:
    seen = set()
    result = []
    for occ in sorted(
        occurrences,
        key=lambda o: (o.path, o.line, o.presentation, o.german, o.exemption or ""),
    ):
        key = (occ.path, occ.line, occ.presentation, occ.german, occ.placeholders, occ.exemption)
        if key not in seen:
            seen.add(key)
            result.append(occ)
    return result


def _build_units(occurrences: list[VisibleCopyOccurrence]) -> list[dict]:
    groups: dict[tuple[str, tuple[str, ...], str | None], list[VisibleCopyOccurrence]] = defaultdict(list)
    for occ in occurrences:
        groups[(occ.german, occ.placeholders, occ.exemption)].append(occ)
    units: list[dict] = []
    for (german, placeholders, exemption), group in groups.items():
        stable_id = "de_" + hashlib.sha256(
            (german + "\0" + "\0".join(placeholders) + "\0" + (exemption or "")).encode("utf-8")
        ).hexdigest()[:16]
        units.append(
            {
                "id": stable_id,
                "german": german,
                "placeholders": list(placeholders),
                "exemption": exemption,
                "occurrence_count": len(group),
                "source_paths": sorted({o.path for o in group}),
                "presentations": sorted({o.presentation for o in group}),
            }
        )
    return sorted(units, key=lambda unit: unit["id"])


def discover_repository(root: Path) -> InventoryReport:
    root = root.resolve()
    policy_path = root / "scripts/visible_copy_policy.json"
    policy = load_json(policy_path)
    occurrences: list[VisibleCopyOccurrence] = []

    source_root = root / "app/src/main/java"
    if source_root.exists():
        for path in sorted(source_root.rglob("*.kt")):
            occurrences.extend(_scan_kotlin(path, root, policy))

    res_root = root / "app/src/main/res"
    if res_root.exists():
        for path in sorted(res_root.rglob("*.xml")):
            occurrences.extend(_scan_android_xml(path, root, policy))

    assets_root = root / "app/src/main/assets"
    if assets_root.exists():
        for path in sorted(assets_root.rglob("*.svg")):
            occurrences.extend(_scan_svg(path, root, policy))

    occurrences.extend(_asset_registry_occurrences(root, policy))
    occurrences = _dedupe_occurrences(occurrences)
    units = _build_units(occurrences)
    metrics = {
        "unique_visible_units_total": len(units),
        "unique_translatable_units": sum(1 for u in units if not u["exemption"]),
        "exempt_visible_units": sum(1 for u in units if u["exemption"]),
        "visible_render_occurrences": len(occurrences),
        "german_word_count": sum(word_count(u["german"]) for u in units if not u["exemption"]),
    }
    breakdown = dict(sorted(Counter(o.presentation for o in occurrences).items()))
    return InventoryReport(
        units=units,
        occurrences=[asdict(o) | {"placeholders": list(o.placeholders)} for o in occurrences],
        metrics=metrics,
        breakdown=breakdown,
    )


def report_payload(report: InventoryReport) -> dict:
    return {
        "schema_version": 1,
        "source_language": "de",
        "metrics": report.metrics,
        "breakdown": report.breakdown or {},
        "units": report.units,
        "occurrences": report.occurrences,
    }


def write_report(report: InventoryReport, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    payload = json.dumps(
        report_payload(report),
        ensure_ascii=False,
        indent=2,
        sort_keys=True,
    ) + "\n"
    path.write_text(payload, encoding="utf-8")


def write_summary(report: InventoryReport, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    metrics = report.metrics
    lines = [
        "# Deutsche sichtbare Harmony-Texte",
        "",
        "Diese Zahlen werden aus dem aktuellen Produktionscode erzeugt; Developer Studio ist ausgeschlossen.",
        "",
        f"- Eindeutige sichtbare Einheiten gesamt: **{metrics['unique_visible_units_total']}**",
        f"- Davon zu übersetzen: **{metrics['unique_translatable_units']}**",
        f"- Sichtbare Ausnahmen (z. B. Marken): **{metrics['exempt_visible_units']}**",
        f"- Sichtbare Render-Vorkommen: **{metrics['visible_render_occurrences']}**",
        f"- Deutsche Wortanzahl der zu übersetzenden Einheiten: **{metrics['german_word_count']}**",
        "",
        "## Vorkommen nach Quelle",
        "",
    ]
    for key, value in sorted((report.breakdown or {}).items()):
        lines.append(f"- `{key}`: {value}")
    lines.append("")
    path.write_text("\n".join(lines), encoding="utf-8")


def _expected_bytes(report: InventoryReport) -> tuple[bytes, bytes]:
    import tempfile
    with tempfile.TemporaryDirectory() as tmp:
        inv = Path(tmp) / "inventory.json"
        summary = Path(tmp) / "summary.md"
        write_report(report, inv)
        write_summary(report, summary)
        return inv.read_bytes(), summary.read_bytes()


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=SCRIPT_DIR.parent)
    parser.add_argument("--write", action="store_true")
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args(argv)

    root = args.root.resolve()
    report = discover_repository(root)
    inventory_path = root / "localization/visible-copy-inventory.de.json"
    summary_path = root / "localization/visible-copy-inventory-summary.md"

    if args.write:
        write_report(report, inventory_path)
        write_summary(report, summary_path)

    if args.check:
        expected_inventory, expected_summary = _expected_bytes(report)
        errors = []
        if not inventory_path.exists() or inventory_path.read_bytes() != expected_inventory:
            errors.append(str(inventory_path.relative_to(root)))
        if not summary_path.exists() or summary_path.read_bytes() != expected_summary:
            errors.append(str(summary_path.relative_to(root)))
        if errors:
            print("Visible-copy inventory is stale or missing: " + ", ".join(errors), file=sys.stderr)
            return 1

    print(json.dumps(report.metrics, ensure_ascii=False, sort_keys=True))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
