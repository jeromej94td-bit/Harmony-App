package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Harmony", appName)
  }

  @Test
  fun `main activity starts without crashing`() {
    try {
      // `setup()` drains the main looper and never becomes idle when the app intentionally
      // contains infinite ambient animations. `create()` still executes onCreate/setContent
      // and therefore keeps this as a useful launch smoke test without waiting for animations.
      val controller = Robolectric.buildActivity(MainActivity::class.java).create()
      val activity = controller.get()
      assertNotNull(activity)
    } catch (e: Throwable) {
      e.printStackTrace()
      throw e
    }
  }
}
