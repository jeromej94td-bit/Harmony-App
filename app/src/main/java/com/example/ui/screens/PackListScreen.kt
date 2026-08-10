package com.example.ui.screens

import com.example.ui.contentText
import com.example.ui.tr
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
import com.example.ui.components.Text
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
import com.example.data.model.isAvailableIn
import com.example.ui.LocalAppLanguage
import com.example.ui.theme.HarmonyMuted
import com.example.ui.theme.HarmonyText

@Composable
fun PackListScreen(
    answers: List<AnswerEntity>,
    selectedTopicId: String?,
    selectedCategoryId: String?,
    packFilter: String,
    onSetFilter: (String) -> Unit,
    onStartPack: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val language = LocalAppLanguage.current

    val topic = HarmonyPacksData.TOPICS.find { it.id == selectedTopicId }
    val category = HarmonyPacksData.CATEGORIES.find { it.id == selectedCategoryId }
    val titleText = when {
        topic != null -> "${topic.emoji} ${contentText(topic.name)}"
        category != null -> "${category.emoji} ${contentText(category.name)}"
        else -> tr("Alle Pakete", "All packs")
    }

    var list = HarmonyPacksData.PACKS.filter { pack ->
        pack.isAvailableIn(language.code) &&
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
                Text(text = tr("✕ Schließen", "✕ Close"), color = HarmonyMuted, fontSize = 13.sp)
            }
        }

        FilterChipsRow(
            selectedFilter = packFilter,
            onFilterSelected = onSetFilter,
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
                    text = tr("Hier ist gerade nichts.\nWechsle den Filter oder wähle ein anderes Thema.", "Nothing here right now.\nChange the filter or choose another topic."),
                    color = HarmonyMuted,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            list.forEach { pack ->
                PaddingPackCard(
                    pack = pack,
                    answers = answers,
                    onStartPack = onStartPack,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                )
            }
        }
    }
}
