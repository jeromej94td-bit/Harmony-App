package com.example

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.LayerDrawable
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "mdpi")
class LauncherIconSafeZoneTest {

  @Test
  fun `launcher artwork is inset from the adaptive icon mask`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val launcherIcon = context.getDrawable(R.mipmap.ic_launcher)

    assertTrue(launcherIcon is AdaptiveIconDrawable)

    val background = (launcherIcon as AdaptiveIconDrawable).background
    assertTrue(
        "The adaptive background must keep the artwork inside an inset layer",
        background is LayerDrawable,
    )

    background as LayerDrawable
    background.bounds = Rect(0, 0, 108, 108)

    assertEquals(2, background.numberOfLayers)
    assertEquals(Rect(9, 9, 99, 99), background.getDrawable(1).bounds)
  }
}
