package com.example.ui.introspection

import android.content.Context
import org.json.JSONObject
import java.io.File

class IntrospectionStore(private val context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val recordingsDirectory = File(context.filesDir, RECORDINGS_DIRECTORY)

    fun hasSavedProgress(): Boolean {
        val raw = prefs.getString(KEY_PROGRESS, null) ?: return false
        val progress = load()
        return progress.hasStarted
    }

    fun load(): IntrospectionProgress {
        val raw = prefs.getString(KEY_PROGRESS, null) ?: return IntrospectionProgress()
        return runCatching {
            val json = JSONObject(raw)
            val answersJson = json.optJSONObject("answers") ?: JSONObject()
            val answers = buildMap {
                IntrospectionStage.entries.filter { it.isQuestion }.forEach { stage ->
                    val stored = answersJson.optJSONObject(stage.name) ?: return@forEach
                    when (stored.optString("type")) {
                        "text" -> {
                            val textVal = stored.optString("value", "")
                            if (textVal.isNotBlank()) {
                                put(stage, IntrospectionAnswer.Text(textVal))
                            }
                        }
                        "audio" -> {
                            val path = stored.optString("value", "")
                            if (path.isNotBlank()) {
                                put(stage, IntrospectionAnswer.Audio(path))
                            }
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
            if (answer.isValid()) {
                val encoded = JSONObject()
                when (answer) {
                    is IntrospectionAnswer.Text -> encoded.put("type", "text").put("value", answer.value)
                    is IntrospectionAnswer.Audio -> {
                        if (answer.filePath.isNotBlank()) {
                            encoded.put("type", "audio").put("value", answer.filePath)
                        }
                    }
                }
                if (encoded.has("value")) {
                    answersJson.put(stage.name, encoded)
                }
            }
        }
        val json = JSONObject()
            .put("stage", progress.stage.name)
            .put("completed", progress.completed)
            .put("updatedAt", progress.updatedAt)
            .put("answers", answersJson)
        prefs.edit().putString(KEY_PROGRESS, json.toString()).apply()
    }

    fun recordingFile(stage: IntrospectionStage): File {
        if (!recordingsDirectory.exists()) {
            recordingsDirectory.mkdirs()
        }
        return File(recordingsDirectory, "${stage.name.lowercase()}_${System.currentTimeMillis()}.m4a")
    }

    fun deleteOrphanedRecording(filePath: String?) {
        if (filePath.isNullOrBlank()) return
        runCatching {
            val file = File(filePath)
            if (file.exists() && file.canonicalPath.startsWith(recordingsDirectory.canonicalPath)) {
                file.delete()
            }
        }
    }

    fun clear(): IntrospectionProgress {
        runCatching {
            if (recordingsDirectory.exists()) {
                recordingsDirectory.listFiles()?.forEach { file ->
                    if (file.isFile) file.delete()
                }
            }
        }
        prefs.edit().remove(KEY_PROGRESS).apply()
        return IntrospectionProgress()
    }

    companion object {
        const val PREFS_NAME = "harmony_introspection"
        const val KEY_PROGRESS = "progress"
        const val RECORDINGS_DIRECTORY = "introspection_recordings"
    }
}
