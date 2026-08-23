package com.example.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class GeneratedHarmonyNewPicGameTest {

    @Test
    fun `drive logo game keeps exact pairs and asset positions`() {
        val pack = GeneratedHarmonyNewPicGame.PACKS.single()

        assertEquals("custom_logo", pack.id)
        assertEquals("Logo ", pack.title)
        assertEquals("🖌", pack.emoji)
        assertEquals(
            listOf(
                "img_pack_1787325043190_0" to "img_pack_1787325043190_1",
                "img_pack_1787325043190_2" to "img_pack_1787325043190_3",
                "img_1787325068077_a" to "img_pack_1787325043190_5"
            ),
            pack.pairs
        )

        assertEquals(
            listOf(
                "1000110101.png" to (0 to 0),
                "1000110102.png" to (0 to 1),
                "1000110103.png" to (1 to 0),
                "1000110104.png" to (1 to 1),
                "1000110105.png" to (2 to 0),
                "1000110111.jpg" to (2 to 1)
            ),
            GeneratedHarmonyNewPicGame.ASSETS.map {
                it.originalFileName to (it.pairIndex to it.side)
            }
        )
    }

    @Test
    fun `all six embedded image copies are valid base64`() {
        assertEquals(6, GeneratedHarmonyNewPicGame.IMAGES.size)
        GeneratedHarmonyNewPicGame.IMAGES.forEach { (key, encoded) ->
            assertTrue("empty image for $key", encoded.isNotBlank())
            assertTrue("invalid image for $key", Base64.getDecoder().decode(encoded).isNotEmpty())
        }
    }

    @Test
    fun `registry adds logo game without dropping existing generated packs`() {
        assertTrue(GeneratedHarmonyContent.PACKS.isNotEmpty())
        assertTrue(GeneratedContentRegistry.PACKS.any { it.id == "custom_logo" })
        GeneratedHarmonyContent.PACKS.forEach { existing ->
            assertTrue(
                "existing generated pack ${existing.id} disappeared",
                GeneratedContentRegistry.PACKS.any { it.id == existing.id }
            )
        }
    }
}
