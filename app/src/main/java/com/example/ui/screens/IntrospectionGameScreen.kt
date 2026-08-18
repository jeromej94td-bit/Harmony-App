package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.ui.introspection.IntrospectionAnswer
import com.example.ui.introspection.IntrospectionProgress
import com.example.ui.introspection.IntrospectionStage
import com.example.ui.introspection.IntrospectionStore
import com.example.ui.theme.HarmonyPink
import com.example.ui.theme.HarmonyPinkSoft
import java.io.File

private val MysticBackground = Color(0xFF08030E)
private val MysticSurface = Color(0xC7251633)
private val MysticPurple = Color(0xFF9D4EDD)
private val MysticViolet = Color(0xFF6A1B9A)
private val MysticText = Color(0xFFF8F1FF)
private val MysticMuted = Color(0xFFC9B6D5)

private fun prepareRawPlayer(context: Context, resource: Int, looping: Boolean = false): MediaPlayer? =
    runCatching {
        val descriptor = context.resources.openRawResourceFd(resource)
            ?: error("Audio resource $resource is not seekable")
        MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
            descriptor.close()
            isLooping = looping
            prepare()
        }
    }.onFailure { it.printStackTrace() }.getOrNull()

@Composable
fun IntrospectionExperienceScreen(appLanguage: String, onExit: () -> Unit) {
    val context = LocalContext.current
    val store = remember { IntrospectionStore(context) }
    var progress by remember { mutableStateOf(store.load()) }
    var showGame by remember { mutableStateOf(progress.hasStarted || progress.completed) }
    var showResumeDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        if (showGame) showExitDialog = true else onExit()
    }

    fun beginNew() {
        progress = store.clear()
        showGame = true
    }

    if (!showGame) {
        IntrospectionHub(
            appLanguage = appLanguage,
            hasSavedRun = progress.hasStarted || progress.completed,
            onBack = onExit,
            onStart = {
                if (progress.hasStarted || progress.completed) showResumeDialog = true else beginNew()
            }
        )
    } else {
        GuidedIntrospection(
            appLanguage = appLanguage,
            initialProgress = progress,
            store = store,
            onProgress = {
                progress = it
                store.save(it)
            },
            onExitRequest = { showExitDialog = true }
        )
    }

    if (showResumeDialog) {
        AlertDialog(
            onDismissRequest = { showResumeDialog = false },
            title = { Text(if (progress.completed) m(appLanguage, "Dein Ergebnis ist gespeichert", "Your result is saved", "Il tuo risultato è salvato") else m(appLanguage, "Deine Reise wartet", "Your journey is waiting", "Il tuo viaggio ti aspetta")) },
            text = { Text(if (progress.completed) m(appLanguage, "Möchtest du dein Ergebnis wieder ansehen oder neu beginnen?", "Would you like to view your result or begin again?", "Vuoi rivedere il risultato o ricominciare?") else m(appLanguage, "Möchtest du dort weitermachen, wo du aufgehört hast?", "Would you like to continue where you left off?", "Vuoi continuare da dove hai interrotto?")) },
            confirmButton = {
                TextButton(onClick = { showResumeDialog = false; showGame = true }) {
                    Text(if (progress.completed) m(appLanguage, "Ergebnis ansehen", "View result", "Vedi risultato") else m(appLanguage, "Fortsetzen", "Continue", "Continua"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResumeDialog = false; beginNew() }) { Text(m(appLanguage, "Neu beginnen", "Start over", "Ricomincia")) }
            }
        )
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(m(appLanguage, "Reise verlassen?", "Leave the journey?", "Lasciare il viaggio?")) },
            text = { Text(m(appLanguage, "Deine bisherigen Antworten bleiben auf diesem Gerät gespeichert.", "Your answers will remain saved on this device.", "Le tue risposte resteranno salvate su questo dispositivo.")) },
            confirmButton = { TextButton(onClick = onExit) { Text(m(appLanguage, "Verlassen", "Leave", "Esci")) } },
            dismissButton = { TextButton(onClick = { showExitDialog = false }) { Text(m(appLanguage, "Bleiben", "Stay", "Resta")) } }
        )
    }
}

