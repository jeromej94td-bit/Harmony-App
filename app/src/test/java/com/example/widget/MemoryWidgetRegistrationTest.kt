package com.example.widget

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoryWidgetRegistrationTest {
    @Test
    fun `memory widget provider and config activity are registered`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pm = context.packageManager
        val provider = pm.getReceiverInfo(
            ComponentName(context, MemoryWidgetProvider::class.java),
            PackageManager.GET_META_DATA
        )
        val activity = pm.getActivityInfo(
            ComponentName(context, MemoryWidgetConfigActivity::class.java),
            0
        )

        assertEquals(MemoryWidgetProvider::class.java.name, provider.name)
        assertEquals(MemoryWidgetConfigActivity::class.java.name, activity.name)
        assertTrue(provider.metaData.containsKey("android.appwidget.provider"))
    }
}
