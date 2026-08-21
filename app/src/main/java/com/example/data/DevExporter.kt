package com.example.data

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.Category
import com.example.data.model.QuestionPack
import com.example.ui.components.TotImageProvider
import com.example.ui.components.getBundledImageResId
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Exportiert Inhalte aus dem Harmony Dev Studio reproduzierbar für Google AI Studio.
 *
 * Der bestehende Button "Für AI Studio exportieren" bekommt dadurch zwei Dateien:
 * - eine direkt verwendbare GeneratedHarmonyContent-Kotlin-Datei,
 * - ein ZIP mit derselben Logik, Manifest und unveränderten Originalbildern.
 */
object DevExporter {

    private const val CHUNK = 24000
    private const val TARGET_PATH = "app/src/main/java/com/example/data/GeneratedHarmonyContent.kt"

    enum class Quality(val label: String, val maxDim: Int, val jpegQuality: Int) {
        KLEIN("Klein · 480px", 480, 62),
        MITTEL("Mittel · 720px", 720, 72),
        GROSS("Groß · 960px", 960, 80)
    }

    data class Result(
        val text: String,
        val kotlinSource: String,
        val packCount: Int,
        val imageCount: Int,
        val approxBytes: Int
    )

    private data class LocalImage(
        val optionKey: String,
        val path: String,
        val originalFileName: String,
        val base64: String? = null
    )

    /**
     * Finds drawable-backed artwork already shipped with the app. This closes the old export
     * gap where only custom/generated absolute file paths were copied into the AI-Studio ZIP.
     */
    internal fun collectBundledAssetNames(
        context: Context,
        packs: List<QuestionPack>
    ): LinkedHashMap<String, String> {
        val result = linkedMapOf<String, String>()
        packs.forEach { pack ->
            pack.pairs.forEach { pair ->
                listOf(pair.first, pair.second).forEach { optionKey ->
                    val resId = TotImageProvider.getBundledImageResId(optionKey) ?: return@forEach
                    result[optionKey] = bundledResourceFileName(context, resId)
                }
            }
        }
        return result
    }

    private fun bundledResourceFileName(context: Context, resId: Int): String {
        val value = android.util.TypedValue()
        return try {
            context.resources.getValue(resId, value, true)
            value.string
                ?.toString()
                ?.substringAfterLast('/')
                ?.takeIf { it.contains('.') }
                ?: "${context.resources.getResourceEntryName(resId)}.bin"
        } catch (_: Exception) {
            "${context.resources.getResourceEntryName(resId)}.bin"
        }
    }

    // ---------------------------------------------------------------
    // Kotlin-Quelle bauen
    // ---------------------------------------------------------------

    fun build(
        context: Context,
        packs: List<QuestionPack>,
        linkPacks: List<LinkEngine.LinkPack> = emptyList(),
        includeImages: Boolean,
        quality: Quality,
        onProgress: ((Int, Int) -> Unit)? = null
    ): Result {
        val version = System.currentTimeMillis()
        val stamp = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN).format(Date())
        val effectiveLinks = if (linkPacks.isEmpty()) DeveloperDataManager.getAllLinkPacks() else linkPacks

        val usedCategoryIds = packs.map { it.cat }.toSet()
        val allCategories = LinkedHashMap<String, Category>()
        DeveloperDataManager.getGeneratedCategories().forEach { allCategories[it.id] = it }
        DeveloperDataManager.getCustomCategories().forEach { allCategories[it.id] = it }
        val categories = allCategories.values.filter { it.id in usedCategoryIds }

        val optionNames = linkedSetOf<String>()
        packs.forEach { pack ->
            pack.pairs.forEach { (a, b) ->
                optionNames.add(a)
                optionNames.add(b)
            }
            pack.questions.forEach { q -> q.options.forEach { optionNames.add(it) } }
        }

