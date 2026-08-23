package com.example.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.ui.components.HarmonyBottomNav
import com.example.ui.theme.HarmonyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MemoryNavigationTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `bottom navigation exposes Merken instead of profile and developer tabs`() {
        var selected: Int? = null
        composeRule.setContent {
            HarmonyTheme(darkTheme = true) {
                HarmonyBottomNav(
                    selectedTab = 0,
                    onTabSelected = { selected = it },
                    appLanguage = "de"
                )
            }
        }

        composeRule.onNodeWithText("Home").assertExists()
        composeRule.onNodeWithText("Spiele").assertExists()
        composeRule.onNodeWithText("Chat").assertExists()
        composeRule.onNodeWithText("Momente").assertExists()
        composeRule.onNodeWithText("Merken").assertExists()
        composeRule.onNodeWithText("Profil").assertDoesNotExist()
        composeRule.onNodeWithText("Dev").assertDoesNotExist()

        composeRule.onNodeWithTag("nav_item_4").performClick()
        assertEquals(4, selected)
    }
}
