package com.example.ui.introspection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IntrospectionModelsTest {

    @Test
    fun `blank text answer cannot advance`() {
        val progress = IntrospectionProgress()

        assertEquals(progress, progress.advanceAfterAnswer(IntrospectionAnswer.Text("   ")))
    }

    @Test
    fun `valid answers advance through the three signs and revelation`() {
        val color = IntrospectionProgress()
            .advanceAfterAnswer(IntrospectionAnswer.Text("Violett, weil es geheimnisvoll wirkt"))
        val animal = color.advanceAfterAnswer(IntrospectionAnswer.Audio("/private/animal.m4a"))
        val water = animal.advanceAfterAnswer(IntrospectionAnswer.Text("Ein stiller Bergsee"))

        assertEquals(IntrospectionStage.ANIMAL, color.stage)
        assertEquals(IntrospectionStage.WATER, animal.stage)
        assertEquals(IntrospectionStage.REVELATION, water.stage)
        assertEquals(3, water.answers.size)
    }

    @Test
    fun `missing audio path cannot advance`() {
        val progress = IntrospectionProgress()

        assertEquals(progress, progress.advanceAfterAnswer(IntrospectionAnswer.Audio("")))
    }

    @Test
    fun `completing revelation exposes results and restart clears all answers`() {
        val answered = IntrospectionProgress(
            stage = IntrospectionStage.REVELATION,
            answers = mapOf(
                IntrospectionStage.COLOR to IntrospectionAnswer.Text("Blau"),
                IntrospectionStage.ANIMAL to IntrospectionAnswer.Text("Wolf"),
                IntrospectionStage.WATER to IntrospectionAnswer.Text("Meer")
            )
        )

        val completed = answered.finishRevelation()
        val restarted = completed.restart()

        assertEquals(IntrospectionStage.RESULTS, completed.stage)
        assertTrue(completed.completed)
        assertEquals(IntrospectionStage.COLOR, restarted.stage)
        assertFalse(restarted.completed)
        assertTrue(restarted.answers.isEmpty())
    }
}
