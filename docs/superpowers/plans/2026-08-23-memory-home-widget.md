# Memory Homescreen Widget Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a configurable Harmony homescreen widget that shows 1–3 open “Das müssen wir uns merken” entries, supports automatic or pinned selection, shows compact URL previews, opens links or the exact Harmony entry, and marks items completed directly from the homescreen.

**Architecture:** Reuse the existing Room-backed Memory data and the app’s existing classic `AppWidgetProvider`/`RemoteViews` pattern. Store only per-widget configuration in `SharedPreferences`, render three fixed RemoteViews slots with size-based visibility, fetch only the already-resolved `previewImageUrl` into a small cache, and route app-open actions through `MainActivity` extras into the existing Memory tab/editor state.

**Tech Stack:** Kotlin, Android AppWidgetProvider/RemoteViews, Room, SharedPreferences, OkHttp, Jetpack Compose for configuration UI, JUnit/Robolectric, Gradle Android resource processing.

**Spec:** `docs/superpowers/specs/2026-08-23-memory-home-widget-design.md`

## Global Constraints

- Keep `minSdk = 24`, `targetSdk = 36`; do not add Jetpack Glance.
- Reuse Room `memory_entries` and existing preview metadata; do not introduce a second Memory datastore.
- Preserve the existing `PicShareWidgetProvider` behavior and manifest registration.
- A widget shows at most 3 entries.
- Default mode is automatic; pinned mode allows up to 3 ordered IDs.
- Missing/completed/deleted pinned IDs are backfilled from newest open entries without duplicates.
- Link-preview tap opens the browser; entry text tap opens the exact Harmony Memory entry; check tap completes without opening Harmony.
- Remote image failure renders a branded fallback and never blocks widget usability.
- Widget writes and successful app-side Memory writes refresh all Memory widgets.

---

## File Structure

### New files

- `app/src/main/java/com/example/widget/MemoryWidgetLogic.kt` — pure selection/size/config model.
- `app/src/main/java/com/example/widget/MemoryWidgetPreferences.kt` — per-widget persistence.
- `app/src/main/java/com/example/widget/MemoryWidgetIntents.kt` — request parsing and unique intents/PendingIntents.
- `app/src/main/java/com/example/widget/MemoryWidgetImageCache.kt` — preview bitmap cache/downloader.
- `app/src/main/java/com/example/widget/MemoryWidgetProvider.kt` — lifecycle, rendering, completion, refresh.
- `app/src/main/java/com/example/widget/MemoryWidgetConfigActivity.kt` — widget setup UI.
- `app/src/main/res/layout/widget_memory.xml` — header, empty state, three fixed row slots.
- `app/src/main/res/drawable/widget_memory_background.xml`
- `app/src/main/res/drawable/widget_memory_slot_background.xml`
- `app/src/main/res/drawable/widget_memory_link_fallback.xml`
- `app/src/main/res/xml/memory_widget_info.xml`
- `app/src/test/java/com/example/widget/MemoryWidgetLogicTest.kt`
- `app/src/test/java/com/example/widget/MemoryWidgetPreferencesTest.kt`
- `app/src/test/java/com/example/widget/MemoryWidgetIntentsTest.kt`
- `app/src/test/java/com/example/widget/MemoryWidgetProviderTest.kt`
- `app/src/test/java/com/example/widget/MemoryWidgetRegistrationTest.kt`

### Modified files

