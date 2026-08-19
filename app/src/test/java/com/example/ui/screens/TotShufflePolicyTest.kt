package com.example.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TotShufflePolicyTest {
    @Test
    fun `shuffle frames use only options from the active pack and exclude visible pair`() {
        val frames = buildTotShuffleFrames(
            allPairs = listOf(
                "Vanille" to "Schokolade",
                "Erdbeere" to "Pistazie",
                "Mango" to "Zitrone"
            ),
            visiblePair = "Vanille" to "Schokolade",
            count = 4,
            random = java.util.Random(7)
        )

        assertEquals(4, frames.size)
        assertTrue(frames.all { it in setOf("Erdbeere", "Pistazie", "Mango", "Zitrone") })
    }

    @Test
    fun `shuffle frames do not invent images when pack has no alternative options`() {
        val frames = buildTotShuffleFrames(
            allPairs = listOf("Vanille" to "Schokolade"),
            visiblePair = "Vanille" to "Schokolade",
            count = 4,
            random = java.util.Random(7)
        )

        assertTrue(frames.isEmpty())
    }
}