        val rememberedNames = DeveloperDataManager.getOriginalFileNames()
        val localImages = optionNames.mapNotNull { optionKey ->
            val path = DeveloperDataManager.imagePathFor(optionKey)
            if (path != null && path.startsWith("/") && File(path).exists()) {
                LocalImage(
                    optionKey = optionKey,
                    path = path,
                    originalFileName = rememberedNames[optionKey] ?: File(path).name
                )
            } else null
        }

        val imagesWithBase64 = if (includeImages) {
            localImages.mapIndexed { index, image ->
                onProgress?.invoke(index + 1, localImages.size)
                image.copy(base64 = DevAssetStore.toBase64(image.path, quality.maxDim, quality.jpegQuality))
            }.filter { it.base64 != null }
        } else {
            emptyList()
        }

        val assetNames = collectBundledAssetNames(context, packs)
        localImages.forEach { image -> assetNames[image.optionKey] = image.originalFileName }

        val packRefs = packs.map { ExportPackRef(it.id, it.title, it.pairs) }
        val assignments = DevExportLogic.assignAssets(packRefs, assetNames)

        val source = buildKotlinSource(
            version = version,
            stamp = stamp,
            categories = categories,
            packs = packs,
            linkPacks = effectiveLinks,
            assignments = assignments,
            imageEntries = imagesWithBase64
        )

