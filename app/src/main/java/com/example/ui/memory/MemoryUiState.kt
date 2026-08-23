package com.example.ui.memory

import com.example.data.MemoryBucket
import com.example.data.model.MemoryCategoryEntity
import com.example.data.model.MemoryEntryEntity

enum class MemoryTab { CURRENT, ARCHIVED }

enum class MemoryEditorMode { NOTE, LIST, LINK }

data class MemoryEntryUi(
    val entity: MemoryEntryEntity,
    val bucket: MemoryBucket
)

data class MemoryUiState(
    val categories: List<MemoryCategoryEntity> = emptyList(),
    val categoryEntryCounts: Map<String, Int> = emptyMap(),
    val visibleEntries: List<MemoryEntryUi> = emptyList(),
    val selectedTab: MemoryTab = MemoryTab.CURRENT,
    val selectedCategoryId: String? = null,
    val query: String = "",
    val editorMode: MemoryEditorMode? = null,
    val editorEntryId: String? = null,
    val selectionMode: Boolean = false,
    val selectedEntryIds: Set<String> = emptySet(),
    val failedPreviewIds: Set<String> = emptySet(),
    val pendingDeleteEntryIds: Set<String> = emptySet(),
    val errorKey: String? = null
)
