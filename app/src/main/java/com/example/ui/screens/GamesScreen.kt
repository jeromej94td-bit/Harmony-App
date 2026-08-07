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
import com.example.ui.components.TimerPill
import com.example.ui.theme.HarmonyBg
import com.example.ui.theme.HarmonyLine
import com.example.ui.theme.HarmonyMuted
import com.example.ui.theme.HarmonyPink
import com.example.ui.theme.HarmonyPinkSoft
import com.example.ui.theme.HarmonyPurple
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
            .padding(bottom = 90.dp)
    ) {
        // Section Title
        Text(
            text = "Fragen & Spiele",
            fontSize = 20.sp,
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
            text = "Kategorien",
            fontSize = 17.sp,
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
                text = "🔥 Tägliche Aktivität",
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
            text = "Themen",
            fontSize = 17.sp,
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val filters = listOf(
            "all" to "Alle",
            "open" to "🟠 Du bist dran",
            "done" to "✅ Beantwortet"
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
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("filter_chip_$filterKey")
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
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
            .size(width = 104.dp, height = 112.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(HarmonySurface2, HarmonySurface)))
            .border(1.dp, HarmonyLine, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag("category_card_${category.id}")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = category.emoji, fontSize = 24.sp)
            Text(
                text = category.name,
                fontSize = 12.sp,
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(HarmonySurface2, HarmonySurface)))
            .border(1.dp, HarmonyLine, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
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
                    text = topic.name,
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
                            .background(Brush.horizontalGradient(listOf(HarmonyPink, HarmonyPurple)))
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = if (percentage >= 100) "✓" else "$percentage%",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (percentage >= 100) HarmonyTeal else HarmonyMuted
            )
        }
    }
}
