import re
import write_final_language_kt

final_map = write_final_language_kt.final_map

header = """package com.example.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

enum class AppLanguage {
    GERMAN,
    ENGLISH
}

object LanguageStore {
    private const val PREFS = "harmony_settings"
    private const val KEY_LANGUAGE = "app_language"

    fun get(context: Context): AppLanguage {
        val value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, AppLanguage.GERMAN.name)
        return runCatching { AppLanguage.valueOf(value ?: AppLanguage.GERMAN.name) }
            .getOrDefault(AppLanguage.GERMAN)
    }

    fun set(context: Context, language: AppLanguage) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language.name)
            .apply()
    }
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.GERMAN }

@Composable
fun tr(german: String, english: String): String =
    if (LocalAppLanguage.current == AppLanguage.ENGLISH) english else german

fun localizedContent(text: String, language: AppLanguage): String {
    if (language == AppLanguage.GERMAN) return text
    val trimmed = text.trim()
    return ENGLISH_CONTENT[trimmed] ?: ENGLISH_CONTENT[text] ?: text
}

@Composable
fun contentText(text: String): String =
    localizedContent(text, LocalAppLanguage.current)

private val ENGLISH_CONTENT = mapOf(
"""

footer = """
)
"""

entries = []
for k, v in sorted(final_map.items()):
    # Escape quotes and backslashes for Kotlin string literal
    k_esc = k.replace('\\', '\\\\').replace('"', '\\"').replace('\n', '\\n')
    v_esc = v.replace('\\', '\\\\').replace('"', '\\"').replace('\n', '\\n')
    entries.append(f'    "{k_esc}" to "{v_esc}"')

content = header + ",\n".join(entries) + footer

with open("app/src/main/java/com/example/ui/Language.kt", "w") as f:
    f.write(content)

print("Language.kt successfully written with", len(entries), "entries.")
