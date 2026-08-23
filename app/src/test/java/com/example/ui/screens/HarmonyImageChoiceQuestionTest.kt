package com.example.ui.screens

import com.example.data.model.HarmonyPacksData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HarmonyImageChoiceQuestionTest {

    @Test
    fun `food pack stores egg fourth and steak fifth with twelve real answers each`() {
        val foodPack = HarmonyPacksData.DEFAULT_PACKS.first { it.id == "essenreden" }

        assertEquals("Wie möchtest du dein Ei am liebsten?", foodPack.questions[3].q)
        assertEquals(
            listOf(
                "4 Minuten – Sehr flüssig",
                "5 Minuten – Flüssig",
                "6 Minuten – Weich & cremig",
                "7 Minuten – Weiches Eigelb",
                "8 Minuten – Cremiges Eigelb",
                "9 Minuten – Fast fest",
                "10 Minuten – Vollständig fest",
                "11 Minuten – Fest",
                "12 Minuten – Sehr fest",
                "13 Minuten – Trocken",
                "14 Minuten – Sehr trocken",
                "15 Minuten – Übergart"
            ),
            foodPack.questions[3].options
        )

        assertEquals("Wie willst du dein Steak?", foodPack.questions[4].q)
        assertEquals(
            listOf(
                "Roh – Kaltes Herz, roh",
                "Fast roh – Sehr kühler Kern, rot",
                "Sehr blutig – Kühler Kern, rot",
                "Blutig (Blue-Rare) – Kühler roter Kern",
                "Sehr englisch (Rare) – Warmer roter Kern",
                "Englisch (Rare) – Warmer Kern, zart",
                "Halb rosa – Rosa Center, saftig",
                "Rosa (Medium-Rare) – Rosa Kern, zart",
                "Medium – Rosa Center, saftig",
                "Halb durch (Medium-Well) – Teils rosa Center",
                "Durch (Well-Done) – Kein Rosa, fest",
                "Übergart – Ganz trocken & fest"
            ),
            foodPack.questions[4].options
        )
    }

    @Test
    fun `travel pack stores travel style as fifth question`() {
        val travelPack = HarmonyPacksData.DEFAULT_PACKS.first { it.id == "reisevor" }

        assertEquals("Wie sieht deine Traumreise aus?", travelPack.questions[4].q)
        assertEquals(
            listOf(
                "Strand & Sonne – Relaxen, Meer & Cocktails",
                "Städtetrip & Kultur – Museen, Architektur & Flanieren",
                "Abenteuer & Trekking – Berge, Wandern & Grenzen testen",
                "Safari & Wildtiere – Naturbeobachtungen, Abenteuer & Wildnis",
                "Kultur & Geschichte – Vergangenheit entdecken, Ruinen & Wissen",
                "Kreuzfahrt – Häfen entdecken, Meerblick & Entspannung pur",
                "Roadtrip & Freiheit – Vanlife, Entdecken & Unabhängigkeit",
                "Wellness & Entspannung – Spa, Erholung & Seele baumeln lassen",
                "Inselhüpfen & Bootstrip – Viele Orte, Boot fahren & Schnorcheln",
                "Camping & Natur – Natur erleben, Lagerfeuer & Sterne",
                "Gourmet & Foodie – Kulinarische Entdeckungen, Restaurants & Genuss",
                "Festival & Musik – Energie, Konzerte & Feiern"
            ),
            travelPack.questions[4].options
        )
    }

    @Test
    fun `only the three requested question positions use the image choice screen`() {
        assertEquals(HarmonyImageChoiceKind.EGG, harmonyImageChoiceKind("essenreden", 3))
        assertEquals(HarmonyImageChoiceKind.STEAK, harmonyImageChoiceKind("essenreden", 4))
        assertEquals(HarmonyImageChoiceKind.TRAVEL, harmonyImageChoiceKind("reisevor", 4))

        assertNull(harmonyImageChoiceKind("essenreden", 2))
        assertNull(harmonyImageChoiceKind("reisevor", 3))
        assertNull(harmonyImageChoiceKind("other", 4))
    }

    @Test
    fun `domino reveal advances left to right before starting the next row`() {
        val delays = (0 until 12).map(::harmonyImageChoiceRevealDelayMillis)

        assertEquals(
            listOf(0L, 110L, 220L, 420L, 530L, 640L, 840L, 950L, 1060L, 1260L, 1370L, 1480L),
            delays
        )
    }
}
