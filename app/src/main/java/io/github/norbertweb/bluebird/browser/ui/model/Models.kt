package io.github.norbertweb.bluebird.browser.model

import java.util.UUID

const val NEWTAB_URL = "io.github.norbertweb.bluebird://newtab"
const val BLANK_URL = "about:blank"
const val MAX_HISTORY_ENTRIES = 500
const val MAX_TABS = 12
const val MAX_DOWNLOAD_ENTRIES = 50

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "New Tab",
    var url: String = NEWTAB_URL,
    var displayUrl: String = "",
    var faviconUrl: String? = null,
    var faviconColor: Long = 0xFF1A73E8,
    var isLoading: Boolean = false,
    var loadProgress: Float = 0f,
    var lastVisited: Long = System.currentTimeMillis(),
    var scrollY: Int = 0
)

data class Bookmark(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val url: String,
    val faviconUrl: String? = null,
    val faviconColor: Long = 0xFF1A73E8,
    val addedAt: Long = System.currentTimeMillis()
)

data class HistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val url: String,
    val faviconUrl: String? = null,
    val faviconColor: Long = 0xFF1A73E8,
    val visitedAt: Long = System.currentTimeMillis()
)

enum class DownloadStatus { DOWNLOADING, COMPLETED, FAILED, PAUSED, CANCELLED }

data class DownloadItem(
    val id: String = UUID.randomUUID().toString(),
    val downloadManagerId: Long = -1L,
    val fileName: String,
    val url: String,
    val mimeType: String,
    val fileSize: Long = 0L,
    var status: DownloadStatus = DownloadStatus.DOWNLOADING,
    val startedAt: Long = System.currentTimeMillis(),
    var progress: Float = 0f,
    var bytesDownloaded: Long = 0L
)

enum class SearchEngine(val label: String, val queryUrl: String, val homeUrl: String) {
    GOOGLE("Google", "https://www.google.com/search?q=", "https://www.google.com"),
    BING("Bing", "https://www.bing.com/search?q=", "https://www.bing.com"),
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q=", "https://duckduckgo.com"),
    ECOSIA("Ecosia", "https://www.ecosia.org/search?q=", "https://www.ecosia.org"),
    BRAVE("Brave", "https://search.brave.com/search?q=", "https://search.brave.com")
}

enum class BrowserPanel { NONE, BOOKMARKS, HISTORY, DOWNLOADS, SETTINGS }

data class BrowserSettings(
    val searchEngine: SearchEngine = SearchEngine.GOOGLE,
    val showBookmarksBar: Boolean = true
)
