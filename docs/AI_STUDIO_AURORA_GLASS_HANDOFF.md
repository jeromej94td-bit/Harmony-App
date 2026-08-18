# Harmony — Aurora Glass implementation handoff

This document is the implementation brief for Google AI Studio. The source of truth is the Android Compose code in this repository.

## Scope

The Aurora Glass treatment belongs to the “This or That?” runner only:

- `app/src/main/java/com/example/ui/screens/QuizRunnerScreen.kt`
  - `QuizRunnerScreen`
  - `TotCardPairView`
  - `TotStyledCard`
  - `TotResultsView`
- `app/src/main/java/com/example/ui/screens/TotShufflePolicy.kt`
- `app/src/main/assets/aurora-glass/aurora_glass_motion.svg`
- `app/src/main/assets/aurora-glass/moral_balance.svg`

The intended behavior is: cards drift in from depth, flip around the vertical axis during a short pack-local shuffle, settle as the real pair with a soft tilt, and only then advance to the next question. Shuffle frames must use existing image keys from the active pack; never invent or borrow an image from another pack.

## Visual direction

Use a restrained Aurora Glass language: deep plum background, translucent surfaces, a cyan-to-violet-to-pink light arc, soft glass highlights, and low-opacity edge glows. Keep the hierarchy calm and readable. Decorative aura must stay behind content and must not cover text or card images.

## Guardrails

Do not change the introspection experience in `app/src/main/java/com/example/ui/screens/IntrospectionGameScreen.kt` or `app/src/main/java/com/example/ui/introspection/`. Do not change its logic, recording, narration, background music, persistence, or symbols. Do not replace original pack images. Do not add a second image-loading system.

## Verification

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

The companion visual reference was created in Figma as **Harmony Aurora Glass — Android Handoff**:

https://www.figma.com/design/gLc7eArSh6hudomoQiZkk2

