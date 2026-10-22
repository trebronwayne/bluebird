package io.github.norbertweb.bluebird

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * Every outward link and system hand-off in ONE place so URLs are trivial to change.
 */
object AppLinks {
    /** Public website. This is the URL that "Share Bluebird" shares. */
    const val WEBSITE_URL = "https://trebronwayne.github.io"

    /** Open-source repository. */
    const val REPO_URL = "https://github.com/trebronwayne/bluebird"

    /** Special toast-action URL: the toast host turns it into the Android share sheet. */
    const val ACTION_SHARE_APP = "bluebird://action/share-app"

    /** Opens the system share sheet with the Bluebird website link. */
    fun shareApp(context: Context, chooserTitle: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Bluebird")
            putExtra(Intent.EXTRA_TEXT, WEBSITE_URL)
        }
        startSafely(context, Intent.createChooser(send, chooserTitle))
    }

    fun openRepo(context: Context) = openUrl(context, REPO_URL)

    fun openUrl(context: Context, url: String) =
        startSafely(context, Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    /** True when Bluebird is the launcher the Home button currently opens. */
    fun isDefaultLauncher(context: Context): Boolean = try {
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolved = context.packageManager.resolveActivity(home, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
        resolved?.activityInfo?.packageName == context.packageName
    } catch (e: Exception) { false }

    /**
     * Intent that lets the user pick (or change) the default launcher.
     * Android 10+: the role-request dialog. Otherwise / on failure: the system Home settings.
     */
    fun defaultLauncherIntent(context: Context, forceChooser: Boolean): Intent {
        if (!forceChooser && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = context.getSystemService(RoleManager::class.java)
            if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_HOME) && !rm.isRoleHeld(RoleManager.ROLE_HOME))
                return rm.createRequestRoleIntent(RoleManager.ROLE_HOME)
        }
        return Intent(Settings.ACTION_HOME_SETTINGS)
    }

    /** Opens the system default-launcher UI; falls back to the generic default-apps page. */
    fun openDefaultLauncherSettings(context: Context, forceChooser: Boolean = true) {
        val first = defaultLauncherIntent(context, forceChooser)
        if (!startSafely(context, first)) {
            startSafely(context, Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
        }
    }

    private fun startSafely(context: Context, intent: Intent): Boolean = try {
        if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent); true
    } catch (e: Exception) { false }
}
