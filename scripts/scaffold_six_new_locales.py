#!/usr/bin/env python3
"""Idempotently wire six additional production locales into Harmony."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
UI = ROOT / "app/src/main/java/com/example/ui"

LOCALES = [
    ("DUTCH", "nl", "Nederlands", "Dutch", "🇳🇱", "DutchContent.kt", "EXACT_DUTCH_CONTENT", "localizeDutchDynamicContent"),
    ("SWEDISH", "sv", "Svenska", "Swedish", "🇸🇪", "SwedishContent.kt", "EXACT_SWEDISH_CONTENT", "localizeSwedishDynamicContent"),
    ("ICELANDIC", "is", "Íslenska", "Icelandic", "🇮🇸", "IcelandicContent.kt", "EXACT_ICELANDIC_CONTENT", "localizeIcelandicDynamicContent"),
    ("KOREAN", "ko", "한국어", "Korean", "🇰🇷", "KoreanContent.kt", "EXACT_KOREAN_CONTENT", "localizeKoreanDynamicContent"),
    ("CHINESE_SIMPLIFIED", "zh-CN", "简体中文", "Chinese (Simplified)", "🇨🇳", "ChineseSimplifiedContent.kt", "EXACT_CHINESE_SIMPLIFIED_CONTENT", "localizeChineseSimplifiedDynamicContent"),
    ("CHINESE_TRADITIONAL", "zh-TW", "繁體中文", "Chinese (Traditional)", "🇹🇼", "ChineseTraditionalContent.kt", "EXACT_CHINESE_TRADITIONAL_CONTENT", "localizeChineseTraditionalDynamicContent"),
]


def write_if_changed(path: Path, text: str) -> None:
    if not path.exists() or path.read_text(encoding="utf-8") != text:
        path.write_text(text, encoding="utf-8")


def patch_language() -> None:
    path = UI / "Language.kt"
    text = path.read_text(encoding="utf-8")
    if "CHINESE_TRADITIONAL(" not in text:
        old = '    NORWEGIAN("no", "Norsk", "Norwegian", "🇳🇴");'
        additions = [
            '    NORWEGIAN("no", "Norsk", "Norwegian", "🇳🇴"),',
            '    DUTCH("nl", "Nederlands", "Dutch", "🇳🇱"),',
            '    SWEDISH("sv", "Svenska", "Swedish", "🇸🇪"),',
            '    ICELANDIC("is", "Íslenska", "Icelandic", "🇮🇸"),',
            '    KOREAN("ko", "한국어", "Korean", "🇰🇷"),',
            '    CHINESE_SIMPLIFIED("zh-CN", "简体中文", "Chinese (Simplified)", "🇨🇳"),',
            '    CHINESE_TRADITIONAL("zh-TW", "繁體中文", "Chinese (Traditional)", "🇹🇼");',
        ]
        if old not in text:
            raise RuntimeError("Could not find AppLanguage insertion point")
        text = text.replace(old, "\n".join(additions))
        path.write_text(text, encoding="utf-8")


def patch_catalog() -> None:
    path = UI / "TranslationCatalog.kt"
    text = path.read_text(encoding="utf-8")
    if "AppLanguage.DUTCH -> EXACT_DUTCH_CONTENT[german]" not in text:
        old = "        AppLanguage.NORWEGIAN -> EXACT_NORWEGIAN_CONTENT[german]\n"
        new = old + """        AppLanguage.DUTCH -> EXACT_DUTCH_CONTENT[german]
        AppLanguage.SWEDISH -> EXACT_SWEDISH_CONTENT[german]
        AppLanguage.ICELANDIC -> EXACT_ICELANDIC_CONTENT[german]
        AppLanguage.KOREAN -> EXACT_KOREAN_CONTENT[german]
        AppLanguage.CHINESE_SIMPLIFIED -> EXACT_CHINESE_SIMPLIFIED_CONTENT[german]
        AppLanguage.CHINESE_TRADITIONAL -> EXACT_CHINESE_TRADITIONAL_CONTENT[german]
"""
        if old not in text:
            raise RuntimeError("Could not find TranslationCatalog baseExact insertion point")
        text = text.replace(old, new)
    if "AppLanguage.DUTCH -> localizeDutchDynamicContent(text)" not in text:
        old = "            AppLanguage.NORWEGIAN -> localizeNorwegianDynamicContent(text)\n"
        new = old + """            AppLanguage.DUTCH -> localizeDutchDynamicContent(text)
            AppLanguage.SWEDISH -> localizeSwedishDynamicContent(text)
            AppLanguage.ICELANDIC -> localizeIcelandicDynamicContent(text)
            AppLanguage.KOREAN -> localizeKoreanDynamicContent(text)
            AppLanguage.CHINESE_SIMPLIFIED -> localizeChineseSimplifiedDynamicContent(text)
            AppLanguage.CHINESE_TRADITIONAL -> localizeChineseTraditionalDynamicContent(text)
