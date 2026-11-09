package io.github.norbertweb.bluebird.ui.components

import android.content.Context
import io.github.norbertweb.bluebird.LauncherScreen
import org.json.JSONArray
import org.json.JSONObject

/**
 * Real "recently opened" tracking for Start's Recommended section.
 *
 * Previously Recommended was seeded from the alphabetical installed-apps list when nothing had
 * been launched, which looked populated but was fake. This store records every genuine launch
 * (installed Android apps, packaged web apps and built-in Bluebird apps) with a timestamp, and
 * Start shows exactly that, newest first, like Windows 11.
 *
 * Keys: "app:<package>", "bpk:<id>", "win:<LauncherScreen.name>".
 */
object RecentAppsStore {
    private const val PREFS = "start_recent_apps_v1"
    private const val KEY = "entries"
    private const val MAX = 20

    data class Entry(val key: String, val label: String, val timestamp: Long)

    // Screens that represent a real, user-launchable app. Utility/infra windows are excluded.
    private val trackedScreens = setOf(
        LauncherScreen.SETTINGS, LauncherScreen.FILE_EXPLORER, LauncherScreen.BROWSER,
        LauncherScreen.CALCULATOR, LauncherScreen.CALENDAR, LauncherScreen.MEDIA_PLAYER,
        LauncherScreen.IMAGE_VIEWER, LauncherScreen.WORD_IMPRESS, LauncherScreen.BLUEBIRD_STORE,
        LauncherScreen.RECYCLE_BIN, LauncherScreen.PremiumTextEditorScreen, LauncherScreen.TERMINAL
    )

    fun screenKey(screen: LauncherScreen) = "win:${screen.name}"

    fun load(context: Context): List<Entry> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
            ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map {
                val o = arr.getJSONObject(it)
                Entry(o.getString("k"), o.optString("l"), o.optLong("t"))
            }
        }.getOrDefault(emptyList())
    }

    fun record(context: Context, key: String, label: String) {
        val updated = (listOf(Entry(key, label, System.currentTimeMillis())) +
            load(context).filter { it.key != key }).take(MAX)
        val arr = JSONArray()
        updated.forEach { arr.put(JSONObject().put("k", it.key).put("l", it.label).put("t", it.timestamp)) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, arr.toString()).apply()
    }

    fun recordScreen(context: Context, screen: LauncherScreen) {
        if (screen !in trackedScreens) return
        val label = builtInApps.firstOrNull { it.third == screen }?.first
            ?: when (screen) {
                LauncherScreen.BLUEBIRD_STORE -> "Bluebird Store"
                LauncherScreen.TERMINAL -> "Terminal"
                else -> screen.name
            }
        record(context, screenKey(screen), label)
    }

    fun remove(context: Context, key: String) {
        val arr = JSONArray()
        load(context).filter { it.key != key }.forEach {
            arr.put(JSONObject().put("k", it.key).put("l", it.label).put("t", it.timestamp))
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, arr.toString()).apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY).apply()
    }
}
