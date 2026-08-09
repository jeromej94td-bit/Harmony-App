package com.example.data.model

/**
 * Language-independent identity for one side of a "This or That" pair.
 *
 * [assetKey] is stable across translations and is the key new image imports should use.
 * [legacyAssetKey] keeps existing installations and exported content compatible while
 * image data is gradually migrated away from display-text keys.
 * [answerValue] remains the canonical stored answer and must never be localized before saving.
 */
data class TotChoiceIdentity(
    val answerValue: String,
    val assetKey: String,
    val legacyAssetKey: String
)

enum class TotChoiceSide(val keyPart: String) {
    FIRST("a"),
    SECOND("b")
}

fun QuestionPack.totChoiceAt(pairIndex: Int, side: TotChoiceSide): TotChoiceIdentity {
    val pair = pairs.getOrNull(pairIndex) ?: ("" to "")
    val answerValue = when (side) {
        TotChoiceSide.FIRST -> pair.first
        TotChoiceSide.SECOND -> pair.second
    }
    return TotChoiceIdentity(
        answerValue = answerValue,
        assetKey = "tot:${id}:${pairIndex}:${side.keyPart}",
        legacyAssetKey = answerValue
    )
}
