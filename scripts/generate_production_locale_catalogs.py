#!/usr/bin/env python3
"""Generate complete offline Kotlin catalogs for selected Harmony production locales."""
from __future__ import annotations

import argparse
import json
import re
import shutil
import time
import urllib.parse
import urllib.request
from pathlib import Path

import audit_localization as audit
from production_locale_registry import BY_CODE, CORE_OVERRIDES

ROOT = Path(__file__).resolve().parents[1]
UI = ROOT / "app/src/main/java/com/example/ui"

PLACEHOLDER_RE = re.compile(
    r'(\$\{[^}]+\}|\$[A-Za-z_][A-Za-z0-9_]*|\{[^}]+\}|%\d*\$?[a-zA-Z])'
)


def protect(text: str) -> tuple[str, dict[str, str]]:
    placeholders: dict[str, str] = {}

    def repl(match: re.Match[str]) -> str:
        token = f"⟦{len(placeholders):02d}⟧"
        placeholders[token] = match.group(0)
        return token

    return PLACEHOLDER_RE.sub(repl, text), placeholders


def restore(text: str, placeholders: dict[str, str]) -> str:
    for token, value in placeholders.items():
        text = re.sub(re.escape(token), lambda _: value, text, flags=re.IGNORECASE)
    return text


def google_request(text: str, target: str) -> str:
    params = urllib.parse.urlencode({"client": "gtx", "sl": "de", "tl": target, "dt": "t", "q": text})
    url = "https://translate.googleapis.com/translate_a/single?" + params
    request = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0 HarmonyLocalization/3.0"})
    last_error: Exception | None = None
    for attempt in range(7):
        try:
            with urllib.request.urlopen(request, timeout=40) as response:
                data = json.loads(response.read().decode("utf-8"))
            return "".join(segment[0] for segment in data[0] if segment and segment[0]).strip()
        except Exception as exc:  # noqa: BLE001
            last_error = exc
            time.sleep(min(12.0, 1.5 * (attempt + 1)))
    raise RuntimeError(f"Translation failed for {target}: {last_error}")


def request_single(source: str, target: str) -> str:
    protected, placeholders = protect(source)
    return restore(google_request(protected, target), placeholders)


def request_batch(items: list[str], target: str) -> dict[str, str]:
    rows: list[tuple[str, str, dict[str, str], str]] = []
    for index, source in enumerate(items):
        protected, placeholders = protect(source)
        marker = f"[[HARMONY{index:03d}]]"
        rows.append((source, marker, placeholders, protected))
    payload = "\n".join(f"{marker} {protected}" for _, marker, _, protected in rows)
    translated = google_request(payload, target)
    parsed: dict[int, str] = {}
    marker_re = re.compile(r'^\[\[HARMONY(\d{3})\]\]\s*(.*)$', re.IGNORECASE)
    for line in translated.splitlines():
        match = marker_re.match(line.strip())
        if match:
            parsed[int(match.group(1))] = match.group(2).strip()
    if len(parsed) != len(items):
        return {source: request_single(source, target) for source in items}
    result: dict[str, str] = {}
    for index, (source, _, placeholders, _) in enumerate(rows):
        result[source] = restore(parsed[index], placeholders)
    return result


def kotlin_escape(value: str) -> str:
    # Canonical audit keys may already contain the two-character Kotlin \n escape.
    # Normalize through a real newline so it is emitted once, not double-escaped.
    value = value.replace(r"\n", "\n")
    return (
        value.replace("\\", "\\\\")
        .replace('"', '\\"')
        .replace("\n", "\\n")
        .replace("$", "\\$")
    )


def canonical_customer_catalog() -> dict[str, str]:
    canonical_all = audit.extract_map(UI / "EnglishContent.kt", "EXACT_ENGLISH_CONTENT")
    if not canonical_all:
        raise RuntimeError("Could not parse canonical English catalog")
    internal = getattr(audit, "INTERNAL_ONLY_KEYS", set())
    return {
        key: value
        for key, value in canonical_all.items()
        if key not in audit.DEV_ONLY_KEYS and key not in internal and "Entwickler" not in key
    }


def write_catalog(path: Path, exact_name: str, dynamic_name: str, values: dict[str, str]) -> None:
    lines = [
        "package com.example.ui",
        "",
        "/** Generated production locale catalog aligned to the current Harmony customer catalog. */",
        f"internal val {exact_name}: Map<String, String> = mapOf(",
    ]
    for source in sorted(values):
        lines.append(f'    "{kotlin_escape(source)}" to "{kotlin_escape(values[source])}",')
    lines += [
        ")",
        "",
        f"internal fun {dynamic_name}(text: String): String? =",
        f"    localizeGeneratedLocaleDynamicContent(text, {exact_name})",
        "",
    ]
    path.write_text("\n".join(lines), encoding="utf-8")


def generate(code: str, artifact_dir: Path | None = None) -> None:
    item = BY_CODE[code]
    path = UI / item["filename"]
    canonical = canonical_customer_catalog()
    existing = audit.extract_map(path, item["exact"]) if path.exists() else {}
    values = {k: v for k, v in existing.items() if k in canonical}
    todo = sorted(set(canonical) - set(values))
    if todo:
        print(f"Translating {len(todo)} strings for {code}", flush=True)
        for offset in range(0, len(todo), 10):
            chunk = todo[offset:offset + 10]
            values.update(request_batch(chunk, item["target"]))
            if offset and offset % 100 == 0:
                print(f"  {code}: {min(offset + 10, len(todo))}/{len(todo)}", flush=True)
    for key, value in CORE_OVERRIDES.get(code, {}).items():
        if key in canonical:
            values[key] = value
    missing = sorted(set(canonical) - set(values))
    if missing:
        raise RuntimeError(f"{code}: generator still missing {len(missing)} canonical keys")
    write_catalog(path, item["exact"], item["dynamic"], values)
    print(f"Wrote {item['filename']}: {len(values)} entries", flush=True)
    if artifact_dir is not None:
        artifact_dir.mkdir(parents=True, exist_ok=True)
        shutil.copy2(path, artifact_dir / item["filename"])


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--codes", required=True, help="Comma-separated locale codes")
    parser.add_argument("--artifact-dir", default=None)
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    codes = [code.strip() for code in args.codes.split(",") if code.strip()]
    unknown = [code for code in codes if code not in BY_CODE]
    if unknown:
        raise SystemExit(f"Unknown locale codes: {', '.join(unknown)}")
    artifact_dir = Path(args.artifact_dir) if args.artifact_dir else None
    for code in codes:
        generate(code, artifact_dir)


if __name__ == "__main__":
    main()
