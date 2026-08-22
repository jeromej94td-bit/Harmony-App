package com.example.ui.screens

import java.util.Random

private const val TOT_SHUFFLE_FRAME_COUNT = 3
internal const val TOT_SHUFFLE_FLIP_OUT_MILLIS = 60
internal const val TOT_SHUFFLE_FLIP_IN_MILLIS = 75

internal data class TotShufflePlan(
    val shuffleKeys: List<String>,
    val finalPair: Pair<String, String>,
    val flipOutMillis: Int = TOT_SHUFFLE_FLIP_OUT_MILLIS,
    val flipInMillis: Int = TOT_SHUFFLE_FLIP_IN_MILLIS
) {
    /** Includes the final flip that restores the real pair before the closing move. */
    val shufflePhaseDurationMillis: Int
        get() = (shuffleKeys.size + 1) * (flipOutMillis + flipInMillis)
}

/** Selects only already-present options from the current This-or-That pack. */
internal fun buildTotShuffleFrames(
    allPairs: List<Pair<String, String>>,
    visiblePair: Pair<String, String>,
    count: Int,
    random: Random = Random()
): List<String> {
    val allowed = allPairs.flatMap { listOf(it.first, it.second) }
        .filterNot { it == visiblePair.first || it == visiblePair.second }
        .distinct()
    if (allowed.isEmpty() || count <= 0) return emptyList()
    return List(count) { allowed[random.nextInt(allowed.size)] }
}

/**
 * Plans the quick decoy shuffle and, critically, the final visible frame.
 * The shuffle may only use options from the current pack, while the frame that is
 * shown before the cards move together is always the real pair the user just chose.
 */
internal fun buildTotShufflePlan(
    allPairs: List<Pair<String, String>>,
    visiblePair: Pair<String, String>,
    random: Random = Random()
): TotShufflePlan = TotShufflePlan(
    shuffleKeys = buildTotShuffleFrames(
        allPairs = allPairs,
        visiblePair = visiblePair,
        count = TOT_SHUFFLE_FRAME_COUNT,
        random = random
    ),
    finalPair = visiblePair
)
