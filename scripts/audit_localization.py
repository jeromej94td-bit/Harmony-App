#!/usr/bin/env python3
"""Static localization gate for Harmony customer-facing copy.

Developer Studio is intentionally excluded: it is temporary and the product owner explicitly
excluded it from this localization pass. The gate covers the production language catalogs and
known customer-facing regressions from the supplied Japanese screen recording.
"""
from __future__ import annotations

import base64
import gzip
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
UI = ROOT / "app/src/main/java/com/example/ui"
REPORT = ROOT / "localization_missing.json"

LOCALES = {
    "en": ("EnglishContent.kt", "EXACT_ENGLISH_CONTENT"),
    "it": ("ItalianContent.kt", "EXACT_ITALIAN_CONTENT"),
    "fr": ("FrenchContent.kt", "EXACT_FRENCH_CONTENT"),
    "ja": ("JapaneseContent.kt", "EXACT_JAPANESE_CONTENT"),
    "pl": ("PolishContent.kt", "EXACT_POLISH_CONTENT"),
    "es-419": ("SpanishContent.kt", "EXACT_SPANISH_LATIN_AMERICA_CONTENT"),
    "es-ES": ("SpanishContent.kt", "EXACT_SPANISH_SPAIN_CONTENT"),
    "pt-BR": ("PortugueseBrazilContent.kt", "EXACT_PORTUGUESE_BRAZIL_CONTENT"),
    "pt-PT": ("PortuguesePortugalContent.kt", "EXACT_PORTUGUESE_PORTUGAL_CONTENT"),
    "da": ("DanishContent.kt", "EXACT_DANISH_CONTENT"),
    "no": ("NorwegianContent.kt", "EXACT_NORWEGIAN_CONTENT"),
}

COMPRESSED_DATA_NAMES = {
    "EXACT_DANISH_CONTENT": "DANISH_CONTENT_DATA",
    "EXACT_NORWEGIAN_CONTENT": "NORWEGIAN_CONTENT_DATA",
}

# Canonical strings that only belong to the temporary developer tooling.
DEV_ONLY_KEYS = {
    "Entwickler Studio Öffnen",
    "Entwickler-Modus",
    "🛠️ Entwickler-Modus",
    "Spiele & Städte bearbeiten, Ordner reinladen, Bilder anpassen",
}

ENTRY_RE = re.compile(r'^\s*"((?:\\.|[^"\\])*)"\s+to\s+"((?:\\.|[^"\\])*)",?\s*$')


def unescape_kotlin(value: str) -> str:
    return (value
            .replace(r'\\"', '"')
            .replace(r'\\n', '\n')
            .replace(r'\\t', '\t')
            .replace(r'\\\\', '\\'))


def parse_map_of(text: str, map_name: str) -> dict[str, str]:
    marker = re.search(rf'\b{re.escape(map_name)}\b[^=]*=\s*mapOf\s*\(', text)
    if not marker:
        return {}
    pos = marker.end()
    depth = 1
    in_string = False
    escaped = False
    end = pos
    while end < len(text) and depth:
        ch = text[end]
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
    body = text[pos:end - 1]
    result: dict[str, str] = {}
    for line in body.splitlines():
        match = ENTRY_RE.match(line)
        if match:
            result[unescape_kotlin(match.group(1))] = unescape_kotlin(match.group(2))
    return result


def parse_compressed_catalog(text: str, map_name: str) -> dict[str, str]:
    data_name = COMPRESSED_DATA_NAMES.get(map_name)
    if not data_name:
        return {}
    match = re.search(
        rf'private\s+const\s+val\s+{re.escape(data_name)}\s*=\s*"""(.*?)"""',
        text,
        re.DOTALL,
    )
    if not match:
        return {}
    clean = "".join(match.group(1).split())
    payload = gzip.decompress(base64.b64decode(clean)).decode("utf-8")
    result: dict[str, str] = {}
    for line in payload.splitlines():
        if not line.strip():
            continue
        key, value = line.split("\t", 1)
        result[unescape_kotlin(key)] = unescape_kotlin(value)
    return result


