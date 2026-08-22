#!/usr/bin/env python3
"""Generate Kotlin translation overrides for missing/current customer strings.

This is a build-time helper only. It calls the public Google Translate web endpoint from CI,
then emits a deterministic Kotlin map that is reviewed and committed. The app itself never
uses a network translation service at runtime.
"""
from __future__ import annotations

import json
import re
import time
import urllib.parse
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MISSING = ROOT / "localization_missing.json"
INTROSPECTION = ROOT / "app/src/main/java/com/example/ui/introspection/IntrospectionStrings.kt"
OUTPUT = ROOT / "generated-localization-updates.kt"

TARGETS = {
    "it": ("ITALIAN", "it"),
    "fr": ("FRENCH", "fr"),
    "ja": ("JAPANESE", "ja"),
    "pl": ("POLISH", "pl"),
    "es-419": ("SPANISH_LATIN_AMERICA", "es"),
    "es-ES": ("SPANISH_SPAIN", "es"),
    "pt-BR": ("PORTUGUESE_BRAZIL", "pt"),
    "pt-PT": ("PORTUGUESE_PORTUGAL", "pt"),
    "da": ("DANISH", "da"),
    "no": ("NORWEGIAN", "no"),
}

INTERNAL_ONLY = {
    ", listOf(", "aufwaermen", "custom_gourmet_eissorten", "dasoderdas", "disney",
    "entertainment", "essen", "familie", "games", "harrypotter", "hochzeit", "iPhone",
    "ichhabenochnie", "kinder", "oder", "parks", "party", "reden", "reisen", "tot",
    "universal", "unterhaltung", "wer", "werwuerde", "zuhause", "{partner}", "{user}",
    "☀️", "❤️",
}

# Strings found visibly untranslated in the supplied Japanese recording plus adjacent controls.
EXTRA_UI_KEYS = {
    "Dein Bild", "Partnerbild", "Privater Paar-Chat", "Nutzer melden", "Bild hinzufügen",
    "Senden", "Geteiltes Bild", "Geteiltes Bild im Vollbildmodus", "Meldung vorbereiten",
    "Unbeantwortete Fragen", "Ihr habt bereits alle Fragen beantwortet.", "Entweder oder",
    "Der andere schaut kurz weg 🤫", "ODER", "entscheidet", "Frage", "Unterhaltung",
    "Hochzeit", "Tiere", "Für Paare", "Reden vor...", "Das oder das", "Party",
    "Überraschungspaket", "Kekse und ein Brief — ich musste weinen vor Freude.",
    "Unser erstes Videodate", "Vier Stunden geredet und die Zeit vergessen.",
    "Deine Antwort", "Partnerantwort", "Unbeantwortet",
}

# Human-reviewed Japanese corrections for bad legacy machine translations seen in the video/audit.
JAPANESE_OVERRIDES = {
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
    "ODER": "または",
    "entscheidet": "が選びます",
    "Der andere schaut kurz weg 🤫": "もう一人は少し目をそらしてね 🤫",
    "Überraschungspaket": "サプライズ小包",
    "Kekse und ein Brief — ich musste weinen vor Freude.": "クッキーと手紙――うれしくて泣いてしまった。",
    "Unser erstes Videodate": "初めてのビデオデート",
    "Vier Stunden geredet und die Zeit vergessen.": "4時間も話して、時間を忘れてしまった。",
}

PLACEHOLDER_RE = re.compile(r'(\\?\$\{[^}]+\}|\{[^}]+\}|%\d*\$?[a-zA-Z])')


def introspection_german_values() -> set[str]:
    text = INTROSPECTION.read_text(encoding="utf-8")
    marker = text.index("private val germanStrings")
    end = text.index("private val englishStrings", marker)
    block = text[marker:end]
    return set(re.findall(r'IntrospectionStringKey\.[A-Z0-9_]+\s+to\s+"((?:\\.|[^"\\])*)"', block))


def protect(text: str) -> tuple[str, dict[str, str]]:
    placeholders: dict[str, str] = {}
    def repl(match: re.Match[str]) -> str:
        token = f"HARMONYPLACEHOLDER{len(placeholders):02d}"
        placeholders[token] = match.group(0)
        return token
    return PLACEHOLDER_RE.sub(repl, text), placeholders


def restore(text: str, placeholders: dict[str, str]) -> str:
    for token, value in placeholders.items():
        text = text.replace(token, value)
    return text


def request_translation(text: str, target: str) -> str:
    protected, placeholders = protect(text)
    params = urllib.parse.urlencode({"client": "gtx", "sl": "de", "tl": target, "dt": "t", "q": protected})
    url = "https://translate.googleapis.com/translate_a/single?" + params
    request = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0 HarmonyLocalization/1.0"})
    last_error: Exception | None = None
    for attempt in range(5):
        try:
            with urllib.request.urlopen(request, timeout=30) as response:
                data = json.loads(response.read().decode("utf-8"))
            translated = "".join(segment[0] for segment in data[0] if segment and segment[0])
            return restore(translated, placeholders).strip()
        except Exception as exc:  # noqa: BLE001 - retry network service failures
            last_error = exc
            time.sleep(1.5 * (attempt + 1))
    raise RuntimeError(f"Translation failed for {target}: {text!r}: {last_error}")


def kotlin_escape(value: str) -> str:
    return (value.replace("\\", "\\\\")
                 .replace('"', '\\"')
                 .replace("\n", "\\n")
                 .replace("$", "${'$'}"))


def main() -> None:
    missing: dict[str, list[str]] = json.loads(MISSING.read_text(encoding="utf-8"))
    extras = EXTRA_UI_KEYS | introspection_german_values()
    lines = [
        "package com.example.ui",
        "",
        "/** Generated once during the localization repair; runtime lookup remains fully local/offline. */",
        "internal val LOCALIZATION_UPDATES: Map<AppLanguage, Map<String, String>> = mapOf(",
    ]

    for code, (enum_name, target) in TARGETS.items():
        keys = sorted((set(missing.get(code, [])) | extras) - INTERNAL_ONLY)
        translations: dict[str, str] = {}
        print(f"Translating {len(keys)} strings for {code} -> {target}", flush=True)
        for index, key in enumerate(keys, 1):
            translations[key] = request_translation(key, target)
            if index % 25 == 0:
                print(f"  {code}: {index}/{len(keys)}", flush=True)
        if code == "ja":
            translations.update(JAPANESE_OVERRIDES)

        lines.append(f"    AppLanguage.{enum_name} to mapOf(")
        for key in sorted(translations):
            lines.append(f'        "{kotlin_escape(key)}" to "{kotlin_escape(translations[key])}",')
        lines.append("    ),")
    lines.append(")")
    lines.append("")
    OUTPUT.write_text("\n".join(lines), encoding="utf-8")
    print(f"Wrote {OUTPUT}", flush=True)


if __name__ == "__main__":
    main()
