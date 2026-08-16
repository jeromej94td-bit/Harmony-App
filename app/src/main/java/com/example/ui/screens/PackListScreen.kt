package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnswerEntity
import com.example.data.model.HarmonyPacksData
import com.example.util.LanguageManager
import com.example.ui.theme.HarmonyMuted
import com.example.ui.theme.HarmonyText

@Composable
fun PackListScreen(
    answers: List<AnswerEntity>,
    selectedTopicId: String?,
    selectedCategoryId: String?,
    packFilter: String,
    appLanguage: String = "de",
    onSetFilter: (String) -> Unit,
    onStartPack: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val topic = HarmonyPacksData.TOPICS.find { it.id == selectedTopicId }
    val category = HarmonyPacksData.CATEGORIES.find { it.id == selectedCategoryId }
    val titleText = when {
        topic != null -> "${topic.emoji} ${LanguageManager.tr(topic.name, appLanguage)}"
        category != null -> "${category.emoji} ${LanguageManager.tr(category.name, appLanguage)}"
        else -> LanguageManager.tr("Alle Pakete", appLanguage)
    }

    var list = HarmonyPacksData.PACKS.filter { pack ->
        when {
            selectedTopicId != null -> pack.topic == selectedTopicId
            selectedCategoryId != null -> pack.cat == selectedCategoryId
            else -> true
        }
    }

    if (packFilter == "open") {
        list = list.filter { pack ->
            val totalLen = if (pack.type == "tot") pack.pairs.size else pack.questions.size
            val ansCount = answers.count { it.packId == pack.id }
            ansCount < totalLen
        }
    } else if (packFilter == "done") {
        list = list.filter { pack ->
            val totalLen = if (pack.type == "tot") pack.pairs.size else pack.questions.size
            val ansCount = answers.count { it.packId == pack.id }
            ansCount >= totalLen && totalLen > 0
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 90.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = titleText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = HarmonyText
            )
            TextButton(
                onClick = onClose,
                modifier = Modifier.testTag("close_pack_list_button")
            ) {
                Text(
                    text = "✕ " + LanguageManager.tr("Schließen", appLanguage),
                    color = HarmonyMuted,
                    fontSize = 13.sp
                )
            }
        }

        FilterChipsRow(
            selectedFilter = packFilter,
            onFilterSelected = onSetFilter,
            appLanguage = appLanguage,
            modifier = Modifier.padding(horizontal = 18.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (list.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 44.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = LanguageManager.tr("Hier ist gerade nichts.\nWechsle den Filter oder wähle ein anderes Thema.", appLanguage),
                    color = HarmonyMuted,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            list.forEach { rawPack ->
                val pack = LanguageManager.translatePack(rawPack, appLanguage)
                PaddingPackCard(
                    appLanguage = appLanguage,
                    pack = pack,
                    answers = answers,
                    onStartPack = onStartPack,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                )
            }
        }
    }
}
