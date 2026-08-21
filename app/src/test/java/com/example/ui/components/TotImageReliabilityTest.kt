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
import java.util.zip.ZipFile

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

    @Test
    fun `AI Studio zip physically contains bundled dream house artwork`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pack = HarmonyPacksData.DEFAULT_PACKS.first { it.id == "traumhaus" }

        val zip = DevExporter.exportAiStudioBundleZip(
            context = context,
            packs = listOf(pack),
            linkPacks = emptyList(),
            includeImages = true
        )

        ZipFile(zip).use { archive ->
            val names = archive.entries().asSequence().map { it.name }.toSet()
            assertTrue("Missing bundled image bytes in AI Studio ZIP", names.contains("images/traumhaus/pair-001/a/traumhaus_altbau.jpg"))
            assertTrue(names.contains("images/traumhaus/pair-001/b/traumhaus_smart_home.jpg"))
        }
    }
}
