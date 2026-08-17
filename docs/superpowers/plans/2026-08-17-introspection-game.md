# Introspection Game Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the approved local-only guided introspection experience with narrator clips, looping background music, text/audio answers, resume/restart, and saved results.

**Architecture:** A small pure Kotlin state model defines progression and is covered by unit tests. A local SharedPreferences/file store persists text and recording paths. A dedicated Compose screen owns MediaPlayer and MediaRecorder lifecycles, while MainActivity routes the new category and hides global chrome only during the immersive experience.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Android MediaPlayer, Android MediaRecorder, SharedPreferences, JUnit 4.

## Global Constraints

- Category: `Tauche ins Unterbewusstsein` with `🧙‍♂️`.
- Experience: `Das Verborgene in dir` with `✨️`.
- Order: Lieblingsfarbe, Lieblingstier, Wasser, Enthüllung, Ergebnisse.
- Answers may be text or audio; audio is not transcribed.
- Audio recordings are limited to five minutes and stored locally only.
- Background music loops, ducks to 68% during narration, pauses during recording, resumes afterward, and stops on exit.
- Never show the background track name in the UI.
- Leaving an unfinished run preserves it; reopening offers continue or restart.
- The approved mystical purple portal visual remains simple and consistent with Harmony.

---

### Task 1: Progress model and tests

**Files:**
- Create: `app/src/main/java/com/example/ui/introspection/IntrospectionModels.kt`
- Create: `app/src/test/java/com/example/ui/introspection/IntrospectionModelsTest.kt`

**Interfaces:**
- Produces `IntrospectionStage`, `IntrospectionAnswer`, `IntrospectionProgress`, and `advanceAfterAnswer()`.

- [ ] Write tests proving empty answers cannot advance, valid text/audio advances in the required order, and restart returns fresh progress.
- [ ] Run the focused test and confirm it fails because the model does not exist.
- [ ] Implement the minimal immutable state model.
- [ ] Run the focused test and full unit suite.

### Task 2: Local persistence and recording ownership

**Files:**
- Create: `app/src/main/java/com/example/ui/introspection/IntrospectionStore.kt`

**Interfaces:**
- Consumes `IntrospectionProgress`.
- Produces `load()`, `save(progress)`, `recordingFile(stage)`, and `clear()`.

- [ ] Add serialization cases to the model test fixture and confirm failure.
- [ ] Persist stage, text, recording paths, and completion flag in app-private storage.
- [ ] Ensure clear deletes only introspection-owned recording files.
- [ ] Run unit tests.

### Task 3: Guided media lifecycle and screen

**Files:**
- Create: `app/src/main/java/com/example/ui/screens/IntrospectionGameScreen.kt`
- Create: `app/src/main/res/raw/merlin_theme.mp3`
- Create: `app/src/main/res/raw/introspection_color.mp3`
- Create: `app/src/main/res/raw/introspection_animal.mp3`
- Create: `app/src/main/res/raw/introspection_water.mp3`
- Create: `app/src/main/res/raw/introspection_reveal.mp3`

**Interfaces:**
- Consumes local progress/store and raw audio resources.
- Produces a full-screen composable with `onExit`.

- [ ] Implement narrator auto-play on stage entry and stage-gated submission.
- [ ] Implement looping background music at normal volume, 68% during narrator playback, and pause/resume around recording.
- [ ] Implement text/audio choice, five-minute recording cap, playback, result display, and lifecycle-safe cleanup.
- [ ] Implement pulsating violet portal visuals, progress, confirmation dialogs, and no visible music metadata.

### Task 4: Category routing and permissions

**Files:**
- Modify: `app/src/main/java/com/example/data/model/Models.kt`
- Modify: `app/src/main/java/com/example/MainActivity.kt`
- Modify: `app/src/main/AndroidManifest.xml`

**Interfaces:**
- Adds category id `unterbewusstsein` and routes it to the experience chooser and game.

- [ ] Add the category without affecting dynamic category merging.
- [ ] Add the one-choice experience hub, resume/restart decision, and immersive routing.
- [ ] Add `RECORD_AUDIO` permission and preserve system-back behavior.
- [ ] Run unit tests and compile checks.

### Task 5: Publish atomically to main

**Files:** All files above plus this plan.

- [ ] Inspect the final diff and verify all five MP3s are valid resources.
- [ ] Build/test with the repository Gradle wrapper where available.
- [ ] Create one Git tree and commit based on the latest `main` SHA.
- [ ] Fast-forward `main` and fetch the committed files to verify the remote result.
