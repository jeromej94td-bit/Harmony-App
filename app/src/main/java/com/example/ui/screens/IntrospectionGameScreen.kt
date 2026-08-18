package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.AppLanguage
import com.example.ui.introspection.AudioPlaybackCard
import com.example.ui.introspection.ContinueOrRestartDialog
import com.example.ui.introspection.EyebrowCapsule
import com.example.ui.introspection.IntrospectionAnswer
import com.example.ui.introspection.IntrospectionColors
import com.example.ui.introspection.IntrospectionConstants
import com.example.ui.introspection.IntrospectionMediaController
import com.example.ui.introspection.IntrospectionPortal
import com.example.ui.introspection.IntrospectionProgress
import com.example.ui.introspection.IntrospectionStage
import com.example.ui.introspection.IntrospectionStore
import com.example.ui.introspection.IntrospectionStringKey
import com.example.ui.introspection.IntrospectionStrings
import com.example.ui.introspection.LeaveConfirmDialog
import com.example.ui.introspection.MysticBackdrop
import com.example.ui.introspection.MysticButton
import com.example.ui.introspection.MysticCard
import com.example.ui.introspection.MysticSecondaryButton
import com.example.ui.introspection.MysticTextField
import com.example.ui.introspection.RecordingVisualizer
import java.io.File

private enum class ScreenState {
    ENTRY,
    QUESTION,
    REVELATION,
    RESULTS
}

