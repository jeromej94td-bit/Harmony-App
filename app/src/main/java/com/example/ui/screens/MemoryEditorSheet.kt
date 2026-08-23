package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MemoryCategoryEntity
import com.example.data.model.MemoryEntryEntity
import com.example.ui.memory.MemoryEditorMode
import com.example.ui.theme.HarmonyLine
import com.example.ui.theme.HarmonyMuted
import com.example.ui.theme.HarmonyPink
import com.example.ui.theme.HarmonyPinkSoft
import com.example.ui.theme.HarmonyPurple
import com.example.ui.theme.HarmonySurface
import com.example.ui.theme.HarmonySurface2
import com.example.ui.theme.HarmonyText
import com.example.util.LanguageManager
import java.net.URI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryEditorSheet(
    mode: MemoryEditorMode,
    categories: List<MemoryCategoryEntity>,
    appLanguage: String,
    onModeChange: (MemoryEditorMode) -> Unit,
    onDismiss: () -> Unit,
    onSaveNote: (String?, String, String, String?) -> Unit,
    onSaveList: (String, String) -> Unit,
    onSaveLink: (String?, String, String, String?) -> Unit,
    modifier: Modifier = Modifier,
    initialEntry: MemoryEntryEntity? = null
) {
    var selectedMode by remember(mode, initialEntry?.id) { mutableStateOf(mode) }
    var selectedCategoryId by rememberSaveable(initialEntry?.id, categories) {
        mutableStateOf(initialEntry?.categoryId ?: categories.firstOrNull()?.id.orEmpty())
    }
    var title by rememberSaveable(initialEntry?.id) { mutableStateOf(initialEntry?.title.orEmpty()) }
    var body by rememberSaveable(initialEntry?.id) { mutableStateOf(initialEntry?.body.orEmpty()) }
    var listLines by rememberSaveable(initialEntry?.id) { mutableStateOf("") }
    var url by rememberSaveable(initialEntry?.id) { mutableStateOf(initialEntry?.url.orEmpty()) }
    var linkNote by rememberSaveable(initialEntry?.id) { mutableStateOf(initialEntry?.body.orEmpty()) }
    var titleTouched by rememberSaveable(initialEntry?.id) { mutableStateOf(false) }
    var listTouched by rememberSaveable(initialEntry?.id) { mutableStateOf(false) }
    var urlTouched by rememberSaveable(initialEntry?.id) { mutableStateOf(false) }

    val normalizedLines = remember(listLines) {
        listLines.lineSequence().map(String::trim).filter(String::isNotEmpty).toList()
    }
    val validUrl = remember(url) { isValidHttpUrl(url) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val canSave = selectedCategoryId.isNotBlank() && when (selectedMode) {
        MemoryEditorMode.NOTE -> title.isNotBlank()
        MemoryEditorMode.LIST -> normalizedLines.isNotEmpty()
        MemoryEditorMode.LINK -> validUrl
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
            .fillMaxSize()
            .testTag("memory_editor_sheet"),
        containerColor = HarmonySurface.copy(alpha = 0.98f),
        contentColor = HarmonyText,
        scrimColor = Color.Black.copy(alpha = 0.66f),
        dragHandle = {
            Spacer(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .size(width = 42.dp, height = 4.dp)
                    .background(HarmonyPinkSoft.copy(alpha = 0.74f), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = LanguageManager.tr(
                            if (initialEntry == null) "Notiz hinzufügen" else "Notiz bearbeiten",
                            appLanguage
                        ),
                        color = HarmonyText,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = LanguageManager.tr("Notiz, Liste oder Link", appLanguage),
                        color = HarmonyMuted,
                        fontSize = 13.sp
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("memory_editor_close")
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = LanguageManager.tr("Schließen", appLanguage),
                        tint = HarmonyMuted
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EditorModeChip(
                    mode = MemoryEditorMode.NOTE,
                    selected = selectedMode == MemoryEditorMode.NOTE,
                    appLanguage = appLanguage,
                    onClick = {
                        selectedMode = MemoryEditorMode.NOTE
                        onModeChange(MemoryEditorMode.NOTE)
                    }
                )
                EditorModeChip(
                    mode = MemoryEditorMode.LIST,
                    selected = selectedMode == MemoryEditorMode.LIST,
                    appLanguage = appLanguage,
                    onClick = {
                        selectedMode = MemoryEditorMode.LIST
                        onModeChange(MemoryEditorMode.LIST)
                    }
                )
                EditorModeChip(
                    mode = MemoryEditorMode.LINK,
                    selected = selectedMode == MemoryEditorMode.LINK,
                    appLanguage = appLanguage,
                    onClick = {
                        selectedMode = MemoryEditorMode.LINK
                        onModeChange(MemoryEditorMode.LINK)
                    }
                )
            }

            Spacer(Modifier.height(18.dp))
            when (selectedMode) {
                MemoryEditorMode.NOTE -> {
                    MemoryTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            titleTouched = true
                        },
                        label = LanguageManager.tr("Titel", appLanguage),
                        modifier = Modifier.testTag("memory_editor_title"),
                        singleLine = true,
                        isError = titleTouched && title.isBlank()
                    )
                    if (titleTouched && title.isBlank()) {
                        ValidationMessage(LanguageManager.tr("Bitte gib einen Titel ein", appLanguage))
                    }
                    Spacer(Modifier.height(12.dp))
                    MemoryTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = LanguageManager.tr("Beschreibung", appLanguage),
                        modifier = Modifier.testTag("memory_editor_body"),
                        minLines = 3
                    )
                }

                MemoryEditorMode.LIST -> {
                    MemoryTextField(
                        value = listLines,
                        onValueChange = {
                            listLines = it
                            listTouched = true
                        },
                        label = LanguageManager.tr("Liste", appLanguage),
                        modifier = Modifier.testTag("memory_editor_list_lines"),
                        minLines = 6,
                        isError = listTouched && normalizedLines.isEmpty(),
                        supportingText = LanguageManager.tr("Eine Notiz pro Zeile", appLanguage)
                    )
                    if (listTouched && normalizedLines.isEmpty()) {
                        ValidationMessage(LanguageManager.tr("Bitte gib mindestens einen Eintrag ein", appLanguage))
                    }
                }

                MemoryEditorMode.LINK -> {
                    MemoryTextField(
                        value = url,
                        onValueChange = {
                            url = it
                            urlTouched = true
                        },
                        label = "URL",
                        modifier = Modifier.testTag("memory_editor_url"),
                        singleLine = true,
                        isError = urlTouched && !validUrl,
                        keyboardType = KeyboardType.Uri
                    )
                    if (urlTouched && !validUrl) {
                        ValidationMessage(
                            LanguageManager.tr(
                                "Bitte gib einen gültigen HTTP- oder HTTPS-Link ein",
                                appLanguage
                            )
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    MemoryTextField(
                        value = linkNote,
                        onValueChange = { linkNote = it },
                        label = LanguageManager.tr("Notiz", appLanguage),
                        modifier = Modifier.testTag("memory_editor_link_note"),
                        minLines = 3
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = LanguageManager.tr("Kategorie", appLanguage),
                color = HarmonyText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .testTag("memory_editor_category"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val selected = category.id == selectedCategoryId
                    FilterChip(
                        selected = selected,
                        onClick = { selectedCategoryId = category.id },
                        label = { Text(memoryCategoryLabel(category, appLanguage)) },
                        leadingIcon = {
                            Icon(
                                memoryCategoryIcon(category.iconKey),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.heightIn(min = 48.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = memoryCategoryColor(category.colorKey).copy(alpha = 0.24f),
                            selectedLabelColor = HarmonyText,
                            selectedLeadingIconColor = memoryCategoryColor(category.colorKey),
                            labelColor = HarmonyMuted,
                            iconColor = HarmonyMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = HarmonyLine,
                            selectedBorderColor = memoryCategoryColor(category.colorKey)
                        )
                    )
                }
            }

            Spacer(Modifier.height(22.dp))
            Button(
                enabled = canSave,
                onClick = {
                    when (selectedMode) {
                        MemoryEditorMode.NOTE -> onSaveNote(
                            initialEntry?.id,
                            selectedCategoryId,
                            title.trim(),
                            body.trim().takeIf(String::isNotEmpty)
                        )

                        MemoryEditorMode.LIST -> onSaveList(
                            selectedCategoryId,
                            normalizedLines.joinToString("\n")
                        )

                        MemoryEditorMode.LINK -> onSaveLink(
                            initialEntry?.id,
                            selectedCategoryId,
                            url.trim(),
                            linkNote.trim().takeIf(String::isNotEmpty)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("memory_editor_save"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HarmonyPink,
                    contentColor = Color.White,
                    disabledContainerColor = HarmonySurface2,
                    disabledContentColor = HarmonyMuted
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(LanguageManager.tr("Speichern", appLanguage), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EditorModeChip(
    mode: MemoryEditorMode,
    selected: Boolean,
    appLanguage: String,
    onClick: () -> Unit
) {
    val (label, icon) = when (mode) {
        MemoryEditorMode.NOTE -> LanguageManager.tr("Notiz", appLanguage) to Icons.Default.Notes
        MemoryEditorMode.LIST -> LanguageManager.tr("Liste", appLanguage) to Icons.Default.Checklist
        MemoryEditorMode.LINK -> LanguageManager.tr("Link", appLanguage) to Icons.Default.AddLink
    }
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = FontWeight.SemiBold) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(19.dp)) },
        modifier = Modifier
            .heightIn(min = 48.dp)
            .testTag("memory_mode_${mode.name.lowercase()}"),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = HarmonyPink.copy(alpha = 0.22f),
            selectedLabelColor = HarmonyText,
            selectedLeadingIconColor = HarmonyPinkSoft,
            labelColor = HarmonyMuted,
            iconColor = HarmonyMuted
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = HarmonyLine,
            selectedBorderColor = HarmonyPinkSoft
        )
    )
}

@Composable
private fun MemoryTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    minLines: Int = 1,
    isError: Boolean = false,
    supportingText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        supportingText = supportingText?.let { text -> ({ Text(text) }) },
        singleLine = singleLine,
        minLines = minLines,
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = HarmonySurface2.copy(alpha = 0.88f),
            unfocusedContainerColor = HarmonySurface2.copy(alpha = 0.62f),
            errorContainerColor = HarmonySurface2.copy(alpha = 0.76f),
            focusedTextColor = HarmonyText,
            unfocusedTextColor = HarmonyText,
            focusedIndicatorColor = HarmonyPinkSoft,
            unfocusedIndicatorColor = HarmonyLine,
            focusedLabelColor = HarmonyPinkSoft,
            unfocusedLabelColor = HarmonyMuted,
            cursorColor = HarmonyPinkSoft
        )
    )
}

@Composable
private fun ValidationMessage(text: String) {
    Text(
        text = text,
        color = HarmonyPinkSoft,
        fontSize = 12.sp,
        modifier = Modifier.padding(start = 12.dp, top = 4.dp)
    )
}

internal fun isValidHttpUrl(rawUrl: String): Boolean = try {
    val uri = URI(rawUrl.trim())
    (uri.scheme.equals("http", ignoreCase = true) || uri.scheme.equals("https", ignoreCase = true)) &&
        !uri.host.isNullOrBlank()
} catch (_: Exception) {
    false
}
