package com.example.data

import java.io.IOException
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkPreviewResolverTest {

    @Test
    fun `normalizer adds https and rejects unsafe schemes`() {
        assertEquals("https://youtube.com/watch?v=abc", normalizeHttpUrl("youtube.com/watch?v=abc"))
        assertNull(normalizeHttpUrl("javascript:alert(1)"))
        assertNull(normalizeHttpUrl("file:///private/note"))
        assertNull(normalizeHttpUrl("intent://open"))
    }

    @Test
    fun `parser accepts meta attributes in either order`() {
        val html = """<meta content="Arrival trailer" property="og:title">
            <meta property="og:image" content="https://img.example/arrival.jpg">"""

        val preview = parseLinkPreviewHtml("https://example.com/x", html)

        assertEquals("Arrival trailer", preview.title)
        assertEquals("https://img.example/arrival.jpg", preview.imageUrl)
    }

    @Test
    fun `parser decodes entities and falls back to Twitter metadata`() {
        val html = """<meta name="twitter:title" content="A &amp; B">
            <meta content="Watch &quot;now&quot;" name="twitter:description">
            <meta name="twitter:image" content="https://img.example/a&amp;b.jpg">"""

        val preview = parseLinkPreviewHtml("https://example.com/x", html)

        assertEquals("A & B", preview.title)
        assertEquals("Watch \"now\"", preview.description)
        assertEquals("https://img.example/a&b.jpg", preview.imageUrl)
    }

    @Test
    fun `OpenGraph metadata takes precedence over Twitter fallback`() {
        val html = """<meta name="twitter:title" content="Twitter title">
            <meta property="og:title" content="OpenGraph title">
            <meta property="og:site_name" content="Example &amp; Co">"""

        val preview = parseLinkPreviewHtml("https://example.com/x", html)

        assertEquals("OpenGraph title", preview.title)
        assertEquals("Example & Co", preview.siteName)
    }

    @Test
    fun `missing metadata leaves nullable preview fields empty`() {
        val preview = parseLinkPreviewHtml("https://example.com/x", "<html><head></head></html>")

        assertEquals("https://example.com/x", preview.normalizedUrl)
        assertNull(preview.title)
        assertNull(preview.description)
        assertNull(preview.imageUrl)
        assertNull(preview.siteName)
    }

    @Test
    fun `youtube id creates standard thumbnail fallback`() {
        assertEquals("https://img.youtube.com/vi/abc123/hqdefault.jpg", youtubeThumbnail("https://youtu.be/abc123"))
    }

    @Test
    fun `youtube watch short and short-link URLs create standard thumbnails`() {
        assertEquals(
            "https://img.youtube.com/vi/watch_id-1/hqdefault.jpg",
            youtubeThumbnail("https://www.youtube.com/watch?v=watch_id-1&t=5")
        )
        assertEquals(
            "https://img.youtube.com/vi/short_id-2/hqdefault.jpg",
            youtubeThumbnail("https://youtube.com/shorts/short_id-2")
        )
        assertEquals(
            "https://img.youtube.com/vi/short_id-3/hqdefault.jpg",
            youtubeThumbnail("https://youtu.be/short_id-3?feature=share")
        )
    }

    @Test
    fun `parser uses a YouTube thumbnail when page has no image metadata`() {
        val preview = parseLinkPreviewHtml(
            "https://youtube.com/watch?v=abc123",
            "<meta property=\"og:title\" content=\"A video\">"
        )

        assertEquals("https://img.youtube.com/vi/abc123/hqdefault.jpg", preview.imageUrl)
    }

    @Test
    fun `resolver parses no more than 512 KiB of HTML`() = runTest {
        val withinCap = "<meta property=\"og:title\" content=\"Inside cap\">"
        val afterCap = "<meta property=\"og:description\" content=\"Outside cap\">"
        val html = withinCap + "x".repeat(512 * 1024 - withinCap.length) + afterCap
        val resolver = OkHttpLinkPreviewResolver(clientFor(html))

        val result = resolver.resolve("https://example.com/x")

        assertTrue(result is LinkPreviewResult.Success)
        val preview = (result as LinkPreviewResult.Success).preview
        assertEquals("Inside cap", preview.title)
        assertNull(preview.description)
    }

    @Test
    fun `resolver returns failure instead of throwing when fetch fails`() = runTest {
        val resolver = OkHttpLinkPreviewResolver(
            OkHttpClient.Builder().addInterceptor { throw IOException("offline") }.build()
        )

        val result = resolver.resolve("example.com")

        assertEquals(LinkPreviewResult.Failure("https://example.com"), result)
    }

    private fun clientFor(html: String): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(Interceptor { chain ->
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(html.toResponseBody())
                .build()
        })
        .build()
}
