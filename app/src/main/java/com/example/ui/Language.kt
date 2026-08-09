package com.example.ui

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
    return ENGLISH_CONTENT[text.trim()] ?: text
}

@Composable
fun contentText(text: String): String =
    localizedContent(text, LocalAppLanguage.current)

private val ENGLISH_CONTENT = mapOf(
    "Fragen & Spiele" to "Questions & Games",
    "Kategorien" to "Categories",
    "Tägliche Aktivität" to "Daily activity",
    "Für dich empfohlen" to "Recommended for you",
    "Paar-Statistiken" to "Couple statistics",
    "Gemeinsame Tage" to "Days together",
    "Beantwortete Fragen" to "Questions answered",
    "Besuchte Städte" to "Cities visited",
    "Besuchte Länder" to "Countries visited",
    "Widgets" to "Widgets",
    "Du fehlst mir" to "I miss you",
    "Denke an dich" to "Thinking of you",
    "Kuss senden" to "Send a kiss",
    "Aufwärmen" to "Warm-up",
    "Beziehung" to "Relationship",
    "Sex & Liebe" to "Sex & Love",
    "Moralische Werte" to "Moral values",
    "Geld & Finanzen" to "Money & Finances",
    "Einander kennenlernen" to "Getting to know each other",
    "Reisen" to "Travel",
    "Familie" to "Family",
    "Hobbys" to "Hobbies",
    "Wer würde eher?" to "Who's more likely?",
    "Zeichnen" to "Draw",
    "Das oder das?" to "This or That?",
    "Zustimmen oder Ablehnen" to "Agree or Disagree",
    "Ich habe noch nie" to "Never have I ever",
    "Was magst du lieber?" to "Which do you prefer?",
    "Antwort mit einem Foto" to "Answer with a photo",
    "Tiefe Gespräche" to "Deep conversations",
    "Reden vor ..." to "Talk before ...",
    "Zuhause & Alltag" to "Home & everyday life",
    "Der perfekte Heiratsantrag" to "The perfect proposal",
    "Vorlieben für den Antrag" to "Proposal preferences",
    "Diskutiere vor dem Kinderkriegen" to "Discuss before having children",
    "Vor der Anschaffung eines Haustiers besprechen" to "Discuss before getting a pet",
    "Vor der gemeinsamen Reise besprechen" to "Discuss before travelling together",
    "Vor dem Kauf eines Hauses besprechen" to "Discuss before buying a house",
    "Reiseziele" to "Travel destinations",
    "Alle" to "All",
    "Du bist dran" to "Your turn",
    "Beantwortet" to "Answered",
    "Überspringen" to "Skip",
    "Weiter" to "Next",
    "Schließen" to "Close",
    "Bearbeiten" to "Edit",
    "Profil" to "Profile",
    "Dein Name" to "Your name",
    "Partnerin" to "Partner",
    "Zusammen seit" to "Together since",
    "Partner-Simulator" to "Partner simulator",
    "Entwickler-Modus" to "Developer mode",
    "Entwickler Studio Öffnen" to "Open Developer Studio",
    "Sprache" to "Language",
    "Deutsch" to "German",
    "Englisch" to "English",
    "KI-Beziehungscoach" to "AI relationship coach",
    "KI-Date-Ideen" to "AI date ideas",
    "Analyse starten" to "Start analysis",
    "Ideen generieren" to "Generate ideas",
    "Namen und Startdatum eurer Beziehung." to "Your names and relationship start date.",
    "Name Partnerin" to "Partner's name",
    "Fragen & Spiele" to "Questions & Games",
    "Diskutiert eure Antworten" to "Discuss your answers"
)
