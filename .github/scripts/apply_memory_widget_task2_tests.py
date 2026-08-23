from pathlib import Path

path = Path('app/src/test/java/com/example/ui/memory/MemoryViewModelTest.kt')
text = path.read_text(encoding='utf-8')

anchor = '''    @Test\n    fun `factory rejects model classes other than memory view model`() {\n'''
insert = '''    @Test\n    fun `successful complete invokes Memory widget refresh callback`() = runMemoryTest {\n        repository.seedEntries(entry("entry-1"))\n        var refreshes = 0\n        val viewModel = viewModel(onMemoryChanged = { refreshes++ })\n        runCurrent()\n\n        viewModel.complete("entry-1")\n        runCurrent()\n\n        assertEquals(1, refreshes)\n    }\n\n    @Test\n    fun `widget link open clears filters and opens link editor`() = runMemoryTest {\n        repository.seedEntries(\n            entry(\n                id = "link-1",\n                kind = MemoryEntryKind.LINK,\n                url = "https://example.com/"\n            )\n        )\n        val viewModel = viewModel()\n        runCurrent()\n        viewModel.setQuery("hidden")\n        viewModel.setCategoryFilter(MemoryDefaults.FILMS_ID)\n\n        viewModel.openEntryFromWidget("link-1")\n        runCurrent()\n\n        assertEquals(MemoryTab.CURRENT, viewModel.uiState.value.selectedTab)\n        assertNull(viewModel.uiState.value.selectedCategoryId)\n        assertEquals("", viewModel.uiState.value.query)\n        assertEquals(MemoryEditorMode.LINK, viewModel.uiState.value.editorMode)\n        assertEquals("link-1", viewModel.uiState.value.editorEntryId)\n    }\n\n'''
if anchor not in text:
    raise SystemExit('test insertion anchor not found')
text = text.replace(anchor, insert + anchor, 1)

old_helper = '''    private fun viewModel() = MemoryViewModel(repository, resolver, clock, dispatcher)\n        .also { viewModelStore.put(UUID.randomUUID().toString(), it) }\n'''
new_helper = '''    private fun viewModel(onMemoryChanged: () -> Unit = {}) =\n        MemoryViewModel(repository, resolver, clock, dispatcher, onMemoryChanged)\n            .also { viewModelStore.put(UUID.randomUUID().toString(), it) }\n'''
if old_helper not in text:
    raise SystemExit('viewModel helper anchor not found')
text = text.replace(old_helper, new_helper, 1)
path.write_text(text, encoding='utf-8')
