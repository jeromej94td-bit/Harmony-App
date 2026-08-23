#!/usr/bin/env python3
"""Verify the effective customer-facing localization after applying repair overrides."""
from __future__ import annotations

import sys
from pathlib import Path

import audit_localization as audit

ROOT = Path(__file__).resolve().parents[1]
UI = ROOT / "app/src/main/java/com/example/ui"
UPDATES = UI / "LocalizationUpdates.kt"

REPAIR_MAPS = {
    "it": "LOCALIZATION_UPDATES_ITALIAN",
    "fr": "LOCALIZATION_UPDATES_FRENCH",
    "ja": "LOCALIZATION_UPDATES_JAPANESE",
    "pl": "LOCALIZATION_UPDATES_POLISH",
    "es-419": "LOCALIZATION_UPDATES_SPANISH_LATIN_AMERICA",
    "es-ES": "LOCALIZATION_UPDATES_SPANISH_SPAIN",
    "pt-BR": "LOCALIZATION_UPDATES_PORTUGUESE_BRAZIL",
    "pt-PT": "LOCALIZATION_UPDATES_PORTUGUESE_PORTUGAL",
    "da": "LOCALIZATION_UPDATES_DANISH",
    "no": "LOCALIZATION_UPDATES_NORWEGIAN",
}

EXPECTED_JA = {
    "Schließen": "閉じる",
    "Dein Bild": "あなたの写真",
    "Partnerbild": "パートナーの写真",
    "Privater Paar-Chat": "二人だけのプライベートチャット",
    "Unbeantwortet": "未回答",
    "Unbeantwortete Fragen": "未回答の質問",
    "Entweder oder": "どちらか",
    "Frage": "質問",
    "Unterhaltung": "エンターテインメント",
    "Hochzeit": "結婚式",
    "Burger": "バーガー",
    "Aussehen": "見た目",
    "Das erste Treffen": "初めて会った日",
    "Überraschungspaket": "サプライズ小包",
    "Tauche ins Unterbewusstsein": "潜在意識の奥へ",
    "Reise beginnen": "旅を始める",
    "Handy weitergeben": "スマホを渡して",
    "ODER": "または",
}

SOURCE_CHECKS = {
    "screens/ChatScreen.kt": [
        'Text("Privater Paar-Chat"',
        'contentDescription = "Nutzer melden"',
        'contentDescription = "Bild hinzufügen"',
    ],
    "screens/PandaEitherOrScreen.kt": [
        'Text("🐼 Entweder oder"',
        'Text("$name entscheidet"',
        'Text("Der andere schaut kurz weg 🤫"',
        'Text("ODER"',
        'Text("Handy weitergeben"',
        'Text("Die erste Antwort bleibt geheim."',
        'Text("Nächste zufällige Frage"',
        'Text("Ihr kennt jede Entscheidung"',
    ],
    "screens/GamesScreen.kt": [
        'Text("Unbeantwortete Fragen"',
        'Text("${unanswered.size} Fragen warten auf euch"',
        'Text("Ihr habt bereits alle Fragen beantwortet."',
        'TextButton(onClick = onDismiss) { Text("Schließen"',
    ],
    "screens/MomentsScreen.kt": [
        'text = "${moment.emoji} ${moment.title}"',
        'text = moment.content,',
    ],
    "components/CommonUI.kt": [
        'label.uppercase(Locale.GERMAN)',
    ],
}


def error(message: str) -> None:
    print(f"::error::{message}")


