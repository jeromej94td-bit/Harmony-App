# Memory Homescreen Widget Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a configurable Harmony homescreen widget that shows 1–3 open “Das müssen wir uns merken” entries, supports pinned or automatic selection, shows compact URL previews, opens links or the exact Harmony entry, and marks items completed directly from the homescreen.

**Architecture:** Reuse the existing Room-backed Memory data and the app’s existing classic `AppWidgetProvider`/`RemoteViews` pattern. Keep widget configuration in per-widget `SharedPreferences`, keep Memory content only in Room, render three fixed RemoteViews slots with size-based visibility, and route app-open actions through `MainActivity` extras into the existing Memory tab/editor state.

**Tech Stack:** Kotlin, Android AppWidgetProvider/RemoteViews, Room, SharedPreferences, OkHttp, Jetpack Compose for the widget configuration activity, JUnit/Robolectric, Gradle Android resource processing.

**Spec:** `docs/superpowers/specs/2026-08-23-memory-home-widget-design.md`

## Global Constraints

- Keep `minSdk = 24`, `targetSdk = 36`; do not introduce Jetpack Glance.
- Reuse Room `memory_entries` and existing preview metadata; do not introduce a second Memory datastore.
- Preserve the existing `PicShareWidgetProvider` behavior and registration.
- A widget shows at most 3 entries.
- Default mode is automatic; pinned mode allows up to 3 ordered IDs.
- Missing/completed/deleted pinned IDs are backfilled from newest open entries without duplicates.
- Link-preview tap opens the browser; entry text tap opens the exact Harmony Memory entry; check tap completes without opening Harmony.
- Remote image failure must render a branded fallback and never make the widget unusable.
- Widget writes and app writes must refresh all Memory widgets.

---

## File Structure

### New files

- `app/src/main/java/com/example/widget/MemoryWidgetLogic.kt` — pure configuration/selection/size logic.
- `app/src/main/java/com/example/widget/MemoryWidgetPreferences.kt` — per-widget SharedPreferences persistence.
- `app/src/main/java/com/example/widget/MemoryWidgetIntents.kt` — unique PendingIntent/request-code construction and app-open request parsing.
- `app/src/main/java/com/example/widget/MemoryWidgetImageCache.kt` — small URL-preview bitmap cache/downloader.
- `app/src/main/java/com/example/widget/MemoryWidgetProvider.kt` — widget lifecycle, rendering, completion action, refresh.
- `app/src/main/java/com/example/widget/MemoryWidgetConfigActivity.kt` — Harmony-styled configuration UI.
- `app/src/main/res/layout/widget_memory.xml` — fixed three-slot RemoteViews layout.
- `app/src/main/res/drawable/widget_memory_background.xml` — dark glass-like rounded shell.
- `app/src/main/res/drawable/widget_memory_slot_background.xml` — row surface.
- `app/src/main/res/drawable/widget_memory_link_fallback.xml` — link preview fallback.
- `app/src/main/res/xml/memory_widget_info.xml` — widget metadata/configuration activity.
- `app/src/test/java/com/example/widget/MemoryWidgetLogicTest.kt` — pure selection/size tests.
- `app/src/test/java/com/example/widget/MemoryWidgetPreferencesTest.kt` — persistence/clamping tests.
- `app/src/test/java/com/example/widget/MemoryWidgetIntentsTest.kt` — app/link/complete intent tests.
- `app/src/test/java/com/example/widget/MemoryWidgetProviderTest.kt` — provider completion/render-state tests with Robolectric where practical.

### Modified files

- `app/src/main/java/com/example/data/db/MemoryDao.kt` — add widget snapshot queries.
- `app/src/main/java/com/example/ui/memory/MemoryViewModel.kt` — add widget refresh callback after successful Memory mutations and widget-entry open helper.
- `app/src/main/java/com/example/ui/memory/MemoryViewModelFactory.kt` does not exist; update the existing factory at the bottom of `MemoryViewModel.kt`.
- `app/src/main/java/com/example/MainActivity.kt` — consume widget open requests and switch to Memory tab/exact entry.
- `app/src/main/AndroidManifest.xml` — register provider/config activity.
- `app/src/main/res/values/strings.xml` — widget picker/config/empty-state strings.
- `app/src/test/java/com/example/ui/memory/MemoryViewModelTest.kt` — refresh/open-entry behavior.

