package com.example.widget

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.example.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MemoryWidgetIntentsTest {
    @Test
    fun `memory entry intent carries exact id`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = memoryEntryActivityIntent(context, widgetId = 7, slot = 2, entryId = "abc")

        assertTrue(intent.getBooleanExtra(EXTRA_OPEN_MEMORY, false))
        assertEquals("abc", intent.getStringExtra(EXTRA_MEMORY_ENTRY_ID))
    }

    @Test
    fun `link intent accepts only http and https`() {
        assertEquals(Intent.ACTION_VIEW, memoryBrowserIntent("https://example.com/")?.action)
        assertEquals(Intent.ACTION_VIEW, memoryBrowserIntent("http://example.com/")?.action)
        assertNull(memoryBrowserIntent("javascript:alert(1)"))
        assertNull(memoryBrowserIntent("file:///tmp/example"))
    }

    @Test
    fun `request codes are unique for widget slot and action`() {
        assertNotEquals(memoryWidgetRequestCode(1, 1, 1), memoryWidgetRequestCode(1, 1, 2))
        assertNotEquals(memoryWidgetRequestCode(1, 1, 1), memoryWidgetRequestCode(1, 2, 1))
        assertNotEquals(memoryWidgetRequestCode(1, 1, 1), memoryWidgetRequestCode(2, 1, 1))
    }

    @Test
    fun `parse returns header exact entry or ordinary launch correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        assertEquals(
            MemoryWidgetOpenRequest(null),
            parseMemoryWidgetOpenRequest(memoryHeaderActivityIntent(context, 2))
        )
        assertEquals(
            MemoryWidgetOpenRequest("abc"),
            parseMemoryWidgetOpenRequest(memoryEntryActivityIntent(context, 2, 1, "abc"))
        )
        assertNull(parseMemoryWidgetOpenRequest(Intent(context, MainActivity::class.java)))
    }
}
