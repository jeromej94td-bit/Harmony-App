package com.example.data

import android.content.Context
import android.net.Uri
import com.example.data.model.Category
import com.example.data.model.HarmonyPacksData
import com.example.data.model.Question
import com.example.data.model.QuestionPack
import com.example.ui.components.TotImageProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale

/**
 * Das Gehirn hinter dem Dev Studio.
 *
 * Drei Ebenen von Inhalten werden zusammengeführt:
 * DEFAULT aus Models.kt, GENERATED aus GeneratedHarmonyContent.kt und
 * CUSTOM aus SharedPreferences. CUSTOM gewinnt vor GENERATED vor DEFAULT.
 *
 * Seit Export v2 wird die Reihenfolge der selbst verwalteten Spiele separat
 * persistiert, damit Bearbeiten/Export/AI-Studio-Import dieselbe Position behalten.
 */
object DeveloperDataManager {

    private const val PREFS_NAME = "dev_studio_prefs"
    private const val KEY_CUSTOM_CATEGORIES = "custom_categories_json"
    private const val KEY_CUSTOM_PACKS = "custom_packs_json"
    private const val KEY_CUSTOM_LINK_PACKS = "custom_link_packs_json"
    private const val KEY_IMAGE_OVERRIDES = "image_overrides_json"
    private const val KEY_GEN_VERSION = "generated_version"
    private const val KEY_GEN_IMAGES = "generated_image_paths_json"

    private val customCategories = mutableListOf<Category>()
    private val customPacks = mutableListOf<QuestionPack>()
    private val customLinkPacks = mutableListOf<LinkEngine.LinkPack>()
    private val imageOverrides = mutableMapOf<String, String>()

    val _customCategories get() = customCategories
    val _customPacks get() = customPacks
    val _imageOverrides get() = imageOverrides

    private val generatedCategories = mutableListOf<Category>()
    private val generatedPacks = mutableListOf<QuestionPack>()
    private val generatedLinkPacks = mutableListOf<LinkEngine.LinkPack>()
    private val generatedImages = mutableMapOf<String, String>()

    data class StagedImage(
        val sourceUri: Uri? = null,
        val existingPath: String? = null,
        val label: String
    )

    // ===============================================================
    // Start
    // ===============================================================

    fun init(context: Context) {
        DevExportStateStore.init(context)
        loadData(context)
        migrateRefreshedChoicePacks()
        installGenerated(context)
        DevExportStateStore.reconcileAndPersist(context, rawOwnPacksById().keys.toList())
        syncWithHarmonyData()
    }

