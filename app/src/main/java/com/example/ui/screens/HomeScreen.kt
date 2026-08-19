package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AnswerEntity
import com.example.data.model.CoupleStatsEntity
import com.example.data.model.EitherOrAnswerCodec
import com.example.data.model.HarmonyPacksData
import com.example.data.model.ProfileEntity
import com.example.data.model.QuestionPack
import com.example.data.model.SharedPicEntity
import com.example.ui.components.AuroraGlassSectionTitle
import com.example.ui.components.AuroraProgressBar
import com.example.ui.components.CategoryTag
import com.example.ui.components.HarmonyCard
import com.example.ui.components.HarmonyPackIcon
import com.example.ui.theme.HarmonyLine
import com.example.ui.theme.HarmonyMuted
import com.example.ui.theme.HarmonyPink
import com.example.ui.theme.HarmonyPinkSoft
import com.example.ui.theme.HarmonyPurple
import com.example.ui.theme.HarmonyPurpleLight
import com.example.ui.theme.HarmonySurface
import com.example.ui.theme.HarmonySurface2
import com.example.ui.theme.HarmonyText
import com.example.ui.theme.topicAccentColor
import com.example.util.LanguageManager
import com.example.widget.PicShareWidgetPreferences
import com.example.widget.PicShareWidgetProvider
import com.example.widget.PicShareWidgetSettings
import java.io.File
import java.util.concurrent.TimeUnit

@Composable
fun HomeScreen(
    profile: ProfileEntity,
    answers: List<AnswerEntity>,
    sharedPics: List<SharedPicEntity>,
    stats: CoupleStatsEntity,
    appLanguage: String = "de",
    onStartPack: (String) -> Unit,
    onAddSharedPictures: (List<Uri>, String) -> Unit,
    onUpdateSharedPicture: (SharedPicEntity) -> Unit,
    onPinWidget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showAnswerList by remember { mutableStateOf(false) }
    val answeredPackIds = answers.mapTo(mutableSetOf()) { it.packId }
    val recommendation = HarmonyPacksData.PACKS
        .firstOrNull { it.id !in answeredPackIds }
        ?: HarmonyPacksData.PACKS.first()
    val translatedRecommendation = LanguageManager.translatePack(recommendation, appLanguage)
    val daysTogether = TimeUnit.MILLISECONDS.toDays(
        (System.currentTimeMillis() - profile.startDate).coerceAtLeast(0)
    ).toInt()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 90.dp)
    ) {
        ConnectBanner(
            appLanguage = appLanguage,
            partnerName = profile.partnerName,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
        )

        PicShareHomeCard(
            pics = sharedPics,
            profile = profile,
            onAddPictures = onAddSharedPictures,
            onUpdatePicture = onUpdateSharedPicture,
            onPinWidget = onPinWidget,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
        )

        Spacer(Modifier.height(10.dp))
        AuroraGlassSectionTitle(
            LanguageManager.tr("Für dich empfohlen", appLanguage),
            Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
        )
        PaddingPackCard(
            appLanguage = appLanguage,
            pack = translatedRecommendation,
            answers = answers,
            onStartPack = onStartPack,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
        )

        Spacer(Modifier.height(18.dp))
        AuroraGlassSectionTitle(
            LanguageManager.tr("Paar-Statistiken", appLanguage),
            Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                value = daysTogether.toString(),
                label = LanguageManager.tr("Gemeinsame Tage", appLanguage),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = answers.size.toString(),
                label = LanguageManager.tr("Beantwortete Fragen", appLanguage),
                onClick = { showAnswerList = true },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(stats.visitedCities.toString(), LanguageManager.tr("Besuchte Städte", appLanguage), Modifier.weight(1f))
            StatCard(stats.visitedCountries.toString(), LanguageManager.tr("Besuchte Länder", appLanguage), Modifier.weight(1f))
        }
    }

    if (showAnswerList) {
        AnswerHistoryDialog(
            answers = answers,
            profile = profile,
            appLanguage = appLanguage,
            onDismiss = { showAnswerList = false }
        )
    }
}

