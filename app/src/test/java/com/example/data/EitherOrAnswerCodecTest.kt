package com.example.data

import com.example.data.model.EitherOrAnswerCodec
import com.example.data.model.HarmonyPacksData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EitherOrAnswerCodecTest {
    @Test
    fun `couple choices round trip and retain match state`() {
        val encoded = EitherOrAnswerCodec.encode("Pizza 🍕", "Pizza 🍕")
        val decoded = EitherOrAnswerCodec.decode(encoded)

        assertEquals("Pizza 🍕", decoded?.userChoice)
        assertEquals("Pizza 🍕", decoded?.partnerChoice)
        assertTrue(decoded?.isMatch == true)
    }

    @Test
    fun `different choices do not match and legacy values are ignored`() {
        val decoded = EitherOrAnswerCodec.decode(EitherOrAnswerCodec.encode("Strand", "Berge"))
        assertFalse(decoded?.isMatch ?: true)
        assertNull(EitherOrAnswerCodec.decode("legacy answer"))
    }

    @Test
    fun `panda either or contains all unique source questions`() {
        val pack = HarmonyPacksData.PACKS.first { it.id == "entweder_oder_panda" }
        assertEquals(70, pack.pairs.size)
        assertEquals(70, pack.pairs.distinct().size)
    }
}
