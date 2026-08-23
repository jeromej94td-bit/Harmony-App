package com.example.data

import androidx.core.text.HtmlCompat
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

private const val MAX_PREVIEW_HTML_BYTES = 512 * 1024

data class LinkPreview(
    val normalizedUrl: String,
    val title: String?,
    val description: String?,
    val imageUrl: String?,
    val siteName: String?
)

sealed interface LinkPreviewResult {
    data class Success(val preview: LinkPreview) : LinkPreviewResult
    data class Failure(val normalizedUrl: String) : LinkPreviewResult
}

fun interface LinkPreviewResolver {
    suspend fun resolve(rawUrl: String): LinkPreviewResult
}

class OkHttpLinkPreviewResolver(
    private val callFactory: Call.Factory = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()
) : LinkPreviewResolver {

    override suspend fun resolve(rawUrl: String): LinkPreviewResult {
        val normalizedUrl = normalizeHttpUrl(rawUrl)
            ?: return LinkPreviewResult.Failure(rawUrl.trim())

        val request = Request.Builder().url(normalizedUrl).get().build()
        return suspendCancellableCoroutine { continuation ->
            val call = callFactory.newCall(request)
            continuation.invokeOnCancellation { call.cancel() }
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: java.io.IOException) {
                    if (continuation.isActive) {
                        continuation.resume(LinkPreviewResult.Failure(normalizedUrl))
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val result = response.toPreviewResult(normalizedUrl)
                    if (continuation.isActive) continuation.resume(result)
                }
            })
        }
    }
}

/** Returns a canonical HTTP(S) URL, or null when [rawUrl] cannot be opened safely. */
fun normalizeHttpUrl(rawUrl: String): String? {
    val trimmed = rawUrl.trim()
    if (trimmed.isEmpty()) return null

    val candidate = when {
        trimmed.startsWith("//") -> "https:$trimmed"
        hasExplicitScheme(trimmed) -> trimmed
        else -> "https://$trimmed"
    }

    return runCatching {
        URI(candidate).let { uri ->
            if (uri.scheme.equals("http", ignoreCase = true) || uri.scheme.equals("https", ignoreCase = true)) {
                uri.takeIf { !it.host.isNullOrBlank() }?.toString()
            } else {
                null
            }
        }
    }.getOrNull()
}

fun parseLinkPreviewHtml(normalizedUrl: String, html: String): LinkPreview {
    val metadata = linkedMapOf<String, String>()
    metaTagsIn(html).forEach { tag ->
        val attributes = linkedMapOf<String, String>()
        ATTRIBUTE_REGEX.findAll(tag).forEach { attribute ->
            val value = attribute.groups[2]?.value
                ?: attribute.groups[3]?.value
                ?: attribute.groups[4]?.value
                ?: return@forEach
            attributes[attribute.groups[1]!!.value.lowercase()] = decodeHtmlEntities(value)
        }
        val name = attributes["property"] ?: attributes["name"] ?: return@forEach
        val content = attributes["content"]?.trim().orEmpty()
        if (content.isNotEmpty()) metadata.putIfAbsent(name.lowercase(), content)
    }

    val image = metadata.firstOf("og:image", "twitter:image", "twitter:image:src")
        ?.let { normalizePreviewImage(it, normalizedUrl) }
        ?: youtubeThumbnail(normalizedUrl)

    return LinkPreview(
        normalizedUrl = normalizedUrl,
        title = metadata.firstOf("og:title", "twitter:title"),
        description = metadata.firstOf("og:description", "twitter:description"),
        imageUrl = image,
        siteName = metadata.firstOf("og:site_name", "twitter:site")
    )
}

private fun Response.toPreviewResult(requestedUrl: String): LinkPreviewResult = try {
    use {
        if (!isSuccessful) {
            LinkPreviewResult.Failure(requestedUrl)
        } else {
            val finalUrl = request.url.toString()
            LinkPreviewResult.Success(
                parseLinkPreviewHtml(finalUrl, body?.readPreviewHtml().orEmpty())
            )
        }
    }
} catch (_: Throwable) {
    LinkPreviewResult.Failure(requestedUrl)
}