---

### Task 1: Pure widget selection, size logic, and configuration persistence

**Files:**
- Create: `app/src/main/java/com/example/widget/MemoryWidgetLogic.kt`
- Create: `app/src/main/java/com/example/widget/MemoryWidgetPreferences.kt`
- Create: `app/src/test/java/com/example/widget/MemoryWidgetLogicTest.kt`
- Create: `app/src/test/java/com/example/widget/MemoryWidgetPreferencesTest.kt`

**Interfaces:**
- Produces: `enum class MemoryWidgetMode { AUTOMATIC, PINNED }`
- Produces: `data class MemoryWidgetConfig(val mode: MemoryWidgetMode, val maxItems: Int, val pinnedIds: List<String>)`
- Produces: `fun effectiveMemoryWidgetSlots(minHeightDp: Int, configuredMax: Int): Int`
- Produces: `fun selectMemoryWidgetEntries(openEntries: List<MemoryEntryEntity>, config: MemoryWidgetConfig, slotCount: Int): List<MemoryEntryEntity>`
- Produces: `object MemoryWidgetPreferences` with `load`, `save`, and `delete`.

- [ ] **Step 1: Write failing selection/size tests**

```kotlin
class MemoryWidgetLogicTest {
    @Test fun `height maps conservatively to one two three slots`() {
        assertEquals(1, effectiveMemoryWidgetSlots(179, 3))
        assertEquals(2, effectiveMemoryWidgetSlots(180, 3))
        assertEquals(2, effectiveMemoryWidgetSlots(279, 3))
        assertEquals(3, effectiveMemoryWidgetSlots(280, 3))
        assertEquals(1, effectiveMemoryWidgetSlots(500, 1))
    }

    @Test fun `automatic mode uses newest open order`() {
        val entries = listOf(entry("new", 300), entry("mid", 200), entry("old", 100))
        val result = selectMemoryWidgetEntries(entries, MemoryWidgetConfig(MemoryWidgetMode.AUTOMATIC, 3, emptyList()), 2)
        assertEquals(listOf("new", "mid"), result.map { it.id })
    }

    @Test fun `pinned mode preserves order and backfills missing ids`() {
        val entries = listOf(entry("new", 300), entry("pin-b", 200), entry("pin-a", 100))
        val config = MemoryWidgetConfig(MemoryWidgetMode.PINNED, 3, listOf("pin-a", "missing", "pin-b"))
        val result = selectMemoryWidgetEntries(entries, config, 3)
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

- [ ] **Step 2: Run the tests and verify RED**

Run:

```bash
gradle :app:testDebugUnitTest --tests 'com.example.widget.MemoryWidgetLogicTest' --no-daemon
```

Expected: compilation failure because the widget logic types/functions do not exist yet.

- [ ] **Step 3: Implement minimal pure logic**

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
    val limit = minOf(slotCount.coerceIn(1, 3), config.maxItems.coerceIn(1, 3))
    if (config.mode == MemoryWidgetMode.AUTOMATIC) return openEntries.take(limit)
    val byId = openEntries.associateBy { it.id }
    val pinned = config.pinnedIds.distinct().take(3).mapNotNull(byId::get)
    val used = pinned.mapTo(mutableSetOf()) { it.id }
    return (pinned + openEntries.filter { used.add(it.id) }).take(limit)
}
```

- [ ] **Step 4: Add SharedPreferences tests**

Use Robolectric `ApplicationProvider.getApplicationContext<Context>()` and verify:

```kotlin
MemoryWidgetPreferences.save(context, 42, MemoryWidgetConfig(MemoryWidgetMode.PINNED, 9, listOf("a", "a", "b", "c", "d")))
assertEquals(
    MemoryWidgetConfig(MemoryWidgetMode.PINNED, 3, listOf("a", "b", "c")),
    MemoryWidgetPreferences.load(context, 42)
)
MemoryWidgetPreferences.delete(context, 42)
assertEquals(MemoryWidgetConfig(), MemoryWidgetPreferences.load(context, 42))
```

