package io.github.norbertweb.bluebird

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.provider.Settings

/**
 * System-level actions for INSTALLED Android apps: open the Android "App info" page and start
 * an uninstall. Built-in Bluebird apps (Files, Settings…) and BPK web apps ("bpk:" packages)
 * are not Android packages, so none of this applies to them (BPK apps have their own uninstall
 * in Program Manager).
 */
object AppActions {

    /** A real, currently-installed Android package (not a built-in or "bpk:" pseudo-package). */
    fun isInstalledAndroidApp(context: Context, packageName: String): Boolean {
        if (packageName.isBlank() || packageName.startsWith("bpk:")) return false
        return try { context.packageManager.getApplicationInfo(packageName, 0); true }
        catch (e: Exception) { false }
    }

    /**
     * Uninstall is offered only when Android would actually allow it: never for Bluebird itself,
     * and not for system apps (unless the user has installed an update on top of one — that
     * update can be removed).
     */
    fun canUninstall(context: Context, packageName: String): Boolean {
        if (!isInstalledAndroidApp(context, packageName)) return false
        if (packageName == context.packageName) return false
        return try {
            val flags = context.packageManager.getApplicationInfo(packageName, 0).flags
            val isSystem = flags and ApplicationInfo.FLAG_SYSTEM != 0
            val isUpdatedSystem = flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
            !isSystem || isUpdatedSystem
        } catch (e: Exception) { false }
    }

    /** Opens Android Settings → Apps → <this app>. */
    fun openAppInfo(context: Context, packageName: String): Boolean =
        start(context, Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)))

    /** Shows the system uninstall confirmation. Falls back to App info if it cannot be shown. */
    fun requestUninstall(context: Context, packageName: String): Boolean {
        val ok = start(context, Intent(Intent.ACTION_DELETE, Uri.fromParts("package", packageName, null)))
        return ok || openAppInfo(context, packageName)
    }

    private fun start(context: Context, intent: Intent): Boolean = try {
        if (context !is android.app.Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent); true
    } catch (e: Exception) { false }
}
