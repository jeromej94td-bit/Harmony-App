package com.example.data

/**
 * Vereinigt den bisherigen generierten Harmony-Content mit zusätzlichen
 * Dev-Studio-Imports, ohne GeneratedHarmonyContent.kt destruktiv zu ersetzen.
 */
object GeneratedContentRegistry {
    val VERSION: Long = (GeneratedHarmonyContent.VERSION * 31L) xor GeneratedHarmonyNewPicGame.VERSION

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
        val byKey = LinkedHashMap<String, GenAssetMeta>()
        GeneratedHarmonyContent.ASSETS.forEach { byKey[it.optionKey] = it }
        GeneratedHarmonyNewPicGame.ASSETS.forEach { byKey[it.optionKey] = it }
        byKey.values.toList()
    }

    val IMAGES: Map<String, String> by lazy {
        LinkedHashMap<String, String>().apply {
            putAll(GeneratedHarmonyContent.IMAGES)
            putAll(GeneratedHarmonyNewPicGame.IMAGES)
        }
    }
}
