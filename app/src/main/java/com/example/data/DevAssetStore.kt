package com.example.data

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Base64
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * Verwaltet alle Bilder, die der Entwickler in die App lädt.
 *
 * Wichtig: Bilder werden beim Import SOFORT in den internen Speicher kopiert
 * (files/dev_assets). Damit überleben sie Neustarts — anders als content://-URIs
 * aus der Galerie, deren Berechtigung nach dem Beenden der App verfällt.
 */
object DevAssetStore {

    private const val DIR_NAME = "dev_assets"

    /** Anzeigegröße in der App. Groß genug für Vollbild-Karten. */
    private const val STORE_MAX_DIM = 1080
    private const val STORE_QUALITY = 84

    fun dir(context: Context): File {
        val d = File(context.filesDir, DIR_NAME)
        if (!d.exists()) d.mkdirs()
        return d
    }

    // ---------------------------------------------------------------
    // Namen / Slugs
    // ---------------------------------------------------------------

    fun slug(raw: String): String {
        val lower = raw.trim().lowercase(Locale.GERMAN)
            .replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss")
        val cleaned = StringBuilder()
        for (c in lower) {
            if (c.isLetterOrDigit()) cleaned.append(c)
            else if (cleaned.isNotEmpty() && cleaned.last() != '_') cleaned.append('_')
        }
        return cleaned.toString().trim('_').take(60).ifEmpty { "bild" }
    }

    fun fileFor(context: Context, key: String): File = File(dir(context), "${slug(key)}.jpg")

    fun pathFor(context: Context, key: String): String = fileFor(context, key).absolutePath

    fun hasImage(context: Context, key: String): Boolean = fileFor(context, key).exists()

    fun deleteImage(context: Context, key: String) {
        val f = fileFor(context, key)
        if (f.exists()) f.delete()
    }

    fun listAll(context: Context): List<File> =
        dir(context).listFiles()?.filter { it.isFile }?.sortedBy { it.name } ?: emptyList()

    fun totalBytes(context: Context): Long = listAll(context).sumOf { it.length() }

    // ---------------------------------------------------------------
    // Import
    // ---------------------------------------------------------------

    /** Kopiert ein Bild aus einer beliebigen Uri in den internen Speicher. Gibt den Pfad zurück. */
    fun importFromUri(context: Context, uri: Uri, key: String): String? {
        val bmp = decodeScaled(context, uri, STORE_MAX_DIM) ?: return null
        return saveBitmap(context, bmp, key)
    }

    fun saveBitmap(context: Context, bmp: Bitmap, key: String): String {
        val target = fileFor(context, key)
        FileOutputStream(target).use { out ->
            bmp.compress(Bitmap.CompressFormat.JPEG, STORE_QUALITY, out)
        }
        return target.absolutePath
    }

