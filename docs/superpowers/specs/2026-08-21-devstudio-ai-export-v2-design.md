# Dev Studio AI Export v2 Design

## Goal

Make the Harmony Dev Studio export deterministic and safe to hand to Google AI Studio: newly created games keep their intended order, imported images keep their original filenames as metadata, every image has an explicit game/pair/side assignment, and export can produce both a readable Kotlin payload and a ZIP bundle containing code, manifest and image files.

## Constraints

- Do not redesign the game runner or unrelated UI.
- Existing Dev Studio data must keep working.
- Newly imported assets must preserve the original source filename in metadata even if Harmony stores an optimized local JPEG internally.
- Exported game order must exactly match the Dev Studio order.
- Existing generated/base64 content remains readable for backward compatibility.
- AI Studio export must remain usable even when ZIP import is unavailable by continuing to provide a standalone text/Kotlin export.

## Data model

Add persistent Dev Studio metadata for:

- `packOrder`: ordered list of custom/generated pack IDs.
- `assetMetadata`: map keyed by option/image key with original filename, owning pack id, pair index and side.

Metadata is stored in SharedPreferences JSON next to the existing custom packs and image overrides. Old installations without the new keys derive a stable fallback order from their current lists and fall back to the internal file name for old images.

## Import flow

`StagedImage` carries `originalFileName` in addition to URI/path/label. Folder and multi-image import populate it from `PickedFile.displayName`. `commitImagePack` assigns the final option key as before, imports the optimized image, then stores metadata that binds the original filename to the final option key and pair side.

Manual image replacement records the picked display name when it is available. Old callers can omit the filename.

## Ordering

Custom pack order becomes explicit instead of relying on repeated `add(0, ...)` calls. Saving a new pack inserts it at the first position only once; updating a pack keeps its current position. Reorder helpers move a pack up or down and persist the resulting ID order. `getAllOwnPacks()` and dynamic pack merging respect this order.

Generated content adds `ORDER: List<String>` so AI Studio round-trips the same sequence. `HarmonyPacksData.PACKS` receives dynamic packs in the supplied order and no longer reverses them by repeatedly prepending.

## Export formats

### Standalone text/Kotlin export

Continue generating `GeneratedHarmonyContent.kt`, now including:

- categories
- `ORDER`
- complete `GenPack` data including `emoji` and `defaultMine`
- link packs
- legacy Base64 image map for compatibility
- asset metadata for original filename and assignment

### AI Studio ZIP bundle

Add `exportAiStudioBundleZip(...)` producing:

- `AI_STUDIO_README.txt`
- `harmony-export-manifest.json`
- `app/src/main/java/com/example/data/GeneratedHarmonyContent.kt`
- `images/<pack-id>/<original-filename>`

Images are copied without renaming in the ZIP. If the same original filename appears twice inside one game, add a subfolder by pair/side instead of changing the filename.

The manifest contains pack order, pack IDs/titles, image key, original filename, relative ZIP path, pair index and side.

## Dev Studio UI

In the game list, editable own packs receive up/down controls. The visible list order is the persisted order. The Export tab keeps the existing standalone button and adds/reworks the AI Studio ZIP action with clear wording.

## Compatibility

- Old SharedPreferences data loads with defaults.
- Old GeneratedHarmonyContent without explicit metadata remains supported through defaults in `DevGenTypes` and empty generated metadata/order values.
- Existing Base64 installer remains active.
- Existing project ZIP export remains available separately.

## Tests

Add unit tests for pure ordering/metadata/export helper logic where Android context is not needed. Cover:

1. stable pack ordering after save/update/move,
2. original filenames preserved in export metadata,
3. pair index/side assignment deterministic,
4. generated Kotlin contains order/emoji/defaultMine metadata,
5. duplicate source filenames retain the original basename through unique ZIP paths.
