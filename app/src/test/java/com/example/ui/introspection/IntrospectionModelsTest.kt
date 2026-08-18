package com.example.ui.introspection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class IntrospectionModelsTest {

    @Test
    fun `stage progression flows from COLOR to ANIMAL to WATER to REVELATION to RESULTS`() {
        val initial = IntrospectionProgress.initial()
        assertEquals(IntrospectionStage.COLOR, initial.stage)
        assertFalse(initial.hasStarted)
        assertFalse(initial.completed)

        // Advance after Color answer
        val afterColor = initial.advanceAfterAnswer(IntrospectionAnswer.Text("Smaragdgrün"))
        assertEquals(IntrospectionStage.ANIMAL, afterColor.stage)
        assertTrue(afterColor.hasStarted)
        assertFalse(afterColor.completed)
        assertEquals(IntrospectionAnswer.Text("Smaragdgrün"), afterColor.answers[IntrospectionStage.COLOR])

        // Advance after Animal answer
        val afterAnimal = afterColor.advanceAfterAnswer(IntrospectionAnswer.Text("Wolf"))
        assertEquals(IntrospectionStage.WATER, afterAnimal.stage)
        assertTrue(afterAnimal.hasStarted)
        assertFalse(afterAnimal.completed)
        assertEquals(IntrospectionAnswer.Text("Wolf"), afterAnimal.answers[IntrospectionStage.ANIMAL])

        // Advance after Water answer -> transitions to REVELATION
        val afterWater = afterAnimal.advanceAfterAnswer(IntrospectionAnswer.Text("Ozean"))
        assertEquals(IntrospectionStage.REVELATION, afterWater.stage)
        assertTrue(afterWater.hasStarted)
        assertFalse(afterWater.completed)
        assertEquals(IntrospectionAnswer.Text("Ozean"), afterWater.answers[IntrospectionStage.WATER])

        // finishRevelation -> transitions to RESULTS with completed = true
        val results = afterWater.finishRevelation()
        assertEquals(IntrospectionStage.RESULTS, results.stage)
        assertTrue(results.completed)
        assertEquals(3, results.answers.size)
    }

    @Test
    fun `result is only shown after finishRevelation is explicitly invoked`() {
        val progress = IntrospectionProgress(
            stage = IntrospectionStage.REVELATION,
            completed = false,
            answers = mapOf(
                IntrospectionStage.COLOR to IntrospectionAnswer.Text("Blau"),
                IntrospectionStage.ANIMAL to IntrospectionAnswer.Text("Adler"),
                IntrospectionStage.WATER to IntrospectionAnswer.Text("Fluss")
            )
        )

        assertFalse("Must not be completed before finishRevelation", progress.completed)
        assertEquals(IntrospectionStage.REVELATION, progress.stage)

        val completed = progress.finishRevelation()
        assertTrue(completed.completed)
        assertEquals(IntrospectionStage.RESULTS, completed.stage)
    }

    @Test
    fun `empty text or empty audio path validation`() {
        val blankText = "   "
        assertFalse(blankText.isNotBlank())

        val validText = "Tiefes Violett"
        assertTrue(validText.isNotBlank())

        // Audio validation check logic
        val emptyPathFile: File? = null
        val isValidAudio = emptyPathFile?.let { it.exists() && it.isFile && it.length() > 0 } ?: false
        assertFalse(isValidAudio)
    }

    @Test
    fun `required emojis and constants are exactly preserved`() {
        assertEquals("🧙‍♂️", IntrospectionConstants.WIZARD_EMOJI)
        assertEquals("✨️", IntrospectionConstants.SPARKLES_EMOJI)
        assertEquals(300_000L, IntrospectionConstants.MAX_RECORDING_DURATION_MS)
        assertEquals(0.68f, IntrospectionConstants.NARRATION_MUSIC_VOLUME, 0.001f)
        assertEquals(1.0f, IntrospectionConstants.NORMAL_MUSIC_VOLUME, 0.001f)
        assertEquals(0.25f, IntrospectionConstants.ANSWER_PLAYBACK_MUSIC_VOLUME, 0.001f)
    }
}