    /** Liest ein Bild herunterskaliert und dreht es laut EXIF gerade. */
    fun decodeScaled(context: Context, uri: Uri, maxDim: Int): Bitmap? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            var sample = 1
            while (bounds.outWidth / (sample * 2) >= maxDim && bounds.outHeight / (sample * 2) >= maxDim) {
                sample *= 2
            }

            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            var bmp = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opts)
            } ?: return null

            bmp = rotateByExif(context, uri, bmp)
            bmp = scaleToMax(bmp, maxDim)
            bmp
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun scaleToMax(bmp: Bitmap, maxDim: Int): Bitmap {
        val w = bmp.width
        val h = bmp.height
        val longest = maxOf(w, h)
        if (longest <= maxDim) return bmp
        val factor = maxDim.toFloat() / longest.toFloat()
        val nw = (w * factor).toInt().coerceAtLeast(1)
        val nh = (h * factor).toInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(bmp, nw, nh, true)
        if (scaled != bmp) bmp.recycle()
        return scaled
    }

    private fun rotateByExif(context: Context, uri: Uri, bmp: Bitmap): Bitmap {
        return try {
            val orientation = context.contentResolver.openInputStream(uri)?.use { input ->
                ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL

            val degrees = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            if (degrees == 0f) return bmp

            val m = Matrix().apply { postRotate(degrees) }
            val rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
            if (rotated != bmp) bmp.recycle()
            rotated
        } catch (e: Exception) {
            bmp
        }
    }

    // ---------------------------------------------------------------
    // Ordner auslesen (SAF, ohne Extra-Dependency)
    // ---------------------------------------------------------------

    data class PickedFile(val uri: Uri, val displayName: String)

    /** Listet alle Bilddateien in einem per OpenDocumentTree gewählten Ordner. */
    fun listImagesInTree(context: Context, treeUri: Uri): List<PickedFile> {
        val result = mutableListOf<PickedFile>()
        try {
            val docId = DocumentsContract.getTreeDocumentId(treeUri)
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId)
            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
            )
            val cursor: Cursor? = context.contentResolver.query(childrenUri, projection, null, null, null)
            cursor?.use { c ->
                while (c.moveToNext()) {
                    val id = c.getString(0)
                    val name = c.getString(1) ?: continue
                    val mime = c.getString(2) ?: ""
                    if (mime.startsWith("image/") || looksLikeImage(name)) {
                        result.add(
                            PickedFile(
                                uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, id),
                                displayName = name
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result.sortedWith(compareBy(NaturalOrder) { it.displayName })
    }

    private fun looksLikeImage(name: String): Boolean {
        val n = name.lowercase(Locale.ROOT)
        return n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png") ||
                n.endsWith(".webp") || n.endsWith(".heic") || n.endsWith(".bmp")
    }

    /** Dateiname für eine einzelne per Galerie gewählte Uri ermitteln. */
    fun displayNameOf(context: Context, uri: Uri): String {
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && c.moveToFirst()) {
                    val n = c.getString(idx)
                    if (!n.isNullOrBlank()) return n
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return uri.lastPathSegment ?: "bild"
    }

    /** "01_vanille-bourbon.jpg" -> "Vanille Bourbon", or "" for numeric ID filenames */
    fun labelFromFileName(fileName: String, stripPairMarker: Boolean): String {
        var s = fileName.substringBeforeLast('.')
        s = s.replace(Regex("^[0-9]+[\\s._-]+"), "")
        s = s.replace(Regex("^[0-9]+(?=[a-zA-Z])"), "")
        if (stripPairMarker) {
            s = s.replace(Regex("^[abAB][\\s._-]+"), "")
            s = s.replace(Regex("[\\s._-][abAB]$"), "")
        }
        s = s.replace('_', ' ').replace('-', ' ').replace(Regex("\\s+"), " ").trim()
        if (s.isEmpty() || s.matches(Regex("^[0-9]+$")) || s.uppercase().startsWith("IMG ") || s.uppercase().startsWith("PXL ")) return ""
        return s.split(" ").joinToString(" ") { word ->
            if (word.isEmpty()) word
            else word.substring(0, 1).uppercase(Locale.GERMAN) + word.substring(1)
        }
    }

    /** Prpüft ob ein Text für den Nutzer als Label/Overlay sichtbar sein soll oder ein interner Bild-Schlüssel ist. */
    fun isUserFacingLabel(text: String): Boolean {
        val t = text.trim()
        if (t.isEmpty()) return false
        if (t.startsWith("img_") || t.startsWith("_img_") || t.startsWith("IMG_") || t.startsWith("PXL_")) return false
        if (t.matches(Regex("^[0-9]+$"))) return false
        if (t.matches(Regex("^[0-9_a-fA-F-]+$")) && t.length > 6) return false
        return true
    }

    /** Sortiert "bild2" vor "bild10". */
    object NaturalOrder : Comparator<String> {
        override fun compare(a: String, b: String): Int {
            var i = 0
            var j = 0
            while (i < a.length && j < b.length) {
                val ca = a[i]
                val cb = b[j]
                if (ca.isDigit() && cb.isDigit()) {
                    var si = i
                    var sj = j
                    while (i < a.length && a[i].isDigit()) i++
                    while (j < b.length && b[j].isDigit()) j++
                    val na = a.substring(si, i).trimStart('0').ifEmpty { "0" }
                    val nb = b.substring(sj, j).trimStart('0').ifEmpty { "0" }
                    if (na.length != nb.length) return na.length - nb.length
                    val cmp = na.compareTo(nb)
                    if (cmp != 0) return cmp
                } else {
                    val cmp = ca.lowercaseChar().compareTo(cb.lowercaseChar())
                    if (cmp != 0) return cmp
                    i++
                    j++
                }
            }
            return (a.length - i) - (b.length - j)
        }
    }

    // ---------------------------------------------------------------
    // Base64 (für den AI-Studio-Export)
    // ---------------------------------------------------------------

    /** Re-komprimiert ein gespeichertes Bild kleiner und gibt Base64 zurück. */
    fun toBase64(path: String, maxDim: Int, quality: Int): String? {
        return try {
            val original = BitmapFactory.decodeFile(path) ?: return null
            val small = scaleToMax(original, maxDim)
            val out = java.io.ByteArrayOutputStream()
            small.compress(Bitmap.CompressFormat.JPEG, quality, out)
            small.recycle()
            Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** Schreibt ein Base64-Bild aus dem generierten Code zurück auf die Platte. */
    fun writeBase64(context: Context, key: String, base64: String): String? {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val target = fileFor(context, key)
            FileOutputStream(target).use { it.write(bytes) }
            target.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
