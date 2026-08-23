#!/usr/bin/env python3
"""Repair concrete Kotlin build blockers exposed by localization CI.

This is intentionally narrow and idempotent: it normalizes malformed Kotlin dollar escaping
in generated locale catalogs and retains the PicShare widget preference compatibility repair.
Premium panda artwork is a real bundled resource and must never be downgraded by this script.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
UI = ROOT / "app/src/main/java/com/example/ui"
WIDGET = ROOT / "app/src/main/java/com/example/widget"


def write(path: Path, text: str) -> None:
    path.write_text(text, encoding="utf-8")


# Generated catalogs can contain multiple literal backslashes before Kotlin '$'.
# Repeatedly collapse them until exactly the single valid Kotlin escape '\$' remains.
for name in (
    "PortugueseBrazilContent.kt",
    "PortuguesePortugalContent.kt",
    "DutchContent.kt",
    "SwedishContent.kt",
    "IcelandicContent.kt",
    "KoreanContent.kt",
    "ChineseSimplifiedContent.kt",
    "ChineseTraditionalContent.kt",
):
    path = UI / name
    if not path.exists():
        continue
    text = path.read_text(encoding="utf-8")
    while r"\\$" in text:
        text = text.replace(r"\\$", r"\$")
    # Preserve stable Kotlin variable identifiers in localized literals.
    text = text.replace("parceiroName", "partnerName")
    write(path, text)

# HomeScreen imports this preference model; create it only if an older branch lacks it.
WIDGET.mkdir(parents=True, exist_ok=True)
prefs = WIDGET / "PicShareWidgetPreferences.kt"
if not prefs.exists():
    write(
        prefs,
        '''package com.example.widget

import android.content.Context

data class PicShareWidgetSettings(
    val caption: String = "",
    val showCaption: Boolean = true,
    val showStatus: Boolean = true,
    val shufflePictures: Boolean = false
)

object PicShareWidgetPreferences {
    private const val PREFS = "picshare_widget_settings"
    private const val KEY_CAPTION = "caption"
    private const val KEY_SHOW_CAPTION = "show_caption"
    private const val KEY_SHOW_STATUS = "show_status"
    private const val KEY_SHUFFLE = "shuffle_pictures"

    fun load(context: Context): PicShareWidgetSettings {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return PicShareWidgetSettings(
            caption = prefs.getString(KEY_CAPTION, "").orEmpty(),
            showCaption = prefs.getBoolean(KEY_SHOW_CAPTION, true),
            showStatus = prefs.getBoolean(KEY_SHOW_STATUS, true),
            shufflePictures = prefs.getBoolean(KEY_SHUFFLE, false)
        )
    }

    fun save(context: Context, settings: PicShareWidgetSettings) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CAPTION, settings.caption)
            .putBoolean(KEY_SHOW_CAPTION, settings.showCaption)
            .putBoolean(KEY_SHOW_STATUS, settings.showStatus)
            .putBoolean(KEY_SHUFFLE, settings.shufflePictures)
            .apply()
    }
}
'''
    )

print("Concrete Kotlin build blockers repaired")