fun youtubeThumbnail(url: String): String? {
    val normalizedUrl = normalizeHttpUrl(url) ?: return null
    val uri = runCatching { URI(normalizedUrl) }.getOrNull() ?: return null
    val host = uri.host?.lowercase()?.removePrefix("www.")?.removePrefix("m.") ?: return null
    val videoId = when (host) {
        "youtu.be" -> uri.path.trim('/').substringBefore('/').takeIf { it.isNotBlank() }
        "youtube.com" -> when {
            uri.path.equals("/watch", ignoreCase = true) -> queryValue(uri.rawQuery, "v")
            uri.path.startsWith("/shorts/", ignoreCase = true) -> uri.path.substringAfter('/', "")
                .substringAfter('/', "")
                .substringBefore('/')
                .takeIf { it.isNotBlank() }
            else -> null
        }

        else -> null
    }

    return videoId
        ?.takeIf { YOUTUBE_ID_REGEX.matches(it) }
        ?.let { "https://img.youtube.com/vi/$it/hqdefault.jpg" }
}

private fun ResponseBody.readPreviewHtml(): String {
    byteStream().use { input ->
        val output = ByteArrayOutputStream(MAX_PREVIEW_HTML_BYTES)
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var remaining = MAX_PREVIEW_HTML_BYTES
        while (remaining > 0) {
            val read = input.read(buffer, 0, minOf(buffer.size, remaining))
            if (read < 0) break
            output.write(buffer, 0, read)
            remaining -= read
        }
        return output.toString(StandardCharsets.UTF_8.name())
    }
}

private fun hasExplicitScheme(value: String): Boolean =
    Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:").containsMatchIn(value)

private fun normalizePreviewImage(value: String, baseUrl: String): String? = runCatching {
    val resolved = URI(baseUrl).resolve(value.trim()).toString()
    normalizeHttpUrl(resolved)
}.getOrNull()

private fun queryValue(query: String?, name: String): String? = query
    ?.split('&')
    ?.firstOrNull { it.substringBefore('=') == name }
    ?.substringAfter('=', "")
    ?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.name()) }

private fun Map<String, String>.firstOf(vararg keys: String): String? =
    keys.firstNotNullOfOrNull { this[it] }

private fun decodeHtmlEntities(value: String): String = HtmlCompat
    .fromHtml(value.replace("<", "&lt;").replace(">", "&gt;"), HtmlCompat.FROM_HTML_MODE_LEGACY)
    .toString()

private val ATTRIBUTE_REGEX = Regex(
    "([a-zA-Z_:][-a-zA-Z0-9_:]*)\\s*=\\s*(?:\\\"([^\\\"]*)\\\"|'([^']*)'|([^\\s\\\"'=<>`]+))",
    RegexOption.IGNORE_CASE
)
private val YOUTUBE_ID_REGEX = Regex("[A-Za-z0-9_-]+")

private fun metaTagsIn(html: String): Sequence<String> = sequence {
    var searchFrom = 0
    while (searchFrom < html.length) {
        val start = html.indexOf("<meta", searchFrom, ignoreCase = true)
        if (start < 0) return@sequence
        val nameEnd = start + 5
        if (nameEnd < html.length && !html[nameEnd].isWhitespace() && html[nameEnd] != '>' && html[nameEnd] != '/') {
            searchFrom = nameEnd
            continue
        }

        var quote: Char? = null
        var end = nameEnd
        while (end < html.length) {
            val character = html[end]
            when {
                quote != null && character == quote -> quote = null
                quote == null && (character == '\'' || character == '\"') -> quote = character
                quote == null && character == '>' -> break
            }
            end++
        }
        if (end == html.length) return@sequence
        yield(html.substring(start, end + 1))
        searchFrom = end + 1
    }
}
