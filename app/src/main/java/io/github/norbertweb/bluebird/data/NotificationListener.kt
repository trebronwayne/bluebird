package io.github.norbertweb.bluebird.data

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

// RealNotification and the whole "collect device notifications into Bluebird"
// feature were removed — see ActionCenter.kt/LauncherViewModel.kt. This class
// was never actually wired to anything (no other file registers
// onNotificationPosted/onNotificationRemoved), so it's safe to delete outright
// along with its <service> entry in AndroidManifest.xml.
//
// Left here as an inert stub — rather than deleted outright — only so the
// build keeps compiling if the manifest still declares this service; it does
// nothing now. Recommended next step: delete this file AND the
// BIND_NOTIFICATION_LISTENER_SERVICE <service> entry for it in the manifest,
// then this file can go away too.
class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        // Intentionally does nothing.
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        // Intentionally does nothing.
    }
}
