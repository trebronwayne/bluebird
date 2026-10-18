package io.github.norbertweb.bluebird.ui.components

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Small bounded per-app log files, kept inside Bluebird's private sandbox. */
class BpkAppLogger(context: Context) {
    private val appContext = context.applicationContext
    fun log(appId: String, level: String, message: String) {
        val safeLevel = level.uppercase(Locale.ROOT).take(8).ifBlank { "INFO" }
        val safeMessage = message.replace(Regex("[\\r\\n\\u0000]"), " ").take(1500)
        val target = BluebirdStorage.appLog(appContext, appId)
        synchronized(target.absolutePath.intern()) {
            target.parentFile?.mkdirs()
            target.appendText("${System.currentTimeMillis()} [$safeLevel] $safeMessage\n", Charsets.UTF_8)
            if (target.length() > MAX_BYTES) {
                val tail = target.readLines(Charsets.UTF_8).takeLast(MAX_LINES)
                target.writeText(tail.joinToString("\n", postfix = "\n"), Charsets.UTF_8)
            }
        }
    }
    fun read(appId: String): String = runCatching { BluebirdStorage.appLog(appContext, appId).takeIf { it.isFile }?.readText()?.takeLast(MAX_READ) ?: "No logs yet." }.getOrDefault("Unable to read app log.")
    fun clear(appId: String) { runCatching { BluebirdStorage.appLog(appContext, appId).delete() } }
    companion object { private const val MAX_BYTES = 256 * 1024L; private const val MAX_LINES = 2000; private const val MAX_READ = 64 * 1024 }
}
