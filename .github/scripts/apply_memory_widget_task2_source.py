from pathlib import Path

path = Path('app/src/main/java/com/example/ui/memory/MemoryViewModel.kt')
text = path.read_text(encoding='utf-8')

def replace_once(old: str, new: str, label: str):
    global text
    if old not in text:
        raise SystemExit(f'anchor not found: {label}')
    text = text.replace(old, new, 1)

replace_once(
'''    private val clock: MemoryClock = SystemMemoryClock,\n    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO\n) : ViewModel() {\n''',
'''    private val clock: MemoryClock = SystemMemoryClock,\n    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,\n    private val onMemoryChanged: () -> Unit = {}\n) : ViewModel() {\n''',
'constructor callback'
)

replace_once(
'''    fun closeEditor() {\n        updateLocal { copy(editorMode = null, editorEntryId = null) }\n    }\n\n    fun saveNote''',
'''    fun closeEditor() {\n        updateLocal { copy(editorMode = null, editorEntryId = null) }\n    }\n\n    fun openEntryFromWidget(entryId: String) {\n        viewModelScope.launch(ioDispatcher) {\n            val entry = repository.getEntry(entryId) ?: return@launch\n            updateLocal {\n                copy(\n                    selectedTab = MemoryTab.CURRENT,\n                    selectedCategoryId = null,\n                    query = "",\n                    editorMode = if (entry.kind == MemoryEntryKind.LINK) MemoryEditorMode.LINK else MemoryEditorMode.NOTE,\n                    editorEntryId = entry.id\n                )\n            }\n        }\n    }\n\n    fun saveNote''',
'open entry helper'
)

replace_once(
'''                if (existing == null) repository.insertEntries(listOf(entry)) else repository.updateEntry(entry)\n                if (request.isLatest()) request.updateFailedState { it - targetId }\n''',
'''                if (existing == null) repository.insertEntries(listOf(entry)) else repository.updateEntry(entry)\n                if (request.isLatest()) {\n                    request.updateFailedState { it - targetId }\n                    onMemoryChanged()\n                }\n''',
'save note refresh'
)

replace_once(
'''            if (entries.isNotEmpty()) repository.insertEntries(entries)\n''',
'''            if (entries.isNotEmpty()) {\n                repository.insertEntries(entries)\n                onMemoryChanged()\n            }\n''',
'save list refresh'
)

replace_once(
'''                if (existing == null) repository.insertEntries(listOf(entry)) else repository.updateEntry(entry)\n                if (!request.isLatest()) return@withLock false\n                request.updateFailedState { it - targetId }\n                true\n''',
'''                if (existing == null) repository.insertEntries(listOf(entry)) else repository.updateEntry(entry)\n                if (!request.isLatest()) return@withLock false\n                request.updateFailedState { it - targetId }\n                onMemoryChanged()\n                true\n''',
'save link refresh'
)

replace_once(
'''        launchOperation(ERROR_CREATE_CATEGORY) {\n            repository.createCategory(name.trim(), colorKey, iconKey, clock.nowMillis())\n        }\n''',
'''        launchOperation(ERROR_CREATE_CATEGORY) {\n            repository.createCategory(name.trim(), colorKey, iconKey, clock.nowMillis())\n            onMemoryChanged()\n        }\n''',
'create category refresh'
)

replace_once(
'''        launchOperation(ERROR_UPDATE_CATEGORY) {\n            repository.updateCategory(id, name.trim(), colorKey, iconKey, clock.nowMillis())\n        }\n''',
'''        launchOperation(ERROR_UPDATE_CATEGORY) {\n            repository.updateCategory(id, name.trim(), colorKey, iconKey, clock.nowMillis())\n            onMemoryChanged()\n        }\n''',
'update category refresh'
)

replace_once(
'''        launchOperation(ERROR_DELETE_CATEGORY) {\n            repository.deleteCustomCategory(id, moveToId, clock.nowMillis())\n            updateLocal {\n''',
'''        launchOperation(ERROR_DELETE_CATEGORY) {\n            repository.deleteCustomCategory(id, moveToId, clock.nowMillis())\n            onMemoryChanged()\n            updateLocal {\n''',
'delete category refresh'
)

replace_once(
'''                val now = clock.nowMillis()\n                repository.setCompleted(entryId, completedAt = now, updatedAt = now)\n''',
'''                val now = clock.nowMillis()\n                repository.setCompleted(entryId, completedAt = now, updatedAt = now)\n                onMemoryChanged()\n''',
'complete refresh'
)

replace_once(
'''                repository.setCompleted(entryId, completedAt = null, updatedAt = clock.nowMillis())\n''',
'''                repository.setCompleted(entryId, completedAt = null, updatedAt = clock.nowMillis())\n                onMemoryChanged()\n''',
'restore refresh'
)

replace_once(
'''                repository.deleteEntry(entryId)\n                if (!request.isLatest()) return@withLock\n                request.updateFailedState { it - entryId }\n''',
'''                repository.deleteEntry(entryId)\n                if (!request.isLatest()) return@withLock\n                request.updateFailedState { it - entryId }\n                onMemoryChanged()\n''',
'delete refresh'
)

replace_once(
'''                        if (request.isLatest()) request.updateFailedState { it - request.entryId }\n                    }\n\n                    is LinkPreviewResult.Failure -> {\n                        repository.updateEntry(current.withoutPreview())\n                        if (request.isLatest()) request.markPreviewFailed()\n                    }\n''',
'''                        if (request.isLatest()) {\n                            request.updateFailedState { it - request.entryId }\n                            onMemoryChanged()\n                        }\n                    }\n\n                    is LinkPreviewResult.Failure -> {\n                        repository.updateEntry(current.withoutPreview())\n                        if (request.isLatest()) {\n                            request.markPreviewFailed()\n                            onMemoryChanged()\n                        }\n                    }\n''',
'preview refresh'
)

replace_once(
'''class MemoryViewModelFactory(\n    private val repository: MemoryRepository,\n    private val linkPreviewResolver: LinkPreviewResolver,\n    private val clock: MemoryClock = SystemMemoryClock\n) : ViewModelProvider.Factory {\n''',
'''class MemoryViewModelFactory(\n    private val repository: MemoryRepository,\n    private val linkPreviewResolver: LinkPreviewResolver,\n    private val clock: MemoryClock = SystemMemoryClock,\n    private val onMemoryChanged: () -> Unit = {}\n) : ViewModelProvider.Factory {\n''',
'factory callback'
)

replace_once(
'''        return MemoryViewModel(repository, linkPreviewResolver, clock) as T\n''',
'''        return MemoryViewModel(\n            repository = repository,\n            linkPreviewResolver = linkPreviewResolver,\n            clock = clock,\n            onMemoryChanged = onMemoryChanged\n        ) as T\n''',
'factory construction'
)

path.write_text(text, encoding='utf-8')