Implementation must use one private preferences file, keys prefixed by `appWidgetId`, and persist pinned IDs as a delimiter-safe `StringSet` plus a separate ordered string or JSON-free newline encoding; preserve order explicitly.

- [ ] **Step 5: Run both test classes and verify GREEN**

```bash
gradle :app:testDebugUnitTest --tests 'com.example.widget.MemoryWidgetLogicTest' --tests 'com.example.widget.MemoryWidgetPreferencesTest' --no-daemon
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/widget/MemoryWidgetLogic.kt app/src/main/java/com/example/widget/MemoryWidgetPreferences.kt app/src/test/java/com/example/widget/MemoryWidgetLogicTest.kt app/src/test/java/com/example/widget/MemoryWidgetPreferencesTest.kt
git commit -m "feat: add Memory widget selection and config logic"
```

---

### Task 2: Room snapshot queries and Memory mutation refresh hook

**Files:**
- Modify: `app/src/main/java/com/example/data/db/MemoryDao.kt`
- Modify: `app/src/main/java/com/example/ui/memory/MemoryViewModel.kt`
- Modify: `app/src/test/java/com/example/ui/memory/MemoryViewModelTest.kt`

**Interfaces:**
- Produces: `suspend fun getOpenEntriesForWidget(): List<MemoryEntryEntity>` on `MemoryDao`.
- Produces: `suspend fun getCategoriesForWidget(): List<MemoryCategoryEntity>` on `MemoryDao`.
- Produces: `MemoryViewModel(..., onMemoryChanged: () -> Unit = {})`.
- Produces: `fun openEntryFromWidget(entryId: String)`.

- [ ] **Step 1: Write failing DAO/query expectations and ViewModel callback tests**

Add ViewModel tests using the existing fake repository pattern:

```kotlin
@Test fun `successful complete invokes widget refresh callback`() = runTest {
    var refreshes = 0
    val vm = createViewModel(onMemoryChanged = { refreshes++ })
    vm.complete("entry-1")
    advanceUntilIdle()
    assertEquals(1, refreshes)
}

@Test fun `open entry from widget clears filters and opens correct editor mode`() = runTest {
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

- [ ] **Step 2: Verify RED**

```bash
gradle :app:testDebugUnitTest --tests 'com.example.ui.memory.MemoryViewModelTest' --no-daemon
```

Expected: compile/test failure because the callback/open method are absent.

- [ ] **Step 3: Add widget snapshot queries**

Append to `MemoryDao`:

```kotlin
@Query("SELECT * FROM memory_entries WHERE completedAt IS NULL ORDER BY updatedAt DESC, createdAt DESC")
suspend fun getOpenEntriesForWidget(): List<MemoryEntryEntity>

@Query("SELECT * FROM memory_categories ORDER BY sortOrder, createdAt")
suspend fun getCategoriesForWidget(): List<MemoryCategoryEntity>
```

- [ ] **Step 4: Add refresh callback and exact-entry opener**

Change constructor/factory signatures to carry `onMemoryChanged: () -> Unit = {}`. Invoke it only after successful database writes in:

- `saveNote`
- `saveList` when at least one entry was inserted
- `saveLink`
- link preview success/failure after stored preview state changes
- `createCategory`
- `updateCategory`
- `deleteCategory`
- `complete`
- `restore`
- `confirmPermanentDelete`

Add:

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

Do not use `LIST` for existing individual rows.

- [ ] **Step 5: Run tests and verify GREEN**

```bash
gradle :app:testDebugUnitTest --tests 'com.example.ui.memory.MemoryViewModelTest' --no-daemon
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/data/db/MemoryDao.kt app/src/main/java/com/example/ui/memory/MemoryViewModel.kt app/src/test/java/com/example/ui/memory/MemoryViewModelTest.kt
git commit -m "feat: expose Memory widget snapshots and refresh hooks"
```

---

### Task 3: Intent contract and exact app/link/complete actions

**Files:**
- Create: `app/src/main/java/com/example/widget/MemoryWidgetIntents.kt`
- Create: `app/src/test/java/com/example/widget/MemoryWidgetIntentsTest.kt`

**Interfaces:**
- Produces constants: `EXTRA_OPEN_MEMORY`, `EXTRA_MEMORY_ENTRY_ID`, `ACTION_COMPLETE_MEMORY_ENTRY`, `EXTRA_APP_WIDGET_ID`.
- Produces: `data class MemoryWidgetOpenRequest(val entryId: String?)`.
- Produces: `fun parseMemoryWidgetOpenRequest(intent: Intent?): MemoryWidgetOpenRequest?`.
- Produces helpers returning `PendingIntent` for header, entry text, link preview, completion.

- [ ] **Step 1: Write failing intent tests**

```kotlin
@Test fun `entry app intent carries exact memory id`() {
    val intent = memoryEntryIntent(context, widgetId = 7, slot = 2, entryId = "abc")
    assertTrue(intent.getBooleanExtra(EXTRA_OPEN_MEMORY, false))
    assertEquals("abc", intent.getStringExtra(EXTRA_MEMORY_ENTRY_ID))
}

