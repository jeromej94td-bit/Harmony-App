package com.example.ui.introspection

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class IntrospectionMediaController(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    companion object {
        private const val TAG = "IntrospectionMedia"
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private var musicPlayer: MediaPlayer? = null
    private var narratorPlayer: MediaPlayer? = null
    private var answerPlayer: MediaPlayer? = null
    private var recorder: MediaRecorder? = null

    private var savedMusicPositionMs: Int = 0
    private var recordingTimerJob: Job? = null
    private var answerProgressJob: Job? = null

    private val _isMusicPlaying = MutableStateFlow(false)
    val isMusicPlaying: StateFlow<Boolean> = _isMusicPlaying.asStateFlow()

    private val _isNarratorPlaying = MutableStateFlow(false)
    val isNarratorPlaying: StateFlow<Boolean> = _isNarratorPlaying.asStateFlow()

    private val _isNarratorCompleted = MutableStateFlow(false)
    val isNarratorCompleted: StateFlow<Boolean> = _isNarratorCompleted.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDurationMs = MutableStateFlow(0L)
    val recordingDurationMs: StateFlow<Long> = _recordingDurationMs.asStateFlow()

    private val _isAnswerPlaying = MutableStateFlow(false)
    val isAnswerPlaying: StateFlow<Boolean> = _isAnswerPlaying.asStateFlow()

    private val _answerProgress = MutableStateFlow(0f)
    val answerProgress: StateFlow<Float> = _answerProgress.asStateFlow()

    private val _activeAnswerStage = MutableStateFlow<IntrospectionStage?>(null)
    val activeAnswerStage: StateFlow<IntrospectionStage?> = _activeAnswerStage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var audioFocusRequest: AudioFocusRequest? = null

    private fun getMusicAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
    }

    private fun getSpeechAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    private fun requestAudioFocus(): Boolean {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (audioFocusRequest == null) {
                    val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(getMusicAudioAttributes())
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener { focusChange ->
                            Log.d(TAG, "Audio focus changed: $focusChange")
                            when (focusChange) {
                                AudioManager.AUDIOFOCUS_LOSS,
                                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                                    pauseBackgroundMusic()
                                    pauseNarrator()
                                    pauseAnswerAudio()
                                }
                                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                                    setMusicVolume(0.2f)
                                }
                                AudioManager.AUDIOFOCUS_GAIN -> {
                                    if (!_isRecording.value) {
                                        val targetVol = if (_isNarratorPlaying.value) {
                                            IntrospectionConstants.NARRATION_MUSIC_VOLUME
                                        } else if (_isAnswerPlaying.value) {
                                            IntrospectionConstants.ANSWER_PLAYBACK_MUSIC_VOLUME
                                        } else {
                                            IntrospectionConstants.NORMAL_MUSIC_VOLUME
                                        }
                                        setMusicVolume(targetVol)
                                        resumeBackgroundMusic()
                                    }
                                }
                            }
                        }
                        .build()
                    audioFocusRequest = request
                }
                val res = audioManager?.requestAudioFocus(audioFocusRequest!!)
                res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            } else {
                @Suppress("DEPRECATION")
                val res = audioManager?.requestAudioFocus(
                    { focusChange ->
                        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                            pauseBackgroundMusic()
                        }
                    },
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                )
                res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            }
        }.getOrDefault(true)
    }

    private fun createPlayerForRawResource(resId: Int, isSpeech: Boolean = false): MediaPlayer? {
        val audioAttributes = if (isSpeech) getSpeechAudioAttributes() else getMusicAudioAttributes()
        val resName = runCatching { context.resources.getResourceEntryName(resId) }.getOrDefault("$resId")
        Log.d(TAG, "createPlayerForRawResource starting for res=$resName (id=$resId, isSpeech=$isSpeech)")

        // Method 1: openRawResourceFd (preferred for bundled raw assets)
        try {
            val afd = context.resources.openRawResourceFd(resId)
            if (afd != null) {
                Log.d(TAG, "Strategy 1 (openRawResourceFd): opened afd for $resName (length=${afd.declaredLength})")
                val mp = MediaPlayer().apply {
                    setAudioAttributes(audioAttributes)
                    if (afd.declaredLength < 0) {
                        setDataSource(afd.fileDescriptor)
                    } else {
                        setDataSource(afd.fileDescriptor, afd.startOffset, afd.declaredLength)
                    }
                    prepare()
                }
                afd.close()
                Log.i(TAG, "Strategy 1 SUCCESS for $resName: duration=${mp.duration}ms")
                return mp
            }
        } catch (e: Exception) {
            Log.w(TAG, "Strategy 1 (openRawResourceFd) failed for $resName: ${e.message}")
        }

        // Method 2: Standard MediaPlayer.create with AudioAttributes
        try {
            val mp = MediaPlayer.create(context, resId, audioAttributes, 0)
            if (mp != null) {
                Log.i(TAG, "Strategy 2 (MediaPlayer.create + attributes) SUCCESS for $resName: duration=${mp.duration}ms")
                return mp
            }
        } catch (e: Exception) {
            Log.w(TAG, "Strategy 2 failed for $resName: ${e.message}")
        }

        // Method 3: Standard MediaPlayer.create
        try {
            val mp = MediaPlayer.create(context, resId)
            if (mp != null) {
                Log.i(TAG, "Strategy 3 (MediaPlayer.create standard) SUCCESS for $resName: duration=${mp.duration}ms")
                return mp
            }
        } catch (e: Exception) {
            Log.w(TAG, "Strategy 3 failed for $resName: ${e.message}")
        }

        // Method 4: Cache File Fallback (copy raw stream to cache directory)
        try {
            val cacheFile = File(context.cacheDir, "raw_audio_${resName}.mp3")
            if (!cacheFile.exists() || cacheFile.length() == 0L) {
                context.resources.openRawResource(resId).use { input ->
                    cacheFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            if (cacheFile.exists() && cacheFile.length() > 0L) {
                Log.d(TAG, "Strategy 4 (Cache file): file size=${cacheFile.length()} bytes at ${cacheFile.absolutePath}")
                val mp = MediaPlayer().apply {
                    setAudioAttributes(audioAttributes)
                    setDataSource(cacheFile.absolutePath)
                    prepare()
                }
                Log.i(TAG, "Strategy 4 (Cache file fallback) SUCCESS for $resName: duration=${mp.duration}ms")
                return mp
            }
        } catch (e: Exception) {
            Log.w(TAG, "Strategy 4 (Cache file fallback) failed for $resName: ${e.message}")
        }

        // Method 5: URI fallback
        try {
            val uri = Uri.parse("android.resource://${context.packageName}/$resId")
            val mp = MediaPlayer().apply {
                setAudioAttributes(audioAttributes)
                setDataSource(context, uri)
                prepare()
            }
            Log.i(TAG, "Strategy 5 (URI fallback) SUCCESS for $resName: duration=${mp.duration}ms")
            return mp
        } catch (e: Exception) {
            Log.e(TAG, "Strategy 5 (URI fallback) failed for $resName: ${e.message}", e)
        }

        val errMsg = "Audiodatei $resName konnte nicht geladen werden"
        Log.e(TAG, "All audio loading strategies failed for $resName (id=$resId)")
        _errorMessage.value = errMsg
        return null
    }

    // --- Background Music ---

    fun startBackgroundMusic() {
        if (_isRecording.value) {
            Log.d(TAG, "startBackgroundMusic skipped: recording in progress")
            return
        }
        if (musicPlayer != null) {
            Log.d(TAG, "startBackgroundMusic: player already exists, resuming")
            resumeBackgroundMusic()
            return
        }
        runCatching {
            requestAudioFocus()
            val player = createPlayerForRawResource(R.raw.merlin_theme, isSpeech = false)
            if (player == null) {
                Log.e(TAG, "startBackgroundMusic: createPlayerForRawResource returned null for merlin_theme")
                _errorMessage.value = "Hintergrundmusik konnte nicht geladen werden"
                return
            }
            player.isLooping = true
            val initialVol = if (_isNarratorPlaying.value) {
                IntrospectionConstants.NARRATION_MUSIC_VOLUME
            } else {
                IntrospectionConstants.NORMAL_MUSIC_VOLUME
            }
            player.setVolume(initialVol, initialVol)
            player.setOnErrorListener { _, what, extra ->
                Log.e(TAG, "musicPlayer error: what=$what, extra=$extra")
                _isMusicPlaying.value = false
                _errorMessage.value = "Hintergrundmusik-Wiedergabefehler ($what)"
                true
            }
            player.start()
            musicPlayer = player
            _isMusicPlaying.value = true
            Log.i(TAG, "Background music started successfully (looping=true, volume=$initialVol)")
        }.onFailure { e ->
            Log.e(TAG, "startBackgroundMusic failed: ${e.message}", e)
            _errorMessage.value = "Fehler beim Starten der Hintergrundmusik"
        }
    }

    fun pauseBackgroundMusic() {
        runCatching {
            musicPlayer?.let { player ->
                if (player.isPlaying) {
                    savedMusicPositionMs = player.currentPosition
                    player.pause()
                    _isMusicPlaying.value = false
                    Log.d(TAG, "Background music paused at ${savedMusicPositionMs}ms")
                }
            }
        }.onFailure { e ->
            Log.w(TAG, "pauseBackgroundMusic error: ${e.message}")
        }
    }

    fun resumeBackgroundMusic() {
        if (_isRecording.value) return
        runCatching {
            musicPlayer?.let { player ->
                if (!player.isPlaying) {
                    if (savedMusicPositionMs > 0) {
                        player.seekTo(savedMusicPositionMs)
                    }
                    val targetVol = if (_isNarratorPlaying.value) {
                        IntrospectionConstants.NARRATION_MUSIC_VOLUME
                    } else if (_isAnswerPlaying.value) {
                        IntrospectionConstants.ANSWER_PLAYBACK_MUSIC_VOLUME
                    } else {
                        IntrospectionConstants.NORMAL_MUSIC_VOLUME
                    }
                    player.setVolume(targetVol, targetVol)
                    player.start()
                    _isMusicPlaying.value = true
                    Log.d(TAG, "Background music resumed (position=${player.currentPosition}ms, volume=$targetVol)")
                }
            } ?: startBackgroundMusic()
        }.onFailure { e ->
            Log.w(TAG, "resumeBackgroundMusic error: ${e.message}")
        }
    }

    private fun setMusicVolume(volume: Float) {
        runCatching {
            musicPlayer?.setVolume(volume, volume)
            Log.d(TAG, "setMusicVolume -> $volume")
        }
    }

    // --- Narrator Audio ---

    fun playNarratorForStage(stage: IntrospectionStage, onComplete: () -> Unit = {}) {
        val rawRes = when (stage) {
            IntrospectionStage.COLOR -> R.raw.introspection_color
            IntrospectionStage.ANIMAL -> R.raw.introspection_animal
            IntrospectionStage.WATER -> R.raw.introspection_water
            IntrospectionStage.REVELATION -> R.raw.introspection_reveal
            IntrospectionStage.RESULTS -> return
        }
        Log.i(TAG, "playNarratorForStage: stage=$stage, resId=$rawRes")
        playNarrator(rawRes, onComplete)
    }

    fun playNarrator(rawResId: Int, onComplete: () -> Unit) {
        stopNarrator()
        stopAnswerAudio()

        // Duck background music for clear spoken voice
        setMusicVolume(IntrospectionConstants.NARRATION_MUSIC_VOLUME)

        runCatching {
            requestAudioFocus()
            val player = createPlayerForRawResource(rawResId, isSpeech = true)
            if (player == null) {
                Log.e(TAG, "playNarrator: createPlayerForRawResource returned null for $rawResId")
                _isNarratorPlaying.value = false
                _isNarratorCompleted.value = true
                setMusicVolume(IntrospectionConstants.NORMAL_MUSIC_VOLUME)
                onComplete()
                return
            }
            player.setVolume(1.0f, 1.0f)
            narratorPlayer = player
            _isNarratorPlaying.value = true
            _isNarratorCompleted.value = false

            player.setOnCompletionListener {
                Log.i(TAG, "Narrator playback completed for $rawResId")
                _isNarratorPlaying.value = false
                _isNarratorCompleted.value = true
                setMusicVolume(IntrospectionConstants.NORMAL_MUSIC_VOLUME)
                onComplete()
            }
            player.setOnErrorListener { _, what, extra ->
                Log.e(TAG, "Narrator playback error for $rawResId: what=$what, extra=$extra")
                _isNarratorPlaying.value = false
                _isNarratorCompleted.value = true
                _errorMessage.value = "Erzähler-Wiedergabefehler ($what)"
                setMusicVolume(IntrospectionConstants.NORMAL_MUSIC_VOLUME)
                onComplete()
                true
            }
            player.start()
            Log.i(TAG, "Narrator started playing for $rawResId (duration=${player.duration}ms)")
        }.onFailure { e ->
            Log.e(TAG, "playNarrator exception for $rawResId: ${e.message}", e)
            _isNarratorPlaying.value = false
            _isNarratorCompleted.value = true
            _errorMessage.value = "Fehler bei der Erzählerwiedergabe"
            setMusicVolume(IntrospectionConstants.NORMAL_MUSIC_VOLUME)
            onComplete()
        }
    }

    fun pauseNarrator() {
        runCatching {
            if (narratorPlayer?.isPlaying == true) {
                narratorPlayer?.pause()
                _isNarratorPlaying.value = false
                Log.d(TAG, "Narrator paused")
            }
        }
    }

    fun stopNarrator() {
        runCatching {
            narratorPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
                Log.d(TAG, "Narrator stopped and released")
            }
        }
        narratorPlayer = null
        _isNarratorPlaying.value = false
        if (!_isRecording.value && !_isAnswerPlaying.value) {
            setMusicVolume(IntrospectionConstants.NORMAL_MUSIC_VOLUME)
        }
    }

    // --- Audio Recording ---

    fun startRecording(
        outputFile: File,
        onMaxReached: (File) -> Unit
    ): Boolean {
        // Pause music & stop other playbacks
        pauseBackgroundMusic()
        stopNarrator()
        stopAnswerAudio()

        Log.i(TAG, "startRecording: destination=${outputFile.absolutePath}")

        return runCatching {
            val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            newRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            recorder = newRecorder
            _isRecording.value = true
            _recordingDurationMs.value = 0L

            recordingTimerJob?.cancel()
            recordingTimerJob = coroutineScope.launch(Dispatchers.Main) {
                val startTime = System.currentTimeMillis()
                while (isActive && _isRecording.value) {
                    val elapsed = System.currentTimeMillis() - startTime
                    _recordingDurationMs.value = elapsed
                    if (elapsed >= IntrospectionConstants.MAX_RECORDING_DURATION_MS) {
                        Log.i(TAG, "Max recording duration reached (${elapsed}ms)")
                        stopRecordingInternal()
                        onMaxReached(outputFile)
                        break
                    }
                    delay(100)
                }
            }
            Log.i(TAG, "Audio recording started successfully")
            true
        }.getOrElse { e ->
            Log.e(TAG, "startRecording failed: ${e.message}", e)
            _isRecording.value = false
            _errorMessage.value = "Mikrofonaufnahme fehlgeschlagen: ${e.message}"
            resumeBackgroundMusic()
            false
        }
    }

    private fun stopRecordingInternal(): Boolean {
        recordingTimerJob?.cancel()
        recordingTimerJob = null
        val rec = recorder ?: return false
        return runCatching {
            rec.stop()
            rec.release()
            recorder = null
            _isRecording.value = false
            Log.i(TAG, "Audio recording stopped and saved")
            true
        }.getOrElse { e ->
            Log.w(TAG, "stopRecordingInternal exception: ${e.message}")
            runCatching { rec.release() }
            recorder = null
            _isRecording.value = false
            false
        }
    }

    fun stopRecording(): Boolean {
        val success = stopRecordingInternal()
        resumeBackgroundMusic()
        return success
    }

    fun discardRecording(file: File?) {
        stopRecordingInternal()
        runCatching {
            if (file != null && file.exists()) {
                file.delete()
                Log.d(TAG, "Recording file discarded: ${file.absolutePath}")
            }
        }
        resumeBackgroundMusic()
    }

    // --- Answer Audio Playback ---

    fun playAnswerAudio(file: File, stage: IntrospectionStage, onComplete: () -> Unit = {}) {
        if (!file.exists() || !file.canRead() || file.length() == 0L) {
            Log.e(TAG, "playAnswerAudio: invalid file ${file.absolutePath}")
            _errorMessage.value = "Aufnahmedatei nicht gefunden oder leer"
            return
        }

        stopNarrator()
        stopAnswerAudio()

        // Duck background music for clear voice response
        setMusicVolume(IntrospectionConstants.ANSWER_PLAYBACK_MUSIC_VOLUME)

        Log.i(TAG, "playAnswerAudio: stage=$stage, size=${file.length()} bytes")

        runCatching {
            requestAudioFocus()
            val player = MediaPlayer().apply {
                setAudioAttributes(getSpeechAudioAttributes())
                setDataSource(file.absolutePath)
                prepare()
            }
            player.setVolume(1.0f, 1.0f)
            answerPlayer = player
            _isAnswerPlaying.value = true
            _activeAnswerStage.value = stage

            answerProgressJob?.cancel()
            answerProgressJob = coroutineScope.launch(Dispatchers.Main) {
                while (isActive && _isAnswerPlaying.value) {
                    val total = player.duration
                    if (total > 0) {
                        _answerProgress.value = (player.currentPosition.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                    }
                    delay(50)
                }
            }

            player.setOnCompletionListener {
                Log.i(TAG, "Answer audio playback finished for $stage")
                stopAnswerAudio()
                onComplete()
            }
            player.setOnErrorListener { _, what, extra ->
                Log.e(TAG, "Answer audio error for $stage: what=$what, extra=$extra")
                stopAnswerAudio()
                _errorMessage.value = "Audioantwort-Wiedergabefehler ($what)"
                true
            }
            player.start()
            Log.i(TAG, "Answer audio playback started (duration=${player.duration}ms)")
        }.onFailure { e ->
            Log.e(TAG, "playAnswerAudio exception: ${e.message}", e)
            stopAnswerAudio()
            _errorMessage.value = "Fehler beim Abspielen der Audioantwort"
        }
    }

    fun pauseAnswerAudio() {
        runCatching {
            answerPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    _isAnswerPlaying.value = false
                    Log.d(TAG, "Answer audio paused")
                }
            }
        }
        answerProgressJob?.cancel()
        setMusicVolume(IntrospectionConstants.NORMAL_MUSIC_VOLUME)
    }

    fun stopAnswerAudio() {
        answerProgressJob?.cancel()
        answerProgressJob = null
        runCatching {
            answerPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
                Log.d(TAG, "Answer audio stopped and released")
            }
        }
        answerPlayer = null
        _isAnswerPlaying.value = false
        _activeAnswerStage.value = null
        _answerProgress.value = 0f
        if (!_isRecording.value && !_isNarratorPlaying.value) {
            setMusicVolume(IntrospectionConstants.NORMAL_MUSIC_VOLUME)
        }
    }

    // --- Cleanup ---

    fun releaseAll() {
        Log.i(TAG, "releaseAll: releasing all audio resources")
        recordingTimerJob?.cancel()
        answerProgressJob?.cancel()

        stopRecordingInternal()
        stopNarrator()
        stopAnswerAudio()

        runCatching {
            musicPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
        }
        musicPlayer = null
        _isMusicPlaying.value = false

        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
                audioManager?.abandonAudioFocusRequest(audioFocusRequest!!)
            } else {
                @Suppress("DEPRECATION")
                audioManager?.abandonAudioFocus(null)
            }
        }
        Log.i(TAG, "releaseAll completed")
    }
}

