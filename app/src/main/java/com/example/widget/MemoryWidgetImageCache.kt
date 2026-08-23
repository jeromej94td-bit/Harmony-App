package com.example.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class MemoryWidgetImageCache(
    private val client: OkHttpClient = defaultClient()
) {
    fun peek(context: Context, url: String): Bitmap? {
        val target = targetFile(context, url)
        if (!target.isFile) return null
        return decodeDownsampled(target)
    }

    suspend fun load(context: Context, url: String): Bitmap? = withContext(Dispatchers.IO) {
        peek(context, url)?.let { return@withContext it }
        if (!isHttpUrl(url)) return@withContext null

        val directory = cacheDirectory(context)
        val target = targetFile(context, url)
        val temp = File(directory, ".${target.name}.${System.nanoTime()}.tmp")
        val request = Request.Builder().url(url).get().build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body ?: return@withContext null
                val announcedLength = body.contentLength()
                if (announcedLength > MAX_DOWNLOAD_BYTES) return@withContext null

                body.byteStream().use { input ->
                    temp.outputStream().use { output ->
                        if (!copyBounded(input, output)) return@withContext null
                    }
                }
            }

            val decoded = decodeDownsampled(temp) ?: return@withContext null
            if (target.exists()) target.delete()
            if (!temp.renameTo(target)) return@withContext decoded
            decodeDownsampled(target) ?: decoded
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Throwable) {
            null
        } finally {
            if (temp.exists()) temp.delete()
        }
    }

    private fun copyBounded(input: InputStream, output: OutputStream): Boolean {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
            if (total > MAX_DOWNLOAD_BYTES) return false
            output.write(buffer, 0, read)
        }
        output.flush()
        return total > 0L
    }

    private fun targetFile(context: Context, url: String): File =
        File(cacheDirectory(context), "${sha256(url)}.img")

    private fun cacheDirectory(context: Context): File =
        File(context.cacheDir, CACHE_DIR).also { it.mkdirs() }

    private fun decodeDownsampled(file: File): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (bounds.outWidth / sample > MAX_WIDTH * 2 || bounds.outHeight / sample > MAX_HEIGHT * 2) {
            sample *= 2
        }
        return BitmapFactory.decodeFile(
            file.absolutePath,
            BitmapFactory.Options().apply { inSampleSize = sample }
        )
    }

    private fun isHttpUrl(url: String): Boolean {
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return false
        val scheme = uri.scheme?.lowercase()
        return (scheme == "http" || scheme == "https") && !uri.host.isNullOrBlank()
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    companion object {
        private const val CACHE_DIR = "memory-widget"
        private const val MAX_WIDTH = 320
        private const val MAX_HEIGHT = 220
        private const val MAX_DOWNLOAD_BYTES = 5L * 1024L * 1024L

        private fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .callTimeout(7, TimeUnit.SECONDS)
            .build()
    }
}
