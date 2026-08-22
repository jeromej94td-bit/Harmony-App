package com.example.ui.screens

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnswerEntity
import com.example.data.model.EitherOrAnswerCodec
import com.example.data.model.HarmonyPacksData
import com.example.data.model.ProfileEntity
import com.example.ui.components.PandaReactionStage
import com.example.ui.theme.HarmonyMuted
import com.example.ui.theme.HarmonyPink
import com.example.ui.theme.HarmonyPurple
import com.example.ui.theme.HarmonySurface2
import com.example.ui.theme.HarmonyText
import com.example.util.LanguageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.exp
import kotlin.random.Random

private enum class CoupleGameStep { USER_CHOICE, HANDOVER, PARTNER_CHOICE, REVEAL }

@Composable
fun PandaEitherOrScreen(
    profile: ProfileEntity,
    answers: List<AnswerEntity>,
    appLanguage: String = "de",
    onSaveAnswer: (questionIndex: Int, userChoice: String, partnerChoice: String) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val rawPack = remember { HarmonyPacksData.PACKS.first { it.id == PANDA_EITHER_OR_PACK_ID } }
    val displayPack = remember(appLanguage) { LanguageManager.translatePack(rawPack, appLanguage) }
    val completedBeforeStart = remember {
        answers.asSequence()
            .filter { it.packId == PANDA_EITHER_OR_PACK_ID }
            .filter { EitherOrAnswerCodec.decode(it.answerText) != null }
            .map { it.questionIndex }
            .toSet()
    }
    val questionOrder = remember { rawPack.pairs.indices.filterNot { it in completedBeforeStart }.shuffled() }

    var orderPosition by remember { mutableIntStateOf(0) }
    var step by remember { mutableStateOf(CoupleGameStep.USER_CHOICE) }
    var userChoice by remember { mutableStateOf<String?>(null) }
    var partnerChoice by remember { mutableStateOf<String?>(null) }
    var reactionKey by remember { mutableIntStateOf(0) }

    val questionIndex = questionOrder.getOrNull(orderPosition)
    val rawPair = questionIndex?.let(rawPack.pairs::get)
    val displayPair = questionIndex?.let(displayPack.pairs::get)
    val isMatch = userChoice != null && userChoice == partnerChoice
    val remaining = questionOrder.size - orderPosition

    fun localizedTemplate(source: String, vararg values: Pair<String, String>): String {
        var result = LanguageManager.tr(source, appLanguage)
        values.forEach { (key, value) -> result = result.replace("{$key}", value) }
        return result
    }

    LaunchedEffect(reactionKey) {
        if (reactionKey > 0 && isMatch) {
            delay(610)
            vibrateHighFive(context)
            playHighFiveClap()
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF12051D), Color(0xFF260A37), Color(0xFF09020F))))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onExit) {
                    Icon(Icons.Default.Close, contentDescription = LanguageManager.tr("Spiel schließen", appLanguage), tint = HarmonyText)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🐼 ${LanguageManager.tr("Entweder oder", appLanguage)}", color = HarmonyText, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                    Text(
                        if (questionIndex == null) LanguageManager.tr("Alle Fragen beantwortet", appLanguage)
                        else localizedTemplate("{count} offen · keine Wiederholungen", "count" to remaining.toString()),
                        color = HarmonyMuted,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.size(48.dp))
            }
            Spacer(Modifier.height(14.dp))

            if (rawPair == null || displayPair == null || questionIndex == null) {
                CompletedEitherOrCard(rawPack.pairs.size, appLanguage, onExit, Modifier.weight(1f))
            } else {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "couple_game_step",
                    modifier = Modifier.weight(1f)
                ) { currentStep ->
                    when (currentStep) {
                        CoupleGameStep.USER_CHOICE -> CoupleChoicePanel(
                            profile.userName, rawPair, displayPair, appLanguage,
                            localizedTemplate(
                                "Frage {current} von {total}",
                                "current" to (completedBeforeStart.size + orderPosition + 1).toString(),
                                "total" to rawPack.pairs.size.toString()
                            )
                        ) { userChoice = it; step = CoupleGameStep.HANDOVER }

                        CoupleGameStep.HANDOVER -> HandoverPanel(profile.partnerName, appLanguage) {
                            step = CoupleGameStep.PARTNER_CHOICE
                        }

                        CoupleGameStep.PARTNER_CHOICE -> CoupleChoicePanel(
                            profile.partnerName, rawPair, displayPair, appLanguage,
                            LanguageManager.tr("Geheime Auswahl", appLanguage)
                        ) {
                            partnerChoice = it
                            onSaveAnswer(questionIndex, userChoice.orEmpty(), it)
                            reactionKey += 1
                            step = CoupleGameStep.REVEAL
                        }

                        CoupleGameStep.REVEAL -> RevealPanel(
                            profile, userChoice.orEmpty(), partnerChoice.orEmpty(), appLanguage,
                            isMatch, reactionKey
                        ) {
                            orderPosition += 1
                            userChoice = null
                            partnerChoice = null
                            step = CoupleGameStep.USER_CHOICE
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoupleChoicePanel(
    name: String,
    rawPair: Pair<String, String>,
    displayPair: Pair<String, String>,
    appLanguage: String,
    progressLabel: String,
    onChoice: (String) -> Unit
) {
    val chooserText = LanguageManager.tr("{name} entscheidet", appLanguage).replace("{name}", name)
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(progressLabel, color = HarmonyPink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(chooserText, color = HarmonyText, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        Text(LanguageManager.tr("Der andere schaut kurz weg 🤫", appLanguage), color = HarmonyMuted, fontSize = 14.sp)
        Spacer(Modifier.height(28.dp))
        ChoiceCard(displayPair.first, Color(0xFFFF5DAA)) { onChoice(rawPair.first) }
        Spacer(Modifier.height(14.dp))
        Text(LanguageManager.tr("ODER", appLanguage), color = HarmonyMuted, fontSize = 12.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(14.dp))
        ChoiceCard(displayPair.second, Color(0xFF9E6BFF)) { onChoice(rawPair.second) }
    }
}

@Composable
private fun ChoiceCard(text: String, accent: Color, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(112.dp)
            .background(Brush.horizontalGradient(listOf(accent.copy(alpha = 0.36f), HarmonySurface2)), RoundedCornerShape(28.dp))
            .border(1.5.dp, accent.copy(alpha = 0.82f), RoundedCornerShape(28.dp))
            .clickable(onClick = onClick).padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = HarmonyText, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
    }
}

@Composable
private fun HandoverPanel(partnerName: String, appLanguage: String, onReady: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(112.dp).background(HarmonyPurple.copy(alpha = 0.28f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = HarmonyPink, modifier = Modifier.size(54.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(LanguageManager.tr("Handy weitergeben", appLanguage), color = HarmonyText, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
        Text(LanguageManager.tr("Die erste Antwort bleibt geheim.", appLanguage), color = HarmonyMuted, fontSize = 15.sp)
        Spacer(Modifier.height(34.dp))
        Button(onClick = onReady, colors = ButtonDefaults.buttonColors(containerColor = HarmonyPink), modifier = Modifier.fillMaxWidth().height(58.dp)) {
            Icon(Icons.Default.Lock, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text(LanguageManager.tr("{partner} ist bereit", appLanguage).replace("{partner}", partnerName), fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun RevealPanel(
    profile: ProfileEntity,
    userChoice: String,
    partnerChoice: String,
    appLanguage: String,
    isMatch: Boolean,
    reactionKey: Int,
    onNext: () -> Unit
) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        PandaReactionStage(isMatch, reactionKey, Modifier.fillMaxWidth().height(265.dp))
        Text(
            LanguageManager.tr(if (isMatch) "Volltreffer! High Five 💥" else "Heute verschieden – auch das gehört zu euch", appLanguage),
            color = HarmonyText, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        AnswerRevealRow(profile.userName, LanguageManager.tr(userChoice, appLanguage), HarmonyPink)
        Spacer(Modifier.height(9.dp))
        AnswerRevealRow(profile.partnerName, LanguageManager.tr(partnerChoice, appLanguage), HarmonyPurple)
        Spacer(Modifier.weight(1f))
        Button(onClick = onNext, colors = ButtonDefaults.buttonColors(containerColor = if (isMatch) HarmonyPink else HarmonyPurple), modifier = Modifier.fillMaxWidth().height(58.dp)) {
            Text(LanguageManager.tr("Nächste zufällige Frage", appLanguage), fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun AnswerRevealRow(name: String, answer: String, accent: Color) {
    Row(
        Modifier.fillMaxWidth().background(accent.copy(alpha = 0.14f), RoundedCornerShape(18.dp))
            .border(1.dp, accent.copy(alpha = 0.42f), RoundedCornerShape(18.dp)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, color = accent, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(0.34f))
        Text(answer, color = HarmonyText, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.66f), textAlign = TextAlign.End)
    }
}

@Composable
private fun CompletedEitherOrCard(completed: Int, appLanguage: String, onExit: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("🐼💕🐼", fontSize = 56.sp)
        Spacer(Modifier.height(16.dp))
        Text(LanguageManager.tr("Ihr kennt jede Entscheidung", appLanguage), color = HarmonyText, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
        Text(
            LanguageManager.tr("{count} von {total} Fragen beantwortet", appLanguage)
                .replace("{count}", completed.toString()).replace("{total}", completed.toString()),
            color = HarmonyMuted, fontSize = 15.sp
        )
        Spacer(Modifier.height(30.dp))
        Button(onClick = onExit, colors = ButtonDefaults.buttonColors(containerColor = HarmonyPink)) {
            Text(LanguageManager.tr("Zurück zu den Spielen", appLanguage), fontWeight = FontWeight.Bold)
        }
    }
}

private fun vibrateHighFive(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    } ?: return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 38, 42, 72), -1))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(longArrayOf(0, 38, 42, 72), -1)
    }
}

private suspend fun playHighFiveClap() = withContext(Dispatchers.Default) {
    val sampleRate = 44_100
    val durationMs = 170
    val count = sampleRate * durationMs / 1_000
    val random = Random(941)
    val samples = ShortArray(count) { index ->
        val t = index.toDouble() / sampleRate
        val envelope = exp(-t * 31.0)
        val crack = if (index < sampleRate / 220) 1.0 else 0.56
        (random.nextDouble(-1.0, 1.0) * envelope * crack * Short.MAX_VALUE * 0.72).toInt().toShort()
    }
    val track = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
        )
        .setBufferSizeInBytes(samples.size * 2)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()
    try {
        if (track.state != AudioTrack.STATE_INITIALIZED) return@withContext
        track.write(samples, 0, samples.size)
        track.play()
        delay(durationMs.toLong() + 45L)
    } finally {
        track.release()
    }
}

const val PANDA_EITHER_OR_PACK_ID = "entweder_oder_panda"
