# Memory Homescreen Widget Design

## Goal

Add a dedicated Android homescreen widget for Harmony's **„Das müssen wir uns merken“** feature. The widget surfaces 1–3 current/open memory entries and lets the user interact with them without duplicating the app's data model.

The widget must reuse the existing Room-backed `memory_entries` data and existing link-preview metadata (`previewTitle`, `previewDescription`, `previewImageUrl`, `previewSiteName`) rather than introducing a second store.

## Decisions approved by the user

- Widget content mode: **hybrid**.
  - Default: automatically show the newest open entries.
  - Optional: the user can pin 1–3 specific open entries when configuring the widget.
- Link interaction:
  - tapping the small link preview opens the URL directly;
  - tapping the entry title/text opens the corresponding memory entry in Harmony.
- Completion interaction:
  - every entry exposes a small check action;
  - tapping it marks the entry completed directly from the homescreen.
- Widget technology: classic Android `AppWidgetProvider` + `RemoteViews`, consistent with the existing `PicShareWidgetProvider` and compatible with the current minSdk 24.

## Scope

### In scope

1. A new `MemoryWidgetProvider` registered in the manifest.
2. A widget configuration flow for choosing:
   - automatic vs pinned mode;
   - desired maximum visible item count: 1, 2, or 3;
   - up to 3 specific open entries in pinned mode.
3. Adaptive rendering based on widget height:
   - compact size: 1 item;
   - medium size: up to 2 items;
   - large size: up to 3 items;
   - never exceed the configured maximum.
4. Note presentation with title plus short body preview.
5. Link presentation with compact preview image or branded fallback, site name, title and optional short note/description.
6. Direct check action to complete an entry.
7. Deep-link-style navigation into Harmony's Memory tab and, for a specific entry, opening/focusing that entry.
8. Direct browser opening from the link-preview affordance.
9. Automatic widget refresh when memory data changes in relevant flows (create/update/preview loaded/complete/restore/delete).
10. Tests around selection, rendering decisions, completion, deep-link intent generation and fallback behavior.

### Out of scope

- Editing note text directly inside the widget.
- Creating a new note directly inside the widget.
- Synchronizing data to a second widget-only database.
- Reimplementing link metadata extraction inside the widget.
- Replacing or modifying `PicShareWidgetProvider`.

## Existing system reused

Harmony already stores memory entries in Room through `MemoryRepository` / `MemoryDao`. A memory entry can be `NOTE` or `LINK`. Link rows already contain the metadata required for the widget preview.

`MemoryViewModel` already owns the app-side completion semantics through `setCompleted`, and the Room database is the source of truth. The widget provider will write through the same database/DAO layer (or a small repository-facing widget helper) so completion state remains consistent.

Harmony also already has `PicShareWidgetProvider`, which provides a working precedent for widget registration, asynchronous database access and pin requests.

## Data selection rules

### Automatic mode

For a configured widget:

1. Query current/open memory entries.
2. Order by `updatedAt DESC`, then `createdAt DESC`, matching the current Memory tab's preference for recently changed open items.
3. Limit by both:
   - the configured maximum (1–3), and
   - the number of rows allowed by the current widget size.

### Pinned mode

Store an ordered list of up to 3 memory entry IDs per widget ID using lightweight widget preferences.

When rendering:

1. Resolve pinned IDs in their configured order.
2. Ignore entries that are deleted or no longer open.
3. Fill any resulting empty slots from the automatic newest-open list, excluding IDs already shown.
4. Respect configured max and size-based max.

This prevents blank gaps after a pinned item is completed or deleted.

### Widget-specific configuration storage

Use per-widget `SharedPreferences` keyed by `appWidgetId` for:

- mode: automatic / pinned;
- configured max visible count: 1–3;
- ordered pinned entry IDs.

The actual memory content remains only in Room.

## UI design

Use Harmony's existing dark glass / pink-violet visual language. `RemoteViews` limits dynamic Compose-like visuals, so the design should prioritize clean hierarchy over effects that are unreliable in launcher widgets.

### Widget shell

- dark translucent-looking rounded background asset / shape;
- pink-violet accent line or header treatment;
- compact header: Memory/Bookmark icon + **„Das müssen wir uns merken“**;
- tapping header opens Memory tab.

### Note row

- category/accent marker;
- title: 1–2 lines;
- body snippet when space allows: 1–2 lines;
- small completion check affordance on the right;
- tapping main text opens the exact entry in Harmony.

### Link row

- small preview image on the left when available;
- fallback graphic when the image is missing/unavailable;
- site name in small accent text;
- preview title;
- optional one-line note/description when height permits;
- completion check on the right;
- tapping preview image opens URL in browser;
- tapping title/text opens the exact entry in Harmony.

### Size behavior

The widget must use `AppWidgetManager` options / min dimensions to determine an effective slot count. The exact launcher cell dimensions vary by manufacturer, so the renderer should use conservative thresholds and never cram 3 entries into a small height.

Expected behavior:

- small: 1 item;
- medium: 2 items;
- tall: 3 items.

The user may configure a lower maximum, which always wins.

## Link preview image handling

`RemoteViews` needs a local bitmap. The existing `previewImageUrl` can point to a remote image, so the widget layer will use a small cache dedicated to already-resolved preview images.

Rules:

1. Do not refetch metadata; only use stored `previewImageUrl`.
2. If the image is cached, decode a downsampled bitmap suitable for the widget.
3. If not cached, fetch the image asynchronously with the app's existing network stack or a minimal OkHttp request, resize/downsample it, save it to app cache, then refresh the widget.
4. On network/image failure, render the branded Harmony link fallback immediately.
5. Never block the widget update waiting indefinitely for an image.

