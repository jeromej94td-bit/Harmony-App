package com.example.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TotShufflePolicyTest {
    @Test
    fun `shuffle transition ends on the next real pair instead of the outgoing pair`() {
        val currentPair = "Vanille" to "Schokolade"
        val nextPair = "Erdbeere" to "Pistazie"
        val frames = buildTotShuffleFrames(
            allPairs = listOf(
                currentPair,
                nextPair,
                "Mango" to "Zitrone"
            ),
            visiblePair = currentPair,
            count = 4,
            random = java.util.Random(7)
        )

        assertEquals(listOf(nextPair.first, nextPair.second), frames.takeLast(2))
        assertTrue(frames.dropLast(2).none { it in setOf(currentPair.first, currentPair.second, nextPair.first, nextPair.second) })
    }

    @Test
    fun `transition plan exposes exactly the next pair as its final visual state`() {
        val currentPair = "Vanille" to "Schokolade"
        val nextPair = "Erdbeere" to "Pistazie"
        val plan = buildTotShufflePlan(
            allPairs = listOf(
                currentPair,
                nextPair,
                "Mango" to "Zitrone"
            ),
            visiblePair = currentPair,
            random = java.util.Random(7)
        )

        assertEquals(nextPair, plan.finalPair)
        assertTrue(plan.shuffleKeys.size <= 2)
        assertTrue(plan.shuffleKeys.none { it in setOf(currentPair.first, currentPair.second, nextPair.first, nextPair.second) })
    }

    @Test
    fun `last pair keeps itself as final state before results`() {
        val firstPair = "Vanille" to "Schokolade"
        val lastPair = "Mango" to "Zitrone"
        val plan = buildTotShufflePlan(
            allPairs = listOf(firstPair, lastPair),
            visiblePair = lastPair,
            random = java.util.Random(7)
        )

        assertEquals(lastPair, plan.finalPair)
    }

    @Test
    fun `single pair pack does not invent shuffle images`() {
        val pair = "Vanille" to "Schokolade"
        val frames = buildTotShuffleFrames(
            allPairs = listOf(pair),
            visiblePair = pair,
            count = 4,
            random = java.util.Random(7)
        )

        assertTrue(frames.isEmpty())
    }
}
