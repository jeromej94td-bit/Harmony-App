package com.example.data

/**
 * Additiver Dev-Studio-Import aus Google Drive: Harmony new pic game.
 * Die verbindliche Zuordnung liegt unter devstudio_imports/custom_logo/harmony-export-manifest.json.
 */
object GeneratedHarmonyNewPicGame {
    const val VERSION: Long = 1787325177486L

    val ORDER: List<String> = listOf("custom_logo")

    val ASSETS: List<GenAssetMeta> = listOf(
        GenAssetMeta(optionKey = "img_pack_1787325043190_0", originalFileName = "1000110101.png", packId = "custom_logo", pairIndex = 0, side = 0),
        GenAssetMeta(optionKey = "img_pack_1787325043190_1", originalFileName = "1000110102.png", packId = "custom_logo", pairIndex = 0, side = 1),
        GenAssetMeta(optionKey = "img_pack_1787325043190_2", originalFileName = "1000110103.png", packId = "custom_logo", pairIndex = 1, side = 0),
        GenAssetMeta(optionKey = "img_pack_1787325043190_3", originalFileName = "1000110104.png", packId = "custom_logo", pairIndex = 1, side = 1),
        GenAssetMeta(optionKey = "img_1787325068077_a", originalFileName = "1000110105.png", packId = "custom_logo", pairIndex = 2, side = 0),
        GenAssetMeta(optionKey = "img_pack_1787325043190_5", originalFileName = "1000110111.jpg", packId = "custom_logo", pairIndex = 2, side = 1)
    )

    val CATEGORIES: List<GenCategory> = emptyList()

    val PACKS: List<GenPack> = listOf(
        GenPack(
            id = "custom_logo",
            title = "Logo ",
            cat = "tot",
            topic = "reisen",
            type = "tot",
            tags = listOf("dasoderdas", "unterhaltung"),
            emoji = "🖌",
            pairs = listOf(
                "img_pack_1787325043190_0" to "img_pack_1787325043190_1",
                "img_pack_1787325043190_2" to "img_pack_1787325043190_3",
                "img_1787325068077_a" to "img_pack_1787325043190_5"
            ),
            questions = emptyList()
        )
    )

    val LINK_PACKS: List<GenLinkPack> = emptyList()

    val IMAGES: Map<String, String> by lazy {
        mapOf(
            "img_pack_1787325043190_0" to newPicImage0(),
            "img_pack_1787325043190_1" to newPicImage1(),
            "img_pack_1787325043190_2" to newPicImage2(),
            "img_pack_1787325043190_3" to newPicImage3(),
            "img_1787325068077_a" to newPicImage4(),
            "img_pack_1787325043190_5" to newPicImage5()
        )
    }
}
