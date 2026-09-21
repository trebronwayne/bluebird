package io.github.norbertweb.bluebird.ui.components

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.norbertweb.bluebird.LauncherScreen
import io.github.norbertweb.bluebird.LauncherUiState
import io.github.norbertweb.bluebird.LauncherViewModel
import io.github.norbertweb.bluebird.ui.theme.LocalIsDarkTheme
import io.github.norbertweb.bluebird.ui.theme.LocalTextScale
import io.github.norbertweb.bluebird.ui.theme.bluebirdColors

// NOTE: Action Center no longer owns any notification state (remote or
// system). It is now purely Quick Settings: tiles + volume/brightness.
// The calendar moved out to CalendarFlyout.kt, triggered separately from
// the taskbar clock. See TipToast.kt for the new "tips" toast, which is
// the only remaining toast-style surface and is unrelated to notifications.

@Composable
fun ActionCenter(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel,
    hazeState: dev.chrisbanes.haze.HazeState,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val textScale = LocalTextScale.current
    val isDark = LocalIsDarkTheme.current
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    val divColor = if (isDark) DS.borderDark else DS.borderLight

    // One compact Windows-style flyout. It shares Start's exact neutral
    // surface/border language through AcrylicSurface + DS/bluebirdColors.
    AcrylicSurface(
        modifier = modifier
            .widthIn(min = 248.dp, max = 288.dp)
            .wrapContentHeight(),
        isDark = isDark,
        accent = accent,
        cornerRadius = 12.dp,
        hazeState = hazeState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(if (isDark) Color(0xE61F1F1F) else Color(0xE6F8F8F8), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 9.dp)
        ) {
            // ── Compact header ───────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Quick Settings",
                    color = textColor,
                    fontSize = (13f * textScale).sp,
                    fontWeight = FontWeight.Bold
                )
                SmallActionIcon(
                    icon = FluentIcon.Settings,
                    contentDescription = "Settings",
                    isDark = isDark,
                    onClick = { viewModel.openWindow(LauncherScreen.SETTINGS) }
                )
            }

            Spacer(Modifier.height(6.dp))

            // Static reference tiles: no fake On/Off/Connected labels. Taps
            // only navigate to a real Android/Bluebird destination where one
            // exists; the tile itself never pretends to own system state.
            // Wi-Fi and Data open Android's real quick-toggle panels (no
            // navigating away); Airplane and Focus have no panel API, so
            // they open their full Android settings screens instead.
            val quickRefs = listOf(
                "Wi-Fi" to { openWifiQuickPanel(context) },
                "Bluetooth" to { viewModel.openBluetoothSettings(context) },
                "Airplane" to { openAndroidSettings(context, Settings.ACTION_AIRPLANE_MODE_SETTINGS) },
                "Focus" to { openAndroidSettings(context, Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS) },
                "Data" to { openDataQuickPanel(context) },
                "Settings" to { viewModel.openWindow(LauncherScreen.SETTINGS) }
            )
            val quickIcons = listOf(
                FluentIcon.Wifi,
                FluentIcon.Bluetooth,
                FluentIcon.Airplane,
                FluentIcon.Prohibited,
                FluentIcon.DataUsage,
                FluentIcon.Settings
            )

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                quickRefs.chunked(3).forEachIndexed { rowIndex, row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        row.forEachIndexed { colIndex, (label, action) ->
                            ReferenceQuickTile(
                                label = label,
                                icon = quickIcons[rowIndex * 3 + colIndex],
                                isDark = isDark,
                                textScale = textScale,
                                modifier = Modifier.weight(1f),
                                onClick = action
                            )
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }

            Spacer(Modifier.height(7.dp))
            HorizontalDivider(color = divColor, thickness = 0.5.dp)
            Spacer(Modifier.height(7.dp))

            // ── Real controls, compactly represented ─────────────────────────
            CompactSliderSection(
                icon = when {
                    uiState.volume < 0.01f -> FluentIcon.SpeakerMute
                    uiState.volume < 0.5f -> FluentIcon.Speaker1
                    else -> FluentIcon.Speaker2
                },
                label = "Volume",
                value = uiState.volume,
                isDark = isDark,
                textColor = textColor,
                textScale = textScale,
                onValueChange = { viewModel.setVolume(it, context) }
            )
            Spacer(Modifier.height(4.dp))
            CompactSliderSection(
                icon = FluentIcon.BrightnessHigh,
                label = "Brightness",
                value = uiState.brightness,
                isDark = isDark,
                textColor = textColor,
                textScale = textScale,
                onValueChange = { viewModel.setBrightness(it, context) }
            )
        }
    }
}

/**
 * Shows Android's Internet Connectivity quick panel - there is no
 * standalone "mobile data" panel in the public API, but this one covers
 * Wi-Fi and mobile data together in the same floating dialog, which is the
 * closest real equivalent. Falls back to the full Data usage settings
 * screen on older devices or if nothing can resolve the panel intent.
 */