Cache files are disposable and can be regenerated from `previewImageUrl`.

## Interaction model

### Complete button

Each row gets a unique broadcast `PendingIntent` carrying `entryId` and `appWidgetId`.

On click:

1. `MemoryWidgetProvider` receives the action.
2. In an IO coroutine, load the entry and mark it completed with current time if still open.
3. Refresh all Memory widgets.
4. The completed row disappears and pinned mode fills the free slot according to the selection rules.

### Open memory entry

Create an activity `PendingIntent` to `MainActivity` with intent extras such as:

- `EXTRA_OPEN_MEMORY = true`
- `EXTRA_MEMORY_ENTRY_ID = <id>`

`MainActivity` / app state handling will consume these extras once and switch to Memory tab (`selectedTab = 4`). For a specific entry, the Memory layer will open/focus the corresponding entry using the existing editor/navigation state rather than starting a duplicate screen.

### Open link

The preview-image click uses `ACTION_VIEW` with the stored normalized `http/https` URL. Only valid normalized URLs already stored by the Memory feature are used.

## Refresh strategy

The widget should refresh on:

- app widget `onUpdate` / resize;
- widget configuration completion;
- memory note creation/update;
- link creation/update;
- link preview resolution success/failure if visible metadata changes;
- complete/restore;
- permanent delete;
- category changes only if they affect visible labels/accents.

A small `MemoryWidgetProvider.refreshAll(context)` helper will broadcast `ACTION_APPWIDGET_UPDATE` for all Memory widget IDs, mirroring the existing PicShare widget pattern.

App-side Memory write paths will call this helper after successful mutations. Widget-side completion will call it after the Room write.

## Configuration flow

Use a dedicated `MemoryWidgetConfigActivity` started by the launcher when adding the widget.

The configuration screen should use Harmony styling and show:

1. segmented choice: **Automatisch** / **Bestimmte auswählen**;
2. visible count: **1 / 2 / 3**;
3. in pinned mode, a scrollable list of current/open entries with note/link indicator, title and optional site name;
4. selection counter, max 3;
5. save/add button.

Defaults:

- mode: automatic;
- visible count: 3;
- pinned IDs: none.

Saving writes widget preferences, updates the widget and returns `RESULT_OK` with the widget ID. Cancelling leaves the widget unconfigured and returns `RESULT_CANCELED`.

## Architecture / files

Expected additions/changes:

- `app/src/main/java/com/example/widget/MemoryWidgetProvider.kt`
- `app/src/main/java/com/example/widget/MemoryWidgetConfigActivity.kt`
- optional focused helper(s) under `com.example.widget` for selection/render model/image cache
- `app/src/main/res/layout/widget_memory.xml` plus row layouts or ViewStubs if needed
- `app/src/main/res/xml/memory_widget_info.xml`
- widget background/fallback drawables
- `AndroidManifest.xml` provider + config activity registration
- `MemoryDao.kt` focused query helpers for open/widget entries if needed
- `MemoryRepository.kt` only if a clean shared API is preferable to direct DAO access
- `MemoryViewModel.kt` refresh hook calls after successful mutations
- `MainActivity.kt` intent handling for Memory tab / entry focus
- tests under `app/src/test/...` for selection and intent/action behavior

Avoid unrelated refactors.

## Error handling

- Missing/deleted pinned ID: skip and fill from automatic list.
- Completed pinned ID: skip and fill from automatic list.
- Missing preview image: fallback visual, no empty image frame.
- Failed image download: fallback and keep widget usable.
- Invalid/missing URL: disable browser-specific action and retain app-open action.
- Database failure during completion: do not optimistically remove the row; next refresh keeps the source-of-truth state.
- Empty Memory list: widget shows an empty-state message inviting the user to open Harmony and add a memory.

## Testing

### Unit tests

- automatic mode returns newest 1/2/3 open entries;
- pinned mode preserves configured order;
- pinned missing/completed entries are backfilled without duplicates;
- configured max count caps size-based count;
- compact/medium/large size calculation returns 1/2/3 appropriately;
- only max 3 pinned IDs are accepted;
- completion action targets the correct entry ID;
- generated app-open intent targets Memory tab / specific entry;
- valid link action uses `ACTION_VIEW`, invalid/missing URL falls back safely.

### Integration/resource checks

- manifest provider/config activity are valid;
- widget info XML references correct layouts/configuration activity;
- Android resource processing succeeds;
- existing PicShare widget remains registered and unchanged in behavior.

## Acceptance criteria

The feature is complete when:

1. Android's widget picker offers a dedicated Harmony Memory widget.
2. Adding it opens a configuration screen.
3. Automatic mode shows the newest open entries.
4. Pinned mode allows selecting and ordering up to 3 specific entries.
5. The homescreen shows 1–3 items depending on widget size/configuration.
6. Notes show meaningful text previews.
7. Links show a compact URL preview with image when available and a polished fallback otherwise.
8. Tapping a link preview opens the website.
9. Tapping entry text opens the corresponding Memory entry inside Harmony.
10. Tapping the check marks the entry completed without opening Harmony.
11. Completed/deleted pinned items are replaced by the next newest open item instead of leaving a gap.
12. Relevant Memory changes refresh the widget.
13. No duplicate Memory data store is introduced.
14. The existing PicShare widget continues to work unchanged.
