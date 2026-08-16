# Harmony - Standalone Android Application

Harmony is a comprehensive couples relationship and question app built with modern Android practices using Kotlin and Jetpack Compose. This version is completely decoupled from Google AI Studio and can be compiled, modified, and built locally in Android Studio.

---

## 🚀 Quick Start Guide

### 1. Open in Android Studio
1. Launch **Android Studio** (Ladybug / Jellyfish or newer recommended, JDK 17 or JDK 21).
2. Select **Open** and choose the root directory of this unzipped project.
3. Allow Gradle to perform the initial project sync.

### 2. Environment & Version Requirements
- **Java Development Kit (JDK):** Java 17 or 21
- **Gradle Version:** Gradle 9.3.1 (provided via `./gradlew`)
- **Android Gradle Plugin (AGP):** 9.1.1
- **Kotlin Version:** 2.0.21

### 3. Build & Test Commands
- **Assemble Debug APK:** `./gradlew :app:assembleDebug`
  Output location: `app/build/outputs/apk/debug/app-debug.apk`
- **Run Unit Tests:** `./gradlew :app:test`

### 4. Data & Image Storage Locations
- **Built-in Content:** `app/src/main/java/com/example/data/GeneratedHarmonyContent.kt`
- **Local Custom Data & Images:** Stored in Android Internal Storage (`context.filesDir/dev_images/`) and managed via `DeveloperDataManager`.
- **Remote Content & Storage:** Synced via Supabase REST API (`category`, `question_pack`) and Supabase Storage (`harmony-images` bucket).

---

## 📂 Project Structure

```
├── app/                      # Android Application Module
│   ├── src/main/java/        # Kotlin source files
│   │   └── com/example/
│   │       ├── data/         # Models, Room/Supabase Repositories, DeveloperDataManager
│   │       ├── ui/           # ViewModels, Composables, Theme, Screens
│   │       └── MainActivity.kt
│   ├── src/main/res/         # XML resources, drawables, strings
│   └── build.gradle.kts      # App-level build configuration
├── gradle/                   # Version catalog (libs.versions.toml) & Gradle wrapper
├── supabase/                 # Supabase SQL migrations & seeds
├── gradlew / gradlew.bat     # Gradle wrapper scripts
├── build.gradle.kts          # Root build configuration
├── settings.gradle.kts       # Project settings
├── .env.example              # Environment variables template
├── ARCHITECTURE.md           # Architecture overview
├── BUILD_REPORT.md           # Detailed build and version report
├── PROJECT_MANIFEST.md       # Complete inventory of project files
└── SUPABASE_SETUP.md         # Database migration and setup instructions
```

---

## 🛠 Features Included

- **Home Dashboard & Daily Activity:** Interactive daily cards, streak pill, and connect banners.
- **Question Packs & Categories:** Supports standard multi-question packs and "Das oder Das?" (This or That) interactive comparison pairs.
- **Developer Studio (`DevStudioScreen`):**
  - **Ordner (Folder):** Batch import image pairs and generate question packs.
  - **🏷️ Kategorien (Categories):** Full CRUD management (add, edit, delete custom & generated categories).
  - **Spiele (Games):** Manage game packs and questions.
  - **Ketten (Chains):** Create and edit link packs.
  - **Bilder (Images):** Custom image overrides and assignments.
  - **Export:** Full JSON project content import/export.
- **Supabase Realtime & Storage Sync:** Auto-syncs categories, packages, and custom image overrides from Supabase Postgrest & Storage.

---

## 🔒 Environment Setup

1. Copy `.env.example` to `.env`.
2. Configure your Supabase or API credentials as needed.

---

## 📄 Documentation Files

- [ARCHITECTURE.md](ARCHITECTURE.md) - Deep dive into state management, data layers, and UI components.
- [BUILD_REPORT.md](BUILD_REPORT.md) - Version metrics and verification report.
- [PROJECT_MANIFEST.md](PROJECT_MANIFEST.md) - Full listing of source code and assets.
- [SUPABASE_SETUP.md](SUPABASE_SETUP.md) - SQL migrations, storage bucket setup, and database seeds.
