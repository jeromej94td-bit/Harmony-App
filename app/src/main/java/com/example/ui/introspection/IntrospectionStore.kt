package com.example.ui.introspection

import android.content.Context
import org.json.JSONObject
import java.io.File

class IntrospectionStore(private val context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val recordingsDirectory = File(context.filesDir, RECORDINGS_DIRECTORY)

    fun load(): IntrospectionProgress {
        val raw = prefs.getString(KEY_PROGRESS, null) ?: return IntrospectionProgress()
        return runCatching {
            val json = JSONObject(raw)
            val answersJson = json.optJSONObject("answers") ?: JSONObject()
            val answers = buildMap {
                IntrospectionStage.entries.filter { it.isQuestion }.forEach { stage ->
                    val stored = answersJson.optJSONObject(stage.name) ?: return@forEach
                    when (stored.optString("type")) {
                        "text" -> put(stage, IntrospectionAnswer.Text(stored.optString("value")))
                        "audio" -> stored.optString("value").takeIf { File(it).isFile }?.let {
                            put(stage, IntrospectionAnswer.Audio(it))
                        }
                    }
                }
            }
            IntrospectionProgress(
                stage = IntrospectionStage.valueOf(json.optString("stage", IntrospectionStage.COLOR.name)),
                answers = answers,
                completed = json.optBoolean("completed", false),
                updatedAt = json.optLong("updatedAt", System.currentTimeMillis())
            )
        }.getOrDefault(IntrospectionProgress())
    }

    fun save(progress: IntrospectionProgress) {
        val answersJson = JSONObject()
        progress.answers.forEach { (stage, answer) ->
            val encoded = JSONObject()
            when (answer) {
                is IntrospectionAnswer.Text -> encoded.put("type", "text").put("value", answer.value)
                is IntrospectionAnswer.Audio -> encoded.put("type", "audio").put("value", answer.filePath)
            }
            answersJson.put(stage.name, encoded)
        }
        val json = JSONObject()
            .put("stage", progress.stage.name)
            .put("completed", progress.completed)
            .put("updatedAt", progress.updatedAt)
            .put("answers", answersJson)
        prefs.edit().putString(KEY_PROGRESS, json.toString()).apply()
    }

    fun recordingFile(stage: IntrospectionStage): File {
        recordingsDirectory.mkdirs()
        return File(recordingsDirectory, "${stage.name.lowercase()}_${System.currentTimeMillis()}.m4a")
    }

    fun clear(): IntrospectionProgress {
        recordingsDirectory.listFiles()?.forEach { file -> if (file.isFile) file.delete() }
        prefs.edit().remove(KEY_PROGRESS).apply()
        return IntrospectionProgress()
    }

    companion object {
        private const val PREFS_NAME = "harmony_introspection"
        private const val KEY_PROGRESS = "progress"
        private const val RECORDINGS_DIRECTORY = "introspection_recordings"
    }
}
