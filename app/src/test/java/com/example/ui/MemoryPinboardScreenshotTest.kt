package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.example.R
import com.example.data.MemoryBucket
import com.example.data.model.MemoryCategoryEntity
import com.example.data.model.MemoryDefaults
import com.example.data.model.MemoryEntryEntity
import com.example.data.model.MemoryEntryKind
import com.example.data.model.MemoryChecklistCodec
import com.example.data.model.MemoryChecklistItem
import com.example.ui.components.AmbientBackground
import com.example.ui.components.HarmonyBottomNav
import com.example.ui.components.HarmonyTopBar
import com.example.ui.memory.MemoryEditorMode
import com.example.ui.memory.MemoryEntryUi
import com.example.ui.memory.MemoryTab
import com.example.ui.memory.MemoryUiState
import com.example.ui.screens.MemoryCategoryDialog
import com.example.ui.screens.MemoryEditorSheet
import com.example.ui.screens.MemoryScreen
import com.example.ui.theme.HarmonyTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class MemoryPinboardScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun integratedPinboardShell() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            val packageName = LocalContext.current.packageName
            MemoryScreenshotTheme {
                AmbientBackground {
                    Scaffold(
                        containerColor = Color.Transparent,
                        topBar = {
                            HarmonyTopBar(
                                userName = "Mia",
                                partnerName = "Noah",
                                onProfileClick = {},
                                onRefresh = {},
                                showMemoryMark = true
                            )
                        },
                        bottomBar = {
                            HarmonyBottomNav(
                                selectedTab = 4,
                                onTabSelected = {},
                                appLanguage = "de"
                            )
                        }
                    ) { padding ->
                        Box(Modifier.padding(padding)) {
                            ScreenForCapture(
                                state = MemoryUiState(
                                    categories = categories,
                                    visibleEntries = listOf(
                                        entry(
                                            id = "link-observatory",
                                            categoryId = MemoryDefaults.PLACES_ID,
                                            title = "Sternwarte im Harz",
                                            kind = MemoryEntryKind.LINK,
                                            url = "https://example.invalid/sternwarte",
                                            body = "Diesen Ort wollen wir uns für ein Wochenende merken.",
                                            previewTitle = "Sternennacht über dem Brocken",
                                            previewDescription = "Ein stiller Ort für unsere nächste klare Nacht.",
                                            previewImageUrl = "android.resource://$packageName/${R.drawable.tokyo_tower_zojoji}",
                                            previewSiteName = "Reiseideen"
                                        ),
                                        entry(
                                            id = "note-picnic",
                                            categoryId = MemoryDefaults.IDEAS_ID,
                                            title = "Picknick bei Sonnenuntergang",
                                            body = "Decke, Erdbeeren und unsere Lieblingsplaylist einpacken."
                                        )
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }

        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage(
            filePath = "build/outputs/roborazzi/memory-pinboard/00-integrated-shell.png"
        )
    }

    @Test
    fun populatedCurrentPinboard() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            val packageName = LocalContext.current.packageName
            MemoryScreenshotTheme {
                ScreenForCapture(
                    state = MemoryUiState(
                        categories = categories,
                        visibleEntries = listOf(
                            entry(
                                id = "link-observatory",
                                categoryId = MemoryDefaults.PLACES_ID,
                                title = "Sternwarte im Harz",
                                kind = MemoryEntryKind.LINK,
                                url = "https://example.invalid/sternwarte",
                                body = "Diesen Ort wollen wir uns für ein Wochenende merken.",
                                previewTitle = "Sternennacht über dem Brocken",
                                previewDescription = "Ein stiller Ort für unsere nächste klare Nacht.",
                                previewImageUrl = "android.resource://$packageName/${R.drawable.tokyo_tower_zojoji}",
                                previewSiteName = "Reiseideen"
                            ),
                            entry(
                                id = "note-picnic",
                                categoryId = MemoryDefaults.IDEAS_ID,
                                title = "Picknick bei Sonnenuntergang",
                                body = "Decke, Erdbeeren und unsere Lieblingsplaylist einpacken."
                            ),
                            entry(
                                id = "list-weekend",
                                categoryId = MemoryDefaults.OTHER_ID,
                                title = "Fürs Wochenende",
                                body = MemoryChecklistCodec.encode(
                                    listOf(
                                        MemoryChecklistItem("market", "Flohmarkt besuchen"),
                                        MemoryChecklistItem("photos", "Fotos entwickeln lassen"),
                                        MemoryChecklistItem("grandma", "Oma anrufen", completed = true)
                                    )
                                ),
                                kind = MemoryEntryKind.LIST
                            )
                        )
                    )
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage(
            filePath = "build/outputs/roborazzi/memory-pinboard/01-current-populated.png"
        )
    }

    @Test
    fun freshlyCompletedArchiveCard() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MemoryScreenshotTheme {
                ScreenForCapture(
                    state = MemoryUiState(
                        categories = categories,
                        visibleEntries = listOf(
                            entry(
                                id = "archive-summer-cinema",
                                categoryId = MemoryDefaults.FILMS_ID,
                                title = "Sommerkino am See",
                                body = "Die Vorstellung am Freitagabend vormerken.",
                                bucket = MemoryBucket.ARCHIVED,
                                completedAt = FIXED_NOW
                            )
                        ),
                        selectedTab = MemoryTab.ARCHIVED
                    )
                )
            }
        }

        composeRule.onRoot().captureRoboImage(
            filePath = "build/outputs/roborazzi/memory-pinboard/02-archived-completed.png"
        )
    }

    @Test
    fun archivedFilmAndSeriesHistory() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MemoryScreenshotTheme {
                ScreenForCapture(
                    state = MemoryUiState(
                        categories = categories,
                        visibleEntries = listOf(
                            entry(
                                id = "archive-film",
                                categoryId = MemoryDefaults.FILMS_ID,
                                title = "Kino unter freiem Himmel",
                                body = "Hat uns beiden richtig gut gefallen.",
                                bucket = MemoryBucket.ARCHIVED,
                                completedAt = FIXED_NOW - 172_800_000L
                            ),
                            entry(
                                id = "archive-series",
                                categoryId = MemoryDefaults.FILMS_ID,
                                title = "Die kleine Küstenserie",
                                body = "Perfekt für verregnete Sonntage.",
                                bucket = MemoryBucket.ARCHIVED,
                                completedAt = FIXED_NOW - 259_200_000L
                            )
                        ),
                        selectedTab = MemoryTab.ARCHIVED
                    )
                )
            }
        }

        composeRule.onRoot().captureRoboImage(
            filePath = "build/outputs/roborazzi/memory-pinboard/03-archived-film-series.png"
        )
    }

    @Test
    fun checklistEditorSheet() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MemoryScreenshotTheme {
                AmbientBackground {
                    MemoryEditorSheet(
                        mode = MemoryEditorMode.LIST,
                        categories = categories,
                        appLanguage = "de",
                        onModeChange = {},
                        onDismiss = {},
                        onSaveNote = { _, _, _, _ -> },
                        onSaveList = { _, _, _, _ -> },
                        onSaveLink = { _, _, _, _ -> },
                        initialEntry = entry(
                            id = "list-editor",
                            categoryId = MemoryDefaults.OTHER_ID,
                            title = "Einkauf für Sonntag",
                            body = MemoryChecklistCodec.encode(
                                listOf(
                                    MemoryChecklistItem("milk", "Milch"),
                                    MemoryChecklistItem("bread", "Brot"),
                                    MemoryChecklistItem("berries", "Erdbeeren", completed = true)
                                )
                            ),
                            kind = MemoryEntryKind.LIST
                        ).entity
                    )
                }
            }
        }
        composeRule.mainClock.advanceTimeBy(1_000L)

        composeRule.onAllNodes(isRoot()).onLast().captureRoboImage(
            filePath = "build/outputs/roborazzi/memory-pinboard/04-checklist-editor.png"
        )
    }

    @Test
    fun failedLinkFallback() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MemoryScreenshotTheme {
                ScreenForCapture(
                    state = MemoryUiState(
                        categories = categories,
                        visibleEntries = listOf(
                            entry(
                                id = "failed-link",
                                categoryId = MemoryDefaults.PLACES_ID,
                                title = "Kleines Café am Wasser",
                                kind = MemoryEntryKind.LINK,
                                url = "https://example.invalid/cafe",
                                previewTitle = "Café am Wasser",
                                previewDescription = "Link-Vorschau bewusst offline getestet.",
                                previewSiteName = "Ausflugsziele"
                            )
                        ),
                        failedPreviewIds = setOf("failed-link")
                    )
                )
            }
        }

        composeRule.onRoot().captureRoboImage(
            filePath = "build/outputs/roborazzi/memory-pinboard/05-failed-link-fallback.png"
        )
    }

    @Test
    fun categoryEditorNativeField() {
        val custom = category(
            id = "custom-weekends",
            systemKey = null,
            customName = "Wochenenden",
            colorKey = "purple",
            iconKey = "sparkles",
            sortOrder = 5
        )
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MemoryScreenshotTheme {
                MemoryCategoryDialog(
                    category = custom,
                    categories = categories + custom,
                    entryCount = 2,
                    appLanguage = "de",
                    onDismiss = {},
                    onCreate = { _, _, _ -> },
                    onUpdate = { _, _, _, _ -> },
                    onDelete = { _, _ -> }
                )
            }
        }
        composeRule.onNodeWithTag("memory_category_name").performClick()

        composeRule.onNodeWithTag("memory_category_dialog").captureRoboImage(
            filePath = "build/outputs/roborazzi/memory-pinboard/06-category-editor-native-field.png"
        )
    }

    @Composable
    private fun MemoryScreenshotTheme(content: @Composable () -> Unit) {
        HarmonyTheme(darkTheme = true, content = content)
    }

    @Composable
    private fun ScreenForCapture(state: MemoryUiState) {
        MemoryScreen(
            state = state,
            appLanguage = "de",
            userName = "Mia",
            partnerName = "Noah",
            onSelectTab = {},
            onQueryChange = {},
            onCategoryFilter = {},
            onOpenEditor = { _, _ -> },
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

    private fun entry(
        id: String,
        categoryId: String,
        title: String,
        body: String? = null,
        kind: MemoryEntryKind = MemoryEntryKind.NOTE,
        url: String? = null,
        previewTitle: String? = null,
        previewDescription: String? = null,
        previewImageUrl: String? = null,
        previewSiteName: String? = null,
        bucket: MemoryBucket = MemoryBucket.CURRENT_OPEN,
        completedAt: Long? = null
    ) = MemoryEntryUi(
        entity = MemoryEntryEntity(
            id = id,
            categoryId = categoryId,
            kind = kind,
            title = title,
            body = body,
            url = url,
            previewTitle = previewTitle,
            previewDescription = previewDescription,
            previewImageUrl = previewImageUrl,
            previewSiteName = previewSiteName,
            previewFetchedAt = FIXED_NOW,
            createdAt = FIXED_NOW - 604_800_000L,
            updatedAt = FIXED_NOW,
            completedAt = completedAt
        ),
        bucket = bucket
    )

    private companion object {
        const val FIXED_NOW = 1_750_000_000_000L

        val categories = listOf(
            category(MemoryDefaults.FILMS_ID, "Filme & Serien", null, "violet", "movie", 0),
            category(MemoryDefaults.IDEAS_ID, "Ideen", null, "gold", "lightbulb", 1),
            category(MemoryDefaults.PLACES_ID, "Orte", null, "blue", "place", 2),
            category(MemoryDefaults.OTHER_ID, "Sonstiges", null, "teal", "bookmark", 3)
        )

        fun category(
            id: String,
            systemKey: String?,
            customName: String?,
            colorKey: String,
            iconKey: String,
            sortOrder: Int
        ) = MemoryCategoryEntity(
            id = id,
            systemKey = systemKey,
            customName = customName,
            colorKey = colorKey,
            iconKey = iconKey,
            sortOrder = sortOrder,
            createdAt = FIXED_NOW,
            updatedAt = FIXED_NOW
        )
    }
}
