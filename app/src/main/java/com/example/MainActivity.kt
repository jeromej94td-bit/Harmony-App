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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel as composeViewModel
import com.example.data.OkHttpLinkPreviewResolver
import com.example.data.db.HarmonyDatabase
import com.example.data.repository.RoomMemoryRepository
import com.example.ui.AppLanguage
import com.example.ui.HarmonyViewModel
import com.example.ui.LocalAppLanguage
import com.example.ui.memory.MemoryViewModel
import com.example.ui.memory.MemoryViewModelFactory
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
import com.example.ui.screens.MemoryEditorSheet
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.PackListScreen
import com.example.ui.screens.PANDA_EITHER_OR_PACK_ID
import com.example.ui.screens.PandaEitherOrScreen
import com.example.ui.screens.ProfileSheet
import com.example.ui.screens.QuizRunnerScreen
import com.example.ui.theme.HarmonyTheme
import com.example.widget.PicShareWidgetProvider

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
            CompositionLocalProvider(
                LocalAppLanguage provides currentLanguage,
                LocalLayoutDirection provides if (currentLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
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
    val context = LocalContext.current
    val memoryFactory = remember(context.applicationContext) {
        MemoryViewModelFactory(
            repository = RoomMemoryRepository(HarmonyDatabase.getInstance(context.applicationContext)),
            linkPreviewResolver = OkHttpLinkPreviewResolver()
        )
    }
    val memoryViewModel: MemoryViewModel = composeViewModel(factory = memoryFactory)
    val memoryState by memoryViewModel.uiState.collectAsStateWithLifecycle()
    var isIntrospectionOpen by remember { mutableStateOf(false) }
    var isPandaEitherOrOpen by remember { mutableStateOf(false) }

    fun openPack(packId: String) {
        if (packId == PANDA_EITHER_OR_PACK_ID) {
            isPandaEitherOrOpen = true
        } else {
            viewModel.startPack(packId)
        }
    }

    val isQuizActive = uiState.activeRun != null
    val isMemoryOverlayActive = memoryState.editorMode != null ||
        memoryState.pendingDeleteEntryIds.isNotEmpty() || memoryState.selectionMode
    val isSheetOrDialogActive = uiState.isProfileSheetOpen || uiState.isAddMomentOpen || isMemoryOverlayActive
    val isNotHomeTab = uiState.selectedTab != 0

    val canHandleBack = isIntrospectionOpen || isPandaEitherOrOpen || isQuizActive || isSheetOrDialogActive || isNotHomeTab

    BackHandler(enabled = canHandleBack) {
        when {
            isIntrospectionOpen -> {
                isIntrospectionOpen = false
            }
            isPandaEitherOrOpen -> {
                isPandaEitherOrOpen = false
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
            memoryState.pendingDeleteEntryIds.isNotEmpty() -> {
                memoryViewModel.dismissPermanentDelete()
            }
            memoryState.editorMode != null -> {
                memoryViewModel.closeEditor()
            }
            memoryState.selectionMode -> {
                memoryViewModel.clearSelection()
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
                if (!isQuizActive && !isIntrospectionOpen && !isPandaEitherOrOpen) {
                    HarmonyTopBar(
                        userName = uiState.profile.userName,
                        partnerName = uiState.profile.partnerName,
                        userAvatarPath = uiState.profile.userAvatarPath,
                        partnerAvatarPath = uiState.profile.partnerAvatarPath,
                        onProfileClick = { viewModel.openProfileSheet() },
                        onRefresh = { viewModel.refreshData() }
                    )
                }
            },
            bottomBar = {
                if (!isQuizActive && !isIntrospectionOpen && !isPandaEitherOrOpen) {
                    val navSelectedTab = when (uiState.selectedTab) {
                        6 -> 1 // When inside PackListScreen, highlight Spiele tab
                        else -> uiState.selectedTab
                    }
                    HarmonyBottomNav(
                        selectedTab = navSelectedTab,
                        onTabSelected = { tab -> viewModel.selectTab(tab) },
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
                        sharedPics = uiState.sharedPics,
                        stats = uiState.stats,
                        appLanguage = uiState.appLanguage,
                        onStartPack = { packId -> openPack(packId) },
                        onAddSharedPictures = { uris, addedBy -> viewModel.addSharedPictures(uris, addedBy) },
                        onUpdateSharedPicture = { pic -> viewModel.updateSharedPicture(pic) },
                        onPinWidget = {
                            val requested = PicShareWidgetProvider.requestPin(context)
                            viewModel.showToast(if (requested) "Widget-Auswahl geöffnet" else "Widget bitte über den Startbildschirm hinzufügen")
                        }
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
                        onStartPack = { packId -> openPack(packId) }
                    )

                    2 -> ChatScreen(
                        messages = uiState.messages,
                        partnerName = uiState.profile.partnerName,
                        partnerAvatarPath = uiState.profile.partnerAvatarPath,
                        appLanguage = uiState.appLanguage,
                        onSendMessage = { text -> viewModel.sendChatMessage(text) },
                        onSendImage = { uri -> viewModel.sendChatImage(uri) },
                        onReportUser = { viewModel.reportPartner() }
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

                    4 -> {
                        MemoryScreen(
                            state = memoryState,
                            appLanguage = uiState.appLanguage,
                            userName = uiState.profile.userName,
                            partnerName = uiState.profile.partnerName,
                            userAvatarPath = uiState.profile.userAvatarPath,
                            partnerAvatarPath = uiState.profile.partnerAvatarPath,
                            onSelectTab = memoryViewModel::selectTab,
                            onQueryChange = memoryViewModel::setQuery,
                            onCategoryFilter = memoryViewModel::setCategoryFilter,
                            onOpenEditor = memoryViewModel::openEditor,
                            onStartSelection = memoryViewModel::startSelection,
                            onToggleEntrySelection = memoryViewModel::toggleEntrySelection,
                            onSelectAllEntries = memoryViewModel::selectAllVisibleEntries,
                            onClearSelection = memoryViewModel::clearSelection,
                            onDeleteSelectedRequest = memoryViewModel::requestSelectedDelete,
                            onComplete = memoryViewModel::complete,
                            onRestore = memoryViewModel::restore,
                            onRetryPreview = memoryViewModel::retryPreview,
                            onDeleteRequest = memoryViewModel::requestPermanentDelete,
                            onDeleteConfirm = memoryViewModel::confirmPermanentDelete,
                            onDeleteDismiss = memoryViewModel::dismissPermanentDelete,
                            onCreateCategory = memoryViewModel::createCategory,
                            onUpdateCategory = memoryViewModel::updateCategory,
                            onDeleteCategory = memoryViewModel::deleteCategory
                        )

                        memoryState.editorMode?.let { editorMode ->
                            MemoryEditorSheet(
                                mode = editorMode,
                                categories = memoryState.categories,
                                appLanguage = uiState.appLanguage,
                                onModeChange = { mode ->
                                    memoryViewModel.openEditor(mode, memoryState.editorEntryId)
                                },
                                onDismiss = memoryViewModel::closeEditor,
                                onSaveNote = { entryId, categoryId, title, body ->
                                    memoryViewModel.saveNote(entryId, categoryId, title, body)
                                    memoryViewModel.closeEditor()
                                },
                                onSaveList = { categoryId, lines ->
                                    memoryViewModel.saveList(categoryId, lines)
                                    memoryViewModel.closeEditor()
                                },
                                onSaveLink = { entryId, categoryId, url, note ->
                                    memoryViewModel.saveLink(entryId, categoryId, url, note)
                                    memoryViewModel.closeEditor()
                                },
                                initialEntry = memoryState.visibleEntries
                                    .firstOrNull { it.entity.id == memoryState.editorEntryId }
                                    ?.entity
                            )
                        }
                    }

                    5 -> DevStudioScreen(
                        onStartPack = { packId -> openPack(packId) },
                        onShowToast = { msg -> viewModel.showToast(msg) }
                    )

                    6 -> PackListScreen(
                        answers = uiState.answers,
                        selectedTopicId = uiState.selectedTopicId,
                        selectedCategoryId = uiState.selectedCategoryId,
                        packFilter = uiState.packFilter,
                        appLanguage = uiState.appLanguage,
                        onSetFilter = { filter -> viewModel.setPackFilter(filter) },
                        onStartPack = { packId -> openPack(packId) },
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
                        onUpdateAvatar = { uri, isUser -> viewModel.updateProfileAvatar(uri, isUser) },
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

                if (isPandaEitherOrOpen) {
                    PandaEitherOrScreen(
                        profile = uiState.profile,
                        answers = uiState.answers,
                        appLanguage = uiState.appLanguage,
                        onSaveAnswer = { questionIndex, userChoice, partnerChoice ->
                            viewModel.saveEitherOrAnswer(questionIndex, userChoice, partnerChoice)
                        },
                        onExit = { isPandaEitherOrOpen = false }
                    )
                }
            }
        }
    }
}
