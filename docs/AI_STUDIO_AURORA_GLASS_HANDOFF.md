# Harmony — Aurora Glass implementation handoff

This document is the implementation brief for Google AI Studio. The source of truth is the Android Compose code in this repository.

## Scope

The Aurora Glass treatment is the visual language for the entire app. The “This or That?” runner is one part of it, not the whole scope.

Start with these shared surfaces so every screen benefits consistently:

- `app/src/main/java/com/example/ui/theme/Theme.kt`
- `app/src/main/java/com/example/ui/components/CommonUI.kt`
- `app/src/main/java/com/example/ui/components/AmbientBackground.kt`
- `app/src/main/java/com/example/ui/screens/HomeScreen.kt`
- `app/src/main/java/com/example/ui/screens/GamesScreen.kt`

The Aurora Glass treatment also belongs to the “This or That?” runner:

- `app/src/main/java/com/example/ui/screens/QuizRunnerScreen.kt`
  - `QuizRunnerScreen`
  - `TotCardPairView`
  - `TotStyledCard`
  - `TotResultsView`
- `app/src/main/java/com/example/ui/screens/TotShufflePolicy.kt`
- `app/src/main/assets/aurora-glass/aurora_glass_motion.svg`
- `app/src/main/assets/aurora-glass/harmony_aurora_glass.svg`
- `app/src/main/assets/aurora-glass/moral_balance.svg`

Use the left concept of the supplied visual reference as the target: transparent glass panels, fine luminous borders, controlled bloom, dark violet background, and one accent color per topic. For **Moralische Werte**, use the gold scale symbol `⚖️` / `moral_balance.svg`; do not use handshake imagery.

Keep the floating heart motif from the introspection reference visible at the outer edges of the animated background. Topic cards should prefer real vector/material icons through `HarmonyTopicIcon`; emojis are a fallback/content hint, not the primary visual system.

`harmony_aurora_glass.svg` is the scalable visual source of truth for the portal: it contains the animated gradients, ring pulses, moving arc, bloom layers and particles. Android should mirror the same look with native Compose animation because SVG SMIL support is not guaranteed by every Android renderer.

The intended behavior is: cards drift in from depth, flip around the vertical axis during a short pack-local shuffle, settle as the real pair with a soft tilt, and only then advance to the next question. Shuffle frames must use existing image keys from the active pack; never invent or borrow an image from another pack.

## Visual direction

Use a restrained Aurora Glass language: deep plum background, translucent surfaces, a cyan-to-violet-to-pink light arc, soft glass highlights, and low-opacity edge glows. Keep the hierarchy calm and readable. Decorative aura must stay behind content and must not cover text or card images.

## Guardrails

Do not change the introspection experience's logic in `app/src/main/java/com/example/ui/screens/IntrospectionGameScreen.kt` or `app/src/main/java/com/example/ui/introspection/`. Its visual treatment may adopt the shared Aurora tokens, but do not change recording, narration, background music, persistence, or the symbols 🧙‍♂️ and ✨️. Do not replace original pack images. Do not add a second image-loading system.

## Verification

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

The companion visual reference was created in Figma as **Harmony Aurora Glass — Android Handoff**:

https://www.figma.com/design/gLc7eArSh6hudomoQiZkk2
