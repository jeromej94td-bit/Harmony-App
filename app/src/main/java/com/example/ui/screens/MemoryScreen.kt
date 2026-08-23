package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MemoryCategoryEntity
import com.example.data.model.MemoryEntryKind
import com.example.R
import com.example.ui.components.AmbientBackground
import com.example.ui.memory.MemoryEditorMode
import com.example.ui.memory.MemoryTab
import com.example.ui.memory.MemoryUiState
import com.example.ui.theme.HarmonyLine
import com.example.ui.theme.HarmonyMuted
import com.example.ui.theme.HarmonyPink
import com.example.ui.theme.HarmonyPinkSoft
import com.example.ui.theme.HarmonyPurple
import com.example.ui.theme.HarmonySurface
import com.example.ui.theme.HarmonySurface2
import com.example.ui.theme.HarmonyText
import com.example.util.LanguageManager
import coil.compose.AsyncImage
import java.io.File

@Composable
fun MemoryScreen(
    state: MemoryUiState,
    appLanguage: String,
    userName: String = "",
    partnerName: String = "",
    userAvatarPath: String? = null,
    partnerAvatarPath: String? = null,
    onSelectTab: (MemoryTab) -> Unit,
    onQueryChange: (String) -> Unit,
    onCategoryFilter: (String?) -> Unit,
    onOpenEditor: (MemoryEditorMode, String?) -> Unit,
    onStartSelection: (String?) -> Unit = {},
    onToggleEntrySelection: (String) -> Unit = {},
    onSelectAllEntries: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onDeleteSelectedRequest: () -> Unit = {},
    onComplete: (String) -> Unit,
    onRestore: (String) -> Unit,
    onRetryPreview: (String) -> Unit,
    onDeleteRequest: (String) -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteDismiss: () -> Unit,
    onCreateCategory: (String, String, String) -> Unit,
    onUpdateCategory: (String, String, String, String) -> Unit,
    onDeleteCategory: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showModeChooser by rememberSaveable { mutableStateOf(false) }
    var showCategoryDialog by rememberSaveable { mutableStateOf(false) }
    var categoryDialogTargetId by rememberSaveable { mutableStateOf<String?>(null) }
    val categoriesById = remember(state.categories) { state.categories.associateBy { it.id } }
    val categoryDialogTarget = state.categories.firstOrNull { it.id == categoryDialogTargetId }

    AmbientBackground(
        modifier = modifier
            .fillMaxSize()
            .testTag("memory_screen")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 164.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp,
                    top = 18.dp,
                    end = 16.dp,
                    bottom = 112.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    MemoryPinboardHeader(
                        state = state,
                        appLanguage = appLanguage,
                        userName = userName,
                        partnerName = partnerName,
                        userAvatarPath = userAvatarPath,
                        partnerAvatarPath = partnerAvatarPath,
                        onSelectTab = onSelectTab,
                        onQueryChange = onQueryChange,
                        onCategoryFilter = onCategoryFilter,
                        onAddCategory = {
                            categoryDialogTargetId = null
                            showCategoryDialog = true
                        },
                        onEditCategory = { category ->
                            categoryDialogTargetId = category.id
                            showCategoryDialog = true
                        },
                        onStartSelection = { onStartSelection(null) },
                        onSelectAllEntries = onSelectAllEntries,
                        onClearSelection = onClearSelection,
                        onDeleteSelectedRequest = onDeleteSelectedRequest
                    )
                }

                if (state.visibleEntries.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        MemoryEmptyState(appLanguage = appLanguage)
                    }
                } else {
                    items(
                        items = state.visibleEntries,
                        key = { it.entity.id },
                        span = { item ->
                            if (item.entity.kind == MemoryEntryKind.LINK) GridItemSpan(maxLineSpan)
                            else GridItemSpan(1)
                        }
                    ) { item ->
                        MemoryEntryCard(
                            item = item,
                            category = categoriesById[item.entity.categoryId],
                            appLanguage = appLanguage,
                            previewFailed = item.entity.id in state.failedPreviewIds,
                            onComplete = onComplete,
                            onRestore = onRestore,
                            onRetryPreview = onRetryPreview,
                            onDeleteRequest = onDeleteRequest,
                            selectionMode = state.selectionMode,
                            selected = item.entity.id in state.selectedEntryIds,
                            onToggleSelection = { onToggleEntrySelection(item.entity.id) },
                            onLongPress = { onStartSelection(item.entity.id) },
                            onOpen = {
                                onOpenEditor(
                                    if (item.entity.kind == MemoryEntryKind.LINK) MemoryEditorMode.LINK
                                    else MemoryEditorMode.NOTE,
                                    item.entity.id
                                )
                            }
                        )
                    }
                }

                state.errorKey?.let { errorKey ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = LanguageManager.tr(memoryErrorLabel(errorKey), appLanguage),
                            color = HarmonyPinkSoft,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(HarmonySurface.copy(alpha = 0.86f), RoundedCornerShape(16.dp))
                                .border(1.dp, HarmonyPink.copy(alpha = 0.40f), RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 24.dp)
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(HarmonyPink, Color(0xFFE65ADC), Color(0xFF8F63FF))
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.38f), CircleShape)
                    .clickable { showModeChooser = true }
                    .semantics {
                        role = Role.Button
                        contentDescription = LanguageManager.tr("Notiz hinzufügen", appLanguage)
                    }
                    .testTag("memory_add_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }
        }
    }

    if (showModeChooser) {
        MemoryModeChooserDialog(
            appLanguage = appLanguage,
            onDismiss = { showModeChooser = false },
            onSelect = { mode ->
                showModeChooser = false
                onOpenEditor(mode, null)
            }
        )
    }

    if (showCategoryDialog) {
        MemoryCategoryDialog(
            category = categoryDialogTarget,
            categories = state.categories,
            entryCount = categoryDialogTarget?.let { target ->
                state.categoryEntryCounts[target.id] ?: 0
            } ?: 0,
            appLanguage = appLanguage,
            onDismiss = {
                showCategoryDialog = false
                categoryDialogTargetId = null
            },
            onCreate = { name, color, icon ->
                onCreateCategory(name, color, icon)
                showCategoryDialog = false
            },
            onUpdate = { id, name, color, icon ->
                onUpdateCategory(id, name, color, icon)
                showCategoryDialog = false
                categoryDialogTargetId = null
            },
            onDelete = { id, moveTarget ->
                onDeleteCategory(id, moveTarget)
                showCategoryDialog = false
                categoryDialogTargetId = null
            }
        )
    }

    if (state.pendingDeleteEntryIds.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = onDeleteDismiss,
            containerColor = HarmonySurface,
            shape = RoundedCornerShape(24.dp),
            icon = {
                Icon(Icons.Default.Bookmarks, contentDescription = null, tint = HarmonyPinkSoft)
            },
            title = {
                Text(
                    LanguageManager.tr("Endgültig löschen", appLanguage),
                    color = HarmonyText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    LanguageManager.tr(
                        if (state.pendingDeleteEntryIds.size == 1) {
                            "Möchtest du diesen Eintrag endgültig löschen?"
                        } else {
                            "Möchtest du die ausgewählten Einträge endgültig löschen?"
                        },
                        appLanguage
                    ),
                    color = HarmonyMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = onDeleteConfirm,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("memory_delete_confirm"),
                    colors = ButtonDefaults.buttonColors(containerColor = HarmonyPink)
                ) {
                    Text(LanguageManager.tr("Löschen", appLanguage))
                }
            },
            dismissButton = {
                TextButton(onClick = onDeleteDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(LanguageManager.tr("Abbrechen", appLanguage))
                }
            }
        )
    }
}