- `app/src/main/java/com/example/data/db/MemoryDao.kt`
- `app/src/main/java/com/example/ui/memory/MemoryViewModel.kt`
- `app/src/main/java/com/example/MainActivity.kt`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/values/strings.xml`
- `app/src/test/java/com/example/ui/memory/MemoryViewModelTest.kt`

---

### Task 1: Pure widget selection and per-widget configuration

**Files:**
- Create: `app/src/main/java/com/example/widget/MemoryWidgetLogic.kt`
- Create: `app/src/main/java/com/example/widget/MemoryWidgetPreferences.kt`
- Create: `app/src/test/java/com/example/widget/MemoryWidgetLogicTest.kt`
- Create: `app/src/test/java/com/example/widget/MemoryWidgetPreferencesTest.kt`

**Interfaces:**
- Produces `MemoryWidgetMode`, `MemoryWidgetConfig`, `effectiveMemoryWidgetSlots`, `selectMemoryWidgetEntries`, `MemoryWidgetPreferences.load/save/delete`.

- [ ] **Step 1: Write the failing selection tests**

```kotlin
class MemoryWidgetLogicTest {
    @Test fun `height maps to one two three slots and configured max wins`() {
        assertEquals(1, effectiveMemoryWidgetSlots(179, 3))
        assertEquals(2, effectiveMemoryWidgetSlots(180, 3))
        assertEquals(2, effectiveMemoryWidgetSlots(279, 3))
        assertEquals(3, effectiveMemoryWidgetSlots(280, 3))
        assertEquals(1, effectiveMemoryWidgetSlots(500, 1))
    }

    @Test fun `automatic mode keeps newest open order`() {
        val entries = listOf(entry("new", 300), entry("mid", 200), entry("old", 100))
        val result = selectMemoryWidgetEntries(
            entries,
            MemoryWidgetConfig(MemoryWidgetMode.AUTOMATIC, 3, emptyList()),
            2
        )
        assertEquals(listOf("new", "mid"), result.map { it.id })
    }

    @Test fun `pinned order is preserved and missing ids are backfilled`() {
        val entries = listOf(entry("new", 300), entry("pin-b", 200), entry("pin-a", 100))
        val result = selectMemoryWidgetEntries(
            entries,
            MemoryWidgetConfig(MemoryWidgetMode.PINNED, 3, listOf("pin-a", "missing", "pin-b")),
            3
        )
        assertEquals(listOf("pin-a", "pin-b", "new"), result.map { it.id })
    }

    private fun entry(id: String, updated: Long) = MemoryEntryEntity(
        id = id,
        categoryId = MemoryDefaults.OTHER_ID,
        kind = MemoryEntryKind.NOTE,
        title = id,
        createdAt = updated,
        updatedAt = updated
    )
}
```

- [ ] **Step 2: Run the logic test and verify RED**

```bash
gradle :app:testDebugUnitTest --tests 'com.example.widget.MemoryWidgetLogicTest' --no-daemon
```

Expected: compilation failure because widget logic does not exist.

- [ ] **Step 3: Implement the pure logic**

```kotlin
enum class MemoryWidgetMode { AUTOMATIC, PINNED }

data class MemoryWidgetConfig(
    val mode: MemoryWidgetMode = MemoryWidgetMode.AUTOMATIC,
    val maxItems: Int = 3,
    val pinnedIds: List<String> = emptyList()
) {
    fun normalized() = copy(
        maxItems = maxItems.coerceIn(1, 3),
        pinnedIds = pinnedIds.distinct().take(3)
    )
}

fun effectiveMemoryWidgetSlots(minHeightDp: Int, configuredMax: Int): Int {
    val byHeight = when {
        minHeightDp < 180 -> 1
        minHeightDp < 280 -> 2
        else -> 3
    }
    return minOf(byHeight, configuredMax.coerceIn(1, 3))
}

