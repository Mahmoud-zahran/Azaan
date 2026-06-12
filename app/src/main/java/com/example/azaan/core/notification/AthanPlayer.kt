package com.example.azaan.core.notification

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import com.example.azaan.R

class AthanPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: Any? = null

    fun playAthan(onComplete: (() -> Unit)? = null) {
        stopAthan()
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        requestAudioFocus()

        try {
            val fd = context.resources.openRawResourceFd(R.raw.athan)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
                fd.close()
                setOnCompletionListener {
                    abandonAudioFocus()
                    stopAthan()
                    onComplete?.invoke()
                }
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            abandonAudioFocus()
            playFallback(onComplete)
        }
    }

    private fun playFallback(onComplete: (() -> Unit)? = null) {
        try {
            val alarmUri = android.media.RingtoneManager.getDefaultUri(
                android.media.RingtoneManager.TYPE_ALARM
            )
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(context, alarmUri)
                setOnCompletionListener {
                    abandonAudioFocus()
                    stopAthan()
                    onComplete?.invoke()
                }
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            abandonAudioFocus()
        }
    }

    fun stopAthan() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        abandonAudioFocus()
    }

    private fun requestAudioFocus() {
        val am = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener { _ -> }
                .build()
            audioFocusRequest = request
            am.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(
                null,
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    private fun abandonAudioFocus() {
        val am = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = audioFocusRequest as? AudioFocusRequest ?: return
            am.abandonAudioFocusRequest(request)
        } else {
            @Suppress("DEPRECATION")
            am.abandonAudioFocus(null)
        }
        audioFocusRequest = null
    }
}