@Composable
private fun IntrospectionHub(appLanguage: String, hasSavedRun: Boolean, onBack: () -> Unit, onStart: () -> Unit) {
    MysticBackdrop {
        Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 18.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = m(appLanguage, "Zurück", "Back", "Indietro"), tint = MysticText)
            }
            Spacer(Modifier.height(40.dp))
            Text("🧙‍♂️", fontSize = 48.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            Text(
                m(appLanguage, "Tauche ins Unterbewusstsein", "Dive into the subconscious", "Immergiti nel subconscio"),
                color = MysticText,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 10.dp)
            )
            Text(
                m(appLanguage, "Drei Zeichen. Drei Antworten. Eine verborgene Bedeutung.", "Three signs. Three answers. One hidden meaning.", "Tre segni. Tre risposte. Un significato nascosto."),
                color = MysticMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(38.dp))
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
                    .background(MysticSurface)
                    .border(1.dp, MysticPurple.copy(alpha = .55f), RoundedCornerShape(28.dp))
                    .clickable(onClick = onStart).padding(24.dp)
            ) {
                Text("✨️", fontSize = 34.sp)
                Text(m(appLanguage, "Das Verborgene in dir", "What lies hidden within you", "Ciò che è nascosto in te"), color = MysticText, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (hasSavedRun) m(appLanguage, "Deine gespeicherte Reise öffnen", "Open your saved journey", "Apri il tuo viaggio salvato") else m(appLanguage, "Eine geführte Reise durch Farbe, Tier und Wasser", "A guided journey through color, animal, and water", "Un viaggio guidato attraverso colore, animale e acqua"),
                    color = MysticMuted,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    if (hasSavedRun) m(appLanguage, "FORTSETZEN  →", "CONTINUE  →", "CONTINUA  →") else m(appLanguage, "BEGINNEN  →", "BEGIN  →", "INIZIA  →"),
                    color = HarmonyPinkSoft,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.End).padding(top = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun GuidedIntrospection(
    appLanguage: String,
    initialProgress: IntrospectionProgress,
    store: IntrospectionStore,
    onProgress: (IntrospectionProgress) -> Unit,
    onExitRequest: () -> Unit
) {
    val context = LocalContext.current
    var progress by remember { mutableStateOf(initialProgress) }
    var answerMode by remember { mutableStateOf("text") }
    var textAnswer by remember { mutableStateOf("") }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var narrator by remember { mutableStateOf<MediaPlayer?>(null) }
    var background by remember { mutableStateOf<MediaPlayer?>(null) }
    var answerPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var narratorPlaying by remember { mutableStateOf(false) }
    var answerPlayingPath by remember { mutableStateOf<String?>(null) }
    var recordingError by remember { mutableStateOf<String?>(null) }
    val latestRecorder by rememberUpdatedState(recorder)
    val latestNarrator by rememberUpdatedState(narrator)
    val latestBackground by rememberUpdatedState(background)
    val latestAnswerPlayer by rememberUpdatedState(answerPlayer)

    fun stopAnswerPlayback() {
        answerPlayer?.release()
        answerPlayer = null
        answerPlayingPath = null
    }

    fun stopRecording(save: Boolean) {
        val current = recorder
        recorder = null
        if (current != null) {
            runCatching { current.stop() }.onFailure { recordedFile?.delete() }
            current.release()
        }
        isRecording = false
        background?.start()
        if (!save) {
            recordedFile?.delete()
            recordedFile = null
        }
    }

    fun startRecording() {
        stopAnswerPlayback()
        narrator?.pause()
        narratorPlaying = false
        background?.pause()
        val output = store.recordingFile(progress.stage)
        val newRecorder = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()
        runCatching {
            newRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            newRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            newRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            newRecorder.setAudioEncodingBitRate(128_000)
            newRecorder.setAudioSamplingRate(44_100)
            newRecorder.setMaxDuration(300_000)
            newRecorder.setOutputFile(output.absolutePath)
            newRecorder.setOnInfoListener { _, what, _ ->
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) stopRecording(true)
            }
            newRecorder.prepare()
            newRecorder.start()
            recorder = newRecorder
            recordedFile = output
            isRecording = true
            recordingError = null
        }.onFailure {
            newRecorder.release()
            output.delete()
            background?.start()
            recordingError = m(appLanguage, "Die Aufnahme konnte nicht gestartet werden.", "The recording could not be started.", "Impossibile avviare la registrazione.")
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startRecording() else recordingError = m(appLanguage, "Für Audioantworten wird Mikrofonzugriff benötigt.", "Microphone access is required for audio answers.", "Per le risposte audio è necessario l'accesso al microfono.")
    }

    DisposableEffect(Unit) {
        background = prepareRawPlayer(context, R.raw.merlin_theme, looping = true)?.apply {
            setOnErrorListener { player, _, _ ->
                player.reset()
                background = null
                recordingError = m(appLanguage, "Die Hintergrundmusik konnte nicht abgespielt werden.", "Background music could not be played.", "Impossibile riprodurre la musica di sottofondo.")
                true
            }
            setVolume(1f, 1f)
            start()
        }
        if (background == null) {
            recordingError = m(appLanguage, "Die Hintergrundmusik konnte nicht geladen werden.", "Background music could not be loaded.", "Impossibile caricare la musica di sottofondo.")
        }
        onDispose {
            latestRecorder?.let { current ->
                runCatching { current.stop() }
                current.release()
            }
            latestNarrator?.release()
            latestBackground?.release()
            latestAnswerPlayer?.release()
        }
    }

    LaunchedEffect(progress.stage) {
        textAnswer = ""
        recordedFile = null
        answerMode = "text"
        narrator?.release()
        narrator = null
        narratorPlaying = false
        if (progress.stage == IntrospectionStage.RESULTS) {
            background?.setVolume(1f, 1f)
            return@LaunchedEffect
        }
        val resource = when (progress.stage) {
            IntrospectionStage.COLOR -> R.raw.introspection_color
            IntrospectionStage.ANIMAL -> R.raw.introspection_animal
            IntrospectionStage.WATER -> R.raw.introspection_water
            IntrospectionStage.REVELATION -> R.raw.introspection_reveal
            IntrospectionStage.RESULTS -> return@LaunchedEffect
        }
        background?.setVolume(.68f, .68f)
        narrator = prepareRawPlayer(context, resource)?.apply {
            setOnCompletionListener {
                narratorPlaying = false
                background?.setVolume(1f, 1f)
                if (progress.stage == IntrospectionStage.REVELATION) {
                    progress = progress.finishRevelation()
                    onProgress(progress)
                }
            }
            setOnErrorListener { player, _, _ ->
                player.reset()
                narrator = null
                narratorPlaying = false
                background?.setVolume(1f, 1f)
                recordingError = m(appLanguage, "Die Audioführung konnte nicht abgespielt werden.", "The audio guide could not be played.", "Impossibile riprodurre la guida audio.")
                true
            }
            start()
        }
        narratorPlaying = narrator != null
        if (narrator == null) {
            recordingError = m(appLanguage, "Die Audioführung konnte nicht geladen werden.", "The audio guide could not be loaded.", "Impossibile caricare la guida audio.")
        }
    }

    fun submitAnswer() {
        val answer = if (answerMode == "audio") {
            recordedFile?.takeIf { it.isFile }?.let { IntrospectionAnswer.Audio(it.absolutePath) }
        } else {
            IntrospectionAnswer.Text(textAnswer.trim()).takeIf { it.isValid() }
        } ?: return
        val updated = progress.advanceAfterAnswer(answer)
        progress = updated
        onProgress(updated)
    }

    MysticBackdrop {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onExitRequest) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = m(appLanguage, "Reise verlassen", "Leave journey", "Esci dal viaggio"), tint = MysticText)
                }
                val step = when (progress.stage) {
                    IntrospectionStage.COLOR -> 1
                    IntrospectionStage.ANIMAL -> 2
                    IntrospectionStage.WATER -> 3
                    else -> 4
                }
                Box(
                    Modifier.weight(1f).height(5.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = .12f))
                ) {
                    Box(Modifier.fillMaxWidth(step / 4f).height(5.dp).background(HarmonyPinkSoft))
                }
                Text("$step/4", color = MysticMuted, modifier = Modifier.padding(start = 14.dp))
            }

            when (progress.stage) {
                IntrospectionStage.RESULTS -> ResultsContent(appLanguage, progress, ::stopAnswerPlayback) { path ->
                    stopAnswerPlayback()
                    answerPlayer = MediaPlayer().apply {
                        setDataSource(path)
                        setOnCompletionListener { stopAnswerPlayback() }
                        prepare()
                        start()
                    }
                    answerPlayingPath = path
                }
                else -> {
                    PortalAura()
                    val copy = stageCopy(progress.stage, appLanguage)
                    Text(copy.eyebrow, color = HarmonyPinkSoft, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 2.sp)
                    Text(copy.title, color = MysticText, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp, modifier = Modifier.padding(top = 8.dp))
                    Text(copy.subtitle, color = MysticMuted, fontSize = 16.sp, lineHeight = 23.sp, modifier = Modifier.padding(top = 10.dp, bottom = 22.dp))

                    if (progress.stage == IntrospectionStage.REVELATION) {
                        Text(
                            if (narratorPlaying) m(appLanguage, "Die Zeichen fügen sich zusammen …", "The signs are coming together …", "I segni si stanno unendo …") else m(appLanguage, "Deine Enthüllung ist vollendet.", "Your revelation is complete.", "La tua rivelazione è completa."),
                            color = MysticText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(28.dp)
                        )
                    } else {
                        AnswerComposer(
                            mode = answerMode,
                            appLanguage = appLanguage,
                            text = textAnswer,
                            recordedFile = recordedFile,
                            isRecording = isRecording,
                            isPlaying = recordedFile?.absolutePath == answerPlayingPath,
                            error = recordingError,
                            onMode = { answerMode = it },
                            onText = { textAnswer = it },
                            onRecord = {
                                if (narratorPlaying) Unit
                                else if (isRecording) stopRecording(true)
                                else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) startRecording()
                                else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            },
                            onDelete = { recordedFile?.delete(); recordedFile = null; stopAnswerPlayback() },
                            onPlay = {
                                recordedFile?.let { file ->
                                    if (answerPlayingPath == file.absolutePath) stopAnswerPlayback() else {
                                        stopAnswerPlayback()
                                        answerPlayer = MediaPlayer().apply {
                                            setDataSource(file.absolutePath)
                                            setOnCompletionListener { stopAnswerPlayback() }
                                            prepare(); start()
                                        }
                                        answerPlayingPath = file.absolutePath
                                    }
                                }
                            }
                        )
                        Button(
                            onClick = ::submitAnswer,
                            enabled = !narratorPlaying && !isRecording && ((answerMode == "text" && textAnswer.isNotBlank()) || (answerMode == "audio" && recordedFile?.isFile == true)),
                            modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 4.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HarmonyPink, disabledContainerColor = HarmonyPink.copy(alpha = .28f))
                        ) { Text(m(appLanguage, "Antwort bestätigen", "Confirm answer", "Conferma risposta"), fontWeight = FontWeight.Bold) }
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

private data class StageCopy(val eyebrow: String, val title: String, val subtitle: String)

private fun stageCopy(stage: IntrospectionStage, language: String): StageCopy = when (stage) {
    IntrospectionStage.COLOR -> StageCopy(m(language, "ERSTES ZEICHEN", "FIRST SIGN", "PRIMO SEGNO"), m(language, "Welche Farbe zieht dich an?", "Which color draws you in?", "Quale colore ti attrae?"), m(language, "Beschreibe nicht nur ihren Namen. Was fühlst du, wenn du sie siehst? Welche Erinnerung, welche Stimmung oder welche Kraft trägt sie für dich?", "Don't just name it. What do you feel when you see it? What memory, mood, or strength does it hold for you?", "Non limitarti a nominarlo. Cosa provi quando lo vedi? Quale ricordo, emozione o forza racchiude per te?"))
    IntrospectionStage.ANIMAL -> StageCopy(m(language, "ZWEITES ZEICHEN", "SECOND SIGN", "SECONDO SEGNO"), m(language, "Welches Tier fasziniert dich?", "Which animal fascinates you?", "Quale animale ti affascina?"), m(language, "Sehr schön. Nun tritt ein Wesen aus dem Schatten. Welches Tier wählst du – und welche Eigenschaften bewunderst du an ihm?", "Beautiful. Now a creature steps from the shadows. Which animal do you choose—and which qualities do you admire in it?", "Bellissimo. Ora una creatura emerge dall'ombra. Quale animale scegli e quali qualità ammiri in lui?"))
    IntrospectionStage.WATER -> StageCopy(m(language, "DRITTES ZEICHEN", "THIRD SIGN", "TERZO SEGNO"), m(language, "Wie erscheint dir das Wasser?", "How does the water appear to you?", "Come ti appare l'acqua?"), m(language, "Stell dir Wasser vor. Ist es still oder wild, klar oder geheimnisvoll, nah oder grenzenlos? Beschreibe das Bild, das vor deinem inneren Auge entsteht.", "Imagine water. Is it still or wild, clear or mysterious, near or boundless? Describe the image forming in your mind.", "Immagina l'acqua. È calma o impetuosa, limpida o misteriosa, vicina o sconfinata? Descrivi l'immagine che nasce nella tua mente."))
    IntrospectionStage.REVELATION -> StageCopy(m(language, "DIE ENTHÜLLUNG", "THE REVELATION", "LA RIVELAZIONE"), m(language, "Höre, was deine Zeichen offenbaren", "Hear what your signs reveal", "Ascolta ciò che rivelano i tuoi segni"), m(language, "Lehne dich zurück. Deine Farbe, dein Tier und dein Wasser beginnen nun, ihre verborgene Sprache zu sprechen.", "Lean back. Your color, animal, and water are about to speak their hidden language.", "Rilassati. Il tuo colore, il tuo animale e la tua acqua stanno per parlare la loro lingua nascosta."))
    IntrospectionStage.RESULTS -> StageCopy(m(language, "DEINE ZEICHEN", "YOUR SIGNS", "I TUOI SEGNI"), m(language, "Das Verborgene in dir", "What lies hidden within you", "Ciò che è nascosto in te"), m(language, "Bewahre deine Antworten und kehre jederzeit zu ihnen zurück.", "Keep your answers and return to them whenever you wish.", "Conserva le tue risposte e torna a esse quando vuoi."))
}

@Composable
private fun AnswerComposer(
    appLanguage: String,
    mode: String,
    text: String,
    recordedFile: File?,
    isRecording: Boolean,
    isPlaying: Boolean,
    error: String?,
    onMode: (String) -> Unit,
    onText: (String) -> Unit,
    onRecord: () -> Unit,
    onDelete: () -> Unit,
    onPlay: () -> Unit
) {
    Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ModePill(m(appLanguage, "Text", "Text", "Testo"), mode == "text", Modifier.weight(1f)) { onMode("text") }
        ModePill("Audio", mode == "audio", Modifier.weight(1f)) { onMode("audio") }
    }
    if (mode == "text") {
        OutlinedTextField(
            value = text,
            onValueChange = onText,
            placeholder = { Text(m(appLanguage, "Lass deine Gedanken fließen …", "Let your thoughts flow …", "Lascia fluire i tuoi pensieri …")) },
            modifier = Modifier.fillMaxWidth().height(170.dp),
            shape = RoundedCornerShape(22.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MysticText,
                unfocusedTextColor = MysticText,
                focusedBorderColor = MysticPurple,
                unfocusedBorderColor = Color.White.copy(alpha = .18f),
                focusedContainerColor = MysticSurface,
                unfocusedContainerColor = MysticSurface,
                focusedPlaceholderColor = MysticMuted,
                unfocusedPlaceholderColor = MysticMuted
            )
        )
    } else {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(MysticSurface)
                .border(1.dp, if (isRecording) HarmonyPink else MysticPurple.copy(alpha = .45f), RoundedCornerShape(22.dp))
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = onRecord, modifier = Modifier.size(66.dp).background(if (isRecording) HarmonyPink else MysticPurple, CircleShape)) {
                Icon(if (isRecording) Icons.Default.Stop else Icons.Default.Mic, contentDescription = if (isRecording) m(appLanguage, "Aufnahme stoppen", "Stop recording", "Ferma registrazione") else m(appLanguage, "Audio aufnehmen", "Record audio", "Registra audio"), tint = Color.White, modifier = Modifier.size(30.dp))
            }
            Text(if (isRecording) m(appLanguage, "Aufnahme läuft · maximal 5 Minuten", "Recording · maximum 5 minutes", "Registrazione · massimo 5 minuti") else if (recordedFile != null) m(appLanguage, "Audioantwort gespeichert", "Audio answer saved", "Risposta audio salvata") else m(appLanguage, "Tippe, um deine Antwort aufzunehmen", "Tap to record your answer", "Tocca per registrare la risposta"), color = MysticText, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 10.dp))
            if (recordedFile != null && !isRecording) {
                Row {
                    IconButton(onClick = onPlay) { Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, "Audio abspielen", tint = MysticText) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Audio löschen", tint = HarmonyPinkSoft) }
                }
            }
            error?.let { Text(it, color = HarmonyPinkSoft, fontSize = 13.sp, textAlign = TextAlign.Center) }
        }
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun ModePill(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier.clip(RoundedCornerShape(14.dp)).background(if (selected) MysticPurple else MysticSurface)
            .border(1.dp, MysticPurple.copy(alpha = .6f), RoundedCornerShape(14.dp)).clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) { Text(label, color = MysticText, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun ResultsContent(appLanguage: String, progress: IntrospectionProgress, stopPlayback: () -> Unit, play: (String) -> Unit) {
    val copy = stageCopy(IntrospectionStage.RESULTS, appLanguage)
    PortalAura()
    Text(copy.eyebrow, color = HarmonyPinkSoft, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 2.sp)
    Text(copy.title, color = MysticText, fontSize = 29.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
    Text(copy.subtitle, color = MysticMuted, modifier = Modifier.padding(top = 8.dp, bottom = 18.dp))
    listOf(
        IntrospectionStage.COLOR to m(appLanguage, "Deine Farbe", "Your color", "Il tuo colore"),
        IntrospectionStage.ANIMAL to m(appLanguage, "Dein Tier", "Your animal", "Il tuo animale"),
        IntrospectionStage.WATER to m(appLanguage, "Dein Wasser", "Your water", "La tua acqua")
    ).forEach { (stage, title) ->
        Column(Modifier.fillMaxWidth().padding(vertical = 7.dp).clip(RoundedCornerShape(20.dp)).background(MysticSurface).border(1.dp, MysticPurple.copy(alpha = .35f), RoundedCornerShape(20.dp)).padding(18.dp)) {
            Text(title, color = HarmonyPinkSoft, fontWeight = FontWeight.Bold)
            when (val answer = progress.answers[stage]) {
                is IntrospectionAnswer.Text -> Text(answer.value, color = MysticText, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
                is IntrospectionAnswer.Audio -> Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { play(answer.filePath) }) { Icon(Icons.Default.PlayArrow, "Audioantwort abspielen", tint = MysticText) }
                    Text(m(appLanguage, "Deine Audioantwort", "Your audio answer", "La tua risposta audio"), color = MysticText)
                }
                null -> Text(m(appLanguage, "Keine Antwort gespeichert", "No answer saved", "Nessuna risposta salvata"), color = MysticMuted)
            }
        }
    }
}

@Composable
private fun PortalAura() {
    val transition = rememberInfiniteTransition(label = "portal")
    val corePulse by transition.animateFloat(
        initialValue = .94f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(tween(2400), RepeatMode.Reverse),
        label = "portal-core-pulse"
    )
    val outerWave by transition.animateFloat(
        initialValue = .78f,
        targetValue = 1.24f,
        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Restart),
        label = "portal-outer-wave"
    )
    val innerWave by transition.animateFloat(
        initialValue = .88f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(3000, delayMillis = 1450), RepeatMode.Restart),
        label = "portal-inner-wave"
    )
    val glowPulse by transition.animateFloat(
        initialValue = .38f,
        targetValue = .76f,
        animationSpec = infiniteRepeatable(tween(2400), RepeatMode.Reverse),
        label = "portal-glow-pulse"
    )
    val outerWaveAlpha = ((1.24f - outerWave) / .46f * .48f).coerceIn(0f, .48f)
    val innerWaveAlpha = ((1.18f - innerWave) / .30f * .36f).coerceIn(0f, .36f)

    Box(Modifier.fillMaxWidth().height(270.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(224.dp).scale(outerWave).alpha(outerWaveAlpha)
                .border(2.dp, Brush.sweepGradient(listOf(HarmonyPinkSoft, MysticPurple, Color(0xFF5B5CFF), HarmonyPinkSoft)), CircleShape)
        )
        Box(
            Modifier.size(192.dp).scale(innerWave).alpha(innerWaveAlpha)
                .border(2.dp, Brush.sweepGradient(listOf(MysticPurple, Color(0xFFFF8CB8), Color(0xFF6D4CFF), MysticPurple)), CircleShape)
        )
        Box(Modifier.size(230.dp).scale(corePulse).blur(34.dp).alpha(glowPulse).background(MysticPurple, CircleShape))
        Box(Modifier.size(195.dp).scale(corePulse).blur(16.dp).alpha(.58f).background(HarmonyPinkSoft, CircleShape))
        Box(
            Modifier.size(184.dp).scale(corePulse)
                .border(12.dp, Brush.sweepGradient(listOf(HarmonyPinkSoft, Color(0xFFFFB1CF), MysticPurple, Color(0xFF5B5CFF), HarmonyPinkSoft)), CircleShape)
        )
        Box(Modifier.size(148.dp).scale(corePulse).background(Brush.radialGradient(listOf(Color(0xFF42145E), Color(0xFF08030E))), CircleShape), contentAlignment = Alignment.Center) {
            Text("✦", color = Color.White.copy(alpha = .85f), fontSize = 44.sp)
        }
    }
}

@Composable
private fun MysticBackdrop(content: @Composable () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF1A0925), MysticBackground, Color(0xFF100315)))
        )
    ) {
        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(MysticViolet.copy(alpha = .22f), Color.Transparent), radius = 900f)))
        content()
    }
}

private fun m(language: String, german: String, english: String, italian: String): String = when {
    language.startsWith("en", ignoreCase = true) -> english
    language.startsWith("it", ignoreCase = true) -> italian
    else -> german
}