fun selectMemoryWidgetEntries(
    openEntries: List<MemoryEntryEntity>,
    config: MemoryWidgetConfig,
    slotCount: Int
): List<MemoryEntryEntity> {
    val normalized = config.normalized()
    val limit = minOf(slotCount.coerceIn(1, 3), normalized.maxItems)
    if (normalized.mode == MemoryWidgetMode.AUTOMATIC) return openEntries.take(limit)
    val byId = openEntries.associateBy { it.id }
    val pinned = normalized.pinnedIds.mapNotNull(byId::get)
    val used = pinned.mapTo(mutableSetOf()) { it.id }
    return (pinned + openEntries.filter { used.add(it.id) }).take(limit)
}
```

- [ ] **Step 4: Write SharedPreferences persistence test**

```kotlin
@Test fun `config is normalized stored loaded and deleted`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    MemoryWidgetPreferences.save(
        context,
        42,
        MemoryWidgetConfig(MemoryWidgetMode.PINNED, 9, listOf("a", "a", "b", "c", "d"))
    )
    assertEquals(
        MemoryWidgetConfig(MemoryWidgetMode.PINNED, 3, listOf("a", "b", "c")),
        MemoryWidgetPreferences.load(context, 42)
    )
    MemoryWidgetPreferences.delete(context, 42)
    assertEquals(MemoryWidgetConfig(), MemoryWidgetPreferences.load(context, 42))
}
```

Persist the ordered IDs as a newline-separated string after rejecting IDs containing `\n`; IDs in this app are UUID/system IDs and do not contain newlines.

- [ ] **Step 5: Run Task 1 tests and verify GREEN**

```bash
gradle :app:testDebugUnitTest --tests 'com.example.widget.MemoryWidgetLogicTest' --tests 'com.example.widget.MemoryWidgetPreferencesTest' --no-daemon
```

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/widget/MemoryWidgetLogic.kt app/src/main/java/com/example/widget/MemoryWidgetPreferences.kt app/src/test/java/com/example/widget/MemoryWidgetLogicTest.kt app/src/test/java/com/example/widget/MemoryWidgetPreferencesTest.kt
git commit -m "feat: add Memory widget selection and config logic"
```

---

### Task 2: Widget Room snapshots and app-side refresh/open hooks

**Files:**
- Modify: `app/src/main/java/com/example/data/db/MemoryDao.kt`
- Modify: `app/src/main/java/com/example/ui/memory/MemoryViewModel.kt`
- Modify: `app/src/test/java/com/example/ui/memory/MemoryViewModelTest.kt`

**Interfaces:**
- Produces `MemoryDao.getOpenEntriesForWidget()` and `MemoryDao.getCategoriesForWidget()`.
- Produces `MemoryViewModel(..., onMemoryChanged: () -> Unit = {})`.
- Produces `MemoryViewModel.openEntryFromWidget(entryId: String)`.

- [ ] **Step 1: Add failing ViewModel tests**

```kotlin
@Test fun `successful complete invokes Memory widget refresh callback`() = runTest {
    var refreshes = 0
    val vm = createViewModel(onMemoryChanged = { refreshes++ })
    vm.complete("entry-1")
    advanceUntilIdle()
    assertEquals(1, refreshes)
}

@Test fun `widget link open clears filters and opens link editor`() = runTest {
    repository.insertEntries(listOf(linkEntry(id = "link-1")))
    val vm = createViewModel()
    vm.setQuery("hidden")
    vm.setCategoryFilter(MemoryDefaults.FILMS_ID)
    vm.openEntryFromWidget("link-1")
    advanceUntilIdle()
    assertEquals(MemoryTab.CURRENT, vm.uiState.value.selectedTab)
    assertNull(vm.uiState.value.selectedCategoryId)
    assertEquals("", vm.uiState.value.query)
    assertEquals(MemoryEditorMode.LINK, vm.uiState.value.editorMode)
    assertEquals("link-1", vm.uiState.value.editorEntryId)
}
```

Use the existing test fixture’s entry builder; if it has a different helper name, construct `MemoryEntryEntity` inline with `kind = LINK` and `url = "https://example.com/"` rather than adding a second competing fixture helper.

- [ ] **Step 2: Verify RED**

```bash
gradle :app:testDebugUnitTest --tests 'com.example.ui.memory.MemoryViewModelTest' --no-daemon
```

- [ ] **Step 3: Add DAO snapshot queries**

