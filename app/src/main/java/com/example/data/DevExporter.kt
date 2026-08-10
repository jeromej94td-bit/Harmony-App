package com.example.data

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.QuestionPack
import com.example.ui.AppLanguage
import com.example.ui.LanguageStore
import com.example.ui.localizedContent
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Baut aus dem, was im Dev Studio angelegt wurde, eine einzige Textdatei,
 * die man in Google AI Studio hochladen kann.
 *
 * Warum Text und kein ZIP: AI Studio nimmt keine ZIP-Dateien an.
 * Bilder werden deshalb als Base64 direkt in den Kotlin-Code geschrieben.
 */
object DevExporter {

    /** Maximale Länge eines einzelnen String-Literals im Kotlin-Code. */
    private const val CHUNK = 24000

    private const val MARK = "====="
    private const val TARGET_PATH = "app/src/main/java/com/example/data/GeneratedHarmonyContent.kt"

    enum class Quality(val label: String, val maxDim: Int, val jpegQuality: Int) {
        KLEIN("Klein · 480px", 480, 62),
        MITTEL("Mittel · 720px", 720, 72),
        GROSS("Groß · 960px", 960, 80)
    }

    data class Result(
        val text: String,
        val packCount: Int,
        val imageCount: Int,
        val approxBytes: Int
    )

    // ---------------------------------------------------------------
    // Bauen
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
        val english = LanguageStore.get(context) == AppLanguage.ENGLISH

        val usedCategoryIds = packs.map { it.cat }.toSet()
        val allCategories = LinkedHashMap<String, com.example.data.model.Category>()
        DeveloperDataManager.getGeneratedCategories().forEach { allCategories[it.id] = it }
        DeveloperDataManager.getCustomCategories().forEach { allCategories[it.id] = it }
        val categories = allCategories.values.filter { usedCategoryIds.contains(it.id) }

        // Alle Optionstexte einsammeln, für die es ein eigenes Bild gibt
        val optionNames = LinkedHashSet<String>()
        packs.forEach { pack ->
            pack.pairs.forEach { (a, b) ->
                optionNames.add(a)
                optionNames.add(b)
            }
            pack.questions.forEach { q -> q.options.forEach { optionNames.add(it) } }
        }
        DeveloperDataManager.getImageOverrides().keys.forEach { optionNames.add(it) }

        val imageEntries = mutableListOf<Pair<String, String>>() // name -> base64
        if (includeImages) {
            val withImages = optionNames.mapNotNull { name ->
                val path = DeveloperDataManager.imagePathFor(name)
                if (path != null && path.startsWith("/") && File(path).exists()) name to path else null
            }
            withImages.forEachIndexed { index, (name, path) ->
                onProgress?.invoke(index + 1, withImages.size)
                val b64 = DevAssetStore.toBase64(path, quality.maxDim, quality.jpegQuality)
                if (b64 != null) imageEntries.add(name to b64)
            }
        }

        val sb = StringBuilder()

        sb.append("##################################################################\n")
        sb.append(if (english) "#  HARMONY — DEV STUDIO CONTENT EXPORT\n" else "#  HARMONY — CONTENT-EXPORT AUS DEM DEV STUDIO\n")
        sb.append(if (english) "#  Created: " else "#  Erstellt: ").append(stamp).append("\n")
        sb.append(if (english) "#  Packs: " else "#  Pakete: ").append(packs.size)
            .append(if (english) "  ·  Images: " else "  ·  Bilder: ").append(imageEntries.size).append("\n")
        sb.append("#\n")
        sb.append(if (english) "#  HOW TO USE THIS IN GOOGLE AI STUDIO:\n" else "#  SO GEHT'S IN GOOGLE AI STUDIO:\n")
        sb.append(if (english) "#  Upload this file and write:\n" else "#  Diese Datei hochladen und schreiben:\n")
        sb.append(if (english) "#  \"Replace app/src/main/java/com/example/data/GeneratedHarmonyContent.kt\n" else "#  \"Ersetze die Datei app/src/main/java/com/example/data/GeneratedHarmonyContent.kt\n")
        sb.append(if (english) "#   completely with the contents of the uploaded file. Do not change anything else.\"\n" else "#   komplett durch den Inhalt aus der hochgeladenen Datei. Sonst nichts ändern.\"\n")
        sb.append("#\n")
        sb.append(if (english) "#  Images are embedded in the code as Base64 — no ZIP or extra uploads.\n" else "#  Die Bilder stecken als Base64 im Code — kein ZIP, keine Extra-Uploads.\n")
        sb.append(if (english) "#  On the first launch after the build, the app writes them to disk once.\n" else "#  Beim ersten Start nach dem Build schreibt die App sie einmalig auf die Platte.\n")
        sb.append("##################################################################\n\n")

        // Marker aus Teilen zusammensetzen, damit diese Quelldatei selbst
        // nicht wie ein Dateitrenner aussieht, wenn sie exportiert wird.
        sb.append(MARK).append(" FILE: ").append(TARGET_PATH).append(" ").append(MARK).append("\n")
        sb.append("package com.example.data\n\n")
        sb.append("/**\n")
        sb.append(if (english) " * AUTO-GENERATED by Harmony Dev Studio on " else " * AUTO-GENERIERT vom Harmony Dev Studio am ").append(stamp).append("\n")
        sb.append(if (english) " * Do not edit manually — the next export will overwrite everything.\n" else " * Nicht von Hand bearbeiten — der nächste Export überschreibt alles.\n")
        sb.append(" */\n")
        sb.append("object GeneratedHarmonyContent {\n\n")
        sb.append("    const val VERSION: Long = ").append(version).append("L\n\n")

