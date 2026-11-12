package io.github.norbertweb.bluebird.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import io.github.norbertweb.bluebird.R

/**
 * The short sound that plays when a notification toast appears (res/raw/notification_sound.ogg).
 *
 * It uses the notification audio stream, so it follows the phone's notification volume, and it stays silent when
 * the phone is on silent or vibrate. On by default; turned off in Settings → System → Notifications
 * ([DesktopPreferences.notificationSound]).
 */
object NotificationSound {
    private var pool: SoundPool? = null
    private var soundId = 0
    @Volatile private var loaded = false

    /** Loads the sound ahead of time so the first toast is not silent. Safe to call many times. */
    @Synchronized
    fun warmUp(context: Context) {
        if (pool != null) return
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val newPool = SoundPool.Builder().setMaxStreams(2).setAudioAttributes(attributes).build()
        newPool.setOnLoadCompleteListener { _, id, status -> if (status == 0 && id == soundId) loaded = true }
        soundId = newPool.load(context.applicationContext, R.raw.notification_sound, 1)
        pool = newPool
    }

    /** Plays the sound once, unless the phone is silent or on vibrate. */
    fun play(context: Context) {
        val audio = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        if (audio != null && audio.ringerMode != AudioManager.RINGER_MODE_NORMAL) return
        warmUp(context)
        val current = pool ?: return
        if (loaded) current.play(soundId, 1f, 1f, 1, 0, 1f)
    }
}