```kotlin
@Query("SELECT * FROM memory_entries WHERE completedAt IS NULL ORDER BY updatedAt DESC, createdAt DESC")
suspend fun getOpenEntriesForWidget(): List<MemoryEntryEntity>

@Query("SELECT * FROM memory_categories ORDER BY sortOrder, createdAt")
suspend fun getCategoriesForWidget(): List<MemoryCategoryEntity>
```

- [ ] **Step 4: Add refresh callback to successful Memory mutations**

Extend the `MemoryViewModel` constructor and the factory at the bottom of the same file with `onMemoryChanged: () -> Unit = {}`. Call it after successful writes for note/list/link save, preview write, category create/update/delete, complete, restore, and permanent delete. Do not call it when validation fails or an exception prevents the Room mutation.

- [ ] **Step 5: Add exact-entry open helper**

```kotlin
fun openEntryFromWidget(entryId: String) {
    viewModelScope.launch(ioDispatcher) {
        val entry = repository.getEntry(entryId) ?: return@launch
        updateLocal {
            copy(
                selectedTab = MemoryTab.CURRENT,
                selectedCategoryId = null,
                query = "",
                editorMode = if (entry.kind == MemoryEntryKind.LINK) MemoryEditorMode.LINK else MemoryEditorMode.NOTE,
                editorEntryId = entry.id
            )
        }
    }
}
```

- [ ] **Step 6: Verify GREEN**

```bash
gradle :app:testDebugUnitTest --tests 'com.example.ui.memory.MemoryViewModelTest' --no-daemon
```

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/example/data/db/MemoryDao.kt app/src/main/java/com/example/ui/memory/MemoryViewModel.kt app/src/test/java/com/example/ui/memory/MemoryViewModelTest.kt
git commit -m "feat: expose Memory widget snapshots and refresh hooks"
```

---

### Task 3: Intent/PendingIntent contract

**Files:**
- Create: `app/src/main/java/com/example/widget/MemoryWidgetIntents.kt`
- Create: `app/src/test/java/com/example/widget/MemoryWidgetIntentsTest.kt`

**Interfaces:**
- Produces constants `EXTRA_OPEN_MEMORY`, `EXTRA_MEMORY_ENTRY_ID`, `EXTRA_APP_WIDGET_ID`, `ACTION_COMPLETE_MEMORY_ENTRY`.
- Produces `MemoryWidgetOpenRequest(entryId: String?)`, request parsing, deterministic request codes, app/link/complete PendingIntent helpers.

- [ ] **Step 1: Write failing intent tests**

```kotlin
@Test fun `memory entry intent carries exact id`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val intent = memoryEntryActivityIntent(context, widgetId = 7, slot = 2, entryId = "abc")
    assertTrue(intent.getBooleanExtra(EXTRA_OPEN_MEMORY, false))
    assertEquals("abc", intent.getStringExtra(EXTRA_MEMORY_ENTRY_ID))
}

@Test fun `link intent accepts only http and https`() {
    assertEquals(Intent.ACTION_VIEW, memoryBrowserIntent("https://example.com/")?.action)
    assertNull(memoryBrowserIntent("javascript:alert(1)"))
}

@Test fun `request codes are unique for widget slot and action`() {
    assertNotEquals(memoryWidgetRequestCode(1, 1, 1), memoryWidgetRequestCode(1, 1, 2))
    assertNotEquals(memoryWidgetRequestCode(1, 1, 1), memoryWidgetRequestCode(1, 2, 1))
}
```

- [ ] **Step 2: Verify RED**

```bash
gradle :app:testDebugUnitTest --tests 'com.example.widget.MemoryWidgetIntentsTest' --no-daemon
```

- [ ] **Step 3: Implement deterministic intents**

```kotlin
fun memoryWidgetRequestCode(widgetId: Int, slot: Int, action: Int): Int =
    (widgetId * 100 + slot * 10 + action) and 0x7fffffff
