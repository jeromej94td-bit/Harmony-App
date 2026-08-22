package com.example.ui.screens

import java.util.Random

private const val TOT_SHUFFLE_DECOY_COUNT = 2

internal data class TotShufflePlan(
    val shuffleKeys: List<String>,
    val finalPair: Pair<String, String>
)

private fun nextTotPair(
    allPairs: List<Pair<String, String>>,
    visiblePair: Pair<String, String>
): Pair<String, String> {
    val currentIndex = allPairs.indexOfFirst { it == visiblePair }
    return if (currentIndex >= 0 && currentIndex < allPairs.lastIndex) {
        allPairs[currentIndex + 1]
    } else {
        visiblePair
    }
}

private fun availableTotShuffleKeys(
    allPairs: List<Pair<String, String>>,
    visiblePair: Pair<String, String>,
    finalPair: Pair<String, String>
): List<String> = allPairs
    .flatMap { listOf(it.first, it.second) }
    .filterNot {
        it == visiblePair.first ||
            it == visiblePair.second ||
            it == finalPair.first ||
            it == finalPair.second
    }
    .distinct()

/**
 * Builds the frame keys consumed by the existing card-flip loop.
 *
 * The outgoing pair may briefly shuffle through other options from the same pack, but
 * the last two frames are always the NEXT real pair. Therefore the pair visible during
 * the closing movement is already the same pair that Compose receives after onPick()
 * advances the index. There is no second "finished pair -> different pair" visual jump.
 */
internal fun buildTotShuffleFrames(
    allPairs: List<Pair<String, String>>,
    visiblePair: Pair<String, String>,
    count: Int,
    random: Random = Random()
): List<String> {
    if (count <= 0 || allPairs.size <= 1) return emptyList()

    val finalPair = nextTotPair(allPairs, visiblePair)
    if (count == 1) return listOf(finalPair.first)

    val allowed = availableTotShuffleKeys(allPairs, visiblePair, finalPair)
    val decoyCount = (count - 2).coerceAtLeast(0)
    val decoys = if (allowed.isEmpty()) {
        emptyList()
    } else {
        List(decoyCount) { allowed[random.nextInt(allowed.size)] }
    }

    return decoys + finalPair.first + finalPair.second
}

/**
 * Pure description of the visual transition: a short random phase followed by exactly
 * one deterministic incoming pair. On the last question there is no incoming pair, so
 * the current pair remains the final state before the results screen.
 */
internal fun buildTotShufflePlan(
    allPairs: List<Pair<String, String>>,
    visiblePair: Pair<String, String>,
    random: Random = Random()
): TotShufflePlan {
    val finalPair = nextTotPair(allPairs, visiblePair)
    val allowed = availableTotShuffleKeys(allPairs, visiblePair, finalPair)
    val decoys = if (allowed.isEmpty()) {
        emptyList()
    } else {
        List(TOT_SHUFFLE_DECOY_COUNT) { allowed[random.nextInt(allowed.size)] }
    }

    return TotShufflePlan(
        shuffleKeys = decoys,
        finalPair = finalPair
    )
}
