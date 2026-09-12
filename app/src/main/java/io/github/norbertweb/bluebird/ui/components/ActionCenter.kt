package io.github.norbertweb.bluebird.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
            .fillMaxWidth(0.76f)
            .widthIn(min = 264.dp, max = 320.dp)
            .heightIn(min = 168.dp, max = 220.dp),
        isDark = isDark,
        accent = accent,
        cornerRadius = 12.dp,
        hazeState = hazeState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color(0xE61F1F1F) else Color(0xE6F8F8F8), RoundedCornerShape(12.dp))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 11.dp, vertical = 10.dp)
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
                    fontSize = (14f * textScale).sp,
                    fontWeight = FontWeight.SemiBold
                )
                SmallActionIcon(
                    icon = FluentIcon.Settings,
                    contentDescription = "Settings",
                    isDark = isDark,
                    onClick = { viewModel.openWindow(LauncherScreen.SETTINGS) }
                )
            }

            Spacer(Modifier.height(7.dp))

            // Static reference tiles: no fake On/Off/Connected labels. Taps
            // only navigate to a real Android/Bluebird destination where one
            // exists; the tile itself never pretends to own system state.
            val quickRefs = listOf(
                "Wi-Fi" to { viewModel.openWifiSettings(context) },
                "Bluetooth" to { viewModel.openBluetoothSettings(context) },
                "Airplane" to { viewModel.openWindow(LauncherScreen.SETTINGS) },
                "Focus" to { viewModel.openWindow(LauncherScreen.SETTINGS) },
                "Data" to { viewModel.openWindow(LauncherScreen.SETTINGS) },
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

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                quickRefs.chunked(3).forEachIndexed { rowIndex, row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = divColor, thickness = 0.5.dp)
            Spacer(Modifier.height(8.dp))

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
            Spacer(Modifier.height(5.dp))
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
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(if (isDark) Color.White.copy(alpha = 0.045f) else Color.Black.copy(alpha = 0.032f))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(icon, contentDescription = label, tint = textColor.copy(alpha = 0.82f), modifier = Modifier.size(14.dp))
        Text(label, color = textColor.copy(alpha = 0.82f), fontSize = (9.5f * textScale).sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                Text(label, color = textColor, fontSize = (10.5f * textScale).sp, fontWeight = FontWeight.Medium)
                Text("${(value * 100).toInt()}%", color = textColor.copy(alpha = 0.44f), fontSize = (9.5f * textScale).sp)
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth().height(23.dp),
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