"""
        if old not in text:
            raise RuntimeError("Could not find TranslationCatalog dynamic insertion point")
        text = text.replace(old, new)
    path.write_text(text, encoding="utf-8")


def patch_introspection() -> None:
    path = UI / "introspection/IntrospectionStrings.kt"
    text = path.read_text(encoding="utf-8")
    if "AppLanguage.CHINESE_TRADITIONAL" not in text:
        old = """            AppLanguage.DANISH,
            AppLanguage.NORWEGIAN ->"""
        new = """            AppLanguage.DANISH,
            AppLanguage.NORWEGIAN,
            AppLanguage.DUTCH,
            AppLanguage.SWEDISH,
            AppLanguage.ICELANDIC,
            AppLanguage.KOREAN,
            AppLanguage.CHINESE_SIMPLIFIED,
            AppLanguage.CHINESE_TRADITIONAL ->"""
        if old not in text:
            raise RuntimeError("Could not find IntrospectionStrings locale group")
        text = text.replace(old, new)
        path.write_text(text, encoding="utf-8")


def patch_audit() -> None:
    path = ROOT / "scripts/audit_localization.py"
    text = path.read_text(encoding="utf-8")
    if '"nl": ("DutchContent.kt"' not in text:
        old = '    "no": ("NorwegianContent.kt", "EXACT_NORWEGIAN_CONTENT", "LOCALIZATION_UPDATES_NORWEGIAN"),\n'
        new = old + """    "nl": ("DutchContent.kt", "EXACT_DUTCH_CONTENT", None),
    "sv": ("SwedishContent.kt", "EXACT_SWEDISH_CONTENT", None),
    "is": ("IcelandicContent.kt", "EXACT_ICELANDIC_CONTENT", None),
    "ko": ("KoreanContent.kt", "EXACT_KOREAN_CONTENT", None),
    "zh-CN": ("ChineseSimplifiedContent.kt", "EXACT_CHINESE_SIMPLIFIED_CONTENT", None),
    "zh-TW": ("ChineseTraditionalContent.kt", "EXACT_CHINESE_TRADITIONAL_CONTENT", None),
"""
        if old not in text:
            raise RuntimeError("Could not find audit LOCALES insertion point")
        text = text.replace(old, new)
    if '"AppLanguage.CHINESE_TRADITIONAL"' not in text:
        old = '        "AppLanguage.DANISH", "AppLanguage.NORWEGIAN",\n'
        new = old + '        "AppLanguage.DUTCH", "AppLanguage.SWEDISH", "AppLanguage.ICELANDIC",\n        "AppLanguage.KOREAN", "AppLanguage.CHINESE_SIMPLIFIED", "AppLanguage.CHINESE_TRADITIONAL",\n'
        if old not in text:
            raise RuntimeError("Could not find audit introspection token insertion point")
        text = text.replace(old, new)
    path.write_text(text, encoding="utf-8")


def patch_effective_verifier() -> None:
    path = ROOT / "scripts/verify_localization_repair.py"
    text = path.read_text(encoding="utf-8")
    if '"AppLanguage.CHINESE_TRADITIONAL"' not in text:
        old = '        "AppLanguage.DANISH", "AppLanguage.NORWEGIAN",\n'
        new = old + '        "AppLanguage.DUTCH", "AppLanguage.SWEDISH", "AppLanguage.ICELANDIC",\n        "AppLanguage.KOREAN", "AppLanguage.CHINESE_SIMPLIFIED", "AppLanguage.CHINESE_TRADITIONAL",\n'
        if old not in text:
            raise RuntimeError("Could not find effective verifier introspection token insertion point")
        text = text.replace(old, new)
        path.write_text(text, encoding="utf-8")


def write_support() -> None:
    path = UI / "GeneratedLocaleSupport.kt"
    text = '''package com.example.ui

/** Shared dynamic localization for generated production catalogs. */
internal fun localizeGeneratedLocaleDynamicContent(
    text: String,
    exact: Map<String, String>
): String? {
    Regex("^([\\p{So}\\p{Sk}\\uFE0F\\u200D]+\\s+)(.+)$").matchEntire(text)?.let { match ->
        exact[match.groupValues[2]]?.let { return match.groupValues[1] + it }
    }
    Regex("^(\\d+[.)]\\s+)(.+)$").matchEntire(text)?.let { match ->
        exact[match.groupValues[2]]?.let { return match.groupValues[1] + it }
    }

    // Resolve variable-bearing catalog templates while preserving names/counts exactly.
    for ((source, target) in exact) {
        val tokens = Regex("(\\\\?\\$\\{[^}]+\\}|\\{[^}]+\\})").findAll(source).map { it.value }.toList()
        if (tokens.isEmpty()) continue
        var cursor = 0
        val pattern = StringBuilder("^")
        for (token in tokens) {
            val index = source.indexOf(token, cursor)
            pattern.append(Regex.escape(source.substring(cursor, index)))
            pattern.append("(.+?)")
            cursor = index + token.length
        }
        pattern.append(Regex.escape(source.substring(cursor))).append('$')
        val match = Regex(pattern.toString()).matchEntire(text) ?: continue
        var localized = target
        tokens.forEachIndexed { index, token ->
            localized = localized.replace(token, match.groupValues[index + 1])
        }
        return localized
    }

    if (text.contains(" · ")) {
        val parts = text.split(" · ")
        val translated = parts.map { exact[it] ?: it }
        if (translated != parts) return translated.joinToString(" · ")
    }
    return null
}
'''
    write_if_changed(path, text)


def write_stubs() -> None:
    for _enum, _code, _native, _english, _flag, filename, exact_name, dynamic_name in LOCALES:
        path = UI / filename
        if path.exists():
            continue
        text = f'''package com.example.ui

/** Generated production locale catalog. */
internal val {exact_name}: Map<String, String> = mapOf()

internal fun {dynamic_name}(text: String): String? =
    localizeGeneratedLocaleDynamicContent(text, {exact_name})
'''
        path.write_text(text, encoding="utf-8")


if __name__ == "__main__":
    patch_language()
    patch_catalog()
    patch_introspection()
    patch_audit()
    patch_effective_verifier()
    write_support()
    write_stubs()
    print("Six-locale scaffolding applied")
