package io.github.norbertweb.bluebird.ui.components

import android.content.Context
import android.webkit.JavascriptInterface
import org.json.JSONObject
import java.io.File

/**
 * Capability-gated bridge for a BPK WebView. Attach only to trusted local app
 * content, never to arbitrary remote pages. Native UI actions are delegated to
 * the host through callbacks so callers can launch Android pickers/settings.
 */
class BpkRuntimeApi(
    context: Context,
    private val appId: String,
    private val onPickFile: (() -> Unit)? = null,
    private val onPickMedia: (() -> Unit)? = null,
    private val onBrightnessRequest: ((Int) -> Boolean)? = null
) {
    private val appContext = context.applicationContext
    private val permissions = BpkPermissionManager(appContext)
    private val logger = BpkAppLogger(appContext)
    private fun allowed(capability: String) = permissions.isGranted(appId, capability)
    private fun result(ok: Boolean, value: String = "", error: String = "") = JSONObject().put("ok", ok).put("value", value).put("error", error).toString()

    @JavascriptInterface fun getAppInfo(): String = JSONObject().put("id", appId).put("apiVersion", 1).toString()

    @JavascriptInterface fun log(level: String, message: String) {
        if (allowed(BpkCapability.LOG)) logger.log(appId, level, message)
    }

    @JavascriptInterface fun storageGet(key: String): String {
        if (!allowed(BpkCapability.APP_STORAGE)) return result(false, error = "permission_denied")
        val f = storageFile(key) ?: return result(false, error = "invalid_key")
        return runCatching { if (f.isFile) result(true, f.readText(Charsets.UTF_8).take(MAX_STORAGE_VALUE)) else result(true, "") }.getOrElse { result(false, error = "read_failed") }
    }

    @JavascriptInterface fun storageSet(key: String, value: String): String {
        if (!allowed(BpkCapability.APP_STORAGE)) return result(false, error = "permission_denied")
        if (value.length > MAX_STORAGE_VALUE) return result(false, error = "value_too_large")
        val f = storageFile(key) ?: return result(false, error = "invalid_key")
        return runCatching { f.parentFile?.mkdirs(); f.writeText(value, Charsets.UTF_8); result(true) }.getOrElse { result(false, error = "write_failed") }
    }

    @JavascriptInterface fun requestFilePicker(): String {
        if (!allowed(BpkCapability.FILE_PICKER)) return result(false, error = "permission_denied")
        return if (onPickFile != null) { onPickFile.invoke(); result(true, "picker_requested") } else result(false, error = "picker_unavailable")
    }

    @JavascriptInterface fun requestMediaPicker(): String {
        if (!allowed(BpkCapability.MEDIA_PICKER)) return result(false, error = "permission_denied")
        return if (onPickMedia != null) { onPickMedia.invoke(); result(true, "picker_requested") } else result(false, error = "picker_unavailable")
    }

    @JavascriptInterface fun setBrightness(value: Int): String {
        if (!allowed(BpkCapability.BRIGHTNESS)) return result(false, error = "permission_denied")
        if (value !in 1..255) return result(false, error = "value_out_of_range")
        val ok = runCatching { onBrightnessRequest?.invoke(value) == true }.getOrDefault(false)
        return if (ok) result(true, value.toString()) else result(false, error = "system_permission_or_host_unavailable")
    }

    @JavascriptInterface fun capabilityGranted(name: String): Boolean = name in BpkCapability.all && allowed(name)

    private fun storageFile(key: String): File? {
        if (key.isBlank() || key.length > 160 || key.contains('/') || key.contains('\\') || key == "." || key == "..") return null
        if (!key.matches(Regex("[A-Za-z0-9._ -]+"))) return null
        return File(BluebirdStorage.appData(appContext, appId), key)
    }

    companion object { private const val MAX_STORAGE_VALUE = 256 * 1024 }
}
