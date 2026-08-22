package com.example.ui.screens

import java.util.Random

private const val TOT_SHUFFLE_DECOY_COUNT = 2

internal data class TotShufflePlan(
    val shuffleKeys: List<String>,
    val finalPair: Pair<String, String>
)

private fun availableTotShuffleKeys(
    allPairs: List<Pair<String, String>>,
    visiblePair: Pair<String, String>
): List<String> = allPairs
    .flatMap { listOf(it.first, it.second) }
    .filterNot { it == visiblePair.first || it == visiblePair.second }
    .distinct()

/**
 * Builds the frame keys consumed by the card-flip loop.
 *
 * The first frames are quick decoys from the active pack. The final two frames always
 * restore the real visible pair (top first, bottom second). This prevents the last
 * random image pair from looking like the final result during the closing movement.
 */
internal fun buildTotShuffleFrames(
    allPairs: List<Pair<String, String>>,
    visiblePair: Pair<String, String>,
    count: Int,
    random: Random = Random()
): List<String> {
    val allowed = availableTotShuffleKeys(allPairs, visiblePair)
    if (allowed.isEmpty() || count <= 0) return emptyList()

    if (count == 1) {
        return listOf(visiblePair.first)
    }

    val decoyCount = (count - 2).coerceAtLeast(0)
    val decoys = List(decoyCount) { allowed[random.nextInt(allowed.size)] }
    return decoys + visiblePair.first + visiblePair.second
}

/**
 * Describes the intended transition independently from Compose animation timing:
 * a short random phase, then one deterministic final pair.
 */
internal fun buildTotShufflePlan(
    allPairs: List<Pair<String, String>>,
    visiblePair: Pair<String, String>,
    random: Random = Random()
): TotShufflePlan {
    val allowed = availableTotShuffleKeys(allPairs, visiblePair)
    val decoys = if (allowed.isEmpty()) {
        emptyList()
    } else {
        List(TOT_SHUFFLE_DECOY_COUNT) { allowed[random.nextInt(allowed.size)] }
    }
    return TotShufflePlan(
        shuffleKeys = decoys,
        finalPair = visiblePair
    )
}
