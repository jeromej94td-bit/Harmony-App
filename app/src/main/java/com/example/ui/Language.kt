package com.example.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

/**
 * Supported app locales. Add future languages here using their stable BCP-47 language code.
 * The language selector is generated from this enum, so it does not need language-specific UI edits.
 */
enum class AppLanguage(val code: String, val nativeName: String, val englishName: String) {
    GERMAN("de", "Deutsch", "German"),
    ENGLISH("en", "English", "English");

    companion object {
        fun fromStored(value: String?): AppLanguage = entries.firstOrNull {
            it.code.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true)
        } ?: GERMAN
    }
}

object LanguageStore {
    private const val PREFS = "harmony_settings"
    private const val KEY_LANGUAGE = "app_language"

    fun get(context: Context): AppLanguage {
        val value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, AppLanguage.GERMAN.code)
        return AppLanguage.fromStored(value)
    }

    fun set(context: Context, language: AppLanguage) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language.code)
            .apply()
    }
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.GERMAN }

@Composable
fun tr(german: String, english: String): String {
    val language = LocalAppLanguage.current
    if (language == AppLanguage.GERMAN) return german
    return TranslationCatalog.exact(german, language)
        ?: english.takeIf { language == AppLanguage.ENGLISH }
        ?: german
}

/**
 * Translates complete display strings only. There is intentionally no word-by-word fallback:
 * partial substitutions create broken mixed-language copy and must never reach customers.
 */
fun localizedContent(text: String, language: AppLanguage): String {
    if (language == AppLanguage.GERMAN) return text
    return TranslationCatalog.translate(text, language) ?: text
}

@Composable
fun contentText(text: String): String = localizedContent(text, LocalAppLanguage.current)

private fun exactEnglish(text: String): String =
    TranslationCatalog.exact(text, AppLanguage.ENGLISH) ?: text

