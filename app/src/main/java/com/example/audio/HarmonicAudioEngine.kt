package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

object HarmonicAudioEngine {

    var isSoundEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) {
                pauseBgm()
            } else {
                resumeBgm()
            }
        }

    private var appContext: Context? = null
    private var bgmPlayer: MediaPlayer? = null
    private var activeSfxPlayer: MediaPlayer? = null
    private var savedBgmPosition: Int = 0
    private val audioScope = CoroutineScope(Dispatchers.Default)

    private const val NORMAL_BGM_VOLUME = 0.70f

    private val pentatonicScale = floatArrayOf(
        261.63f, // C4
        293.66f, // D4
        329.63f, // E4
        392.00f, // G4
        440.00f, // A4
        523.25f, // C5
        587.33f, // D5
        659.25f, // E5
        783.99f, // G5
        880.00f  // A5
    )

    /**
     * Initializes the audio engine and starts game background music in a loop.
     */
    fun init(context: Context) {
        appContext = context.applicationContext
        startBgm()
    }

    /**
     * Starts game music in a loop immediately when game starts.
     */
    fun startBgm() {
        if (!isSoundEnabled) return
        val ctx = appContext ?: return
        try {
            if (bgmPlayer == null) {
                bgmPlayer = MediaPlayer.create(ctx, R.raw.bgm_game_music)?.apply {
                    isLooping = true
                    setVolume(NORMAL_BGM_VOLUME, NORMAL_BGM_VOLUME)
                    if (savedBgmPosition > 0) {
                        seekTo(savedBgmPosition)
                    }
                    start()
                }
            } else if (bgmPlayer?.isPlaying == false) {
                if (savedBgmPosition > 0) {
                    try { bgmPlayer?.seekTo(savedBgmPosition) } catch (_: Exception) {}
                }
                bgmPlayer?.start()
            }
        } catch (_: Exception) {
            // Audio setup fallback
        }
    }

    fun pauseBgm() {
        try {
            if (bgmPlayer?.isPlaying == true) {
                savedBgmPosition = bgmPlayer?.currentPosition ?: 0
                bgmPlayer?.pause()
            }
        } catch (_: Exception) {}
    }

    fun resumeBgm() {
        if (!isSoundEnabled) return
        val ctx = appContext ?: return
        try {
            if (bgmPlayer == null) {
                startBgm()
            } else if (bgmPlayer?.isPlaying == false) {
                bgmPlayer?.setVolume(NORMAL_BGM_VOLUME, NORMAL_BGM_VOLUME)
                bgmPlayer?.start()
            }
        } catch (_: Exception) {}
    }

    /**
     * Plays an intervening SFX:
     * 1. Pauses the background music completely.
     * 2. Plays the SFX with crystal clarity.
     * 3. Once the SFX completes, resumes BGM exactly where it paused.
     */
    private fun playInterveningSfx(resId: Int) {
        if (!isSoundEnabled) return
        val ctx = appContext ?: return
        try {
            pauseBgm()

            // Stop any previous SFX cleanly
            try {
                activeSfxPlayer?.stop()
                activeSfxPlayer?.release()
            } catch (_: Exception) {}

            val player = MediaPlayer.create(ctx, resId)
            if (player != null) {
                activeSfxPlayer = player
                player.setVolume(0.95f, 0.95f)
                player.setOnCompletionListener { mp ->
                    try {
                        mp.release()
                    } catch (_: Exception) {}
                    activeSfxPlayer = null
                    // Intervening sound completed: Resume background music exactly from where it left off
                    resumeBgm()
                }
                player.start()
            } else {
                resumeBgm()
            }
        } catch (_: Exception) {
            resumeBgm()
        }
    }

    /**
     * Plays Next Level voice and sound effect cleanly.
     * Background music pauses while voice plays, then resumes immediately after.
     */
    fun playNextLevel() {
        playInterveningSfx(R.raw.sfx_next_level)
    }

    /**
     * Plays Win / Victory audio fanfare.
     * Background music pauses, fanfare plays, then BGM resumes.
     */
    fun playVictoryCascade() {
        playInterveningSfx(R.raw.sfx_win)
    }

    /**
     * Plays Collision Error / Red Line buzz sound.
     */
    fun playCollisionBuzz() {
        playInterveningSfx(R.raw.sfx_error)
    }

    /**
     * Plays Broken Red Line audio effect when special ability or breaker activates.
     */
    fun playBrokenRedLine() {
        playInterveningSfx(R.raw.sfx_broken_line)
    }

    fun playDrillBeam() {
        playBrokenRedLine()
    }

    /**
     * Plays Atagul Games Intro fanfare chime.
     */
    fun playIntroJingle() {
        if (!isSoundEnabled) return
        audioScope.launch {
            // Elegant 4-note ascending chord for Atagul Games intro
            playSynthTone(261.63f, 180, 0.45f) // C4
            delay(120)
            playSynthTone(329.63f, 200, 0.50f) // E4
            delay(140)
            playSynthTone(392.00f, 240, 0.55f) // G4
            delay(160)
            playSynthTone(523.25f, 600, 0.65f) // C5
        }
    }

    /**
     * Plays melodic harmonic tone when a node is connected during drawing.
     */
    fun playNodeTone(sequenceIndex: Int) {
        if (!isSoundEnabled) return
        val freq = pentatonicScale[sequenceIndex % pentatonicScale.size]
        audioScope.launch {
            playSynthTone(freq, durationMs = 110, volume = 0.55f)
        }
    }

    private fun playMediaResource(context: Context, resId: Int) {
        try {
            val player = MediaPlayer.create(context, resId) ?: return
            player.setOnCompletionListener {
                try {
                    it.release()
                } catch (_: Exception) {}
            }
            player.start()
        } catch (_: Exception) {
            // Fallback
        }
    }

    private fun playSynthTone(freq: Float, durationMs: Int, volume: Float) {
        val sampleRate = 22050
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = 1.0 - (i.toDouble() / numSamples)
            val sample = sin(2 * PI * freq * t) * envelope * volume
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        playPcm(buffer, sampleRate)
    }

    private fun playPcm(buffer: ShortArray, sampleRate: Int) {
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            track.setNotificationMarkerPosition(buffer.size)
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(t: AudioTrack?) {
                    try {
                        t?.stop()
                        t?.release()
                    } catch (_: Exception) {}
                }
                override fun onPeriodicNotification(t: AudioTrack?) {}
            })
        } catch (_: Exception) {}
    }
}