@Composable
private fun PicShareHomeCard(
    pics: List<SharedPicEntity>,
    profile: ProfileEntity,
    onAddPictures: (List<Uri>, String) -> Unit,
    onUpdatePicture: (SharedPicEntity) -> Unit,
    onPinWidget: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showManager by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) onAddPictures(uris, "me")
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(HarmonyPink.copy(alpha = 0.30f), HarmonyPurple.copy(alpha = 0.28f), HarmonySurface2)
                )
            )
            .border(1.4.dp, HarmonyPink.copy(alpha = 0.62f), RoundedCornerShape(26.dp))
            .clickable { showManager = true }
            .padding(16.dp)
            .testTag("picshare_home_card")
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).background(HarmonyPink.copy(alpha = 0.22f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = HarmonyPinkSoft)
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("PicShare für euch", color = HarmonyText, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    Text("${pics.size} Bilder · auf diesem Gerät bereit", color = HarmonyMuted, fontSize = 11.5.sp)
                    pics.firstOrNull()?.caption?.takeIf { it.isNotBlank() }?.let { currentCaption ->
                        Text(
                            "„$currentCaption“",
                            color = HarmonyPinkSoft,
                            fontSize = 10.5.sp,
                            maxLines = 1
                        )
                    }
                }
                PicPreviewStack(pics.take(3))
            }
            Spacer(Modifier.height(13.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                CompactAction(
                    label = "Bilder",
                    icon = Icons.Default.AddPhotoAlternate,
                    onClick = { picker.launch("image/*") },
                    modifier = Modifier.weight(1f)
                )
                CompactAction(
                    label = "Widget",
                    icon = Icons.Default.Widgets,
                    onClick = onPinWidget,
                    modifier = Modifier.weight(1f)
                )
                CompactAction(
                    label = "Status",
                    icon = Icons.Default.Home,
                    onClick = { showManager = true },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(9.dp))
            Text(
                "Partner-Synchronisierung folgt mit der späteren Verknüpfung · beide dürfen bearbeiten",
                color = HarmonyPinkSoft,
                fontSize = 10.5.sp,
                lineHeight = 14.sp
            )
        }
    }

    if (showManager) {
        PicShareManagerDialog(
            pics = pics,
            profile = profile,
            onAddPictures = { picker.launch("image/*") },
            onUpdatePicture = onUpdatePicture,
            onPinWidget = onPinWidget,
            onDismiss = { showManager = false }
        )
    }
}

