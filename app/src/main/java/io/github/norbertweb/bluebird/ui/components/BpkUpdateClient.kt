package io.github.norbertweb.bluebird.ui.components

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/** Minimal HTTPS update feed client. Feed fields: id, version, packageUrl, sha256. */
class BpkUpdateClient(context: Context, private val packageManager: BpkPackageManager = BpkPackageManager(context)) {
    private val context = context.applicationContext

    data class UpdateInfo(val id: String, val version: String, val packageUrl: String, val sha256: String)

    fun check(feedUrl: String, expectedAppId: String): UpdateInfo? {
        require(feedUrl.startsWith("https://", true)) { "Update feeds must use HTTPS" }
        val json = JSONObject(readUrl(feedUrl, MAX_FEED_BYTES))
        val info = UpdateInfo(json.getString("id"), json.getString("version"), json.getString("packageUrl"), json.getString("sha256").lowercase())
        require(info.id == expectedAppId) { "Update feed app ID mismatch" }
        require(info.packageUrl.startsWith("https://", true)) { "Package URL must use HTTPS" }
        require(info.sha256.matches(Regex("[a-f0-9]{64}"))) { "Invalid SHA-256 in update feed" }
        val installed = packageManager.apps().firstOrNull { it.id == expectedAppId } ?: return info
        return info.takeIf { compareVersions(it.version, installed.version) > 0 }
    }

    /** Downloads, verifies and installs atomically through the existing BPK v1 installer. */
    fun downloadAndInstall(update: UpdateInfo): InstalledBpkApp {
        require(update.packageUrl.startsWith("https://", true))
        val dir = File(context.cacheDir, "bpk-updates").apply { mkdirs() }
        val temp = File(dir, "${update.id}.download.part")
        try {
            download(update.packageUrl, temp, MAX_PACKAGE_BYTES)
            require(sha256(temp).equals(update.sha256, true)) { "Downloaded package SHA-256 mismatch" }
            val staged = packageManager.stage(temp)
            require(staged.manifest.id == update.id) { "Downloaded package app ID mismatch" }
            require(staged.manifest.version == update.version) { "Downloaded package version mismatch" }
            val current = packageManager.apps().firstOrNull { it.id == update.id }
            return packageManager.install(staged, current?.let { File(it.installDir) })
        } finally { temp.delete() }
    }

    private fun readUrl(url: String, max: Int): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply { connectTimeout = 10000; readTimeout = 15000; instanceFollowRedirects = false }
        try {
            require(conn.responseCode in 200..299) { "Update feed HTTP ${conn.responseCode}" }
            val bytes = conn.inputStream.use { it.readBytesBounded(max) }
            return bytes.toString(Charsets.UTF_8)
        } finally { conn.disconnect() }
    }
    private fun download(url: String, target: File, max: Long) {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply { connectTimeout = 15000; readTimeout = 30000; instanceFollowRedirects = false }
        try {
            require(conn.responseCode in 200..299) { "Package download HTTP ${conn.responseCode}" }
            require(conn.contentLengthLong <= max || conn.contentLengthLong < 0) { "Package is too large" }
            conn.inputStream.use { input -> target.outputStream().buffered().use { out ->
                val buffer = ByteArray(8192); var total = 0L
                while (true) { val n = input.read(buffer); if (n < 0) break; total += n; require(total <= max) { "Package is too large" }; out.write(buffer, 0, n) }
            } }
        } finally { conn.disconnect() }
    }
    private fun sha256(file: File): String = MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
    private fun compareVersions(a: String, b: String): Int {
        val left = a.split(Regex("[.+_-]")); val right = b.split(Regex("[.+_-]"))
        for (i in 0 until maxOf(left.size, right.size)) {
            val x = left.getOrNull(i).orEmpty(); val y = right.getOrNull(i).orEmpty()
            val cmp = if (x.toLongOrNull() != null && y.toLongOrNull() != null) x.toLong().compareTo(y.toLong()) else x.compareTo(y, true)
            if (cmp != 0) return cmp
        }
        return 0
    }
    private fun java.io.InputStream.readBytesBounded(max: Int): ByteArray {
        val out = java.io.ByteArrayOutputStream(); val buffer = ByteArray(4096); var total = 0
        while (true) { val n = read(buffer); if (n < 0) break; total += n; require(total <= max) { "Response is too large" }; out.write(buffer, 0, n) }
        return out.toByteArray()
    }
    companion object { private const val MAX_FEED_BYTES = 128 * 1024; private const val MAX_PACKAGE_BYTES = 100L * 1024L * 1024L }
}
