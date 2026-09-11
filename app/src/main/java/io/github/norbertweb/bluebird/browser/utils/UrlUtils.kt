package io.github.norbertweb.bluebird.browser.utils

import android.net.Uri
import io.github.norbertweb.bluebird.browser.model.NEWTAB_URL
import io.github.norbertweb.bluebird.browser.model.SearchEngine
import java.util.Locale

object UrlUtils {
    fun resolveUrl(raw: String, searchEngine: SearchEngine): String {
        val value = raw.trim()
        if (value.isBlank()) return NEWTAB_URL
        if (value == NEWTAB_URL) return NEWTAB_URL
        if (value.equals("about:blank", true)) return "about:blank"
        if (value.startsWith("http://", true) || value.startsWith("https://", true)) return value
        if (looksLikeUrl(value)) return "https://$value"
        return searchEngine.queryUrl + Uri.encode(value)
    }

    fun looksLikeUrl(raw: String): Boolean {
        val value = raw.trim()
        if (value.isBlank() || value.any(Char::isWhitespace)) return false
        if (value.equals("localhost", true)) return true
        if (value.matches(Regex("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}(?::[0-9]{1,5})?$"))) return true
        return value.contains('.') && !value.endsWith('.') && !value.contains(":")
    }

    fun displayUrl(url: String): String = if (url == NEWTAB_URL || url.isBlank()) "" else url
        .removePrefix("https://").removePrefix("http://").trimEnd('/')

    fun host(url: String): String? = runCatching { Uri.parse(url).host }.getOrNull()
    fun isWebUrl(url: String) = url.startsWith("http://", true) || url.startsWith("https://", true)
    fun isSecure(url: String) = url.startsWith("https://", true)
    fun faviconUrl(pageUrl: String): String? = runCatching {
        val uri = Uri.parse(pageUrl)
        if (uri.scheme != "http" && uri.scheme != "https" || uri.host.isNullOrBlank()) null
        else "${uri.scheme}://${uri.host}/favicon.ico"
    }.getOrNull()
    fun colorForUrl(url: String): Long {
        val colors = longArrayOf(0xFF1A73E8, 0xFFE53935, 0xFF43A047, 0xFFFB8C00, 0xFF8E24AA, 0xFF00ACC1)
        return colors[Math.floorMod((host(url) ?: url).hashCode(), colors.size)]
    }
}