@Test fun `valid link intent uses ACTION_VIEW`() {
    val intent = memoryLinkIntent("https://example.com/path")
    assertEquals(Intent.ACTION_VIEW, intent.action)
    assertEquals("https://example.com/path", intent.data.toString())
}

@Test fun `pending request codes differ by widget slot and action`() {
    assertNotEquals(memoryWidgetRequestCode(1, 1, 1), memoryWidgetRequestCode(1, 1, 2))
    assertNotEquals(memoryWidgetRequestCode(1, 1, 1), memoryWidgetRequestCode(1, 2, 1))
}
```

- [ ] **Step 2: Verify RED**

```bash
gradle :app:testDebugUnitTest --tests 'com.example.widget.MemoryWidgetIntentsTest' --no-daemon
```

- [ ] **Step 3: Implement intent contract**

Use deterministic request codes:

```kotlin
fun memoryWidgetRequestCode(widgetId: Int, slot: Int, action: Int): Int =
    (widgetId * 100 + slot * 10 + action) and 0x7fffffff
```

App-open intents must target `MainActivity`, set `Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP`, and carry `EXTRA_OPEN_MEMORY=true`. Link intent must accept only `http`/`https` URIs; otherwise return `null` so the provider leaves preview click bound to app-open rather than browser-open.

Completion PendingIntent must target `MemoryWidgetProvider` with `ACTION_COMPLETE_MEMORY_ENTRY`, `entryId`, and widget ID.

- [ ] **Step 4: Run tests and verify GREEN**

```bash
gradle :app:testDebugUnitTest --tests 'com.example.widget.MemoryWidgetIntentsTest' --no-daemon
```

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/widget/MemoryWidgetIntents.kt app/src/test/java/com/example/widget/MemoryWidgetIntentsTest.kt
git commit -m "feat: define Memory widget interaction intents"
```

---

### Task 4: RemoteViews layout, image cache, provider rendering, completion, and resize behavior

**Files:**
- Create: `app/src/main/java/com/example/widget/MemoryWidgetImageCache.kt`
- Create: `app/src/main/java/com/example/widget/MemoryWidgetProvider.kt`
- Create: `app/src/main/res/layout/widget_memory.xml`
- Create: `app/src/main/res/drawable/widget_memory_background.xml`
- Create: `app/src/main/res/drawable/widget_memory_slot_background.xml`
- Create: `app/src/main/res/drawable/widget_memory_link_fallback.xml`
- Create: `app/src/test/java/com/example/widget/MemoryWidgetProviderTest.kt`

**Interfaces:**
- Consumes Task 1 logic/preferences, Task 2 DAO queries, Task 3 intents.
- Produces: `class MemoryWidgetProvider : AppWidgetProvider()`.
- Produces: `fun refreshAll(context: Context)` companion helper.
- Produces three fixed row slots with unique IDs.

- [ ] **Step 1: Write provider behavior tests before implementation**

Cover at minimum:

