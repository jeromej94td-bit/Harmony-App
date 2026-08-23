package com.example.ui

import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream
import kotlin.text.Charsets.UTF_8

/** Machine-translated Danish catalog aligned to the complete Harmony source catalog. */
internal val EXACT_DANISH_CONTENT: Map<String, String> by lazy {
    val compressed = decodeDanishBase64(DANISH_CONTENT_DATA)
    val payload = GZIPInputStream(ByteArrayInputStream(compressed)).readBytes().toString(UTF_8)
    payload.lineSequence()
        .filter(String::isNotBlank)
        .associate { line ->
            val separator = line.indexOf('\t')
            decodeDanishCatalogToken(line.substring(0, separator)) to
                decodeDanishCatalogToken(line.substring(separator + 1))
        }
}

private fun decodeDanishCatalogToken(value: String): String = buildString {
    var index = 0
    while (index < value.length) {
        val char = value[index]
        if (char == '\\' && index + 1 < value.length) {
            when (val next = value[index + 1]) {
                'n' -> append('\n')
                't' -> append('\t')
                else -> append(next)
            }
            index += 2
        } else {
            append(char)
            index += 1
        }
    }
}

private fun decodeDanishBase64(value: String): ByteArray {
    val clean = value.filterNot { it.isWhitespace() }
    val table = IntArray(128) { -1 }
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".forEachIndexed { index, char -> table[char.code] = index }
    val out = ArrayList<Byte>(clean.length * 3 / 4)
    var buffer = 0
    var bits = 0
    clean.forEach { char ->
        if (char != '=') {
            buffer = (buffer shl 6) or table[char.code]
            bits += 6
            if (bits >= 8) {
                bits -= 8
                out.add(((buffer shr bits) and 0xFF).toByte())
            }
        }
    }
    return out.toByteArray()
}

private const val DANISH_CONTENT_DATA = """
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
"""

private fun exactDanish(text: String): String =
    TranslationCatalog.exact(text, AppLanguage.DANISH) ?: text

