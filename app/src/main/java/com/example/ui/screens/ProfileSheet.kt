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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.data.model.ProfileEntity
import com.example.ui.components.HarmonyCard
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.HarmonyBg
import com.example.ui.theme.HarmonyLine
import com.example.ui.theme.HarmonyMuted
import com.example.ui.theme.HarmonyPink
import com.example.ui.theme.HarmonyPurple
import com.example.ui.theme.HarmonySurface
import com.example.ui.theme.HarmonySurface2
import com.example.ui.theme.HarmonyText
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSheet(
    profile: ProfileEntity,
    coachLoading: Boolean,
    coachResult: String?,
    dateIdeasLoading: Boolean,
    dateIdeasResult: String?,
    isEditProfileOpen: Boolean,
    onDismiss: () -> Unit,
    onToggleSimulator: () -> Unit,
    onOpenEditProfile: () -> Unit,
    onCloseEditProfile: () -> Unit,
    onSaveEditProfile: (String, String, Long) -> Unit,
    onRunCoach: () -> Unit,
    onRunDateIdeas: (String) -> Unit,
    onOpenDevStudio: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    var dateWishText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = HarmonyBg,
        scrimColor = Color.Black.copy(alpha = 0.72f),
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            // Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "💞", fontSize = 34.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${profile.userName} & ${profile.partnerName}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = HarmonyText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Invite-Code: HRM-8731 · alles freigeschaltet",
                    fontSize = 12.sp,
                    color = HarmonyMuted
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // AI Coach Card
            HarmonyCard {
                Column {
                    Text(
                        text = "✨ KI-Beziehungscoach",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarmonyText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Analysiert eure Antworten & Chats nach Gottman und GFK.",
                        fontSize = 12.5.sp,
                        color = HarmonyMuted,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onRunCoach,
                        enabled = !coachLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("run_coach_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = HarmonyPink)
                    ) {
                        if (coachLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Analysiere eure Muster...", color = Color.White, fontSize = 13.5.sp)
                        } else {
                            Text(text = "Analyse starten", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (coachResult != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, HarmonyLine, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = coachResult,
                                fontSize = 13.5.sp,
                                color = HarmonyText,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AI Date Ideas Card
            HarmonyCard {
                Column {
                    Text(
                        text = "💡 KI-Date-Ideen",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarmonyText
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = dateWishText,
                        onValueChange = { dateWishText = it },
                        placeholder = { Text("Wünsche? z.B. günstig, draußen", color = HarmonyMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("date_wishes_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HarmonyPink,
                            unfocusedBorderColor = HarmonyLine,
                            focusedTextColor = HarmonyText,
                            unfocusedTextColor = HarmonyText
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { onRunDateIdeas(dateWishText) },
                        enabled = !dateIdeasLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("run_date_ideas_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = HarmonyPink)
                    ) {
                        if (dateIdeasLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Sammle Ideen...", color = Color.White, fontSize = 13.5.sp)
                        } else {
                            Text(text = "Ideen generieren", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (dateIdeasResult != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, HarmonyLine, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = dateIdeasResult,
                                fontSize = 13.5.sp,
                                color = HarmonyText,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Profile Details Card
            HarmonyCard {
                Column {
                    Text(
                        text = "Profil",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarmonyText
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileRow(label = "Dein Name", value = profile.userName)
                    ProfileRow(label = "Partnerin", value = profile.partnerName)
                    ProfileRow(label = "Zusammen seit", value = formatTimestamp(profile.startDate))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Partner-Simulator", fontSize = 13.5.sp, color = HarmonyText)
                        Switch(
                            checked = profile.simulatorEnabled,
                            onCheckedChange = { onToggleSimulator() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = HarmonyPink
                            ),
                            modifier = Modifier.testTag("simulator_toggle")
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = onOpenEditProfile,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("edit_profile_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.06f))
                    ) {
                        Text(text = "Bearbeiten", color = HarmonyText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Developer Studio Card
            if (onOpenDevStudio != null) {
                HarmonyCard {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "🛠️ Entwickler-Modus",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HarmonyText
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Spiele & Städte bearbeiten, Ordner reinladen, Bilder anpassen",
                                    fontSize = 11.5.sp,
                                    color = HarmonyMuted
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onDismiss()
                                onOpenDevStudio()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("open_dev_studio_button"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = HarmonyPurple)
                        ) {
                            Text(text = "Entwickler Studio Öffnen", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("close_profile_sheet_button")
            ) {
                Text(
                    text = "Schließen",
                    color = HarmonyPink,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Edit Profile Dialog
    if (isEditProfileOpen) {
        var userEdit by remember { mutableStateOf(profile.userName) }
        var partnerEdit by remember { mutableStateOf(profile.partnerName) }
        var startEdit by remember { mutableStateOf(formatTimestamp(profile.startDate)) }

        Dialog(onDismissRequest = onCloseEditProfile) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.verticalGradient(listOf(HarmonySurface2, HarmonySurface)))
                    .border(1.dp, HarmonyLine, RoundedCornerShape(24.dp))
                    .padding(22.dp)
            ) {
                Column {
                    Text(
                        text = "Profil bearbeiten",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarmonyText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Namen und Startdatum eurer Beziehung.",
                        fontSize = 13.sp,
                        color = HarmonyMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = userEdit,
                        onValueChange = { userEdit = it },
                        placeholder = { Text("Dein Name", color = HarmonyMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_user_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HarmonyPink,
                            unfocusedBorderColor = HarmonyLine,
                            focusedTextColor = HarmonyText,
                            unfocusedTextColor = HarmonyText
                        )
                    )

                    Spacer(modifier = Modifier.height(9.dp))

                    OutlinedTextField(
                        value = partnerEdit,
                        onValueChange = { partnerEdit = it },
                        placeholder = { Text("Name Partnerin", color = HarmonyMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_partner_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HarmonyPink,
                            unfocusedBorderColor = HarmonyLine,
                            focusedTextColor = HarmonyText,
                            unfocusedTextColor = HarmonyText
                        )
                    )

                    Spacer(modifier = Modifier.height(9.dp))

                    OutlinedTextField(
                        value = startEdit,
                        onValueChange = { startEdit = it },
                        placeholder = { Text("Zusammen seit (TT.MM.JJJJ)", color = HarmonyMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_start_date_input"),
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
                            onClick = onCloseEditProfile,
                            modifier = Modifier.weight(1f),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f))
                        ) {
                            Text(text = "Abbrechen", color = HarmonyText)
                        }
                        Button(
                            onClick = {
                                val parsedDate = try {
                                    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
                                    sdf.parse(startEdit)?.time ?: profile.startDate
                                } catch (e: Exception) {
                                    profile.startDate
                                }
                                onSaveEditProfile(userEdit, partnerEdit, parsedDate)
                            },
                            modifier = Modifier.weight(1f),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = HarmonyPink)
                        ) {
                            Text(text = "Speichern", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .border(width = 0.dp, color = Color.Transparent),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.5.sp, color = HarmonyText)
        Text(text = value, fontSize = 13.sp, color = HarmonyMuted)
    }
}
