package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MemoryCategoryEntity
import com.example.data.model.MemoryChecklistCodec
import com.example.data.model.MemoryChecklistItem
import com.example.data.model.MemoryEntryEntity
import com.example.data.model.MemoryEntryKind
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
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryEditorSheet(
    mode: MemoryEditorMode,
    categories: List<MemoryCategoryEntity>,
    appLanguage: String,
    onModeChange: (MemoryEditorMode) -> Unit,
    onDismiss: () -> Unit,
    onSaveNote: (String?, String, String, String?) -> Unit,
    onSaveList: (String?, String, String, List<MemoryChecklistItem>) -> Unit,
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
    var listItems by remember(initialEntry?.id) {
        mutableStateOf(
            initialEntry
                ?.takeIf { it.kind == MemoryEntryKind.LIST }
                ?.let { MemoryChecklistCodec.decode(it.body) }
                ?.takeIf { it.isNotEmpty() }
                ?: listOf(newChecklistItem())
        )
    }
    var url by rememberSaveable(initialEntry?.id) { mutableStateOf(initialEntry?.url.orEmpty()) }
    var linkNote by rememberSaveable(initialEntry?.id) { mutableStateOf(initialEntry?.body.orEmpty()) }
    var titleTouched by rememberSaveable(initialEntry?.id) { mutableStateOf(false) }
    var listTouched by rememberSaveable(initialEntry?.id) { mutableStateOf(false) }
    var urlTouched by rememberSaveable(initialEntry?.id) { mutableStateOf(false) }

    val normalizedItems = remember(listItems) {
        val clean = listItems.mapNotNull { item ->
            val text = item.text.trim()
            if (text.isEmpty()) null else item.copy(text = text)
        }
        clean.filterNot { it.completed } + clean.filter { it.completed }
    }
    val validUrl = remember(url) { isValidHttpUrl(url) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val configuration = LocalConfiguration.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var focusedFieldKey by remember(initialEntry?.id) { mutableStateOf<String?>(null) }
    val onFieldFocusChange: (String, Boolean) -> Unit = { key, focused ->
        if (focused) focusedFieldKey = key else if (focusedFieldKey == key) focusedFieldKey = null
    }
    val canSave = selectedCategoryId.isNotBlank() && when (selectedMode) {
        MemoryEditorMode.NOTE -> title.isNotBlank()
        MemoryEditorMode.LIST -> normalizedItems.isNotEmpty()
        MemoryEditorMode.LINK -> validUrl
    }

    BackHandler(enabled = focusedFieldKey != null) {
        keyboardController?.hide()
        focusManager.clearFocus(force = true)
        focusedFieldKey = null
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (focusedFieldKey != null) {
                keyboardController?.hide()
                focusManager.clearFocus(force = true)
                focusedFieldKey = null
            } else {
                onDismiss()
            }
        },
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
                .height((configuration.screenHeightDp * 0.9f).dp)
                .imePadding()
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
                        isError = titleTouched && title.isBlank(),
                        focusKey = "note-title",
                        onFocusChange = onFieldFocusChange
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
                        minLines = 3,
                        focusKey = "note-body",
                        onFocusChange = onFieldFocusChange
                    )
                }

                MemoryEditorMode.LIST -> {
                    MemoryTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = LanguageManager.tr("Titel", appLanguage),
                        modifier = Modifier.testTag("memory_editor_title"),
                        singleLine = true,
                        focusKey = "list-title",
                        onFocusChange = onFieldFocusChange
                    )
                    Spacer(Modifier.height(12.dp))
                    MemoryChecklistEditor(
                        items = listItems,
                        onItemsChange = {
                            listItems = it
                            listTouched = true
                        },
                        appLanguage = appLanguage,
                        onFocusChange = onFieldFocusChange
                    )
                    if (listTouched && normalizedItems.isEmpty()) {
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
                        keyboardType = KeyboardType.Uri,
                        focusKey = "link-url",
                        onFocusChange = onFieldFocusChange
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
                        minLines = 3,
                        focusKey = "link-note",
                        onFocusChange = onFieldFocusChange
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
                            initialEntry?.id,
                            selectedCategoryId,
                            title.trim(),
                            normalizedItems
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

private fun newChecklistItem() = MemoryChecklistItem(
    id = UUID.randomUUID().toString(),
    text = "",
    completed = false
)

@Composable
private fun MemoryChecklistEditor(
    items: List<MemoryChecklistItem>,
    onItemsChange: (List<MemoryChecklistItem>) -> Unit,
    appLanguage: String,
    onFocusChange: (String, Boolean) -> Unit
) {
    val activeItems = items.filterNot { it.completed }
    val completedItems = items.filter { it.completed }
    var showCompleted by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HarmonySurface2.copy(alpha = 0.42f), RoundedCornerShape(20.dp))
            .border(1.dp, HarmonyLine, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        activeItems.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = false,
                    onCheckedChange = {
                        onItemsChange(items.map { candidate ->
                            if (candidate.id == item.id) candidate.copy(completed = true) else candidate
                        })
                    },
                    modifier = Modifier.testTag("memory_checklist_active_item_${index}_toggle"),
                    colors = CheckboxDefaults.colors(
                        checkedColor = HarmonyPink,
                        uncheckedColor = HarmonyMuted,
                        checkmarkColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = item.text,
                    onValueChange = { text ->
                        onItemsChange(items.map { candidate ->
                            if (candidate.id == item.id) candidate.copy(text = text) else candidate
                        })
                    },
                    placeholder = {
                        Text(LanguageManager.tr("Listeneintrag", appLanguage), color = HarmonyMuted)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            if (index == activeItems.lastIndex && item.text.isNotBlank()) {
                                onItemsChange(items + newChecklistItem())
                            }
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("memory_checklist_active_item_${index}_text")
                        .onFocusChanged { onFocusChange("checklist-${item.id}", it.isFocused) },
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = HarmonyText,
                        unfocusedTextColor = HarmonyText,
                        focusedIndicatorColor = HarmonyPinkSoft,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = HarmonyPinkSoft
                    )
                )
                IconButton(
                    onClick = {
                        val remaining = items.filterNot { it.id == item.id }
                        onItemsChange(if (remaining.isEmpty()) listOf(newChecklistItem()) else remaining)
                    },
                    modifier = Modifier.testTag("memory_checklist_active_item_${index}_delete")
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = LanguageManager.tr("Eintrag löschen", appLanguage),
                        tint = HarmonyMuted
                    )
                }
            }
        }

        TextButton(
            onClick = { onItemsChange(items + newChecklistItem()) },
            modifier = Modifier.testTag("memory_checklist_add")
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = HarmonyPinkSoft)
            Spacer(Modifier.size(8.dp))
            Text(
                LanguageManager.tr("Listeneintrag", appLanguage),
                color = HarmonyText,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (completedItems.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCompleted = !showCompleted }
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (showCompleted) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = HarmonyMuted
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = if (completedItems.size == 1) {
                        LanguageManager.tr("1 erledigter Eintrag", appLanguage)
                    } else {
                        LanguageManager.tr("${completedItems.size} erledigte Einträge", appLanguage)
                    },
                    color = HarmonyMuted,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (showCompleted) {
                completedItems.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = true,
                            onCheckedChange = {
                                onItemsChange(items.map { candidate ->
                                    if (candidate.id == item.id) candidate.copy(completed = false) else candidate
                                })
                            },
                            modifier = Modifier.testTag("memory_checklist_completed_item_${index}_toggle"),
                            colors = CheckboxDefaults.colors(
                                checkedColor = HarmonyMuted,
                                checkmarkColor = HarmonySurface
                            )
                        )
                        Text(
                            text = item.text,
                            color = HarmonyMuted.copy(alpha = 0.68f),
                            textDecoration = TextDecoration.LineThrough,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                                .testTag("memory_checklist_completed_item_${index}_text")
                        )
                    }
                }
            }
        }
    }
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
    keyboardType: KeyboardType = KeyboardType.Text,
    focusKey: String,
    onFocusChange: (String, Boolean) -> Unit
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
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { onFocusChange(focusKey, it.isFocused) },
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
