package com.example.ui

import com.example.data.model.HarmonyPacksData
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UniversalStudiosIntroContractTest {
    @Test
    fun universalStudiosPackLivesUnderFilmAndSeries() {
        val pack = HarmonyPacksData.PACKS.first { it.id == "cj_universal_quiz" }
        assertEquals("filme_serien", pack.topic)
        assertEquals("Universal Studios Genie oder Neuling?", pack.title)
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