```

`memoryEntryActivityIntent` targets `MainActivity`, sets `FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP`, and carries `EXTRA_OPEN_MEMORY=true` plus the entry ID. Header intent carries only `EXTRA_OPEN_MEMORY=true`. Completion targets `MemoryWidgetProvider` and carries entry ID plus widget ID. Browser intent returns `null` for every scheme except `http` and `https`.

- [ ] **Step 4: Verify GREEN**

```bash
gradle :app:testDebugUnitTest --tests 'com.example.widget.MemoryWidgetIntentsTest' --no-daemon
```

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/widget/MemoryWidgetIntents.kt app/src/test/java/com/example/widget/MemoryWidgetIntentsTest.kt
git commit -m "feat: define Memory widget interaction intents"
```

---

### Task 4: Three-slot RemoteViews widget, image cache, completion, and resize

**Files:**
- Create: `app/src/main/java/com/example/widget/MemoryWidgetImageCache.kt`
- Create: `app/src/main/java/com/example/widget/MemoryWidgetProvider.kt`
- Create: `app/src/main/res/layout/widget_memory.xml`
- Create: `app/src/main/res/drawable/widget_memory_background.xml`
- Create: `app/src/main/res/drawable/widget_memory_slot_background.xml`
- Create: `app/src/main/res/drawable/widget_memory_link_fallback.xml`
- Create: `app/src/test/java/com/example/widget/MemoryWidgetProviderTest.kt`

**Interfaces:**
- Consumes Tasks 1–3.
- Produces `MemoryWidgetProvider.refreshAll(context)` and `MemoryWidgetProvider.updateOne(context, appWidgetId)`.

- [ ] **Step 1: Write failing provider completion/deletion tests**

Use Robolectric with the app Room test setup. Seed one open `MemoryEntryEntity`, broadcast `ACTION_COMPLETE_MEMORY_ENTRY`, wait for the async provider work, then assert `dao.getEntry(id)?.completedAt != null`. Save config for widget IDs 11 and 12, invoke `onDeleted(context, intArrayOf(11))`, then assert ID 11 returns defaults while ID 12 still loads its saved config.

- [ ] **Step 2: Verify RED**

```bash
gradle :app:testDebugUnitTest --tests 'com.example.widget.MemoryWidgetProviderTest' --no-daemon
```

- [ ] **Step 3: Create the fixed three-slot layout**

`widget_memory.xml` contains:

- root `memory_widget_root` with `@drawable/widget_memory_background`;
- header `memory_widget_header` and `memory_widget_title`;
- empty text `memory_widget_empty`;
- slot IDs `memory_slot_1`, `memory_slot_2`, `memory_slot_3`;
- for each N=1..3: `memory_slot_N_image`, `memory_slot_N_site`, `memory_slot_N_title`, `memory_slot_N_body`, `memory_slot_N_check`.

Use only RemoteViews-safe widgets (`LinearLayout`, `FrameLayout`, `ImageView`, `TextView`). Each row uses `@drawable/widget_memory_slot_background`, 12dp padding, white primary text, muted secondary text, and a compact check glyph/text button at the right. Unused slots are hidden with `View.GONE`.

- [ ] **Step 4: Implement preview image cache**

```kotlin
class MemoryWidgetImageCache(private val client: OkHttpClient = defaultClient()) {
    suspend fun load(context: Context, url: String): Bitmap? = withContext(Dispatchers.IO) {
        // implementation uses SHA-256 URL key, cacheDir/memory-widget, cached decode first,
        // then bounded OkHttp download and downsample to at most about 320x220.
    }
}
```

The implementation must use an atomic temp-file rename for successful downloads, 4s connect / 5s read / 7s call timeout, return `null` for non-2xx/empty/undecodable responses, and rethrow `CancellationException`. Replace the explanatory comment above with concrete code before the test is allowed to pass.

- [ ] **Step 5: Implement provider rendering**

`onUpdate` and `onAppWidgetOptionsChanged` run with `goAsync()` + `Dispatchers.IO`. For each widget ID:

