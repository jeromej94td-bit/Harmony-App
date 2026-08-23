package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.ui.components.AuroraProgressBar
import com.example.ui.components.HarmonyTopicIcon
import com.example.ui.components.GameCategoryVisual
import com.example.ui.components.TimerPill
import com.example.ui.introspection.IntrospectionPortal
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
import com.example.ui.theme.topicAccentColor
import kotlin.math.sin

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
    var showUnansweredQuestions by remember { mutableStateOf(false) }
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
        ) {
            Text(
                text = LanguageManager.tr("Fragen & Spiele", appLanguage),
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                color = HarmonyText,
                letterSpacing = 0.3.sp,
                modifier = Modifier.align(Alignment.Center)
            )

            IconButton(
                onClick = {
                    isSearchActive = !isSearchActive
                    if (!isSearchActive) {
                        searchQuery = ""
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(HarmonyPink.copy(alpha = if (isSearchActive) 0.28f else 0.13f), Color.Transparent)
                        )
                    )
                    .border(1.dp, HarmonyPink.copy(alpha = if (isSearchActive) 0.72f else 0.30f), CircleShape)
                    .testTag("search_icon_button")
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
                        focusedContainerColor = HarmonySurface2.copy(alpha = 0.76f),
                        unfocusedContainerColor = HarmonySurface2.copy(alpha = 0.66f),
                        disabledContainerColor = HarmonySurface2.copy(alpha = 0.66f),
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
            onFilterSelected = { filter ->
                onSetFilter(filter)
                if (filter == "open") showUnansweredQuestions = true
            },
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
            AuroraGlassSectionTitle(
                LanguageManager.tr("Kategorien", appLanguage),
                Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
            )

            // Horizontal Category Rail
            LazyRow(
                contentPadding = PaddingValues(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(HarmonyPacksData.CATEGORIES) { category ->
                    val translatedCategory = LanguageManager.translateCategory(category, appLanguage)
                    val displayCategory = if (category.id == "unterbewusstsein" && appLanguage == "de") {
                        translatedCategory.copy(name = "Tauche ins Selbstbewusstsein ein")
                    } else {
                        translatedCategory
                    }
                    CategoryRailCard(
                        category = displayCategory,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = HarmonyPinkSoft,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = LanguageManager.tr("Tägliche Aktivität", appLanguage),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarmonyText
                    )
                }
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

    if (showUnansweredQuestions) {
        UnansweredQuestionsDialog(
            answers = answers,
            appLanguage = appLanguage,
            onStartPack = { packId ->
                showUnansweredQuestions = false
                onStartPack(packId)
            },
            onDismiss = { showUnansweredQuestions = false }
        )
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
            Triple("all", LanguageManager.tr("Alle", appLanguage), Icons.Default.Apps),
            Triple("open", LanguageManager.tr("Unbeantwortet", appLanguage), Icons.Default.RadioButtonUnchecked),
            Triple("done", LanguageManager.tr("Beantwortet", appLanguage), Icons.Default.CheckCircle)
        )

        filters.forEach { (filterKey, label, icon) ->
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else HarmonyMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
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
}

private data class UnansweredQuestionItem(
    val packId: String,
    val packTitle: String,
    val categoryName: String,
    val topicId: String,
    val questionIndex: Int,
    val question: String
)

@Composable
private fun UnansweredQuestionsDialog(
    answers: List<AnswerEntity>,
    appLanguage: String,
    onStartPack: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val unanswered = remember(answers, appLanguage) {
        val answeredKeys = answers.mapTo(hashSetOf()) { it.packId to it.questionIndex }
        HarmonyPacksData.PACKS.flatMap { rawPack ->
            val pack = LanguageManager.translatePack(rawPack, appLanguage)
            val category = HarmonyPacksData.CATEGORIES
                .firstOrNull { it.id == rawPack.cat }
                ?.let { LanguageManager.translateCategory(it, appLanguage).name }
                .orEmpty()
            val total = if (pack.type == "tot") pack.pairs.size else pack.questions.size
            (0 until total).mapNotNull { index ->
                if ((rawPack.id to index) in answeredKeys) return@mapNotNull null
                val questionText = if (pack.type == "tot") {
                    pack.pairs.getOrNull(index)?.let { "${it.first}  ↔  ${it.second}" }
                } else {
                    pack.questions.getOrNull(index)?.q
                } ?: return@mapNotNull null
                UnansweredQuestionItem(
                    packId = rawPack.id,
                    packTitle = pack.title,
                    categoryName = category,
                    topicId = rawPack.topic,
                    questionIndex = index,
                    question = questionText
                )
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(LanguageManager.tr("Unbeantwortete Fragen", appLanguage), fontWeight = FontWeight.ExtraBold)
                Text(LanguageManager.tr("{count} Fragen warten auf euch", appLanguage).replace("{count}", unanswered.size.toString()), color = HarmonyMuted, fontSize = 12.sp)
            }
        },
        text = {
            if (unanswered.isEmpty()) {
                Text(LanguageManager.tr("Ihr habt bereits alle Fragen beantwortet.", appLanguage), color = HarmonyMuted)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp)
                        .testTag("unanswered_questions_list"),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    items(
                        items = unanswered,
                        key = { "${it.packId}-${it.questionIndex}" }
                    ) { item ->
                        val accent = topicAccentColor(item.topicId)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(17.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(accent.copy(alpha = 0.18f), HarmonyPurple.copy(alpha = 0.12f), HarmonySurface2)
                                    )
                                )
                                .border(1.dp, accent.copy(alpha = 0.50f), RoundedCornerShape(17.dp))
                                .clickable { onStartPack(item.packId) }
                                .padding(12.dp)
                                .testTag("unanswered_question_${item.packId}_${item.questionIndex}")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(27.dp)
                                        .clip(CircleShape)
                                        .background(accent.copy(alpha = 0.28f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${item.questionIndex + 1}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(item.packTitle, color = accent, fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold)
                                    if (item.categoryName.isNotBlank()) {
                                        Text(item.categoryName, color = HarmonyMuted, fontSize = 9.5.sp)
                                    }
                                }
                            }
                            Spacer(Modifier.height(7.dp))
                            Text(item.question, color = HarmonyText, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(LanguageManager.tr("Schließen", appLanguage), color = HarmonyPink) }
        }
    )
}

@Composable
fun CategoryRailCard(category: Category, onClick: () -> Unit) {
    val accent = Color(category.tagColorHex)
    val isPortalCategory = category.id == "unterbewusstsein"
    val transition = rememberInfiniteTransition(label = "category_power_${category.id}")
    val glowAlpha by transition.animateFloat(
        initialValue = if (isPortalCategory) 0.68f else 0.42f,
        targetValue = if (isPortalCategory) 1f else 0.72f,
        animationSpec = infiniteRepeatable(tween(1900), RepeatMode.Reverse),
        label = "category_glow_${category.id}"
    )
    val breathe by transition.animateFloat(
        initialValue = if (isPortalCategory) 0.985f else 1f,
        targetValue = if (isPortalCategory) 1.025f else 1.008f,
        animationSpec = infiniteRepeatable(tween(2300), RepeatMode.Reverse),
        label = "category_breathe_${category.id}"
    )
    Box(
        modifier = Modifier
            .size(width = 124.dp, height = if (isPortalCategory) 154.dp else 136.dp)
            .graphicsLayer {
                scaleX = breathe
                scaleY = breathe
            }
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        accent.copy(alpha = if (isPortalCategory) 0.48f else 0.32f),
                        HarmonyPurple.copy(alpha = 0.22f),
                        HarmonySurface2.copy(alpha = 0.94f)
                    )
                )
            )
            .border(
                width = if (isPortalCategory) 2.dp else 1.dp,
                brush = Brush.sweepGradient(
                    listOf(
                        accent.copy(alpha = glowAlpha),
                        Color.White.copy(alpha = glowAlpha * 0.82f),
                        HarmonyPink.copy(alpha = glowAlpha),
                        accent.copy(alpha = glowAlpha)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .testTag("category_card_${category.id}")
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = glowAlpha * 0.34f), Color.Transparent),
                    center = Offset(size.width * 0.28f, size.height * 0.24f),
                    radius = size.width * 0.72f
                ),
                radius = size.width * 0.72f,
                center = Offset(size.width * 0.28f, size.height * 0.24f)
            )
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (isPortalCategory) {
                IntrospectionPortal(
                    size = 64.dp,
                    isRevelation = true,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                GameCategoryVisual(
                    categoryId = category.id,
                    accent = accent,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            Text(
                text = category.name,
                fontSize = if (isPortalCategory) 11.5.sp else 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = HarmonyText,
                lineHeight = if (isPortalCategory) 14.sp else 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
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
    val accent = topicAccentColor(topic.id)
    val phase = remember(topic.id) { (topic.id.hashCode() and 0xFFFF) / 65535f }
    val transition = rememberInfiniteTransition(label = "topic_power_${topic.id}")
    val glowPulse by transition.animateFloat(
        initialValue = 0.58f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2100), RepeatMode.Reverse),
        label = "topic_glow_${topic.id}"
    )
    val energyTravel by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(4200, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "topic_energy_${topic.id}"
    )
    val breathe by transition.animateFloat(
        initialValue = 0.997f,
        targetValue = 1.012f,
        animationSpec = infiniteRepeatable(tween(2500), RepeatMode.Reverse),
        label = "topic_breathe_${topic.id}"
    )
    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .graphicsLayer {
                scaleX = breathe
                scaleY = breathe
            }
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.42f),
                        HarmonyPurple.copy(alpha = 0.25f),
                        HarmonySurface.copy(alpha = 0.98f)
                    ),
                    start = Offset(energyTravel * 680f - 340f, 0f),
                    end = Offset(energyTravel * 680f + 560f, 480f)
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.sweepGradient(
                    listOf(
                        accent.copy(alpha = glowPulse),
                        Color.White.copy(alpha = glowPulse * 0.76f),
                        HarmonyPink.copy(alpha = glowPulse),
                        HarmonyPurpleLight.copy(alpha = glowPulse),
                        accent.copy(alpha = glowPulse)
                    )
                ),
                shape = shape
            )
            .clickable(onClick = onClick)
            .testTag("topic_card_${topic.id}")
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val movingX = (energyTravel * 1.35f - 0.18f) * size.width
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = glowPulse * 0.42f), Color.Transparent),
                    center = Offset(movingX, size.height * 0.25f),
                    radius = size.height * 1.12f
                ),
                radius = size.height * 1.12f,
                center = Offset(movingX, size.height * 0.25f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(HarmonyPink.copy(alpha = glowPulse * 0.20f), Color.Transparent),
                    center = Offset(size.width * 0.96f, size.height * 0.86f),
                    radius = size.height * 0.90f
                ),
                radius = size.height * 0.90f,
                center = Offset(size.width * 0.96f, size.height * 0.86f)
            )
            repeat(7) { index ->
                val particleProgress = (energyTravel + phase + index * 0.17f) % 1f
                val particleAlpha = sin(particleProgress * Math.PI).toFloat().coerceIn(0f, 1f)
                val particleY = size.height * (0.18f + index * 0.105f) +
                    sin((energyTravel * 6.28f + index).toDouble()).toFloat() * 7f
                drawCircle(
                    color = if (index % 2 == 0) Color.White else accent,
                    radius = 2.2f + (index % 3) * 1.15f,
                    center = Offset(size.width * particleProgress, particleY),
                    alpha = particleAlpha * (0.32f + glowPulse * 0.48f)
                )
            }
            val arcSize = size.height * 1.12f
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(Color.Transparent, accent, Color.White, Color.Transparent),
                    center = Offset(size.width - arcSize * 0.34f, size.height * 0.5f)
                ),
                startAngle = energyTravel * 360f,
                sweepAngle = 138f,
                useCenter = false,
                topLeft = Offset(size.width - arcSize * 0.84f, size.height * 0.5f - arcSize * 0.5f),
                size = Size(arcSize, arcSize),
                alpha = glowPulse * 0.72f,
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HarmonyTopicIcon(topicId = topic.id, accent = accent, size = 62.dp)
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 0.15.sp
                )
                Spacer(modifier = Modifier.height(11.dp))
                AuroraProgressBar(
                    progress = percentage.coerceIn(0, 100) / 100f,
                    accent = accent,
                    modifier = Modifier
                        .fillMaxWidth(),
                    height = 9.dp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.20f + glowPulse * 0.12f))
                    .border(1.dp, accent.copy(alpha = glowPulse), CircleShape)
                    .padding(horizontal = 11.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (percentage >= 100) "✓" else "$percentage%",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (percentage >= 100) Color.White else accent
                )
            }
        }
    }
}
