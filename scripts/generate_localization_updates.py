#!/usr/bin/env python3
"""Generate reviewed Kotlin overrides for missing/current customer strings.

Build-time helper only. CI uses the public Google Translate endpoint to fill missing native
copy, then the generated Kotlin is committed so the Android app remains fully local/offline.
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

# Concrete regressions seen in the supplied Japanese recording plus their surrounding controls.
EXTRA_UI_KEYS = {
    "Dein Bild", "Partnerbild", "Privater Paar-Chat", "Nutzer melden", "Bild hinzufügen",
    "Senden", "Geteiltes Bild", "Geteiltes Bild im Vollbildmodus", "Meldung vorbereiten",
    "Möchtest du {partner} melden? Die Meldung wird erst nach deiner Bestätigung vorbereitet.",
    "Unbeantwortete Fragen", "{count} Fragen warten auf euch",
    "Ihr habt bereits alle Fragen beantwortet.", "Entweder oder", "Spiel schließen",
    "Alle Fragen beantwortet", "{count} offen · keine Wiederholungen",
    "Frage {current} von {total}", "Geheime Auswahl", "{name} entscheidet",
    "Der andere schaut kurz weg 🤫", "ODER", "Handy weitergeben",
    "Die erste Antwort bleibt geheim.", "{partner} ist bereit", "Volltreffer! High Five 💥",
    "Heute verschieden – auch das gehört zu euch", "Nächste zufällige Frage",
    "Ihr kennt jede Entscheidung", "{count} von {total} Fragen beantwortet",
    "Zurück zu den Spielen", "Frage", "Unterhaltung", "Hochzeit", "Tiere", "Für Paare",
    "Reden vor...", "Das oder das", "Party", "Wer würde eher?", "Ich habe noch nie",
    "Essen & Genuss", "Zuhause & Alltag", "Spiele",
    "Überraschungspaket", "Kekse und ein Brief — ich musste weinen vor Freude.",
    "Unser erstes Videodate", "Vier Stunden geredet und die Zeit vergessen.",
    "Deine Antwort", "Partnerantwort", "Unbeantwortet",
}

# Human-reviewed Japanese corrections for the bad legacy machine translations and all video paths.
JAPANESE_OVERRIDES = {
    "Schließen": "閉じる",
    "Dein Bild": "あなたの写真",
    "Partnerbild": "パートナーの写真",
    "Privater Paar-Chat": "二人だけのプライベートチャット",
    "Nutzer melden": "ユーザーを報告",
    "Bild hinzufügen": "写真を追加",
    "Senden": "送信",
    "Meldung vorbereiten": "報告を準備",
    "Möchtest du {partner} melden? Die Meldung wird erst nach deiner Bestätigung vorbereitet.": "{partner}を報告しますか？報告内容はあなたが確認した後に準備されます。",
    "Unbeantwortete Fragen": "未回答の質問",
    "{count} Fragen warten auf euch": "未回答の質問が{count}件あります",
    "Ihr habt bereits alle Fragen beantwortet.": "すべての質問に回答済みです。",
    "Unbeantwortet": "未回答",
    "Entweder oder": "どちらか",
    "Spiel schließen": "ゲームを閉じる",
    "Alle Fragen beantwortet": "すべての質問に回答済み",
    "{count} offen · keine Wiederholungen": "残り{count}問・重複なし",
    "Frage {current} von {total}": "質問 {current}/{total}",
    "Geheime Auswahl": "秘密の選択",
    "{name} entscheidet": "{name}が選びます",
    "Frage": "質問",
    "Unterhaltung": "エンターテインメント",
    "Hochzeit": "結婚式",
    "Burger": "バーガー",
    "Aussehen": "見た目",
    "Das erste Treffen": "初めて会った日",
    "ODER": "または",
    "Der andere schaut kurz weg 🤫": "もう一人は少し目をそらしてね 🤫",
    "Handy weitergeben": "スマホを渡して",
    "Die erste Antwort bleibt geheim.": "最初の回答は秘密のままです。",
    "{partner} ist bereit": "{partner}の準備ができた",
    "Volltreffer! High Five 💥": "完全一致！ハイタッチ 💥",
    "Heute verschieden – auch das gehört zu euch": "今日は違う選択――それも二人らしさ",
    "Nächste zufällige Frage": "次のランダム質問",
    "Ihr kennt jede Entscheidung": "すべての選択を知り尽くしたね",
    "{count} von {total} Fragen beantwortet": "{total}問中{count}問に回答",
    "Zurück zu den Spielen": "ゲームに戻る",
    "Überraschungspaket": "サプライズ小包",
    "Kekse und ein Brief — ich musste weinen vor Freude.": "クッキーと手紙――うれしくて泣いてしまった。",
    "Unser erstes Videodate": "初めてのビデオデート",
    "Vier Stunden geredet und die Zeit vergessen.": "4時間も話して、時間を忘れてしまった。",
    "✨️ Das Verborgene in dir": "✨️ あなたの内に秘められたもの",
    "Tauche ins Unterbewusstsein": "潜在意識の奥へ",
    "Begib dich auf eine mystische Reise durch deine inneren Welten. Beantworte drei intuitive Fragen mit deiner Stimme oder deinen Worten, um verborgene Wahrheiten über dich zu enthüllen.": "心の内なる世界を巡る神秘的な旅へ。声や言葉で3つの直感的な質問に答え、自分の中に眠る真実を見つけましょう。",
    "Reise beginnen": "旅を始める",
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


def google_request(text: str, target: str) -> str:
    params = urllib.parse.urlencode({"client": "gtx", "sl": "de", "tl": target, "dt": "t", "q": text})
    url = "https://translate.googleapis.com/translate_a/single?" + params
    request = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0 HarmonyLocalization/1.0"})
    last_error: Exception | None = None
    for attempt in range(5):
        try:
            with urllib.request.urlopen(request, timeout=30) as response:
                data = json.loads(response.read().decode("utf-8"))
            return "".join(segment[0] for segment in data[0] if segment and segment[0]).strip()
        except Exception as exc:  # noqa: BLE001
            last_error = exc
            time.sleep(1.2 * (attempt + 1))
    raise RuntimeError(f"Translation failed for {target}: {text!r}: {last_error}")


def request_translation(text: str, target: str) -> str:
    protected, placeholders = protect(text)
    return restore(google_request(protected, target), placeholders)


def request_batch(items: list[str], target: str) -> dict[str, str]:
    protected_rows: list[tuple[str, str, dict[str, str]]] = []
    for index, source in enumerate(items):
        protected, placeholders = protect(source)
        marker = f"[[HARMONY{index:03d}]]"
        protected_rows.append((source, marker, placeholders))
    payload = "\n".join(f"{marker} {protect(source)[0]}" for source, marker, _ in protected_rows)
    translated = google_request(payload, target)
    parsed: dict[int, str] = {}
    pattern = re.compile(r'^\[\[HARMONY(\d{3})\]\]\s*(.*)$')
    for line in translated.splitlines():
        match = pattern.match(line.strip())
        if match:
            parsed[int(match.group(1))] = match.group(2).strip()
    if len(parsed) != len(items):
        return {source: request_translation(source, target) for source in items}
    result: dict[str, str] = {}
    for index, (source, _, placeholders) in enumerate(protected_rows):
        result[source] = restore(parsed[index], placeholders)
    return result


def kotlin_escape(value: str) -> str:
    # `$` must be escaped as `\$` in Kotlin source. Using `${'$'}` here changes the
    # literal key shape and makes dynamic `${profile.partnerName}` catalog lookups miss.
    return (value.replace("\\", "\\\\")
                 .replace('"', '\\"')
                 .replace("\n", "\\n")
                 .replace("$", "\\$"))


def main() -> None:
    missing: dict[str, list[str]] = json.loads(MISSING.read_text(encoding="utf-8"))
    extras = EXTRA_UI_KEYS | introspection_german_values()
    maps: list[tuple[str, str, dict[str, str]]] = []

    for code, (enum_name, target) in TARGETS.items():
        keys = sorted((set(missing.get(code, [])) | extras) - INTERNAL_ONLY)
        translations: dict[str, str] = {}
        print(f"Translating {len(keys)} strings for {code} -> {target}", flush=True)
        for offset in range(0, len(keys), 8):
            chunk = keys[offset:offset + 8]
            translations.update(request_batch(chunk, target))
            if offset and offset % 80 == 0:
                print(f"  {code}: {min(offset + 8, len(keys))}/{len(keys)}", flush=True)
        if code == "ja":
            translations.update(JAPANESE_OVERRIDES)
        maps.append((code, enum_name, translations))

    lines = [
        "package com.example.ui",
        "",
        "/** Generated during the full localization repair; runtime lookup is entirely local/offline. */",
    ]
    for _, enum_name, translations in maps:
        map_name = f"LOCALIZATION_UPDATES_{enum_name}"
        lines.append(f"internal val {map_name}: Map<String, String> = mapOf(")
        for key in sorted(translations):
            lines.append(f'    "{kotlin_escape(key)}" to "{kotlin_escape(translations[key])}",')
        lines.append(")")
        lines.append("")

    lines.append("internal val LOCALIZATION_UPDATES: Map<AppLanguage, Map<String, String>> = mapOf(")
    for _, enum_name, _ in maps:
        lines.append(f"    AppLanguage.{enum_name} to LOCALIZATION_UPDATES_{enum_name},")
    lines.append(")")
    lines.append("")
    OUTPUT.write_text("\n".join(lines), encoding="utf-8")
    print(f"Wrote {OUTPUT}", flush=True)


if __name__ == "__main__":
    main()