        return Result(
            text = source,
            kotlinSource = source,
            packCount = packs.size,
            imageCount = assetNames.size,
            approxBytes = source.toByteArray(Charsets.UTF_8).size
        )
    }

    private fun buildKotlinSource(
        version: Long,
        stamp: String,
        categories: List<Category>,
        packs: List<QuestionPack>,
        linkPacks: List<LinkEngine.LinkPack>,
        assignments: List<ExportAssetAssignment>,
        imageEntries: List<LocalImage>
    ): String {
        val sb = StringBuilder()
        sb.append("package com.example.data\n\n")
        sb.append("/**\n")
        sb.append(" * AUTO-GENERIERT vom Harmony Dev Studio am ").append(stamp).append(".\n")
        sb.append(" * Enthält Reihenfolge, vollständige Spieldaten und Bildzuweisungen.\n")
        sb.append(" */\n")
        sb.append("object GeneratedHarmonyContent {\n")
        sb.append("    const val VERSION: Long = ").append(version).append("L\n\n")

        sb.append("    val ORDER: List<String> = listOf(")
        sb.append(packs.joinToString(", ") { str(it.id) })
        sb.append(")\n\n")

        sb.append("    val ASSETS: List<GenAssetMeta> = listOf(")
        if (assignments.isEmpty()) {
            sb.append(")\n\n")
        } else {
            sb.append('\n')
            assignments.forEachIndexed { index, asset ->
                sb.append("        GenAssetMeta(")
                    .append("optionKey = ").append(str(asset.optionKey)).append(", ")
                    .append("originalFileName = ").append(str(asset.originalFileName)).append(", ")
                    .append("packId = ").append(str(asset.packId)).append(", ")
                    .append("pairIndex = ").append(asset.pairIndex).append(", ")
                    .append("side = ").append(asset.side).append(")")
                sb.append(if (index == assignments.lastIndex) "\n" else ",\n")
            }
            sb.append("    )\n\n")
        }

        sb.append("    val CATEGORIES: List<GenCategory> = listOf(")
        if (categories.isEmpty()) {
            sb.append(")\n\n")
        } else {
            sb.append('\n')
            categories.forEachIndexed { index, c ->
                sb.append("        GenCategory(")
                    .append(str(c.id)).append(", ")
                    .append(str(c.name)).append(", ")
                    .append(str(c.emoji)).append(", ")
                    .append("0x").append(java.lang.Long.toHexString(c.tagColorHex).uppercase()).append("L)")
                sb.append(if (index == categories.lastIndex) "\n" else ",\n")
            }
            sb.append("    )\n\n")
        }

        sb.append("    val PACKS: List<GenPack> = listOf(")
        if (packs.isEmpty()) {
            sb.append(")\n\n")
        } else {
            sb.append('\n')
            packs.forEachIndexed { index, p ->
                sb.append("        GenPack(\n")
                sb.append("            id = ").append(str(p.id)).append(",\n")
                sb.append("            title = ").append(str(p.title)).append(",\n")
                sb.append("            cat = ").append(str(p.cat)).append(",\n")
                sb.append("            topic = ").append(str(p.topic)).append(",\n")
                sb.append("            type = ").append(str(p.type)).append(",\n")
                sb.append("            tags = listOf(").append(p.tags.joinToString(", ") { str(it) }).append("),\n")
                sb.append("            emoji = ").append(str(p.emoji)).append(",\n")

                sb.append("            pairs = listOf(")
                if (p.pairs.isEmpty()) {
                    sb.append("),\n")
                } else {
                    sb.append('\n')
                    p.pairs.forEachIndexed { pairIndex, pair ->
                        sb.append("                ").append(str(pair.first)).append(" to ").append(str(pair.second))
                        sb.append(if (pairIndex == p.pairs.lastIndex) "\n" else ",\n")
                    }
                    sb.append("            ),\n")
                }

                sb.append("            questions = listOf(")
                if (p.questions.isEmpty()) {
                    sb.append(")\n")
                } else {
                    sb.append('\n')
                    p.questions.forEachIndexed { questionIndex, q ->
                        sb.append("                GenQuestion(")
                            .append("q = ").append(str(q.q)).append(", ")
                            .append("options = listOf(")
                            .append(q.options.joinToString(", ") { str(it) })
                            .append("), defaultMine = ")
                            .append(q.defaultMine?.let { str(it) } ?: "null")
                            .append(")")
                        sb.append(if (questionIndex == p.questions.lastIndex) "\n" else ",\n")
                    }
                    sb.append("            )\n")
                }

                sb.append("        )")
                sb.append(if (index == packs.lastIndex) "\n" else ",\n")
            }
            sb.append("    )\n\n")
        }

        sb.append("    val LINK_PACKS: List<GenLinkPack> = listOf(")
        if (linkPacks.isEmpty()) {
            sb.append(")\n\n")
        } else {
            sb.append('\n')
            linkPacks.forEachIndexed { index, lp ->
                sb.append("        GenLinkPack(\n")
                sb.append("            id = ").append(str(lp.id)).append(",\n")
                sb.append("            title = ").append(str(lp.title)).append(",\n")
                sb.append("            cat = ").append(str(lp.cat)).append(",\n")
                sb.append("            steps = listOf(\n")
                lp.steps.forEachIndexed { stepIndex, step ->
                    sb.append("                GenLinkStep(\n")
                    sb.append("                    templateA = ").append(str(step.templateA)).append(",\n")
                    sb.append("                    slotA = GenLinkSlot(source = ").append(str(step.slotA.source))
                        .append(", packId = ").append(str(step.slotA.packId))
                        .append(", pairIndex = ").append(step.slotA.pairIndex)
                        .append(", side = ").append(step.slotA.side)
                        .append(", text = ").append(str(step.slotA.text)).append("),\n")
                    sb.append("                    templateB = ").append(str(step.templateB)).append(",\n")
                    sb.append("                    slotB = GenLinkSlot(source = ").append(str(step.slotB.source))
                        .append(", packId = ").append(str(step.slotB.packId))
                        .append(", pairIndex = ").append(step.slotB.pairIndex)
                        .append(", side = ").append(step.slotB.side)
                        .append(", text = ").append(str(step.slotB.text)).append("),\n")
                    sb.append("                    caption = ").append(str(step.caption)).append("\n")
                    sb.append("                )")
                    sb.append(if (stepIndex == lp.steps.lastIndex) "\n" else ",\n")
                }
                sb.append("            )\n")
                sb.append("        )")
                sb.append(if (index == linkPacks.lastIndex) "\n" else ",\n")
            }
            sb.append("    )\n\n")
        }

        if (imageEntries.isEmpty()) {
            sb.append("    val IMAGES: Map<String, String> by lazy { emptyMap() }\n")
        } else {
            sb.append("    val IMAGES: Map<String, String> by lazy {\n")
            sb.append("        mapOf(\n")
            imageEntries.forEachIndexed { index, image ->
                sb.append("            ").append(str(image.optionKey)).append(" to i").append(index).append("()")
                sb.append(if (index == imageEntries.lastIndex) "\n" else ",\n")
            }
            sb.append("        )\n")
            sb.append("    }\n\n")

            imageEntries.forEachIndexed { index, image ->
                val b64 = image.base64 ?: return@forEachIndexed
                sb.append("    // ").append(image.originalFileName.replace("\n", " ")).append("\n")
                sb.append("    private fun i").append(index).append("(): String = buildString {\n")
                var pos = 0
                while (pos < b64.length) {
                    val end = minOf(pos + CHUNK, b64.length)
                    sb.append("        append(\"").append(b64, pos, end).append("\")\n")
                    pos = end
                }
                sb.append("    }\n\n")
            }
        }

        sb.append("}\n")
        return sb.toString()
    }

    private fun str(raw: String): String = buildString {
        append('"')
        raw.forEach { c ->
            when (c) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '$' -> append("\\$")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(c)
            }
        }
        append('"')
    }

    // ---------------------------------------------------------------
    // AI Studio ZIP
    // ---------------------------------------------------------------

    fun exportAiStudioBundleZip(
        context: Context,
        packs: List<QuestionPack> = DeveloperDataManager.getAllOwnPacks(),
        linkPacks: List<LinkEngine.LinkPack> = DeveloperDataManager.getAllLinkPacks(),
        includeImages: Boolean = true,
        quality: Quality = Quality.MITTEL,
        onProgress: ((Int, Int) -> Unit)? = null
    ): File {
        val result = build(
            context = context,
            packs = packs,
            linkPacks = linkPacks,
            includeImages = includeImages,
            quality = quality,
            onProgress = onProgress
        )
        return writeAiStudioBundle(
            context = context,
            packs = packs,
            kotlinSource = result.kotlinSource,
            includeImages = includeImages,
            onProgress = onProgress
        )
    }

    private fun writeAiStudioBundle(
        context: Context,
        packs: List<QuestionPack>,
        kotlinSource: String,
        includeImages: Boolean,
        onProgress: ((Int, Int) -> Unit)? = null
    ): File {
        val rememberedNames = DeveloperDataManager.getOriginalFileNames()
        val packRefs = packs.map { ExportPackRef(it.id, it.title, it.pairs) }
        val namesForExistingAssets = collectBundledAssetNames(context, packs)
        packs.forEach { pack ->
            pack.pairs.forEach { pair ->
                listOf(pair.first, pair.second).forEach { optionKey ->
                    val path = DeveloperDataManager.imagePathFor(optionKey)
                    if (path != null && path.startsWith("/") && File(path).exists()) {
                        namesForExistingAssets[optionKey] = rememberedNames[optionKey] ?: File(path).name
                    }
                }
            }
        }

        val assignments = if (includeImages) {
            DevExportLogic.assignAssets(packRefs, namesForExistingAssets)
        } else {
            emptyList()
        }
        val zipPaths = DevExportLogic.zipPaths(assignments)
        val manifest = DevExportLogic.buildManifestJson(packRefs, assignments)
        val readme = buildAiStudioReadme(packs.size, assignments.size, includeImages)

        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.GERMAN).format(Date())
        val destFile = File(exportDir(context), "harmony-ai-studio_$stamp.zip")

        ZipOutputStream(BufferedOutputStream(FileOutputStream(destFile))).use { zos ->
            putTextEntry(zos, "AI_STUDIO_README.txt", readme)
            putTextEntry(zos, "harmony-export-manifest.json", manifest)
            putTextEntry(zos, TARGET_PATH, kotlinSource)

            assignments.forEachIndexed { index, asset ->
                onProgress?.invoke(index + 1, assignments.size)
                val original = DevAssetStore.originalFileFor(context, asset.optionKey, asset.originalFileName)
                val fallbackPath = DeveloperDataManager.imagePathFor(asset.optionKey)
                val sourceFile = when {
                    original.exists() -> original
                    fallbackPath != null && fallbackPath.startsWith("/") && File(fallbackPath).exists() -> File(fallbackPath)
                    else -> null
                }
                val bundledResId = if (sourceFile == null) {
                    TotImageProvider.getBundledImageResId(asset.optionKey)
                } else {
                    null
                }

                if (sourceFile == null && bundledResId == null) return@forEachIndexed

                zos.putNextEntry(ZipEntry(zipPaths.getValue(asset)))
                if (sourceFile != null) {
                    sourceFile.inputStream().use { input -> input.copyTo(zos) }
                } else {
                    context.resources.openRawResource(bundledResId!!).use { input -> input.copyTo(zos) }
                }
                zos.closeEntry()
            }
        }
        return destFile
    }

    private fun buildAiStudioReadme(packCount: Int, imageCount: Int, includeImages: Boolean): String = """
HARMONY — EXPORT FÜR GOOGLE AI STUDIO

Inhalt:
- $TARGET_PATH
- harmony-export-manifest.json
- ${if (includeImages) "$imageCount Original- oder App-Bilder unter images/..." else "keine Bilddateien"}
- $packCount Spiele in verbindlicher Reihenfolge

ANWEISUNG FÜR AI STUDIO
1. Ersetze $TARGET_PATH vollständig durch die gleichnamige Datei aus diesem Paket.
2. Beachte harmony-export-manifest.json als verbindliche Zuordnung und Reihenfolge.
3. Bilddateien NICHT umbenennen. zipPath zeigt Spiel, Paar und Seite.
4. Kopiere auch die bereits aus Harmony gebündelten Bilder aus images/... mit; sie sind absichtlich Teil des Exports.
5. Bestehende Harmony-Spielmechanik, Navigation und andere Features nicht umbauen.
6. Falls Android-Ressourcennamen technisch angepasst werden müssen, muss die Zuordnung über optionKey/packId/pairIndex/side erhalten bleiben.
7. Danach kompilieren und alle GenPack-/GenQuestion-/GenAssetMeta-Aufrufe gegen DevGenTypes.kt prüfen.

Eigene Originaldateien bleiben im ZIP unverändert. Bereits in Harmony gebündelte Drawables werden bytegenau aus den App-Ressourcen kopiert.
""".trimIndent()

    private fun putTextEntry(zos: ZipOutputStream, name: String, text: String) {
        zos.putNextEntry(ZipEntry(name))
        zos.write(text.toByteArray(Charsets.UTF_8))
        zos.closeEntry()
    }

    // ---------------------------------------------------------------
    // Teilen / Hilfen
    // ---------------------------------------------------------------

    private fun exportDir(context: Context): File {
        val d = File(context.cacheDir, "exports")
        if (!d.exists()) d.mkdirs()
        return d
    }

    fun writeToFile(context: Context, fileName: String, content: String): File {
        val f = File(exportDir(context), fileName)
        f.writeText(content)
        return f
    }

    /**
     * Beim bisherigen "Für AI Studio exportieren"-Flow werden Kotlin-Datei UND
     * passendes ZIP gemeinsam geteilt. Andere Dateien werden unverändert einzeln geteilt.
     */
    fun shareFile(context: Context, file: File, mime: String = "text/plain") {
        val isAiStudioContent = file.name.startsWith("harmony_content_") &&
            (file.extension.equals("kt", true) || file.extension.equals("txt", true))

        if (isAiStudioContent) {
            val source = try { file.readText() } catch (_: Exception) { "" }
            val selectedIds = DevExportLogic.extractOrderIds(source)
            val byId = DeveloperDataManager.getAllOwnPacks().associateBy { it.id }
            val selectedPacks = selectedIds.mapNotNull { byId[it] }
                .ifEmpty { DeveloperDataManager.getAllOwnPacks() }
            val includeImages = !source.contains("val IMAGES: Map<String, String> by lazy { emptyMap() }")

            val zipFile = try {
                writeAiStudioBundle(
                    context = context,
                    packs = selectedPacks,
                    kotlinSource = source,
                    includeImages = includeImages
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            if (zipFile != null && zipFile.exists()) {
                val uris = arrayListOf(
                    FileProvider.getUriForFile(context, "${context.packageName}.devfiles", file),
                    FileProvider.getUriForFile(context, "${context.packageName}.devfiles", zipFile)
                )
                val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "application/octet-stream"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                    putExtra(Intent.EXTRA_SUBJECT, "Harmony Export für AI Studio")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "AI Studio Export teilen").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                return
            }
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.devfiles", file)
        val effectiveMime = if (file.extension.equals("zip", true)) "application/zip" else mime
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = effectiveMime
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export teilen").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun shareImages(context: Context, paths: List<String>) {
        val uris = ArrayList<Uri>()
        paths.take(60).forEach { p ->
            val f = File(p)
            if (f.exists()) uris.add(FileProvider.getUriForFile(context, "${context.packageName}.devfiles", f))
        }
        if (uris.isEmpty()) return

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "image/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Bilder teilen").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun copyToClipboard(context: Context, label: String, text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    fun suggestFileName(prefix: String): String {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.GERMAN).format(Date())
        val extension = if (prefix == "harmony_content") "kt" else "txt"
        return "${prefix}_$stamp.$extension"
    }

    /**
     * Bestehender vollständiger Projekt-Export. In eine .kt-Datei wird nur noch
     * kompilierbarer Kotlin-Quelltext geschrieben, nie der frühere #-Vorspann.
     */
    fun exportFullProjectZip(context: Context): File {
        val destFile = File(exportDir(context), "harmony-independent-project.zip")
        val ownPacks = DeveloperDataManager.getAllOwnPacks()
        val contentResult = if (ownPacks.isNotEmpty()) {
            build(
                context = context,
                packs = ownPacks,
                linkPacks = DeveloperDataManager.getAllLinkPacks(),
                includeImages = true,
                quality = Quality.MITTEL
            ).kotlinSource
        } else null

        val assetStream = try {
            context.assets.open("harmony_project.zip")
        } catch (_: Exception) {
            null
        }

        if (assetStream == null) {
            val fallbackFile = File(exportDir(context), "harmony_backup.json")
            fallbackFile.writeText(DeveloperDataManager.exportProjectJson(context, includeImages = true))
            return fallbackFile
        }

        try {
            ZipInputStream(assetStream).use { zis ->
                ZipOutputStream(BufferedOutputStream(FileOutputStream(destFile))).use { zos ->
                    var entry: ZipEntry? = zis.nextEntry
                    val buffer = ByteArray(8192)
                    var replacedGeneratedContent = false

                    while (entry != null) {
                        val entryName = entry.name
                        zos.putNextEntry(ZipEntry(entryName))
                        if (entryName == TARGET_PATH && contentResult != null) {
                            zos.write(contentResult.toByteArray(Charsets.UTF_8))
                            replacedGeneratedContent = true
                        } else {
                            var len: Int
                            while (zis.read(buffer).also { len = it } > 0) zos.write(buffer, 0, len)
                        }
                        zos.closeEntry()
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }

                    if (!replacedGeneratedContent && contentResult != null) {
                        putTextEntry(zos, TARGET_PATH, contentResult)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return destFile
    }
}