1. load `MemoryWidgetConfig`;
2. read `OPTION_APPWIDGET_MIN_HEIGHT`;
3. calculate effective slot count;
4. query `getOpenEntriesForWidget()` and `getCategoriesForWidget()`;
5. call `selectMemoryWidgetEntries`;
6. bind each visible slot and hide remaining slots;
7. call `manager.updateAppWidget(widgetId, views)`.

NOTE binding hides image/site; title=`entry.title`; body=`entry.body.orEmpty()`. LINK binding shows cached preview bitmap if available, otherwise `widget_memory_link_fallback`; site=`previewSiteName ?: Uri.parse(url).host ?: "Link"`; title=`previewTitle ?: title`; body=`previewDescription ?: body ?: ""`. Text click opens exact app entry. Link image click opens browser when valid, otherwise exact app entry. Check click sends completion broadcast.

Completion handling must reload the row from Room and only set completion when `completedAt == null`, using the same timestamp for `completedAt` and `updatedAt`, then call `refreshAll(context)`.

`onDeleted` deletes preferences for only the deleted widget IDs.

- [ ] **Step 6: Verify provider tests and resources**

```bash
gradle :app:testDebugUnitTest --tests 'com.example.widget.MemoryWidgetProviderTest' --no-daemon
gradle :app:processDebugResources --no-daemon
```

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/example/widget/MemoryWidgetImageCache.kt app/src/main/java/com/example/widget/MemoryWidgetProvider.kt app/src/main/res/layout/widget_memory.xml app/src/main/res/drawable/widget_memory_background.xml app/src/main/res/drawable/widget_memory_slot_background.xml app/src/main/res/drawable/widget_memory_link_fallback.xml app/src/test/java/com/example/widget/MemoryWidgetProviderTest.kt
git commit -m "feat: render interactive Memory homescreen widget"
```

---

### Task 5: Configuration activity and widget registration

**Files:**
- Create: `app/src/main/java/com/example/widget/MemoryWidgetConfigActivity.kt`
- Create: `app/src/main/res/xml/memory_widget_info.xml`
- Create: `app/src/test/java/com/example/widget/MemoryWidgetRegistrationTest.kt`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/res/values/strings.xml`

**Interfaces:**
- Consumes Task 1 config persistence and Task 4 `updateOne`.

- [ ] **Step 1: Write failing registration test**

```kotlin
@RunWith(RobolectricTestRunner::class)
class MemoryWidgetRegistrationTest {
    @Test fun `memory widget provider and config activity are registered`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pm = context.packageManager
        val provider = pm.getReceiverInfo(
            ComponentName(context, MemoryWidgetProvider::class.java),
            PackageManager.GET_META_DATA
        )
        val activity = pm.getActivityInfo(
            ComponentName(context, MemoryWidgetConfigActivity::class.java),
            0
        )
        assertEquals(MemoryWidgetProvider::class.java.name, provider.name)
        assertEquals(MemoryWidgetConfigActivity::class.java.name, activity.name)
        assertTrue(provider.metaData.containsKey("android.appwidget.provider"))
    }
}
```

- [ ] **Step 2: Verify RED**

```bash
gradle :app:testDebugUnitTest --tests 'com.example.widget.MemoryWidgetRegistrationTest' --no-daemon
```

- [ ] **Step 3: Add widget metadata**

```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:description="@string/memory_widget_description"
    android:initialLayout="@layout/widget_memory"
    android:minWidth="180dp"
    android:minHeight="110dp"
    android:previewLayout="@layout/widget_memory"
    android:resizeMode="horizontal|vertical"
    android:targetCellWidth="3"
    android:targetCellHeight="2"
    android:configure="com.example.widget.MemoryWidgetConfigActivity"
    android:widgetCategory="home_screen" />
```

Do not set `updatePeriodMillis`; explicit data/config/resize refresh drives updates.

- [ ] **Step 4: Register activity/provider without modifying PicShare registration**

