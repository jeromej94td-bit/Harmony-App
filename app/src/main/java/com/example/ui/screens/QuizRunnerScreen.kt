package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.ui.util.triggerMiniVibration
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.example.R
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.LinkEngine
import com.example.data.model.ProfileEntity
import com.example.data.model.QuestionPack
import com.example.ui.ActivePackRun
import com.example.ui.AppLanguage
import com.example.ui.contentText
import com.example.ui.tr
import com.example.ui.components.CategoryTag
import com.example.ui.components.TotImageProvider
import com.example.ui.theme.HarmonyBg
import com.example.ui.theme.HarmonyGold
import com.example.ui.theme.HarmonyLine
import com.example.ui.theme.HarmonyMuted
import com.example.ui.theme.HarmonyPink
import com.example.ui.theme.HarmonyPinkSoft
import com.example.ui.theme.HarmonyPurple
import com.example.ui.theme.HarmonyPurpleLight
import com.example.ui.theme.HarmonySurface
import com.example.ui.theme.HarmonySurface2
import com.example.ui.theme.HarmonyText

@Composable
fun QuizRunnerScreen(
    activeRun: ActivePackRun,
    profile: ProfileEntity,
    isExitConfirmOpen: Boolean,
    isOwnAnswerDialogOpen: Boolean,
    onPickAnswer: (String) -> Unit,
    onPickTot: (String) -> Unit,
    onNextStep: () -> Unit,
    onAskExit: () -> Unit,
    onCloseExitConfirm: () -> Unit,
    onCloseRunner: () -> Unit,
    onOpenOwnAnswerDialog: (Int?, String?) -> Unit,
    onCloseOwnAnswerDialog: () -> Unit,
    onSaveOwnAnswer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pack = activeRun.pack
    val totalLen = if (pack.type == "tot") pack.pairs.size else pack.questions.size

    val category = com.example.data.model.HarmonyPacksData.CATEGORIES.find { it.id == pack.cat }
    val catColor = category?.tagColorHex?.let { Color(it) } ?: HarmonyPink

    val animatedCatColor by androidx.compose.animation.animateColorAsState(
        targetValue = catColor,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 800, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "runnerCatColor"
    )

    LaunchedEffect(activeRun.isFinished) {
        if (activeRun.isFinished) {
            triggerMiniVibration(context, durationMs = 70L)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("quiz_runner_screen"),
        color = Color(0xFF10060E)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            animatedCatColor.copy(alpha = 0.28f),
                            animatedCatColor.copy(alpha = 0.10f),
                            Color(0xFF10060E)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Runner Top Bar
                if (pack.type == "tot" && !activeRun.isFinished) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onAskExit,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f))
                                .testTag("runner_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = tr("Zurück", "Back"),
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = tr("Das oder das?", "This or That?"),
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(130.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color.White.copy(alpha = 0.35f))
                            )
                        }

                        Box(modifier = Modifier.size(36.dp))
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onAskExit,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .testTag("runner_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = tr("Zurück", "Back"),
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Progress Track
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                        ) {
                            val fraction = if (activeRun.isFinished) 1f else ((activeRun.currentIndex + 1).toFloat() / totalLen).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Brush.horizontalGradient(listOf(animatedCatColor, animatedCatColor.copy(alpha = 0.75f))))
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = if (activeRun.isFinished) "$totalLen/$totalLen" else "${activeRun.currentIndex + 1}/$totalLen",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                // Runner Body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(
                            start = if (pack.type == "tot") 8.dp else 22.dp,
                            end = if (pack.type == "tot") 8.dp else 22.dp,
                            bottom = if (pack.type == "tot") 12.dp else 0.dp
                        )
                ) {
                    if (activeRun.isFinished) {
                        if (pack.type == "tot") {
                            TotResultsView(
                                pack = pack,
                                activeRun = activeRun,
                                profile = profile,
                                onClose = onAskExit
                            )
                        } else {
                            // Standard Finished Screen
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "💞", fontSize = 56.sp)
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = tr("Fertig!", "Done!"),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Deine Antworten sind gespeichert. Sobald ${profile.partnerName} das Paket beendet, werden beide Antworten gemeinsam sichtbar.",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    } else if (pack.type == "tot") {
                        // This or That Mode
                        val pair = pack.pairs.getOrNull(activeRun.currentIndex) ?: ("" to "")
                        val selectedAns = activeRun.currentAnswers[activeRun.currentIndex]
                        val caption = LinkEngine.captionFor(pack.id, activeRun.currentIndex)

                        Column(modifier = Modifier.fillMaxSize()) {
                            if (!caption.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White.copy(alpha = 0.12f))
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = contentText(caption),
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            TotCardPairView(
                                firstText = pair.first,
                                secondText = pair.second,
                                selectedAns = selectedAns,
                                onPick = { chosen ->
                                    onPickTot(chosen)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else if (pack.type == "disc") {
                        // Discussion Mode
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(vertical = 12.dp)
                        ) {
                            CategoryTag(tag = pack.tags.firstOrNull() ?: "reden")
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = contentText(pack.title),
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            pack.questions.forEachIndexed { qIdx, question ->
                                val mineAns = activeRun.currentAnswers[qIdx] ?: question.defaultMine

                                Column(modifier = Modifier.padding(bottom = 18.dp)) {
                                    Text(
                                        text = "${qIdx + 1}. ${contentText(question.q)}",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        lineHeight = 20.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    if (mineAns != null) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    Brush.horizontalGradient(
                                                        listOf(HarmonyPink.copy(alpha = 0.16f), HarmonyPurple.copy(alpha = 0.14f))
                                                    )
                                                )
                                                .border(1.dp, HarmonyPink.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                                .padding(11.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .background(Brush.linearGradient(listOf(HarmonyPink, HarmonyPinkSoft))),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = profile.userName.take(1),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(9.dp))
                                            Text(
                                                text = contentText(mineAns),
                                                fontSize = 13.sp,
                                                color = Color.White,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White.copy(alpha = 0.03f))
                                                .border(1.dp, Color.White.copy(alpha = 0.13f), RoundedCornerShape(12.dp))
                                                .clickable { onOpenOwnAnswerDialog(qIdx, "disc") }
                                                .padding(11.dp)
                                        ) {
                                            Text(
                                                text = tr("✎ Tippe, um zu antworten", "✎ Tap to answer"),
                                                fontSize = 12.5.sp,
                                                color = HarmonyMuted
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(7.dp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.05f))
                                            .border(1.dp, HarmonyLine, RoundedCornerShape(12.dp))
                                            .padding(11.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(Brush.linearGradient(listOf(HarmonyPurple, HarmonyPurpleLight))),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = profile.partnerName.take(1),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(9.dp))
                                        Text(
                                            text = tr("Verbinde dich mit ${profile.partnerName}, um die Antwort zu sehen", "Connect with ${profile.partnerName} to see the answer"),
                                            fontSize = 13.sp,
                                            color = HarmonyMuted
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Standard Quiz Mode
                        val q = pack.questions.getOrNull(activeRun.currentIndex)
                        val selectedAns = activeRun.currentAnswers[activeRun.currentIndex]
                        val scrollState = rememberScrollState()

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.Center
                        ) {
                            CategoryTag(tag = pack.tags.firstOrNull() ?: "unterhaltung")
                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = q?.q ?: "",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                lineHeight = 33.sp
                            )
                            Spacer(modifier = Modifier.height(26.dp))

                            val options = (q?.options ?: emptyList()) + "Schreibe deine eigene Antwort"

                            options.forEachIndexed { optIdx, optText ->
                                val isOwn = optIdx == options.size - 1
                                val isSelected = if (isOwn) {
                                    selectedAns != null && selectedAns !in (q?.options ?: emptyList())
                                } else {
                                    selectedAns == optText
                                }

                                QuizOptionButton(
                                    number = optIdx + 1,
                                    text = if (isSelected && isOwn) selectedAns ?: optText else optText,
                                    isSelected = isSelected,
                                    isOwn = isOwn,
                                    onClick = {
                                        triggerMiniVibration(context, 40L)
                                        if (isOwn) {
                                            onOpenOwnAnswerDialog(activeRun.currentIndex, null)
                                        } else {
                                            onPickAnswer(optText)
                                        }
                                    },
                                    modifier = Modifier.padding(bottom = 11.dp)
                                )
                            }
                        }
                    }
                }

                // Runner Footer
                if (activeRun.isFinished || pack.type == "disc") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (activeRun.isFinished) {
                            Button(
                                onClick = onCloseRunner,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("finish_runner_button"),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = HarmonyPink)
                            ) {
                                Text(text = tr("Zurück", "Back"), fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else if (pack.type == "disc") {
                            Button(
                                onClick = onCloseRunner,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("finish_disc_button"),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = HarmonyPink)
                            ) {
                                Text(text = "Fertig", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Exit Confirm Dialog
            if (isExitConfirmOpen) {
                Dialog(onDismissRequest = onCloseExitConfirm) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Brush.verticalGradient(listOf(HarmonySurface2, HarmonySurface)))
                            .border(1.dp, HarmonyLine, RoundedCornerShape(24.dp))
                            .padding(22.dp)
                    ) {
                        Column {
                            Text(
                                text = tr("Quiz verlassen?", "Leave quiz?"),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarmonyText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = tr("Möchtest du das Quiz wirklich verlassen? Dein bisheriger Fortschritt bleibt gespeichert.", "Are you sure you want to leave? Your progress will be saved."),
                                fontSize = 13.sp,
                                color = HarmonyMuted,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onCloseExitConfirm,
                                    modifier = Modifier.weight(1f),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f))
                                ) {
                                    Text(text = tr("Weiter spielen", "Keep playing"), color = HarmonyText)
                                }
                                Button(
                                    onClick = onCloseRunner,
                                    modifier = Modifier.weight(1f),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = HarmonyPink)
                                ) {
                                    Text(text = tr("Quiz verlassen", "Leave quiz"), color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Own Answer Dialog
            if (isOwnAnswerDialogOpen) {
                var textInput by remember { mutableStateOf("") }
                Dialog(onDismissRequest = onCloseOwnAnswerDialog) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Brush.verticalGradient(listOf(HarmonySurface2, HarmonySurface)))
                            .border(1.dp, HarmonyLine, RoundedCornerShape(24.dp))
                            .padding(22.dp)
                    ) {
                        Column {
                            Text(
                                text = tr("Deine eigene Antwort", "Your own answer"),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarmonyText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = tr("Schreib frei, was dir wirklich dazu einfällt.", "Write what truly comes to mind."),
                                fontSize = 13.sp,
                                color = HarmonyMuted
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                placeholder = { Text("Deine Antwort...", color = HarmonyMuted) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("own_answer_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = HarmonyPink,
                                    unfocusedBorderColor = HarmonyLine,
                                    focusedTextColor = HarmonyText,
                                    unfocusedTextColor = HarmonyText
                                )
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onCloseOwnAnswerDialog,
                                    modifier = Modifier.weight(1f),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f))
                                ) {
                                    Text(text = tr("Abbrechen", "Cancel"), color = HarmonyText)
                                }
                                Button(
                                    onClick = {
                                        triggerMiniVibration(context, 40L)
                                        onSaveOwnAnswer(textInput)
                                    },
                                    enabled = textInput.isNotBlank(),
                                    modifier = Modifier.weight(1f),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = HarmonyPink)
                                ) {
                                    Text(text = tr("Übernehmen", "Save"), color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizOptionButton(
    number: Int,
    text: String,
    isSelected: Boolean,
    isOwn: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isSelected) Brush.horizontalGradient(
                    listOf(HarmonyPink.copy(alpha = 0.35f), HarmonyPurple.copy(alpha = 0.35f))
                )
                else Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.07f), Color.White.copy(alpha = 0.07f))
                )
            )
            .border(
                1.5.dp,
                if (isSelected) HarmonyPink else Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(15.dp)
            .testTag("quiz_option_$number")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(27.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) HarmonyPink else Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(13.dp))
            Text(
                text = text,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Medium,
                color = if (isOwn && !isSelected) Color.White.copy(alpha = 0.72f) else Color.White,
                fontStyle = if (isOwn) FontStyle.Italic else FontStyle.Normal,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
fun TotCardPairView(
    firstText: String,
    secondText: String,
    selectedAns: String?,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val oderText = stringResource(R.string.oder_text)
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val slideDistancePx = with(density) { configuration.screenWidthDp.dp.toPx() * 1.35f }

    val topOffsetX = remember { Animatable(0f) }
    val bottomOffsetX = remember { Animatable(0f) }
    val topExtraRotation = remember { Animatable(0f) }
    val bottomExtraRotation = remember { Animatable(0f) }
    val oderScale = remember { Animatable(1f) }

    var isAnimating by remember { mutableStateOf(false) }

    fun handlePick(option: String) {
        if (isAnimating) return
        isAnimating = true
        triggerMiniVibration(context, 40L)

        scope.launch {
            val exitDuration = 320
            val exitEasing = FastOutSlowInEasing

            coroutineScope {
                launch {
                    topOffsetX.animateTo(-slideDistancePx, animationSpec = tween(exitDuration, easing = exitEasing))
                }
                launch {
                    topExtraRotation.animateTo(-6f, animationSpec = tween(exitDuration, easing = exitEasing))
                }
                launch {
                    bottomOffsetX.animateTo(slideDistancePx, animationSpec = tween(exitDuration, easing = exitEasing))
                }
                launch {
                    bottomExtraRotation.animateTo(6f, animationSpec = tween(exitDuration, easing = exitEasing))
                }
                launch {
                    oderScale.animateTo(0f, animationSpec = tween(exitDuration / 2, easing = exitEasing))
                }
            }

            onPick(option)

            topOffsetX.snapTo(-slideDistancePx)
            topExtraRotation.snapTo(-8f)
            bottomOffsetX.snapTo(slideDistancePx)
            bottomExtraRotation.snapTo(8f)
            oderScale.snapTo(0f)

            val enterDuration = 440
            val enterEasing = CubicBezierEasing(0.05f, 0.75f, 0.1f, 1.0f)

            coroutineScope {
                launch {
                    topOffsetX.animateTo(0f, animationSpec = tween(enterDuration, easing = enterEasing))
                }
                launch {
                    topExtraRotation.animateTo(0f, animationSpec = tween(enterDuration, easing = enterEasing))
                }
                launch {
                    bottomOffsetX.animateTo(0f, animationSpec = tween(enterDuration, easing = enterEasing))
                }
                launch {
                    bottomExtraRotation.animateTo(0f, animationSpec = tween(enterDuration, easing = enterEasing))
                }
                launch {
                    oderScale.animateTo(1f, animationSpec = tween(enterDuration, easing = FastOutSlowInEasing))
                }
            }

            isAnimating = false
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Card (tilted -3.2f)
            TotStyledCard(
                text = contentText(firstText),
                tagAlignment = Alignment.TopStart,
                isSelected = selectedAns == firstText,
                rotationAngle = -3.2f + topExtraRotation.value,
                onClick = { handlePick(firstText) },
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        translationX = topOffsetX.value
                    }
            )

            // Bottom Card (tilted +3.2f)
            TotStyledCard(
                text = contentText(secondText),
                tagAlignment = Alignment.BottomStart,
                isSelected = selectedAns == secondText,
                rotationAngle = 3.2f + bottomExtraRotation.value,
                onClick = { handlePick(secondText) },
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        translationX = bottomOffsetX.value
                    }
            )
        }

        // Central "oder" badge
        Box(
            modifier = Modifier
                .size(50.dp)
                .graphicsLayer {
                    scaleX = oderScale.value
                    scaleY = oderScale.value
                }
                .clip(CircleShape)
                .background(Color.White)
                .border(1.5.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contentText(oderText),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF231127)
            )
        }
    }
}

@Composable
fun TotStyledCard(
    text: String,
    tagAlignment: Alignment,
    isSelected: Boolean,
    rotationAngle: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageUrl = remember(text, TotImageProvider.version) { TotImageProvider.getImageUrl(text) }
    val imageRequest = remember(imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { rotationZ = rotationAngle }
            .clip(RoundedCornerShape(26.dp))
            .border(
                width = 3.dp,
                color = Color.White,
                shape = RoundedCornerShape(26.dp)
            )
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = imageRequest,
            contentDescription = text,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black.copy(alpha = 0.15f),
                            0.5f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.25f)
                        )
                    )
                )
        )

        // Destination Tag Pill
        if (com.example.data.DevAssetStore.isUserFacingLabel(text)) {
            Box(
                modifier = Modifier
                    .align(tagAlignment)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = text,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TotResultsView(
    pack: QuestionPack,
    activeRun: ActivePackRun,
    profile: ProfileEntity,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = tr("Zurück", "Back"),
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = pack.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "•••",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (selectedTab == 0) Brush.horizontalGradient(listOf(HarmonyPink, HarmonyPurple))
                        else Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.08f)))
                    )
                    .clickable { selectedTab = 0 }
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = tr("Ergebnisse", "Results"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (selectedTab == 1) Brush.horizontalGradient(listOf(HarmonyPink, HarmonyPurple))
                        else Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.08f)))
                    )
                    .clickable { selectedTab = 1 }
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "💬", fontSize = 13.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = tr("Diskussion", "Discussion"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (selectedTab == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "85%",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = HarmonyPinkSoft
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tr("Antwortähnlichkeit", "Answer similarity"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarmonyPink
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            pack.pairs.forEachIndexed { index, pair ->
                val myAns = activeRun.currentAnswers[index]

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(1.dp, HarmonyLine, RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SideBySideTotCard(
                                text = contentText(pair.first),
                                isSelected = myAns == pair.first,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(180.dp)
                            )

                            SideBySideTotCard(
                                text = contentText(pair.second),
                                isSelected = myAns == pair.second,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(180.dp)
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 20.dp)
            ) {
                Text(
                    text = tr("Diskutiert eure Antworten", "Discuss your answers"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tr("Habt ihr überraschende Unterschiede entdeckt? Sprecht darüber, warum euch bestimmte Optionen besser gefallen!", "Did you discover surprising differences? Talk about why you prefer certain options!"),
                    fontSize = 13.5.sp,
                    color = HarmonyMuted,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun SideBySideTotCard(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageUrl = remember(text, TotImageProvider.version) { TotImageProvider.getImageUrl(text) }
    val imageRequest = remember(imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build()
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) HarmonyPink else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        AsyncImage(
            model = imageRequest,
            contentDescription = text,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.35f to Color.Black.copy(alpha = 0.15f),
                            1.0f to Color.Black.copy(alpha = 0.88f)
                        )
                    )
                )
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(HarmonyPink),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
            }
        }

        if (com.example.data.DevAssetStore.isUserFacingLabel(text)) {
            Text(
                text = text,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 16.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            )
        }
    }
}
