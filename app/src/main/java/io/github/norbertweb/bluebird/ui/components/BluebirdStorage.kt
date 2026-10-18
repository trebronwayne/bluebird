package io.github.norbertweb.bluebird.ui.components

import android.content.Context
import android.os.Environment
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * All BPK system files live in Bluebird's private app sandbox. BPK v1 archive
 * layout is unchanged. Bluebird File Explorer can expose these folders through
 * an in-app virtual location; other apps cannot browse internal storage directly.
 */
object BluebirdStorage {
    private const val ROOT_NAME = "bluebird-system"
    fun root(context: Context): File {
        val root = File(context.filesDir, ROOT_NAME)
        if (!root.exists()) root.mkdirs()
        migrateLegacyOnce(context, root)
        return root
    }

    fun appDataRoot(context: Context): File = File(root(context), "AppData").apply { mkdirs() }
    fun appData(context: Context, appId: String): File {
        require(appId.matches(Regex("[A-Za-z0-9._-]{2,128}"))) { "Invalid app id" }
        return File(appDataRoot(context), appId).apply { mkdirs() }
    }
    fun appLogsRoot(context: Context): File = File(root(context), "ProgramData/AppLogs").apply { mkdirs() }
    fun appLog(context: Context, appId: String): File {
        require(appId.matches(Regex("[A-Za-z0-9._-]{2,128}"))) { "Invalid app id" }
        return File(appLogsRoot(context), "$appId.log")
    }

    private fun migrateLegacyOnce(context: Context, destination: File) {
        val marker = File(context.filesDir, "$ROOT_NAME-migration-v1.done")
        if (marker.exists()) return
        // Earlier builds used either public "Bluebird Storage" or filesDir/Bluebird Storage.
        val candidates = listOf(
            File(Environment.getExternalStorageDirectory(), "Bluebird Storage"),
            File(context.filesDir, "Bluebird Storage")
        )
        candidates.firstOrNull { it.isDirectory && it.canonicalFile != destination.canonicalFile }?.let { legacy ->
            runCatching {
                legacy.walkTopDown().forEach { source ->
                    val relative = source.relativeTo(legacy)
                    val target = File(destination, relative.path)
                    if (source.isDirectory) target.mkdirs()
                    else if (!target.exists()) {
                        target.parentFile?.mkdirs()
                        source.copyTo(target, overwrite = false)
                    }
                }
                rewriteMigratedRegistryPaths(destination, legacy)
                marker.writeText("done")
            }
        } ?: runCatching { marker.writeText("done") }
    }

    /** Registry entries stored absolute paths in older releases; rewrite those to the migrated tree. */
    private fun rewriteMigratedRegistryPaths(destination: File, legacy: File) {
        val registry = File(destination, "ProgramData/installed-apps.json")
        if (!registry.isFile) return
        val oldRoot = legacy.canonicalFile.path
        val array = JSONArray(registry.readText(Charsets.UTF_8))
        for (index in 0 until array.length()) {
            val app = array.optJSONObject(index) ?: continue
            listOf("installDir", "executablePath", "iconPath", "packagePath").forEach { key ->
                val oldPath = app.optString(key).takeIf { it.isNotBlank() } ?: return@forEach
                val canonical = runCatching { File(oldPath).canonicalFile.path }.getOrNull() ?: return@forEach
                if (canonical == oldRoot || canonical.startsWith(oldRoot + File.separator)) {
                    val relative = canonical.removePrefix(oldRoot).trimStart(File.separatorChar)
                    app.put(key, File(destination, relative).absolutePath)
                }
            }
        }
        val temp = File(registry.parentFile, "installed-apps.json.tmp")
        temp.writeText(array.toString(), Charsets.UTF_8)
        check(temp.renameTo(registry) || run { registry.writeText(array.toString(), Charsets.UTF_8); temp.delete(); true })
    }
}
