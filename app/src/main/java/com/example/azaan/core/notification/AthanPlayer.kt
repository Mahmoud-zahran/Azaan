package com.example.azaan.core.notification

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.example.azaan.R

class AthanPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun playAthan(onComplete: (() -> Unit)? = null) {
        stopAthan()
        try {
            mediaPlayer = MediaPlayer.create(context, R.raw.athan).apply {
                setOnCompletionListener {
                    stopAthan()
                    onComplete?.invoke()
                }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            playFallback(onComplete)
        }
    }

    private fun playFallback(onComplete: (() -> Unit)? = null) {
        try {
            val alarmUri = android.media.RingtoneManager.getDefaultUri(
                android.media.RingtoneManager.TYPE_ALARM
            )
            mediaPlayer = MediaPlayer.create(context, alarmUri).apply {
                setOnCompletionListener {
                    stopAthan()
                    onComplete?.invoke()
                }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAthan() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
