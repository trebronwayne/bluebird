package io.github.norbertweb.bluebird.ui.components

import android.content.Context

/**
 * Settings for the recurring "build apps with AI" BPK reminder toast.
 * Enabled by default; the person can switch it off in Settings > BPK apps > Reminders.
 */
object BpkReminderPrefs {
    private const val PREFS = "bpk_reminder_prefs"

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("enabled", true)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("enabled", enabled).apply()
    }

    fun lastShownAt(context: Context): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong("last_shown", 0L)

    fun nextIndex(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("next_index", 0)

    fun markShown(context: Context, now: Long, nextIndex: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong("last_shown", now).putInt("next_index", nextIndex).apply()
    }
}
