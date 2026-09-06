package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import com.example.ui.components.AmbientBackground
import com.example.ui.components.HarmonyBottomNav
import com.example.ui.theme.HarmonyTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class BottomNavGameCarouselTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun gameVisualUsesTenSecondsAndKeepsEitherOrForSeventeenSeconds() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            HarmonyTheme(darkTheme = true) {
                AmbientBackground {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                        HarmonyBottomNav(
                            selectedTab = 0,
                            onTabSelected = {},
                            appLanguage = "de"
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag("nav_games_visual_zeich_running", useUnmergedTree = true).assertExists()

        composeTestRule.mainClock.advanceTimeBy(10_700)
        composeTestRule.onNodeWithTag("nav_games_visual_tot_running", useUnmergedTree = true).assertExists()

        composeTestRule.mainClock.advanceTimeBy(16_900)
        composeTestRule.onNodeWithTag("nav_games_visual_tot_running", useUnmergedTree = true).assertExists()

        composeTestRule.mainClock.advanceTimeBy(700)
        composeTestRule.onNodeWithTag("nav_games_visual_zust_running", useUnmergedTree = true).assertExists()
    }

    @Test
    fun selectingGamesPausesTheCurrentVisual() {
        composeTestRule.mainClock.autoAdvance = false
        val selectedTab = mutableIntStateOf(0)
        composeTestRule.setContent {
            HarmonyTheme(darkTheme = true) {
                AmbientBackground {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                        HarmonyBottomNav(
                            selectedTab = selectedTab.intValue,
                            onTabSelected = { selectedTab.intValue = it },
                            appLanguage = "de"
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag("nav_games_visual_zeich_running", useUnmergedTree = true).assertExists()
        composeTestRule.mainClock.advanceTimeBy(10_700)
        composeTestRule.runOnIdle { selectedTab.intValue = 1 }
        composeTestRule.mainClock.advanceTimeByFrame()
        composeTestRule.onNodeWithTag("nav_games_visual_tot_paused", useUnmergedTree = true).assertExists()

        composeTestRule.mainClock.advanceTimeBy(30_000)
        composeTestRule.onNodeWithTag("nav_games_visual_tot_paused", useUnmergedTree = true).assertExists()
    }

    @Test
    fun bottomNavigationPreviewFrames() {
        composeTestRule.mainClock.autoAdvance = false
        val selectedTab = mutableIntStateOf(0)
        composeTestRule.setContent {
            HarmonyTheme(darkTheme = true) {
                AmbientBackground {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                        HarmonyBottomNav(
                            selectedTab = selectedTab.intValue,
                            onTabSelected = { selectedTab.intValue = it },
                            appLanguage = "de"
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag("nav_games_visual_zeich_running", useUnmergedTree = true).assertExists()
        composeTestRule.onRoot().captureRoboImage(
            filePath = "build/bottom-nav-rework-preview/01-home-zeichenspiel.png"
        )

        composeTestRule.mainClock.advanceTimeBy(10_700)
        composeTestRule.onNodeWithTag("nav_games_visual_tot_running", useUnmergedTree = true).assertExists()
        composeTestRule.onRoot().captureRoboImage(
            filePath = "build/bottom-nav-rework-preview/02-home-das-oder-das.png"
        )

        composeTestRule.runOnIdle { selectedTab.intValue = 1 }
        composeTestRule.mainClock.advanceTimeByFrame()
        composeTestRule.onNodeWithTag("nav_games_visual_tot_paused", useUnmergedTree = true).assertExists()
        composeTestRule.onRoot().captureRoboImage(
            filePath = "build/bottom-nav-rework-preview/03-spiele-pausiert.png"
        )
    }
}
