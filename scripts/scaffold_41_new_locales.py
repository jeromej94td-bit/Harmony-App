#!/usr/bin/env python3
"""Idempotently wire the 41 approved production locales into Harmony."""
from pathlib import Path

from production_locale_registry import NEW_LOCALES, RTL_CODES

ROOT = Path(__file__).resolve().parents[1]
UI = ROOT / "app/src/main/java/com/example/ui"


def patch_language() -> None:
    path = UI / "Language.kt"
    text = path.read_text(encoding="utf-8")
    if 'HUNGARIAN("hu"' not in text:
        old = '    CHINESE_TRADITIONAL("zh-TW", "繁體中文", "Chinese (Traditional)", "🇹🇼");'
        if old not in text:
            raise RuntimeError("Could not find AppLanguage insertion point")
        rows = ['    CHINESE_TRADITIONAL("zh-TW", "繁體中文", "Chinese (Traditional)", "🇹🇼"),']
        for index, item in enumerate(NEW_LOCALES):
            suffix = ";" if index == len(NEW_LOCALES) - 1 else ","
            rows.append(
                f'    {item["enum"]}("{item["code"]}", "{item["native"]}", '
                f'"{item["english"]}", "{item["flag"]}"){suffix}'
            )
        text = text.replace(old, "\n".join(rows))
    if "val isRtl: Boolean" not in text:
        marker = "\n    companion object {"
        rtl_codes = ", ".join(f'"{code}"' for code in sorted(RTL_CODES))
        prop = f"\n    val isRtl: Boolean\n        get() = code in setOf({rtl_codes})\n"
        if marker not in text:
            raise RuntimeError("Could not find AppLanguage companion insertion point")
        text = text.replace(marker, prop + marker, 1)
    path.write_text(text, encoding="utf-8")


def patch_catalog() -> None:
    path = UI / "TranslationCatalog.kt"
    text = path.read_text(encoding="utf-8")
    if "AppLanguage.HUNGARIAN -> EXACT_HUNGARIAN_CONTENT[german]" not in text:
        old = "        AppLanguage.CHINESE_TRADITIONAL -> EXACT_CHINESE_TRADITIONAL_CONTENT[german]\n"
        if old not in text:
            raise RuntimeError("Could not find exact catalog insertion point")
        routes = "".join(
            f'        AppLanguage.{item["enum"]} -> {item["exact"]}[german]\n'
            for item in NEW_LOCALES
        )
        text = text.replace(old, old + routes, 1)
    if "AppLanguage.HUNGARIAN -> localizeHungarianDynamicContent(text)" not in text:
        old = "            AppLanguage.CHINESE_TRADITIONAL -> localizeChineseTraditionalDynamicContent(text)\n"
        if old not in text:
            raise RuntimeError("Could not find dynamic catalog insertion point")
        routes = "".join(
            f'            AppLanguage.{item["enum"]} -> {item["dynamic"]}(text)\n'
            for item in NEW_LOCALES
        )
        text = text.replace(old, old + routes, 1)
    path.write_text(text, encoding="utf-8")


def patch_introspection() -> None:
    path = UI / "introspection/IntrospectionStrings.kt"
    text = path.read_text(encoding="utf-8")
    if "AppLanguage.HUNGARIAN" in text:
        return
    old = "            AppLanguage.CHINESE_TRADITIONAL ->"
    additions = "".join(f'            AppLanguage.{item["enum"]},\n' for item in NEW_LOCALES[:-1])
    additions += f'            AppLanguage.{NEW_LOCALES[-1]["enum"]} ->'
    replacement = "            AppLanguage.CHINESE_TRADITIONAL,\n" + additions
    if old not in text:
        raise RuntimeError("Could not find Introspection locale insertion points")
    text = text.replace(old, replacement)
    path.write_text(text, encoding="utf-8")


def patch_main_activity_rtl() -> None:
    path = ROOT / "app/src/main/java/com/example/MainActivity.kt"
    text = path.read_text(encoding="utf-8")
    if "import androidx.compose.ui.platform.LocalLayoutDirection" not in text:
        marker = "import androidx.compose.ui.platform.LocalContext\n"
        if marker not in text:
            raise RuntimeError("Could not find LocalContext import for RTL patch")
        text = text.replace(marker, marker + "import androidx.compose.ui.platform.LocalLayoutDirection\n", 1)
    if "import androidx.compose.ui.unit.LayoutDirection" not in text:
        marker = "import androidx.compose.ui.unit.dp\n"
        if marker not in text:
            raise RuntimeError("Could not find dp import for RTL patch")
        text = text.replace(marker, "import androidx.compose.ui.unit.LayoutDirection\n" + marker, 1)
    old = "            CompositionLocalProvider(LocalAppLanguage provides currentLanguage) {"
    if "currentLanguage.isRtl" not in text:
        new = """            CompositionLocalProvider(
                LocalAppLanguage provides currentLanguage,
                LocalLayoutDirection provides if (currentLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {"""
        if old not in text:
            raise RuntimeError("Could not find root CompositionLocalProvider for RTL patch")
        text = text.replace(old, new, 1)
    path.write_text(text, encoding="utf-8")


def write_stubs() -> None:
    for item in NEW_LOCALES:
        path = UI / item["filename"]
        if path.exists():
            continue
        path.write_text(
            "package com.example.ui\n\n"
            "/** Generated production locale catalog aligned to Harmony's canonical customer catalog. */\n"
            f'internal val {item["exact"]}: Map<String, String> = mapOf()\n\n'
            f'internal fun {item["dynamic"]}(text: String): String? =\n'
            f'    localizeGeneratedLocaleDynamicContent(text, {item["exact"]})\n',
            encoding="utf-8",
        )


def main() -> None:
    patch_language()
    patch_catalog()
    patch_introspection()
    patch_main_activity_rtl()
    write_stubs()
    print("41-locale scaffolding applied")


if __name__ == "__main__":
    main()
