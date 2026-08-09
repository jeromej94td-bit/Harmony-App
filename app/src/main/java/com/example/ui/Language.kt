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
    return ENGLISH_CONTENT[text.trim()] ?: translateGermanContent(text)
}

@Composable
fun contentText(text: String): String =
    localizedContent(text, LocalAppLanguage.current)

private fun translateGermanContent(text: String): String {
    if (!looksGerman(text)) return text
    var result = text
    EXACT_CONTENT_TRANSLATIONS.entries
        .sortedByDescending { it.key.length }
        .forEach { (german, english) ->
            result = result.replace(german, english, ignoreCase = true)
        }
    GERMAN_WORD_TRANSLATIONS.entries
        .sortedByDescending { it.key.length }
        .forEach { (german, english) ->
            result = Regex("\\b${Regex.escape(german)}\\b", RegexOption.IGNORE_CASE)
                .replace(result, english)
        }
    return result
}

private fun looksGerman(text: String): Boolean =
    text.any { it in "ÄÖÜäöüß" } ||
        Regex("(?i)\\b(der|die|das|wir|ihr|euch|uns|mit|für|und|oder|wenn|werden|haben|sein|nicht|mehr|eine|einen|einem|einer|zu|von|im|in|auf|vor|über|dass|wie|was|wer|kann|können|müssen|möchte|möchtest)\\b")
            .containsMatchIn(text)

private val EXACT_CONTENT_TRANSLATIONS = mapOf(
    "Sind wir in der Lage, alle Kosten für ein Kind/mehrere Kinder zu decken?" to "Can we afford all the costs of one child/several children?",
    "Möglicherweise müssen wir Einsparungen vornehmen" to "We may need to make some savings",
    "Werden wir genug Zeit für das Kind / die Kinder haben?" to "Will we have enough time for the child / children?",
    "Ja, wir werden sicherstellen, dass die Zeit mit der Familie Vorrang hat." to "Yes, we will make sure family time comes first.",
    "Wie werden wir Zeit für unsere Beziehung finden, wenn das Baby da ist?" to "How will we find time for our relationship when the baby is here?",
    "Regelmäßige Rendezvous oder gemeinsame Zeit" to "Regular dates or quality time together",
    "Diskutiert eure Antworten" to "Discuss your answers",
    "Was ist dein Lieblingswetter?" to "What is your favorite type of weather?"
)

