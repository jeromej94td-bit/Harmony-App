package com.example.data.model

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

@JsonClass(generateAdapter = true)
data class MemoryChecklistItem(
    val id: String,
    val text: String,
    val completed: Boolean = false
)

object MemoryChecklistCodec {
    private val adapter by lazy {
        val type = Types.newParameterizedType(List::class.java, MemoryChecklistItem::class.java)
        Moshi.Builder().build().adapter<List<MemoryChecklistItem>>(type)
    }

    fun encode(items: List<MemoryChecklistItem>): String = adapter.toJson(items)

    fun decode(raw: String?): List<MemoryChecklistItem> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching { adapter.fromJson(raw).orEmpty() }
            .getOrDefault(emptyList())
            .mapNotNull { item ->
                val text = item.text.trim()
                if (item.id.isBlank() || text.isEmpty()) null else item.copy(text = text)
            }
    }
}
