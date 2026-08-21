package com.example.data

/**
 * Typen für Inhalte, die vom Harmony Dev Studio als Kotlin-Code exportiert wurden.
 *
 * Der Export kann als einzelne GeneratedHarmonyContent.kt oder zusammen mit
 * Manifest und Original-Bilddateien als AI-Studio-ZIP ausgegeben werden.
 * Alle neuen Felder besitzen Defaults, damit ältere generierte Dateien weiter
 * mit dem aktuellen App-Code kompatibel bleiben.
 */

data class GenCategory(
    val id: String,
    val name: String,
    val emoji: String,
    val color: Long = 0xFFFFC46B
)

data class GenQuestion(
    val q: String,
    val options: List<String> = emptyList(),
    val defaultMine: String? = null
)

data class GenPack(
    val id: String,
    val title: String,
    val cat: String,
    val topic: String = "reisen",
    val type: String = "tot",
    val tags: List<String> = listOf("dasoderdas", "unterhaltung"),
    val pairs: List<Pair<String, String>> = emptyList(),
    val questions: List<GenQuestion> = emptyList(),
    val emoji: String = ""
)

/**
 * Metadaten eines Bildes im AI-Studio-Export. `optionKey` bleibt der interne
 * Harmony-Schlüssel, `originalFileName` ist dagegen genau der vom Nutzer
 * ausgewählte Dateiname und darf beim Export nicht umbenannt werden.
 */
data class GenAssetMeta(
    val optionKey: String,
    val originalFileName: String,
    val packId: String,
    val pairIndex: Int,
    val side: Int
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
