package com.example.ui.introspection

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.IntrospectionExperienceScreen
import com.example.ui.theme.HarmonyTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class IntrospectionUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        val store = IntrospectionStore(ApplicationProvider.getApplicationContext())
        store.clear()
    }

    @Test
    fun `entry screen displays eyebrow, title, subtitle and start button`() {
        composeTestRule.setContent {
            HarmonyTheme {
                IntrospectionExperienceScreen(
                    appLanguage = "de",
                    onExit = {}
                )
            }
        }

        composeTestRule.onNodeWithText("✨️ Das Verborgene in dir").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tauche ins Unterbewusstsein").assertIsDisplayed()
        composeTestRule.onNodeWithTag("entry_start_button").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `starting fresh journey opens first question and text entry enables submit button`() {
        composeTestRule.setContent {
            HarmonyTheme {
                IntrospectionExperienceScreen(
                    appLanguage = "de",
                    onExit = {}
                )
            }
        }

        // Click Start
        composeTestRule.onNodeWithTag("entry_start_button").performScrollTo().performClick()

        // Verify Question 1 is visible
        composeTestRule.onNodeWithText("Schritt 1 von 3").assertIsDisplayed()
        composeTestRule.onNodeWithText("Was ist deine Lieblingsfarbe?").assertIsDisplayed()

        // Submit button should be initially disabled (empty input)
        composeTestRule.onNodeWithTag("confirm_answer_button").performScrollTo().assertIsNotEnabled()

        // Enter text
        composeTestRule.onNodeWithTag("mystic_text_field").performTextInput("Tiefes Meeresblau")

        // Submit button should now be enabled
        composeTestRule.onNodeWithTag("confirm_answer_button").performScrollTo().assertIsEnabled()
    }

    @Test
    fun `existing saved progress triggers continue or restart dialog`() {
        // Save existing progress
        val store = IntrospectionStore(ApplicationProvider.getApplicationContext())
        store.save(
            IntrospectionProgress(
                stage = IntrospectionStage.ANIMAL,
                completed = false,
                answers = mapOf(
                    IntrospectionStage.COLOR to IntrospectionAnswer.Text("Smaragdgrün")
                )
            )
        )

        composeTestRule.setContent {
            HarmonyTheme {
                IntrospectionExperienceScreen(
                    appLanguage = "de",
                    onExit = {}
                )
            }
        }

        // On entry screen, click Start
        composeTestRule.onNodeWithTag("entry_start_button").performScrollTo().performClick()

        // Verify dialog is displayed
        composeTestRule.onNodeWithText("Bestehende Reise gefunden").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dialog_continue_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dialog_restart_button").assertIsDisplayed()

        // Click continue -> Resumes to Question 2 (Animal)
        composeTestRule.onNodeWithTag("dialog_continue_button").performClick()
        composeTestRule.onNodeWithText("Schritt 2 von 3").assertIsDisplayed()
        composeTestRule.onNodeWithText("Was ist dein Lieblingstier?").assertIsDisplayed()
    }

    @Test
    fun `portal component renders without crashing`() {
        composeTestRule.setContent {
            HarmonyTheme {
                IntrospectionPortal(isRevelation = false)
            }
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun `revelation portal component renders without crashing`() {
        composeTestRule.setContent {
            HarmonyTheme {
                IntrospectionPortal(isRevelation = true)
            }
        }

        composeTestRule.waitForIdle()
    }
}
