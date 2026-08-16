package com.example.data

import com.example.data.model.Category
import com.example.data.model.Question
import com.example.data.model.QuestionPack
import com.example.ui.components.TotImageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

object SupabaseSync {
    suspend fun fetchAndSync() {
        try {
            val repo = HarmonyRepositorySupabase()
            
            // 1. Fetch categories
            val catsDto = repo.getCategories()
            
            // 2. Fetch packages
            val packsDto = repo.getPackages()
            
            val newPacks = mutableListOf<QuestionPack>()
            
            for (pDto in packsDto) {
                val pairsDto = if (pDto.type == "tot") repo.getPairs(pDto.id) else emptyList()
                val qDto = if (pDto.type == "disc") repo.getQuestions(pDto.id) else emptyList()
                
                val pairs = pairsDto.sortedBy { it.pair_index }.map { 
                    it.left_text to it.right_text 
                }
                
                // Add images to DeveloperDataManager if keys exist
                pairsDto.forEach { pair ->
                    pair.left_image_key?.takeIf { it.isNotBlank() }?.let { key ->
                        val url = repo.getImageUrl(key)
                        DeveloperDataManager._imageOverrides[key] = url
                        TotImageProvider.setCustomImage(key, url)
                    }
                    pair.right_image_key?.takeIf { it.isNotBlank() }?.let { key ->
                        val url = repo.getImageUrl(key)
                        DeveloperDataManager._imageOverrides[key] = url
                        TotImageProvider.setCustomImage(key, url)
                    }
                }
                
                val questions = qDto.sortedBy { it.question_index }.map {
                    Question(q = it.text)
                }
                
                newPacks.add(
                    QuestionPack(
                        id = pDto.id,
                        title = pDto.title,
                        tags = emptyList(), // or derive from somewhere
                        cat = pDto.category_id,
                        topic = "supabase",
                        type = pDto.type,
                        questions = questions,
                        pairs = pairs
                    )
                )
            }
            
            withContext(Dispatchers.Main) {
                // Remove previously fetched supabase packages and add new ones
                DeveloperDataManager._customCategories.removeAll { catsDto.any { c -> c.id == it.id } }
                catsDto.forEach { c ->
                    DeveloperDataManager._customCategories.add(
                        Category(
                            id = c.id,
                            name = c.name,
                            emoji = c.emoji,
                            tagColorHex = c.tagColorHex ?: 0xFFFFFFFF
                        )
                    )
                }
                
                DeveloperDataManager._customPacks.removeAll { newPacks.any { np -> np.id == it.id } }
                DeveloperDataManager._customPacks.addAll(newPacks)
                
                DeveloperDataManager.syncWithHarmonyData()
                Log.d("SupabaseSync", "Synced ${newPacks.size} packages from Supabase.")
            }
        } catch (e: Exception) {
            Log.e("SupabaseSync", "Error fetching data from Supabase", e)
        }
    }
}
