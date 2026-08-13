package com.example.ui

import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream
import kotlin.text.Charsets.UTF_8

/** Complete Norwegian Bokmål catalog aligned to the current Harmony source catalog. */
internal val EXACT_NORWEGIAN_CONTENT: Map<String, String> by lazy {
    val compressed = decodeNorwegianBase64(NORWEGIAN_CONTENT_DATA)
    val payload = GZIPInputStream(ByteArrayInputStream(compressed)).readBytes().toString(UTF_8)
    payload.lineSequence()
        .filter(String::isNotBlank)
        .associate { line ->
            val separator = line.indexOf('\t')
            decodeNorwegianCatalogToken(line.substring(0, separator)) to
                decodeNorwegianCatalogToken(line.substring(separator + 1))
        }
}

private fun decodeNorwegianCatalogToken(value: String): String = buildString {
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

private fun decodeNorwegianBase64(value: String): ByteArray {
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

private const val NORWEGIAN_CONTENT_DATA = """
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
nwjLzFy1AAA=
"""

private fun exactNorwegian(text: String): String =
    TranslationCatalog.exact(text, AppLanguage.NORWEGIAN) ?: text

internal fun localizeNorwegianDynamicContent(text: String): String? {
    Regex("^([\\p{So}\\p{Sk}\\uFE0F\\u200D]+\\s+)(.+)$").matchEntire(text)?.let {
        TranslationCatalog.exact(it.groupValues[2], AppLanguage.NORWEGIAN)?.let { translated -> return it.groupValues[1] + translated }
    }
    Regex("^(\\d+[.)]\\s+)(.+)$").matchEntire(text)?.let {
        TranslationCatalog.exact(it.groupValues[2], AppLanguage.NORWEGIAN)?.let { translated -> return it.groupValues[1] + translated }
    }
    Regex("^(\\d+) Bilder geladen — Namen prüfen, dann erstellen\\.$").matchEntire(text)?.let { return "${it.groupValues[1]} bilder lastet inn — sjekk navnene, og opprett deretter." }
    Regex("^(\\d+) Bilder geladen\\.$").matchEntire(text)?.let { return "${it.groupValues[1]} bilder lastet inn." }
    Regex("^(\\d+) Paare aus (\\d+) Bildern$").matchEntire(text)?.let { return "${it.groupValues[1]} par fra ${it.groupValues[2]} bilder" }
    Regex("^Bild (\\d+) von (\\d+)…$").matchEntire(text)?.let { return "Bilde ${it.groupValues[1]} av ${it.groupValues[2]}…" }
    Regex("^Speichere Bild (\\d+) von (\\d+)…$").matchEntire(text)?.let { return "Lagrer bilde ${it.groupValues[1]} av ${it.groupValues[2]}…" }
    Regex("^Paar (\\d+)$").matchEntire(text)?.let { return "Par ${it.groupValues[1]}" }
    Regex("^Frage (\\d+)$").matchEntire(text)?.let { return "Spørsmål ${it.groupValues[1]}" }
    Regex("^Schritt (\\d+)$").matchEntire(text)?.let { return "Trinn ${it.groupValues[1]}" }
    Regex("^(\\d+) Paare$").matchEntire(text)?.let { return "${it.groupValues[1]} par" }
    Regex("^(\\d+) Fragen$").matchEntire(text)?.let { return "${it.groupValues[1]} spørsmål" }
    Regex("^(\\d+) Schritt\\(e\\)$").matchEntire(text)?.let { return "${it.groupValues[1]} trinn" }
    Regex("^(\\d+) Paare · (\\d+) Fragen$").matchEntire(text)?.let { return "${it.groupValues[1]} par · ${it.groupValues[2]} spørsmål" }
    Regex("^(\\d+) Einträge$").matchEntire(text)?.let { return "${it.groupValues[1]} oppføringer" }
    Regex("^Fertig: (\\d+) Pakete · (\\d+) Bilder · (.+)$").matchEntire(text)?.let { return "Ferdig: ${it.groupValues[1]} pakker · ${it.groupValues[2]} bilder · ${it.groupValues[3]}" }
    Regex("^🎉 (\\d+) Pakete/Ketten & Bilder erfolgreich eingespielt!$").matchEntire(text)?.let { return "🎉 ${it.groupValues[1]} pakker/kjeder og bilder ble importert!" }
    Regex("^🎉 '(.+)' angelegt · (\\d+) Paare spielbereit$").matchEntire(text)?.let { return "🎉 '${exactNorwegian(it.groupValues[1])}' opprettet · ${it.groupValues[2]} par klare til spill" }
    Regex("^• ([AB]): (.+) \\(Bild: (.+)\\)$").matchEntire(text)?.let { return "• ${it.groupValues[1]}: ${exactNorwegian(it.groupValues[2])} (Bilde: ${it.groupValues[3]})" }
    Regex("^([AB]): (.+)$").matchEntire(text)?.let { return "${it.groupValues[1]}: ${exactNorwegian(it.groupValues[2])}" }
    Regex("^'(.+)' gelöscht\\.$").matchEntire(text)?.let { return "'${exactNorwegian(it.groupValues[1])}' slettet." }
    Regex("^'(.+)' gespeichert\\.$").matchEntire(text)?.let { return "'${exactNorwegian(it.groupValues[1])}' lagret." }
    Regex("^Kategorie '(.+)' gespeichert\\.$").matchEntire(text)?.let { return "Kategori '${exactNorwegian(it.groupValues[1])}' lagret." }
    Regex("^Kette '(.+)' gespeichert\\.$").matchEntire(text)?.let { return "Kjede '${exactNorwegian(it.groupValues[1])}' lagret." }
    Regex("^Kette '(.+)' gelöscht\\.$").matchEntire(text)?.let { return "Kjede '${exactNorwegian(it.groupValues[1])}' slettet." }
    Regex("^Bild für '(.+)' gesetzt\\.$").matchEntire(text)?.let { return "Bilde angitt for '${exactNorwegian(it.groupValues[1])}'." }
    Regex("^Eigenes Bild für '(.+)' entfernt\\.$").matchEntire(text)?.let { return "Eget bilde fjernet for '${exactNorwegian(it.groupValues[1])}'." }
    Regex("^(.+) „(.+)\" an (.+) gesendet$").matchEntire(text)?.let { return "${it.groupValues[1]} «${exactNorwegian(it.groupValues[2])}» sendt til ${it.groupValues[3]}" }
    Regex("^6 Monate im (.+)$").matchEntire(text)?.let { return "6 måneder i ${exactNorwegian(it.groupValues[1])}" }
    Regex("^1 Jahr lang in (.+)$").matchEntire(text)?.let { return "1 år i ${exactNorwegian(it.groupValues[1])}" }
    Regex("^Weil du (.+) gewählt hast …$").matchEntire(text)?.let { return "Fordi du valgte ${exactNorwegian(it.groupValues[1])}…" }
    Regex("^Verbinde dich mit (.+), um die Antwort zu sehen$").matchEntire(text)?.let { return "Koble til ${it.groupValues[1]} for å se svaret" }
    Regex("^Verbinde dich mit (.+)$").matchEntire(text)?.let { return "Koble til ${it.groupValues[1]}" }
    Regex("^Deine Antworten sind gespeichert\\. Sobald (.+) das Paket beendet, werden beide Antworten gemeinsam sichtbar\\.$").matchEntire(text)?.let { return "Svarene dine er lagret. Når ${it.groupValues[1]} fullfører pakken, blir begge svarene synlige sammen." }
    Regex("^Fehler bei der Umformulierung: (.+)$").matchEntire(text)?.let { return "Feil ved omformulering: ${it.groupValues[1]}" }
    Regex("^Fehler bei der Analyse: (.+)$").matchEntire(text)?.let { return "Feil ved analysen: ${it.groupValues[1]}" }
    Regex("^Fehler bei der Ideengenerierung: (.+)$").matchEntire(text)?.let { return "Feil ved idégenerering: ${it.groupValues[1]}" }
    if (text.contains(" · ")) {
        val translated = text.split(" · ").joinToString(" · ") { TranslationCatalog.translate(it, AppLanguage.NORWEGIAN) ?: it }
        if (translated != text) return translated
    }
    return null
}
