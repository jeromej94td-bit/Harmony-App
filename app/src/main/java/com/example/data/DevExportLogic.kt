package com.example.data

/**
 * Android-unabhängige Logik für den Dev-Studio-Export.
 * Sie hält Reihenfolge und Asset-Zuordnung deterministisch und ist separat prüfbar.
 */
data class ExportPackRef(
    val id: String,
    val title: String,
    val pairs: List<Pair<String, String>>
)

data class ExportAssetAssignment(
    val optionKey: String,
    val originalFileName: String,
    val packId: String,
    val packTitle: String,
    val pairIndex: Int,
    /** 0 = linke/A-Seite, 1 = rechte/B-Seite. */
    val side: Int
)

object DevExportLogic {

    fun reconcileOrder(storedOrder: List<String>, availableIds: List<String>): List<String> {
        val available = availableIds.toSet()
        val result = LinkedHashSet<String>()
        storedOrder.forEach { if (it in available) result.add(it) }
        availableIds.forEach { result.add(it) }
        return result.toList()
    }

    fun move(order: List<String>, id: String, delta: Int): List<String> {
        if (delta == 0) return order.toList()
        val current = order.indexOf(id)
        if (current < 0) return order.toList()
        val target = (current + delta).coerceIn(0, order.lastIndex)
        if (target == current) return order.toList()
        val copy = order.toMutableList()
        val item = copy.removeAt(current)
        copy.add(target, item)
        return copy
    }

    fun assignAssets(
        packs: List<ExportPackRef>,
        originalFileNames: Map<String, String>
    ): List<ExportAssetAssignment> {
        val result = mutableListOf<ExportAssetAssignment>()
        val seen = mutableSetOf<String>()
        for (pack in packs) {
            pack.pairs.forEachIndexed { pairIndex, pair ->
                for ((optionKey, side) in listOf(pair.first to 0, pair.second to 1)) {
                    val original = originalFileNames[optionKey] ?: continue
                    if (seen.add(optionKey)) {
                        result += ExportAssetAssignment(
                            optionKey = optionKey,
                            originalFileName = safeBaseName(original),
                            packId = pack.id,
                            packTitle = pack.title,
                            pairIndex = pairIndex,
                            side = side
                        )
                    }
                }
            }
        }
        return result
    }

    /** Paar/Seite liegen im Pfad, der Original-Dateiname selbst bleibt unverändert. */
    fun zipPaths(assets: List<ExportAssetAssignment>): Map<ExportAssetAssignment, String> {
        val result = LinkedHashMap<ExportAssetAssignment, String>()
        assets.forEach { asset ->
            val pack = safeSegment(asset.packId.ifBlank { "spiel" })
            val pair = "pair-${(asset.pairIndex + 1).toString().padStart(3, '0')}"
            val side = if (asset.side == 0) "a" else "b"
            result[asset] = "images/$pack/$pair/$side/${safeBaseName(asset.originalFileName)}"
        }
        return result
    }

    fun buildManifestJson(
        packs: List<ExportPackRef>,
        assets: List<ExportAssetAssignment>
    ): String {
        val paths = zipPaths(assets)
        return buildString {
            append("{\n")
            append("  \"format\": \"harmony-ai-studio-export\",\n")
            append("  \"version\": 2,\n")
            append("  \"packOrder\": [")
            packs.forEachIndexed { index, pack ->
                if (index > 0) append(", ")
                append(json(pack.id))
            }
            append("],\n")
            append("  \"packs\": [\n")
            packs.forEachIndexed { index, pack ->
                append("    {\"id\": ").append(json(pack.id))
                    .append(", \"title\": ").append(json(pack.title)).append("}")
                if (index != packs.lastIndex) append(',')
                append('\n')
            }
            append("  ],\n")
            append("  \"assets\": [\n")
            assets.forEachIndexed { index, asset ->
                append("    {\"optionKey\": ").append(json(asset.optionKey))
                    .append(", \"originalFileName\": ").append(json(asset.originalFileName))
                    .append(", \"packId\": ").append(json(asset.packId))
                    .append(", \"packTitle\": ").append(json(asset.packTitle))
                    .append(", \"pairIndex\": ").append(asset.pairIndex)
                    .append(", \"side\": ").append(asset.side)
                    .append(", \"zipPath\": ").append(json(paths.getValue(asset)))
                    .append('}')
                if (index != assets.lastIndex) append(',')
                append('\n')
            }
            append("  ]\n")
            append('}')
        }
    }

    /**
     * Liest die ORDER-Liste aus einer von DevExporter erzeugten Kotlin-Datei.
     * So kann der Share-Schritt exakt die vom Nutzer ausgewählten Spiele in das
     * zusätzliche ZIP übernehmen, ohne sich auf globale App-Reihenfolge zu verlassen.
     */
    fun extractOrderIds(generatedSource: String): List<String> {
        val orderStart = generatedSource.indexOf("val ORDER")
        if (orderStart < 0) return emptyList()
        val listStart = generatedSource.indexOf("listOf(", orderStart)
        if (listStart < 0) return emptyList()
        val close = generatedSource.indexOf(')', listStart)
        if (close < 0) return emptyList()
        val body = generatedSource.substring(listStart + "listOf(".length, close)
        return Regex("\"((?:\\\\.|[^\"\\\\])*)\"")
            .findAll(body)
            .map { unescapeKotlinString(it.groupValues[1]) }
            .toList()
    }

    fun safeBaseName(raw: String): String {
        val clean = raw.replace('\\', '/').substringAfterLast('/').trim()
        return clean.ifBlank { "image.jpg" }.replace("/", "_")
    }

    private fun safeSegment(raw: String): String {
        val out = buildString {
            raw.trim().forEach { c ->
                when {
                    c.isLetterOrDigit() || c == '-' || c == '_' -> append(c)
                    else -> append('_')
                }
            }
        }.trim('_')
        return out.ifBlank { "spiel" }
    }

    private fun unescapeKotlinString(raw: String): String {
        val out = StringBuilder()
        var i = 0
        while (i < raw.length) {
            if (raw[i] == '\\' && i + 1 < raw.length) {
                when (val next = raw[i + 1]) {
                    '\\' -> out.append('\\')
                    '"' -> out.append('"')
                    'n' -> out.append('\n')
                    'r' -> out.append('\r')
                    't' -> out.append('\t')
                    '$' -> out.append('$')
                    else -> out.append(next)
                }
                i += 2
            } else {
                out.append(raw[i])
                i++
            }
        }
        return out.toString()
    }

    private fun json(raw: String): String = buildString {
        append('"')
        raw.forEach { c ->
            when (c) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(c)
            }
        }
        append('"')
    }
}
