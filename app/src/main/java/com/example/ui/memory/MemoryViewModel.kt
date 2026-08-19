package com.example.ui.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.LinkPreviewResolver
import com.example.data.LinkPreviewResult
import com.example.data.MemoryArchivePolicy
import com.example.data.MemoryBucket
import com.example.data.normalizeHttpUrl
import com.example.data.model.MemoryClock
import com.example.data.model.MemoryEntryEntity
import com.example.data.model.MemoryEntryKind
import com.example.data.model.SystemMemoryClock
import com.example.data.repository.MemoryRepository
import java.net.URI
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MemoryViewModel(
    private val repository: MemoryRepository,
    private val linkPreviewResolver: LinkPreviewResolver,
    private val clock: MemoryClock = SystemMemoryClock,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val localState = MutableStateFlow(MemoryLocalState(nowMillis = clock.nowMillis()))
    private var expiryJob: Job? = null

    val uiState: StateFlow<MemoryUiState> = combine(
        repository.categories,
        repository.entries,
        localState
    ) { categories, entries, local ->
        val classified = entries.map { entry ->
            MemoryEntryUi(entry, MemoryArchivePolicy.bucketAt(entry, local.nowMillis))
        }
        val visible = classified
            .asSequence()
            .filter { item ->
                when (local.selectedTab) {
                    MemoryTab.CURRENT -> item.bucket != MemoryBucket.ARCHIVED
                    MemoryTab.ARCHIVED -> item.bucket == MemoryBucket.ARCHIVED
                }
            }
            .filter { local.selectedCategoryId == null || it.entity.categoryId == local.selectedCategoryId }
            .filter { it.entity.matches(local.query) }
            .let { items ->
                when (local.selectedTab) {
                    MemoryTab.CURRENT -> items.sortedWith(
                        compareBy<MemoryEntryUi> {
                            if (it.bucket == MemoryBucket.CURRENT_OPEN) 0 else 1
                        }.thenByDescending { it.entity.updatedAt }
                            .thenByDescending { it.entity.createdAt }
                    )

                    MemoryTab.ARCHIVED -> items.sortedWith(
                        compareByDescending<MemoryEntryUi> { it.entity.completedAt ?: Long.MIN_VALUE }
                            .thenByDescending { it.entity.updatedAt }
                    )
                }
            }
            .toList()

        MemoryUiState(
            categories = categories,
            visibleEntries = visible,
            selectedTab = local.selectedTab,
            selectedCategoryId = local.selectedCategoryId,
            query = local.query,
            editorMode = local.editorMode,
            editorEntryId = local.editorEntryId,
            failedPreviewIds = local.failedPreviewIds,
            pendingDeleteEntryId = local.pendingDeleteEntryId,
            nextExpiryAt = MemoryArchivePolicy.nextExpiryAt(entries, local.nowMillis),
            errorKey = local.errorKey
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = MemoryUiState()
    )

    init {
        launchOperation(ERROR_SEED_DEFAULTS) {
            repository.ensureDefaultCategories(clock.nowMillis())
        }
        viewModelScope.launch {
            uiState.map { it.nextExpiryAt }
                .distinctUntilChanged()
                .collect(::scheduleExpiry)
        }
    }

    fun selectTab(tab: MemoryTab) {
        updateLocal { copy(selectedTab = tab) }
    }

    fun setQuery(query: String) {
        updateLocal { copy(query = query) }
    }

    fun setCategoryFilter(categoryId: String?) {
        updateLocal { copy(selectedCategoryId = categoryId) }
    }

    fun openEditor(mode: MemoryEditorMode, entryId: String?) {
        updateLocal { copy(editorMode = mode, editorEntryId = entryId) }
    }

    fun closeEditor() {
        updateLocal { copy(editorMode = null, editorEntryId = null) }
    }

    fun saveNote(entryId: String?, categoryId: String, title: String, body: String?) {
        launchOperation(ERROR_SAVE_NOTE) {
            val now = clock.nowMillis()
            val existing = entryId?.let { repository.getEntry(it) }
            if (entryId != null && existing == null) error("Memory entry no longer exists.")
            val trimmedBody = body?.trim()?.takeIf { it.isNotEmpty() }
            val entry = existing?.copy(
                categoryId = categoryId,
                kind = MemoryEntryKind.NOTE,
                title = title.trim(),
                body = trimmedBody,
                url = null,
                previewTitle = null,
                previewDescription = null,
                previewImageUrl = null,
                previewSiteName = null,
                previewFetchedAt = null,
                updatedAt = now
            ) ?: MemoryEntryEntity(
                id = UUID.randomUUID().toString(),
                categoryId = categoryId,
                kind = MemoryEntryKind.NOTE,
                title = title.trim(),
                body = trimmedBody,
                createdAt = now,
                updatedAt = now
            )
            if (existing == null) repository.insertEntries(listOf(entry)) else repository.updateEntry(entry)
        }
    }

    fun saveList(categoryId: String, rawLines: String) {
        launchOperation(ERROR_SAVE_LIST) {
            val now = clock.nowMillis()
            val entries = rawLines.lineSequence()
                .map(String::trim)
                .filter(String::isNotEmpty)
                .map { title ->
                    MemoryEntryEntity(
                        id = UUID.randomUUID().toString(),
                        categoryId = categoryId,
                        kind = MemoryEntryKind.NOTE,
                        title = title,
                        createdAt = now,
                        updatedAt = now
                    )
                }
                .toList()
            if (entries.isNotEmpty()) repository.insertEntries(entries)
        }
    }

    fun saveLink(entryId: String?, categoryId: String, rawUrl: String, note: String?) {
        val normalizedUrl = normalizeMemoryUrl(rawUrl)
        if (normalizedUrl == null) {
            updateLocal { copy(errorKey = ERROR_INVALID_LINK) }
            return
        }
        launchOperation(ERROR_SAVE_LINK) {
            val now = clock.nowMillis()
            val existing = entryId?.let { repository.getEntry(it) }
            if (entryId != null && existing == null) error("Memory entry no longer exists.")
            val trimmedNote = note?.trim()?.takeIf { it.isNotEmpty() }
            val entry = existing?.copy(
                categoryId = categoryId,
                kind = MemoryEntryKind.LINK,
                title = normalizedUrl,
                body = trimmedNote,
                url = normalizedUrl,
                previewTitle = null,
                previewDescription = null,
                previewImageUrl = null,
                previewSiteName = null,
                previewFetchedAt = null,
                updatedAt = now
            ) ?: MemoryEntryEntity(
                id = UUID.randomUUID().toString(),
                categoryId = categoryId,
                kind = MemoryEntryKind.LINK,
                title = normalizedUrl,
                body = trimmedNote,
                url = normalizedUrl,
                createdAt = now,
                updatedAt = now
            )

            if (existing == null) repository.insertEntries(listOf(entry)) else repository.updateEntry(entry)
            updateLocal { copy(failedPreviewIds = failedPreviewIds - entry.id) }
            resolvePreview(entry.id, normalizedUrl)
        }
    }

    fun createCategory(name: String, colorKey: String, iconKey: String) {
        launchOperation(ERROR_CREATE_CATEGORY) {
            repository.createCategory(name.trim(), colorKey, iconKey, clock.nowMillis())
        }
    }

    fun updateCategory(id: String, name: String, colorKey: String, iconKey: String) {
        launchOperation(ERROR_UPDATE_CATEGORY) {
            repository.updateCategory(id, name.trim(), colorKey, iconKey, clock.nowMillis())
        }
    }

    fun deleteCategory(id: String, moveToId: String) {
        launchOperation(ERROR_DELETE_CATEGORY) {
            repository.deleteCustomCategory(id, moveToId, clock.nowMillis())
            updateLocal {
                if (selectedCategoryId == id) copy(selectedCategoryId = null) else this
            }
        }
    }

    fun retryPreview(entryId: String) {
        launchOperation(ERROR_PREVIEW) {
            val entry = repository.getEntry(entryId) ?: return@launchOperation
            val url = entry.url ?: return@launchOperation
            updateLocal { copy(failedPreviewIds = failedPreviewIds - entryId) }
            resolvePreview(entryId, url)
        }
    }

    fun complete(entryId: String) {
        launchOperation(ERROR_COMPLETE) {
            val now = clock.nowMillis()
            repository.setCompleted(entryId, completedAt = now, updatedAt = now)
        }
    }

    fun restore(entryId: String) {
        launchOperation(ERROR_RESTORE) {
            repository.setCompleted(entryId, completedAt = null, updatedAt = clock.nowMillis())
        }
    }

    fun requestPermanentDelete(entryId: String) {
        updateLocal { copy(pendingDeleteEntryId = entryId) }
    }

    fun dismissPermanentDelete() {
        updateLocal { copy(pendingDeleteEntryId = null) }
    }

    fun confirmPermanentDelete() {
        val entryId = localState.value.pendingDeleteEntryId ?: return
        launchOperation(ERROR_DELETE_ENTRY) {
            repository.deleteEntry(entryId)
            updateLocal {
                if (pendingDeleteEntryId == entryId) copy(pendingDeleteEntryId = null) else this
            }
        }
    }

    fun refreshTime() {
        updateLocal { copy(nowMillis = clock.nowMillis()) }
    }

    private suspend fun resolvePreview(entryId: String, requestedUrl: String) {
        val result = try {
            linkPreviewResolver.resolve(requestedUrl)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Throwable) {
            LinkPreviewResult.Failure(requestedUrl)
        }
        val current = repository.getEntry(entryId)
        if (current?.url != requestedUrl) return

        when (result) {
            is LinkPreviewResult.Success -> {
                val preview = result.preview
                repository.updateEntry(
                    current.copy(
                        previewTitle = preview.title,
                        previewDescription = preview.description,
                        previewImageUrl = preview.imageUrl,
                        previewSiteName = preview.siteName,
                        previewFetchedAt = clock.nowMillis()
                    )
                )
                updateLocal { copy(failedPreviewIds = failedPreviewIds - entryId) }
            }

            is LinkPreviewResult.Failure -> {
                updateLocal { copy(failedPreviewIds = failedPreviewIds + entryId) }
            }
        }
    }

    private fun scheduleExpiry(expiryAt: Long?) {
        expiryJob?.cancel()
        expiryJob = expiryAt?.let { target ->
            viewModelScope.launch {
                delay((target - clock.nowMillis()).coerceAtLeast(0L))
                refreshTime()
            }
        }
    }

    private fun launchOperation(errorKey: String, block: suspend () -> Unit) {
        viewModelScope.launch(ioDispatcher) {
            updateLocal { copy(errorKey = null) }
            try {
                block()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                updateLocal { copy(errorKey = errorKey) }
            }
        }
    }

    private inline fun updateLocal(transform: MemoryLocalState.() -> MemoryLocalState) {
        localState.value = localState.value.transform()
    }

    private companion object {
        const val ERROR_SEED_DEFAULTS = "memory_seed_defaults_failed"
        const val ERROR_SAVE_NOTE = "memory_save_note_failed"
        const val ERROR_SAVE_LIST = "memory_save_list_failed"
        const val ERROR_SAVE_LINK = "memory_save_link_failed"
        const val ERROR_INVALID_LINK = "memory_invalid_link"
        const val ERROR_PREVIEW = "memory_preview_failed"
        const val ERROR_CREATE_CATEGORY = "memory_create_category_failed"
        const val ERROR_UPDATE_CATEGORY = "memory_update_category_failed"
        const val ERROR_DELETE_CATEGORY = "memory_delete_category_failed"
        const val ERROR_COMPLETE = "memory_complete_failed"
        const val ERROR_RESTORE = "memory_restore_failed"
        const val ERROR_DELETE_ENTRY = "memory_delete_entry_failed"
    }
}

class MemoryViewModelFactory(
    private val repository: MemoryRepository,
    private val linkPreviewResolver: LinkPreviewResolver,
    private val clock: MemoryClock = SystemMemoryClock
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == MemoryViewModel::class.java)
        @Suppress("UNCHECKED_CAST")
        return MemoryViewModel(repository, linkPreviewResolver, clock) as T
    }
}

private data class MemoryLocalState(
    val selectedTab: MemoryTab = MemoryTab.CURRENT,
    val selectedCategoryId: String? = null,
    val query: String = "",
    val editorMode: MemoryEditorMode? = null,
    val editorEntryId: String? = null,
    val failedPreviewIds: Set<String> = emptySet(),
    val pendingDeleteEntryId: String? = null,
    val nowMillis: Long,
    val errorKey: String? = null
)

private fun MemoryEntryEntity.matches(query: String): Boolean {
    val normalized = query.trim()
    if (normalized.isEmpty()) return true
    return sequenceOf(
        title,
        body,
        url,
        previewTitle,
        previewDescription,
        previewSiteName
    ).filterNotNull().any { it.contains(normalized, ignoreCase = true) }
}

private fun normalizeMemoryUrl(rawUrl: String): String? = normalizeHttpUrl(rawUrl)?.let { normalized ->
    val uri = URI(normalized)
    if (uri.rawPath.isNullOrEmpty()) {
        URI(uri.scheme, uri.rawAuthority, "/", uri.rawQuery, uri.rawFragment).toString()
    } else {
        normalized
    }
}
