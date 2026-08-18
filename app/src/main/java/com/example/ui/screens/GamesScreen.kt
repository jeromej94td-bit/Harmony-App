package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.util.LanguageManager
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnswerEntity
import com.example.data.model.Category
import com.example.data.model.HarmonyPacksData
import com.example.data.model.Topic
import com.example.ui.components.AuroraGlassSectionTitle
import com.example.ui.components.TimerPill
import com.example.ui.theme.HarmonyGold
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
    appLanguage: String = "de",
    onSetFilter: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onTopicClick: (String) -> Unit,
    onStartPack: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    // Find daily pack
    val answeredPackIds = answers.groupBy { it.packId }.keys
    val rawDailyPack = HarmonyPacksData.PACKS.find { it.id !in answeredPackIds } ?: HarmonyPacksData.PACKS.first()
    val dailyPack = LanguageManager.translatePack(rawDailyPack, appLanguage)

    // Filter search results over title, category, topic, tags, questions, and option choices
    val trimmedQuery = searchQuery.trim().lowercase()
    val searchResults = if (trimmedQuery.isNotEmpty()) {
        HarmonyPacksData.PACKS.filter { pack ->
            val matchesTitle = pack.title.lowercase().contains(trimmedQuery)

            val catObj = HarmonyPacksData.CATEGORIES.find { it.id == pack.cat }
            val matchesCat = pack.cat.lowercase().contains(trimmedQuery) ||
                    (catObj?.name?.lowercase()?.contains(trimmedQuery) == true)

            val topicObj = HarmonyPacksData.TOPICS.find { it.id == pack.topic }
            val matchesTopic = pack.topic.lowercase().contains(trimmedQuery) ||
                    (topicObj?.name?.lowercase()?.contains(trimmedQuery) == true)

            val matchesTags = pack.tags.any { it.lowercase().contains(trimmedQuery) }

            val matchesQuestions = pack.questions.any { q ->
                q.q.lowercase().contains(trimmedQuery) ||
                        q.options.any { opt -> opt.lowercase().contains(trimmedQuery) }
            }

            val matchesPairs = pack.pairs.any { pair ->
                pair.first.lowercase().contains(trimmedQuery) ||
                        pair.second.lowercase().contains(trimmedQuery)
            }

            matchesTitle || matchesCat || matchesTopic || matchesTags || matchesQuestions || matchesPairs
        }.filter { pack ->
            val totalCount = if (pack.type == "tot") pack.pairs.size else pack.questions.size
            val ansCount = answers.count { it.packId == pack.id }
            val isDone = ansCount >= totalCount && totalCount > 0
            when (packFilter) {
                "open" -> !isDone
                "done" -> isDone
                else -> true
            }
        }
    } else null

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 90.dp)
    ) {
        // Top Header with Lupe Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = LanguageManager.tr("Fragen & Spiele", appLanguage),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = HarmonyText
            )

            IconButton(
                onClick = {
                    isSearchActive = !isSearchActive
                    if (!isSearchActive) {
                        searchQuery = ""
                    }
                },
                modifier = Modifier.testTag("search_icon_button")
            ) {
                Icon(
                    imageVector = if (isSearchActive && searchQuery.isEmpty()) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = if (isSearchActive) LanguageManager.tr("Suche schließen", appLanguage) else LanguageManager.tr("Suche öffnen", appLanguage),
                    tint = if (isSearchActive) HarmonyPink else HarmonyText
                )
            }
        }

        // Search Input Field with automatic keyboard focus
        AnimatedVisibility(
            visible = isSearchActive || searchQuery.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = LanguageManager.tr("Titel, Kategorie, Thema, Tags, Fragen...", appLanguage),
                            color = HarmonyMuted,
                            fontSize = 13.5.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = HarmonyPink,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.testTag("clear_search_text_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Suchfeld löschen",
                                    tint = HarmonyMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    isSearchActive = false
                                    searchQuery = ""
                                },
                                modifier = Modifier.testTag("close_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Suche schließen",
                                    tint = HarmonyMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = HarmonySurface2,
                        unfocusedContainerColor = HarmonySurface2,
                        disabledContainerColor = HarmonySurface2,
                        focusedBorderColor = HarmonyPink,
                        unfocusedBorderColor = HarmonyLine,
                        focusedTextColor = HarmonyText,
                        unfocusedTextColor = HarmonyText
                    ),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { keyboardController?.hide() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .testTag("search_input_field")
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Filter Chips
        FilterChipsRow(
            selectedFilter = packFilter,
            onFilterSelected = onSetFilter,
            appLanguage = appLanguage,
            modifier = Modifier.padding(horizontal = 18.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (searchResults != null) {
            // Search Mode Results View
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageManager.tr("Suchergebnisse", appLanguage),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = HarmonyText
                )
                Text(
                    text = "${searchResults.size} " + LanguageManager.tr("Treffer", appLanguage),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = HarmonyMuted
                )
            }

            if (searchResults.isEmpty()) {
                // Keine Treffer Meldung
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp, horizontal = 24.dp)
                        .testTag("no_search_results_view"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔍",
                        fontSize = 42.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = LanguageManager.tr("Keine Treffer gefunden", appLanguage),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarmonyText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = LanguageManager.tr("Für diese Suche wurden keine passenden Fragen oder Spiele gefunden.", appLanguage),
                        fontSize = 13.5.sp,
                        color = HarmonyMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.horizontalGradient(listOf(HarmonyPink, HarmonyPurple)))
                            .clickable { searchQuery = "" }
                            .padding(horizontal = 20.dp, vertical = 11.dp)
                            .testTag("clear_search_button")
                    ) {
                        Text(
                            text = LanguageManager.tr("Suche zurücksetzen", appLanguage),
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                searchResults.forEach { pack ->
                    val translatedPack = LanguageManager.translatePack(pack, appLanguage)
                    PaddingPackCard(
                        appLanguage = appLanguage,
                        pack = translatedPack,
                        answers = answers,
                        onStartPack = onStartPack,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                    )
                }
            }
        } else {
            // Normal View
            // Categories Header
            Text(
                text = LanguageManager.tr("Kategorien", appLanguage),
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
                    val translatedCategory = LanguageManager.translateCategory(category, appLanguage)
                    CategoryRailCard(
                        category = translatedCategory,
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
                    text = "🔥 " + LanguageManager.tr("Tägliche Aktivität", appLanguage),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = HarmonyText
                )
                TimerPill()
            }

            PaddingPackCard(
                appLanguage = appLanguage,
                pack = dailyPack,
                answers = answers,
                onStartPack = onStartPack,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Topics Progress Header
            AuroraGlassSectionTitle(LanguageManager.tr("Themen", appLanguage), Modifier.padding(horizontal = 18.dp, vertical = 4.dp))

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

                val translatedTopic = LanguageManager.translateTopic(topic, appLanguage)
                TopicProgressCard(
                    topic = translatedTopic,
                    percentage = pct,
                    onClick = { onTopicClick(topic.id) },
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
fun FilterChipsRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    appLanguage: String = "de",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val filters = listOf(
            "all" to LanguageManager.tr("Alle", appLanguage),
            "open" to ("🟠 " + LanguageManager.tr("Du bist dran", appLanguage)),
            "done" to ("✅ " + LanguageManager.tr("Beantwortet", appLanguage))
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
    val accent = Color(category.tagColorHex)
    Box(
        modifier = Modifier
            .size(width = 104.dp, height = 112.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.22f), HarmonySurface2)))
            .border(1.dp, accent.copy(alpha = 0.48f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag("category_card_${category.id}")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = if (category.id == "moral") "⚖️" else category.emoji, fontSize = 26.sp)
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
    val accent = when (topic.id) {
        "moral" -> HarmonyGold
        "geld" -> HarmonyTeal
        "beziehung" -> HarmonyPink
        "sex" -> Color(0xFFFF5A6E)
        else -> HarmonyPurpleLight
    }
    val infiniteTransition = rememberInfiniteTransition(label = "aurora_topic")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradientShift"
    )

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.25f),
            Color.White.copy(alpha = 0.12f),
            Color.White.copy(alpha = 0.25f)
        ),
        start = Offset(0f, 0f),
        end = Offset(400f, 500f)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.20f), HarmonySurface.copy(alpha = 0.96f))))
            .border(1.dp, accent.copy(alpha = 0.48f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag("topic_card_${topic.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = if (topic.id == "moral") "⚖️" else topic.emoji, fontSize = 26.sp)
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
                            .background(Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.72f))))
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = if (percentage >= 100) "✓" else "$percentage%",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (percentage >= 100) accent else HarmonyMuted
            )
        }
    }
}