@Composable
private fun MemoryPinboardHeader(
    state: MemoryUiState,
    appLanguage: String,
    userName: String,
    partnerName: String,
    userAvatarPath: String?,
    partnerAvatarPath: String?,
    onSelectTab: (MemoryTab) -> Unit,
    onQueryChange: (String) -> Unit,
    onCategoryFilter: (String?) -> Unit,
    onAddCategory: () -> Unit,
    onEditCategory: (MemoryCategoryEntity) -> Unit,
    onStartSelection: () -> Unit,
    onSelectAllEntries: () -> Unit,
    onClearSelection: () -> Unit,
    onDeleteSelectedRequest: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_full),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(28.dp))
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = LanguageManager.tr("Das müssen wir uns merken", appLanguage),
                color = HarmonyText,
                fontSize = 27.sp,
                lineHeight = 31.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            MemoryCoupleAvatars(
                userName = userName,
                partnerName = partnerName,
                userAvatarPath = userAvatarPath,
                partnerAvatarPath = partnerAvatarPath
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = LanguageManager.tr("Gemeinsam sammeln. Nie vergessen.", appLanguage),
                color = HarmonyMuted,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center
            )
        }

        if (state.selectionMode) {
            Spacer(Modifier.height(14.dp))
            Surface(
                color = HarmonySurface2.copy(alpha = 0.92f),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, HarmonyPinkSoft.copy(alpha = 0.56f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LanguageManager.tr("${state.selectedEntryIds.size} ausgewählt", appLanguage),
                        color = HarmonyText,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f).padding(start = 6.dp)
                    )
                    IconButton(
                        onClick = onSelectAllEntries,
                        modifier = Modifier.testTag("memory_selection_all")
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = LanguageManager.tr("Alle auswählen", appLanguage), tint = HarmonyPinkSoft)
                    }
                    IconButton(
                        onClick = onDeleteSelectedRequest,
                        enabled = state.selectedEntryIds.isNotEmpty(),
                        modifier = Modifier.testTag("memory_selection_delete")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = LanguageManager.tr("Auswahl löschen", appLanguage), tint = HarmonyPinkSoft)
                    }
                    IconButton(
                        onClick = onClearSelection,
                        modifier = Modifier.testTag("memory_selection_close")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = LanguageManager.tr("Auswahl schließen", appLanguage), tint = HarmonyMuted)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HarmonySurface.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
                .border(1.dp, HarmonyLine, RoundedCornerShape(18.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MemoryTabButton(
                label = LanguageManager.tr("Aktuell", appLanguage),
                selected = state.selectedTab == MemoryTab.CURRENT,
                onClick = { onSelectTab(MemoryTab.CURRENT) },
                modifier = Modifier.weight(1f).testTag("memory_tab_current")
            )
            MemoryTabButton(
                label = LanguageManager.tr("Erledigte Notizen", appLanguage),
                selected = state.selectedTab == MemoryTab.ARCHIVED,
                onClick = { onSelectTab(MemoryTab.ARCHIVED) },
                modifier = Modifier.weight(1f).testTag("memory_tab_archived")
            )
        }

        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = HarmonyPinkSoft)
            },
            trailingIcon = {
                IconButton(
                    onClick = onStartSelection,
                    modifier = Modifier.testTag("memory_selection_start")
                ) {
                    Icon(
                        Icons.Default.DoneAll,
                        contentDescription = LanguageManager.tr("Notizen auswählen", appLanguage),
                        tint = HarmonyPinkSoft
                    )
                }
            },
            placeholder = {
                Text(LanguageManager.tr("Suchen", appLanguage), color = HarmonyMuted)
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("memory_search"),
            shape = RoundedCornerShape(22.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = HarmonySurface2.copy(alpha = 0.82f),
                unfocusedContainerColor = HarmonySurface.copy(alpha = 0.76f),
                focusedTextColor = HarmonyText,
                unfocusedTextColor = HarmonyText,
                focusedIndicatorColor = HarmonyPinkSoft,
                unfocusedIndicatorColor = HarmonyLine,
                cursorColor = HarmonyPinkSoft
            )
        )

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MemoryCategoryChip(
                label = LanguageManager.tr("Alle", appLanguage),
                icon = Icons.Default.Bookmarks,
                selected = state.selectedCategoryId == null,
                accent = HarmonyPinkSoft,
                onClick = { onCategoryFilter(null) },
                modifier = Modifier.testTag("memory_category_all")
            )
            state.categories.forEach { category ->
                MemoryCategoryChip(
                    label = memoryCategoryLabel(category, appLanguage),
                    icon = memoryCategoryIcon(category.iconKey),
                    selected = state.selectedCategoryId == category.id,
                    accent = memoryCategoryColor(category.colorKey),
                    onClick = {
                        if (state.selectedCategoryId == category.id) onEditCategory(category)
                        else onCategoryFilter(category.id)
                    },
                    modifier = Modifier.testTag("memory_category_${category.id}")
                )
            }
            MemoryCategoryChip(
                label = LanguageManager.tr("Kategorie hinzufügen", appLanguage),
                icon = Icons.Default.Add,
                selected = false,
                accent = HarmonyPurple,
                onClick = onAddCategory,
                modifier = Modifier.testTag("memory_category_add")
            )
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun MemoryCoupleAvatars(
    userName: String,
    partnerName: String,
    userAvatarPath: String?,
    partnerAvatarPath: String?
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        MemoryPinboardAvatar(
            name = userName,
            path = userAvatarPath,
            colors = listOf(HarmonyPink, HarmonyPinkSoft)
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 3.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(HarmonyPink),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(13.dp)
            )
        }
        MemoryPinboardAvatar(
            name = partnerName,
            path = partnerAvatarPath,
            colors = listOf(HarmonyPurple, Color(0xFF7567FF))
        )
    }
}

