package io.github.norbertweb.bluebird.browser.data

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebStorage
import io.github.norbertweb.bluebird.browser.model.*
import org.json.JSONArray
import org.json.JSONObject

/** Small persistent store for the launcher browser: tabs, bookmarks, history, and two UI settings. */
class BrowserRepository(context: Context) {
    private val prefs = context.getSharedPreferences("bluebird_browser", Context.MODE_PRIVATE)

    fun saveSettings(settings: BrowserSettings) {
        prefs.edit()
            .putString("search_engine", settings.searchEngine.name)
            .putBoolean("bookmarks_bar", settings.showBookmarksBar)
            .apply()
    }

    fun loadSettings() = BrowserSettings(
        searchEngine = runCatching { SearchEngine.valueOf(prefs.getString("search_engine", SearchEngine.GOOGLE.name)!!) }
            .getOrDefault(SearchEngine.GOOGLE),
        showBookmarksBar = prefs.getBoolean("bookmarks_bar", true)
    )

    fun saveTabs(tabs: List<BrowserTab>, activeTabId: String) {
        val arr = JSONArray()
        tabs.take(MAX_TABS).forEach { tab ->
            arr.put(JSONObject().apply {
                put("id", tab.id); put("title", tab.title); put("url", tab.url)
                put("displayUrl", tab.displayUrl); put("faviconUrl", tab.faviconUrl)
                put("faviconColor", tab.faviconColor); put("lastVisited", tab.lastVisited)
            })
        }
        prefs.edit().putString("tabs", arr.toString()).putString("active", activeTabId).apply()
    }

    fun loadTabs(): Pair<List<BrowserTab>, String> {
        val raw = prefs.getString("tabs", null) ?: return emptyList<BrowserTab>() to ""
        return runCatching {
            val arr = JSONArray(raw)
            val tabs = (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                BrowserTab(
                    id = o.optString("id"), title = o.optString("title", "New Tab"),
                    url = o.optString("url", NEWTAB_URL), displayUrl = o.optString("displayUrl", ""),
                    faviconUrl = o.optString("faviconUrl", "").takeIf { it.isNotBlank() },
                    faviconColor = o.optLong("faviconColor", 0xFF1A73E8),
                    lastVisited = o.optLong("lastVisited", System.currentTimeMillis())
                )
            }.ifEmpty { listOf(BrowserTab()) }
            tabs to prefs.getString("active", tabs.first().id).orEmpty()
        }.getOrElse { listOf(BrowserTab()) to "" }
    }

    fun saveBookmarks(items: List<Bookmark>) {
        val arr = JSONArray()
        items.take(500).forEach { b -> arr.put(JSONObject().apply {
            put("id", b.id); put("title", b.title); put("url", b.url)
            put("faviconUrl", b.faviconUrl); put("faviconColor", b.faviconColor); put("addedAt", b.addedAt)
        }) }
        prefs.edit().putString("bookmarks", arr.toString()).apply()
    }

    fun loadBookmarks(): List<Bookmark> = runCatching {
        val arr = JSONArray(prefs.getString("bookmarks", "[]"))
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Bookmark(o.optString("id"), o.optString("title", "Bookmark"), o.optString("url"),
                o.optString("faviconUrl", "").takeIf { it.isNotBlank() }, o.optLong("faviconColor", 0xFF1A73E8), o.optLong("addedAt"))
        }
    }.getOrDefault(emptyList())

    fun saveHistory(items: List<HistoryEntry>) {
        val arr = JSONArray()
        items.takeLast(MAX_HISTORY_ENTRIES).forEach { h -> arr.put(JSONObject().apply {
            put("id", h.id); put("title", h.title); put("url", h.url)
            put("faviconUrl", h.faviconUrl); put("faviconColor", h.faviconColor); put("visitedAt", h.visitedAt)
        }) }
        prefs.edit().putString("history", arr.toString()).apply()
    }

    fun loadHistory(): List<HistoryEntry> = runCatching {
        val arr = JSONArray(prefs.getString("history", "[]"))
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            HistoryEntry(o.optString("id"), o.optString("title", ""), o.optString("url"),
                o.optString("faviconUrl", "").takeIf { it.isNotBlank() }, o.optLong("faviconColor", 0xFF1A73E8), o.optLong("visitedAt"))
        }
    }.getOrDefault(emptyList())

    fun clearHistory() { prefs.edit().remove("history").apply() }
    fun clearBookmarks() { prefs.edit().remove("bookmarks").apply() }


}