```kotlin
@Test fun `complete action marks only requested open entry complete`() { /* seed Room, send provider broadcast, assert completedAt set */ }
@Test fun `deleted widget removes only its config`() { /* save configs for 1 and 2, delete 1, assert 2 remains */ }
@Test fun `small widget selection renders one item`() { /* options minHeight 150, config max 3 */ }
```

Use Robolectric, real in-memory Room where existing test utilities permit, and avoid asserting pixel output.

- [ ] **Step 2: Verify RED**

```bash
gradle :app:testDebugUnitTest --tests 'com.example.widget.MemoryWidgetProviderTest' --no-daemon
```

- [ ] **Step 3: Add fixed three-slot XML**

`widget_memory.xml` must contain one header and three explicitly duplicated slot groups with unique IDs:

- Slot 1: `memory_slot_1`, `memory_slot_1_image`, `memory_slot_1_site`, `memory_slot_1_title`, `memory_slot_1_body`, `memory_slot_1_check`.
- Slot 2: same suffix `_2_*`.
- Slot 3: same suffix `_3_*`.

Use `LinearLayout`/`FrameLayout`/`ImageView`/`TextView` only, rounded drawable backgrounds, 12–16dp internal spacing, white primary text, muted secondary text, pink-violet accent. Keep all three slots in the base layout and hide unused slots with `View.GONE` from `RemoteViews`.

- [ ] **Step 4: Implement bounded image cache**

`MemoryWidgetImageCache` must:

```kotlin
suspend fun load(context: Context, url: String): Bitmap?
```

Rules:

- SHA-256 hash URL into `cacheDir/memory-widget/<hash>.webp`.
- Decode cached file first with bounds/downsampling; cap roughly 320×220.
- Otherwise fetch with OkHttp using connect/read/call timeouts of about 4/5/7 seconds.
- Reject non-2xx, empty body, and undecodable image.
- Save successful bytes atomically, then decode downsampled bitmap.
- Return `null` on ordinary network/image failures; rethrow coroutine cancellation.

No preview metadata requests occur here; only stored `previewImageUrl` is fetched.

- [ ] **Step 5: Implement provider rendering**

`onUpdate` and `onAppWidgetOptionsChanged` must use `goAsync()`/IO coroutines, load config, read `OPTION_APPWIDGET_MIN_HEIGHT`, calculate slots, query open entries/categories, call selection logic, then bind rows.

For each row:

- NOTE: hide image + site; title=`entry.title`; body=`entry.body.orEmpty()`; row text click = app-open exact entry; check = completion broadcast.
- LINK: show image if cache returns bitmap else `widget_memory_link_fallback`; site=`previewSiteName ?: Uri.host ?: "Link"`; title=`previewTitle ?: title`; body=`previewDescription ?: body ?: ""`; image click = browser PendingIntent when URL valid, otherwise app-open; text click = app-open exact entry; check = completion.
- Empty dataset: hide all slots and show one empty-state text in the root layout.

Completion action handling:

```kotlin
if (intent.action == ACTION_COMPLETE_MEMORY_ENTRY) {
    val id = intent.getStringExtra(EXTRA_MEMORY_ENTRY_ID) ?: return
    val pending = goAsync()
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val dao = HarmonyDatabase.getInstance(context).memoryDao()
            val entry = dao.getEntry(id)
            if (entry != null && entry.completedAt == null) {
                val now = System.currentTimeMillis()
                dao.setCompletedAt(id, now, now)
            }
            refreshAll(context)
        } finally { pending.finish() }
    }
}
```

`onDeleted` removes per-widget preferences. `refreshAll` mirrors the existing PicShare update-broadcast pattern.

- [ ] **Step 6: Run tests and resource processing**

```bash
gradle :app:testDebugUnitTest --tests 'com.example.widget.MemoryWidgetProviderTest' --no-daemon
gradle :app:processDebugResources --no-daemon
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/example/widget/MemoryWidgetImageCache.kt app/src/main/java/com/example/widget/MemoryWidgetProvider.kt app/src/main/res/layout/widget_memory.xml app/src/main/res/drawable/widget_memory_background.xml app/src/main/res/drawable/widget_memory_slot_background.xml app/src/main/res/drawable/widget_memory_link_fallback.xml app/src/test/java/com/example/widget/MemoryWidgetProviderTest.kt
git commit -m "feat: render interactive Memory homescreen widget"
```

