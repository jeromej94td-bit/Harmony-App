# Build & Environment Report

This report summarizes the local build, test configuration, and environment details for the independent Harmony Android project.

---

## Technical Specifications & Versions

- **Java Version:** OpenJDK 21 LTS (Temurin 21.0.11)
- **Gradle Version:** 9.3.1 (managed via included Gradle Wrapper)
- **Android Gradle Plugin (AGP):** 9.1.1
- **Kotlin Version:** 2.2.10
- **Jetpack Compose BOM:** 2024.09.00
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **Compile SDK:** 34

---

## Build Verification

The project is configured to build completely independently of Google AI Studio.

### Commands Executed & Verified:
```bash
./gradlew :app:assembleDebug
./gradlew :app:test
```

### Build Status:
- **Build Outcome:** `BUILD SUCCESSFUL`
- **Debug APK Location:** `app/build/outputs/apk/debug/app-debug.apk`
- **Unit & Robolectric Tests:** `PASSED`

---

## Included Wrapper Files
- `gradlew` (Linux / macOS execution script)
- `gradlew.bat` (Windows execution script)
- `gradle/wrapper/gradle-wrapper.jar` (Gradle Wrapper binary)
- `gradle/wrapper/gradle-wrapper.properties` (Gradle distribution configuration referencing Gradle 9.3.1)

---

## Known Limitations & Configuration Notes
1. **Supabase Environment Variables:** Update `SUPABASE_PROJECT_ID` and `SUPABASE_ANON_KEY` in `.env` or `SupabaseClientProvider.kt` if connecting to a self-hosted or different Supabase instance.
2. **Offline Fallback:** If internet access is unavailable, local preset categories and question packs stored in `DeveloperDataManager` and `HarmonyPacksData` serve as local fallback data.
