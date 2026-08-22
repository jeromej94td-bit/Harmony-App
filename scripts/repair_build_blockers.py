#!/usr/bin/env python3
"""Repair concrete pre-existing Kotlin build blockers exposed by localization CI.

This is intentionally narrow and idempotent: it only fixes malformed literal escaping,
a stale Portuguese helper name, two references to non-existent panda raster resources, and
the missing PicShare widget preference model that HomeScreen already imports.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
UI = ROOT / "app/src/main/java/com/example/ui"
WIDGET = ROOT / "app/src/main/java/com/example/widget"


def write(path: Path, text: str) -> None:
    path.write_text(text, encoding="utf-8")


# 1) Two legacy Portuguese catalogs accidentally contain two backslashes before Kotlin '$'.
# Kotlin then escapes the backslash and tries to interpolate partnerName/profile/d as variables.
for name in ("PortugueseBrazilContent.kt", "PortuguesePortugalContent.kt"):
    path = UI / name
    text = path.read_text(encoding="utf-8")
    text = text.replace(r"\\$", r"\$")
    # A translated variable identifier would also break the stable lookup/value contract.
    text = text.replace("parceiroName", "partnerName")
    write(path, text)

# 2) The Brazilian helper is named localizeBrazilianPortugueseDynamicContent in its source.
path = UI / "TranslationCatalog.kt"
text = path.read_text(encoding="utf-8")
text = text.replace("localizePortugueseBrazilDynamicContent", "localizeBrazilianPortugueseDynamicContent")
write(path, text)

# 3) GameCategoryVisuals referenced raster resources that do not exist. The repo already has
# the vector PandaCategoryIcon implementation for exactly these category IDs, so use it.
path = UI / "components/GameCategoryVisuals.kt"
text = path.read_text(encoding="utf-8")
old = '''        "wer" -> PandaArtworkIcon(
            drawableRes = R.drawable.panda_thinking_harmony,
            accent = accent,
            animationLabel = "thinking_panda",
            modifier = modifier
        )

        "nie" -> PandaArtworkIcon(
            drawableRes = R.drawable.panda_never_harmony,
            accent = accent,
            animationLabel = "never_panda",
            modifier = modifier
        )'''
new = '''        "wer" -> PandaCategoryIcon(categoryId = "wer", accent = accent, modifier = modifier)

        "nie" -> PandaCategoryIcon(categoryId = "nie", accent = accent, modifier = modifier)'''
text = text.replace(old, new)
write(path, text)

# 4) HomeScreen already imports this preference model, but no implementation exists in the repo.
# Keep it deliberately small and compatible with the fields HomeScreen reads/writes.
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
