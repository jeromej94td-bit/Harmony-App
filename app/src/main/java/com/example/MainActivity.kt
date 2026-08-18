package com.example

import android.os.Bundle
import android.graphics.Color as AndroidColor
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppLanguage
import com.example.ui.HarmonyViewModel
import com.example.ui.LocalAppLanguage
import com.example.ui.components.AmbientBackground
import com.example.ui.components.HarmonyBottomNav
import com.example.ui.components.HarmonyToast
import com.example.ui.components.HarmonyTopBar
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DevStudioScreen
import com.example.ui.screens.GamesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IntrospectionExperienceScreen
import com.example.ui.screens.MomentsScreen
import com.example.ui.screens.PackListScreen
import com.example.ui.screens.ProfileSheet
import com.example.ui.screens.QuizRunnerScreen
import com.example.ui.theme.HarmonyTheme

class MainActivity : ComponentActivity() {

    private val viewModel: HarmonyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.navigationBarColor = AndroidColor.TRANSPARENT
        window.statusBarColor = AndroidColor.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightNavigationBars = false
            isAppearanceLightStatusBars = false
        }
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val currentLanguage = AppLanguage.fromCode(uiState.appLanguage)
            CompositionLocalProvider(LocalAppLanguage provides currentLanguage) {
                HarmonyTheme(darkTheme = uiState.isDarkMode) {
                    HarmonyApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun HarmonyApp(viewModel: HarmonyViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isIntrospectionOpen by remember { mutableStateOf(false) }

    val isQuizActive = uiState.activeRun != null
    val isSheetOrDialogActive = uiState.isProfileSheetOpen || uiState.isAddMomentOpen
    val isNotHomeTab = uiState.selectedTab != 0

    val canHandleBack = isIntrospectionOpen || isQuizActive || isSheetOrDialogActive || isNotHomeTab

    BackHandler(enabled = canHandleBack) {
        when {
            isIntrospectionOpen -> {
                isIntrospectionOpen = false
            }
            isQuizActive -> {
                if (uiState.isExitConfirmOpen) {
                    viewModel.closeExitConfirm()
                } else if (uiState.isOwnAnswerDialogOpen) {
                    viewModel.closeOwnAnswerDialog()
                } else {
                    viewModel.askExitRun()
                }
            }
            uiState.isProfileSheetOpen -> {
                viewModel.closeProfileSheet()
            }
            uiState.isAddMomentOpen -> {
                viewModel.closeAddMomentDialog()
            }
            uiState.selectedTab == 6 -> { // PackListScreen
                viewModel.selectTab(1) // Back to GamesScreen
            }
            uiState.selectedTab != 0 -> {
                viewModel.selectTab(0) // Back to HomeScreen
            }
        }
    }

    AmbientBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                if (!isQuizActive && !isIntrospectionOpen) {
                    HarmonyTopBar(
                        userName = uiState.profile.userName,
                        partnerName = uiState.profile.partnerName,
                        onProfileClick = { viewModel.openProfileSheet() },
                        onRefresh = { viewModel.refreshData() }
                    )
                }
            },
            bottomBar = {
                if (!isQuizActive && !isIntrospectionOpen) {
                    val navSelectedTab = when (uiState.selectedTab) {
                        6 -> 1 // When inside PackListScreen, highlight Spiele tab
                        else -> uiState.selectedTab
                    }
                    HarmonyBottomNav(
                        selectedTab = navSelectedTab,
                        onTabSelected = { tab ->
                            if (tab == 4) {
                                viewModel.openProfileSheet()
                            } else {
                                viewModel.selectTab(tab)
                            }
                        },
                        appLanguage = uiState.appLanguage
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Main Content depending on selected Tab
                when (uiState.selectedTab) {
                    0 -> HomeScreen(
                        profile = uiState.profile,
                        answers = uiState.answers,
                        stats = uiState.stats,
                        isRefreshing = uiState.isRefreshing,
                        appLanguage = uiState.appLanguage,
                        onRefresh = { viewModel.refreshData() },
                        onStartPack = { packId -> viewModel.startPack(packId) },
                        onSendWidget = { name, emoji -> viewModel.sendWidget(name, emoji) }
                    )

                    1 -> GamesScreen(
                        answers = uiState.answers,
                        packFilter = uiState.packFilter,
                        appLanguage = uiState.appLanguage,
                        onSetFilter = { filter -> viewModel.setPackFilter(filter) },
                        onCategoryClick = { catId ->
                            if (catId == "unterbewusstsein") {
                                isIntrospectionOpen = true
                            } else {
                                viewModel.openCategory(catId)
                            }
                        },
                        onTopicClick = { topicId -> viewModel.openTopic(topicId) },
                        onStartPack = { packId -> viewModel.startPack(packId) }
                    )

                    2 -> ChatScreen(
                        messages = uiState.messages,
                        partnerName = uiState.profile.partnerName,
                        gfkPanelOpen = uiState.gfkPanelOpen,
                        gfkLoading = uiState.gfkLoading,
                        gfkResult = uiState.gfkResult,
                        appLanguage = uiState.appLanguage,
                        onSendMessage = { text -> viewModel.sendChatMessage(text) },
                        onToggleGfkPanel = { viewModel.toggleGfkPanel() },
                        onRunGfk = { draft -> viewModel.runGfk(draft) }
                    )

                    3 -> MomentsScreen(
                        moments = uiState.moments,
                        profile = uiState.profile,
                        isAddMomentOpen = uiState.isAddMomentOpen,
                        appLanguage = uiState.appLanguage,
                        onOpenAddMoment = { viewModel.openAddMomentDialog() },
                        onCloseAddMoment = { viewModel.closeAddMomentDialog() },
                        onAddMoment = { title, content -> viewModel.addMoment(title, content) }
                    )

                    5 -> DevStudioScreen(
                        onStartPack = { packId -> viewModel.startPack(packId) },
                        onShowToast = { msg -> viewModel.showToast(msg) }
                    )

                    6 -> PackListScreen(
                        answers = uiState.answers,
                        selectedTopicId = uiState.selectedTopicId,
                        selectedCategoryId = uiState.selectedCategoryId,
                        packFilter = uiState.packFilter,
                        appLanguage = uiState.appLanguage,
                        onSetFilter = { filter -> viewModel.setPackFilter(filter) },
                        onStartPack = { packId -> viewModel.startPack(packId) },
                        onClose = { viewModel.selectTab(1) }
                    )
                }

                // Toast Notification Overlay
                HarmonyToast(
                    message = uiState.toastMessage,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )

                // Profile Sheet
                if (uiState.isProfileSheetOpen) {
                    val currentLanguage = AppLanguage.fromCode(uiState.appLanguage)
                    ProfileSheet(
                        profile = uiState.profile,
                        coachLoading = uiState.coachLoading,
                        coachResult = uiState.coachResult,
                        dateIdeasLoading = uiState.dateIdeasLoading,
                        dateIdeasResult = uiState.dateIdeasResult,
                        isEditProfileOpen = uiState.isEditProfileOpen,
                        isDarkMode = uiState.isDarkMode,
                        onToggleDarkMode = { enabled -> viewModel.toggleDarkMode(enabled) },
                        language = currentLanguage,
                        onLanguageChange = { lang -> viewModel.setLanguage(lang.code) },
                        onDismiss = { viewModel.closeProfileSheet() },
                        onToggleSimulator = { viewModel.toggleSimulator() },
                        onOpenEditProfile = { viewModel.openEditProfile() },
                        onCloseEditProfile = { viewModel.closeEditProfile() },
                        onSaveEditProfile = { u, p, s -> viewModel.saveEditProfile(u, p, s) },
                        onRunCoach = { viewModel.runCoach() },
                        onRunDateIdeas = { wishes -> viewModel.runDateIdeas(wishes) },
                        onOpenDevStudio = { viewModel.selectTab(5) }
                    )
                }

                // Full-Screen Quiz Runner Overlay
                uiState.activeRun?.let { activeRun ->
                    QuizRunnerScreen(
                        activeRun = activeRun,
                        profile = uiState.profile,
                        isExitConfirmOpen = uiState.isExitConfirmOpen,
                        isOwnAnswerDialogOpen = uiState.isOwnAnswerDialogOpen,
                        appLanguage = uiState.appLanguage,
                        onPickAnswer = { optText -> viewModel.pickAnswer(optText) },
                        onPickTot = { optionText -> viewModel.pickAnswer(optionText) },
                        onNextStep = { viewModel.nextStep() },
                        onAskExit = { viewModel.askExitRun() },
                        onCloseExitConfirm = { viewModel.closeExitConfirm() },
                        onCloseRunner = { viewModel.closeRunner() },
                        onOpenOwnAnswerDialog = { idx, mode -> viewModel.openOwnAnswerDialog(idx, mode) },
                        onCloseOwnAnswerDialog = { viewModel.closeOwnAnswerDialog() },
                        onSaveOwnAnswer = { ansText -> viewModel.saveOwnAnswer(ansText) }
                    )
                }

                if (isIntrospectionOpen) {
                    IntrospectionExperienceScreen(
                        appLanguage = uiState.appLanguage,
                        onExit = { isIntrospectionOpen = false }
                    )
                }
            }
        }
    }
}