---

### Task 5: Widget configuration activity and Android registration

**Files:**
- Create: `app/src/main/java/com/example/widget/MemoryWidgetConfigActivity.kt`
- Create: `app/src/main/res/xml/memory_widget_info.xml`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/res/values/strings.xml`

**Interfaces:**
- Consumes `MemoryWidgetPreferences.save` and `MemoryWidgetProvider.refreshAll`/direct widget update.
- Produces launcher widget picker entry and config UI.

- [ ] **Step 1: Add failing manifest/resource contract test or verification script assertion**

At minimum add a focused unit/resource test that parses the merged manifest under Robolectric or use an existing resource test pattern to assert that `MemoryWidgetProvider` and `MemoryWidgetConfigActivity` resolve. The expected provider metadata must point to `@xml/memory_widget_info`.

- [ ] **Step 2: Implement `memory_widget_info.xml`**

Use:

```xml
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

Do not add a periodic refresh interval; data changes and resize/config updates drive refresh.

- [ ] **Step 3: Register provider and config activity**

Add inside `<application>` while leaving PicShare unchanged:

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

- [ ] **Step 4: Implement configuration activity**

On create:

```kotlin
private val appWidgetId by lazy {
    intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        ?: AppWidgetManager.INVALID_APPWIDGET_ID
}
```

Set `RESULT_CANCELED` immediately. If ID invalid, finish.

Load `dao.getOpenEntriesForWidget()` on IO into Compose state. Render HarmonyTheme screen with:

- segmented `Automatisch` / `Bestimmte auswählen`;
- count buttons 1/2/3;
- pinned mode list of open entries, each row showing note/link icon, preview/title and site name where available;
- selection counter `x / 3`;
- enforce max 3 by refusing fourth selection;
- save button.

Save action:

```kotlin
MemoryWidgetPreferences.save(context, appWidgetId, MemoryWidgetConfig(mode, count, selectedIds))
MemoryWidgetProvider.updateOne(context, appWidgetId)
setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
finish()
```

`updateOne` can be a provider companion helper that triggers/update-renders one widget; add it in Task 4 if not already present.

- [ ] **Step 5: Add strings**

Add concrete resource strings for widget name/description, auto/pinned, count, selection counter, save/add, empty state, fallback site `Link`. Avoid touching unrelated localization catalogs in this feature.

- [ ] **Step 6: Verify resource build**

```bash
gradle :app:processDebugResources --no-daemon
gradle :app:compileDebugKotlin --no-daemon
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/example/widget/MemoryWidgetConfigActivity.kt app/src/main/res/xml/memory_widget_info.xml app/src/main/AndroidManifest.xml app/src/main/res/values/strings.xml
git commit -m "feat: add Memory widget configuration flow"
```

---

### Task 6: Deep-link-style navigation into exact Memory entry

**Files:**
- Modify: `app/src/main/java/com/example/MainActivity.kt`
- Modify: `app/src/test/java/com/example/widget/MemoryWidgetIntentsTest.kt`
- Modify: `app/src/test/java/com/example/ui/memory/MemoryViewModelTest.kt` if additional state assertions are needed.

**Interfaces:**
- Consumes `parseMemoryWidgetOpenRequest` and `MemoryViewModel.openEntryFromWidget`.
- Produces correct handling for cold start and `onNewIntent` while app is alive.

- [ ] **Step 1: Add failing cold/warm intent parsing behavior tests**

Ensure request parsing returns:

```kotlin
MemoryWidgetOpenRequest(entryId = null) // header tap
MemoryWidgetOpenRequest(entryId = "abc") // entry tap
null // ordinary app launch
```

Add a MainActivity Robolectric test if current project test setup supports activity launch reliably; otherwise keep parsing pure and verify activity wiring through compile/resource tests plus focused code review.

- [ ] **Step 2: Add activity request state**

In `MainActivity` add a Compose-observable member state:

```kotlin
private var memoryWidgetOpenRequest by mutableStateOf<MemoryWidgetOpenRequest?>(null)
```

