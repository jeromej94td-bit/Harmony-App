package com.example.data

import org.junit.Assert.assertEquals
import org.junit.Test

class DevGenTypesV2Test {
    @Test
    fun `generated types preserve emoji default answer and asset metadata`() {
        val question = GenQuestion(
            q = "Frage",
            options = listOf("A", "B"),
            defaultMine = "A"
        )
        val pack = GenPack(
            id = "p",
            title = "Pack",
            cat = "tot",
            emoji = "🍦",
            questions = listOf(question)
        )
        val asset = GenAssetMeta(
            optionKey = "A",
            originalFileName = "01a_A.png",
            packId = "p",
            pairIndex = 0,
            side = 0
        )

        assertEquals("🍦", pack.emoji)
        assertEquals("A", pack.questions.single().defaultMine)
        assertEquals("01a_A.png", asset.originalFileName)
    }
}