def extract_map(path: Path, map_name: str) -> dict[str, str]:
    text = path.read_text(encoding="utf-8")
    return parse_map_of(text, map_name) or parse_compressed_catalog(text, map_name)


def fail(message: str) -> None:
    print(f"::error::{message}")


def main() -> int:
    canonical_all = extract_map(UI / "EnglishContent.kt", "EXACT_ENGLISH_CONTENT")
    if not canonical_all:
        fail("Could not parse canonical English catalog")
        return 2
    canonical = {k: v for k, v in canonical_all.items() if k not in DEV_ONLY_KEYS and "Entwickler" not in k}

    print(f"Canonical customer catalog: {len(canonical)} keys ({len(canonical_all) - len(canonical)} developer-only excluded)")
    failed = False
    catalogs: dict[str, dict[str, str]] = {}
    report: dict[str, list[str]] = {}

    for code, (filename, map_name) in LOCALES.items():
        catalog = extract_map(UI / filename, map_name)
        catalogs[code] = catalog
        missing = sorted(set(canonical) - set(catalog))
        report[code] = missing
        if missing:
            failed = True
            fail(f"{code}: missing {len(missing)} / {len(canonical)} customer keys")
            print(f"MISSING[{code}]=" + " | ".join(missing))
        else:
            print(f"{code}: {len(catalog)} keys, coverage OK")

    REPORT.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")

    # Video-derived regression checks: these were visibly German/wrong in Japanese.
    ja = catalogs.get("ja", {})
    expected_ja = {
        "Schließen": "閉じる",
        "Dein Bild": "あなたの写真",
        "Partnerbild": "パートナーの写真",
        "Privater Paar-Chat": "二人だけのプライベートチャット",
        "Unbeantwortet": "未回答",
        "Entweder oder": "どちらか",
        "Frage": "質問",
        "Unterhaltung": "エンターテインメント",
        "Hochzeit": "結婚式",
        "Burger": "バーガー",
        "Aussehen": "見た目",
        "Das erste Treffen": "初めて会った日",
    }
    for key, expected in expected_ja.items():
        actual = ja.get(key)
        if actual != expected:
            failed = True
            fail(f"ja video regression: {key!r} -> {actual!r}, expected {expected!r}")

    # The subconscious journey had an English fallback in Japanese in the supplied video.
    introspection = (UI / "introspection/IntrospectionStrings.kt").read_text(encoding="utf-8")
    required_locale_tokens = [
        "AppLanguage.JAPANESE", "AppLanguage.POLISH", "AppLanguage.FRENCH",
        "AppLanguage.SPANISH_LATIN_AMERICA", "AppLanguage.SPANISH_SPAIN",
        "AppLanguage.PORTUGUESE_BRAZIL", "AppLanguage.PORTUGUESE_PORTUGAL",
        "AppLanguage.DANISH", "AppLanguage.NORWEGIAN",
    ]
    for token in required_locale_tokens:
        if token not in introspection:
            failed = True
            fail(f"IntrospectionStrings has no explicit {token} localization path")

    # Customer screens must not hardcode the German strings that leaked in the video.
    source_checks = {
        "screens/ChatScreen.kt": ['Text("Privater Paar-Chat"'],
        "screens/PandaEitherOrScreen.kt": [
            'Text("🐼 Entweder oder"',
            'Text("$name entscheidet"',
            'Text("Der andere schaut kurz weg 🤫"',
            'Text("ODER"',
        ],
        "screens/GamesScreen.kt": [
            'Text("Unbeantwortete Fragen"',
            'Text("${unanswered.size} Fragen warten auf euch"',
            'Text("Ihr habt bereits alle Fragen beantwortet."',
            'TextButton(onClick = onDismiss) { Text("Schließen"',
        ],
    }
    for rel, needles in source_checks.items():
        text = (UI / rel).read_text(encoding="utf-8")
        for needle in needles:
            if needle in text:
                failed = True
                fail(f"Hardcoded customer-facing German remains in {rel}: {needle}")

    if failed:
        print("Localization audit FAILED")
        return 1
    print("Localization audit PASSED")
    return 0


if __name__ == "__main__":
    sys.exit(main())
