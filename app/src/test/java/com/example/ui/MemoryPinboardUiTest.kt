package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click as viewClick
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.RootMatchers.isDialog
import com.example.data.MemoryBucket
import com.example.data.model.MemoryCategoryEntity
import com.example.data.model.MemoryChecklistItem
import com.example.data.model.MemoryDefaults
import com.example.data.model.MemoryEntryEntity
import com.example.data.model.MemoryEntryKind
import com.example.ui.memory.MemoryEditorMode
import com.example.ui.memory.MemoryEntryUi
import com.example.ui.memory.MemoryTab
import com.example.ui.memory.MemoryUiState
import com.example.ui.screens.MemoryCategoryDialog
import com.example.ui.screens.MemoryEditorSheet
import com.example.ui.screens.MemoryScreen
import com.example.ui.theme.HarmonyTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.Matchers.equalTo
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class MemoryPinboardUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `screen exposes navigation add modes and completion actions`() {
        var selectedTab: MemoryTab? = null
        var completedId: String? = null
        var editorMode: MemoryEditorMode? = null

        setScreen(
            state = memoryState(
                entries = listOf(memoryEntry("entry-1", title = "Sterne beobachten"))
            ),
            onSelectTab = { selectedTab = it },
            onOpenEditor = { mode, _ -> editorMode = mode },
            onComplete = { completedId = it }
        )

        composeRule.onNodeWithTag("memory_screen").assertExists()
        composeRule.onNodeWithTag("memory_tab_archived").performClick()
        assertEquals(MemoryTab.ARCHIVED, selectedTab)

        composeRule.onNodeWithTag("memory_entry_entry-1_complete").performClick()
        assertEquals("entry-1", completedId)

        composeRule.onNodeWithTag("memory_add_button").performClick()
        composeRule.onNodeWithTag("memory_mode_list").assertExists().performClick()
        assertEquals(MemoryEditorMode.LIST, editorMode)
    }

    @Test
    fun `tapping a note card opens its full editor`() {
        var openedMode: MemoryEditorMode? = null
        var openedId: String? = null
        setScreen(
            state = memoryState(
                entries = listOf(memoryEntry("editable-note", title = "Sterne beobachten"))
            ),
            onOpenEditor = { mode, id ->
                openedMode = mode
                openedId = id
            }
        )

        composeRule.onNodeWithText("Sterne beobachten").performClick()

        assertEquals(MemoryEditorMode.NOTE, openedMode)
        assertEquals("editable-note", openedId)
    }

    @Test
    fun `link card puts the couples note before preview metadata`() {
        val link = memoryEntry(
            id = "watch-link",
            title = "https://m.youtube.com/watch?v=abc",
            kind = MemoryEntryKind.LINK,
            url = "https://m.youtube.com/watch?v=abc"
        ).let { item ->
            item.copy(
                entity = item.entity.copy(
                    body = "Diesen Film wollen wir zusammen anschauen",
                    previewTitle = "Offizieller Trailer",
                    previewDescription = "Automatisch geladene Beschreibung"
                )
            )
        }
        setScreen(state = memoryState(entries = listOf(link)))

        composeRule.onNodeWithTag("memory_entry_watch-link_primary", useUnmergedTree = true)
            .assertTextEquals("Diesen Film wollen wir zusammen anschauen")
        composeRule.onNodeWithTag("memory_entry_watch-link_preview_title", useUnmergedTree = true)
            .assertTextEquals("Offizieller Trailer")
    }

    @Test
    fun `long press selects several notes for one delete request`() {
        var deleteRequests = 0
        var state by mutableStateOf(
            memoryState(
                entries = listOf(
                    memoryEntry("first", title = "Erste Notiz"),
                    memoryEntry("second", title = "Zweite Notiz")
                )
            )
        )
        composeRule.setContent {
            HarmonyTheme(darkTheme = true) {
                MemoryScreen(
                    state = state,
                    appLanguage = "de",
                    onSelectTab = {},
                    onQueryChange = {},
                    onCategoryFilter = {},
                    onOpenEditor = { _, _ -> },
                    onStartSelection = { id ->
                        state = state.copy(
                            selectionMode = true,
                            selectedEntryIds = id?.let { state.selectedEntryIds + it } ?: state.selectedEntryIds
                        )
                    },
                    onToggleEntrySelection = { id ->
                        state = state.copy(
                            selectedEntryIds = if (id in state.selectedEntryIds) {
                                state.selectedEntryIds - id
                            } else {
                                state.selectedEntryIds + id
                            }
                        )
                    },
                    onSelectAllEntries = {},
                    onClearSelection = {},
                    onDeleteSelectedRequest = { deleteRequests += 1 },
                    onComplete = {},
                    onRestore = {},
                    onRetryPreview = {},
                    onDeleteRequest = {},
                    onDeleteConfirm = {},
                    onDeleteDismiss = {},
                    onCreateCategory = { _, _, _ -> },
                    onUpdateCategory = { _, _, _, _ -> },
                    onDeleteCategory = { _, _ -> }
                )
            }
        }

        composeRule.onNodeWithTag("memory_entry_first").performTouchInput { longClick() }
        composeRule.onNodeWithTag("memory_entry_second").performClick()
        composeRule.runOnIdle { assertEquals(setOf("first", "second"), state.selectedEntryIds) }

        composeRule.onNodeWithTag("memory_selection_delete").performClick()
        composeRule.runOnIdle { assertEquals(1, deleteRequests) }
    }

    @Test
    fun `failed preview exposes retry action`() {
        var retriedId: String? = null
        val failedLink = memoryEntry(
            id = "failed-link",
            title = "https://example.invalid",
            kind = MemoryEntryKind.LINK,
            url = "https://example.invalid"
        )
        setScreen(
            state = memoryState(
                entries = listOf(failedLink),
                failedPreviewIds = setOf(failedLink.entity.id)
            ),
            onRetryPreview = { retriedId = it }
        )

        composeRule.onNodeWithTag("memory_entry_failed-link_retry").performScrollTo().performClick()
        assertEquals("failed-link", retriedId)
    }

    @Test
    fun `archived entry announces completion and restores`() {
        var restoredId: String? = null
        val archived = memoryEntry(
            id = "archived-entry",
            title = "Museum besuchen",
            bucket = MemoryBucket.ARCHIVED,
            completedAt = 10_000L
        )

        setScreen(
            state = memoryState(
                tab = MemoryTab.ARCHIVED,
                entries = listOf(archived)
            ),
            onRestore = { restoredId = it }
        )

        composeRule.onNodeWithText("Erledigt").assertExists()
        composeRule.onNodeWithTag("memory_entry_archived-entry_restore").performScrollTo().performClick()
        assertEquals("archived-entry", restoredId)
    }

    @Test
    fun `archived permanent deletion calls request before confirm`() {
        var requestedId: String? = null
        var confirmCalls = 0
        var state by mutableStateOf(
            memoryState(
                tab = MemoryTab.ARCHIVED,
                entries = listOf(
                    memoryEntry(
                        id = "archived-entry",
                        title = "Arrival",
                        bucket = MemoryBucket.ARCHIVED,
                        completedAt = 1L
                    )
                )
            )
        )

        composeRule.setContent {
            HarmonyTheme(darkTheme = true) {
                MemoryScreen(
                    state = state,
                    appLanguage = "de",
                    onSelectTab = {},
                    onQueryChange = {},
                    onCategoryFilter = {},
                    onOpenEditor = { _, _ -> },
                    onComplete = {},
                    onRestore = {},
                    onRetryPreview = {},
                    onDeleteRequest = {
                        requestedId = it
                        state = state.copy(pendingDeleteEntryIds = setOf(it))
                    },
                    onDeleteConfirm = {
                        confirmCalls += 1
                        state = state.copy(pendingDeleteEntryIds = emptySet())
                    },
                    onDeleteDismiss = { state = state.copy(pendingDeleteEntryIds = emptySet()) },
                    onCreateCategory = { _, _, _ -> },
                    onUpdateCategory = { _, _, _, _ -> },
                    onDeleteCategory = { _, _ -> }
                )
            }
        }

        composeRule.onNodeWithTag("memory_entry_archived-entry_menu").performClick()
        composeRule.onNodeWithTag("memory_entry_archived-entry_delete").performClick()
        assertEquals("archived-entry", requestedId)
        assertEquals(0, confirmCalls)
        composeRule.onNodeWithTag("memory_delete_confirm").assertExists().performClick()
        assertEquals(1, confirmCalls)
    }

    @Test
    fun `list editor keeps items in one checklist and moves checked rows to completed`() {
        var savedEntryId: String? = "not-called"
        var savedCategory: String? = null
        var savedTitle: String? = null
        var savedItems: List<MemoryChecklistItem> = emptyList()
        composeRule.setContent {
            HarmonyTheme(darkTheme = true) {
                MemoryEditorSheet(
                    mode = MemoryEditorMode.LIST,
                    categories = testCategories,
                    appLanguage = "de",
                    onModeChange = {},
                    onDismiss = {},
                    onSaveNote = { _, _, _, _ -> },
                    onSaveList = { entryId, categoryId, title, items ->
                        savedEntryId = entryId
                        savedCategory = categoryId
                        savedTitle = title
                        savedItems = items
                    },
                    onSaveLink = { _, _, _, _ -> }
                )
            }
        }

        composeRule.onNodeWithTag("memory_editor_save").assertIsNotEnabled()
        composeRule.onNodeWithTag("memory_editor_title").performTextInput("Einkauf")
        composeRule.onNodeWithTag("memory_checklist_active_item_0_text").performTextInput("Milch")
        composeRule.onNodeWithTag("memory_checklist_add").performClick()
        composeRule.onNodeWithTag("memory_checklist_active_item_1_text").performTextInput("Brot")
        composeRule.onNodeWithTag("memory_checklist_active_item_0_toggle").performClick()
        composeRule.onNodeWithTag("memory_checklist_completed_item_0_text")
            .assertTextContains("Milch", substring = true)
        composeRule.onNodeWithTag("memory_editor_save").performScrollTo().assertIsEnabled().performClick()

        assertEquals(null, savedEntryId)
        assertEquals(MemoryDefaults.FILMS_ID, savedCategory)
        assertEquals("Einkauf", savedTitle)
        assertEquals(listOf("Brot", "Milch"), savedItems.map { it.text })
        assertEquals(listOf(false, true), savedItems.map { it.completed })
    }

    @Test
    fun `first back press clears editor focus without dismissing the note`() {
        var dismissCalls = 0
        composeRule.setContent {
            HarmonyTheme(darkTheme = true) {
                MemoryEditorSheet(
                    mode = MemoryEditorMode.NOTE,
                    categories = testCategories,
                    appLanguage = "de",
                    onModeChange = {},
                    onDismiss = { dismissCalls += 1 },
                    onSaveNote = { _, _, _, _ -> },
                    onSaveList = { _, _, _, _ -> },
                    onSaveLink = { _, _, _, _ -> }
                )
            }
        }

        composeRule.onNodeWithTag("memory_editor_title").performClick().assertIsFocused()
        pressBack()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("memory_editor_sheet").assertExists()
        assertEquals(0, dismissCalls)
    }

    @Test
    fun `link editor accepts only http or https urls`() {
        var saved = false
        composeRule.setContent {
            HarmonyTheme(darkTheme = true) {
                MemoryEditorSheet(
                    mode = MemoryEditorMode.LINK,
                    categories = testCategories,
                    appLanguage = "de",
                    onModeChange = {},
                    onDismiss = {},
                    onSaveNote = { _, _, _, _ -> },
                    onSaveList = { _, _, _, _ -> },
                    onSaveLink = { _, _, _, _ -> saved = true }
                )
            }
        }

        composeRule.onNodeWithTag("memory_editor_url").performTextInput("javascript:alert(1)")
        composeRule.onNodeWithTag("memory_editor_save").assertIsNotEnabled()
        composeRule.onNodeWithText("Bitte gib einen gültigen HTTP- oder HTTPS-Link ein").assertExists()

        composeRule.onNodeWithTag("memory_editor_url").performTextClearance()
        composeRule.onNodeWithTag("memory_editor_url").performTextInput("https://harmony.example")
        composeRule.onNodeWithTag("memory_editor_save").assertIsEnabled().performClick()
        assertTrue(saved)
    }

    @Test
    fun `existing note opens as a fully populated editing sheet`() {
        val existing = MemoryEntryEntity(
            id = "editable",
            categoryId = MemoryDefaults.FILMS_ID,
            kind = MemoryEntryKind.NOTE,
            title = "The Bear",
            body = "Gemeinsam weiterschauen",
            createdAt = 1L,
            updatedAt = 2L
        )
        composeRule.setContent {
            HarmonyTheme(darkTheme = true) {
                MemoryEditorSheet(
                    mode = MemoryEditorMode.NOTE,
                    categories = testCategories,
                    appLanguage = "de",
                    onModeChange = {},
                    onDismiss = {},
                    onSaveNote = { _, _, _, _ -> },
                    onSaveList = { _, _, _, _ -> },
                    onSaveLink = { _, _, _, _ -> },
                    initialEntry = existing
                )
            }
        }

        composeRule.onNodeWithText("Notiz bearbeiten").assertExists()
        composeRule.onNodeWithTag("memory_editor_title").assertTextContains("The Bear", substring = true)
        composeRule.onNodeWithTag("memory_editor_body")
            .assertTextContains("Gemeinsam weiterschauen", substring = true)
    }

    @Test
    fun `default category dialog allows moving entries before deletion`() {
        var deletedTarget: String? = null
        composeRule.setContent {
            HarmonyTheme(darkTheme = true) {
                MemoryCategoryDialog(
                    category = testCategories.first(),
                    categories = testCategories,
                    entryCount = 2,
                    appLanguage = "de",
                    onDismiss = {},
                    onCreate = { _, _, _ -> },
                    onUpdate = { _, _, _, _ -> },
                    onDelete = { _, target -> deletedTarget = target }
                )
            }
        }
        composeRule.onNodeWithTag("memory_category_move_${MemoryDefaults.OTHER_ID}").performClick()
        composeRule.onNodeWithTag("memory_category_delete").assertIsEnabled().performClick()
        composeRule.runOnIdle { assertEquals(MemoryDefaults.OTHER_ID, deletedTarget) }
    }

    @Test
    fun `non-empty custom category requires move target before deletion`() {
        val custom = category(
            id = "custom-dates",
            systemKey = null,
            customName = "Date-Ideen",
            colorKey = "purple",
            iconKey = "sparkles"
        )
        var deletedTarget: String? = null
        composeRule.setContent {
            HarmonyTheme(darkTheme = true) {
                MemoryCategoryDialog(
                    category = custom,
                    categories = testCategories + custom,
                    entryCount = 2,
                    appLanguage = "de",
                    onDismiss = {},
                    onCreate = { _, _, _ -> },
                    onUpdate = { _, _, _, _ -> },
                    onDelete = { _, target -> deletedTarget = target }
                )
            }
        }

        composeRule.onNodeWithTag("memory_category_delete").assertIsNotEnabled()
        composeRule.onNodeWithTag("memory_category_move_${MemoryDefaults.OTHER_ID}").performScrollTo()
        composeRule.onNodeWithTag("memory_category_move_${MemoryDefaults.OTHER_ID}").performClick()
        composeRule.onNodeWithTag("memory_category_delete").performScrollTo().assertIsEnabled().performClick()
        composeRule.runOnIdle { assertEquals(MemoryDefaults.OTHER_ID, deletedTarget) }
    }

    @Test
    fun `custom category name activates editing and creates category`() {
        var created: Triple<String, String, String>? = null
        composeRule.setContent {
            HarmonyTheme(darkTheme = true) {
                MemoryCategoryDialog(
                    category = null,
                    categories = testCategories,
                    entryCount = 0,
                    appLanguage = "de",
                    onDismiss = {},
                    onCreate = { name, color, icon -> created = Triple(name, color, icon) },
                    onUpdate = { _, _, _, _ -> },
                    onDelete = { _, _ -> }
                )
            }
        }

        composeRule.onNodeWithTag("memory_category_save").assertIsNotEnabled()
        composeRule.onNodeWithTag("memory_category_name").performClick()
        composeRule.onNodeWithTag("memory_category_name_editing").assertExists()
        onView(withTagValue(equalTo("memory_category_name_view")))
            .inRoot(isDialog())
            .perform(
                viewClick(),
                typeText("Wochenenden"),
                closeSoftKeyboard()
            )
        composeRule.onNodeWithTag("memory_category_save").assertIsEnabled().performClick()
        composeRule.runOnIdle {
            assertEquals("Wochenenden", created?.first)
            assertEquals("violet", created?.second)
            assertEquals("bookmark", created?.third)
        }
    }

    private fun setScreen(
        state: MemoryUiState,
        onSelectTab: (MemoryTab) -> Unit = {},
        onOpenEditor: (MemoryEditorMode, String?) -> Unit = { _, _ -> },
        onComplete: (String) -> Unit = {},
        onRestore: (String) -> Unit = {},
        onRetryPreview: (String) -> Unit = {}
    ) {
        composeRule.setContent {
            HarmonyTheme(darkTheme = true) {
                MemoryScreen(
                    state = state,
                    appLanguage = "de",
                    onSelectTab = onSelectTab,
                    onQueryChange = {},
                    onCategoryFilter = {},
                    onOpenEditor = onOpenEditor,
                    onComplete = onComplete,
                    onRestore = onRestore,
                    onRetryPreview = onRetryPreview,
                    onDeleteRequest = {},
                    onDeleteConfirm = {},
                    onDeleteDismiss = {},
                    onCreateCategory = { _, _, _ -> },
                    onUpdateCategory = { _, _, _, _ -> },
                    onDeleteCategory = { _, _ -> }
                )
            }
        }
    }

    private fun memoryState(
        tab: MemoryTab = MemoryTab.CURRENT,
        entries: List<MemoryEntryUi>,
        failedPreviewIds: Set<String> = emptySet()
    ) = MemoryUiState(
        categories = testCategories,
        visibleEntries = entries,
        selectedTab = tab,
        failedPreviewIds = failedPreviewIds
    )

    private fun memoryEntry(
        id: String,
        title: String,
        kind: MemoryEntryKind = MemoryEntryKind.NOTE,
        url: String? = null,
        bucket: MemoryBucket = MemoryBucket.CURRENT_OPEN,
        completedAt: Long? = null
    ) = MemoryEntryUi(
        entity = MemoryEntryEntity(
            id = id,
            categoryId = MemoryDefaults.FILMS_ID,
            kind = kind,
            title = title,
            url = url,
            createdAt = 1L,
            updatedAt = 2L,
            completedAt = completedAt
        ),
        bucket = bucket
    )

    private companion object {
        val testCategories = listOf(
            category(MemoryDefaults.FILMS_ID, "Filme & Serien", null, "violet", "movie"),
            category(MemoryDefaults.OTHER_ID, "Sonstiges", null, "teal", "bookmark")
        )

        fun category(
            id: String,
            systemKey: String?,
            customName: String?,
            colorKey: String,
            iconKey: String
        ) = MemoryCategoryEntity(
            id = id,
            systemKey = systemKey,
            customName = customName,
            colorKey = colorKey,
            iconKey = iconKey,
            sortOrder = 0,
            createdAt = 1L,
            updatedAt = 1L
        )
    }
}
