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
import androidx.compose.ui.test.onNodeWithTag
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
import com.example.data.model.Question
import com.example.data.model.QuestionPack
import com.example.data.model.SharedPicEntity
import com.example.ui.ActivePackRun
import com.example.ui.components.AmbientBackground
import com.example.ui.components.HarmonyBottomNav
import com.example.ui.components.HarmonyTopBar
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CategoryRailCard
import com.example.ui.screens.GamesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PandaEitherOrScreen
import com.example.ui.screens.PaddingPackCard
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
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class PandaReworkScreenshotTest {
    @get:Rule val composeTestRule = createComposeRule()

    private val profile = ProfileEntity(userName = "Ralf", partnerName = "J")
    private val pandaImagePath = File("src/main/res/drawable-nodpi/panda_thinking_harmony.png").absolutePath

    @Test
    fun allowedCategoryIconsDraft() {
        composeTestRule.mainClock.autoAdvance = false
        val categoryIds = listOf("zeich", "zust", "lieber", "foto", "tief", "reden")
        composeTestRule.setContent {
            HarmonyTheme(darkTheme = true) {
                AmbientBackground {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        categoryIds.chunked(3).forEach { rowIds ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                rowIds.forEach { id ->
                                    CategoryRailCard(HarmonyPacksData.CATEGORIES.first { it.id == id }, onClick = {})
                                }
                            }
                        }
                    }
                }
            }
        }
        composeTestRule.mainClock.advanceTimeBy(2_800)
        composeTestRule.onRoot().captureRoboImage(filePath = "build/current-rework-preview/01-neue-spiele-icons.png")
    }

    @Test
    fun unansweredQuestionsDraft() {
        composeTestRule.setContent {
            HarmonyTheme(darkTheme = true) {
                AmbientBackground {
                    GamesScreen(
                        answers = listOf(AnswerEntity("zuhause", 0, "Antwort")),
                        packFilter = "all",
                        onSetFilter = {},
                        onCategoryClick = {},
                        onTopicClick = {},
                        onStartPack = {}
                    )
                }
            }
        }
        composeTestRule.onNodeWithText("Unbeantwortet").performClick()
        composeTestRule.onAllNodes(isRoot()).onLast().captureRoboImage(filePath = "build/current-rework-preview/02-unbeantwortete-fragen.png")
    }

    @Test
    fun videogamePackControllerDraft() {
        val pack = QuestionPack(
            id = "cj_videogame_quiz",
            title = "Bist du ein Videospiel-Guru?",
            tags = listOf("games", "quiz"),
            cat = "zust",
            topic = "hobbys",
            type = "quiz",
            questions = listOf(Question("Welches Spiel passt zu dir?"))
        )
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            HarmonyTheme(darkTheme = true) {
                AmbientBackground {
                    Box(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
                        PaddingPackCard(
                            appLanguage = "de",
                            pack = pack,
                            answers = emptyList(),
                            onStartPack = {},
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        composeTestRule.mainClock.advanceTimeBy(2_100)
        composeTestRule.onRoot().captureRoboImage(filePath = "build/current-rework-preview/05-videospiel-controller.png")
    }

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
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            HarmonyTheme(darkTheme = true) {
                AmbientBackground {
                    HomeScreen(
                        profile = profile,
                        answers = listOf(AnswerEntity("zuhause", 0, "Die Gemütlichkeit und Ruhe")),
                        sharedPics = listOf(SharedPicEntity(filePath = pandaImagePath, caption = "Du bist mein Lieblingsmensch 💕")),
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
        composeTestRule.mainClock.advanceTimeBy(100)
        composeTestRule.onNodeWithTag("picshare_manager_dialog").captureRoboImage(filePath = "build/current-rework-preview/03-picshare-widget-einstellungen.png")
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
    fun chatImageFullscreenDraft() {
        composeTestRule.setContent {
            HarmonyTheme(darkTheme = true) {
                Box(Modifier.fillMaxSize().background(Color(0xFF100519))) {
                    ChatScreen(
                        messages = listOf(ChatMessageEntity(91, "them", "Für dich 💕", imagePath = pandaImagePath)),
                        partnerName = "J",
                        partnerAvatarPath = null,
                        onSendMessage = {},
                        onSendImage = {},
                        onReportUser = {}
                    )
                }
            }
        }
        composeTestRule.onNodeWithTag("chat_image_91").performClick()
        composeTestRule.onAllNodes(isRoot()).onLast().captureRoboImage(filePath = "build/current-rework-preview/04-chat-vollbild.png")
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
