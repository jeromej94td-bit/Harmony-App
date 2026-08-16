package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class CategoryDto(
    val id: String,
    val name: String,
    val emoji: String,
    val tagColorHex: Long? = null
)

data class PackageDto(
    val id: String,
    val title: String,
    val type: String,
    val category_id: String
)

data class PairDto(
    val pair_index: Int,
    val left_text: String,
    val right_text: String,
    val left_image_key: String? = null,
    val right_image_key: String? = null
)

data class QuestionDto(
    val question_index: Int,
    val text: String
)

object SupabaseClientProvider {
    var projectId: String = ""
        private set
    var anonKey: String = ""
        private set

    fun init(projectId: String, anonKey: String) {
        this.projectId = projectId
        this.anonKey = anonKey
    }

    val baseUrl: String
        get() = "https://$projectId.supabase.co/rest/v1"
}

class HarmonyRepositorySupabase {

    private fun makeRequest(endpoint: String): String? {
        val projectId = SupabaseClientProvider.projectId
        val anonKey = SupabaseClientProvider.anonKey
        if (projectId.isBlank() || anonKey.isBlank()) return null

        val urlString = "${SupabaseClientProvider.baseUrl}/$endpoint"
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("apikey", anonKey)
            conn.setRequestProperty("Authorization", "Bearer $anonKey")
            conn.setRequestProperty("Accept", "application/json")
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            if (conn.responseCode == 200) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else if (conn.responseCode == 404) {
                Log.w("SupabaseRepo", "HTTP 404 on $urlString. The table might not exist yet. Please run the SQL migrations in SUPABASE_SETUP.md.")
                null
            } else {
                Log.e("SupabaseRepo", "HTTP Error ${conn.responseCode} on $urlString")
                null
            }
        } catch (e: Exception) {
            Log.e("SupabaseRepo", "Exception during Supabase request: ${e.message}")
            null
        } finally {
            conn?.disconnect()
        }
    }

    suspend fun getCategories(): List<CategoryDto> = withContext(Dispatchers.IO) {
        val json = makeRequest("categories?select=*") ?: return@withContext emptyList()
        val list = mutableListOf<CategoryDto>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    CategoryDto(
                        id = obj.optString("id", ""),
                        name = obj.optString("name", ""),
                        emoji = obj.optString("emoji", "🎯"),
                        tagColorHex = if (obj.has("tag_color_hex") && !obj.isNull("tag_color_hex")) obj.getLong("tag_color_hex") else null
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }

    suspend fun getPackages(): List<PackageDto> = withContext(Dispatchers.IO) {
        val json = makeRequest("packages?select=*") ?: return@withContext emptyList()
        val list = mutableListOf<PackageDto>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    PackageDto(
                        id = obj.optString("id", ""),
                        title = obj.optString("title", ""),
                        type = obj.optString("type", "tot"),
                        category_id = obj.optString("category_id", "tot")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }

    suspend fun getPairs(packageId: String): List<PairDto> = withContext(Dispatchers.IO) {
        val json = makeRequest("pairs?package_id=eq.$packageId&select=*") ?: return@withContext emptyList()
        val list = mutableListOf<PairDto>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    PairDto(
                        pair_index = obj.optInt("pair_index", i),
                        left_text = obj.optString("left_text", ""),
                        right_text = obj.optString("right_text", ""),
                        left_image_key = obj.optString("left_image_key", null),
                        right_image_key = obj.optString("right_image_key", null)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }

    suspend fun getQuestions(packageId: String): List<QuestionDto> = withContext(Dispatchers.IO) {
        val json = makeRequest("questions?package_id=eq.$packageId&select=*") ?: return@withContext emptyList()
        val list = mutableListOf<QuestionDto>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    QuestionDto(
                        question_index = obj.optInt("question_index", i),
                        text = obj.optString("text", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }

    fun getImageUrl(imageKey: String): String {
        val projectId = SupabaseClientProvider.projectId
        return "https://$projectId.supabase.co/storage/v1/object/public/tot_images/$imageKey"
    }
}
