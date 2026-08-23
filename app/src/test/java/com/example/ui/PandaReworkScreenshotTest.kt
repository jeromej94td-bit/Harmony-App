package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnswerEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.CoupleStatsEntity
import com.example.data.model.EitherOrAnswerCodec
import com.example.data.model.HarmonyPacksData
import com.example.data.model.ProfileEntity
import com.example.ui.ActivePackRun
import com.example.ui.components.AmbientBackground
import com.example.ui.components.HarmonyBottomNav
import com.example.ui.components.HarmonyTopBar
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CategoryRailCard
import com.example.ui.screens.GamesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PandaEitherOrScreen
import com.example.ui.screens.ProfileSheet
import com.example.ui.screens.QuizRunnerScreen
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
class PandaReworkScreenshotTest {
    @get:Rule val composeTestRule = createComposeRule()

    private val profile = ProfileEntity(userName = "Ralf", partnerName = "J")

    @Test
    fun pandaCategoriesDraft() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            HarmonyTheme(darkTheme = true) {
                AmbientBackground {
                    Scaffold(
                        containerColor = Color.Transparent,
                        topBar = { HarmonyTopBar("Ralf", "J", onProfileClick = {}) },
                        bottomBar = { HarmonyBottomNav(1, {}, "de") }
                    ) { padding ->
                        GamesScreen(emptyList(), "all", onSetFilter = {}, onCategoryClick = {}, onTopicClick = {}, onStartPack = {}, modifier = Modifier.padding(padding))
                    }
                }
            }
        }
        composeTestRule.mainClock.advanceTimeBy(5_500)
        composeTestRule.onRoot().captureRoboImage(filePath = "build/panda-rework-preview/01-panda-kategorien.png")
    }

    @Test
    fun cardAndNeverCategoryDraft() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            HarmonyTheme(darkTheme = true) {
                AmbientBackground {
                    Column(Modifier.fillMaxSize().padding(top = 96.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CategoryRailCard(HarmonyPacksData.CATEGORIES.first { it.id == "tot" }, onClick = {})
                            CategoryRailCard(HarmonyPacksData.CATEGORIES.first { it.id == "nie" }, onClick = {})
                        }
                    }
                }
            }
        }
        composeTestRule.mainClock.advanceTimeBy(5_500)
        composeTestRule.onRoot().captureRoboImage(filePath = "build/panda-rework-preview/09-karten-und-nie.png")
    }

    @Test
    fun readableQuestionOverlayDraft() {
        composeTestRule.mainClock.autoAdvance = false
        val pack = HarmonyPacksData.PACKS.first { it.cat == "wer" && it.type == "quiz" }
        composeTestRule.setContent {
            HarmonyTheme(darkTheme = true) {
                Box(Modifier.fillMaxSize().background(Color(0xFFFFD600))) {
                    Text(
                        text = "HINTERGRUND DARF NICHT DURCHSCHEINEN",
                        color = Color.Black,
                        fontSize = 38.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    QuizRunnerScreen(
                        activeRun = ActivePackRun(pack = pack),
                        profile = profile,
                        isExitConfirmOpen = false,
                        isOwnAnswerDialogOpen = false,
                        appLanguage = "de",
                        onPickAnswer = {},
                        onPickTot = {},
                        onNextStep = {},
                        onAskExit = {},
                        onCloseExitConfirm = {},
                        onCloseRunner = {},
                        onOpenOwnAnswerDialog = { _, _ -> },
                        onCloseOwnAnswerDialog = {},
                        onSaveOwnAnswer = {}
                    )
                }
            }
        }
        composeTestRule.mainClock.advanceTimeBy(10_500)
        composeTestRule.onRoot().captureRoboImage(filePath = "build/panda-rework-preview/08-fragen-lesbar.png")
    }

    @Test
    fun eitherOrHighFiveDraft() {
        val answeredExceptFirst = (1 until 70).map {
            AnswerEntity("entweder_oder_panda", it, EitherOrAnswerCodec.encode("A", "B"))
        }
        composeTestRule.setContent {
            HarmonyTheme(darkTheme = true) {
                PandaEitherOrScreen(profile, answeredExceptFirst, onSaveAnswer = { _, _, _ -> }, onExit = {})
            }
        }
        composeTestRule.onNodeWithText("Frühstück im Bett 🥐").performClick()
        composeTestRule.onNodeWithText("J ist bereit").performClick()
        composeTestRule.onNodeWithText("Frühstück im Bett 🥐").performClick()
        composeTestRule.mainClock.advanceTimeBy(900)
        composeTestRule.onRoot().captureRoboImage(filePath = "build/panda-rework-preview/02-entweder-oder-high-five.png")
    }

    @Test
    fun compactHomeDraft() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            HarmonyTheme(darkTheme = true) {
                AmbientBackground {
                    Scaffold(
                        containerColor = Color.Transparent,
                        topBar = { HarmonyTopBar("Ralf", "J", onProfileClick = {}) },
                        bottomBar = { HarmonyBottomNav(0, {}, "de") }
                    ) { padding ->
                        HomeScreen(
                            profile = profile,
                            answers = listOf(AnswerEntity("zuhause", 0, "Die Gemütlichkeit und Ruhe")),
                            sharedPics = emptyList(),
                            stats = CoupleStatsEntity(),
                            onStartPack = {},
                            onAddSharedPictures = { _, _ -> },
                            onUpdateSharedPicture = {},
                            onPinWidget = {},
                            modifier = Modifier.padding(padding)
                        )
                    }
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "build/panda-rework-preview/03-home-picshare.png")
    }

    @Test
    fun homeDialogsDrafts() {
        composeTestRule.setContent {
            HarmonyTheme(darkTheme = true) {
                AmbientBackground {
                    HomeScreen(
                        profile = profile,
                        answers = listOf(AnswerEntity("zuhause", 0, "Die Gemütlichkeit und Ruhe")),
                        sharedPics = emptyList(),
                        stats = CoupleStatsEntity(),
                        onStartPack = {},
                        onAddSharedPictures = { _, _ -> },
                        onUpdateSharedPicture = {},
                        onPinWidget = {}
                    )
                }
            }
        }
        composeTestRule.onNodeWithText("Status").performClick()
        composeTestRule.onAllNodes(isRoot()).onLast().captureRoboImage(filePath = "build/panda-rework-preview/06-picshare-status.png")
        composeTestRule.onNodeWithText("Speichern").performClick()
        composeTestRule.onNodeWithText("Liste öffnen").performClick()
        composeTestRule.onAllNodes(isRoot()).onLast().captureRoboImage(filePath = "build/panda-rework-preview/07-antwortliste.png")
    }

    @Test
    fun chatDraft() {
        composeTestRule.setContent {
            HarmonyTheme(darkTheme = true) {
                Box(Modifier.fillMaxSize().background(Color(0xFF100519))) {
                    ChatScreen(
                        messages = listOf(
                            ChatMessageEntity(1, "them", "Hey du 💕 wie war dein Tag?"),
                            ChatMessageEntity(2, "me", "Jetzt viel besser – ich schicke dir gleich ein Bild ☺️")
                        ),
                        partnerName = "J",
                        partnerAvatarPath = null,
                        onSendMessage = {},
                        onSendImage = {},
                        onReportUser = {}
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "build/panda-rework-preview/04-chat-bilder-melden.png")
    }

    @Test
    fun profileDraft() {
        composeTestRule.setContent {
            HarmonyTheme(darkTheme = true) {
                ProfileSheet(
                    profile = profile,
                    isEditProfileOpen = false,
                    onDismiss = {},
                    onToggleSimulator = {},
                    onOpenEditProfile = {},
                    onCloseEditProfile = {},
                    onSaveEditProfile = { _, _, _ -> },
                    onUpdateAvatar = { _, _ -> }
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "build/panda-rework-preview/05-profilbilder-ohne-ki.png")
    }
}
