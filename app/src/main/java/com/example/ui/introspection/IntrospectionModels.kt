package com.example.ui.introspection

enum class IntrospectionStage {
    COLOR,
    ANIMAL,
    WATER,
    REVELATION,
    RESULTS;

    val isQuestion: Boolean get() = this == COLOR || this == ANIMAL || this == WATER
}

sealed interface IntrospectionAnswer {
    data class Text(val value: String) : IntrospectionAnswer
    data class Audio(val filePath: String) : IntrospectionAnswer

    fun isValid(): Boolean = when (this) {
        is Text -> value.isNotBlank()
        is Audio -> filePath.isNotBlank()
    }
}

data class IntrospectionProgress(
    val stage: IntrospectionStage = IntrospectionStage.COLOR,
    val answers: Map<IntrospectionStage, IntrospectionAnswer> = emptyMap(),
    val completed: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun advanceAfterAnswer(answer: IntrospectionAnswer): IntrospectionProgress {
        if (!stage.isQuestion || !answer.isValid()) return this
        val next = when (stage) {
            IntrospectionStage.COLOR -> IntrospectionStage.ANIMAL
            IntrospectionStage.ANIMAL -> IntrospectionStage.WATER
            IntrospectionStage.WATER -> IntrospectionStage.REVELATION
            else -> stage
        }
        return copy(
            stage = next,
            answers = answers + (stage to answer),
            updatedAt = System.currentTimeMillis()
        )
    }

    fun finishRevelation(): IntrospectionProgress = if (stage == IntrospectionStage.REVELATION) {
        copy(stage = IntrospectionStage.RESULTS, completed = true, updatedAt = System.currentTimeMillis())
    } else {
        this
    }

    fun restart(): IntrospectionProgress = IntrospectionProgress()

    val hasStarted: Boolean get() = answers.isNotEmpty() || stage != IntrospectionStage.COLOR || completed

    companion object {
        fun initial(): IntrospectionProgress = IntrospectionProgress()
    }
}

object IntrospectionConstants {
    const val MAX_RECORDING_DURATION_MS = 300_000L // 5 minutes
    const val NORMAL_MUSIC_VOLUME = 1.0f
    const val NARRATION_MUSIC_VOLUME = 0.68f
    const val ANSWER_PLAYBACK_MUSIC_VOLUME = 0.25f
    const val WIZARD_EMOJI = "🧙‍♂️"
    const val SPARKLES_EMOJI = "✨️"
}
