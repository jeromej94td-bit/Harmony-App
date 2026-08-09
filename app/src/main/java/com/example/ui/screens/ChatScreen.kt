package com.example.ui.screens

import com.example.ui.tr
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.ChatMessageEntity
import com.example.ui.components.formatTimeOnly
import com.example.ui.theme.HarmonyLine
import com.example.ui.theme.HarmonyMuted
import com.example.ui.theme.HarmonyPink
import com.example.ui.theme.HarmonyPinkSoft
import com.example.ui.theme.HarmonyPurple
import com.example.ui.theme.HarmonySurface
import com.example.ui.theme.HarmonySurface2
import com.example.ui.theme.HarmonyText

@Composable
fun ChatScreen(
    messages: List<ChatMessageEntity>,
    partnerName: String,
    gfkPanelOpen: Boolean,
    gfkLoading: Boolean,
    gfkResult: String?,
    onSendMessage: (String) -> Unit,
    onToggleGfkPanel: () -> Unit,
    onRunGfk: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var chatInputText by remember { mutableStateOf("") }
    var gfkDraftText by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        // GFK Panel Drawer
        AnimatedVisibility(
            visible = gfkPanelOpen,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.verticalGradient(listOf(HarmonySurface2, HarmonySurface)))
                    .border(1.dp, HarmonyLine, RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = HarmonyPinkSoft,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tr("✨ GFK-Brücke", "✨ NVC bridge"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = HarmonyText
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tr("Formuliert deinen Entwurf gewaltfrei um: Beobachtung, Gefühl, Bedürfnis, Bitte.", "Reframes your draft using observation, feeling, need, and request."),
                        fontSize = 12.sp,
                        color = HarmonyMuted,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = gfkDraftText,
                        onValueChange = { gfkDraftText = it },
                        placeholder = { Text(tr("Was möchtest du eigentlich sagen?", "What do you actually want to say?"), color = HarmonyMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gfk_draft_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HarmonyPink,
                            unfocusedBorderColor = HarmonyLine,
                            focusedTextColor = HarmonyText,
                            unfocusedTextColor = HarmonyText
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onRunGfk(gfkDraftText) },
                        enabled = gfkDraftText.isNotBlank() && !gfkLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("gfk_rephrase_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = HarmonyPink)
                    ) {
                        if (gfkLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = tr("Formuliere um...", "Reframing..."), color = Color.White, fontSize = 13.sp)
                        } else {
                            Text(text = tr("Umformulieren", "Reframe"), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (gfkResult != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, HarmonyLine, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = gfkResult,
                                fontSize = 13.5.sp,
                                color = HarmonyText,
                                lineHeight = 19.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                chatInputText = gfkResult
                                onToggleGfkPanel()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("insert_gfk_button"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f))
                        ) {
                            Text(text = tr("In Chat einfügen", "Insert into chat"), color = HarmonyText, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            items(messages) { message ->
                ChatMessageBubble(message = message)
            }
        }

        // Bottom Chat Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleGfkPanel,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(HarmonySurface)
                    .border(1.dp, HarmonyLine, CircleShape)
                    .testTag("toggle_gfk_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "GFK Brücke",
                    tint = HarmonyPinkSoft,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = chatInputText,
                onValueChange = { chatInputText = it },
                placeholder = { Text(tr("Nachricht an $partnerName...", "Message to $partnerName..."), color = HarmonyMuted) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HarmonyPink,
                    unfocusedBorderColor = HarmonyLine,
                    focusedTextColor = HarmonyText,
                    unfocusedTextColor = HarmonyText,
                    focusedContainerColor = HarmonySurface,
                    unfocusedContainerColor = HarmonySurface
                ),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (chatInputText.isNotBlank()) {
                        onSendMessage(chatInputText)
                        chatInputText = ""
                    }
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(HarmonyPink, HarmonyPurple)))
                    .testTag("send_chat_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Senden",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessageEntity) {
    val isMe = message.sender == "me"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.76f)
                .then(
                    if (isMe) {
                        Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 19.dp,
                                    topEnd = 19.dp,
                                    bottomStart = 19.dp,
                                    bottomEnd = 4.dp
                                )
                            )
                            .background(Brush.linearGradient(listOf(HarmonyPink, HarmonyPurple)))
                            .padding(horizontal = 15.dp, vertical = 11.dp)
                    } else {
                        Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 19.dp,
                                    topEnd = 19.dp,
                                    bottomStart = 4.dp,
                                    bottomEnd = 19.dp
                                )
                            )
                            .background(HarmonySurface2)
                            .border(
                                1.dp,
                                HarmonyLine,
                                RoundedCornerShape(
                                    topStart = 19.dp,
                                    topEnd = 19.dp,
                                    bottomStart = 4.dp,
                                    bottomEnd = 19.dp
                                )
                            )
                            .padding(horizontal = 15.dp, vertical = 11.dp)
                    }
                )
        ) {
            Column {
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    color = Color.White,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimeOnly(message.timestamp),
                    fontSize = 9.5.sp,
                    color = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