        // --- Kategorien ---
        sb.append("    val CATEGORIES: List<GenCategory> = listOf(\n")
        categories.forEachIndexed { i, c ->
            sb.append("        GenCategory(")
                .append(str(c.id)).append(", ")
                .append(str(c.name)).append(", ")
                .append(str(c.emoji)).append(", ")
                .append("0x").append(java.lang.Long.toHexString(c.tagColorHex).uppercase()).append("L)")
            sb.append(if (i == categories.lastIndex) "\n" else ",\n")
        }
        sb.append("    )\n\n")

        // --- Pakete ---
        sb.append("    val PACKS: List<GenPack> = listOf(\n")
        packs.forEachIndexed { i, p ->
            sb.append("        GenPack(\n")
            sb.append("            id = ").append(str(p.id)).append(",\n")
            sb.append("            title = ").append(str(p.title)).append(",\n")
            sb.append("            cat = ").append(str(p.cat)).append(",\n")
            sb.append("            topic = ").append(str(p.topic)).append(",\n")
            sb.append("            type = ").append(str(p.type)).append(",\n")
            sb.append("            tags = listOf(").append(p.tags.joinToString(", ") { str(it) }).append("),\n")

            sb.append("            pairs = listOf(")
            if (p.pairs.isEmpty()) {
                sb.append("),\n")
            } else {
                sb.append("\n")
                p.pairs.forEachIndexed { pi, pair ->
                    sb.append("                ").append(str(pair.first))
                        .append(" to ").append(str(pair.second))
                    sb.append(if (pi == p.pairs.lastIndex) "\n" else ",\n")
                }
                sb.append("            ),\n")
            }

            sb.append("            questions = listOf(")
            if (p.questions.isEmpty()) {
                sb.append(")\n")
            } else {
                sb.append("\n")
                p.questions.forEachIndexed { qi, q ->
                    sb.append("                GenQuestion(").append(str(q.q))
                    if (q.options.isNotEmpty()) {
                        sb.append(", listOf(")
                            .append(q.options.joinToString(", ") { str(it) })
                            .append(")")
                    }
                    sb.append(")")
                    sb.append(if (qi == p.questions.lastIndex) "\n" else ",\n")
                }
                sb.append("            )\n")
            }

            sb.append("        )")
            sb.append(if (i == packs.lastIndex) "\n" else ",\n")
        }
        sb.append("    )\n\n")

        // --- Ketten-Pakete ---
        sb.append("    val LINK_PACKS: List<GenLinkPack> = listOf(\n")
        linkPacks.forEachIndexed { i, lp ->
            sb.append("        GenLinkPack(\n")
            sb.append("            id = ").append(str(lp.id)).append(",\n")
            sb.append("            title = ").append(str(lp.title)).append(",\n")
            sb.append("            cat = ").append(str(lp.cat)).append(",\n")
            sb.append("            steps = listOf(\n")
            lp.steps.forEachIndexed { si, step ->
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
                sb.append(if (si == lp.steps.lastIndex) "\n" else ",\n")
            }
            sb.append("            )\n")
            sb.append("        )")
            sb.append(if (i == linkPacks.lastIndex) "\n" else ",\n")
        }
        sb.append("    )\n\n")

        // --- Bilder ---
        if (imageEntries.isEmpty()) {
            sb.append("    val IMAGES: Map<String, String> by lazy { emptyMap() }\n")
        } else {
            sb.append("    val IMAGES: Map<String, String> by lazy {\n")
            sb.append("        mapOf(\n")
            imageEntries.forEachIndexed { i, (name, _) ->
                sb.append("            ").append(str(name)).append(" to i").append(i).append("()")
                sb.append(if (i == imageEntries.lastIndex) "\n" else ",\n")
            }
            sb.append("        )\n")
            sb.append("    }\n\n")

            imageEntries.forEachIndexed { i, (name, b64) ->
                sb.append("    // ").append(name.replace("\n", " ")).append("  (")
                    .append(b64.length / 1024).append(" kB Base64)\n")
                sb.append("    private fun i").append(i).append("(): String = buildString {\n")
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

        val text = sb.toString()
        return Result(
            text = text,
            packCount = packs.size,
            imageCount = imageEntries.size,
            approxBytes = text.length
        )
    }

    /** Kotlin-String-Literal inklusive Escaping. */
    private fun str(raw: String): String {
        val sb = StringBuilder("\"")
        for (c in raw) {
            when (c) {
                '\\' -> sb.append("\\\\")
                '"' -> sb.append("\\\"")
                '$' -> sb.append("\\$")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> sb.append(c)
            }
        }
        sb.append("\"")
        return sb.toString()
    }

    // ---------------------------------------------------------------
    // Teilen
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

    fun shareFile(context: Context, file: File, mime: String = "text/plain") {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.devfiles", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooserTitle = localizedContent("Export teilen", LanguageStore.get(context))
        context.startActivity(Intent.createChooser(intent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    /** Teilt die Bilddateien einzeln — falls du sie lieber direkt in AI Studio hochlädst. */
    fun shareImages(context: Context, paths: List<String>) {
        val uris = ArrayList<Uri>()
        paths.take(60).forEach { p ->
            val f = File(p)
            if (f.exists()) {
                uris.add(FileProvider.getUriForFile(context, "${context.packageName}.devfiles", f))
            }
        }
        if (uris.isEmpty()) return

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "image/jpeg"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooserTitle = localizedContent("Bilder teilen", LanguageStore.get(context))
        context.startActivity(Intent.createChooser(intent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun copyToClipboard(context: Context, label: String, text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    fun suggestFileName(prefix: String): String {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.GERMAN).format(Date())
        return "${prefix}_$stamp.txt"
    }
}
