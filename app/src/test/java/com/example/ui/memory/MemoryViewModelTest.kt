package com.example.ui.memory

import androidx.lifecycle.ViewModel
import com.example.data.LinkPreview
import com.example.data.LinkPreviewResolver
import com.example.data.LinkPreviewResult
import com.example.data.MemoryArchivePolicy
import com.example.data.MemoryBucket
import com.example.data.model.MemoryCategoryEntity
import com.example.data.model.MemoryClock
import com.example.data.model.MemoryDefaults
import com.example.data.model.MemoryEntryEntity
import com.example.data.model.MemoryEntryKind
import com.example.data.repository.MemoryRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MemoryViewModelTest {
    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(scheduler)
    private lateinit var repository: FakeMemoryRepository
    private lateinit var resolver: FakeLinkPreviewResolver
    private lateinit var clock: MutableMemoryClock

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeMemoryRepository()
        resolver = FakeLinkPreviewResolver()
        clock = MutableMemoryClock(START)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init seeds defaults once and publishes the repository categories`() = runTest(scheduler) {
        val viewModel = viewModel()

        runCurrent()

        assertEquals(1, repository.defaultSeedCount)
        assertEquals(MemoryDefaults.orderedIds, viewModel.uiState.value.categories.map { it.id })
    }

    @Test
    fun `list input creates one entry per nonblank line preserving duplicates and order`() = runTest(scheduler) {
        val viewModel = viewModel()
        runCurrent()

        viewModel.saveList(MemoryDefaults.SERIES_ID, "  Dark  \n\nSeverance\nDark")
        runCurrent()

        assertEquals(listOf("Dark", "Severance", "Dark"), repository.inserted.map { it.title })
        assertEquals(listOf(START, START, START), repository.inserted.map { it.createdAt })
    }

    @Test
    fun `current groups open before grace and archived sorts newest completion first at exact boundary`() = runTest(scheduler) {
        repository.seedEntries(
            entry("grace-newer", updatedAt = 900L, completedAt = START - 10L),
            entry("open-older", updatedAt = 100L),
            entry("open-newer", updatedAt = 200L),
            entry("archived-old", updatedAt = 800L, completedAt = START - MemoryArchivePolicy.GRACE_PERIOD_MS - 5L),
            entry("archived-new", updatedAt = 700L, completedAt = START - MemoryArchivePolicy.GRACE_PERIOD_MS)
        )
        val viewModel = viewModel()
        runCurrent()

        assertEquals(listOf("open-newer", "open-older", "grace-newer"), viewModel.uiState.value.visibleEntries.map { it.entity.id })
        assertEquals(
            listOf(MemoryBucket.CURRENT_OPEN, MemoryBucket.CURRENT_OPEN, MemoryBucket.CURRENT_GRACE),
            viewModel.uiState.value.visibleEntries.map { it.bucket }
        )

        viewModel.selectTab(MemoryTab.ARCHIVED)
        runCurrent()

        assertEquals(listOf("archived-new", "archived-old"), viewModel.uiState.value.visibleEntries.map { it.entity.id })
    }

    @Test
    fun `completion remains current until manual refresh at scheduled 24 hour boundary`() = runTest(scheduler) {
        repository.seedEntries(entry("entry-1"))
        val viewModel = viewModel()
        runCurrent()

        viewModel.complete("entry-1")
        runCurrent()
        assertEquals(MemoryBucket.CURRENT_GRACE, viewModel.uiState.value.visibleEntries.single().bucket)
        assertEquals(START + MemoryArchivePolicy.GRACE_PERIOD_MS, viewModel.uiState.value.nextExpiryAt)

        clock.now = START + MemoryArchivePolicy.GRACE_PERIOD_MS
        viewModel.refreshTime()
        viewModel.selectTab(MemoryTab.ARCHIVED)
        runCurrent()

        assertEquals("entry-1", viewModel.uiState.value.visibleEntries.single().entity.id)
    }

    @Test
    fun `nearest expiry reschedules when entries change and refreshes only at the replacement boundary`() = runTest(scheduler) {
        val firstExpiry = START + MemoryArchivePolicy.GRACE_PERIOD_MS
        val secondExpiry = firstExpiry + 1_000L
        repository.seedEntries(
            entry("first", completedAt = START),
            entry("second", completedAt = START + 1_000L)
        )
        val viewModel = viewModel()
        runCurrent()
        assertEquals(firstExpiry, viewModel.uiState.value.nextExpiryAt)

        repository.seedEntries(entry("second", completedAt = START + 1_000L))
        runCurrent()
        assertEquals(secondExpiry, viewModel.uiState.value.nextExpiryAt)

        clock.now = firstExpiry
        scheduler.advanceTimeBy(MemoryArchivePolicy.GRACE_PERIOD_MS)
        runCurrent()
        assertEquals(MemoryBucket.CURRENT_GRACE, viewModel.uiState.value.visibleEntries.single().bucket)

        clock.now = secondExpiry
        scheduler.advanceTimeBy(1_000L)
        runCurrent()
        viewModel.selectTab(MemoryTab.ARCHIVED)
        runCurrent()
        assertEquals("second", viewModel.uiState.value.visibleEntries.single().entity.id)
    }

    @Test
    fun `category and query filters match searchable entry content`() = runTest(scheduler) {
        repository.seedEntries(
            entry("film", categoryId = MemoryDefaults.FILMS_ID, title = "Arrival", body = "Language"),
            entry("series", categoryId = MemoryDefaults.SERIES_ID, title = "Dark", body = "Winden")
        )
        val viewModel = viewModel()
        runCurrent()

        viewModel.setCategoryFilter(MemoryDefaults.FILMS_ID)
        viewModel.setQuery("language")
        runCurrent()

        assertEquals(listOf("film"), viewModel.uiState.value.visibleEntries.map { it.entity.id })
        assertEquals(MemoryDefaults.FILMS_ID, viewModel.uiState.value.selectedCategoryId)
        assertEquals("language", viewModel.uiState.value.query)
    }

    @Test
    fun `editor state opens an existing mode and closes without changing filters`() = runTest(scheduler) {
        val viewModel = viewModel()
        runCurrent()
        viewModel.setQuery("dark")

        viewModel.openEditor(MemoryEditorMode.NOTE, "entry-1")
        runCurrent()
        assertEquals(MemoryEditorMode.NOTE, viewModel.uiState.value.editorMode)
        assertEquals("entry-1", viewModel.uiState.value.editorEntryId)

        viewModel.closeEditor()
        runCurrent()
        assertNull(viewModel.uiState.value.editorMode)
        assertNull(viewModel.uiState.value.editorEntryId)
        assertEquals("dark", viewModel.uiState.value.query)
    }

    @Test
    fun `editing a note preserves identity creation and completion fields`() = runTest(scheduler) {
        repository.seedEntries(entry("note", createdAt = 17L, updatedAt = 18L, completedAt = 19L))
        val viewModel = viewModel()
        runCurrent()

        viewModel.saveNote("note", MemoryDefaults.IDEAS_ID, "New title", "New body")
        runCurrent()

        val saved = repository.requireEntry("note")
        assertEquals("note", saved.id)
        assertEquals(17L, saved.createdAt)
        assertEquals(19L, saved.completedAt)
        assertEquals(START, saved.updatedAt)
        assertEquals(MemoryDefaults.IDEAS_ID, saved.categoryId)
        assertEquals("New title", saved.title)
        assertEquals("New body", saved.body)
    }

    @Test
    fun `editing link clears stale preview before fetch then preserves identity on success`() = runTest(scheduler) {
        repository.seedEntries(
            entry(
                id = "link",
                kind = MemoryEntryKind.LINK,
                title = "Old",
                url = "https://old.example/",
                previewTitle = "Old preview",
                previewFetchedAt = 7L,
                createdAt = 11L,
                completedAt = 12L
            )
        )
        val pending = resolver.enqueuePending()
        val viewModel = viewModel()
        runCurrent()

        viewModel.saveLink("link", MemoryDefaults.PLACES_ID, "new.example", "Trip")
        runCurrent()

        val beforePreview = repository.requireEntry("link")
        assertEquals("https://new.example/", beforePreview.url)
        assertNull(beforePreview.previewTitle)
        assertNull(beforePreview.previewFetchedAt)
        assertEquals(11L, beforePreview.createdAt)
        assertEquals(12L, beforePreview.completedAt)

        pending.complete(success("https://new.example", title = "New preview"))
        runCurrent()

        val afterPreview = repository.requireEntry("link")
        assertEquals("link", afterPreview.id)
        assertEquals(11L, afterPreview.createdAt)
        assertEquals(12L, afterPreview.completedAt)
        assertEquals("New preview", afterPreview.previewTitle)
        assertEquals(START, afterPreview.previewFetchedAt)
    }

    @Test
    fun `link row is committed before preview failure and retry updates the same row`() = runTest(scheduler) {
        val first = resolver.enqueuePending()
        val viewModel = viewModel()
        runCurrent()

        viewModel.saveLink(null, MemoryDefaults.FILMS_ID, "https://example.invalid", "Watch")
        runCurrent()

        val saved = repository.entriesSnapshot.single()
        assertEquals("https://example.invalid/", saved.url)
        assertEquals(listOf("https://example.invalid/"), resolver.requestedUrls)

        first.complete(LinkPreviewResult.Failure("https://example.invalid/"))
        runCurrent()
        assertEquals(setOf(saved.id), viewModel.uiState.value.failedPreviewIds)
        assertNotNull(repository.getEntry(saved.id))

        resolver.enqueue(success("https://example.invalid/", title = "Recovered"))
        viewModel.retryPreview(saved.id)
        runCurrent()

        assertEquals("Recovered", repository.requireEntry(saved.id).previewTitle)
        assertFalse(saved.id in viewModel.uiState.value.failedPreviewIds)
    }

    @Test
    fun `late preview response cannot overwrite a newer edited url`() = runTest(scheduler) {
        repository.seedEntries(entry("link", kind = MemoryEntryKind.LINK, url = "https://initial.example/"))
        val oldRequest = resolver.enqueuePending()
        val newRequest = resolver.enqueuePending()
        val viewModel = viewModel()
        runCurrent()

        viewModel.saveLink("link", MemoryDefaults.FILMS_ID, "old.example", null)
        runCurrent()
        viewModel.saveLink("link", MemoryDefaults.FILMS_ID, "new.example", null)
        runCurrent()

        oldRequest.complete(success("https://old.example", title = "Stale"))
        runCurrent()
        assertEquals("https://new.example/", repository.requireEntry("link").url)
        assertNull(repository.requireEntry("link").previewTitle)

        newRequest.complete(success("https://new.example", title = "Current"))
        runCurrent()
        assertEquals("Current", repository.requireEntry("link").previewTitle)
    }

    @Test
    fun `completion restore and permanent delete confirmation mutate only the requested entry`() = runTest(scheduler) {
        repository.seedEntries(entry("first"), entry("second"))
        val viewModel = viewModel()
        runCurrent()

        viewModel.complete("first")
        runCurrent()
        assertEquals(START, repository.requireEntry("first").completedAt)
        viewModel.restore("first")
        runCurrent()
        assertNull(repository.requireEntry("first").completedAt)

        viewModel.requestPermanentDelete("first")
        runCurrent()
        assertEquals("first", viewModel.uiState.value.pendingDeleteEntryId)
        assertNotNull(repository.getEntry("first"))
        viewModel.dismissPermanentDelete()
        runCurrent()
        assertNull(viewModel.uiState.value.pendingDeleteEntryId)
        assertNotNull(repository.getEntry("first"))

        viewModel.requestPermanentDelete("first")
        viewModel.confirmPermanentDelete()
        runCurrent()
        assertNull(repository.getEntry("first"))
        assertNotNull(repository.getEntry("second"))
        assertNull(viewModel.uiState.value.pendingDeleteEntryId)
    }

    @Test
    fun `category create update and delete actions keep repository state observable`() = runTest(scheduler) {
        val viewModel = viewModel()
        runCurrent()

        viewModel.createCategory("Trips", "blue", "place")
        runCurrent()
        val custom = viewModel.uiState.value.categories.single { it.customName == "Trips" }
        viewModel.updateCategory(custom.id, "Journeys", "teal", "map")
        runCurrent()
        assertEquals("Journeys", viewModel.uiState.value.categories.single { it.id == custom.id }.customName)

        repository.seedEntries(entry("custom-entry", categoryId = custom.id))
        runCurrent()
        viewModel.deleteCategory(custom.id, MemoryDefaults.OTHER_ID)
        runCurrent()
        assertNull(viewModel.uiState.value.categories.find { it.id == custom.id })
        assertEquals(MemoryDefaults.OTHER_ID, repository.requireEntry("custom-entry").categoryId)
    }

    @Test
    fun `failed write exposes recoverable error and the next successful action clears it`() = runTest(scheduler) {
        val viewModel = viewModel()
        runCurrent()
        repository.failNextWrite = true

        viewModel.saveNote(null, MemoryDefaults.IDEAS_ID, "First", null)
        runCurrent()
        assertNotNull(viewModel.uiState.value.errorKey)

        viewModel.saveNote(null, MemoryDefaults.IDEAS_ID, "Second", null)
        runCurrent()
        assertNull(viewModel.uiState.value.errorKey)
        assertEquals(listOf("Second"), repository.entriesSnapshot.map { it.title })
    }

    @Test
    fun `factory rejects model classes other than memory view model`() {
        val factory = MemoryViewModelFactory(repository, resolver, clock)

        assertThrows(IllegalArgumentException::class.java) {
            factory.create(OtherViewModel::class.java)
        }
    }

    private fun viewModel() = MemoryViewModel(repository, resolver, clock, dispatcher)

    private fun entry(
        id: String,
        categoryId: String = MemoryDefaults.FILMS_ID,
        kind: MemoryEntryKind = MemoryEntryKind.NOTE,
        title: String = id,
        body: String? = null,
        url: String? = null,
        previewTitle: String? = null,
        previewFetchedAt: Long? = null,
        createdAt: Long = 1L,
        updatedAt: Long = 1L,
        completedAt: Long? = null
    ) = MemoryEntryEntity(
        id = id,
        categoryId = categoryId,
        kind = kind,
        title = title,
        body = body,
        url = url,
        previewTitle = previewTitle,
        previewFetchedAt = previewFetchedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        completedAt = completedAt
    )

    private fun success(url: String, title: String) = LinkPreviewResult.Success(
        LinkPreview(url, title, "Description", "https://images.example/cover.jpg", "Example")
    )

    private class OtherViewModel : ViewModel()

    companion object {
        private const val START = 10_000_000L
    }
}

private class MutableMemoryClock(var now: Long) : MemoryClock {
    override fun nowMillis() = now
}

private class FakeLinkPreviewResolver : LinkPreviewResolver {
    private val responses = ArrayDeque<CompletableDeferred<LinkPreviewResult>>()
    val requestedUrls = mutableListOf<String>()

    fun enqueue(result: LinkPreviewResult) {
        responses += CompletableDeferred(result)
    }

    fun enqueuePending(): CompletableDeferred<LinkPreviewResult> =
        CompletableDeferred<LinkPreviewResult>().also { responses += it }

    override suspend fun resolve(rawUrl: String): LinkPreviewResult {
        requestedUrls += rawUrl
        return responses.removeFirst().await()
    }
}

private class FakeMemoryRepository : MemoryRepository {
    private val categoryState = MutableStateFlow<List<MemoryCategoryEntity>>(emptyList())
    private val entryState = MutableStateFlow<List<MemoryEntryEntity>>(emptyList())
    override val categories: Flow<List<MemoryCategoryEntity>> = categoryState
    override val entries: Flow<List<MemoryEntryEntity>> = entryState
    val inserted = mutableListOf<MemoryEntryEntity>()
    var defaultSeedCount = 0
    var failNextWrite = false

    val entriesSnapshot: List<MemoryEntryEntity> get() = entryState.value

    override suspend fun ensureDefaultCategories(nowMillis: Long) {
        defaultSeedCount++
        if (categoryState.value.isEmpty()) {
            categoryState.value = MemoryDefaults.orderedIds.mapIndexed { index, id ->
                MemoryCategoryEntity(
                    id = id,
                    systemKey = id,
                    colorKey = "color-$index",
                    iconKey = "icon-$index",
                    sortOrder = index,
                    createdAt = nowMillis,
                    updatedAt = nowMillis
                )
            }
        }
    }

    override suspend fun createCategory(name: String, colorKey: String, iconKey: String, nowMillis: Long): String {
        failIfRequested()
        val id = "custom-${categoryState.value.count { it.customName != null } + 1}"
        categoryState.value += MemoryCategoryEntity(
            id = id,
            customName = name,
            colorKey = colorKey,
            iconKey = iconKey,
            sortOrder = Int.MAX_VALUE,
            createdAt = nowMillis,
            updatedAt = nowMillis
        )
        return id
    }

    override suspend fun updateCategory(id: String, name: String, colorKey: String, iconKey: String, nowMillis: Long) {
        failIfRequested()
        categoryState.value = categoryState.value.map {
            if (it.id == id) it.copy(customName = name, colorKey = colorKey, iconKey = iconKey, updatedAt = nowMillis) else it
        }
    }

    override suspend fun deleteCustomCategory(id: String, moveToId: String, nowMillis: Long) {
        failIfRequested()
        entryState.value = entryState.value.map {
            if (it.categoryId == id) it.copy(categoryId = moveToId, updatedAt = nowMillis) else it
        }
        categoryState.value = categoryState.value.filterNot { it.id == id }
    }

    override suspend fun insertEntries(entries: List<MemoryEntryEntity>) {
        failIfRequested()
        inserted += entries
        entryState.value += entries
    }

    override suspend fun getEntry(id: String): MemoryEntryEntity? = entryState.value.find { it.id == id }

    override suspend fun updateEntry(entry: MemoryEntryEntity) {
        failIfRequested()
        entryState.value = entryState.value.map { if (it.id == entry.id) entry else it }
    }

    override suspend fun setCompleted(id: String, completedAt: Long?, updatedAt: Long) {
        failIfRequested()
        entryState.value = entryState.value.map {
            if (it.id == id) it.copy(completedAt = completedAt, updatedAt = updatedAt) else it
        }
    }

    override suspend fun deleteEntry(id: String) {
        failIfRequested()
        entryState.value = entryState.value.filterNot { it.id == id }
    }

    fun seedEntries(vararg entries: MemoryEntryEntity) {
        entryState.value = entries.toList()
    }

    fun requireEntry(id: String): MemoryEntryEntity = checkNotNull(entryState.value.find { it.id == id })

    private fun failIfRequested() {
        if (failNextWrite) {
            failNextWrite = false
            error("planned write failure")
        }
    }
}
