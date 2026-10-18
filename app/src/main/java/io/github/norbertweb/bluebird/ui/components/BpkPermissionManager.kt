package io.github.norbertweb.bluebird.ui.components

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** User-controlled capabilities for a BPK web app. Unknown capabilities are denied. */
object BpkCapability {
    const val LOG = "logs"
    const val APP_STORAGE = "app_storage"
    const val INTERNET = "internet"
    const val MEDIA_PICKER = "media_picker"
    const val FILE_PICKER = "file_picker"
    const val VIDEO = "video_playback"
    const val AUDIO = "audio_playback"
    const val BRIGHTNESS = "brightness_control"
    val all = linkedSetOf(LOG, APP_STORAGE, INTERNET, MEDIA_PICKER, FILE_PICKER, VIDEO, AUDIO, BRIGHTNESS)
}

class BpkPermissionManager(context: Context) {
    private val file = File(BluebirdStorage.root(context), "ProgramData/app-permissions.json")
    init { file.parentFile?.mkdirs() }

    @Synchronized fun granted(appId: String): Set<String> = runCatching {
        val obj = if (file.isFile) JSONObject(file.readText()) else JSONObject()
        val arr = obj.optJSONArray(appId) ?: JSONArray()
        buildSet { for (i in 0 until arr.length()) arr.optString(i).takeIf { it in BpkCapability.all }?.let(::add) }
    }.getOrDefault(emptySet())

    @Synchronized fun isGranted(appId: String, capability: String) = capability in granted(appId)

    @Synchronized fun setGranted(appId: String, capability: String, enabled: Boolean) {
        require(appId.matches(Regex("[A-Za-z0-9._-]{2,128}")))
        require(capability in BpkCapability.all) { "Unknown capability" }
        val root = if (file.isFile) runCatching { JSONObject(file.readText()) }.getOrDefault(JSONObject()) else JSONObject()
        val current = granted(appId).toMutableSet()
        if (enabled) current.add(capability) else current.remove(capability)
        root.put(appId, JSONArray(current.toList()))
        val temp = File(file.parentFile, "${file.name}.tmp")
        temp.writeText(root.toString(2))
        if (file.exists()) file.delete()
        check(temp.renameTo(file)) { "Could not save app permissions" }
    }

    @Synchronized fun reset(appId: String) {
        val root = if (file.isFile) runCatching { JSONObject(file.readText()) }.getOrDefault(JSONObject()) else JSONObject()
        root.remove(appId); file.writeText(root.toString(2))
    }
}
