package com.example.ui.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import com.example.data.LinkPreview
import com.example.data.LinkPreviewResolver
import com.example.data.LinkPreviewResult
import com.example.data.MemoryBucket
import com.example.data.model.MemoryCategoryEntity
import com.example.data.model.MemoryChecklistCodec
import com.example.data.model.MemoryChecklistItem
import com.example.data.model.MemoryClock
import com.example.data.model.MemoryDefaults
import com.example.data.model.MemoryEntryEntity
import com.example.data.model.MemoryEntryKind
import com.example.data.repository.MemoryRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
class MemoryViewModelTest {
    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(scheduler)
    private lateinit var repository: FakeMemoryRepository
    private lateinit var resolver: FakeLinkPreviewResolver
    private lateinit var clock: MutableMemoryClock
    private lateinit var viewModelStore: ViewModelStore

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeMemoryRepository()
        resolver = FakeLinkPreviewResolver()
        clock = MutableMemoryClock(START)
        viewModelStore = ViewModelStore()
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun `init seeds defaults once and publishes the repository categories`() = runMemoryTest {
        val viewModel = viewModel()

        runCurrent()

        assertEquals(1, repository.defaultSeedCount)
        assertEquals(MemoryDefaults.orderedIds, viewModel.uiState.value.categories.map { it.id })
    }

    @Test
    fun `list input creates one checklist entry preserving item state and order`() = runMemoryTest {
        val viewModel = viewModel()
        runCurrent()

        val items = listOf(
            MemoryChecklistItem(id = "dark", text = "Dark", completed = false),
            MemoryChecklistItem(id = "severance", text = "Severance", completed = true)
        )
        viewModel.saveList(
            entryId = null,
            categoryId = MemoryDefaults.FILMS_ID,
            title = "Watchlist",
            items = items
        )
        runCurrent()

        val saved = repository.inserted.single()
        assertEquals(MemoryEntryKind.LIST, saved.kind)
        assertEquals("Watchlist", saved.title)
        assertEquals(items, MemoryChecklistCodec.decode(saved.body))
        assertEquals(START, saved.createdAt)
    }

    @Test
    fun `current contains open entries and archived sorts newest completion first`() = runMemoryTest {
        repository.seedEntries(
            entry("open-older", updatedAt = 100L),
            entry("open-newer", updatedAt = 200L),
            entry("archived-old", updatedAt = 800L, completedAt = START - 5L),
            entry("archived-new", updatedAt = 700L, completedAt = START)
        )
        val viewModel = viewModel()
        runCurrent()

        assertEquals(listOf("open-newer", "open-older"), viewModel.uiState.value.visibleEntries.map { it.entity.id })
        assertEquals(
            listOf(MemoryBucket.CURRENT_OPEN, MemoryBucket.CURRENT_OPEN),
            viewModel.uiState.value.visibleEntries.map { it.bucket }
        )

        viewModel.selectTab(MemoryTab.ARCHIVED)
        runCurrent()

        assertEquals(listOf("archived-new", "archived-old"), viewModel.uiState.value.visibleEntries.map { it.entity.id })
    }

    @Test
    fun `completion moves entry directly to archived`() = runMemoryTest {
        repository.seedEntries(entry("entry-1"))
        val viewModel = viewModel()
        runCurrent()

        viewModel.complete("entry-1")
        runCurrent()
        assertTrue(viewModel.uiState.value.visibleEntries.isEmpty())

        viewModel.selectTab(MemoryTab.ARCHIVED)
        runCurrent()

        assertEquals("entry-1", viewModel.uiState.value.visibleEntries.single().entity.id)
        assertEquals(MemoryBucket.ARCHIVED, viewModel.uiState.value.visibleEntries.single().bucket)
    }