private fun openDataQuickPanel(context: Context) {
    val panelIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    } else {
        null
    }

    val opened = panelIntent != null && runCatching { context.startActivity(panelIntent) }.isSuccess
    if (!opened) {
        openAndroidSettings(context, Settings.ACTION_DATA_USAGE_SETTINGS)
    }
}

/**
 * Shows Android's real Wi-Fi quick-toggle panel (a floating bottom sheet the
 * OS draws over Bluebird, letting the user flip Wi-Fi on/off without ever
 * leaving the launcher) instead of jumping to the full Settings app. This is
 * the public android.provider.Settings.Panel API, added in Android 10 (API
 * 29) - there is no equivalent public panel for Bluetooth or airplane mode,
 * so those still go to their full Settings screens.
 */
private fun openWifiQuickPanel(context: Context) {
    val panelIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Intent(Settings.Panel.ACTION_WIFI).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    } else {
        null
    }

    val opened = panelIntent != null && runCatching { context.startActivity(panelIntent) }.isSuccess
    if (!opened) {
        // Older devices, or no app on the device can resolve the panel:
        // fall back to the real Wi-Fi settings screen rather than doing
        // nothing.
        openAndroidSettings(context, Settings.ACTION_WIFI_SETTINGS)
    }
}

/** Opens a real Android system Settings screen (Airplane mode, Zen mode, Data usage, ...). */
private fun openAndroidSettings(context: Context, action: String) {
    runCatching {
        context.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

@Composable
private fun SmallActionIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isDark) Color.White.copy(alpha = 0.045f) else Color.Black.copy(alpha = 0.035f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription, tint = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight, modifier = Modifier.size(15.dp))
    }
}

@Composable
private fun ReferenceQuickTile(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDark: Boolean,
    textScale: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    Column(
        modifier = modifier
            .heightIn(min = 36.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(if (isDark) Color.White.copy(alpha = 0.045f) else Color.Black.copy(alpha = 0.032f))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(icon, contentDescription = label, tint = textColor.copy(alpha = 0.85f), modifier = Modifier.size(13.dp))
        Text(
            label,
            color = textColor.copy(alpha = 0.9f),
            fontSize = (9.5f * textScale).sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CompactSliderSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: Float,
    isDark: Boolean,
    textColor: Color,
    textScale: Float,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = label, tint = textColor.copy(alpha = 0.84f), modifier = Modifier.size(17.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(label, color = textColor, fontSize = (10.5f * textScale).sp, fontWeight = FontWeight.Bold)
                Text("${(value * 100).toInt()}%", color = textColor.copy(alpha = 0.55f), fontSize = (9.5f * textScale).sp, fontWeight = FontWeight.SemiBold)
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth().height(21.dp),
                colors = SliderDefaults.colors(
                    thumbColor = bluebirdColors.AccentBlue,
                    activeTrackColor = bluebirdColors.AccentBlue,
                    inactiveTrackColor = if (isDark) DS.borderDark else DS.borderLight
                )
            )
        }
    }
}

@Composable
private fun SliderSection(
    label: String,
    value: Float,
    percent: String,
    isDark: Boolean,
    textColor: Color,
    subColor: Color,
    textScale: Float,
    leadIcon: @Composable () -> Unit,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        leadIcon()
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, color = textColor, fontSize = (12 * textScale).sp, fontWeight = FontWeight.Medium)
                Text(percent, color = subColor, fontSize = (11 * textScale).sp)
            }
            Slider(
                value         = value,
                onValueChange = onValueChange,
                modifier      = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
                colors        = SliderDefaults.colors(
                    thumbColor            = bluebirdColors.AccentBlue,
                    activeTrackColor      = bluebirdColors.AccentBlue,
                    inactiveTrackColor    = if (isDark) DS.borderDark else DS.borderLight
                )
            )
        }
    }
}

// ─── Text Scale Section ───────────────────────────────────────────────────────


// ─── Battery Section ──────────────────────────────────────────────────────────

@Composable
private fun BatterySection(
    level: Int,
    charging: Boolean,
    isDark: Boolean,
    textColor: Color,
    subColor: Color,
    textScale: Float
) {
    val batteryColor = when {
        charging  -> bluebirdColors.AccentBlue
        level > 20 -> bluebirdColors.Success
        else       -> bluebirdColors.Error
    }
    val levelFraction = level / 100f

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = if (charging) FluentIcon.BatteryCharge else
                    when {
                        level > 80 -> FluentIcon.Battery9
                        level > 50 -> FluentIcon.Battery6
                        level > 20 -> FluentIcon.Battery3
                        else       -> FluentIcon.Battery0
                    },
            contentDescription = "Battery",
            tint     = batteryColor,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$level% — ${if (charging) "Charging" else "On Battery"}",
                    color = textColor,
                    fontSize = (12 * textScale).sp,
                    fontWeight = FontWeight.Medium
                )
                if (charging) {
                    Icon(
                        imageVector = FluentIcon.Flash,
                        contentDescription = null,
                        tint     = bluebirdColors.AccentBlue,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            LinearProgressIndicator(
                progress = { levelFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color      = batteryColor,
                trackColor = if (isDark) DS.borderDark else DS.borderLight
            )
        }
    }
}
