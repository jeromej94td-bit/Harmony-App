package com.example.data

import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Timeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
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
    fun `parser decodes standard named and numeric entities without changing unknown entities`() {
        val html = """<meta property="og:title" content="Copyright &copy; &hellip; &#x1F642; &unknown;">"""

        val preview = parseLinkPreviewHtml("https://example.com/x", html)

        assertEquals("Copyright © … 🙂 &unknown;", preview.title)
    }

    @Test
    fun `parser decodes broad standard entities including digit names and accented letters`() {
        val html = """<meta property="og:title" content="Caf&eacute; &ouml; &frac12; &#169; &#x1F642; &definitelyUnknown;">"""

        val preview = parseLinkPreviewHtml("https://example.com/x", html)

        assertEquals("Café ö ½ © 🙂 &definitelyUnknown;", preview.title)
    }

    @Test
    fun `parser preserves raw metadata markup as literal text`() {
        val html = """<meta property="og:title" content="A <b>B</b> C">"""

        val preview = parseLinkPreviewHtml("https://example.com/x", html)

        assertEquals("A <b>B</b> C", preview.title)
    }

    @Test
    fun `parser decodes escaped metadata markup once without reparsing it`() {
        val html = """<meta property="og:title" content="A &lt;b&gt;B&lt;/b&gt; C">"""

        val preview = parseLinkPreviewHtml("https://example.com/x", html)

        assertEquals("A <b>B</b> C", preview.title)
    }

    @Test
    fun `parser keeps a greater-than sign inside quoted meta content`() {
        val preview = parseLinkPreviewHtml(
            "https://example.com/x",
            "<meta property=\"og:title\" content=\"2 > 1\">"
        )

        assertEquals("2 > 1", preview.title)
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
    fun `resolver caps HTML by UTF-8 bytes rather than character count`() = runTest {
        val title = "<meta property=\"og:title\" content=\"Inside multibyte cap\">"
        val bytePadding = "✨".repeat((512 * 1024 - title.toByteArray().size) / "✨".toByteArray().size)
        val afterCap = "<meta property=\"og:description\" content=\"Outside multibyte cap\">"
        val resolver = OkHttpLinkPreviewResolver(clientFor(title + bytePadding + afterCap))

        val result = resolver.resolve("https://example.com/x")

        assertTrue(result is LinkPreviewResult.Success)
        val preview = (result as LinkPreviewResult.Success).preview
        assertEquals("Inside multibyte cap", preview.title)
        assertNull(preview.description)
    }

    @Test
    fun `resolver uses final response URL for returned preview and relative image`() = runTest {
        val resolver = OkHttpLinkPreviewResolver(
            clientFor(
                html = "<meta property=\"og:image\" content=\"images/arrival.jpg\">",
                finalUrl = "https://redirected.example/films/arrival"
            )
        )

        val result = resolver.resolve("https://example.com/start")

        assertTrue(result is LinkPreviewResult.Success)
        val preview = (result as LinkPreviewResult.Success).preview
        assertEquals("https://redirected.example/films/arrival", preview.normalizedUrl)
        assertEquals("https://redirected.example/films/images/arrival.jpg", preview.imageUrl)
    }

    @Test
    fun `resolver cancels the in-flight OkHttp call with coroutine cancellation`() = runBlocking {
        val call = BlockingCall(Request.Builder().url("https://example.com/x").build())
        val resolver = OkHttpLinkPreviewResolver(Call.Factory { call })
        val resolution = async(Dispatchers.Default) { resolver.resolve("https://example.com/x") }

        try {
            assertTrue(call.started.await(1, TimeUnit.SECONDS))
            resolution.cancel()
            withTimeout(1_000) { resolution.join() }

            assertTrue(call.enqueued)
            assertTrue(call.cancelled)
        } finally {
            call.release()
            resolution.cancelAndJoin()
        }
    }

    @Test
    fun `resolver returns failure instead of throwing when fetch fails`() = runTest {
        val resolver = OkHttpLinkPreviewResolver(
            OkHttpClient.Builder().addInterceptor { throw IOException("offline") }.build()
        )

        val result = resolver.resolve("example.com")

        assertEquals(LinkPreviewResult.Failure("https://example.com"), result)
    }

    private fun clientFor(html: String, finalUrl: String? = null): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(Interceptor { chain ->
            Response.Builder()
                .request(finalUrl?.let { Request.Builder().url(it).build() } ?: chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(html.toResponseBody())
                .build()
        })
        .build()

    private class BlockingCall(private val callRequest: Request) : Call {
        val started = CountDownLatch(1)
        private val released = CountDownLatch(1)
        @Volatile var enqueued = false
        @Volatile var cancelled = false
        @Volatile private var executed = false

        override fun request(): Request = callRequest

        override fun execute(): Response {
            executed = true
            started.countDown()
            released.await()
            throw IOException("released")
        }

        override fun enqueue(responseCallback: Callback) {
            enqueued = true
            started.countDown()
            Thread {
                released.await()
                responseCallback.onFailure(this, IOException("cancelled"))
            }.start()
        }

        override fun cancel() {
            cancelled = true
            released.countDown()
        }

        override fun isExecuted(): Boolean = executed || enqueued

        override fun isCanceled(): Boolean = cancelled

        override fun timeout(): Timeout = Timeout.NONE

        override fun clone(): Call = BlockingCall(callRequest)

        fun release() {
            released.countDown()
        }
    }
}
