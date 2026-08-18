package com.example.ui.introspection

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class IntrospectionStoreTest {

    private lateinit var context: Context
    private lateinit var store: IntrospectionStore

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        store = IntrospectionStore(context)
        store.clear()
    }

    @Test
    fun `initial store is empty and has no saved progress`() {
        val initial = store.load()
        assertFalse(initial.hasStarted)
        assertFalse(initial.completed)
        assertFalse(store.hasSavedProgress())
        assertEquals(IntrospectionStage.COLOR, initial.stage)
    }

    @Test
    fun `save and load persists all text answers accurately`() {
        val progress = IntrospectionProgress(
            stage = IntrospectionStage.RESULTS,
            completed = true,
            answers = mapOf(
                IntrospectionStage.COLOR to IntrospectionAnswer.Text("Smaragdgrün"),
                IntrospectionStage.ANIMAL to IntrospectionAnswer.Text("Schneeleopard"),
                IntrospectionStage.WATER to IntrospectionAnswer.Text("Klarer Bergsee")
            )
        )

        store.save(progress)
        assertTrue(store.hasSavedProgress())

        val loaded = store.load()
        assertEquals(IntrospectionStage.RESULTS, loaded.stage)
        assertTrue(loaded.completed)
        assertEquals(3, loaded.answers.size)
        assertEquals(IntrospectionAnswer.Text("Smaragdgrün"), loaded.answers[IntrospectionStage.COLOR])
        assertEquals(IntrospectionAnswer.Text("Schneeleopard"), loaded.answers[IntrospectionStage.ANIMAL])
        assertEquals(IntrospectionAnswer.Text("Klarer Bergsee"), loaded.answers[IntrospectionStage.WATER])
    }

    @Test
    fun `save and load persists mixed text and audio answers accurately`() {
        val audioFile = store.recordingFile(IntrospectionStage.ANIMAL)
        audioFile.writeBytes(byteArrayOf(1, 2, 3, 4)) // simulate existing file

        val progress = IntrospectionProgress(
            stage = IntrospectionStage.WATER,
            completed = false,
            answers = mapOf(
                IntrospectionStage.COLOR to IntrospectionAnswer.Text("Smaragdgrün"),
                IntrospectionStage.ANIMAL to IntrospectionAnswer.Audio(audioFile.absolutePath)
            )
        )

        store.save(progress)
        assertTrue(store.hasSavedProgress())

        val loaded = store.load()
        assertEquals(IntrospectionStage.WATER, loaded.stage)
        assertFalse(loaded.completed)
        assertEquals(2, loaded.answers.size)
        assertEquals(IntrospectionAnswer.Text("Smaragdgrün"), loaded.answers[IntrospectionStage.COLOR])
        assertEquals(
            IntrospectionAnswer.Audio(audioFile.absolutePath),
            loaded.answers[IntrospectionStage.ANIMAL]
        )
    }

    @Test
    fun `missing audio file does not crash and handles gracefully`() {
        val nonExistentPath = "/non/existent/path/introspection_color.m4a"
        val progress = IntrospectionProgress(
            stage = IntrospectionStage.WATER,
            completed = false,
            answers = mapOf(
                IntrospectionStage.COLOR to IntrospectionAnswer.Audio(nonExistentPath)
            )
        )

        store.save(progress)
        val loaded = store.load()
        val loadedAnswer = loaded.answers[IntrospectionStage.COLOR] as? IntrospectionAnswer.Audio
        assertNotNull(loadedAnswer)
        val file = File(loadedAnswer!!.filePath)
        assertFalse(file.exists()) // Verification that missing file status is detected safely
    }

    @Test
    fun `clear resets state and deletes only introspection files and data`() {
        // Setup another app pref to verify clear() does NOT delete other app settings
        val otherAppPrefs = context.getSharedPreferences("harmony_app_settings", Context.MODE_PRIVATE)
        otherAppPrefs.edit().putString("test_user_key", "important_value").commit()

        val audioFile = store.recordingFile(IntrospectionStage.COLOR)
        audioFile.writeBytes(byteArrayOf(1, 2, 3))
        assertTrue(audioFile.exists())

        val progress = IntrospectionProgress(
            stage = IntrospectionStage.RESULTS,
            completed = true,
            answers = mapOf(
                IntrospectionStage.COLOR to IntrospectionAnswer.Audio(audioFile.absolutePath),
                IntrospectionStage.ANIMAL to IntrospectionAnswer.Text("Katze")
            )
        )
        store.save(progress)
        assertTrue(store.hasSavedProgress())

        val cleared = store.clear()
        assertFalse(cleared.completed)
        assertFalse(cleared.hasStarted)
        assertTrue(cleared.answers.isEmpty())
        assertFalse(store.hasSavedProgress())
        assertFalse(audioFile.exists()) // Introspection audio file removed

        // Other app prefs must be untouched!
        assertEquals("important_value", otherAppPrefs.getString("test_user_key", null))
    }

    @Test
    fun `recordingFile creates deterministic file path in files directory`() {
        val file = store.recordingFile(IntrospectionStage.COLOR)
        assertNotNull(file)
        assertTrue(file.name.contains("color"))
        assertTrue(file.name.endsWith(".m4a"))
    }
}
