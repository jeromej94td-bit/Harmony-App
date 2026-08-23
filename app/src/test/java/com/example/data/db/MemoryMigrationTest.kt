package com.example.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoryMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        HarmonyDatabase::class.java
    )

    @Test
    fun `migration from version 2 preserves profiles and creates memory tables`() {
        helper.createDatabase(TEST_DB, 2).apply {
            execSQL(
                """
                INSERT INTO profiles(
                    id, userName, partnerName, startDate, simulatorEnabled, userAvatarPath, partnerAvatarPath
                ) VALUES (1, 'Existing', 'Partner', 42, 1, NULL, NULL)
                """.trimIndent()
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(
            TEST_DB,
            3,
            true,
            HarmonyDatabase.MIGRATION_2_3
        )
        migrated.query("SELECT COUNT(*) FROM profiles").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
        migrated.execSQL(
            """
            INSERT INTO memory_categories(
                id, systemKey, customName, colorKey, iconKey, sortOrder, createdAt, updatedAt
            ) VALUES ('system-films', 'films', NULL, 'violet', 'movie', 0, 1, 1)
            """.trimIndent()
        )
        migrated.execSQL(
            """
            INSERT INTO memory_entries(
                id, categoryId, kind, title, body, url, previewTitle, previewDescription,
                previewImageUrl, previewSiteName, previewFetchedAt, createdAt, updatedAt, completedAt
            ) VALUES ('entry-1', 'system-films', 'NOTE', 'Arrival', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 1, NULL)
            """.trimIndent()
        )
        migrated.query("SELECT COUNT(*) FROM memory_entries").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
        migrated.close()
    }

    @Test
    fun `migration from version 3 keeps existing categories visible`() {
        helper.createDatabase(TEST_DB_V3, 3).apply {
            execSQL(
                """
                INSERT INTO memory_categories(
                    id, systemKey, customName, colorKey, iconKey, sortOrder, createdAt, updatedAt
                ) VALUES ('system-films', 'Filme', NULL, 'violet', 'movie', 0, 1, 1)
                """.trimIndent()
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(
            TEST_DB_V3,
            4,
            true,
            HarmonyDatabase.MIGRATION_3_4
        )
        migrated.query("SELECT isVisible FROM memory_categories WHERE id = 'system-films'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
        migrated.close()
    }

    private companion object {
        const val TEST_DB = "memory-migration-test"
        const val TEST_DB_V3 = "memory-migration-v3-test"
    }
}
