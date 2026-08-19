package com.example.ui.screens

import java.util.Random

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