```xml
<activity
    android:name=".widget.MemoryWidgetConfigActivity"
    android:exported="true"
    android:theme="@style/Theme.MyApplication" />

<receiver
    android:name=".widget.MemoryWidgetProvider"
    android:exported="false">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/memory_widget_info" />
</receiver>
```

- [ ] **Step 5: Implement config activity**

The activity immediately calls `setResult(RESULT_CANCELED)`, reads `EXTRA_APPWIDGET_ID`, and finishes for `INVALID_APPWIDGET_ID`. It loads `getOpenEntriesForWidget()` on IO, then renders a HarmonyTheme Compose screen with:

- `Automatisch` / `Bestimmte auswählen` mode toggle;
- 1 / 2 / 3 maximum count;
- pinned-mode list of current open rows with note/link indicator, title and site name;
- maximum 3 selected IDs, kept in tap order;
- save button.

Save exactly:

```kotlin
MemoryWidgetPreferences.save(
    applicationContext,
    appWidgetId,
    MemoryWidgetConfig(mode, maxItems, selectedIds)
)
MemoryWidgetProvider.updateOne(applicationContext, appWidgetId)
setResult(
    RESULT_OK,
    Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
)
finish()
```

- [ ] **Step 6: Add concrete strings**

```xml
<string name="memory_widget_name">Harmony · Merken</string>
<string name="memory_widget_description">Zeigt bis zu drei Dinge, die ihr euch merken möchtet.</string>
<string name="memory_widget_auto">Automatisch</string>
<string name="memory_widget_pinned">Bestimmte auswählen</string>
<string name="memory_widget_count">Anzahl</string>
<string name="memory_widget_save">Widget hinzufügen</string>
<string name="memory_widget_empty">Noch nichts gemerkt · Harmony öffnen</string>
<string name="memory_widget_link">Link</string>
```

- [ ] **Step 7: Verify GREEN and compile resources**

```bash
gradle :app:testDebugUnitTest --tests 'com.example.widget.MemoryWidgetRegistrationTest' --no-daemon
gradle :app:processDebugResources --no-daemon
gradle :app:compileDebugKotlin --no-daemon
```

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/example/widget/MemoryWidgetConfigActivity.kt app/src/main/res/xml/memory_widget_info.xml app/src/test/java/com/example/widget/MemoryWidgetRegistrationTest.kt app/src/main/AndroidManifest.xml app/src/main/res/values/strings.xml
git commit -m "feat: add Memory widget configuration flow"
```

---

### Task 6: Cold/warm app navigation and widget refresh wiring

**Files:**
- Modify: `app/src/main/java/com/example/MainActivity.kt`
- Modify: `app/src/test/java/com/example/widget/MemoryWidgetIntentsTest.kt`
- Modify: `app/src/test/java/com/example/ui/memory/MemoryViewModelTest.kt`

**Interfaces:**
- Consumes Task 3 parsing and Task 2 `openEntryFromWidget`.

- [ ] **Step 1: Extend request parsing tests**

```kotlin
@Test fun `parse returns header exact entry or ordinary launch correctly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertEquals(
        MemoryWidgetOpenRequest(null),
        parseMemoryWidgetOpenRequest(memoryHeaderActivityIntent(context, 2))
    )
    assertEquals(
        MemoryWidgetOpenRequest("abc"),
        parseMemoryWidgetOpenRequest(memoryEntryActivityIntent(context, 2, 1, "abc"))
    )
    assertNull(parseMemoryWidgetOpenRequest(Intent(context, MainActivity::class.java)))
}
```

- [ ] **Step 2: Add cold/warm request state to MainActivity**

```kotlin
private var memoryWidgetOpenRequest by mutableStateOf<MemoryWidgetOpenRequest?>(null)

override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    memoryWidgetOpenRequest = parseMemoryWidgetOpenRequest(intent)
}
```