    @Test
    fun `category and query filters match searchable entry content`() = runMemoryTest {
        repository.seedEntries(
            entry("film", categoryId = MemoryDefaults.FILMS_ID, title = "Arrival", body = "Language"),
            entry("series", categoryId = MemoryDefaults.IDEAS_ID, title = "Dark", body = "Winden")
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
    fun `editor state opens an existing mode and closes without changing filters`() = runMemoryTest {
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
    fun `editing a note preserves identity creation and completion fields`() = runMemoryTest {
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
    fun `editing link clears stale preview before fetch then preserves identity on success`() = runMemoryTest {
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
    fun `link row is committed before preview failure and retry updates the same row`() = runMemoryTest {
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
    fun `late preview response cannot overwrite a newer edited url`() = runMemoryTest {
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
    fun `concurrent preview failures atomically retain every failed entry id`() = runMemoryTest {
        val entryCount = 32
        val ids = List(entryCount) { "link-$it" }
        repository.seedEntries(*ids.map { entry(it, kind = MemoryEntryKind.LINK, url = "https://$it.example/") }.toTypedArray())
        val started = AtomicInteger()
        val allStarted = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val concurrentResolver = LinkPreviewResolver { url ->
            if (started.incrementAndGet() == entryCount) allStarted.complete(Unit)
            release.await()
            LinkPreviewResult.Failure(url)
        }
        val executor = Executors.newFixedThreadPool(entryCount)
        val concurrentDispatcher = executor.asCoroutineDispatcher()

        try {
            val viewModel = MemoryViewModel(repository, concurrentResolver, clock, concurrentDispatcher)
                .also { viewModelStore.put("concurrent", it) }
            runCurrent()
            ids.forEach(viewModel::retryPreview)
            allStarted.await()

            release.complete(Unit)
            val queuedWorkDrained = CountDownLatch(entryCount)
            repeat(entryCount) { executor.execute(queuedWorkDrained::countDown) }
            queuedWorkDrained.await()
            runCurrent()

            assertEquals(ids.toSet(), viewModel.uiState.value.failedPreviewIds)
        } finally {
            concurrentDispatcher.close()
        }
    }

    @Test
    fun `older blocked same url save cannot commit row or preview after newer invocation`() = runMemoryTest {
        repository.seedEntries(entry("link", kind = MemoryEntryKind.LINK, url = "https://same.example/"))
        val releaseOlderWrite = CompletableDeferred<Unit>()
        repository.beforeUpdateEntry = { candidate ->
            if (candidate.body == "First" && candidate.previewFetchedAt == null) releaseOlderWrite.await()
        }
        resolver.enqueue(success("https://same.example/", title = "Latest"))
        resolver.enqueue(success("https://same.example/", title = "Stale"))
        val viewModel = viewModel()
        runCurrent()

        viewModel.saveLink("link", MemoryDefaults.FILMS_ID, "same.example", "First")
        runCurrent()
        viewModel.saveLink("link", MemoryDefaults.FILMS_ID, "same.example", "Second")
        runCurrent()

        releaseOlderWrite.complete(Unit)
        runCurrent()

        assertEquals("Second", repository.requireEntry("link").body)
        assertEquals("Latest", repository.requireEntry("link").previewTitle)
        assertFalse("link" in viewModel.uiState.value.failedPreviewIds)
        assertEquals(
            listOf("Second"),
            repository.updatedEntries.filter { it.previewFetchedAt == null }.map { it.body }
        )
    }

    @Test
    fun `same url preview responses only let the latest invocation publish metadata`() = runMemoryTest {
        repository.seedEntries(entry("link", kind = MemoryEntryKind.LINK, url = "https://same.example/"))
        val older = resolver.enqueuePending()
        val latest = resolver.enqueuePending()
        val viewModel = viewModel()
        runCurrent()

        viewModel.saveLink("link", MemoryDefaults.FILMS_ID, "same.example", "First")
        runCurrent()
        viewModel.saveLink("link", MemoryDefaults.FILMS_ID, "same.example", "Second")
        runCurrent()

        latest.complete(success("https://same.example/", title = "Latest"))
        runCurrent()
        older.complete(success("https://same.example/", title = "Stale"))
        runCurrent()

        assertEquals("Second", repository.requireEntry("link").body)
        assertEquals("Latest", repository.requireEntry("link").previewTitle)
    }

    @Test
    fun `A to B to A edits reject the first A preview response`() = runMemoryTest {
        repository.seedEntries(entry("link", kind = MemoryEntryKind.LINK, url = "https://initial.example/"))
        val firstA = resolver.enqueuePending()
        val middleB = resolver.enqueuePending()
        val latestA = resolver.enqueuePending()
        val viewModel = viewModel()
        runCurrent()

        viewModel.saveLink("link", MemoryDefaults.FILMS_ID, "a.example", null)
        runCurrent()
        viewModel.saveLink("link", MemoryDefaults.FILMS_ID, "b.example", null)
        runCurrent()
        viewModel.saveLink("link", MemoryDefaults.FILMS_ID, "a.example", null)
        runCurrent()

        middleB.complete(success("https://b.example/", title = "Middle"))
        latestA.complete(success("https://a.example/", title = "Latest A"))
        runCurrent()
        firstA.complete(success("https://a.example/", title = "Stale A"))
        runCurrent()

        assertEquals("https://a.example/", repository.requireEntry("link").url)
        assertEquals("Latest A", repository.requireEntry("link").previewTitle)
    }

    @Test
    fun `overlapping retries cannot let older failure replace latest success`() = runMemoryTest {
        repository.seedEntries(entry("link", kind = MemoryEntryKind.LINK, url = "https://retry.example/"))
        resolver.enqueue(LinkPreviewResult.Failure("https://retry.example/"))
        val viewModel = viewModel()
        runCurrent()
        viewModel.retryPreview("link")
        runCurrent()
        assertEquals(setOf("link"), viewModel.uiState.value.failedPreviewIds)

        val older = resolver.enqueuePending()
        val latest = resolver.enqueuePending()
        viewModel.retryPreview("link")
        runCurrent()
        viewModel.retryPreview("link")
        runCurrent()

        latest.complete(success("https://retry.example/", title = "Latest"))
        runCurrent()
        older.complete(LinkPreviewResult.Failure("https://retry.example/"))
        runCurrent()

        assertEquals("Latest", repository.requireEntry("link").previewTitle)
        assertFalse("link" in viewModel.uiState.value.failedPreviewIds)
    }

    @Test
    fun `latest retry failure clears metadata committed by superseded successful retry`() = runMemoryTest {
        repository.seedEntries(entry("link", kind = MemoryEntryKind.LINK, url = "https://retry.example/"))
        val olderCommitStarted = CompletableDeferred<Unit>()
        val releaseOlderCommit = CompletableDeferred<Unit>()
        repository.beforeUpdateEntry = { candidate ->
            if (candidate.previewTitle == "Superseded") {
                olderCommitStarted.complete(Unit)
                withContext(NonCancellable) { releaseOlderCommit.await() }
            }
        }
        resolver.enqueue(success("https://retry.example/", title = "Superseded"))
        resolver.enqueue(LinkPreviewResult.Failure("https://retry.example/"))
        val viewModel = viewModel()
        runCurrent()

        viewModel.retryPreview("link")
        runCurrent()
        assertTrue(olderCommitStarted.isCompleted)

        viewModel.retryPreview("link")
        runCurrent()
        releaseOlderCommit.complete(Unit)
        runCurrent()

        val finalEntry = repository.requireEntry("link")
        assertNull(finalEntry.previewTitle)
        assertNull(finalEntry.previewDescription)
        assertNull(finalEntry.previewImageUrl)
        assertNull(finalEntry.previewSiteName)
        assertNull(finalEntry.previewFetchedAt)
        assertEquals(setOf("link"), viewModel.uiState.value.failedPreviewIds)
    }

    @Test
    fun `preview reload failure keeps saved link retryable without save error`() = runMemoryTest {
        resolver.enqueue(success("https://saved.example/", title = "Preview"))
        repository.failNextEntryRead = true
        val viewModel = viewModel()
        runCurrent()

        viewModel.saveLink(null, MemoryDefaults.FILMS_ID, "saved.example", null)
        runCurrent()

        val saved = repository.entriesSnapshot.single()
        assertEquals("https://saved.example/", saved.url)
        assertNull(saved.previewTitle)
        assertEquals(setOf(saved.id), viewModel.uiState.value.failedPreviewIds)
        assertNull(viewModel.uiState.value.errorKey)
    }

    @Test
    fun `preview metadata write failure keeps saved link retryable without save error`() = runMemoryTest {
        resolver.enqueue(success("https://saved.example/", title = "Preview"))
        repository.failNextPreviewUpdate = true
        val viewModel = viewModel()
        runCurrent()

        viewModel.saveLink(null, MemoryDefaults.FILMS_ID, "saved.example", null)
        runCurrent()

        val saved = repository.entriesSnapshot.single()
        assertEquals("https://saved.example/", saved.url)
        assertNull(saved.previewTitle)
        assertEquals(setOf(saved.id), viewModel.uiState.value.failedPreviewIds)
        assertNull(viewModel.uiState.value.errorKey)
    }

    @Test
    fun `saving failed link as note clears failed preview state`() = runMemoryTest {
        repository.seedEntries(entry("link", kind = MemoryEntryKind.LINK, url = "https://failed.example/"))
        resolver.enqueue(LinkPreviewResult.Failure("https://failed.example/"))
        val viewModel = viewModel()
        runCurrent()
        viewModel.retryPreview("link")
        runCurrent()

        viewModel.saveNote("link", MemoryDefaults.IDEAS_ID, "Now a note", null)
        runCurrent()

        assertEquals(MemoryEntryKind.NOTE, repository.requireEntry("link").kind)
        assertFalse("link" in viewModel.uiState.value.failedPreviewIds)
    }

    @Test
    fun `permanent delete clears failed preview state`() = runMemoryTest {
        repository.seedEntries(entry("link", kind = MemoryEntryKind.LINK, url = "https://failed.example/"))
        resolver.enqueue(LinkPreviewResult.Failure("https://failed.example/"))
        val viewModel = viewModel()
        runCurrent()
        viewModel.retryPreview("link")
        runCurrent()

        viewModel.requestPermanentDelete("link")
        viewModel.confirmPermanentDelete()
        runCurrent()

        assertNull(repository.getEntry("link"))
        assertFalse("link" in viewModel.uiState.value.failedPreviewIds)
    }

    @Test
    fun `selection deletes every marked note together and keeps unselected notes`() = runMemoryTest {
        repository.seedEntries(entry("first"), entry("second"), entry("third"))
        val viewModel = viewModel()
        runCurrent()

        viewModel.startSelection("first")
        viewModel.toggleEntrySelection("second")
        runCurrent()

        assertTrue(viewModel.uiState.value.selectionMode)
        assertEquals(setOf("first", "second"), viewModel.uiState.value.selectedEntryIds)

        viewModel.requestSelectedDelete()
        runCurrent()
        assertEquals(setOf("first", "second"), viewModel.uiState.value.pendingDeleteEntryIds)

        viewModel.confirmPermanentDelete()
        runCurrent()

        assertNull(repository.getEntry("first"))
        assertNull(repository.getEntry("second"))
        assertNotNull(repository.getEntry("third"))
        assertFalse(viewModel.uiState.value.selectionMode)
        assertTrue(viewModel.uiState.value.selectedEntryIds.isEmpty())
        assertTrue(viewModel.uiState.value.pendingDeleteEntryIds.isEmpty())
    }

    @Test
    fun `retry clears failed state for missing and non link entries`() = runMemoryTest {
        repository.seedEntries(
            entry("missing", kind = MemoryEntryKind.LINK, url = "https://missing.example/"),
            entry("note", kind = MemoryEntryKind.LINK, url = "https://note.example/")
        )
        resolver.enqueue(LinkPreviewResult.Failure("https://missing.example/"))
        resolver.enqueue(LinkPreviewResult.Failure("https://note.example/"))
        val viewModel = viewModel()
        runCurrent()
        viewModel.retryPreview("missing")
        viewModel.retryPreview("note")
        runCurrent()
        assertEquals(setOf("missing", "note"), viewModel.uiState.value.failedPreviewIds)

        repository.seedEntries(entry("note", kind = MemoryEntryKind.NOTE, url = null))
        runCurrent()
        viewModel.retryPreview("missing")
        viewModel.retryPreview("note")
        runCurrent()

        assertTrue(viewModel.uiState.value.failedPreviewIds.isEmpty())
        assertEquals(2, resolver.requestedUrls.size)
    }

    @Test
    fun `completion restore and permanent delete confirmation mutate only the requested entry`() = runMemoryTest {
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
        assertEquals(setOf("first"), viewModel.uiState.value.pendingDeleteEntryIds)
        assertNotNull(repository.getEntry("first"))
        viewModel.dismissPermanentDelete()
        runCurrent()
        assertTrue(viewModel.uiState.value.pendingDeleteEntryIds.isEmpty())
        assertNotNull(repository.getEntry("first"))

        viewModel.requestPermanentDelete("first")
        viewModel.confirmPermanentDelete()
        runCurrent()
        assertNull(repository.getEntry("first"))
        assertNotNull(repository.getEntry("second"))
        assertTrue(viewModel.uiState.value.pendingDeleteEntryIds.isEmpty())
    }

    @Test
    fun `category create update and delete actions keep repository state observable`() = runMemoryTest {
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
    fun `failed write exposes recoverable error and the next successful action clears it`() = runMemoryTest {
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
        .also { viewModelStore.put(UUID.randomUUID().toString(), it) }

    private fun runMemoryTest(testBody: suspend TestScope.() -> Unit) = runTest(scheduler) {
        try {
            testBody()
        } finally {
            viewModelStore.clear()
            runCurrent()
        }
    }

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
    val updatedEntries = mutableListOf<MemoryEntryEntity>()
    var defaultSeedCount = 0
    var failNextWrite = false
    var failNextEntryRead = false
    var failNextPreviewUpdate = false
    var beforeUpdateEntry: suspend (MemoryEntryEntity) -> Unit = {}

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

    override suspend fun getEntry(id: String): MemoryEntryEntity? {
        if (failNextEntryRead) {
            failNextEntryRead = false
            error("planned entry read failure")
        }
        return entryState.value.find { it.id == id }
    }

    override suspend fun updateEntry(entry: MemoryEntryEntity) {
        beforeUpdateEntry(entry)
        if (failNextPreviewUpdate && entry.previewFetchedAt != null) {
            failNextPreviewUpdate = false
            error("planned preview update failure")
        }
        failIfRequested()
        updatedEntries += entry
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

    override suspend fun deleteEntries(ids: Set<String>) {
        failIfRequested()
        entryState.value = entryState.value.filterNot { it.id in ids }
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