@Composable
fun IntrospectionExperienceScreen(
    appLanguage: String,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val appLang = remember(appLanguage) { AppLanguage.fromCode(appLanguage) }

    val store = remember { IntrospectionStore(context) }
    val mediaController = remember { IntrospectionMediaController(context, coroutineScope) }

    var progress by remember { mutableStateOf(store.load()) }
    var screenState by remember { mutableStateOf(ScreenState.ENTRY) }

    var showContinueDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showPermissionSettingsDialog by remember { mutableStateOf(false) }

    // Media states
    val isNarratorPlaying by mediaController.isNarratorPlaying.collectAsState()
    val isRecording by mediaController.isRecording.collectAsState()
    val recordingDurationMs by mediaController.recordingDurationMs.collectAsState()
    val isAnswerPlaying by mediaController.isAnswerPlaying.collectAsState()
    val answerProgress by mediaController.answerProgress.collectAsState()
    val activeAnswerStage by mediaController.activeAnswerStage.collectAsState()
    val mediaErrorMessage by mediaController.errorMessage.collectAsState()

    // Question input local states
    var inputMode by remember { mutableStateOf("text") } // "text" or "voice"
    var currentTextAnswer by remember { mutableStateOf("") }
    var currentRecordedFile by remember { mutableStateOf<File?>(null) }
    var permissionDeniedMessage by remember { mutableStateOf<String?>(null) }

    val combinedErrorMessage = permissionDeniedMessage ?: mediaErrorMessage

    // Auto-stop recording on leave / exit
    BackHandler {
        if (isRecording) {
            mediaController.stopRecording()
        }
        if (screenState != ScreenState.ENTRY && screenState != ScreenState.RESULTS) {
            showLeaveDialog = true
        } else {
            mediaController.releaseAll()
            onExit()
        }
    }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            permissionDeniedMessage = null
            val targetFile = store.recordingFile(progress.stage)
            currentRecordedFile = targetFile
            mediaController.startRecording(targetFile) { completedFile ->
                currentRecordedFile = completedFile
            }
        } else {
            permissionDeniedMessage = IntrospectionStrings.tr(
                IntrospectionStringKey.PERMISSION_DENIED_ERROR,
                appLang
            )
            showPermissionSettingsDialog = true
        }
    }

    // Initialize media / cleanup
    DisposableEffect(Unit) {
        mediaController.startBackgroundMusic()
        onDispose {
            mediaController.releaseAll()
        }
    }

    // When entering QUESTION / REVELATION / RESULTS, manage narrator
    LaunchedEffect(progress.stage, screenState) {
        if (screenState == ScreenState.QUESTION && progress.stage.isQuestion) {
            // Preload existing text/audio for stage if any
            when (val existing = progress.answers[progress.stage]) {
                is IntrospectionAnswer.Text -> {
                    currentTextAnswer = existing.value
                    inputMode = "text"
                }
                is IntrospectionAnswer.Audio -> {
                    val file = File(existing.filePath)
                    if (file.exists() && file.isFile) {
                        currentRecordedFile = file
                        inputMode = "voice"
                    }
                }
                null -> {
                    currentTextAnswer = ""
                    currentRecordedFile = null
                    inputMode = "text"
                }
            }
            mediaController.playNarratorForStage(progress.stage)
        } else if (screenState == ScreenState.REVELATION) {
            mediaController.playNarrator(com.example.R.raw.introspection_reveal) {
                // IMPORTANT: Revelation transition MUST only trigger onCompletion callback!
                val completedProgress = progress.finishRevelation()
                progress = completedProgress
                store.save(completedProgress)
                screenState = ScreenState.RESULTS
            }
        }
    }

    fun beginNewRun() {
        progress = store.clear()
        currentTextAnswer = ""
        currentRecordedFile = null
        inputMode = "text"
        screenState = ScreenState.QUESTION
        mediaController.startBackgroundMusic()
    }

    fun continueExistingRun() {
        screenState = if (progress.completed) ScreenState.RESULTS
        else if (progress.stage == IntrospectionStage.REVELATION) ScreenState.REVELATION
        else ScreenState.QUESTION
        mediaController.startBackgroundMusic()
    }

    fun submitCurrentAnswer() {
        val answer = if (inputMode == "voice") {
            currentRecordedFile?.let { file ->
                if (file.exists() && file.isFile && file.length() > 0) {
                    IntrospectionAnswer.Audio(file.absolutePath)
                } else null
            }
        } else {
            if (currentTextAnswer.isNotBlank()) {
                IntrospectionAnswer.Text(currentTextAnswer.trim())
            } else null
        } ?: return

        mediaController.stopNarrator()
        mediaController.stopAnswerAudio()

        val nextProgress = progress.advanceAfterAnswer(answer)
        progress = nextProgress
        store.save(nextProgress)

        currentTextAnswer = ""
        currentRecordedFile = null
        inputMode = "text"

        if (nextProgress.stage == IntrospectionStage.REVELATION) {
            screenState = ScreenState.REVELATION
        }
    }

    MysticBackdrop {
        AnimatedContent(
            targetState = screenState,
            transitionSpec = {
                (fadeIn(animationSpec = tween(420, easing = FastOutSlowInEasing)) +
                        slideInVertically(
                            animationSpec = tween(420, easing = FastOutSlowInEasing),
                            initialOffsetY = { 40 }
                        ))
                    .togetherWith(fadeOut(animationSpec = tween(180)))
            },
            label = "screenStateTransition"
        ) { state ->
            when (state) {
                ScreenState.ENTRY -> {
                    EntryScreen(
                        appLang = appLang,
                        onBack = {
                            mediaController.releaseAll()
                            onExit()
                        },
                        onStart = {
                            if (store.hasSavedProgress()) {
                                showContinueDialog = true
                            } else {
                                beginNewRun()
                            }
                        }
                    )
                }

                ScreenState.QUESTION -> {
                    QuestionScreen(
                        progress = progress,
                        appLang = appLang,
                        inputMode = inputMode,
                        textAnswer = currentTextAnswer,
                        recordedFile = currentRecordedFile,
                        isNarratorPlaying = isNarratorPlaying,
                        isRecording = isRecording,
                        recordingDurationMs = recordingDurationMs,
                        isAnswerPlaying = isAnswerPlaying,
                        answerProgress = answerProgress,
                        errorMessage = combinedErrorMessage,
                        onInputModeChange = {
                            inputMode = it
                            mediaController.clearErrorMessage()
                        },
                        onTextChange = {
                            currentTextAnswer = it
                            if (mediaErrorMessage != null) mediaController.clearErrorMessage()
                        },
                        onReplayNarrator = {
                            if (isNarratorPlaying) {
                                mediaController.stopNarrator()
                            } else {
                                mediaController.playNarratorForStage(progress.stage)
                            }
                        },
                        onStartRecord = {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                permissionDeniedMessage = null
                                val targetFile = store.recordingFile(progress.stage)
                                currentRecordedFile = targetFile
                                mediaController.startRecording(targetFile) { file ->
                                    currentRecordedFile = file
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        onStopRecord = {
                            mediaController.stopRecording()
                        },
                        onDiscardRecord = {
                            mediaController.discardRecording(currentRecordedFile)
                            currentRecordedFile = null
                        },
                        onPlayPauseAudio = {
                            currentRecordedFile?.let { file ->
                                if (isAnswerPlaying) {
                                    mediaController.stopAnswerAudio()
                                } else {
                                    mediaController.playAnswerAudio(file, progress.stage)
                                }
                            }
                        },
                        onConfirm = { submitCurrentAnswer() },
                        onBackRequest = {
                            if (isRecording) {
                                mediaController.stopRecording()
                            }
                            showLeaveDialog = true
                        }
                    )
                }

                ScreenState.REVELATION -> {
                    RevelationScreen(
                        appLang = appLang,
                        onBackRequest = {
                            showLeaveDialog = true
                        }
                    )
                }

                ScreenState.RESULTS -> {
                    ResultsScreen(
                        progress = progress,
                        appLang = appLang,
                        isAnswerPlaying = isAnswerPlaying,
                        activeStage = activeAnswerStage,
                        onPlayAnswer = { file, stage ->
                            if (isAnswerPlaying && activeAnswerStage == stage) {
                                mediaController.stopAnswerAudio()
                            } else {
                                mediaController.playAnswerAudio(file, stage)
                            }
                        },
                        onRestart = {
                            beginNewRun()
                        },
                        onFinish = {
                            mediaController.releaseAll()
                            onExit()
                        }
                    )
                }
            }
        }
    }

    // Frame 02: Continue or Restart Dialog
    if (showContinueDialog) {
        ContinueOrRestartDialog(
            onContinue = {
                showContinueDialog = false
                continueExistingRun()
            },
            onRestart = {
                showContinueDialog = false
                beginNewRun()
            },
            onDismiss = {
                showContinueDialog = false
            },
            appLanguage = appLang
        )
    }

    // Leave Confirm Dialog
    if (showLeaveDialog) {
        LeaveConfirmDialog(
            onConfirmLeave = {
                showLeaveDialog = false
                mediaController.releaseAll()
                onExit()
            },
            onDismiss = {
                showLeaveDialog = false
            },
            appLanguage = appLang
        )
    }

    // Permission Settings Dialog
    if (showPermissionSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionSettingsDialog = false },
            containerColor = IntrospectionColors.SurfaceHighlighted,
            title = {
                Text(
                    text = IntrospectionStrings.tr(
                        IntrospectionStringKey.MIC_PERMISSION_REQUIRED_TITLE,
                        appLang
                    ),
                    color = IntrospectionColors.PrimaryText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = IntrospectionStrings.tr(
                        IntrospectionStringKey.MIC_PERMISSION_REQUIRED_DESC,
                        appLang
                    ),
                    color = IntrospectionColors.SecondaryText
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionSettingsDialog = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        text = IntrospectionStrings.tr(
                            IntrospectionStringKey.OPEN_SETTINGS_BUTTON,
                            appLang
                        ),
                        color = IntrospectionColors.PrimaryPink,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionSettingsDialog = false }) {
                    Text(
                        text = IntrospectionStrings.tr(
                            IntrospectionStringKey.CANCEL_BUTTON,
                            appLang
                        ),
                        color = IntrospectionColors.SecondaryText
                    )
                }
            }
        )
    }
}

