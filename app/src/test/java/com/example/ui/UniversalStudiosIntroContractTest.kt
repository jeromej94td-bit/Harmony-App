package com.example.ui

import com.example.data.GeneratedHarmonyContent
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UniversalStudiosIntroContractTest {
    @Test
    fun universalStudiosPackLivesUnderFilmAndSeries() {
        val pack = GeneratedHarmonyContent.PACKS.first { it.id == UNIVERSAL_STUDIOS_INTRO_PACK_ID }
        assertEquals("filme_serien", pack.topic)
        assertEquals("Universal Studios Genie oder Neuling?", pack.title)
    }

    @Test
    fun introStartsOnlyForTheOpeningQuestionOfUniversalStudios() {
        assertTrue(shouldPlayUniversalStudiosIntro(UNIVERSAL_STUDIOS_INTRO_PACK_ID, 0))
        assertFalse(shouldPlayUniversalStudiosIntro(UNIVERSAL_STUDIOS_INTRO_PACK_ID, 1))
        assertFalse(shouldPlayUniversalStudiosIntro("cj_disney_quiz", 0))
    }

    @Test
    fun universalStudiosIntroVideoIsBundledLocally() {
        val intro = File("src/main/res/raw/universal_studios_intro.mp4")
        assertTrue(
            "The Universal Studios pack needs a bundled intro video before questions are shown",
            intro.isFile && intro.length() > 10_000L
        )
    }
}
