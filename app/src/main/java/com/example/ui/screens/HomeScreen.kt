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
import com.example.ui.components.Text
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
import com.example.data.model.isAvailableIn
import com.example.ui.LocalAppLanguage
import com.example.data.model.ProfileEntity
import com.example.data.model.QuestionPack
import com.example.ui.components.CategoryTag
import com.example.ui.contentText
import com.example.ui.tr
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

@Composable
fun HomeScreen(
    profile: ProfileEntity,
    answers: List<AnswerEntity>,
    stats: CoupleStatsEntity,
    onStartPack: (String) -> Unit,
    onSendWidget: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val language = LocalAppLanguage.current
    val availablePacks = HarmonyPacksData.PACKS.filter { it.isAvailableIn(language.code) }

    // Find daily pack (first unanswered or default to first pack)
    val answeredPackIds = answers.groupBy { it.packId }.keys
    val dailyPack = availablePacks.find { it.id !in answeredPackIds } ?: availablePacks.first()
    val recommendedPacks = availablePacks.filter { it.id != dailyPack.id }.take(3)

    // Calculate days together
    val daysTogether = TimeUnit.MILLISECONDS.toDays(
        (System.currentTimeMillis() - profile.startDate).coerceAtLeast(0)
    ).toInt()
    val totalAnswersCount = answers.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 112.dp)
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
                text = tr("🔥 Tägliche Aktivität", "🔥 Daily activity"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = HarmonyText
            )
            TimerPill()
        }

        // Daily Question Card
        PaddingPackCard(
            pack = dailyPack,
            answers = answers,
            onStartPack = onStartPack,
            modifier = Modifier.padding(horizontal = 18.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Connect banner
        ConnectBanner(
            partnerName = profile.partnerName,
            modifier = Modifier.padding(horizontal = 18.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Recommendations Section
        Text(
            text = tr("Für dich empfohlen", "Recommended for you"),
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = HarmonyText,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        recommendedPacks.forEach { pack ->
            PaddingPackCard(
                pack = pack,
                answers = answers,
                onStartPack = onStartPack,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Quick Widgets Section
        Text(
            text = tr("Widgets", "Widgets"),
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = HarmonyText,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            WidgetCard(
                title = tr("Du fehlst mir", "I miss you"),
                emoji = "🥺",
                onClick = { onSendWidget(tr("Du fehlst mir", "I miss you"), "🥺") },
                modifier = Modifier.weight(1f)
            )
            WidgetCard(
                title = tr("Denke an dich", "Thinking of you"),
                emoji = "💭",
                onClick = { onSendWidget(tr("Ich denke an dich", "Thinking of you"), "💭") },
                modifier = Modifier.weight(1f)
            )
            WidgetCard(
                title = tr("Kuss senden", "Send a kiss"),
                emoji = "😘",
                onClick = { onSendWidget(tr("Kuss", "Kiss"), "😘") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Couple Statistics Section
        Text(
            text = tr("Paar-Statistiken", "Couple statistics"),
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = HarmonyText,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(value = daysTogether.toString(), label = tr("Gemeinsame Tage", "Days together"), modifier = Modifier.weight(1f))
            StatCard(value = totalAnswersCount.toString(), label = tr("Beantwortete Fragen", "Questions answered"), modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(value = stats.visitedCities.toString(), label = tr("Besuchte Städte", "Cities visited"), modifier = Modifier.weight(1f))
            StatCard(value = stats.visitedCountries.toString(), label = tr("Besuchte Länder", "Countries visited"), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun PaddingPackCard(
    pack: QuestionPack,
    answers: List<AnswerEntity>,
    onStartPack: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val packAnswers = answers.filter { it.packId == pack.id }
    val answeredCount = packAnswers.size
    val totalCount = if (pack.type == "tot") pack.pairs.size else pack.questions.size
    val isDone = answeredCount >= totalCount && totalCount > 0

    HarmonyCard(
        modifier = modifier.testTag("pack_card_${pack.id}"),
        onClick = { onStartPack(pack.id) }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(packAccent(pack.cat), packAccent(pack.cat).copy(alpha = 0.35f))
                        )
                    )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row {
                pack.tags.forEach { tag ->
                    CategoryTag(tag = contentText(tag), modifier = Modifier.padding(end = 6.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val packEmoji = pack.emoji.ifBlank {
                com.example.data.model.HarmonyPacksData.CATEGORIES.find { it.id == pack.cat }?.emoji ?: "🎯"
            }

            Text(
                text = "$packEmoji  ${contentText(pack.title)}",
                fontSize = 18.5.sp,
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
                        text = if (isDone) tr("ERGEBNISSE", "RESULTS") else tr("BEANTWORTE", "ANSWER"),
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

private fun packAccent(categoryId: String): Color =
    HarmonyPacksData.CATEGORIES.find { it.id == categoryId }?.tagColorHex?.let { Color(it) } ?: HarmonyPink

@Composable
fun ConnectBanner(partnerName: String, modifier: Modifier = Modifier) {
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
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "💞", fontSize = 24.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = tr("Verbinde dich mit $partnerName", "Connect with $partnerName"),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = HarmonyText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tr("Beantwortet Fragen gleichzeitig — Antworten werden erst sichtbar, wenn ihr beide fertig seid.", "Answer questions at the same time — answers become visible when you are both finished."),
                fontSize = 13.5.sp,
                color = HarmonyMuted,
                lineHeight = 19.sp
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
            .padding(vertical = 18.dp, horizontal = 10.dp)
            .testTag("widget_$title"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 26.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 13.5.sp,
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
                fontSize = 12.5.sp,
                color = HarmonyMuted,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
