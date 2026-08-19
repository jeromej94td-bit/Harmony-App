package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.ProfileEntity
import com.example.ui.AppLanguage
import com.example.ui.TranslationCatalog
import com.example.ui.tr
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
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSheet(
    profile: ProfileEntity,
    isEditProfileOpen: Boolean,
    onDismiss: () -> Unit,
    onToggleSimulator: () -> Unit,
    onOpenEditProfile: () -> Unit,
    onCloseEditProfile: () -> Unit,
    onSaveEditProfile: (String, String, Long) -> Unit,
    onUpdateAvatar: (Uri, Boolean) -> Unit,
    onOpenDevStudio: (() -> Unit)? = null,
    isDarkMode: Boolean = true,
    onToggleDarkMode: ((Boolean) -> Unit)? = null,
    language: AppLanguage = AppLanguage.GERMAN,
    onLanguageChange: (AppLanguage) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    var isLanguageExpanded by remember { mutableStateOf(false) }
    val userAvatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onUpdateAvatar(it, true) }
    }
    val partnerAvatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onUpdateAvatar(it, false) }
    }

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
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProfileAvatar(
                        path = profile.userAvatarPath,
                        fallback = profile.userName.take(1),
                        label = tr("Dein Bild", "Your photo"),
                        onClick = { userAvatarPicker.launch("image/*") }
                    )
                    Text(text = "💕", fontSize = 24.sp)
                    ProfileAvatar(
                        path = profile.partnerAvatarPath,
                        fallback = profile.partnerName.take(1),
                        label = tr("Partnerbild", "Partner photo"),
                        onClick = { partnerAvatarPicker.launch("image/*") }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${profile.userName} & ${profile.partnerName}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = HarmonyText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tr("Invite-Code: HRM-8731 · alles freigeschaltet", "Invite code: HRM-8731 · everything unlocked"),
                    fontSize = 12.sp,
                    color = HarmonyMuted
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Profile Details Card
            HarmonyCard {
                Column {
                    Text(
                        text = tr("Profil", "Profile"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarmonyText
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileRow(label = tr("Dein Name", "Your name"), value = profile.userName)
                    ProfileRow(label = tr("Partnerin", "Partner"), value = profile.partnerName)
                    ProfileRow(label = tr("Zusammen seit", "Together since"), value = formatTimestamp(profile.startDate))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = tr("Partner-Simulator", "Partner simulator"), fontSize = 13.5.sp, color = HarmonyText)
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

                    if (onToggleDarkMode != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = tr("Dunkles Design", "Dark mode"), fontSize = 13.5.sp, color = HarmonyText)
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { onToggleDarkMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = HarmonyPink
                                ),
                                modifier = Modifier.testTag("dark_mode_toggle")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = onOpenEditProfile,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("edit_profile_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.06f))
                    ) {
                        Text(text = tr("Bearbeiten", "Edit"), color = HarmonyText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Language
            HarmonyCard {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = tr("Sprache", "Language"),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarmonyText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tr("App-Sprache auswählen", "Select app language"),
                                fontSize = 11.5.sp,
                                color = HarmonyMuted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    // Clickable dropdown trigger header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(
                                1.dp,
                                if (isLanguageExpanded) HarmonyPink.copy(alpha = 0.7f) else HarmonyLine,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { isLanguageExpanded = !isLanguageExpanded }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .testTag("language_selector_trigger"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = language.flagEmoji, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == AppLanguage.ENGLISH) language.englishName else language.nativeName,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isLanguageExpanded) tr("Tippen zum Zuklappen", "Tap to collapse") else tr("Tippen zum Auswählen & Scrollen", "Tap to select & scroll"),
                                color = HarmonyMuted,
                                fontSize = 11.5.sp
                            )
                        }
                        Text(
                            text = if (isLanguageExpanded) "▲" else "▼",
                            color = HarmonyPink,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Expanded scrollable language list
                    AnimatedVisibility(
                        visible = isLanguageExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.04f))
                                .border(1.dp, HarmonyLine, RoundedCornerShape(16.dp))
                                .padding(6.dp)
                        ) {
                            val availableLanguages = remember {
                                AppLanguage.entries.filter(TranslationCatalog::hasCompletePack)
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                availableLanguages.forEach { option ->
                                    val isSelected = (language == option)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) HarmonyPink.copy(alpha = 0.85f)
                                                else Color.White.copy(alpha = 0.04f)
                                            )
                                            .clickable {
                                                onLanguageChange(option)
                                                isLanguageExpanded = false
                                            }
                                            .padding(horizontal = 14.dp, vertical = 12.dp)
                                            .testTag("language_option_${option.name}"),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = option.flagEmoji, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = if (language == AppLanguage.ENGLISH) option.englishName else option.nativeName,
                                            modifier = Modifier.weight(1f),
                                            color = Color.White,
                                            fontSize = 14.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                        if (isSelected) {
                                            Text(
                                                text = "✓",
                                                color = Color.White,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
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
                                    text = tr("🛠️ Entwickler-Modus", "🛠️ Developer mode"),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HarmonyText
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tr("Spiele & Städte bearbeiten, Ordner reinladen, Bilder anpassen", "Edit games and destinations, import folders, adjust images"),
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
                                .height(52.dp)
                                .testTag("open_dev_studio_button"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = HarmonyPurple)
                        ) {
                            Text(text = tr("Entwickler Studio Öffnen", "Open Developer Studio"), color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
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
                    text = tr("Schließen", "Close"),
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
                        text = tr("Profil bearbeiten", "Edit profile"),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarmonyText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tr("Namen und Startdatum eurer Beziehung.", "Your names and relationship start date."),
                        fontSize = 13.sp,
                        color = HarmonyMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = userEdit,
                        onValueChange = { userEdit = it },
                        placeholder = { Text(tr("Dein Name", "Your name"), color = HarmonyMuted) },
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
                        placeholder = { Text(tr("Name Partnerin", "Partner's name"), color = HarmonyMuted) },
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
                        placeholder = { Text(tr("Zusammen seit (TT.MM.JJJJ)", "Together since (DD.MM.YYYY)"), color = HarmonyMuted) },
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
                            Text(text = tr("Abbrechen", "Cancel"), color = HarmonyText)
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
                            Text(text = tr("Speichern", "Save"), color = Color.White)
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

@Composable
private fun ProfileAvatar(
    path: String?,
    fallback: String,
    label: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(74.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(HarmonyPink, HarmonyPurple)))
                .border(2.dp, Color.White.copy(alpha = 0.72f), CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (path != null) {
                AsyncImage(
                    model = File(path),
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(fallback.uppercase(), color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(25.dp)
                    .background(HarmonyPink, CircleShape)
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("＋", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = HarmonyMuted, fontSize = 10.sp)
    }
}
