package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class MemoryEntryKind { NOTE, LINK }

object MemoryDefaults {
    const val FILMS_ID = "system-films"
    /** Kept for the one-time migration into [FILMS_ID]. */
    const val SERIES_ID = "system-series"
    const val IDEAS_ID = "system-ideas"
    const val PLACES_ID = "system-places"
    const val OTHER_ID = "system-other"
    val orderedIds = listOf(FILMS_ID, IDEAS_ID, PLACES_ID, OTHER_ID)
}

fun interface MemoryClock {
    fun nowMillis(): Long
}

object SystemMemoryClock : MemoryClock {
    override fun nowMillis() = System.currentTimeMillis()
}

@Entity(tableName = "memory_categories")
data class MemoryCategoryEntity(
    @PrimaryKey val id: String,
    val systemKey: String? = null,
    val customName: String? = null,
    val colorKey: String,
    val iconKey: String,
    val sortOrder: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val isVisible: Boolean = true
)

@Entity(
    tableName = "memory_entries",
    foreignKeys = [ForeignKey(
        entity = MemoryCategoryEntity::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.RESTRICT
    )],
    indices = [Index("categoryId"), Index("completedAt")]
)
data class MemoryEntryEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val kind: MemoryEntryKind,
    val title: String,
    val body: String? = null,
    val url: String? = null,
    val previewTitle: String? = null,
    val previewDescription: String? = null,
    val previewImageUrl: String? = null,
    val previewSiteName: String? = null,
    val previewFetchedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long? = null
)