In `onCreate`, parse `intent`. In `onNewIntent`, call `super`, `setIntent(intent)`, and replace the state with the parsed request.

Pass request and an `onMemoryWidgetRequestConsumed` callback into `HarmonyApp`.

- [ ] **Step 3: Consume request inside `HarmonyApp`**

Add:

```kotlin
LaunchedEffect(memoryWidgetOpenRequest) {
    val request = memoryWidgetOpenRequest ?: return@LaunchedEffect
    viewModel.selectTab(4)
    request.entryId?.let(memoryViewModel::openEntryFromWidget)
    onMemoryWidgetRequestConsumed()
}
```

Header tap therefore opens tab 4 without an editor; entry tap opens tab 4 and the correct NOTE/LINK editor state.

- [ ] **Step 4: Wire MemoryViewModel refresh callback from MainActivity**

When creating `MemoryViewModelFactory`, pass:

```kotlin
onMemoryChanged = { MemoryWidgetProvider.refreshAll(context.applicationContext) }
```

This connects app-side mutations to widgets without adding Android Context to the ViewModel.

- [ ] **Step 5: Run focused and compile tests**

```bash
gradle :app:testDebugUnitTest --tests 'com.example.widget.MemoryWidgetIntentsTest' --tests 'com.example.ui.memory.MemoryViewModelTest' --no-daemon
gradle :app:compileDebugKotlin --no-daemon
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/MainActivity.kt app/src/main/java/com/example/ui/memory/MemoryViewModel.kt app/src/test/java/com/example/widget/MemoryWidgetIntentsTest.kt app/src/test/java/com/example/ui/memory/MemoryViewModelTest.kt
git commit -m "feat: open Memory entries from homescreen widget"
```

---

### Task 7: End-to-end regression verification and merge readiness

**Files:**
- Review all files changed by Tasks 1–6.
- No new production feature files unless verification exposes a defect.

**Interfaces:**
- Produces a branch ready for PR/merge to `main`.

- [ ] **Step 1: Run all focused Memory/widget unit tests**

```bash
gradle :app:testDebugUnitTest --tests 'com.example.widget.*' --tests 'com.example.ui.memory.MemoryViewModelTest' --no-daemon
```

Expected: PASS.

- [ ] **Step 2: Run existing Memory tests to guard regressions**

```bash
gradle :app:testDebugUnitTest --tests 'com.example.ui.MemoryPinboardUiTest' --tests 'com.example.ui.MemoryPinboardScreenshotTest' --tests 'com.example.data.db.MemoryMigrationTest' --no-daemon
```

Expected: PASS. If screenshot tests are environment-sensitive, record the exact failure and do not overwrite baselines unless the Memory app screen itself intentionally changed.

- [ ] **Step 3: Verify Android resources and Kotlin compilation**

```bash
gradle :app:processDebugResources --no-daemon
gradle :app:compileDebugKotlin --no-daemon
```

Expected: PASS.

- [ ] **Step 4: Verify manifest preserves PicShare and adds Memory widget**

Check merged manifest/resource output contains both `.widget.PicShareWidgetProvider` and `.widget.MemoryWidgetProvider`, and that `memory_widget_info.xml` references the new config activity.

- [ ] **Step 5: Manual behavior checklist on emulator/device**

Verify exactly:

1. Widget picker shows Harmony Memory widget.
2. Config opens; default is automatic/3.
3. Automatic small/medium/tall sizes show 1/2/3 entries.
4. Pinned selection maxes at three and preserves order.
5. Completing a pinned item removes it and backfills next newest open item.
6. Note text tap opens Memory tab and the note editor.
7. Link image tap opens browser.
8. Link text tap opens Harmony link entry.
9. Check tap completes without opening Harmony.
10. Link preview image fallback is polished when image is absent/offline.
11. Existing PicShare widget still updates and opens Harmony.

- [ ] **Step 6: Review diff for scope**

Confirm no unrelated files changed and no second Memory datastore/dependency was introduced.

- [ ] **Step 7: Final commit if verification-only fixes were needed**

```bash
git add <only files changed to fix verified defects>
git commit -m "fix: harden Memory homescreen widget verification"
```

If no fixes were needed, do not create an empty commit.
