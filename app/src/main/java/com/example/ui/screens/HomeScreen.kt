package com.example.ui.screens

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnswerEntity
import com.example.data.model.CoupleStatsEntity
import com.example.data.model.HarmonyPacksData
import com.example.data.model.ProfileEntity
import com.example.data.model.QuestionPack
import com.example.ui.components.AuroraGlassSectionTitle
import com.example.ui.components.CategoryTag
import com.example.ui.components.HarmonyCard
import com.example.ui.components.TimerPill
import com.example.ui.theme.HarmonyBg
import com.example.ui.theme.HarmonyLine
import com.example.ui.theme.HarmonyMuted
import com.example.ui.theme.HarmonyPink
import com.example.ui.theme.HarmonyPinkSoft
import com.example.ui.theme.HarmonyPurple
import com.example.ui.theme.HarmonyPurpleLight
import com.example.ui.theme.HarmonySurface
import com.example.ui.theme.HarmonySurface2
import com.example.ui.theme.HarmonyText
import java.util.concurrent.TimeUnit


import com.example.util.LanguageManager

@Composable
fun HomeScreen(
    profile: ProfileEntity,
    answers: List<AnswerEntity>,
    stats: CoupleStatsEntity,
    isRefreshing: Boolean = false,
    appLanguage: String = "de",
    onRefresh: () -> Unit = {},
    onStartPack: (String) -> Unit,
    onSendWidget: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Find daily pack (first unanswered or default to first pack)
    val answeredPackIds = answers.groupBy { it.packId }.keys
    val rawDailyPack = HarmonyPacksData.PACKS.find { it.id !in answeredPackIds } ?: HarmonyPacksData.PACKS.first()
    val dailyPack = LanguageManager.translatePack(rawDailyPack, appLanguage)
    val recommendedPacks = HarmonyPacksData.PACKS.filter { it.id != rawDailyPack.id }.take(3).map {
        LanguageManager.translatePack(it, appLanguage)
    }

    // Calculate days together
    val daysTogether = TimeUnit.MILLISECONDS.toDays(
        (System.currentTimeMillis() - profile.startDate).coerceAtLeast(0)
    ).toInt()
    val totalAnswersCount = answers.size

    Column(
        // isRefreshing = isRefreshing,
        // onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 90.dp)
        ) {
        // Daily Activity Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔥 " + LanguageManager.tr("Tägliche Aktivität", appLanguage),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = HarmonyText
            )
            TimerPill()
        }

        // Daily Question Card
        PaddingPackCard(
            appLanguage = appLanguage,
            pack = dailyPack,
            answers = answers,
            onStartPack = onStartPack,
            modifier = Modifier.padding(horizontal = 18.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Connect banner
        ConnectBanner(
            appLanguage = appLanguage,
            partnerName = profile.partnerName,
            modifier = Modifier.padding(horizontal = 18.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Recommendations Section
        AuroraGlassSectionTitle(LanguageManager.tr("Für dich empfohlen", appLanguage), Modifier.padding(horizontal = 18.dp, vertical = 4.dp))

        recommendedPacks.forEach { pack ->
            PaddingPackCard(
                appLanguage = appLanguage,
                pack = pack,
                answers = answers,
                onStartPack = onStartPack,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Quick Widgets Section
        AuroraGlassSectionTitle(LanguageManager.tr("Widgets", appLanguage), Modifier.padding(horizontal = 18.dp, vertical = 4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            WidgetCard(
                title = LanguageManager.tr("Du fehlst mir", appLanguage),
                emoji = "🥺",
                onClick = { onSendWidget(LanguageManager.tr("Du fehlst mir", appLanguage), "🥺") },
                modifier = Modifier.weight(1f)
            )
            WidgetCard(
                title = LanguageManager.tr("Denke an dich", appLanguage),
                emoji = "💭",
                onClick = { onSendWidget(LanguageManager.tr("Denke an dich", appLanguage), "💭") },
                modifier = Modifier.weight(1f)
            )
            WidgetCard(
                title = LanguageManager.tr("Kuss senden", appLanguage),
                emoji = "😘",
                onClick = { onSendWidget(LanguageManager.tr("Kuss senden", appLanguage), "😘") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Couple Statistics Section
        AuroraGlassSectionTitle(LanguageManager.tr("Paar-Statistiken", appLanguage), Modifier.padding(horizontal = 18.dp, vertical = 4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(value = daysTogether.toString(), label = LanguageManager.tr("Gemeinsame Tage", appLanguage), modifier = Modifier.weight(1f))
            StatCard(value = totalAnswersCount.toString(), label = LanguageManager.tr("Beantwortete Fragen", appLanguage), modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(value = stats.visitedCities.toString(), label = LanguageManager.tr("Besuchte Städte", appLanguage), modifier = Modifier.weight(1f))
            StatCard(value = stats.visitedCountries.toString(), label = LanguageManager.tr("Besuchte Länder", appLanguage), modifier = Modifier.weight(1f))
        }
    }
}
}

@Composable
fun PaddingPackCard(
    appLanguage: String,
    pack: QuestionPack,
    answers: List<AnswerEntity>,
    onStartPack: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val packAnswers = answers.filter { it.packId == pack.id }
    val answeredCount = packAnswers.size
    val totalCount = if (pack.type == "tot") pack.pairs.size else pack.questions.size
    val isDone = answeredCount >= totalCount && totalCount > 0

    val topicAccent = when (pack.topic) {
        "moral" -> HarmonyGold
        "geld" -> HarmonyTeal
        "beziehung" -> HarmonyPink
        else -> HarmonyPurple
    }

    HarmonyCard(
        modifier = modifier.testTag("pack_card_${pack.id}"),
        onClick = { onStartPack(pack.id) },
        accent = topicAccent
    ) {
        Column {
            Row {
                pack.tags.forEach { tag ->
                    CategoryTag(tag = tag, modifier = Modifier.padding(end = 6.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val packEmoji = pack.emoji.ifBlank {
                com.example.data.model.HarmonyPacksData.CATEGORIES.find { it.id == pack.cat }?.emoji ?: "🎯"
            }

            Text(
                text = "$packEmoji  ${pack.title}",
                fontSize = 16.5.sp,
                fontWeight = FontWeight.Bold,
                color = HarmonyText,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(HarmonyPink, HarmonyPinkSoft))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (answeredCount > 0) "✓" else "?",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .offset(x = (-8).dp)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(HarmonyPurple, HarmonyPurpleLight))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "?",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = if (isDone) LanguageManager.tr("ERGEBNISSE", appLanguage) else LanguageManager.tr("BEANTWORTE", appLanguage),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = HarmonyPink,
                        letterSpacing = 0.9.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = HarmonyPink,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (answeredCount in 1..<totalCount) {
                Spacer(modifier = Modifier.height(10.dp))
                val progressFraction = answeredCount.toFloat() / totalCount
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction)
                            .height(5.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Brush.horizontalGradient(listOf(HarmonyPink, HarmonyPurple)))
                    )
                }
            }
        }
    }
}

@Composable
fun ConnectBanner(
    appLanguage: String,
    partnerName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(HarmonyPink.copy(alpha = 0.14f), HarmonyPurple.copy(alpha = 0.14f))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "💞", fontSize = 24.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "${LanguageManager.tr("Verbinde dich mit", appLanguage)} $partnerName",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = HarmonyText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = LanguageManager.tr("Beantwortet Fragen gleichzeitig — Antworten werden erst sichtbar, wenn ihr beide fertig seid.", appLanguage),
                fontSize = 12.sp,
                color = HarmonyMuted,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun WidgetCard(title: String, emoji: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(HarmonySurface2, HarmonySurface)))
            .border(1.dp, HarmonyLine, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp)
            .testTag("widget_$title"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 26.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = HarmonyText,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(HarmonySurface2, HarmonySurface)))
            .border(1.dp, HarmonyLine, RoundedCornerShape(18.dp))
            .padding(15.dp)
    ) {
        Column {
            Text(
                text = value,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = HarmonyText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = HarmonyMuted,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
