# Supabase Setup & Migration Guide

This document contains instructions to set up, configure, and synchronize your Supabase backend for the Harmony application.

---

## 🔑 Environment Configuration

Add your Supabase credentials to `.env` or set them in `SupabaseClientProvider.kt`:

```kotlin
object SupabaseClientProvider {
    fun init(projectId: String, anonKey: String) { ... }
}
```

- **Project ID:** `rspgnonlpkxdudbjxnrl` (or your local/custom project ID)
- **Anon Key:** `sb_publishable_qNtemRRaLIW0nbFb52uKLw_rWwlgUo1` (Public publishable key)

---

## 🗄 Database Schema (SQL Migrations)

Run the following SQL queries in your Supabase SQL Editor:

### 1. Categories Table (`categories`)
```sql
CREATE TABLE IF NOT EXISTS categories (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    emoji TEXT DEFAULT '🎯',
    tag_color_hex BIGINT DEFAULT 4294951019,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Row Level Security (RLS)
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Allow public read access to categories" ON categories FOR SELECT USING (true);
```

### 2. Packages Table (`packages`)
```sql
CREATE TABLE IF NOT EXISTS packages (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    type TEXT NOT NULL DEFAULT 'tot', -- 'tot' (Das oder Das) or 'disc' (Questions)
    category_id TEXT REFERENCES categories(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Row Level Security (RLS)
ALTER TABLE packages ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Allow public read access to packages" ON packages FOR SELECT USING (true);
```

### 3. Question Pairs Table (`pairs` for 'tot' packages)
```sql
CREATE TABLE IF NOT EXISTS pairs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    package_id TEXT REFERENCES packages(id) ON DELETE CASCADE,
    pair_index INT NOT NULL,
    left_text TEXT NOT NULL,
    right_text TEXT NOT NULL,
    left_image_key TEXT,
    right_image_key TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Row Level Security (RLS)
ALTER TABLE pairs ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Allow public read access to pairs" ON pairs FOR SELECT USING (true);
```

### 4. Questions Table (`questions` for 'disc' packages)
```sql
CREATE TABLE IF NOT EXISTS questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    package_id TEXT REFERENCES packages(id) ON DELETE CASCADE,
    question_index INT NOT NULL,
    text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Row Level Security (RLS)
ALTER TABLE questions ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Allow public read access to questions" ON questions FOR SELECT USING (true);
```

---

## 🖼 Storage Bucket Setup (`tot_images`)

1. In Supabase Dashboard, navigate to **Storage > Create a new bucket**.
2. **Bucket Name:** `tot_images`
3. Set bucket accessibility to **Public**.
4. Configure bucket policy:
```sql
CREATE POLICY "Public Read Access" ON storage.objects
FOR SELECT USING (bucket_id = 'tot_images');
```

---

## 🔄 Client Synchronization Engine

In the Android codebase, `com.example.data.SupabaseSync` executes `fetchAndSync()` to:
1. Query `categories` table and populate `DeveloperDataManager._customCategories`.
2. Query `packages`, `pairs`, and `questions` tables.
3. Download images from `tot_images` bucket for matching `left_image_key` and `right_image_key`.
4. Register images into `TotImageProvider` and `DeveloperDataManager._imageOverrides`.
5. Notify Jetpack Compose state streams to update UI instantly.
