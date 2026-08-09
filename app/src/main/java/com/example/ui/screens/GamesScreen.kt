package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import com.example.data.model.Category
import com.example.data.model.HarmonyPacksData
import com.example.data.model.Topic
import com.example.ui.components.HarmonyCard
import com.example.ui.contentText
import com.example.ui.tr
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
import com.example.ui.theme.HarmonyTeal
import com.example.ui.theme.HarmonyText

@Composable
fun GamesScreen(
    answers: List<AnswerEntity>,
    packFilter: String,
    onSetFilter: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onTopicClick: (String) -> Unit,
    onStartPack: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Find daily pack
    val answeredPackIds = answers.groupBy { it.packId }.keys
    val dailyPack = HarmonyPacksData.PACKS.find { it.id !in answeredPackIds } ?: HarmonyPacksData.PACKS.first()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 112.dp)
    ) {
        // Section Title
        Text(
            text = tr("Fragen & Spiele", "Questions & Games"),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = HarmonyText,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
        )

        // Filter Chips
        FilterChipsRow(
            selectedFilter = packFilter,
            onFilterSelected = onSetFilter,
            modifier = Modifier.padding(horizontal = 18.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Categories Header
        Text(
            text = tr("Kategorien", "Categories"),
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = HarmonyText,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
        )

        // Horizontal Category Rail
        LazyRow(
            contentPadding = PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(HarmonyPacksData.CATEGORIES) { category ->
                CategoryRailCard(
                    category = category,
                    onClick = { onCategoryClick(category.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Daily Activity Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tr("🔥 Tägliche Aktivität", "🔥 Daily activity"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = HarmonyText
            )
            TimerPill()
        }

        PaddingPackCard(
            pack = dailyPack,
            answers = answers,
            onStartPack = onStartPack,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Topics Progress Header
        Text(
            text = tr("Themen", "Topics"),
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = HarmonyText,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
        )

        // Topics List with progress bars
        HarmonyPacksData.TOPICS.forEach { topic ->
            val packsForTopic = HarmonyPacksData.PACKS.filter { it.topic == topic.id }
            val donePacksCount = packsForTopic.count { pack ->
                val totalLen = if (pack.type == "tot") pack.pairs.size else pack.questions.size
                val ansCount = answers.count { it.packId == pack.id }
                ansCount >= totalLen && totalLen > 0
            }

            val pct = if (packsForTopic.isNotEmpty()) {
                (donePacksCount.toFloat() / packsForTopic.size * 100).toInt()
            } else 0

            TopicProgressCard(
                topic = topic,
                percentage = pct,
                onClick = { onTopicClick(topic.id) },
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
fun FilterChipsRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val filters = listOf(
            "all" to tr("Alle", "All"),
            "open" to tr("🟠 Du bist dran", "🟠 Your turn"),
            "done" to tr("✅ Beantwortet", "✅ Answered")
        )

        filters.forEach { (filterKey, label) ->
            val isSelected = selectedFilter == filterKey
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Brush.horizontalGradient(listOf(HarmonyPink, HarmonyPurple))
                        else Brush.linearGradient(listOf(Color.White.copy(alpha = 0.04f), Color.White.copy(alpha = 0.04f)))
                    )
                    .border(
                        1.dp,
                        if (isSelected) Color.Transparent else HarmonyLine,
                        CircleShape
                    )
                    .clickable { onFilterSelected(filterKey) }
                    .padding(horizontal = 17.dp, vertical = 11.dp)
                    .testTag("filter_chip_$filterKey")
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else HarmonyMuted
                )
            }
        }
    }
}

@Composable
fun CategoryRailCard(category: Category, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 126.dp, height = 148.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(category.tagColorHex).copy(alpha = 0.34f),
                        HarmonySurface2,
                        HarmonySurface
                    )
                )
            )
            .border(1.dp, Color(category.tagColorHex).copy(alpha = 0.58f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
            .testTag("category_card_${category.id}")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = category.emoji, fontSize = 24.sp)
            Text(
                text = contentText(category.name),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = HarmonyText,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun TopicProgressCard(
    topic: Topic,
    percentage: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = topicAccent(topic.id)
    val pulseTransition = rememberInfiniteTransition(label = "topic_pulse_${topic.id}")
    val pulse by pulseTransition.animateFloat(
        initialValue = 0.42f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "topic_glow_${topic.id}"
    )
    val cardShape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        HarmonySurface2.copy(alpha = 0.98f),
                        accent.copy(alpha = 0.14f + pulse * 0.10f),
                        HarmonySurface.copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                width = 1.35.dp,
                brush = Brush.linearGradient(
                    listOf(
                        accent.copy(alpha = 0.58f + pulse * 0.24f),
                        accent.copy(alpha = 0.18f),
                        accent.copy(alpha = 0.40f + pulse * 0.18f)
                    )
                ),
                shape = cardShape
            )
            .clickable(onClick = onClick)
            .padding(18.dp)
            .testTag("topic_card_${topic.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = topic.emoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contentText(topic.name),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = HarmonyText
                )
                Spacer(modifier = Modifier.height(7.dp))
                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percentage.coerceIn(0, 100) / 100f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(accent, accent.copy(alpha = 0.78f), accent.copy(alpha = 0.46f))
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = if (percentage >= 100) "✓" else "$percentage%",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (percentage >= 100) accent else HarmonyMuted
            )
        }
    }
}

private fun topicAccent(topicId: String): Color = when (topicId) {
    // Gedämpfte Edelsteinpalette: bewusst hochwertig, klar unterscheidbar, nie neon.
    "aufwaermen" -> Color(0xFFC39A4B) // Antikgold
    "beziehung" -> Color(0xFFB66A78) // Dusty Rose
    "sex" -> Color(0xFFA8614E) // Terrakotta
    "moral" -> Color(0xFF76678F) // Amethyst
    "geld" -> Color(0xFF6F8C76) // Salbeigrün
    "kennen" -> Color(0xFFA47A5B) // Kupfer
    "reisen" -> Color(0xFF617A99) // Saphirblau
    "familie" -> Color(0xFF8C895C) // Olive
    "hobbys" -> Color(0xFF4F8580) // Petrol
    else -> Color(0xFF98758A)
}
