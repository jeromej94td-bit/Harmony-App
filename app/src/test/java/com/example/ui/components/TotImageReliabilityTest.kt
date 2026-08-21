package com.example.ui.components

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.DevExporter
import com.example.data.model.HarmonyPacksData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TotImageReliabilityTest {

    @Test
    fun `all this or that cards have a local render fallback`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        assertTrue(TotImageProvider.fallbackDrawableResId != 0)
        assertNotNull(context.getDrawable(TotImageProvider.fallbackDrawableResId))
    }

    @Test
    fun `bundled artwork from all default this or that packs is discoverable for AI export`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packs = HarmonyPacksData.DEFAULT_PACKS.filter { it.type == "tot" }
        val options = packs
            .flatMap { pack -> pack.pairs.flatMap { listOf(it.first, it.second) } }
            .distinct()
        val bundledOptions = options
            .filter { TotImageProvider.getBundledImageResId(it) != null }
            .toSet()

        assertTrue("Expected bundled artwork in the default This-or-That packs", bundledOptions.isNotEmpty())

        val exportedNames = DevExporter.collectBundledAssetNames(context, packs)

        assertEquals(bundledOptions, exportedNames.keys)
        assertEquals("traumhaus_altbau.jpg", exportedNames["Altbau mit Charme"])
        assertEquals("aussen_infinity.jpg", exportedNames["Infinity-Pool"])
        assertTrue(exportedNames.values.all { name ->
            name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp") || name.endsWith(".gif")
        })
    }
}
