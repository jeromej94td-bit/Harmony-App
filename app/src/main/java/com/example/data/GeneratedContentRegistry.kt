package com.example.data

/**
 * Vereinigt den bisherigen generierten Harmony-Content mit zusätzlichen
 * Dev-Studio-Imports, ohne GeneratedHarmonyContent.kt destruktiv zu ersetzen.
 *
 * Ältere GeneratedHarmonyContent-Dateien besitzen noch keine ASSETS-Metadaten.
 * Deshalb stammen die Asset-Metadaten ausschließlich aus den neuen additiven
 * Imports; bestehende PACKS/IMAGES bleiben vollständig erhalten. Bereits auf
 * main verwaltete Standard-Assets bleiben bewusst bei ihren aktuellen Installern.
 */
object GeneratedContentRegistry {
    val VERSION: Long =
        (GeneratedHarmonyContent.VERSION * 31L) xor GeneratedHarmonyNewPicGame.VERSION

    val CATEGORIES: List<GenCategory> by lazy {
        (GeneratedHarmonyContent.CATEGORIES + GeneratedHarmonyNewPicGame.CATEGORIES)
            .distinctBy { it.id }
    }

    val PACKS: List<GenPack> by lazy {
        val byId = LinkedHashMap<String, GenPack>()
        GeneratedHarmonyContent.PACKS.forEach { byId[it.id] = it }
        GeneratedHarmonyNewPicGame.PACKS.forEach { byId[it.id] = it }
        byId.values.toList()
    }

    val LINK_PACKS: List<GenLinkPack> by lazy {
        (GeneratedHarmonyContent.LINK_PACKS + GeneratedHarmonyNewPicGame.LINK_PACKS)
            .distinctBy { it.id }
    }

    val ASSETS: List<GenAssetMeta> by lazy {
        GeneratedHarmonyNewPicGame.ASSETS.distinctBy { it.optionKey }
    }

    val IMAGES: Map<String, String> by lazy {
        LinkedHashMap<String, String>().apply {
            putAll(GeneratedHarmonyContent.IMAGES)
            putAll(GeneratedHarmonyNewPicGame.IMAGES)
        }
    }
}
