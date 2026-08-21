package com.example.ui.components

import com.example.data.GeneratedHarmonyMarkenAlltag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkenAlltagImagesTest {

    @Test
    fun `marken alltag has eleven complete pairs`() {
        val pack = GeneratedHarmonyMarkenAlltag.PACKS.single()
        assertEquals("markenalltag", pack.id)
        assertEquals(11, pack.pairs.size)
        assertEquals("Instagram" to "YouTube", pack.pairs.last())
    }

    @Test
    fun `every marken alltag option has a bundled drawable`() {
        val options = GeneratedHarmonyMarkenAlltag.PACKS.single()
            .pairs
            .flatMap { listOf(it.first, it.second) }

        assertEquals(22, options.distinct().size)
        options.forEach { option ->
            assertNotNull("Missing bundled image for $option", MarkenAlltagImages.get(option))
        }
    }

    @Test
    fun `installed defaults resolve locally through TotImageProvider`() {
        MarkenAlltagImages.installAsDefaults()

        GeneratedHarmonyMarkenAlltag.PACKS.single()
            .pairs
            .flatMap { listOf(it.first, it.second) }
            .forEach { option ->
                assertTrue(
                    "$option should resolve to an Android drawable",
                    TotImageProvider.getImageUrl(option) is Int
                )
            }
    }
}
