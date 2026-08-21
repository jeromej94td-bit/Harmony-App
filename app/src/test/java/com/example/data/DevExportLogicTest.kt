package com.example.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DevExportLogicTest {

    @Test
    fun `reconcileOrder keeps requested order and appends unknown ids once`() {
        val result = DevExportLogic.reconcileOrder(
            storedOrder = listOf("pack_b", "pack_a", "missing"),
            availableIds = listOf("pack_a", "pack_b", "pack_c")
        )
        assertEquals(listOf("pack_b", "pack_a", "pack_c"), result)
    }

    @Test
    fun `move changes only selected pack position`() {
        assertEquals(
            listOf("a", "c", "b", "d"),
            DevExportLogic.move(listOf("a", "b", "c", "d"), "b", +1)
        )
        assertEquals(
            listOf("b", "a", "c", "d"),
            DevExportLogic.move(listOf("a", "b", "c", "d"), "b", -1)
        )
    }

    @Test
    fun `assignAssets maps option key to pack pair index and side`() {
        val packs = listOf(
            ExportPackRef(
                id = "ice",
                title = "Eis",
                pairs = listOf("Vanille" to "Schoko", "Mango" to "Zitrone")
            )
        )
        val names = mapOf(
            "Vanille" to "01a_Vanille.png",
            "Schoko" to "01b_Schoko.jpg",
            "Zitrone" to "02b_Zitrone.webp"
        )

        val assigned = DevExportLogic.assignAssets(packs, names)

        assertEquals(
            ExportAssetAssignment("Vanille", "01a_Vanille.png", "ice", "Eis", 0, 0),
            assigned.first { it.optionKey == "Vanille" }
        )
        assertEquals(1, assigned.first { it.optionKey == "Zitrone" }.pairIndex)
        assertEquals(1, assigned.first { it.optionKey == "Zitrone" }.side)
    }

    @Test
    fun `zipPaths keep original basename even for duplicates`() {
        val assets = listOf(
            ExportAssetAssignment("A", "same.png", "pack", "Pack", 0, 0),
            ExportAssetAssignment("B", "same.png", "pack", "Pack", 0, 1)
        )

        val paths = DevExportLogic.zipPaths(assets)

        assertEquals("images/pack/pair-001/a/same.png", paths[assets[0]])
        assertEquals("images/pack/pair-001/b/same.png", paths[assets[1]])
        assertTrue(paths.values.all { it.endsWith("/same.png") })
    }

    @Test
    fun `manifest preserves pack order and original filenames`() {
        val packs = listOf(
            ExportPackRef("p2", "Second", emptyList()),
            ExportPackRef("p1", "First", listOf("A" to "B"))
        )
        val assets = DevExportLogic.assignAssets(packs, mapOf("A" to "original A.png"))

        val manifest = DevExportLogic.buildManifestJson(packs, assets)

        assertTrue(manifest.indexOf("\"p2\"") < manifest.indexOf("\"p1\""))
        assertTrue(manifest.contains("\"originalFileName\": \"original A.png\""))
        assertTrue(manifest.contains("\"pairIndex\": 0"))
        assertTrue(manifest.contains("\"side\": 0"))
    }

    @Test
    fun `extractOrderIds reads exact generated order`() {
        val source = """
            object GeneratedHarmonyContent {
                val ORDER: List<String> = listOf("pack_b", "pack_a", "pack_c")
            }
        """.trimIndent()

        assertEquals(
            listOf("pack_b", "pack_a", "pack_c"),
            DevExportLogic.extractOrderIds(source)
        )
    }
}
