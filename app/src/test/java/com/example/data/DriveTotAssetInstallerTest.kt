package com.example.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DriveTotAssetInstallerTest {

    @Test
    fun testInstallDoesNotCrash() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        try {
            val result = DriveTotAssetInstaller.install(context)
            println("DriveTotAssetInstaller result count: " + result.size)
            assertNotNull(result)
            assertEquals(result, DriveTotAssetInstaller.install(context))
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }
}
