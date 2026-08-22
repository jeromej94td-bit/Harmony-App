package com.example.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TotShufflePolicyTest {
    @Test
    fun `shuffle transition uses pack options then restores the real pair before closing`() {
        val visiblePair = "Vanille" to "Schokolade"
        val frames = buildTotShuffleFrames(
            allPairs = listOf(
                visiblePair,
                "Erdbeere" to "Pistazie",
                "Mango" to "Zitrone"
            ),
            visiblePair = visiblePair,
            count = 4,
            random = java.util.Random(7)
        )

        assertEquals(4, frames.size)
        assertTrue(frames.take(2).all { it in setOf("Erdbeere", "Pistazie", "Mango", "Zitrone") })
        assertEquals(listOf(visiblePair.first, visiblePair.second), frames.takeLast(2))
    }

    @Test
    fun `shuffle transition does not invent images when pack has no alternative options`() {
        val frames = buildTotShuffleFrames(
            allPairs = listOf("Vanille" to "Schokolade"),
            visiblePair = "Vanille" to "Schokolade",
            count = 4,
            random = java.util.Random(7)
        )

        assertTrue(frames.isEmpty())
    }

    @Test
    fun `transition plan keeps the random phase short and ends on the real pair`() {
        val visiblePair = "Vanille" to "Schokolade"
        val plan = buildTotShufflePlan(
            allPairs = listOf(
                visiblePair,
                "Erdbeere" to "Pistazie",
                "Mango" to "Zitrone"
            ),
            visiblePair = visiblePair,
            random = java.util.Random(7)
        )

        assertEquals(visiblePair, plan.finalPair)
        assertTrue(plan.shuffleKeys.size <= 2)
        assertTrue(plan.shuffleKeys.none { it == visiblePair.first || it == visiblePair.second })
    }
}
