# Project Manifest & File Inventory

This manifest provides a complete inventory of all files included in the standalone Harmony Android project.

---

## 🛠 Configuration & Gradle Files
- `build.gradle.kts`: Root Gradle build configuration.
- `settings.gradle.kts`: Root project settings and module inclusions.
- `gradle.properties`: JVM arguments and Gradle execution properties.
- `gradle/libs.versions.toml`: Centralized Version Catalog (AGP, Kotlin, Compose, KSP, Room, Supabase, Ktor).
- `gradlew`: Executable shell script for Unix/macOS.
- `gradlew.bat`: Executable batch script for Windows.
- `gradle/wrapper/gradle-wrapper.jar`: Gradle 9.3.1 Wrapper JAR.
- `gradle/wrapper/gradle-wrapper.properties`: Wrapper configuration referencing Gradle 9.3.1 distribution.
- `.env.example`: Template for API keys and Supabase credentials.
- `metadata.json`: Platform identification metadata.

---

## 📱 Application Source Code (`app/src/main/java/com/example`)

### 1. Root & Navigation
- `MainActivity.kt`: Entry point hosting Jetpack Compose navigation scaffold, top bar, and tab bar.

### 2. Data & Business Logic (`com/example/data`)
- `DeveloperDataManager.kt`: In-memory and local storage manager for categories, question packs, image overrides, and developer asset storage.
- `DevAssetStore.kt`: Asset store handling URI imports and file-based asset caching.
- `DevExporter.kt`: JSON import/export generator for complete project data.
- `DevGenTypes.kt`: Data models for image staging and generation tasks.
- `GeneratedHarmonyContent.kt`: Default fallback categories, questions, and "Das oder Das" pairs.
- `LinkEngine.kt`: Chain/link pack calculation and progression engine.
- `SupabaseClient.kt`: Supabase client initialization, DTO models (`SupabaseCategoryDto`, `SupabasePackageDto`, `SupabasePairDto`, `SupabaseQuestionDto`), and `HarmonyRepositorySupabase`.
- `SupabaseSync.kt`: Background synchronization engine mapping remote Supabase data into local application state.
- `repository/HarmonyRepository.kt`: Local database access, initial data seeding, and Gemini relationship coach client calls.
- `db/`: Room database classes, DAOs, and entities (`AnswerEntity`, `ChatMessageEntity`, `CoupleStatsEntity`, `MomentEntity`, `ProfileEntity`).
- `model/`: Data models (`Category`, `QuestionPack`, `Question`, etc.).

### 3. UI Layer & Jetpack Compose (`com/example/ui`)
- `HarmonyViewModel.kt`: Central ViewModel providing `HarmonyUiState` StateFlow.
- `screens/`:
  - `HomeScreen.kt`: Daily activity dashboard, progress card, widgets, connect banner.
  - `DevStudioScreen.kt`: Developer dashboard with Folder Import, Category CRUD management, Games, Link Chains, Images, Testing, and JSON Export.
  - `GamesScreen.kt`: Question pack explorer, category filters, and active game run screens.
  - `PackListScreen.kt`: Question pack listing and details.
  - `MomentsScreen.kt`: Couples memory timeline and chat log.
- `components/`:
  - `CommonUI.kt`: Shared top bar, navigation bar, cards, and avatar pills.
  - `TotImageProvider.kt`: Image provider for "Das oder Das" pair cards with local override fallback.
- `theme/`: Material Design 3 color palette, typography, and shapes (`Theme.kt`, `Color.kt`, `Type.kt`).

---

## 🎨 Resources (`app/src/main/res`)
- `drawable/`: Custom vector assets and launcher icons.
- `values/strings.xml`: Application label ("Harmony") and localized UI strings.
- `values/colors.xml`: XML color definitions.
- `mipmap/`: Adaptive app icons.

---

## 🗄 Database & Migrations (`supabase/`)
- `supabase/migrations/`: SQL migration scripts for `categories`, `packages`, `pairs`, and `questions`.
- `supabase/seed/`: Initial seed data for default question packs.

---

## 🧪 Unit & UI Testing (`app/src/test` & `app/src/androidTest`)
- `ExampleRobolectricTest.kt`: Fast JVM-based UI and logic tests powered by Robolectric.