private val GERMAN_WORD_TRANSLATIONS = mapOf(
    "möglicherweise" to "maybe", "müssen" to "must", "muss" to "must", "wir" to "we",
    "ihr" to "you", "euch" to "you", "uns" to "us", "mein" to "my", "meine" to "my",
    "dein" to "your", "deine" to "your", "unser" to "our", "unsere" to "our",
    "der" to "the", "die" to "the", "das" to "the", "den" to "the", "dem" to "the",
    "ein" to "a", "eine" to "a", "einen" to "a", "einem" to "a", "einer" to "a",
    "und" to "and", "oder" to "or", "aber" to "but", "wenn" to "if", "dass" to "that",
    "für" to "for", "mit" to "with", "ohne" to "without", "von" to "from",
    "zu" to "to", "im" to "in", "in" to "in", "auf" to "on", "vor" to "before",
    "nach" to "after", "über" to "about", "auch" to "also", "noch" to "still",
    "nicht" to "not", "nur" to "only", "mehr" to "more", "alle" to "all",
    "allem" to "everything", "wie" to "how", "was" to "what", "wer" to "who",
    "wann" to "when", "warum" to "why", "kann" to "can", "können" to "can",
    "werden" to "will", "wird" to "will", "habe" to "have", "haben" to "have",
    "hat" to "has", "sein" to "be", "sind" to "are", "ist" to "is",
    "finde" to "find", "finden" to "find", "geben" to "give", "gibt" to "gives",
    "machen" to "make", "machen" to "make", "gehen" to "go", "kommen" to "come",
    "sprechen" to "talk", "reden" to "talk", "besprechen" to "discuss",
    "teilen" to "share", "lieben" to "love", "liebe" to "love", "mag" to "like",
    "möchte" to "would like", "möchtest" to "would you like", "brauchen" to "need",
    "genug" to "enough", "zeit" to "time", "kosten" to "costs", "kind" to "child",
    "kinder" to "children", "familie" to "family", "beziehung" to "relationship",
    "partner" to "partner", "baby" to "baby", "haus" to "house", "tag" to "day",
    "tage" to "days", "frage" to "question", "fragen" to "questions",
    "antwort" to "answer", "antworten" to "answers", "gemeinsam" to "together",
    "regelmäßig" to "regular", "neu" to "new", "alt" to "old", "gut" to "good",
    "schön" to "beautiful", "wichtig" to "important", "bereit" to "ready",
    "möglich" to "possible", "besser" to "better", "lieber" to "prefer",
    "erste" to "first", "letzte" to "last", "jeden" to "every", "jeder" to "every",
    "etwas" to "something", "nichts" to "nothing", "alles" to "everything",
    "viele" to "many", "einem" to "a", "einer" to "a",
    "du" to "you", "ich" to "I", "mich" to "me", "dich" to "you", "dir" to "you",
    "meinem" to "my", "meines" to "my", "deinem" to "your", "deinem" to "your",
    "unserem" to "our", "unseres" to "our", "partners" to "partner's",
    "am" to "at the", "an" to "at", "bei" to "with", "aus" to "from", "um" to "around",
    "als" to "than", "es" to "it", "sie" to "she", "sich" to "themselves",
    "ja" to "yes", "nie" to "never", "noch" to "still", "vielleicht" to "maybe",
    "würde" to "would", "würdest" to "would you", "wäre" to "would be",
    "soll" to "should", "sollte" to "should", "wollen" to "want", "gerne" to "gladly",
    "magst" to "do you like", "fühlst" to "feel", "hältst" to "think",
    "meisten" to "most", "beste" to "best", "besten" to "best", "wichtigste" to "most important",
    "neue" to "new", "große" to "big", "großer" to "big", "gemeinsame" to "shared",
    "gemeinsamen" to "shared", "viel" to "much", "lange" to "long", "sofort" to "immediately",
    "zwei" to "two", "tägliche" to "daily", "privat" to "private", "beide" to "both",
    "keine" to "no", "welches" to "which", "welcher" to "which", "eines" to "a",
    "einer" to "a", "einem" to "a", "wichtig" to "important", "sehr" to "very",
    "etwas" to "something", "mehr" to "more", "nur" to "only", "tun" to "do",
    "machen" to "make", "macht" to "makes", "führen" to "have", "bleiben" to "stay",
    "verbringen" to "spend", "gehört" to "belongs", "hält" to "holds", "nötig" to "necessary",
    "überhaupt" to "at all", "besprechen" to "discuss", "anschaffung" to "purchase",
    "antrag" to "proposal", "küche" to "kitchen", "garten" to "garden", "hause" to "home",
    "freunden" to "friends", "freunde" to "friends", "freund" to "friend",
    "ort" to "place", "stadt" to "city", "land" to "country", "reise" to "trip",
    "reisen" to "travel", "draußen" to "outside", "umgebung" to "surroundings",
    "öffentlichen" to "public", "überraschung" to "surprise", "details" to "details",
    "einfach" to "simple", "interessen" to "interests", "hobbys" to "hobbies",
    "gespräche" to "conversations", "tiefe" to "deep", "foto" to "photo",
    "zustimmen" to "agree", "ablehnen" to "disagree", "aufwärmen" to "warm-up",
    "zuhause" to "at home", "ausschlafen" to "sleep in", "spontan" to "spontaneous",
    "natur" to "nature", "klassisch" to "classic", "kulturelle" to "cultural",
    "erkunden" to "explore", "traumhaus" to "dream home", "gemütliche" to "cozy",
    "aktivitäten" to "activities", "solitär" to "solitaire", "aufstehen" to "get up",
    "abend" to "evening", "zeichnen" to "draw", "zeichne" to "draw", "spaß" to "fun",
    "tier" to "pet", "familie" to "family", "beziehung" to "relationship",
    "lieber" to "rather", "teile" to "share",
    "aktivität" to "activity", "alltägliche" to "everyday", "anklänge" to "echoes",
    "atmosphäre" to "atmosphere", "auffälligste" to "most noticeable", "aufräumen" to "tidy up",
    "außenbereich" to "outdoor area", "außenkamin" to "outdoor fireplace", "außenpool" to "outdoor pool",
    "außer" to "except", "außerhalb" to "outside", "berühmte" to "famous", "berührung" to "touch",
    "berührungen" to "touches", "beschäftigt" to "busy", "brücke" to "bridge",
    "eingeführt" to "introduced", "enttäuschung" to "disappointment", "erzählen" to "tell",
    "früh" to "early", "fußbodenheizung" to "underfloor heating", "fällt" to "falls",
    "gefällt" to "likes", "gefühl" to "feeling", "gegenüber" to "compared with", "gehören" to "belong",
    "gelöscht" to "deleted", "gemüsebeet" to "vegetable garden", "gemütlich" to "cozy",
    "gemütlicher" to "cozier", "gemütliches" to "cozy", "gemütlichkeit" to "coziness",
    "genieße" to "enjoy", "gespräch" to "conversation", "gesprächsanreger" to "conversation starter",
    "gewächshaus" to "greenhouse", "gleichgültigkeit" to "indifference", "glücklich" to "happy",
    "groß" to "big", "großartig" to "great", "größere" to "larger", "größte" to "greatest",
    "hochzeitssträuße" to "wedding bouquets", "hängematte" to "hammock", "hängt" to "hangs",
    "hättest" to "would have", "intimität" to "intimacy", "kräftige" to "bold", "kräuterbeet" to "herb garden",
    "körperlich" to "physical", "körperliche" to "physical", "köstlich" to "delicious", "küchen" to "kitchens",
    "kümmert" to "cares", "lügen" to "lies", "missverständnisse" to "misunderstandings", "mädchen" to "girl",
    "müde" to "tired", "mündlich" to "spoken", "nächste" to "next", "nächsten" to "next", "nächte" to "nights",
    "nähe" to "closeness", "persönlich" to "personal", "pläne" to "plans", "regelmäßige" to "regular",
    "schnappschüsse" to "snapshots", "schönen" to "beautiful", "schönster" to "most beautiful",
    "sehenswürdigkeit" to "sight", "spaziergänge" to "walks", "spät" to "late", "später" to "later",
    "ständig" to "constantly", "stühle" to "chairs", "süß" to "cute", "tiefgründige" to "deep",
    "tänzer" to "dancer", "tür" to "door", "ungestört" to "undisturbed",
    "universitätssportmannschaft" to "university sports team", "vergnügungspark" to "amusement park",
    "weiße" to "white", "weißgold" to "white gold", "welche" to "which", "wofür" to "what for",
    "wohnküche" to "open-plan kitchen", "während" to "while", "wünsche" to "wishes",
    "wünschen" to "wish", "zufälliger" to "random", "ändern" to "change", "öfter" to "more often",
    "übe" to "practice", "übernimmt" to "takes over", "überraschend" to "surprising",
    "überschneiden" to "overlap", "übertrieben" to "exaggerated", "üppig" to "lush"
)

