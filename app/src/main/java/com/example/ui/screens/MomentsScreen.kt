package com.example.ui.screens

import com.example.ui.contentText
import com.example.ui.tr
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.ui.components.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MomentEntity
import com.example.data.model.ProfileEntity
import com.example.ui.components.CategoryTag
import com.example.ui.components.HarmonyCard
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.HarmonyLine
import com.example.ui.theme.HarmonyMuted
import com.example.ui.theme.HarmonyPink
import com.example.ui.theme.HarmonySurface
import com.example.ui.theme.HarmonySurface2
import com.example.ui.theme.HarmonyText
import java.util.concurrent.TimeUnit

@Composable
fun MomentsScreen(
    moments: List<MomentEntity>,
    profile: ProfileEntity,
    isAddMomentOpen: Boolean,
    onOpenAddMoment: () -> Unit,
    onCloseAddMoment: () -> Unit,
    onAddMoment: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Calculate dynamic milestones from relationship start date
    val dayMs = 86400000L
    val daysTogether = TimeUnit.MILLISECONDS.toDays(
        (System.currentTimeMillis() - profile.startDate).coerceAtLeast(0)
    ).toInt()

    val milestoneTargets = listOf(100, 365, 500, 730, 1000, 1095)
    val computedMilestones = milestoneTargets.filter { daysTogether >= it }.map { d ->
        val milestoneTitle = when (d) {
            365 -> tr("1 Jahr zusammen 🎉", "Together for 1 year 🎉")
            730 -> tr("2 Jahre zusammen 🎉", "Together for 2 years 🎉")
            1095 -> tr("3 Jahre zusammen 🎉", "Together for 3 years 🎉")
            else -> tr("$d Tage zusammen 💞", "$d days together 💞")
        }
        MomentEntity(
            id = -d.toLong(),
            title = milestoneTitle,
            content = tr("Ein Harmony-Meilenstein eurer Liebe.", "A Harmony milestone in your love story."),
            emoji = "🏆",
            timestamp = profile.startDate + (d * dayMs),
            isMilestone = true
        )
    }

    val allItems = (moments + computedMilestones).sortedByDescending { it.timestamp }

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
                text = tr("Momente", "Moments"),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = HarmonyText
            )
            TextButton(
                onClick = onOpenAddMoment,
                modifier = Modifier.testTag("add_moment_button")
            ) {
                Text(text = tr("+ Hinzufügen", "+ Add"), color = HarmonyPink, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (allItems.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 44.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = tr("Noch keine Momente.\nHaltet euer erstes Erlebnis fest 💞", "No moments yet.\nCapture your first memory 💞"),
                    color = HarmonyMuted,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            allItems.forEach { moment ->
                HarmonyCard(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "${moment.emoji} ${contentText(moment.title)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarmonyText
                            )
                            if (moment.isMilestone) {
                                CategoryTag(tag = "hochzeit")
                            }
                        }

                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = moment.content,
                            fontSize = 13.sp,
                            color = HarmonyMuted,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatTimestamp(moment.timestamp),
                            fontSize = 11.sp,
                            color = HarmonyMuted.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }

    // Add Moment Dialog
    if (isAddMomentOpen) {
        var titleInput by remember { mutableStateOf("") }
        var contentInput by remember { mutableStateOf("") }

        Dialog(onDismissRequest = onCloseAddMoment) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.verticalGradient(listOf(HarmonySurface2, HarmonySurface)))
                    .border(1.dp, HarmonyLine, RoundedCornerShape(24.dp))
                    .padding(22.dp)
            ) {
                Column {
                    Text(
                        text = tr("Moment festhalten", "Capture a moment"),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarmonyText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tr("Was wollt ihr nie vergessen?", "What do you never want to forget?"),
                        fontSize = 13.sp,
                        color = HarmonyMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        placeholder = { Text(tr("Titel", "Title"), color = HarmonyMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("moment_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HarmonyPink,
                            unfocusedBorderColor = HarmonyLine,
                            focusedTextColor = HarmonyText,
                            unfocusedTextColor = HarmonyText
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = contentInput,
                        onValueChange = { contentInput = it },
                        placeholder = { Text(tr("Was ist passiert?", "What happened?"), color = HarmonyMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("moment_content_input"),
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
                            onClick = onCloseAddMoment,
                            modifier = Modifier.weight(1f),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f))
                        ) {
                            Text(text = tr("Abbrechen", "Cancel"), color = HarmonyText)
                        }
                        Button(
                            onClick = { onAddMoment(titleInput, contentInput) },
                            enabled = titleInput.isNotBlank() && contentInput.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = HarmonyPink)
                        ) {
                            Text(text = tr("Hinzufügen", "Add"), color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
