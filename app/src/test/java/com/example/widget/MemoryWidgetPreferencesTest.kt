package com.example.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MemoryWidgetPreferencesTest {
    @Test
    fun `config is normalized stored loaded and deleted`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        MemoryWidgetPreferences.save(
            context,
            42,
            MemoryWidgetConfig(MemoryWidgetMode.PINNED, 9, listOf("a", "a", "b", "c", "d"))
        )
        assertEquals(
            MemoryWidgetConfig(MemoryWidgetMode.PINNED, 3, listOf("a", "b", "c")),
            MemoryWidgetPreferences.load(context, 42)
        )
        MemoryWidgetPreferences.delete(context, 42)
        assertEquals(MemoryWidgetConfig(), MemoryWidgetPreferences.load(context, 42))
    }
}