def main() -> int:
    failed = False
    if not UPDATES.exists():
        error("LocalizationUpdates.kt is missing")
        return 1

    updates_text = UPDATES.read_text(encoding="utf-8")
    if "__CONTENT_TOO_LARGE__" in updates_text:
        error("LocalizationUpdates.kt still contains the temporary placeholder")
        failed = True
    if "HARMONYPLACE" in updates_text.upper():
        error("Unrestored translation placeholder remains in LocalizationUpdates.kt")
        failed = True

    canonical_all = audit.extract_map(UI / "EnglishContent.kt", "EXACT_ENGLISH_CONTENT")
    internal_only = getattr(audit, "INTERNAL_ONLY_KEYS", set())
    canonical = {
        key: value for key, value in canonical_all.items()
        if key not in audit.DEV_ONLY_KEYS
        and key not in internal_only
        and "Entwickler" not in key
    }
    if not canonical:
        error("Could not parse canonical English catalog")
        return 2

    repair_maps: dict[str, dict[str, str]] = {}
    for code, map_name in REPAIR_MAPS.items():
        repair = audit.parse_map_of(updates_text, map_name)
        repair_maps[code] = repair
        if not repair:
            error(f"Could not parse repair map {map_name}")
            failed = True

    for code, (filename, base_map_name, _repair_map_name) in audit.LOCALES.items():
        base = audit.extract_map(UI / filename, base_map_name)
        effective = dict(base)
        effective.update(repair_maps.get(code, {}))
        missing = sorted(set(canonical) - set(effective))
        if missing:
            error(f"{code}: effective localization still missing {len(missing)} customer keys")
            print(f"MISSING_EFFECTIVE[{code}]=" + " | ".join(missing))
            failed = True
        else:
            print(f"{code}: effective coverage OK ({len(effective)} keys)")

    ja = dict(audit.extract_map(UI / "JapaneseContent.kt", "EXACT_JAPANESE_CONTENT"))
    ja.update(repair_maps.get("ja", {}))
    for key, expected in EXPECTED_JA.items():
        actual = ja.get(key)
        if actual != expected:
            error(f"Japanese video regression remains: {key!r} -> {actual!r}, expected {expected!r}")
            failed = True

    introspection = (UI / "introspection/IntrospectionStrings.kt").read_text(encoding="utf-8")
    for token in [
        "AppLanguage.JAPANESE", "AppLanguage.POLISH", "AppLanguage.FRENCH",
        "AppLanguage.SPANISH_LATIN_AMERICA", "AppLanguage.SPANISH_SPAIN",
        "AppLanguage.PORTUGUESE_BRAZIL", "AppLanguage.PORTUGUESE_PORTUGAL",
        "AppLanguage.DANISH", "AppLanguage.NORWEGIAN",
        "AppLanguage.DUTCH", "AppLanguage.SWEDISH", "AppLanguage.ICELANDIC",
        "AppLanguage.KOREAN", "AppLanguage.CHINESE_SIMPLIFIED", "AppLanguage.CHINESE_TRADITIONAL",
    ]:
        if token not in introspection:
            error(f"IntrospectionStrings has no explicit {token} path")
            failed = True

    for rel, needles in SOURCE_CHECKS.items():
        source = (UI / rel).read_text(encoding="utf-8")
        for needle in needles:
            if needle in source:
                error(f"Hardcoded customer-facing source remains in {rel}: {needle}")
                failed = True

    catalog = (UI / "TranslationCatalog.kt").read_text(encoding="utf-8")
    if "LOCALIZATION_UPDATES_JAPANESE[german]?.let { return it }" not in catalog:
        error("Japanese correction overrides do not take precedence over legacy translations")
        failed = True
    if "PT_BR_REVIEWED_OVERRIDES[german]?.let { return it }" not in catalog:
        error("Brazilian Portuguese reviewed overrides do not take precedence")
        failed = True
    if "LOCALIZATION_UPDATES_PORTUGUESE_BRAZIL[german]?.let { return it }" not in catalog:
        error("Brazilian Portuguese current repair map does not take precedence over legacy generic Portuguese")
        failed = True

    if failed:
        print("Effective localization FAILED")
        return 1
    print("Effective localization PASSED")
    return 0


if __name__ == "__main__":
    sys.exit(main())