/** Localizes variable-bearing app messages without translating isolated words. */
internal fun localizeEnglishDynamicContent(text: String): String? {
    Regex("^(\\d+) Bilder geladen — Namen prüfen, dann erstellen\\.$").matchEntire(text)?.let {
        return "${it.groupValues[1]} images loaded — review the names, then create the pack."
    }
    Regex("^(\\d+) Bilder geladen\\.$").matchEntire(text)?.let {
        return "${it.groupValues[1]} images loaded."
    }
    Regex("^(\\d+) Paare aus (\\d+) Bildern$").matchEntire(text)?.let {
        return "${it.groupValues[1]} pairs from ${it.groupValues[2]} images"
    }
    Regex("^Bild (\\d+) von (\\d+)…$").matchEntire(text)?.let {
        return "Image ${it.groupValues[1]} of ${it.groupValues[2]}…"
    }
    Regex("^Speichere Bild (\\d+) von (\\d+)…$").matchEntire(text)?.let {
        return "Saving image ${it.groupValues[1]} of ${it.groupValues[2]}…"
    }
    Regex("^Paar (\\d+)$").matchEntire(text)?.let { return "Pair ${it.groupValues[1]}" }
    Regex("^Frage (\\d+)$").matchEntire(text)?.let { return "Question ${it.groupValues[1]}" }
    Regex("^Schritt (\\d+)$").matchEntire(text)?.let { return "Step ${it.groupValues[1]}" }
    Regex("^(\\d+) Paare$").matchEntire(text)?.let { return "${it.groupValues[1]} pairs" }
    Regex("^(\\d+) Fragen$").matchEntire(text)?.let { return "${it.groupValues[1]} questions" }
    Regex("^(\\d+) Schritt\\(e\\)$").matchEntire(text)?.let { return "${it.groupValues[1]} steps" }
    Regex("^(\\d+) Paare · (\\d+) Fragen$").matchEntire(text)?.let {
        return "${it.groupValues[1]} pairs · ${it.groupValues[2]} questions"
    }
    Regex("^(\\d+) Einträge$").matchEntire(text)?.let { return "${it.groupValues[1]} entries" }
    Regex("^Fertig: (\\d+) Pakete · (\\d+) Bilder · (.+)$").matchEntire(text)?.let {
        return "Done: ${it.groupValues[1]} packs · ${it.groupValues[2]} images · ${it.groupValues[3]}"
    }
    Regex("^🎉 (\\d+) Pakete/Ketten & Bilder erfolgreich eingespielt!$").matchEntire(text)?.let {
        return "🎉 ${it.groupValues[1]} packs/chains and images imported successfully!"
    }
    Regex("^🎉 '(.+)' angelegt · (\\d+) Paare spielbereit$").matchEntire(text)?.let {
        return "🎉 '${exactEnglish(it.groupValues[1])}' created · ${it.groupValues[2]} pairs ready to play"
    }
    Regex("^• ([AB]): (.+) \\(Bild: (.+)\\)$").matchEntire(text)?.let {
        return "• ${it.groupValues[1]}: ${exactEnglish(it.groupValues[2])} (Image: ${it.groupValues[3]})"
    }
    Regex("^([AB]): (.+)$").matchEntire(text)?.let {
        return "${it.groupValues[1]}: ${exactEnglish(it.groupValues[2])}"
    }
    Regex("^'(.+)' gelöscht\\.$").matchEntire(text)?.let {
        return "'${exactEnglish(it.groupValues[1])}' deleted."
    }
    Regex("^'(.+)' gespeichert\\.$").matchEntire(text)?.let {
        return "'${exactEnglish(it.groupValues[1])}' saved."
    }
    Regex("^Kategorie '(.+)' gespeichert\\.$").matchEntire(text)?.let {
        return "Category '${exactEnglish(it.groupValues[1])}' saved."
    }
    Regex("^Kette '(.+)' gespeichert\\.$").matchEntire(text)?.let {
        return "Chain '${exactEnglish(it.groupValues[1])}' saved."
    }
    Regex("^Kette '(.+)' gelöscht\\.$").matchEntire(text)?.let {
        return "Chain '${exactEnglish(it.groupValues[1])}' deleted."
    }
    Regex("^Bild für '(.+)' gesetzt\\.$").matchEntire(text)?.let {
        return "Image set for '${exactEnglish(it.groupValues[1])}'."
    }
    Regex("^Eigenes Bild für '(.+)' entfernt\\.$").matchEntire(text)?.let {
        return "Custom image removed for '${exactEnglish(it.groupValues[1])}'."
    }
    Regex("^(.+) „(.+)\" an (.+) gesendet$").matchEntire(text)?.let {
        return "${it.groupValues[1]} “${exactEnglish(it.groupValues[2])}” sent to ${it.groupValues[3]}"
    }
    Regex("^6 Monate im (.+)$").matchEntire(text)?.let {
        return "6 months in ${exactEnglish(it.groupValues[1])}"
    }
    Regex("^1 Jahr lang in (.+)$").matchEntire(text)?.let {
        return "1 year in ${exactEnglish(it.groupValues[1])}"
    }
    Regex("^Weil du (.+) gewählt hast …$").matchEntire(text)?.let {
        return "Because you chose ${exactEnglish(it.groupValues[1])}…"
    }
    Regex("^Verbinde dich mit (.+), um die Antwort zu sehen$").matchEntire(text)?.let {
        return "Connect with ${it.groupValues[1]} to see the answer"
    }
    Regex("^Verbinde dich mit (.+)$").matchEntire(text)?.let {
        return "Connect with ${it.groupValues[1]}"
    }
    Regex("^Deine Antworten sind gespeichert\\. Sobald (.+) das Paket beendet, werden beide Antworten gemeinsam sichtbar\\.$")
        .matchEntire(text)?.let {
            return "Your answers have been saved. Once ${it.groupValues[1]} finishes the pack, both sets of answers will appear together."
        }
    Regex("^Fehler bei der Umformulierung: (.+)$").matchEntire(text)?.let {
        return "Rewriting failed: ${it.groupValues[1]}"
    }
    Regex("^Fehler bei der Analyse: (.+)$").matchEntire(text)?.let {
        return "Analysis failed: ${it.groupValues[1]}"
    }
    Regex("^Fehler bei der Ideengenerierung: (.+)$").matchEntire(text)?.let {
        return "Idea generation failed: ${it.groupValues[1]}"
    }
    if (text.contains(" · ")) {
        val translated = text.split(" · ").joinToString(" · ") {
            TranslationCatalog.translate(it, AppLanguage.ENGLISH) ?: it
        }
        if (translated != text) return translated
    }
    return null
}
