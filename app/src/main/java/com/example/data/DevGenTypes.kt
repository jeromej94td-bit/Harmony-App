package com.example.data

/**
 * Typen für Inhalte, die vom Harmony Dev Studio als Kotlin-Code exportiert wurden.
 *
 * Der Export erzeugt genau eine Datei: GeneratedHarmonyContent.kt
 * Diese Datei kann man direkt in Google AI Studio hochladen / einfügen —
 * kein ZIP nötig, weil Bilder als Base64-Text im Code stehen.
 */

data class GenCategory(
    val id: String,
    val name: String,
    val emoji: String,
    val color: Long = 0xFFFFC46B
)

data class GenQuestion(
    val q: String,
    val options: List<String> = emptyList()
)

data class GenPack(
    val id: String,
    val title: String,
    val cat: String,
    val topic: String = "reisen",
    val type: String = "tot",
    val tags: List<String> = listOf("dasoderdas", "unterhaltung"),
    val pairs: List<Pair<String, String>> = emptyList(),
    val questions: List<GenQuestion> = emptyList()
)

data class GenLinkSlot(
    val source: String = "PICKED",
    val packId: String = "",
    val pairIndex: Int = -1,
    val side: Int = 0,
    val text: String = ""
)

data class GenLinkStep(
    val templateA: String = "{}",
    val slotA: GenLinkSlot = GenLinkSlot(),
    val templateB: String = "{}",
    val slotB: GenLinkSlot = GenLinkSlot(source = "OPTION"),
    val caption: String = ""
)

/** Ketten-Paket: speichert Bauanleitungen, keine fertigen Paare. */
data class GenLinkPack(
    val id: String,
    val title: String,
    val cat: String,
    val steps: List<GenLinkStep>
)
