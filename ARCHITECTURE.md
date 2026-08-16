# Harmony - Architecture & Technical Design

This document details the architecture, component hierarchy, state management, and data synchronization flow of the Harmony application.

---

## 🏛 High-Level Architectural Overview

Harmony follows modern Android development standards combining **MVVM (Model-View-ViewModel)** with Jetpack Compose for reactive UI state rendering and **Clean Architecture principles**.

```
       +-------------------------------------------------+
       |                  Jetpack Compose                |
       |  (MainActivity, HomeScreen, DevStudioScreen)    |
       +------------------------+------------------------+
                                |
                                v
       +-------------------------------------------------+
       |                HarmonyViewModel                 |
       |  (Exposes HarmonyUiState via StateFlow)         |
       +------------------------+------------------------+
                                |
             +------------------+------------------+
             |                                     |
             v                                     v
+------------------------+             +------------------------+
| DeveloperDataManager   |             |  SupabaseSync /        |
| (In-Memory & Local     |             |  HarmonyRepository     |
| SharedPreferences)     |             |  (Supabase Backend)    |
+------------------------+             +------------------------+
```

---

## 📦 Core Layers

### 1. UI Layer (`com.example.ui`)
- **Jetpack Compose UI:** Reactive UI components built using Material Design 3.
- **Theme:** Centralized palette in `Theme.kt`, `Color.kt`, and `Type.kt`.
- **Primary Screens:**
  - `HomeScreen`: Displays daily activity cards, progress, recommendations, and quick widgets.
  - `DevStudioScreen`: Developer controls for categories, games, image overrides, and project export/import.
  - `GamesScreen`, `PackListScreen`, `MomentsScreen`: Category filtering, question pack details, and couple moments.

### 2. ViewModel & State Flow (`HarmonyViewModel.kt`)
- Holds `_uiState` (`StateFlow<HarmonyUiState>`).
- Combines multiple reactive streams: profile, answers, messages, moments, stats, categories, and developer data.
- Handles user triggers such as `refreshData()`, `openProfileSheet()`, and `startPack()`.

### 3. Data & Persistence Layer (`com.example.data`)
- **`DeveloperDataManager`:** Central manager for custom categories, question packs, image overrides, and developer asset storage (`DevAssetStore`).
- **`SupabaseClientProvider` & `SupabaseSync`:** Connects to Supabase Postgrest and Storage.
  - Fetches category tables (`categories`), game packages (`packages`), question pairs (`pairs`), and question lists (`questions`).
  - Downloads and maps custom images from Supabase storage buckets (`tot_images`).
  - Synchronizes remote data with `DeveloperDataManager` and `HarmonyPacksData`.

---

## 🔄 Data Synchronization Lifecycle

1. **Initialization:** On app startup, `HarmonyViewModel.init` initializes `DeveloperDataManager` and `SupabaseClientProvider`.
2. **Fetch & Sync:** `SupabaseSync.fetchAndSync()` is invoked asynchronously to pull remote categories and packs.
3. **Local Merging:** Remote categories and custom packs override or augment local static presets (`HarmonyPacksData`).
4. **State Emitting:** `HarmonyUiState` emits updated data to Jetpack Compose screens seamlessly.