internal fun localizeDanishDynamicContent(text: String): String? {
    Regex("^([\\p{So}\\p{Sk}\\uFE0F\\u200D]+\\s+)(.+)$").matchEntire(text)?.let {
        TranslationCatalog.exact(it.groupValues[2], AppLanguage.DANISH)?.let { translated -> return it.groupValues[1] + translated }
    }
    Regex("^(\\d+[.)]\\s+)(.+)$").matchEntire(text)?.let {
        TranslationCatalog.exact(it.groupValues[2], AppLanguage.DANISH)?.let { translated -> return it.groupValues[1] + translated }
    }
    Regex("^(\\d+) Bilder geladen — Namen prüfen, dann erstellen\\.$").matchEntire(text)?.let { return "${it.groupValues[1]} billeder indlæst — tjek navnene, og opret derefter." }
    Regex("^(\\d+) Bilder geladen\\.$").matchEntire(text)?.let { return "${it.groupValues[1]} billeder indlæst." }
    Regex("^(\\d+) Paare aus (\\d+) Bildern$").matchEntire(text)?.let { return "${it.groupValues[1]} par fra ${it.groupValues[2]} billeder" }
    Regex("^Bild (\\d+) von (\\d+)…$").matchEntire(text)?.let { return "Billede ${it.groupValues[1]} af ${it.groupValues[2]}…" }
    Regex("^Speichere Bild (\\d+) von (\\d+)…$").matchEntire(text)?.let { return "Gemmer billede ${it.groupValues[1]} af ${it.groupValues[2]}…" }
    Regex("^Paar (\\d+)$").matchEntire(text)?.let { return "Par ${it.groupValues[1]}" }
    Regex("^Frage (\\d+)$").matchEntire(text)?.let { return "Spørgsmål ${it.groupValues[1]}" }
    Regex("^Schritt (\\d+)$").matchEntire(text)?.let { return "Trin ${it.groupValues[1]}" }
    Regex("^(\\d+) Paare$").matchEntire(text)?.let { return "${it.groupValues[1]} par" }
    Regex("^(\\d+) Fragen$").matchEntire(text)?.let { return "${it.groupValues[1]} spørgsmål" }
    Regex("^(\\d+) Schritt\\(e\\)$").matchEntire(text)?.let { return "${it.groupValues[1]} trin" }
    Regex("^(\\d+) Paare · (\\d+) Fragen$").matchEntire(text)?.let { return "${it.groupValues[1]} par · ${it.groupValues[2]} spørgsmål" }
    Regex("^(\\d+) Einträge$").matchEntire(text)?.let { return "${it.groupValues[1]} poster" }
    Regex("^Fertig: (\\d+) Pakete · (\\d+) Bilder · (.+)$").matchEntire(text)?.let { return "Færdig: ${it.groupValues[1]} pakker · ${it.groupValues[2]} billeder · ${it.groupValues[3]}" }
    Regex("^🎉 (\\d+) Pakete/Ketten & Bilder erfolgreich eingespielt!$").matchEntire(text)?.let { return "🎉 ${it.groupValues[1]} pakker/kæder og billeder blev importeret!" }
    Regex("^🎉 '(.+)' angelegt · (\\d+) Paare spielbereit$").matchEntire(text)?.let { return "🎉 '${exactDanish(it.groupValues[1])}' oprettet · ${it.groupValues[2]} par klar til spil" }
    Regex("^• ([AB]): (.+) \\(Bild: (.+)\\)$").matchEntire(text)?.let { return "• ${it.groupValues[1]}: ${exactDanish(it.groupValues[2])} (Billede: ${it.groupValues[3]})" }
    Regex("^([AB]): (.+)$").matchEntire(text)?.let { return "${it.groupValues[1]}: ${exactDanish(it.groupValues[2])}" }
    Regex("^'(.+)' gelöscht\\.$").matchEntire(text)?.let { return "'${exactDanish(it.groupValues[1])}' slettet." }
    Regex("^'(.+)' gespeichert\\.$").matchEntire(text)?.let { return "'${exactDanish(it.groupValues[1])}' gemt." }
    Regex("^Kategorie '(.+)' gespeichert\\.$").matchEntire(text)?.let { return "Kategori '${exactDanish(it.groupValues[1])}' gemt." }
    Regex("^Kette '(.+)' gespeichert\\.$").matchEntire(text)?.let { return "Kæde '${exactDanish(it.groupValues[1])}' gemt." }
    Regex("^Kette '(.+)' gelöscht\\.$").matchEntire(text)?.let { return "Kæde '${exactDanish(it.groupValues[1])}' slettet." }
    Regex("^Bild für '(.+)' gesetzt\\.$").matchEntire(text)?.let { return "Billede sat for '${exactDanish(it.groupValues[1])}'." }
    Regex("^Eigenes Bild für '(.+)' entfernt\\.$").matchEntire(text)?.let { return "Eget billede fjernet for '${exactDanish(it.groupValues[1])}'." }
    Regex("^(.+) „(.+)\" an (.+) gesendet$").matchEntire(text)?.let { return "${it.groupValues[1]} „${exactDanish(it.groupValues[2])}“ sendt til ${it.groupValues[3]}" }
    Regex("^6 Monate im (.+)$").matchEntire(text)?.let { return "6 måneder i ${exactDanish(it.groupValues[1])}" }
    Regex("^1 Jahr lang in (.+)$").matchEntire(text)?.let { return "1 år i ${exactDanish(it.groupValues[1])}" }
    Regex("^Weil du (.+) gewählt hast …$").matchEntire(text)?.let { return "Fordi du valgte ${exactDanish(it.groupValues[1])}…" }
    Regex("^Verbinde dich mit (.+), um die Antwort zu sehen$").matchEntire(text)?.let { return "Forbind med ${it.groupValues[1]} for at se svaret" }
    Regex("^Verbinde dich mit (.+)$").matchEntire(text)?.let { return "Forbind med ${it.groupValues[1]}" }
    Regex("^Deine Antworten sind gespeichert\\. Sobald (.+) das Paket beendet, werden beide Antworten gemeinsam sichtbar\\.$").matchEntire(text)?.let { return "Dine svar er gemt. Når ${it.groupValues[1]} afslutter pakken, bliver begge svar synlige sammen." }
    Regex("^Fehler bei der Umformulierung: (.+)$").matchEntire(text)?.let { return "Fejl ved omformulering: ${it.groupValues[1]}" }
    Regex("^Fehler bei der Analyse: (.+)$").matchEntire(text)?.let { return "Fejl ved analysen: ${it.groupValues[1]}" }
    Regex("^Fehler bei der Ideengenerierung: (.+)$").matchEntire(text)?.let { return "Fejl ved idégenerering: ${it.groupValues[1]}" }
    if (text.contains(" · ")) {
        val translated = text.split(" · ").joinToString(" · ") { TranslationCatalog.translate(it, AppLanguage.DANISH) ?: it }
        if (translated != text) return translated
    }
    return null
}