// --- Screen 01: Entry View ---

@Composable
private fun EntryScreen(
    appLang: AppLanguage,
    onBack: () -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("entry_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = IntrospectionStrings.tr(
                        IntrospectionStringKey.BACK_BUTTON_CD,
                        appLang
                    ),
                    tint = IntrospectionColors.PrimaryText
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Big Atmospheric Portal
        IntrospectionPortal(
            isRevelation = false,
            size = 240.dp
        )

        Spacer(Modifier.height(18.dp))

        EyebrowCapsule(
            text = IntrospectionStrings.tr(IntrospectionStringKey.ENTRY_EYEBROW, appLang)
        )

        Spacer(Modifier.height(14.dp))

        Text(
            text = IntrospectionConstants.WIZARD_EMOJI,
            fontSize = 38.sp
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = IntrospectionStrings.tr(IntrospectionStringKey.ENTRY_TITLE, appLang),
            color = IntrospectionColors.PrimaryText,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )

        Spacer(Modifier.height(14.dp))

        Text(
            text = IntrospectionStrings.tr(IntrospectionStringKey.ENTRY_SUBTITLE, appLang),
            color = IntrospectionColors.SecondaryText,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(Modifier.height(36.dp))

        MysticButton(
            text = IntrospectionStrings.tr(IntrospectionStringKey.ENTRY_START_BUTTON, appLang),
            onClick = onStart,
            leadingEmoji = IntrospectionConstants.SPARKLES_EMOJI,
            testTag = "entry_start_button"
        )

        Spacer(Modifier.height(24.dp))
    }
}

