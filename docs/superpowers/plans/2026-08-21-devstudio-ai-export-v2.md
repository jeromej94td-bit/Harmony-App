# Dev Studio AI Export v2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Harmony Dev Studio exports preserve original image filenames, exact game order, complete pack data and explicit image assignments, while producing both a standalone Kotlin export and an AI Studio ZIP bundle.

**Architecture:** Persist ordering and asset metadata beside current Dev Studio preferences, extend generated content types with defaults for compatibility, and centralize deterministic export assembly in `DevExporter`. UI changes remain limited to reorder controls and export actions.

**Tech Stack:** Kotlin, Android SharedPreferences/JSON, Jetpack Compose, JUnit, java.util.zip.

**Spec:** `docs/superpowers/specs/2026-08-21-devstudio-ai-export-v2-design.md`

## Global Constraints

- Do not redesign game runners or unrelated UI.
- Old Dev Studio and generated content must continue to load.
- Preserve original image filenames as metadata; do not rename exported image files.
- Persist and export exact game order.
- Keep standalone text/Kotlin export as a fallback.

---

### Task 1: Generated export types

**Files:**
- Modify: `app/src/main/java/com/example/data/DevGenTypes.kt`
- Test: `app/src/test/java/com/example/data/DevExportTypesTest.kt`

**Interfaces:**
- Produces: `GenAssetMeta`, `GenPack.emoji`, `GenQuestion.defaultMine`, generated `ORDER`/`ASSETS` support.

- [ ] Add a failing unit test that constructs the extended generated types and verifies emoji/defaultMine/asset fields.
- [ ] Run the targeted unit test and confirm failure before production changes.
- [ ] Extend `GenQuestion`, `GenPack`, and add `GenAssetMeta` with backward-compatible defaults.
- [ ] Re-run the targeted unit test and confirm pass.

### Task 2: Persistent ordering and asset metadata

**Files:**
- Modify: `app/src/main/java/com/example/data/DeveloperDataManager.kt`
- Modify: `app/src/main/java/com/example/data/DevAssetStore.kt`
- Test: `app/src/test/java/com/example/data/DevOrderingLogicTest.kt`

**Interfaces:**
- Produces: `AssetMetadata`, `getPackOrder()`, `getAssetMetadata()`, `movePack(packId, delta)`, original filename support in `StagedImage`.

- [ ] Add failing tests for deterministic order merge and move behavior using extracted pure helper functions.
- [ ] Run the targeted tests and confirm expected failures.
- [ ] Add preference keys and JSON load/save for pack order and asset metadata.
- [ ] Preserve original filenames during folder/gallery commit and image replacement.
- [ ] Make save/update keep existing position and new pack insertion deterministic.
- [ ] Make `getAllOwnPacks()` return persisted order.
- [ ] Re-run targeted tests.

### Task 3: Runtime order integration

**Files:**
- Modify: `app/src/main/java/com/example/data/model/Models.kt`
- Modify: `app/src/main/java/com/example/data/DeveloperDataManager.kt`

**Interfaces:**
- Consumes: ordered dynamic pack list from `DeveloperDataManager`.
- Produces: `HarmonyPacksData.PACKS` that preserves supplied dynamic order.

- [ ] Add a failing test or pure helper assertion proving repeated dynamic insertion does not reverse order.
- [ ] Change dynamic pack merge to append dynamic-only packs in their provided sequence instead of `add(0, ...)` reversal.
- [ ] Verify tests.

### Task 4: Deterministic standalone export

**Files:**
- Modify: `app/src/main/java/com/example/data/DevExporter.kt`
- Test: `app/src/test/java/com/example/data/DevExporterTextTest.kt`

**Interfaces:**
- Consumes: ordered packs and asset metadata.
- Produces: Kotlin text containing `ORDER`, complete pack fields and `ASSETS`.

- [ ] Add failing tests around pure export text helpers for order, emoji, defaultMine and asset metadata.
- [ ] Run and confirm failures.
- [ ] Extend Kotlin generation while retaining legacy Base64 `IMAGES`.
- [ ] Re-run tests.

### Task 5: AI Studio ZIP bundle

**Files:**
- Modify: `app/src/main/java/com/example/data/DevExporter.kt`
- Test: `app/src/test/java/com/example/data/DevExporterManifestTest.kt`

**Interfaces:**
- Produces: `exportAiStudioBundleZip(context, packs, includeImages, quality)` and deterministic manifest/path helpers.

- [ ] Add failing tests for manifest JSON and duplicate-filename ZIP path behavior.
- [ ] Run and confirm failures.
- [ ] Implement manifest builder and ZIP writer with `AI_STUDIO_README.txt`, manifest, generated Kotlin and image files.
- [ ] Ensure duplicate basenames use pair/side subfolders without changing the original filename.
- [ ] Re-run tests.

### Task 6: Dev Studio UI wiring

**Files:**
- Modify: `app/src/main/java/com/example/ui/screens/DevStudioScreen.kt`

**Interfaces:**
- Consumes: `movePack` and `exportAiStudioBundleZip`.

- [ ] Add up/down controls for editable own games in the Dev Studio list.
- [ ] Update folder/gallery staging so original source filename reaches `StagedImage`.
- [ ] Keep standalone `Für AI Studio exportieren` text export and add a clearly named ZIP bundle action.
- [ ] Preserve existing full-project ZIP export as a separate backup action.

### Task 7: Verification

**Files:**
- Review all modified files.

- [ ] Run targeted unit tests for new export/order logic.
- [ ] Run broader project unit tests if available.
- [ ] Inspect generated Kotlin syntax for escaping and field completeness.
- [ ] Review git diff for unrelated changes.
- [ ] Open a PR from `feat/devstudio-ai-export-v2` to `main` with test evidence and migration notes.
