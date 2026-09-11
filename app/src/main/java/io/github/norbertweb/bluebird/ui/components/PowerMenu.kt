package io.github.norbertweb.bluebird.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.norbertweb.bluebird.ui.theme.bluebirdColors

// ─────────────────────────────────────────────────────────
// POWER MENU — scoped to Bluebird itself, not the device.
//
// The previous version tried to sleep/reboot/shut down the *phone* via
// hidden system APIs and reflection (DevicePolicyManager.lockNow(),
// PowerManager.reboot(), a reflected PowerManager.shutdown(...)). None of
// that works on a normal, non-system-signed install — it's a silent
// no-op on real devices, which made it a fake feature. Bluebird is a
// launcher app, not the OS, so "power" here now means power over the
// Bluebird app process itself:
//   • Reload — restarts the Bluebird app
//   • Shut Down — closes the Bluebird app
// ─────────────────────────────────────────────────────────
@Composable
fun PowerMenuButton(
    isDark: Boolean,
    textPrimary: androidx.compose.ui.graphics.Color,
    onBluebirdLock: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var showPowerMenu by remember { mutableStateOf(false) }

    Box {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(DS.sectionCorner))
                .background(if (showPowerMenu) DS.accentStart.copy(alpha = 0.15f) else androidx.compose.ui.graphics.Color.Transparent)
                .clickable { showPowerMenu = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = FluentIcon.Power,
                contentDescription = "Power options",
                tint = if (showPowerMenu) DS.accentStart else textPrimary.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
        DropdownMenu(
            expanded = showPowerMenu,
            onDismissRequest = { showPowerMenu = false },
            modifier = Modifier
                .background(DS.controlFill(isDark, strength = 1.15f), RoundedCornerShape(10.dp))
                .border(0.5.dp, if (isDark) DS.borderDark.copy(alpha = 0.72f) else DS.borderLight.copy(alpha = 0.72f), RoundedCornerShape(10.dp))
        ) {
            PowerMenuItem(label = "Lock screen", icon = FluentIcon.Accessibility, isDark = isDark) {
                showPowerMenu = false
                // Lock only the Bluebird desktop overlay; never touch Android system state.
                onBluebirdLock?.invoke()
            }
            PowerMenuItem(label = "Turn off screen", icon = FluentIcon.Power, isDark = isDark) {
                showPowerMenu = false
                // Android exposes device screen locking as the public
                // accessibility global action. Keep this separate in the
                // Bluebird UI so the requested power-menu semantics remain clear.
                // Lock only the Bluebird desktop overlay; never touch Android system state.
                onBluebirdLock?.invoke()
            }
            PowerMenuItem(label = "Restart", icon = FluentIcon.ArrowSync, isDark = isDark) {
                showPowerMenu = false
                restartBluebird(context)
            }
            PowerMenuItem(label = "Shut Down", icon = FluentIcon.Power, isDark = isDark) {
                showPowerMenu = false
                shutDownBluebird(context)
            }
        }
    }
}

/** Restarts the Bluebird app process: relaunches the launch activity, then kills this process. */
private fun restartBluebird(context: Context) {
    runCatching {
        val packageManager = context.packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage(context.packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(launchIntent)
        }
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}

/** Closes the Bluebird app: finishes the current activity (if any) and ends the process. */
private fun shutDownBluebird(context: Context) {
    runCatching {
        (context as? android.app.Activity)?.finishAndRemoveTask()
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}

@Composable
private fun PowerMenuItem(label: String, icon: ImageVector, isDark: Boolean, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    val iconTint = if (label == "Shut Down") DS.badgeRed else DS.accentStart

    Row(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (pressed) (if (isDark) DS.pressedDark else DS.pressedLight) else androidx.compose.ui.graphics.Color.Transparent)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                    onTap = { onClick() }
                )
            }
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(14.dp))
        Text(label, fontSize = 12.sp, color = textColor, fontWeight = FontWeight.Medium)
    }
}