// --- Screen 03 / 04 / 05 / 06: Question View ---

@Composable
private fun QuestionScreen(
    progress: IntrospectionProgress,
    appLang: AppLanguage,
    inputMode: String,
    textAnswer: String,
    recordedFile: File?,
    isNarratorPlaying: Boolean,
    isRecording: Boolean,
    recordingDurationMs: Long,
    isAnswerPlaying: Boolean,
    answerProgress: Float,
    errorMessage: String?,
    onInputModeChange: (String) -> Unit,
    onTextChange: (String) -> Unit,
    onReplayNarrator: () -> Unit,
    onStartRecord: () -> Unit,
    onStopRecord: () -> Unit,
    onDiscardRecord: () -> Unit,
    onPlayPauseAudio: () -> Unit,
    onConfirm: () -> Unit,
    onBackRequest: () -> Unit
) {
    val step = when (progress.stage) {
        IntrospectionStage.COLOR -> 1
        IntrospectionStage.ANIMAL -> 2
        IntrospectionStage.WATER -> 3
        else -> 4
    }

    val (stageTitleKey, stageQuestionKey, stagePromptKey) = when (progress.stage) {
        IntrospectionStage.COLOR -> Triple(
            IntrospectionStringKey.STAGE_COLOR_TITLE,
            IntrospectionStringKey.STAGE_COLOR_QUESTION,
            IntrospectionStringKey.STAGE_COLOR_PROMPT
        )
        IntrospectionStage.ANIMAL -> Triple(
            IntrospectionStringKey.STAGE_ANIMAL_TITLE,
            IntrospectionStringKey.STAGE_ANIMAL_QUESTION,
            IntrospectionStringKey.STAGE_ANIMAL_PROMPT
        )
        else -> Triple(
            IntrospectionStringKey.STAGE_WATER_TITLE,
            IntrospectionStringKey.STAGE_WATER_QUESTION,
            IntrospectionStringKey.STAGE_WATER_PROMPT
        )
    }

    val canConfirm = !isNarratorPlaying && !isRecording && (
            (inputMode == "text" && textAnswer.isNotBlank()) ||
                    (inputMode == "voice" && recordedFile?.let { it.exists() && it.isFile && it.length() > 0 } == true)
            )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackRequest,
                modifier = Modifier.testTag("question_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = IntrospectionStrings.tr(
                        IntrospectionStringKey.LEAVE_BUTTON_CD,
                        appLang
                    ),
                    tint = IntrospectionColors.PrimaryText
                )
            }

            Spacer(Modifier.width(8.dp))

            // Progress Bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(IntrospectionColors.SurfaceHighlighted)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(step / 3f)
                        .height(6.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    IntrospectionColors.PrimaryPink,
                                    IntrospectionColors.Magenta
                                )
                            )
                        )
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = String.format(
                    IntrospectionStrings.tr(IntrospectionStringKey.STEP_OF_THREE, appLang),
                    step
                ),
                color = IntrospectionColors.SecondaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(8.dp))

        // Centered Portal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            IntrospectionPortal(
                isRevelation = false,
                size = 170.dp
            )
        }

        Spacer(Modifier.height(12.dp))

        // Title and Question
        Text(
            text = IntrospectionStrings.tr(stageTitleKey, appLang).uppercase(),
            color = IntrospectionColors.PrimaryPink,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = IntrospectionStrings.tr(stageQuestionKey, appLang),
            color = IntrospectionColors.PrimaryText,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 32.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = IntrospectionStrings.tr(stagePromptKey, appLang),
            color = IntrospectionColors.SecondaryText,
            fontSize = 15.sp,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(16.dp))

        // Narrator status badge with interactive replay / stop capability
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                onClick = onReplayNarrator,
                shape = RoundedCornerShape(50),
                color = if (isNarratorPlaying) IntrospectionColors.PrimaryPink.copy(alpha = 0.22f)
                else IntrospectionColors.SurfaceHighlighted.copy(alpha = 0.5f),
                border = BorderStroke(
                    1.dp,
                    if (isNarratorPlaying) IntrospectionColors.PrimaryPink
                    else IntrospectionColors.PortalViolet.copy(alpha = 0.4f)
                ),
                modifier = Modifier.testTag("narrator_audio_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isNarratorPlaying) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (isNarratorPlaying) IntrospectionColors.PeachLight else IntrospectionColors.SecondaryText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isNarratorPlaying) {
                            IntrospectionStrings.tr(IntrospectionStringKey.NARRATOR_PLAYING_BADGE, appLang)
                        } else {
                            IntrospectionStrings.tr(IntrospectionStringKey.PLAY_NARRATOR_BUTTON, appLang)
                        },
                        color = if (isNarratorPlaying) IntrospectionColors.PeachLight else IntrospectionColors.SecondaryText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Mode Switcher Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Text Tab
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(14.dp),
                color = if (inputMode == "text") IntrospectionColors.PortalViolet
                else IntrospectionColors.SurfaceDark,
                border = BorderStroke(
                    1.dp,
                    if (inputMode == "text") IntrospectionColors.PrimaryPink.copy(alpha = 0.8f)
                    else IntrospectionColors.PortalViolet.copy(alpha = 0.3f)
                ),
                onClick = { onInputModeChange("text") }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = IntrospectionStrings.tr(IntrospectionStringKey.TAB_TEXT, appLang),
                        color = IntrospectionColors.PrimaryText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            // Voice Tab
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(14.dp),
                color = if (inputMode == "voice") IntrospectionColors.PortalViolet
                else IntrospectionColors.SurfaceDark,
                border = BorderStroke(
                    1.dp,
                    if (inputMode == "voice") IntrospectionColors.PrimaryPink.copy(alpha = 0.8f)
                    else IntrospectionColors.PortalViolet.copy(alpha = 0.3f)
                ),
                onClick = { onInputModeChange("voice") }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = IntrospectionStrings.tr(IntrospectionStringKey.TAB_VOICE, appLang),
                        color = IntrospectionColors.PrimaryText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Input Content
        if (inputMode == "text") {
            MysticTextField(
                value = textAnswer,
                onValueChange = onTextChange,
                placeholder = IntrospectionStrings.tr(
                    IntrospectionStringKey.TEXT_INPUT_PLACEHOLDER,
                    appLang
                )
            )
        } else {
            // Voice Mode
            when {
                isRecording -> {
                    // Frame 04: Active Recording
                    RecordingVisualizer(
                        durationMs = recordingDurationMs,
                        onStop = onStopRecord,
                        onDiscard = onDiscardRecord,
                        appLanguage = appLang
                    )
                }

                recordedFile != null -> {
                    // Frame 05: Audio Recorded
                    AudioPlaybackCard(
                        isPlaying = isAnswerPlaying,
                        progress = answerProgress,
                        onPlayPause = onPlayPauseAudio,
                        onRecordAgain = onDiscardRecord,
                        appLanguage = appLang
                    )
                }

                else -> {
                    // Tap to Record Prompt
                    MysticCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = IntrospectionColors.SurfaceDark
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.radialGradient(
                                            listOf(
                                                IntrospectionColors.PortalViolet,
                                                IntrospectionColors.DeepViolet
                                            )
                                        )
                                    )
                                    .clickable(onClick = onStartRecord)
                                    .testTag("start_record_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = IntrospectionStrings.tr(
                                        IntrospectionStringKey.RECORD_BUTTON_CD,
                                        appLang
                                    ),
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = IntrospectionStrings.tr(
                                    IntrospectionStringKey.RECORD_BUTTON_CD,
                                    appLang
                                ),
                                color = IntrospectionColors.PrimaryText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        if (errorMessage != null) {
            Spacer(Modifier.height(14.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0x33FF5252),
                border = BorderStroke(1.dp, Color(0x88FF5252))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Hinweis",
                        tint = Color(0xFFFF6B6B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = errorMessage,
                        color = Color(0xFFFFD0D0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Confirm Button
        MysticButton(
            text = IntrospectionStrings.tr(
                IntrospectionStringKey.CONFIRM_ANSWER_BUTTON,
                appLang
            ),
            onClick = onConfirm,
            enabled = canConfirm,
            testTag = "confirm_answer_button"
        )

        Spacer(Modifier.height(24.dp))
    }
}

// --- Screen 07: Revelation View ---

@Composable
private fun RevelationScreen(
    appLang: AppLanguage,
    onBackRequest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = onBackRequest,
                modifier = Modifier.testTag("revelation_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = IntrospectionStrings.tr(
                        IntrospectionStringKey.LEAVE_BUTTON_CD,
                        appLang
                    ),
                    tint = IntrospectionColors.PrimaryText
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Heightened Glow Portal with faster particle motion
        IntrospectionPortal(
            isRevelation = true,
            size = 260.dp
        )

        Spacer(Modifier.height(30.dp))

        Text(
            text = IntrospectionStrings.tr(IntrospectionStringKey.REVELATION_TITLE, appLang),
            color = IntrospectionColors.PrimaryText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(14.dp))

        Text(
            text = IntrospectionStrings.tr(IntrospectionStringKey.REVELATION_SUBTITLE, appLang),
            color = IntrospectionColors.SecondaryText,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(24.dp))
    }
}

// --- Screen 08: Results View ---

@Composable
private fun ResultsScreen(
    progress: IntrospectionProgress,
    appLang: AppLanguage,
    isAnswerPlaying: Boolean,
    activeStage: IntrospectionStage?,
    onPlayAnswer: (File, IntrospectionStage) -> Unit,
    onRestart: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = IntrospectionConstants.WIZARD_EMOJI,
            fontSize = 40.sp
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = IntrospectionStrings.tr(IntrospectionStringKey.RESULTS_TITLE, appLang),
            color = IntrospectionColors.PrimaryText,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = IntrospectionStrings.tr(IntrospectionStringKey.RESULTS_SUBTITLE, appLang),
            color = IntrospectionColors.SecondaryText,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        // 3 Result Cards
        listOf(
            Triple(
                IntrospectionStage.COLOR,
                IntrospectionStrings.tr(IntrospectionStringKey.STAGE_COLOR_TITLE, appLang),
                IntrospectionStrings.tr(IntrospectionStringKey.RESULTS_COLOR_MEANING, appLang)
            ),
            Triple(
                IntrospectionStage.ANIMAL,
                IntrospectionStrings.tr(IntrospectionStringKey.STAGE_ANIMAL_TITLE, appLang),
                IntrospectionStrings.tr(IntrospectionStringKey.RESULTS_ANIMAL_MEANING, appLang)
            ),
            Triple(
                IntrospectionStage.WATER,
                IntrospectionStrings.tr(IntrospectionStringKey.STAGE_WATER_TITLE, appLang),
                IntrospectionStrings.tr(IntrospectionStringKey.RESULTS_WATER_MEANING, appLang)
            )
        ).forEachIndexed { index, (stage, stageTitle, meaning) ->
            ResultCard(
                index = index + 1,
                title = stageTitle,
                meaning = meaning,
                answer = progress.answers[stage],
                appLang = appLang,
                isPlaying = isAnswerPlaying && activeStage == stage,
                onPlayAudio = { file -> onPlayAnswer(file, stage) }
            )
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.height(16.dp))

        MysticSecondaryButton(
            text = IntrospectionStrings.tr(
                IntrospectionStringKey.START_NEW_JOURNEY_BUTTON,
                appLang
            ),
            onClick = onRestart,
            testTag = "results_restart_button"
        )

        Spacer(Modifier.height(12.dp))

        MysticButton(
            text = IntrospectionStrings.tr(
                IntrospectionStringKey.FINISH_JOURNEY_BUTTON,
                appLang
            ),
            onClick = onFinish,
            testTag = "results_finish_button"
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ResultCard(
    index: Int,
    title: String,
    meaning: String,
    answer: IntrospectionAnswer?,
    appLang: AppLanguage,
    isPlaying: Boolean,
    onPlayAudio: (File) -> Unit
) {
    MysticCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = IntrospectionColors.SurfaceDark,
        borderColor = IntrospectionColors.PortalViolet.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "$index. $title",
                color = IntrospectionColors.PrimaryPink,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            when (answer) {
                is IntrospectionAnswer.Text -> {
                    Text(
                        text = "„${answer.value}“",
                        color = IntrospectionColors.PrimaryText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp
                    )
                }

                is IntrospectionAnswer.Audio -> {
                    val file = remember(answer.filePath) { File(answer.filePath) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onPlayAudio(file) },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(IntrospectionColors.SurfaceHighlighted)
                                .testTag("result_play_audio_button_$index")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) {
                                    IntrospectionStrings.tr(
                                        IntrospectionStringKey.PAUSE_AUDIO_ANSWER_CD,
                                        appLang
                                    )
                                } else {
                                    IntrospectionStrings.tr(
                                        IntrospectionStringKey.PLAY_AUDIO_ANSWER_CD,
                                        appLang
                                    )
                                },
                                tint = IntrospectionColors.PrimaryPink
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = IntrospectionStrings.tr(
                                IntrospectionStringKey.AUDIO_ANSWER_LABEL,
                                appLang
                            ),
                            color = IntrospectionColors.PrimaryText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                null -> {
                    Text(
                        text = "-",
                        color = IntrospectionColors.SecondaryText,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(IntrospectionColors.SurfaceHighlighted)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = meaning,
                color = IntrospectionColors.SecondaryText,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}