@Composable
private fun MemoryPinboardAvatar(name: String, path: String?, colors: List<Color>) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(colors))
            .border(2.dp, Color.White.copy(alpha = 0.72f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!path.isNullOrBlank()) {
            AsyncImage(
                model = File(path),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = name.take(1).ifBlank { "H" }.uppercase(),
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun MemoryTabButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(15.dp),
        color = if (selected) HarmonyPink.copy(alpha = 0.22f) else Color.Transparent,
        border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, HarmonyPinkSoft) else null
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
            Text(
                text = label,
                color = if (selected) HarmonyText else HarmonyMuted,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MemoryCategoryChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = FontWeight.SemiBold) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
        modifier = modifier.heightIn(min = 48.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = HarmonySurface.copy(alpha = 0.70f),
            labelColor = HarmonyMuted,
            iconColor = HarmonyMuted,
            selectedContainerColor = accent.copy(alpha = 0.22f),
            selectedLabelColor = HarmonyText,
            selectedLeadingIconColor = accent
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = HarmonyLine,
            selectedBorderColor = accent
        )
    )
}

@Composable
private fun MemoryEmptyState(appLanguage: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HarmonySurface.copy(alpha = 0.76f), RoundedCornerShape(24.dp))
            .border(1.dp, HarmonyLine, RoundedCornerShape(24.dp))
            .padding(horizontal = 24.dp, vertical = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Bookmarks,
            contentDescription = null,
            tint = HarmonyPinkSoft,
            modifier = Modifier.size(42.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = LanguageManager.tr("Keine Einträge gefunden", appLanguage),
            color = HarmonyText,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MemoryModeChooserDialog(
    appLanguage: String,
    onDismiss: () -> Unit,
    onSelect: (MemoryEditorMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HarmonySurface,
        shape = RoundedCornerShape(26.dp),
        title = {
            Text(
                text = LanguageManager.tr("Notiz hinzufügen", appLanguage),
                color = HarmonyText,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ModeChoiceButton(
                    label = LanguageManager.tr("Notiz", appLanguage),
                    icon = Icons.Default.Notes,
                    tag = "memory_mode_note",
                    onClick = { onSelect(MemoryEditorMode.NOTE) }
                )
                ModeChoiceButton(
                    label = LanguageManager.tr("Liste", appLanguage),
                    icon = Icons.Default.Checklist,
                    tag = "memory_mode_list",
                    onClick = { onSelect(MemoryEditorMode.LIST) }
                )
                ModeChoiceButton(
                    label = LanguageManager.tr("Link", appLanguage),
                    icon = Icons.Default.AddLink,
                    tag = "memory_mode_link",
                    onClick = { onSelect(MemoryEditorMode.LINK) }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(LanguageManager.tr("Abbrechen", appLanguage))
            }
        }
    )
}

@Composable
private fun ModeChoiceButton(
    label: String,
    icon: ImageVector,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .clickable(onClick = onClick)
            .testTag(tag),
        color = HarmonySurface2.copy(alpha = 0.86f),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, HarmonyLine)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = HarmonyPinkSoft, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, color = HarmonyText, fontWeight = FontWeight.Bold)
        }
    }
}

private fun memoryErrorLabel(errorKey: String): String = when (errorKey) {
    "memory_invalid_link" -> "Bitte gib einen gültigen HTTP- oder HTTPS-Link ein"
    "memory_create_category_failed", "memory_update_category_failed", "memory_delete_category_failed" ->
        "Kategorie konnte nicht gespeichert werden"
    else -> "Notiz konnte nicht gespeichert werden"
}
