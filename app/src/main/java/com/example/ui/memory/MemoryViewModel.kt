package com.example.ui.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.LinkPreviewResolver
import com.example.data.LinkPreviewResult
import com.example.data.MemoryArchivePolicy
import com.example.data.MemoryBucket
import com.example.data.normalizeHttpUrl
import com.example.data.model.MemoryChecklistCodec
import com.example.data.model.MemoryChecklistItem
import com.example.data.model.MemoryClock
import com.example.data.model.MemoryEntryEntity
import com.example.data.model.MemoryEntryKind
import com.example.data.model.SystemMemoryClock
import com.example.data.repository.MemoryRepository
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MemoryViewModel(
    private val repository: MemoryRepository,
    private val linkPreviewResolver: LinkPreviewResolver,
    private val clock: MemoryClock = SystemMemoryClock,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val localState = MutableStateFlow(MemoryLocalState(nowMillis = clock.nowMillis()))
    private val entryGuards = ConcurrentHashMap<String, MemoryEntryGuard>()

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
            categoryEntryCounts = entries.groupingBy { it.categoryId }.eachCount(),
            visibleEntries = visible,
            selectedTab = local.selectedTab,
            selectedCategoryId = local.selectedCategoryId,
            query = local.query,
            editorMode = local.editorMode,
            editorEntryId = local.editorEntryId,
            selectionMode = local.selectionMode,
            selectedEntryIds = local.selectedEntryIds,
            failedPreviewIds = local.failedPreviewIds,
            pendingDeleteEntryIds = local.pendingDeleteEntryIds,
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

    fun startSelection(entryId: String? = null) {
        updateLocal {
            copy(
                selectionMode = true,
                selectedEntryIds = entryId?.let { selectedEntryIds + it } ?: selectedEntryIds
            )
        }
    }

    fun toggleEntrySelection(entryId: String) {
        updateLocal {
            copy(
                selectionMode = true,
                selectedEntryIds = if (entryId in selectedEntryIds) selectedEntryIds - entryId
                else selectedEntryIds + entryId
            )
        }
    }

    fun selectAllVisibleEntries() {
        val visibleIds = uiState.value.visibleEntries.mapTo(linkedSetOf()) { it.entity.id }
        updateLocal { copy(selectionMode = true, selectedEntryIds = visibleIds) }
    }

    fun clearSelection() {
        updateLocal { copy(selectionMode = false, selectedEntryIds = emptySet()) }
    }

    fun saveNote(entryId: String?, categoryId: String, title: String, body: String?) {
        val targetId = entryId ?: UUID.randomUUID().toString()
        val request = beginEntryRequest(targetId)
        launchEntryOperation(request, ERROR_SAVE_NOTE) {
            request.guard.rowMutex.withLock {
                if (!request.isLatest()) return@withLock
                val now = clock.nowMillis()
                val existing = entryId?.let { repository.getEntry(it) }
                if (!request.isLatest()) return@withLock
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
                    id = targetId,
                    categoryId = categoryId,
                    kind = MemoryEntryKind.NOTE,
                    title = title.trim(),
                    body = trimmedBody,
                    createdAt = now,
                    updatedAt = now
                )
                if (existing == null) repository.insertEntries(listOf(entry)) else repository.updateEntry(entry)
                if (request.isLatest()) request.updateFailedState { it - targetId }
            }
        }
    }

    fun saveList(
        entryId: String?,
        categoryId: String,
        title: String,
        items: List<MemoryChecklistItem>
    ) {
        val normalizedItems = items.mapNotNull { item ->
            val text = item.text.trim()
            if (item.id.isBlank() || text.isEmpty()) null else item.copy(text = text)
        }
        if (normalizedItems.isEmpty()) return

        val targetId = entryId ?: UUID.randomUUID().toString()
        val request = beginEntryRequest(targetId)
        launchEntryOperation(request, ERROR_SAVE_LIST) {
            request.guard.rowMutex.withLock {
                if (!request.isLatest()) return@withLock
                val now = clock.nowMillis()
                val existing = entryId?.let { repository.getEntry(it) }
                if (!request.isLatest()) return@withLock
                if (entryId != null && existing == null) error("Memory entry no longer exists.")
                val entry = existing?.copy(
                    categoryId = categoryId,
                    kind = MemoryEntryKind.LIST,
                    title = title.trim().ifBlank { normalizedItems.first().text },
                    body = MemoryChecklistCodec.encode(normalizedItems),
                    url = null,
                    previewTitle = null,
                    previewDescription = null,
                    previewImageUrl = null,
                    previewSiteName = null,
                    previewFetchedAt = null,
                    updatedAt = now
                ) ?: MemoryEntryEntity(
                    id = targetId,
                    categoryId = categoryId,
                    kind = MemoryEntryKind.LIST,
                    title = title.trim().ifBlank { normalizedItems.first().text },
                    body = MemoryChecklistCodec.encode(normalizedItems),
                    createdAt = now,
                    updatedAt = now
                )
                if (existing == null) repository.insertEntries(listOf(entry)) else repository.updateEntry(entry)
                if (request.isLatest()) request.updateFailedState { it - targetId }
            }
        }
    }

    fun saveLink(entryId: String?, categoryId: String, rawUrl: String, note: String?) {
        val normalizedUrl = normalizeMemoryUrl(rawUrl)
        if (normalizedUrl == null) {
            updateLocal { copy(errorKey = ERROR_INVALID_LINK) }
            return
        }
        val targetId = entryId ?: UUID.randomUUID().toString()
        val request = beginEntryRequest(targetId)
        launchEntryOperation(request, ERROR_SAVE_LINK) {
            val saved = request.guard.rowMutex.withLock {
                if (!request.isLatest()) return@withLock false
                val now = clock.nowMillis()
                val existing = entryId?.let { repository.getEntry(it) }
                if (!request.isLatest()) return@withLock false
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
                    id = targetId,
                    categoryId = categoryId,
                    kind = MemoryEntryKind.LINK,
                    title = normalizedUrl,
                    body = trimmedNote,
                    url = normalizedUrl,
                    createdAt = now,
                    updatedAt = now
                )

                if (existing == null) repository.insertEntries(listOf(entry)) else repository.updateEntry(entry)
                if (!request.isLatest()) return@withLock false
                request.updateFailedState { it - targetId }
                true
            }
            if (saved) resolvePreview(request, normalizedUrl)
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
        val request = beginEntryRequest(entryId)
        launchEntryOperation(request, errorKey = null) {
            val url = request.guard.rowMutex.withLock {
                if (!request.isLatest()) return@withLock null
                val entry = repository.getEntry(entryId)
                if (!request.isLatest()) return@withLock null
                if (entry?.kind != MemoryEntryKind.LINK || entry.url == null) {
                    request.updateFailedState { it - entryId }
                    return@withLock null
                }
                request.updateFailedState { it - entryId }
                entry.url
            }
            if (url != null) resolvePreview(request, url)
        }
    }

    fun complete(entryId: String) {
        launchOperation(ERROR_COMPLETE) {
            entryGuard(entryId).rowMutex.withLock {
                val now = clock.nowMillis()
                repository.setCompleted(entryId, completedAt = now, updatedAt = now)
            }
        }
    }

    fun restore(entryId: String) {
        launchOperation(ERROR_RESTORE) {
            entryGuard(entryId).rowMutex.withLock {
                repository.setCompleted(entryId, completedAt = null, updatedAt = clock.nowMillis())
            }
        }
    }

    fun requestPermanentDelete(entryId: String) {
        updateLocal { copy(pendingDeleteEntryIds = setOf(entryId)) }
    }

    fun requestSelectedDelete() {
        val selected = localState.value.selectedEntryIds
        if (selected.isNotEmpty()) updateLocal { copy(pendingDeleteEntryIds = selected) }
    }

    fun dismissPermanentDelete() {
        updateLocal { copy(pendingDeleteEntryIds = emptySet()) }
    }

    fun confirmPermanentDelete() {
        val entryIds = localState.value.pendingDeleteEntryIds
        if (entryIds.isEmpty()) return
        entryIds.forEach(::beginEntryRequest)
        launchOperation(ERROR_DELETE_ENTRY) {
            repository.deleteEntries(entryIds)
            updateFailedPreviewIds { it - entryIds }
            updateLocal {
                if (pendingDeleteEntryIds == entryIds) {
                    copy(
                        pendingDeleteEntryIds = emptySet(),
                        selectionMode = false,
                        selectedEntryIds = selectedEntryIds - entryIds
                    )
                } else {
                    this
                }
            }
        }
    }

    fun refreshTime() {
        updateLocal { copy(nowMillis = clock.nowMillis()) }
    }

    private suspend fun resolvePreview(request: MemoryEntryRequest, requestedUrl: String) {
        try {
            val result = linkPreviewResolver.resolve(requestedUrl)
            request.guard.rowMutex.withLock {
                if (!request.isLatest()) return
                val current = repository.getEntry(request.entryId)
                if (!request.isLatest()) return
                if (current?.kind != MemoryEntryKind.LINK || current.url != requestedUrl) return

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
                        if (request.isLatest()) request.updateFailedState { it - request.entryId }
                    }

                    is LinkPreviewResult.Failure -> {
                        repository.updateEntry(current.withoutPreview())
                        if (request.isLatest()) request.markPreviewFailed()
                    }
                }
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Throwable) {
            request.markPreviewFailed()
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

    private fun launchEntryOperation(
        request: MemoryEntryRequest,
        errorKey: String?,
        block: suspend () -> Unit
    ) {
        val job = viewModelScope.launch(ioDispatcher, start = CoroutineStart.LAZY) {
            if (errorKey != null) updateLocal { copy(errorKey = null) }
            try {
                block()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                if (errorKey == null) {
                    request.markPreviewFailed()
                } else {
                    request.ifLatest { updateLocal { copy(errorKey = errorKey) } }
                }
            }
        }
        if (request.register(job)) {
            job.invokeOnCompletion { request.unregister(job) }
            job.start()
        } else {
            job.cancel()
        }
    }

    private inline fun updateLocal(transform: MemoryLocalState.() -> MemoryLocalState) {
        localState.update { it.transform() }
    }

    private fun entryGuard(entryId: String): MemoryEntryGuard =
        entryGuards.computeIfAbsent(entryId) { MemoryEntryGuard() }

    private fun beginEntryRequest(entryId: String): MemoryEntryRequest {
        val guard = entryGuard(entryId)
        val (token, previousJob) = guard.beginRequest()
        previousJob?.cancel()
        return MemoryEntryRequest(entryId, guard, token, ::updateFailedPreviewIds)
    }

    private fun updateFailedPreviewIds(transform: (Set<String>) -> Set<String>) {
        updateLocal { copy(failedPreviewIds = transform(failedPreviewIds)) }
    }

    private companion object {
        const val ERROR_SEED_DEFAULTS = "memory_seed_defaults_failed"
        const val ERROR_SAVE_NOTE = "memory_save_note_failed"
        const val ERROR_SAVE_LIST = "memory_save_list_failed"
        const val ERROR_SAVE_LINK = "memory_save_link_failed"
        const val ERROR_INVALID_LINK = "memory_invalid_link"
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
    val selectionMode: Boolean = false,
    val selectedEntryIds: Set<String> = emptySet(),
    val failedPreviewIds: Set<String> = emptySet(),
    val pendingDeleteEntryIds: Set<String> = emptySet(),
    val nowMillis: Long,
    val errorKey: String? = null
)

private class MemoryEntryGuard {
    val rowMutex = Mutex()
    private var latestToken: Any? = null
    private var activeJob: Job? = null

    fun beginRequest(): Pair<Any, Job?> = synchronized(this) {
        val token = Any()
        val previousJob = activeJob
        latestToken = token
        activeJob = null
        token to previousJob
    }

    fun isLatest(token: Any): Boolean = synchronized(this) {
        latestToken === token
    }

    fun ifLatest(token: Any, block: () -> Unit) {
        synchronized(this) {
            if (latestToken === token) block()
        }
    }

    fun register(token: Any, job: Job): Boolean = synchronized(this) {
        if (latestToken !== token) false
        else {
            activeJob = job
            true
        }
    }

    fun unregister(token: Any, job: Job) {
        synchronized(this) {
            if (latestToken === token && activeJob === job) activeJob = null
        }
    }
}

private class MemoryEntryRequest(
    val entryId: String,
    val guard: MemoryEntryGuard,
    private val token: Any,
    private val updateFailedIds: ((Set<String>) -> Set<String>) -> Unit
) {
    fun isLatest(): Boolean = guard.isLatest(token)

    fun ifLatest(block: () -> Unit) {
        guard.ifLatest(token, block)
    }

    fun register(job: Job): Boolean = guard.register(token, job)

    fun unregister(job: Job) {
        guard.unregister(token, job)
    }

    fun updateFailedState(transform: (Set<String>) -> Set<String>) {
        guard.ifLatest(token) { updateFailedIds(transform) }
    }

    fun markPreviewFailed() {
        updateFailedState { it + entryId }
    }
}

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

private fun MemoryEntryEntity.withoutPreview(): MemoryEntryEntity = copy(
    previewTitle = null,
    previewDescription = null,
    previewImageUrl = null,
    previewSiteName = null,
    previewFetchedAt = null
)

private fun normalizeMemoryUrl(rawUrl: String): String? = normalizeHttpUrl(rawUrl)?.let { normalized ->
    val uri = URI(normalized)
    if (uri.rawPath.isNullOrEmpty()) {
        URI(uri.scheme, uri.rawAuthority, "/", uri.rawQuery, uri.rawFragment).toString()
    } else {
        normalized
    }
}
