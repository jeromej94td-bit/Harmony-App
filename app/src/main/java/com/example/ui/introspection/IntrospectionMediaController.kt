package com.example.ui.introspection

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
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

    private fun getMediaAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
    }

    private fun requestAudioFocus(): Boolean {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (audioFocusRequest == null) {
                    val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(getMediaAudioAttributes())
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener { focusChange ->
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

    private fun createPlayerForRawResource(resId: Int): MediaPlayer? {
        // Method 1: Standard MediaPlayer.create with AudioAttributes
        try {
            val mp = MediaPlayer.create(context, resId, getMediaAudioAttributes(), 0)
            if (mp != null) {
                return mp
            }
        } catch (e: Exception) {
            android.util.Log.w("IntrospectionMedia", "MediaPlayer.create with AudioAttributes failed for $resId", e)
        }

        // Method 2: Standard MediaPlayer.create
        try {
            val mp = MediaPlayer.create(context, resId)
            if (mp != null) {
                return mp
            }
        } catch (e: Exception) {
            android.util.Log.w("IntrospectionMedia", "MediaPlayer.create standard failed for $resId", e)
        }

        // Method 3: URI fallback
        try {
            val uri = android.net.Uri.parse("android.resource://${context.packageName}/$resId")
            val mp = MediaPlayer().apply {
                setAudioAttributes(getMediaAudioAttributes())
                setDataSource(context, uri)
                prepare()
            }
            return mp
        } catch (e: Exception) {
            android.util.Log.e("IntrospectionMedia", "URI fallback failed for $resId", e)
        }

        return null
    }

    // --- Background Music ---

    fun startBackgroundMusic() {
        if (_isRecording.value) return
        if (musicPlayer != null) {
            resumeBackgroundMusic()
            return
        }
        runCatching {
            requestAudioFocus()
            val player = createPlayerForRawResource(R.raw.merlin_theme) ?: return
            player.isLooping = true
            val initialVol = if (_isNarratorPlaying.value) {
                IntrospectionConstants.NARRATION_MUSIC_VOLUME
            } else {
                IntrospectionConstants.NORMAL_MUSIC_VOLUME
            }
            player.setVolume(initialVol, initialVol)
            player.start()
            musicPlayer = player
            _isMusicPlaying.value = true
        }.onFailure {
            _errorMessage.value = it.message
        }
    }

    fun pauseBackgroundMusic() {
        runCatching {
            musicPlayer?.let { player ->
                if (player.isPlaying) {
                    savedMusicPositionMs = player.currentPosition
                    player.pause()
                    _isMusicPlaying.value = false
                }
            }
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
                }
            } ?: startBackgroundMusic()
        }
    }

    private fun setMusicVolume(volume: Float) {
        runCatching {
            musicPlayer?.setVolume(volume, volume)
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
        playNarrator(rawRes, onComplete)
    }

    fun playNarrator(rawResId: Int, onComplete: () -> Unit) {
        stopNarrator()
        stopAnswerAudio()

        // Duck background music to exactly 0.68
        setMusicVolume(IntrospectionConstants.NARRATION_MUSIC_VOLUME)

        runCatching {
            requestAudioFocus()
            val player = createPlayerForRawResource(rawResId)
            if (player == null) {
                _isNarratorPlaying.value = false
                _isNarratorCompleted.value = true
                setMusicVolume(IntrospectionConstants.NORMAL_MUSIC_VOLUME)
                onComplete()
                return
            }
            narratorPlayer = player
            _isNarratorPlaying.value = true
            _isNarratorCompleted.value = false

            player.setOnCompletionListener {
                _isNarratorPlaying.value = false
                _isNarratorCompleted.value = true
                setMusicVolume(IntrospectionConstants.NORMAL_MUSIC_VOLUME)
                onComplete()
            }
            player.setOnErrorListener { _, _, _ ->
                _isNarratorPlaying.value = false
                _isNarratorCompleted.value = true
                setMusicVolume(IntrospectionConstants.NORMAL_MUSIC_VOLUME)
                onComplete()
                true
            }
            player.start()
        }.onFailure {
            _isNarratorPlaying.value = false
            _isNarratorCompleted.value = true
            setMusicVolume(IntrospectionConstants.NORMAL_MUSIC_VOLUME)
            onComplete()
        }
    }

    fun pauseNarrator() {
        runCatching {
            if (narratorPlayer?.isPlaying == true) {
                narratorPlayer?.pause()
                _isNarratorPlaying.value = false
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
                        stopRecordingInternal()
                        onMaxReached(outputFile)
                        break
                    }
                    delay(100)
                }
            }
            true
        }.getOrElse {
            _isRecording.value = false
            _errorMessage.value = it.message
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
            true
        }.getOrElse {
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
            }
        }
        resumeBackgroundMusic()
    }

    // --- Answer Audio Playback ---

    fun playAnswerAudio(file: File, stage: IntrospectionStage, onComplete: () -> Unit = {}) {
        if (!file.exists() || !file.canRead() || file.length() == 0L) {
            _errorMessage.value = "Audio file not found"
            return
        }

        stopNarrator()
        stopAnswerAudio()

        // Duck background music for clear voice response
        setMusicVolume(IntrospectionConstants.ANSWER_PLAYBACK_MUSIC_VOLUME)

        runCatching {
            requestAudioFocus()
            val player = MediaPlayer().apply {
                setAudioAttributes(getMediaAudioAttributes())
                setDataSource(file.absolutePath)
                prepare()
            }
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
                stopAnswerAudio()
                onComplete()
            }
            player.setOnErrorListener { _, _, _ ->
                stopAnswerAudio()
                true
            }
            player.start()
        }.onFailure {
            stopAnswerAudio()
        }
    }

    fun pauseAnswerAudio() {
        runCatching {
            answerPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    _isAnswerPlaying.value = false
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
    }
}