Initialize the same state from the launch intent in `onCreate`, pass it to `HarmonyApp`, and provide a consumed callback that sets it to `null`.

- [ ] **Step 3: Consume request in HarmonyApp**

```kotlin
LaunchedEffect(memoryWidgetOpenRequest) {
    val request = memoryWidgetOpenRequest ?: return@LaunchedEffect
    viewModel.selectTab(4)
    request.entryId?.let(memoryViewModel::openEntryFromWidget)
    onMemoryWidgetRequestConsumed()
}
```

Header tap opens Memory tab with no editor. Entry tap opens Memory tab and the correct NOTE/LINK editor.

- [ ] **Step 4: Wire app-side refresh callback**

When creating `MemoryViewModelFactory`, pass:

```kotlin
onMemoryChanged = { MemoryWidgetProvider.refreshAll(context.applicationContext) }
```

No Android `Context` is added to `MemoryViewModel`.

- [ ] **Step 5: Verify focused tests and compile**

```bash
gradle :app:testDebugUnitTest --tests 'com.example.widget.MemoryWidgetIntentsTest' --tests 'com.example.ui.memory.MemoryViewModelTest' --no-daemon
gradle :app:compileDebugKotlin --no-daemon
```

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/MainActivity.kt app/src/main/java/com/example/ui/memory/MemoryViewModel.kt app/src/test/java/com/example/widget/MemoryWidgetIntentsTest.kt app/src/test/java/com/example/ui/memory/MemoryViewModelTest.kt
git commit -m "feat: open Memory entries from homescreen widget"
```

---

### Task 7: Full regression verification and merge readiness

**Files:**
- Review only files changed by Tasks 1–6.

**Interfaces:**
- Produces a verified feature branch ready for PR/merge to `main`.

- [ ] **Step 1: Run all new widget and MemoryViewModel tests**

```bash
gradle :app:testDebugUnitTest --tests 'com.example.widget.*' --tests 'com.example.ui.memory.MemoryViewModelTest' --no-daemon
```

Expected: PASS.

- [ ] **Step 2: Run existing Memory regression tests**

```bash
gradle :app:testDebugUnitTest --tests 'com.example.ui.MemoryPinboardUiTest' --tests 'com.example.ui.MemoryPinboardScreenshotTest' --tests 'com.example.data.db.MemoryMigrationTest' --no-daemon
```

Expected: PASS. Do not update screenshot baselines because the in-app Memory screen is not intentionally redesigned by this feature.

- [ ] **Step 3: Verify Android resources and Kotlin compilation**

```bash
gradle :app:processDebugResources --no-daemon
gradle :app:compileDebugKotlin --no-daemon
```

Expected: PASS.

- [ ] **Step 4: Verify both widgets remain registered**

Confirm the merged manifest contains `.widget.PicShareWidgetProvider` and `.widget.MemoryWidgetProvider`, and `memory_widget_info.xml` names `MemoryWidgetConfigActivity`.

- [ ] **Step 5: Device/emulator acceptance pass**

Verify all 11 behaviors:

1. Widget picker shows Harmony Memory widget.
2. Configuration opens; defaults are automatic + 3.
3. Small/medium/tall sizes show 1/2/3 entries.
4. Pinned selection allows at most 3 and preserves tap order.
5. Completed/deleted pinned item is backfilled by next newest open item.
6. Note text opens exact note in Harmony.
7. Link image opens browser.
8. Link text opens exact link entry in Harmony.
9. Check completes without opening Harmony.
10. Missing/offline preview image shows branded fallback.
11. Existing PicShare widget still works.

- [ ] **Step 6: Scope review**

Run `git diff main...HEAD --stat` and `git diff main...HEAD`. Confirm there is no unrelated refactor, no new widget framework dependency, and no second Memory datastore.

If any verification step fails, return to the owning task, add a failing regression test for the exact defect, implement the minimal fix, rerun that task’s tests, and commit that focused fix before repeating Task 7. Do not patch failures only in the verification task.
