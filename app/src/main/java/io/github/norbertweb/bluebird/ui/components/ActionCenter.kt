package io.github.norbertweb.bluebird.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.norbertweb.bluebird.LauncherScreen
import io.github.norbertweb.bluebird.LauncherUiState
import io.github.norbertweb.bluebird.LauncherViewModel
import io.github.norbertweb.bluebird.RealNotification
import io.github.norbertweb.bluebird.ui.theme.LocalIsDarkTheme
import io.github.norbertweb.bluebird.ui.theme.LocalTextScale
import io.github.norbertweb.bluebird.ui.theme.bluebirdColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Remote Bluebird notification model. This is intentionally kept as the one
// non-system notification source in Action Center; it is populated by the
// existing LauncherViewModel refresh path.
data class BluebirdRemoteNotification(
    val id: String,
    val type: String,
    val priority: String,
    val title: String,
    val body: String,
    val timestamp: String,
    val expiresAt: String?,
    val actionLabel: String?,
    val actionUrl: String?,
    val badgeColor: String = "#4C63D9"
)

private val notifTimeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

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
    val subColor = textColor.copy(alpha = 0.56f)
    val divColor = if (isDark) DS.borderDark else DS.borderLight

    // Refresh whenever the panel enters composition. The ViewModel owns the
    // network/throttle policy; this UI does not invent its own polling loop.
    LaunchedEffect(Unit) {
        runCatching { viewModel.refreshRemoteNotificationsIfDue() }
    }

    val remoteNotifications = remember(uiState.remoteNotifications, uiState.dismissedRemoteNotificationIds) {
        uiState.remoteNotifications.filter { it.id !in uiState.dismissedRemoteNotificationIds }.take(3)
    }

    // One compact Windows-style flyout. It shares Start's exact neutral
    // surface/border language through AcrylicSurface + DS/bluebirdColors.
    AcrylicSurface(
        modifier = modifier
            .fillMaxWidth(0.86f)
            .widthIn(min = 300.dp, max = 420.dp)
            .heightIn(min = 360.dp, max = 620.dp),
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
                .padding(horizontal = 14.dp, vertical = 12.dp)
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

            Spacer(Modifier.height(8.dp))

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

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = divColor, thickness = 0.5.dp)
            Spacer(Modifier.height(9.dp))

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

            Spacer(Modifier.height(9.dp))
            HorizontalDivider(color = divColor, thickness = 0.5.dp)
            Spacer(Modifier.height(9.dp))

            // ── Windows-style compact calendar ────────────────────────────────
            CompactCalendar(
                isDark = isDark,
                textColor = textColor,
                accent = bluebirdColors.AccentBlue,
                textScale = textScale
            )

            if (remoteNotifications.isNotEmpty()) {
                Spacer(Modifier.height(9.dp))
                HorizontalDivider(color = divColor, thickness = 0.5.dp)
                Spacer(Modifier.height(9.dp))
                Text(
                    "From Bluebird",
                    color = bluebirdColors.AccentBlue,
                    fontSize = (12f * textScale).sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                remoteNotifications.forEach { notif ->
                    BluebirdRemoteNotifCard(
                        notif = notif,
                        isDark = isDark,
                        textScale = textScale,
                        onDismiss = { viewModel.dismissRemoteNotification(notif.id) },
                        onAction = { url ->
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(5.dp))
                }
            }

            Spacer(Modifier.height(9.dp))
            HorizontalDivider(color = divColor, thickness = 0.5.dp)
            Spacer(Modifier.height(9.dp))

            // ── Real device notifications ────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Notifications",
                    color = textColor,
                    fontSize = (13f * textScale).sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (uiState.notifications.isNotEmpty()) {
                    TextButton(
                        onClick = viewModel::dismissAllNotifications,
                        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 0.dp)
                    ) {
                        Text("Clear all", color = bluebirdColors.AccentBlue, fontSize = (10f * textScale).sp)
                    }
                }
            }

            Spacer(Modifier.height(5.dp))
            if (uiState.notifications.isEmpty()) {
                EmptyNotificationsPlaceholder(isDark = isDark, textColor = subColor, textScale = textScale)
            } else {
                val grouped = remember(uiState.notifications) { uiState.notifications.groupBy { it.appName } }
                grouped.forEach { (appName, notifs) ->
                    NotificationGroup(
                        appName = appName,
                        notifs = notifs,
                        isDark = isDark,
                        textColor = textColor,
                        textScale = textScale,
                        onDismiss = { id -> viewModel.dismissNotification(id) },
                        onOpen = { packageName ->
                            runCatching {
                                context.packageManager.getLaunchIntentForPackage(packageName)
                                    ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    ?.let(context::startActivity)
                            }
                        }
                    )
                    Spacer(Modifier.height(5.dp))
                }
            }
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
            .height(42.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(if (isDark) Color.White.copy(alpha = 0.045f) else Color.Black.copy(alpha = 0.032f))
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(icon, contentDescription = label, tint = textColor.copy(alpha = 0.82f), modifier = Modifier.size(16.dp))
        Text(label, color = textColor.copy(alpha = 0.82f), fontSize = (10f * textScale).sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
private fun CompactCalendar(
    isDark: Boolean,
    textColor: Color,
    accent: Color,
    textScale: Float
) {
    val today = remember { Calendar.getInstance() }
    val monthName = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(today.time) }
    val first = remember {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    val firstDayOffset = remember {
        // Monday-first, matching the compact Windows calendar rhythm.
        ((first.get(Calendar.DAY_OF_WEEK) + 5) % 7)
    }
    val daysInMonth = remember { today.getActualMaximum(Calendar.DAY_OF_MONTH) }
    val weekDays = listOf("M", "T", "W", "T", "F", "S", "S")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(monthName, color = textColor, fontSize = (12f * textScale).sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("Today", color = accent, fontSize = (9.5f * textScale).sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(day, color = textColor.copy(alpha = 0.42f), fontSize = (8.5f * textScale).sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        Spacer(Modifier.height(3.dp))

        val totalCells = ((firstDayOffset + daysInMonth + 6) / 7) * 7
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            repeat(totalCells / 7) { rowIndex ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(7) { colIndex ->
                        val cell = rowIndex * 7 + colIndex
                        val day = cell - firstDayOffset + 1
                        val isCurrent = day == today.get(Calendar.DAY_OF_MONTH) && day in 1..daysInMonth
                        Box(
                            modifier = Modifier.weight(1f).height(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day in 1..daysInMonth) {
                                Box(
                                    modifier = if (isCurrent) Modifier.size(21.dp).clip(CircleShape).background(accent) else Modifier.size(21.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        day.toString(),
                                        color = if (isCurrent) Color.White else textColor.copy(alpha = 0.78f),
                                        fontSize = (9.5f * textScale).sp,
                                        fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
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

// ─── Notification Group ───────────────────────────────────────────────────────

@Composable
private fun NotificationGroup(
    appName: String,
    notifs: List<RealNotification>,
    isDark: Boolean,
    textColor: Color,
    textScale: Float,
    onDismiss: (String) -> Unit,
    onOpen: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    val headerBg = if (isDark) DS.surfaceDark else DS.surfaceLight

    Column {
        // Group header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .background(headerBg)
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(bluebirdColors.AccentBlue)
                )
                Text(
                    text = appName,
                    color = textColor,
                    fontSize = (12 * textScale).sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (notifs.size > 1) {
                    Text(
                        text = "${notifs.size}",
                        color = bluebirdColors.AccentBlue,
                        fontSize = (11 * textScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Icon(
                imageVector = if (expanded) FluentIcon.ChevronUp else FluentIcon.ChevronDown,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }

        // ── Notification list — shown/hidden instantly, no expand animation ──
        if (expanded) {
            Column {
                notifs.forEach { notif ->
                    SwipeToDismissNotifCard(
                        notification = notif,
                        isDark       = isDark,
                        textColor    = textColor,
                        textScale    = textScale,
                        onDismiss    = { onDismiss(notif.id) },
                        onOpen       = { onOpen(notif.packageName) }
                    )
                }
            }
        }
    }
}

// ─── Swipe-to-Dismiss Notification Card ──────────────────────────────────────

@Composable
private fun SwipeToDismissNotifCard(
    notification: RealNotification,
    isDark: Boolean,
    textColor: Color,
    textScale: Float,
    onDismiss: () -> Unit,
    onOpen: () -> Unit
) {
    var offsetX       by remember { mutableStateOf(0f) }
    var isDismissed   by remember { mutableStateOf(false) }
    val threshold     = 300f
    val cardBg        = if (isDark) DS.surfaceDark else DS.surfaceLight
    val haptics       = LocalHapticFeedback.current
    var thresholdCrossed by remember { mutableStateOf(false) }

    if (isDismissed) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (offsetX > 4f) 3.dp else 0.dp,
                shape     = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
                clip      = false
            )
    ) {
        // Dismiss background
        if (offsetX > 40f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                    .background(bluebirdColors.Error.copy(alpha = (offsetX / threshold).coerceIn(0f, 1f))),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = FluentIcon.Delete,
                    contentDescription = null,
                    tint     = Color.White,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(20.dp)
                        .scale(if (offsetX > threshold) 1.15f else 1f)
                )
            }
        }

        Row(
            modifier = Modifier
                .offset { IntOffset(offsetX.toInt(), 0) }
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                .background(cardBg)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX > threshold) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                isDismissed = true
                            } else {
                                offsetX = 0f
                            }
                            thresholdCrossed = false
                        },
                        onHorizontalDrag = { _, delta ->
                            offsetX = (offsetX + delta).coerceAtLeast(0f)
                            // Tick a light haptic exactly once when the drag
                            // first crosses the dismiss threshold, so the
                            // user feels the "point of no return" — a small
                            // but very Windows-11 tactile detail.
                            if (offsetX > threshold && !thresholdCrossed) {
                                thresholdCrossed = true
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            } else if (offsetX <= threshold) {
                                thresholdCrossed = false
                            }
                        }
                    )
                }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // App icon placeholder
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bluebirdColors.AccentBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = FluentIcon.Alert,
                    contentDescription = null,
                    tint     = bluebirdColors.AccentBlue,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text       = notification.title,
                    color      = textColor,
                    fontSize   = (12 * textScale).sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    text     = notification.body,
                    color    = textColor.copy(alpha = 0.7f),
                    fontSize = (11 * textScale).sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = remember(notification.time) {
                        notifTimeFormat.format(java.util.Date(notification.time))
                    },
                    color    = textColor.copy(alpha = 0.4f),
                    fontSize = (10 * textScale).sp
                )

                // Action buttons
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    NotifActionButton(label = "Open", isDark = isDark, textScale = textScale, onClick = onOpen)
                    NotifActionButton(label = "Dismiss", isDark = isDark, textScale = textScale, onClick = onDismiss)
                }
            }
        }
    }
}

@Composable
private fun NotifActionButton(
    label: String,
    isDark: Boolean,
    textScale: Float,
    onClick: () -> Unit
) {
    val bg = if (isDark) DS.surfaceDark else DS.surfaceLight
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            color    = bluebirdColors.AccentBlue,
            fontSize = (11 * textScale).sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─── Bluebird Remote Notif Card ───────────────────────────────────────────────


// ─── Bluebird Remote Notif Card ───────────────────────────────────────────────

@Composable
private fun BluebirdRemoteNotifCard(
    notif: BluebirdRemoteNotification,
    isDark: Boolean,
    textScale: Float,
    onDismiss: () -> Unit,
    onAction: (String) -> Unit
) {
    val accent = try {
        Color(android.graphics.Color.parseColor(notif.badgeColor))
    } catch (e: Exception) { bluebirdColors.AccentBlue }

    val bg     = accent.copy(alpha = if (isDark) 0.12f else 0.1f)
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Bluebird brand dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(accent)
                .padding(top = 4.dp)
        )

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text       = notif.title,
                color      = textColor,
                fontSize   = (12 * textScale).sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text     = notif.body,
                color    = textColor.copy(alpha = 0.75f),
                fontSize = (11 * textScale).sp,
                maxLines = 3
            )
            if (!notif.actionLabel.isNullOrBlank() && !notif.actionUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accent)
                        .clickable { onAction(notif.actionUrl!!) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        notif.actionLabel!!,
                        color    = Color.White,
                        fontSize = (11 * textScale).sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(4.dp))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = FluentIcon.Dismiss,
                contentDescription = "Dismiss",
                tint     = textColor.copy(alpha = 0.4f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}


// ─── Empty Placeholder ────────────────────────────────────────────────────────

@Composable
private fun EmptyNotificationsPlaceholder(
    isDark: Boolean,
    textColor: Color,
    textScale: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = FluentIcon.AlertOff,
                contentDescription = null,
                tint     = textColor.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )
            Text(
                "No new notifications",
                color    = textColor.copy(alpha = 0.4f),
                fontSize = (12 * textScale).sp
            )
        }
    }
}