@Composable
private fun PicShareManagerDialog(
    pics: List<SharedPicEntity>,
    profile: ProfileEntity,
    onAddPictures: () -> Unit,
    onUpdatePicture: (SharedPicEntity) -> Unit,
    onPinWidget: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val latest = pics.firstOrNull()
    val savedSettings = remember { PicShareWidgetPreferences.load(context) }
    var caption by remember(latest?.id) {
        mutableStateOf(savedSettings.caption.ifBlank { latest?.caption.orEmpty() })
    }
    var target by remember(latest?.id) { mutableStateOf(latest?.target ?: "partner_home") }
    var showCaption by remember { mutableStateOf(savedSettings.showCaption) }
    var showStatus by remember { mutableStateOf(savedSettings.showStatus) }
    var shufflePictures by remember { mutableStateOf(savedSettings.shufflePictures) }
    val selectedCount = pics.count { it.selectedForWidget }
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("picshare_manager_dialog"),
        title = {
            Column {
                Text("PicShare Widget", fontWeight = FontWeight.ExtraBold)
                Text("Kompakt einrichten · Wechsel alle 6 Sekunden", color = HarmonyMuted, fontSize = 11.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 540.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("●", color = Color(0xFF65E8B2), fontSize = 19.sp)
                    Spacer(Modifier.width(7.dp))
                    Column {
                        Text("$selectedCount von ${pics.size} Bildern im Widget", fontWeight = FontWeight.Bold)
                        Text("${profile.partnerName}: Verknüpfung folgt später", color = HarmonyMuted, fontSize = 11.sp)
                    }
                }
                if (pics.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text("Bilder auswählen", color = HarmonyText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Antippen, um ein Bild ein- oder auszublenden", color = HarmonyMuted, fontSize = 10.5.sp)
                    Spacer(Modifier.height(7.dp))
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pics.take(8).forEach { pic ->
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onUpdatePicture(pic.copy(selectedForWidget = !pic.selectedForWidget)) }
                                    .border(
                                        if (pic.selectedForWidget) 2.dp else 1.dp,
                                        if (pic.selectedForWidget) HarmonyPink else HarmonyLine,
                                        RoundedCornerShape(14.dp)
                                    )
                            ) {
                                AsyncImage(
                                    model = File(pic.filePath),
                                    contentDescription = "PicShare Bild auswählen",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (pic.selectedForWidget) {
                                    Box(
                                        Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(5.dp)
                                            .size(20.dp)
                                            .background(HarmonyPink, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✓", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = caption,
                        onValueChange = { caption = it },
                        label = { Text("Widget-Text") },
                        supportingText = { Text("Dieser Text gilt für alle rotierenden Bilder.", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HarmonyPink,
                            unfocusedBorderColor = HarmonyLine,
                            focusedTextColor = HarmonyText,
                            unfocusedTextColor = HarmonyText
                        ),
                        shape = RoundedCornerShape(15.dp)
                    )
                    Spacer(Modifier.height(5.dp))
                    PicShareSettingToggle("Widget-Text anzeigen", showCaption) { showCaption = it }
                    PicShareSettingToggle("Harmony-Statuszeile anzeigen", showStatus) { showStatus = it }
                    PicShareSettingToggle("Bildreihenfolge mischen", shufflePictures) { shufflePictures = it }
                    Spacer(Modifier.height(8.dp))
                    Text("Ziel nach der Verknüpfung", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        TargetChip("Startbildschirm", target == "partner_home") { target = "partner_home" }
                        TargetChip("Sperrbildschirm", target == "partner_lock") { target = "partner_lock" }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactAction("Bilder", Icons.Default.AddPhotoAlternate, onAddPictures, Modifier.weight(1f))
                    CompactAction("Widget", Icons.Default.Widgets, onPinWidget, Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val settings = PicShareWidgetSettings(
                        caption = caption,
                        showCaption = showCaption,
                        showStatus = showStatus,
                        shufflePictures = shufflePictures
                    )
                    PicShareWidgetPreferences.save(context, settings)
                    latest?.let { onUpdatePicture(it.copy(caption = caption.trim(), target = target)) }
                    PicShareWidgetProvider.refreshAll(context)
                    onDismiss()
                },
                modifier = Modifier.testTag("save_picshare_widget_settings")
            ) { Text("Speichern", color = HarmonyPink, fontWeight = FontWeight.ExtraBold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen", color = HarmonyMuted) } }
    )
}

@Composable
private fun PicShareSettingToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = HarmonyText, fontSize = 11.5.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = Modifier.size(width = 48.dp, height = 30.dp))
    }
}

@Composable
private fun TargetChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (selected) Color.White else HarmonyMuted,
        fontSize = 10.5.sp,
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) HarmonyPurple else Color.White.copy(alpha = 0.06f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp)
    )
}

@Composable
private fun CompactAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = HarmonyPinkSoft, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(5.dp))
        Text(label, color = HarmonyText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PicPreviewStack(pics: List<SharedPicEntity>) {
    Box(modifier = Modifier.width(70.dp).height(45.dp)) {
        pics.forEachIndexed { index, pic ->
            AsyncImage(
                model = File(pic.filePath),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .offset(x = (index * 13).dp)
                    .size(43.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, HarmonySurface2, RoundedCornerShape(12.dp))
            )
        }
        if (pics.isEmpty()) {
            Box(Modifier.size(43.dp).background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Text("＋", color = HarmonyPink, fontSize = 24.sp)
            }
        }
    }
}

@Composable
private fun AnswerHistoryDialog(
    answers: List<AnswerEntity>,
    profile: ProfileEntity,
    appLanguage: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Beantwortete Fragen", fontWeight = FontWeight.ExtraBold) },
        text = {
            if (answers.isEmpty()) {
                Text("Noch keine Antworten gespeichert.", color = HarmonyMuted)
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 470.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    items(answers.sortedByDescending { it.timestamp }, key = { "${it.packId}-${it.questionIndex}" }) { answer ->
                        val rawPack = HarmonyPacksData.PACKS.firstOrNull { it.id == answer.packId }
                        val pack = rawPack?.let { LanguageManager.translatePack(it, appLanguage) }
                        val question = when {
                            pack == null -> "Frage ${answer.questionIndex + 1}"
                            pack.type == "tot" -> pack.pairs.getOrNull(answer.questionIndex)?.let { "${it.first}  ↔  ${it.second}" }
                            else -> pack.questions.getOrNull(answer.questionIndex)?.q
                        } ?: "Frage ${answer.questionIndex + 1}"
                        val coupleChoice = EitherOrAnswerCodec.decode(answer.answerText)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.055f), RoundedCornerShape(16.dp))
                                .border(1.dp, HarmonyLine, RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Text(pack?.title ?: answer.packId, color = HarmonyPinkSoft, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            Text(question, color = HarmonyText, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp)
                            Spacer(Modifier.height(6.dp))
                            if (coupleChoice != null) {
                                Text("${profile.userName}: ${coupleChoice.userChoice}", color = HarmonyMuted, fontSize = 12.sp)
                                Text("${profile.partnerName}: ${coupleChoice.partnerChoice}", color = HarmonyMuted, fontSize = 12.sp)
                            } else {
                                Text(answer.answerText, color = HarmonyMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Schließen", color = HarmonyPink) } }
    )
}

@Composable
fun PaddingPackCard(
    appLanguage: String,
    pack: QuestionPack,
    answers: List<AnswerEntity>,
    onStartPack: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val answeredCount = answers.count { it.packId == pack.id }
    val totalCount = if (pack.type == "tot") pack.pairs.size else pack.questions.size
    val isDone = answeredCount >= totalCount && totalCount > 0
    val topicAccent = topicAccentColor(pack.topic)

    HarmonyCard(modifier = modifier.testTag("pack_card_${pack.id}"), onClick = { onStartPack(pack.id) }, accent = topicAccent) {
        Column {
            Row { pack.tags.forEach { tag -> CategoryTag(tag, Modifier.padding(end = 6.dp)) } }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                HarmonyPackIcon(pack = pack, accent = topicAccent, size = 42.dp)
                Spacer(Modifier.width(10.dp))
                Text(pack.title, color = HarmonyText, fontSize = 16.5.sp, fontWeight = FontWeight.Bold, lineHeight = 22.sp, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(26.dp).clip(CircleShape).background(Brush.linearGradient(listOf(topicAccent, topicAccent.copy(alpha = 0.72f)))), contentAlignment = Alignment.Center) {
                        Text(if (answeredCount > 0) "✓" else "?", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(Modifier.offset(x = (-8).dp).size(26.dp).clip(CircleShape).background(Brush.linearGradient(listOf(HarmonyPurple, HarmonyPurpleLight))), contentAlignment = Alignment.Center) {
                        Text("?", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (isDone) LanguageManager.tr("ERGEBNISSE", appLanguage) else LanguageManager.tr("BEANTWORTE", appLanguage),
                        color = topicAccent,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.9.sp
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = topicAccent, modifier = Modifier.size(14.dp))
                }
            }
            if (answeredCount in 1..<totalCount) {
                Spacer(Modifier.height(10.dp))
                AuroraProgressBar(answeredCount.toFloat() / totalCount, topicAccent, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun ConnectBanner(appLanguage: String, partnerName: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(HarmonyPink.copy(alpha = 0.14f), HarmonyPurple.copy(alpha = 0.14f))))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(42.dp).clip(CircleShape).background(HarmonyPink.copy(alpha = 0.18f)).border(1.dp, HarmonyPink.copy(alpha = 0.58f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Favorite, contentDescription = null, tint = HarmonyPinkSoft, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("${LanguageManager.tr("Verbinde dich mit", appLanguage)} $partnerName", color = HarmonyText, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
            Text(LanguageManager.tr("Beantwortet Fragen gleichzeitig — Antworten werden erst sichtbar, wenn ihr beide fertig seid.", appLanguage), color = HarmonyMuted, fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
fun StatCard(value: String, label: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(HarmonySurface2, HarmonySurface)))
            .border(1.dp, HarmonyLine, RoundedCornerShape(18.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(15.dp)
    ) {
        Column {
            Text(value, color = HarmonyText, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(4.dp))
            Text(label, color = HarmonyMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            if (onClick != null) Text("Liste öffnen", color = HarmonyPinkSoft, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
        }
    }
}
