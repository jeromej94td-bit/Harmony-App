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
    val visibleEntries: List<MemoryEntryUi> = emptyList(),
    val selectedTab: MemoryTab = MemoryTab.CURRENT,
    val selectedCategoryId: String? = null,
    val query: String = "",
    val editorMode: MemoryEditorMode? = null,
    val editorEntryId: String? = null,
    val failedPreviewIds: Set<String> = emptySet(),
    val pendingDeleteEntryId: String? = null,
    val nextExpiryAt: Long? = null,
    val errorKey: String? = null
)