private val ENGLISH_CONTENT = mapOf(
    "Fragen & Spiele" to "Questions & Games",
    "unterhaltung" to "Entertainment",
    "dasoderdas" to "This or That",
    "hochzeit" to "Wedding",
    "kinder" to "Children",
    "reden" to "Discussion",
    "reisen" to "Travel",
    "familie" to "Family",
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
    "Diskutiert eure Antworten" to "Discuss your answers",
    "ERGEBNISSE" to "RESULTS",
    "BEANTWORTE" to "ANSWER",
    "Alle Pakete" to "All packs",
    "Fertig" to "Done",
    "Abbrechen" to "Cancel",
    "Speichern" to "Save",
    "Hinzufügen" to "Add",
    "Momente" to "Moments",
    "Titel" to "Title",
    "Was ist passiert?" to "What happened?",
    "Schließen" to "Close",
    "Zurück" to "Back",
    "Quiz verlassen?" to "Leave quiz?",
    "Weiter spielen" to "Keep playing",
    "Übernehmen" to "Save",
    "Deine eigene Antwort" to "Your own answer",
    "Deine Antwort..." to "Your answer...",
    "Frage hinzufügen" to "Add question",
    "Paar" to "Pair",
    "Galerie" to "Gallery",
    "Testen" to "Test",
    "Spiel bearbeiten" to "Edit game",
    "Neues Spiel" to "New game"
)