    private fun migrateRefreshedChoicePacks() {
        val refreshedIds = setOf("traumhaus", "aussen", "ringe")
        customPacks.removeAll { it.id in refreshedIds && it.pairs.size < 12 }
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun rawOwnPacksById(): LinkedHashMap<String, QuestionPack> {
        val byId = LinkedHashMap<String, QuestionPack>()
        generatedPacks.forEach { byId[it.id] = it }
        customPacks.forEach { byId[it.id] = it }
        return byId
    }

    // ===============================================================
    // Generierter Content
    // ===============================================================

    private fun installGenerated(context: Context) {
        generatedCategories.clear()
        generatedPacks.clear()
        generatedLinkPacks.clear()
        generatedImages.clear()

        GeneratedContentRegistry.CATEGORIES.forEach { gc ->
            generatedCategories.add(Category(gc.id, gc.name, gc.emoji, gc.color))
        }
        GeneratedContentRegistry.PACKS.forEach { gp ->
            generatedPacks.add(
                QuestionPack(
                    id = gp.id,
                    title = gp.title,
                    tags = gp.tags,
                    cat = gp.cat,
                    topic = gp.topic,
                    type = gp.type,
                    questions = gp.questions.map {
                        Question(q = it.q, options = it.options, defaultMine = it.defaultMine)
                    },
                    pairs = gp.pairs,
                    emoji = gp.emoji
                )
            )
        }
        GeneratedContentRegistry.LINK_PACKS.forEach { glp ->
            generatedLinkPacks.add(
                LinkEngine.LinkPack(
                    id = glp.id,
                    title = glp.title,
                    cat = glp.cat,
                    steps = glp.steps.map { s ->
                        LinkEngine.LinkStep(
                            templateA = s.templateA,
                            slotA = LinkEngine.LinkSlot(
                                source = s.slotA.source,
                                packId = s.slotA.packId,
                                pairIndex = s.slotA.pairIndex,
                                side = s.slotA.side,
                                text = s.slotA.text
                            ),
                            templateB = s.templateB,
                            slotB = LinkEngine.LinkSlot(
                                source = s.slotB.source,
                                packId = s.slotB.packId,
                                pairIndex = s.slotB.pairIndex,
                                side = s.slotB.side,
                                text = s.slotB.text
                            ),
                            caption = s.caption
                        )
                    }
                )
            )
        }

        GeneratedContentRegistry.ASSETS.forEach { asset ->
            DevExportStateStore.recordOriginalFileName(
                context = context,
                optionKey = asset.optionKey,
                fileName = asset.originalFileName
            )
        }

        val p = prefs(context)
        val storedVersion = p.getLong(KEY_GEN_VERSION, -1L)
        val storedImagePaths = try {
            JSONObject(p.getString(KEY_GEN_IMAGES, "{}") ?: "{}")
        } catch (_: Exception) {
            JSONObject()
        }
        val needsImageRepair = GeneratedContentRegistry.IMAGES.keys.any { key ->
            val path = storedImagePaths.optString(key, "")
            path.isBlank() || !File(path).exists()
        }

        if (storedVersion != GeneratedContentRegistry.VERSION || needsImageRepair) {
            val written = JSONObject()
            GeneratedContentRegistry.IMAGES.forEach { (name, base64) ->
                val path = DevAssetStore.writeBase64(context, "gen_${DevAssetStore.slug(name)}", base64)
                if (path != null) {
                    generatedImages[name] = path
                    written.put(name, path)
                }
            }
            p.edit()
                .putLong(KEY_GEN_VERSION, GeneratedContentRegistry.VERSION)
                .putString(KEY_GEN_IMAGES, written.toString())
                .apply()
        } else {
            storedImagePaths.keys().forEach { key ->
                generatedImages[key] = storedImagePaths.getString(key)
            }
        }
    }

    // ===============================================================
    // Laden / Speichern
    // ===============================================================

    private fun loadData(context: Context) {
        val p = prefs(context)

        customCategories.clear()
        try {
            val array = JSONArray(p.getString(KEY_CUSTOM_CATEGORIES, "[]") ?: "[]")
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                customCategories.add(
                    Category(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        emoji = obj.optString("emoji", "🎯"),
                        tagColorHex = obj.optLong("tagColorHex", 0xFFFFC46B)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        customPacks.clear()
        try {
            val array = JSONArray(p.getString(KEY_CUSTOM_PACKS, "[]") ?: "[]")
            for (i in 0 until array.length()) customPacks.add(packFromJson(array.getJSONObject(i)))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        customLinkPacks.clear()
        try {
            val array = JSONArray(p.getString(KEY_CUSTOM_LINK_PACKS, "[]") ?: "[]")
            for (i in 0 until array.length()) {
                customLinkPacks.add(LinkEngine.packFromJson(array.getJSONObject(i)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        imageOverrides.clear()
        try {
            val obj = JSONObject(p.getString(KEY_IMAGE_OVERRIDES, "{}") ?: "{}")
            obj.keys().forEach { key -> imageOverrides[key] = obj.getString(key) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun packFromJson(obj: JSONObject): QuestionPack {
        val questions = mutableListOf<Question>()
        obj.optJSONArray("questions")?.let { qArray ->
            for (i in 0 until qArray.length()) {
                val qObj = qArray.getJSONObject(i)
                val opts = mutableListOf<String>()
                qObj.optJSONArray("options")?.let { oArr ->
                    for (o in 0 until oArr.length()) opts.add(oArr.getString(o))
                }
                questions.add(
                    Question(
                        q = qObj.getString("q"),
                        options = opts,
                        defaultMine = if (qObj.has("defaultMine")) qObj.getString("defaultMine") else null
                    )
                )
            }
        }

        val pairs = mutableListOf<Pair<String, String>>()
        obj.optJSONArray("pairs")?.let { pArray ->
            for (i in 0 until pArray.length()) {
                val pObj = pArray.getJSONObject(i)
                pairs.add(pObj.getString("first") to pObj.getString("second"))
            }
        }

        val tags = mutableListOf<String>()
        obj.optJSONArray("tags")?.let { tArr ->
            for (i in 0 until tArr.length()) tags.add(tArr.getString(i))
        }

        return QuestionPack(
            id = obj.getString("id"),
            title = obj.getString("title"),
            tags = if (tags.isEmpty()) listOf("unterhaltung") else tags,
            cat = obj.optString("cat", "tot"),
            topic = obj.optString("topic", "reisen"),
            type = obj.optString("type", "tot"),
            questions = questions,
            pairs = pairs,
            emoji = obj.optString("emoji", "")
        )
    }

    private fun packToJson(pack: QuestionPack): JSONObject {
        val obj = JSONObject()
            .put("id", pack.id)
            .put("title", pack.title)
            .put("cat", pack.cat)
            .put("topic", pack.topic)
            .put("type", pack.type)
        if (pack.emoji.isNotBlank()) obj.put("emoji", pack.emoji)

        val tagsArr = JSONArray()
        pack.tags.forEach { tagsArr.put(it) }
        obj.put("tags", tagsArr)

        val qArr = JSONArray()
        pack.questions.forEach { q ->
            val qObj = JSONObject().put("q", q.q)
            val oArr = JSONArray()
            q.options.forEach { oArr.put(it) }
            qObj.put("options", oArr)
            q.defaultMine?.let { qObj.put("defaultMine", it) }
            qArr.put(qObj)
        }
        obj.put("questions", qArr)

        val pArr = JSONArray()
        pack.pairs.forEach { pair ->
            pArr.put(JSONObject().put("first", pair.first).put("second", pair.second))
        }
        obj.put("pairs", pArr)
        return obj
    }

    private fun saveData(context: Context) {
        val catArray = JSONArray()
        customCategories.forEach { cat ->
            catArray.put(
                JSONObject()
                    .put("id", cat.id)
                    .put("name", cat.name)
                    .put("emoji", cat.emoji)
                    .put("tagColorHex", cat.tagColorHex)
            )
        }

        val packArray = JSONArray()
        customPacks.forEach { packArray.put(packToJson(it)) }
        val linkPackArray = JSONArray()
        customLinkPacks.forEach { linkPackArray.put(LinkEngine.packToJson(it)) }
        val imgObj = JSONObject()
        imageOverrides.forEach { (key, value) -> imgObj.put(key, value) }

        prefs(context).edit()
            .putString(KEY_CUSTOM_CATEGORIES, catArray.toString())
            .putString(KEY_CUSTOM_PACKS, packArray.toString())
            .putString(KEY_CUSTOM_LINK_PACKS, linkPackArray.toString())
            .putString(KEY_IMAGE_OVERRIDES, imgObj.toString())
            .apply()

        syncWithHarmonyData()
    }

    fun syncWithHarmonyData() {
        val mergedCats = generatedCategories.toMutableList()
        customCategories.forEach { cc ->
            val idx = mergedCats.indexOfFirst { it.id == cc.id }
            if (idx >= 0) mergedCats[idx] = cc else mergedCats.add(cc)
        }

        val mergedPacks = getAllOwnPacks()
        val basePacks = HarmonyPacksData.DEFAULT_PACKS
            .filter { !LinkEngine.isLinkPack(it.id) }
            .toMutableList()

        val allLinkPacks = (customLinkPacks + generatedLinkPacks).distinctBy { it.id }
        val availableById = LinkedHashMap<String, QuestionPack>()
        basePacks.forEach { availableById[it.id] = it }
        mergedPacks.forEach { availableById[it.id] = it }

        LinkEngine.clearCaptions()
        val materializedPacks = allLinkPacks.map { lp ->
            LinkEngine.materialize(lp, availableById.values.toList())
        }

        val finalPacks = mergedPacks + materializedPacks

        HarmonyPacksData.setDynamicCategories(mergedCats)
        // Models.kt fügt dynamische, neue Packs aktuell jeweils vorne ein.
        // Deshalb geben wir die gewünschte Reihenfolge rückwärts hinein, damit
        // die tatsächlich sichtbare Reihenfolge exakt dem Dev Studio entspricht.
        HarmonyPacksData.setDynamicPacks(finalPacks.asReversed())

        TotImageProvider.clearGeneratedImages()
        generatedImages.forEach { (name, path) -> TotImageProvider.setGeneratedImage(name, path) }
        imageOverrides.forEach { (name, path) -> TotImageProvider.setCustomImage(name, path) }
    }

    // ===============================================================
    // Lesen / Reihenfolge
    // ===============================================================

    fun getCustomLinkPacks(): List<LinkEngine.LinkPack> = customLinkPacks.toList()
    fun getGeneratedLinkPacks(): List<LinkEngine.LinkPack> = generatedLinkPacks.toList()
    fun getAllLinkPacks(): List<LinkEngine.LinkPack> = (customLinkPacks + generatedLinkPacks).distinctBy { it.id }

    fun getLinkPackById(id: String): LinkEngine.LinkPack? =
        customLinkPacks.find { it.id == id } ?: generatedLinkPacks.find { it.id == id }

    fun saveLinkPack(context: Context, pack: LinkEngine.LinkPack) {
        customLinkPacks.removeAll { it.id == pack.id }
        customLinkPacks.add(0, pack)
        saveData(context)
    }

    fun deleteLinkPack(context: Context, packId: String) {
        customLinkPacks.removeAll { it.id == packId }
        saveData(context)
    }

    fun getCustomCategories(): List<Category> = customCategories.toList()
    fun getCustomPacks(): List<QuestionPack> = customPacks.toList()
    fun getGeneratedPacks(): List<QuestionPack> = generatedPacks.toList()
    fun getGeneratedCategories(): List<Category> = generatedCategories.toList()

    /** Alle selbst erzeugten Pakete in exakt gespeicherter Dev-Studio-Reihenfolge. */
    fun getAllOwnPacks(): List<QuestionPack> {
        val byId = rawOwnPacksById()
        val order = DevExportStateStore.orderedIds(byId.keys.toList())
        return order.mapNotNull { byId[it] }
    }

    fun getPackOrder(): List<String> = getAllOwnPacks().map { it.id }

    fun movePack(context: Context, packId: String, delta: Int) {
        DevExportStateStore.movePack(context, packId, delta, rawOwnPacksById().keys.toList())
        syncWithHarmonyData()
    }

    fun hasUserImage(optionName: String): Boolean = imageOverrides.containsKey(optionName.trim())
    fun getImageOverrides(): Map<String, String> = imageOverrides.toMap()
    fun getGeneratedImages(): Map<String, String> = generatedImages.toMap()
    fun getOriginalFileNames(): Map<String, String> = DevExportStateStore.originalFileNames()

    fun isEditable(packId: String): Boolean = customPacks.any { it.id == packId }

    fun imagePathFor(optionName: String): String? =
        imageOverrides[optionName.trim()] ?: generatedImages[optionName.trim()]

    fun allOptionNames(): List<String> {
        val set = LinkedHashSet<String>()
        HarmonyPacksData.PACKS.forEach { pack ->
            pack.pairs.forEach { (a, b) ->
                set.add(a)
                set.add(b)
            }
            pack.questions.forEach { q -> q.options.forEach { set.add(it) } }
        }
        imageOverrides.keys.forEach { set.add(it) }
        return set.toList()
    }

    fun customOptionNames(): List<String> {
        val set = LinkedHashSet<String>()
        getAllOwnPacks().forEach { pack ->
            pack.pairs.forEach { (a, b) ->
                set.add(a)
                set.add(b)
            }
            pack.questions.forEach { q -> q.options.forEach { set.add(it) } }
        }
        return set.toList()
    }

    // ===============================================================
    // Schreiben
    // ===============================================================

    fun makeCategoryId(name: String): String {
        val clean = name.trim().lowercase(Locale.GERMAN)
            .replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss")
        val sb = StringBuilder()
        for (c in clean) {
            if (c.isLetterOrDigit()) sb.append(c)
            else if (sb.isNotEmpty() && sb.last() != '_') sb.append('_')
        }
        return sb.toString().trim('_').ifEmpty { "kategorie" }
    }

    fun makePackId(title: String): String = "custom_" + makeCategoryId(title)

    fun addOrUpdateCategory(context: Context, name: String, emoji: String, colorHex: Long? = null): Category {
        val cleanName = name.trim()
        val id = makeCategoryId(cleanName)
        val existing = customCategories.find { it.id == id || it.name.equals(cleanName, true) }
        val category = existing?.copy(
            name = cleanName,
            emoji = emoji.ifEmpty { "🎯" },
            tagColorHex = colorHex ?: existing.tagColorHex
        ) ?: Category(
            id = id,
            name = cleanName,
            emoji = emoji.ifEmpty { "🎯" },
            tagColorHex = colorHex ?: 0xFFFFC46B
        )

        customCategories.removeAll { it.id == category.id }
        customCategories.add(category)
        saveData(context)
        return category
    }

    fun updateCategory(context: Context, category: Category) {
        customCategories.removeAll { it.id == category.id }
        customCategories.add(category)
        saveData(context)
    }

    fun deleteCategory(context: Context, categoryId: String) {
        customCategories.removeAll { it.id == categoryId }
        val removedIds = customPacks.filter { it.cat == categoryId }.map { it.id }
        customPacks.removeAll { it.cat == categoryId }
        removedIds.forEach { DevExportStateStore.removePack(context, it) }
        DevExportStateStore.reconcileAndPersist(context, rawOwnPacksById().keys.toList())
        saveData(context)
    }

    fun savePack(context: Context, pack: QuestionPack) {
        val wasKnown = rawOwnPacksById().containsKey(pack.id)
        customPacks.removeAll { it.id == pack.id }
        customPacks.add(0, pack)
        val availableIds = rawOwnPacksById().keys.toList()
        if (wasKnown) {
            DevExportStateStore.ensurePack(context, pack.id, availableIds)
        } else {
            DevExportStateStore.registerNewPack(context, pack.id, availableIds)
        }
        saveData(context)
    }

    fun deletePack(context: Context, packId: String) {
        customPacks.removeAll { it.id == packId }
        DevExportStateStore.removePack(context, packId)
        DevExportStateStore.reconcileAndPersist(context, rawOwnPacksById().keys.toList())
        saveData(context)
    }

    fun setImageOverride(context: Context, optionName: String, urlOrPath: String) {
        if (optionName.isNotBlank() && urlOrPath.isNotBlank()) {
            imageOverrides[optionName.trim()] = urlOrPath.trim()
            saveData(context)
        }
    }

    fun deleteImageOverride(context: Context, optionName: String) {
        val key = optionName.trim()
        imageOverrides.remove(key)
        TotImageProvider.removeCustomImage(key)
        DevAssetStore.deleteImage(context, key)
        saveData(context)
    }

    fun setImageFromUri(context: Context, optionName: String, uri: Uri): String? {
        val key = optionName.trim()
        if (key.isEmpty()) return null
        val path = DevAssetStore.importFromUri(context, uri, key) ?: return null
        imageOverrides[key] = path
        saveData(context)
        return path
    }

    /** Ändert nur den sichtbaren Namen; die Bildzuordnung bleibt erhalten. */
    fun renameOptionKeepingImage(context: Context, oldKey: String, newLabel: String): String {
        val sourceKey = oldKey.trim()
        val typedLabel = newLabel.trim()
        if (sourceKey.isEmpty()) return typedLabel
        if (typedLabel == sourceKey) return sourceKey
        if (typedLabel.isEmpty() && !DevAssetStore.isUserFacingLabel(sourceKey)) return sourceKey

        val targetKey = if (typedLabel.isNotEmpty()) {
            typedLabel
        } else {
            "img_hidden_${System.currentTimeMillis()}_${DevAssetStore.slug(sourceKey).take(24)}"
        }

        val path = imagePathFor(sourceKey)
        if (!path.isNullOrBlank()) {
            imageOverrides[targetKey] = path
            TotImageProvider.setCustomImage(targetKey, path)
        } else {
            TotImageProvider.setAlias(targetKey, sourceKey)
        }
        return targetKey
    }

    /** Tauscht nur die Bildbelegung zweier Karten; Texte und Paarpositionen bleiben unverändert. */
    fun swapOptionImages(context: Context, firstKey: String, secondKey: String) {
        val first = firstKey.trim()
        val second = secondKey.trim()
        if (first.isEmpty() || second.isEmpty() || first == second) return

        val firstPath = imagePathFor(first)
        val secondPath = imagePathFor(second)

        if (secondPath != null) {
            imageOverrides[first] = secondPath
            TotImageProvider.setCustomImage(first, secondPath)
        } else {
            imageOverrides.remove(first)
            TotImageProvider.removeCustomImage(first)
            TotImageProvider.setAlias(first, second)
        }

        if (firstPath != null) {
            imageOverrides[second] = firstPath
            TotImageProvider.setCustomImage(second, firstPath)
        } else {
            imageOverrides.remove(second)
            TotImageProvider.removeCustomImage(second)
            TotImageProvider.setAlias(second, first)
        }

        saveData(context)
    }

    // ===============================================================
    // Ordner-Import
    // ===============================================================

    fun commitImagePack(
        context: Context,
        categoryName: String,
        categoryEmoji: String,
        packTitle: String,
        packEmoji: String = "",
        items: List<StagedImage>,
        categoryColorHex: Long? = null,
        onProgress: ((Int, Int) -> Unit)? = null
    ): QuestionPack {
        val category = addOrUpdateCategory(context, categoryName, categoryEmoji, categoryColorHex)
        val resolved = mutableListOf<String>()
        val packId = makePackId(packTitle)
        val wasKnown = rawOwnPacksById().containsKey(packId)

        items.forEachIndexed { index, item ->
            onProgress?.invoke(index + 1, items.size)
            val rawLabel = item.label.trim()
            val label = if (rawLabel.isNotEmpty() && !rawLabel.matches(Regex("^[0-9]+$"))) {
                uniqueLabel(rawLabel, resolved)
            } else {
                "img_${packId}_${index + 1}"
            }

            val path = when {
                item.sourceUri != null -> DevAssetStore.importFromUri(context, item.sourceUri, label)
                item.existingPath != null -> item.existingPath
                else -> null
            }
            if (path != null) imageOverrides[label] = path
            resolved.add(label)
        }

        val pairs = mutableListOf<Pair<String, String>>()
        var i = 0
        while (i + 1 < resolved.size) {
            pairs.add(resolved[i] to resolved[i + 1])
            i += 2
        }

        val pack = QuestionPack(
            id = packId,
            title = packTitle.trim().ifEmpty { "Neues Spiel" },
            tags = listOf("dasoderdas", "unterhaltung"),
            cat = category.id,
            topic = "reisen",
            type = "tot",
            questions = emptyList(),
            pairs = pairs,
            emoji = packEmoji.ifBlank { categoryEmoji }
        )

        customPacks.removeAll { it.id == pack.id }
        customPacks.add(0, pack)
        val availableIds = rawOwnPacksById().keys.toList()
        if (wasKnown) {
            DevExportStateStore.ensurePack(context, pack.id, availableIds)
        } else {
            DevExportStateStore.registerNewPack(context, pack.id, availableIds)
        }
        saveData(context)
        return pack
    }

    private fun uniqueLabel(base: String, taken: List<String>): String {
        if (!taken.contains(base)) return base
        var n = 2
        while (taken.contains("$base $n")) n++
        return "$base $n"
    }

    fun orderForPairing(files: List<DevAssetStore.PickedFile>): List<DevAssetStore.PickedFile> {
        val markerRegex = Regex("^(\\d+)[\\s._-]?([abAB])(?![a-zA-Z])")
        val matched = files.mapNotNull { f ->
            val m = markerRegex.find(f.displayName.substringBeforeLast('.'))
            if (m == null) null
            else Triple(m.groupValues[1].toIntOrNull() ?: 0, m.groupValues[2].lowercase(), f)
        }
        if (matched.size >= files.size && files.size >= 2) {
            return matched.sortedWith(compareBy({ it.first }, { it.second })).map { it.third }
        }
        return files.sortedWith(compareBy(DevAssetStore.NaturalOrder) { it.displayName })
    }

    fun usesPairMarkers(files: List<DevAssetStore.PickedFile>): Boolean {
        if (files.isEmpty()) return false
        val markerRegex = Regex("^(\\d+)[\\s._-]?([abAB])(?![a-zA-Z])")
        return files.all { markerRegex.containsMatchIn(it.displayName.substringBeforeLast('.')) }
    }

    // ===============================================================
    // Text-Batch-Import
    // ===============================================================

    fun importBatchPack(
        context: Context,
        categoryName: String,
        categoryEmoji: String,
        packTitle: String,
        packType: String,
        rawText: String,
        packEmoji: String = ""
    ): QuestionPack {
        val cat = addOrUpdateCategory(context, categoryName, categoryEmoji)
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val pairs = mutableListOf<Pair<String, String>>()
        val questions = mutableListOf<Question>()

        if (packType == "tot") {
            val delimiters = listOf(" | ", "|", " vs. ", " vs ", " VS ", " VS. ", " oder ", " ODER ", ";", ",")
            for (line in lines) {
                val cleanLine = line.removePrefix("-").removePrefix("*").removePrefix("•").trim()
                var split: List<String>? = null
                for (delim in delimiters) {
                    if (cleanLine.contains(delim)) {
                        val parts = cleanLine.split(delim, limit = 2).map { it.trim() }
                        if (parts.size == 2 && parts[0].isNotEmpty() && parts[1].isNotEmpty()) {
                            split = parts
                            break
                        }
                    }
                }
                if (split != null) pairs.add(split[0] to split[1])
            }
            if (pairs.isEmpty() && lines.size >= 2) {
                var i = 0
                while (i < lines.size - 1) {
                    val a = lines[i].removePrefix("-").removePrefix("*").trim()
                    val b = lines[i + 1].removePrefix("-").removePrefix("*").trim()
                    if (a.isNotEmpty() && b.isNotEmpty()) pairs.add(a to b)
                    i += 2
                }
            }
        } else {
            for (line in lines) {
                val cleanLine = line.removePrefix("-").removePrefix("*").removePrefix("•").trim()
                if (cleanLine.contains("|")) {
                    val parts = cleanLine.split("|").map { it.trim() }
                    questions.add(Question(q = parts[0], options = parts.drop(1)))
                } else {
                    questions.add(Question(q = cleanLine))
                }
            }
        }

        val pack = QuestionPack(
            id = makePackId(packTitle),
            title = packTitle.trim().ifEmpty { "Neues Spiel" },
            tags = if (packType == "tot") listOf("dasoderdas", "unterhaltung") else listOf("unterhaltung"),
            cat = cat.id,
            topic = "reisen",
            type = packType,
            questions = questions,
            pairs = pairs,
            emoji = packEmoji.ifBlank { categoryEmoji }
        )
        savePack(context, pack)
        return pack
    }

    // ===============================================================
    // Backup / Restore
    // ===============================================================

    fun exportProjectJson(context: Context, includeImages: Boolean = true): String {
        val root = JSONObject()
        root.put("format", "harmony-dev-studio")
        root.put("version", 4)

        val cats = JSONArray()
        val exportCats = (customCategories + generatedCategories).distinctBy { it.id }
        exportCats.forEach { c ->
            cats.put(
                JSONObject().put("id", c.id).put("name", c.name)
                    .put("emoji", c.emoji).put("tagColorHex", c.tagColorHex)
            )
        }
        root.put("categories", cats)

        val packs = JSONArray()
        getAllOwnPacks().forEach { packs.put(packToJson(it)) }
        root.put("packs", packs)

        val order = JSONArray()
        getPackOrder().forEach { order.put(it) }
        root.put("packOrder", order)

        val originalNames = JSONObject()
        DevExportStateStore.originalFileNames().forEach { (key, name) -> originalNames.put(key, name) }
        root.put("originalFileNames", originalNames)

        val linkPacksArr = JSONArray()
        getAllLinkPacks().forEach { linkPacksArr.put(LinkEngine.packToJson(it)) }
        root.put("linkPacks", linkPacksArr)

        val imgsObj = JSONObject()
        val imgB64Obj = JSONObject()
        val allOverrides = LinkedHashMap<String, String>()
        imageOverrides.forEach { (k, v) -> allOverrides[k] = v }
        generatedImages.forEach { (k, v) -> if (!allOverrides.containsKey(k)) allOverrides[k] = v }

        allOverrides.forEach { (key, path) ->
            imgsObj.put(key, path)
            if (includeImages && path.startsWith("/") && File(path).exists()) {
                val b64 = DevAssetStore.toBase64(path, maxDim = 720, quality = 75)
                if (b64 != null) imgB64Obj.put(key, b64)
            }
        }
        root.put("images", imgsObj)
        if (includeImages) root.put("imagesBase64", imgB64Obj)
        return root.toString(2)
    }

    fun importProjectJson(context: Context, rawText: String, replace: Boolean = false): Int {
        val cleanText = rawText.trim()
        if (cleanText.isEmpty()) return 0

        if (replace) {
            customCategories.clear()
            customPacks.clear()
            customLinkPacks.clear()
            imageOverrides.clear()
        }

        var totalPacksRestored = 0
        var restoredOrder: List<String> = emptyList()
        val restoredOriginalNames = linkedMapOf<String, String>()

        if (cleanText.startsWith("{")) {
            try {
                val root = JSONObject(cleanText)

                root.optJSONArray("categories")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        val cat = Category(
                            id = o.getString("id"),
                            name = o.getString("name"),
                            emoji = o.optString("emoji", "🎯"),
                            tagColorHex = o.optLong("tagColorHex", 0xFFFFC46B)
                        )
                        customCategories.removeAll { it.id == cat.id }
                        customCategories.add(cat)
                    }
                }

                root.optJSONArray("packs")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val pack = packFromJson(arr.getJSONObject(i))
                        customPacks.removeAll { it.id == pack.id }
                        customPacks.add(pack)
                        totalPacksRestored++
                    }
                }

                root.optJSONArray("packOrder")?.let { arr ->
                    restoredOrder = (0 until arr.length()).map { arr.optString(it) }.filter { it.isNotBlank() }
                }
                root.optJSONObject("originalFileNames")?.let { obj ->
                    obj.keys().forEach { key -> restoredOriginalNames[key] = obj.optString(key) }
                }

                root.optJSONArray("linkPacks")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val lp = LinkEngine.packFromJson(arr.getJSONObject(i))
                        customLinkPacks.removeAll { it.id == lp.id }
                        customLinkPacks.add(lp)
                        totalPacksRestored++
                    }
                }

                root.optJSONObject("imagesBase64")?.let { b64Obj ->
                    b64Obj.keys().forEach { key ->
                        val savedPath = DevAssetStore.writeBase64(context, key, b64Obj.getString(key))
                        if (savedPath != null) {
                            imageOverrides[key] = savedPath
                            TotImageProvider.setCustomImage(key, savedPath)
                        }
                    }
                }

                root.optJSONObject("images")?.let { obj ->
                    obj.keys().forEach { key ->
                        val path = obj.getString(key)
                        if (!imageOverrides.containsKey(key)) {
                            if (path.startsWith("http://") || path.startsWith("https://") ||
                                (path.startsWith("/") && File(path).exists())
                            ) {
                                imageOverrides[key] = path
                                TotImageProvider.setCustomImage(key, path)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                totalPacksRestored = importFromKotlinExportText(context, cleanText)
            }
        } else {
            totalPacksRestored = importFromKotlinExportText(context, cleanText)
        }

        val availableIds = rawOwnPacksById().keys.toList()
        if (restoredOrder.isNotEmpty() || restoredOriginalNames.isNotEmpty()) {
            DevExportStateStore.restore(context, restoredOrder, restoredOriginalNames, availableIds)
        } else {
            DevExportStateStore.reconcileAndPersist(context, availableIds)
        }
        saveData(context)
        return totalPacksRestored
    }

    private fun importFromKotlinExportText(context: Context, text: String): Int {
        var count = 0
        try {
            val fnMap = mutableMapOf<String, String>()
            val fnRegex = Regex("private\\s+fun\\s+(i\\d+)\\(\\):\\s*String\\s*=\\s*buildString\\s*\\{([\\s\\S]*?)\\}")
            fnRegex.findAll(text).forEach { match ->
                val fnName = match.groupValues[1]
                val body = match.groupValues[2]
                val appendRegex = Regex("append\\(\"([^\"]+)\"\\)")
                val b64Builder = StringBuilder()
                appendRegex.findAll(body).forEach { app -> b64Builder.append(app.groupValues[1]) }
                if (b64Builder.isNotEmpty()) fnMap[fnName] = b64Builder.toString()
            }

            val imgMapRegex = Regex("\"([^\"]+)\"\\s*to\\s*(i\\d+)\\(\\)")
            imgMapRegex.findAll(text).forEach { match ->
                val name = match.groupValues[1]
                val b64 = fnMap[match.groupValues[2]]
                if (b64 != null) {
                    val path = DevAssetStore.writeBase64(context, name, b64)
                    if (path != null) {
                        imageOverrides[name] = path
                        TotImageProvider.setCustomImage(name, path)
                    }
                }
            }

            val packTitleRegex = Regex("title\\s*=\\s*\"([^\"]+)\"")
            val pairsRegex = Regex("\"([^\"]+)\"\\s+to\\s+\"([^\"]+)\"")
            val genPackBlocks = text.split("GenPack(").drop(1)
            for (block in genPackBlocks) {
                val titleMatch = packTitleRegex.find(block) ?: continue
                val title = titleMatch.groupValues[1]
                val pairs = mutableListOf<Pair<String, String>>()
                val pairsBlock = block.substringAfter("pairs = listOf(", "").substringBefore("),", "")
                pairsRegex.findAll(pairsBlock).forEach { pm ->
                    pairs.add(pm.groupValues[1] to pm.groupValues[2])
                }
                val idMatch = Regex("id\\s*=\\s*\"([^\"]+)\"").find(block)
                val catMatch = Regex("cat\\s*=\\s*\"([^\"]+)\"").find(block)
                val pack = QuestionPack(
                    id = idMatch?.groupValues?.get(1) ?: makePackId(title),
                    title = title,
                    tags = listOf("dasoderdas"),
                    cat = catMatch?.groupValues?.get(1) ?: "tot",
                    topic = "reisen",
                    type = "tot",
                    questions = emptyList(),
                    pairs = pairs
                )
                customPacks.removeAll { it.id == pack.id }
                customPacks.add(pack)
                count++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return count
    }

    fun resetAll(context: Context) {
        customCategories.clear()
        customPacks.clear()
        customLinkPacks.clear()
        imageOverrides.keys.toList().forEach { DevAssetStore.deleteImage(context, it) }
        imageOverrides.clear()
        DevExportStateStore.clear(context)
        saveData(context)
    }
}
