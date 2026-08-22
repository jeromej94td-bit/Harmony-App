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

    @Test
    fun `transition plan ends on the real pair and keeps shuffle phase short`() {
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

        assertEquals(3, plan.shuffleKeys.size)
        assertTrue(plan.shuffleKeys.none { it == visiblePair.first || it == visiblePair.second })
        assertEquals(visiblePair, plan.finalPair)
        assertTrue(plan.shufflePhaseDurationMillis <= 550)
    }
}
