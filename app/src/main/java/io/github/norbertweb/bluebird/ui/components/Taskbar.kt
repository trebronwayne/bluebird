package io.github.norbertweb.bluebird.ui.components

// Icons come from the shared FluentIcon object (FluentIcon.kt), which wraps
// the io.github.niyajali:fluentui-system-icons Compose Multiplatform library.
// Dependency (module build.gradle.kts):
//     implementation("io.github.niyajali:fluentui-system-icons:1.0.1")
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.norbertweb.bluebird.AppInfo
import io.github.norbertweb.bluebird.LauncherUiState
import io.github.norbertweb.bluebird.LauncherViewModel
import io.github.norbertweb.bluebird.R
import io.github.norbertweb.bluebird.WindowState
import io.github.norbertweb.bluebird.ui.theme.LocalIsDarkTheme
import io.github.norbertweb.bluebird.ui.theme.bluebirdColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// Taskbar Settings State
// ─────────────────────────────────────────────────────────────────────────────
data class TaskbarSettings(
    val showSearchPill: Boolean = true,
    val showWidgets: Boolean = true,
    val showClock: Boolean = true,
    val showBattery: Boolean = true,
    val showVolume: Boolean = true,
    val showNetwork: Boolean = true
)

private object TaskbarPrefs {
    const val FILE = "taskbar_settings"
    private const val K_SEARCH_PILL = "showSearchPill"
    private const val K_WIDGETS = "showWidgets"
    private const val K_CLOCK = "showClock"
    private const val K_BATTERY = "showBattery"
    private const val K_VOLUME = "showVolume"
    private const val K_NETWORK = "showNetwork"
    private const val K_HIDDEN = "taskbarHidden"
    private const val K_SHOW_DESKTOP_ICONS = "show_desktop_icons"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun setHidden(context: Context, hidden: Boolean) {
        prefs(context).edit().putBoolean(K_HIDDEN, hidden).apply()
    }

    fun isHidden(context: Context): Boolean =
        prefs(context).getBoolean(K_HIDDEN, false)

    fun showDesktopIcons(context: Context): Boolean =
        prefs(context).getBoolean(K_SHOW_DESKTOP_ICONS, true)

    fun setShowDesktopIcons(context: Context, show: Boolean) {
        prefs(context).edit().putBoolean(K_SHOW_DESKTOP_ICONS, show).apply()
    }

    fun load(context: Context): TaskbarSettings {
        val p = prefs(context)
        return TaskbarSettings(
            showSearchPill = p.getBoolean(K_SEARCH_PILL, true),
            showWidgets = p.getBoolean(K_WIDGETS, true),
            showClock = p.getBoolean(K_CLOCK, true),
            showBattery = p.getBoolean(K_BATTERY, true),
            showVolume = p.getBoolean(K_VOLUME, true),
            showNetwork = p.getBoolean(K_NETWORK, true)
        )
    }

    fun save(context: Context, s: TaskbarSettings) {
        prefs(context).edit()
            .putBoolean(K_SEARCH_PILL, s.showSearchPill)
            .putBoolean(K_WIDGETS, s.showWidgets)
            .putBoolean(K_CLOCK, s.showClock)
            .putBoolean(K_BATTERY, s.showBattery)
            .putBoolean(K_VOLUME, s.showVolume)
            .putBoolean(K_NETWORK, s.showNetwork)
            .apply()
    }
}

// Icon lookups (including `iconForKey`) now come from the shared FluentIcon.kt,
// used by every UI file in this package (Start Menu, Desktop, TaskBar, WindowManager, Settings).

/**
 * Resolves a window's real bitmap icon (e.g. a fetched favicon) off the main thread.
 * Returns null while loading or on any failure/absence, so callers should keep the
 * Material-icon fallback visible until this resolves.
 */
@Composable
fun rememberWindowBitmapIcon(customIconPath: String?): androidx.compose.ui.graphics.ImageBitmap? {
    val context = LocalContext.current
    val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, customIconPath) {
        value = if (!customIconPath.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                try {
                    val f = java.io.File(context.filesDir, customIconPath)
                    if (f.exists()) android.graphics.BitmapFactory.decodeFile(f.absolutePath) else null
                } catch (_: Exception) { null }
            }
        } else null
    }
    return bitmap?.asImageBitmap()
}

// ─────────────────────────────────────────────────────────────────────────────
// Main Taskbar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun bluebirdTaskbar(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel,
    hazeState: dev.chrisbanes.haze.HazeState,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current

    // The production Taskbar intentionally has one fixed, compact form.
    // It follows the Start Menu material and never switches into launcher-style
    // alternate layouts that could crowd narrow screens.
    val taskbarHeight = 40.dp
    val iconSize = 15.dp
    val taskbarShape = RectangleShape
    val taskbarBorderColor = if (isDark) DS.borderDark else DS.borderLight
    val taskbarEdgeColor = if (isDark) Color(0x1FFFFFFF) else Color(0x1F171A21)
    val showDesktopIcons = remember { TaskbarPrefs.showDesktopIcons(context) }
    var desktopIconsVisible by remember(showDesktopIcons) { mutableStateOf(showDesktopIcons) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(taskbarHeight)
            .hazeMaterial(hazeState, windowsMica(accent, isDark), taskbarShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(taskbarEdgeColor, Color.Transparent),
                    endY = 10f
                )
            )
            .border(0.5.dp, taskbarBorderColor, taskbarShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left-aligned Windows-style shell order:
            // Widgets → Start → Search → app/window strip.
            TaskbarIconButton(
                icon = FluentIcon.Widget,
                contentDescription = "Widgets",
                isActive = uiState.isWidgetsOpen,
                isDark = isDark,
                onClick = { viewModel.toggleWidgets() },
                iconSize = iconSize
            )

            Spacer(Modifier.width(8.dp))

            StartButton(
                isActive = uiState.isStartMenuOpen,
                isDark = isDark,
                onClick = { viewModel.toggleStartMenu() }
            )

            Spacer(Modifier.width(6.dp))

            SearchPill(
                isDark = isDark,
                onClick = { viewModel.toggleSearch() }
            )

            Spacer(Modifier.width(6.dp))

            // This strip is allowed to scroll instead of shrinking into overlap.
            TaskbarWindowStrip(
                uiState = uiState,
                viewModel = viewModel,
                iconSize = iconSize,
                isDark = isDark,
                context = context,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            Spacer(Modifier.width(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                TaskbarIconButton(
                    icon = FluentIcon.Desktop,
                    contentDescription = if (desktopIconsVisible) "Hide desktop icons" else "Show desktop icons",
                    isDark = isDark,
                    onClick = {
                        val next = !desktopIconsVisible
                        desktopIconsVisible = next
                        TaskbarPrefs.setShowDesktopIcons(context, next)
                    },
                    iconSize = iconSize
                )
                SystemTray(
                    uiState = uiState,
                    settings = TaskbarSettings(),
                    isDark = isDark,
                    onClickActionCenter = { viewModel.toggleActionCenter() }
                )
            }
        }
    }
}

@Composable
private fun TaskbarWindowStrip(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel,
    iconSize: Dp,
    isDark: Boolean,
    context: Context,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val pinnedApps = uiState.pinnedTaskbarApps.distinctBy { it.packageName }

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        pinnedApps.forEach { app ->
            val runningWindowsForApp = remember(uiState.openWindows, app.packageName, app.name) {
                uiState.openWindows.filter { it.title.contains(app.name, ignoreCase = true) }
            }
            val runningWindow = runningWindowsForApp.firstOrNull { !it.isMinimized }
            val isRunning = runningWindowsForApp.isNotEmpty()
            val isActive = runningWindow?.id == uiState.activeWindowId

            TaskbarAppIcon(
                appInfo = app,
                showLabel = false,
                isRunning = isRunning,
                isActive = isActive,
                isDark = isDark,
                onClick = {
                    val existing = uiState.openWindows.firstOrNull { it.title.contains(app.name, true) }
                    if (existing != null) {
                        if (existing.isMinimized) viewModel.restoreWindow(existing.id)
                        else if (existing.id == uiState.activeWindowId) viewModel.minimizeWindow(existing.id)
                        else viewModel.setActiveWindow(existing.id)
                    } else {
                        viewModel.openApp(context, app)
                    }
                },
                onLongClick = {}
            )
        }

        uiState.openWindows.forEach { window ->
            if (pinnedApps.none { window.title.contains(it.name, ignoreCase = true) }) {
                TaskbarWindowIcon(
                    windowState = window,
                    showLabel = false,
                    isActive = window.id == uiState.activeWindowId,
                    isDark = isDark,
                    onClick = {
                        if (window.isMinimized) viewModel.restoreWindow(window.id)
                        else if (window.id == uiState.activeWindowId) viewModel.minimizeWindow(window.id)
                        else viewModel.setActiveWindow(window.id)
                    }
                )
            }
        }
    }
}
@Composable
private fun StartButton(isActive: Boolean, isDark: Boolean, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    // Windows 11 gives the Start button a soft accent glow while the menu is
    // open, plus a lighter press flash — now an instant state switch instead
    // of a spring/tween animation.
    val bgAlpha = if (isActive) 1f else if (pressed) 0.6f else 0f
    val hoverBg = if (isDark) bluebirdColors.HoverBg else bluebirdColors.HoverBgLight
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isActive) DS.accentStart.copy(alpha = 0.22f * bgAlpha)
                else hoverBg.copy(alpha = bgAlpha)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_taskbar_start_logo),
            contentDescription = "Start",
            modifier = Modifier.size(18.dp)
        )
    }
}

// (WindowsLogo Canvas glyph removed — StartButton now renders the custom
// ic_taskbar_start_logo.xml gradient logo via painterResource instead.)

// ─────────────────────────────────────────────────────────────────────────────
// Search Pill
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SearchPill(isDark: Boolean, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val bgColor = if (isDark) {
        if (pressed) Color(0x1FFFFFFF) else Color(0x14FFFFFF)
    } else {
        if (pressed) Color(0x14171A21) else Color(0x0A171A21)
    }
    val borderColor = if (isDark) DS.borderDark else DS.borderLight
    val mutedText = if (isDark) Color(0xFFAAAAAA) else Color(0xFF6B7280)
    val mutedIcon = if (isDark) Color(0xFFBBBBBB) else Color(0xFF6B7280)
    Row(
        modifier = Modifier
            .width(160.dp).height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(0.5.dp, borderColor.copy(alpha = 0.72f), RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                    onTap = { onClick() }
                )
            }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Icon(imageVector = FluentIcon.Search, contentDescription = "Search", tint = mutedIcon, modifier = Modifier.size(15.dp))
        Text("Search", color = mutedText, fontSize = 12.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Generic Taskbar Icon Button
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun TaskbarIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    isActive: Boolean = false,
    isDark: Boolean = true,
    onClick: () -> Unit,
    iconSize: Dp = 15.dp,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    // Was `val scale = if (pressed) 0.88f else 1f` — an instant hard-cut with
    // no animation at all between the two values, so every icon press in the
    // taskbar (Start, Search, Widgets, Desktop toggle, pinned apps) snapped
    // rather than moved. This is the single most-touched interaction in the
    // whole shell, so it's the highest-leverage place to actually use the
    // Motion spring system (previously defined in SoftUI.kt but never
    // referenced anywhere in the app).
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = Motion.micro(),
        label = "taskbarIconPress"
    )
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    val hoverBg = if (isDark) bluebirdColors.HoverBg else bluebirdColors.HoverBgLight
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = modifier
                .size(32.dp).scale(scale)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isActive) hoverBg else Color.Transparent)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                        onTap   = { onClick() }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = contentDescription, tint = textColor, modifier = Modifier.size(iconSize))
        }
        if (isActive) {
            Box(Modifier.width(12.dp).height(2.dp)
                .clip(RoundedCornerShape(1.dp)).background(bluebirdColors.AccentBlue))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pinned App Icon
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TaskbarAppIcon(
    appInfo: AppInfo,
    showLabel: Boolean,
    isRunning: Boolean,
    isActive: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var pressed  by remember { mutableStateOf(false) }
    // Same fix as TaskbarIconButton above — was an instant hard-cut scale.
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.85f else 1f,
        animationSpec = Motion.micro(),
        label = "taskbarAppIconPress"
    )
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    val hoverBg = if (isDark) Color(0x12FFFFFF) else Color(0x0A171A21)
    // This is a Material3 DropdownMenu, which renders its own internal
    // Surface that isn't reachable to redirect through hazeMaterial without
    // replacing DropdownMenu with a custom Popup()-based menu (a bigger
    // follow-up, not done in this pass) — so it stays a flat, neutral tint
    // rather than an accent-tinted or blurred one.
    val menuBg = (if (isDark) DS.surfaceDark else DS.surfaceLight).copy(alpha = 0.96f)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .then(if (showLabel) Modifier.width(56.dp) else Modifier.size(32.dp))
                .height(32.dp).scale(scale)
                .clip(RoundedCornerShape(6.dp))
                .background(when {
                    isActive  -> bluebirdColors.AccentBlue.copy(alpha = 0.18f)
                    isRunning -> hoverBg
                    else      -> Color.Transparent
                })
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress     = { pressed = true; tryAwaitRelease(); pressed = false },
                        onTap       = { onClick() },
                        onLongPress = { showMenu = true }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (showLabel) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    AppIconSmall(drawable = appInfo.icon, contentDescription = appInfo.name,
                        modifier = Modifier.size(18.dp).clip(RoundedCornerShape(3.dp)))
                    Text(appInfo.name, color = textColor, fontSize = 10.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            } else {
                AppIconSmall(drawable = appInfo.icon, contentDescription = appInfo.name,
                    modifier = Modifier.size(20.dp).clip(RoundedCornerShape(4.dp)))
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(menuBg)
            ) {
                DropdownMenuItem(
                    text = { Text("Open", color = textColor, fontSize = 12.sp) },
                    onClick = { showMenu = false; onClick() },
                    leadingIcon = { Icon(imageVector = FluentIcon.Open, contentDescription = null, tint = textColor,
                        modifier = Modifier.size(13.dp)) }
                )
                DropdownMenuItem(
                    text = { Text("Unpin from taskbar", color = textColor, fontSize = 12.sp) },
                    onClick = { showMenu = false; onLongClick() },
                    leadingIcon = { Icon(imageVector = FluentIcon.Pin, contentDescription = null, tint = textColor,
                        modifier = Modifier.size(13.dp)) }
                )
                DropdownMenuItem(
                    text = { Text("Close window", color = DS.badgeRed, fontSize = 12.sp) },
                    onClick = { showMenu = false },
                    leadingIcon = { Icon(imageVector = FluentIcon.Dismiss, contentDescription = null, tint = DS.badgeRed,
                        modifier = Modifier.size(13.dp)) }
                )
            }
        }

        if (isRunning || isActive) {
            Box(
                modifier = Modifier
                    .width(if (isActive) 14.dp else 4.dp).height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (isActive) bluebirdColors.AccentBlue else textColor.copy(alpha = 0.45f))
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Running Window Icon (supports group badge)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TaskbarWindowIcon(
    windowState: WindowState,
    showLabel: Boolean,
    isActive: Boolean,
    windowCount: Int = 1,
    isDark: Boolean = true,
    onClick: () -> Unit
) {
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    val hoverBg = if (isDark) bluebirdColors.HoverBg else bluebirdColors.HoverBgLight
    val bgColor = if (isActive) bluebirdColors.AccentBlue.copy(alpha = 0.22f)
        else hoverBg.copy(alpha = 0.5f)
    val bmpIcon = rememberWindowBitmapIcon(windowState.customIconPath)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .wrapContentWidth().height(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(bgColor)
                .clickable(onClick = onClick)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box {
                    if (bmpIcon != null) {
                        Image(bmpIcon, contentDescription = windowState.title,
                            modifier = Modifier.size(15.dp).clip(RoundedCornerShape(3.dp)))
                    } else {
                        // Now checks for the same custom SVG Start Menu/Desktop use before
                        // falling back to iconForKey()'s Fluent glyph.
                        WindowKeyIcon(
                            key = windowState.iconKey,
                            tint = if (isActive) textColor else textColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    if (windowCount > 1) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp)
                                .size(13.dp)
                                .clip(CircleShape)
                                .background(bluebirdColors.AccentBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$windowCount", color = Color.White, fontSize = 7.sp,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (showLabel) {
                    Text(text = windowState.title,
                        color = if (isActive) textColor else textColor.copy(alpha = 0.8f),
                        fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        val indicatorWidth = if (isActive) 16.dp else if (windowCount > 1) 10.dp else 6.dp
        Box(
            modifier = Modifier
                .width(indicatorWidth)
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(if (isActive) DS.accentStart else textColor.copy(alpha = 0.35f))
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// System Tray
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SystemTray(
    uiState: LauncherUiState,
    settings: TaskbarSettings,
    isDark: Boolean,
    onClickActionCenter: () -> Unit
) {
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClickActionCenter)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (settings.showNetwork) {
            Icon(
                imageVector = if (uiState.isWifiOn) FluentIcon.Wifi else FluentIcon.WifiOff,
                contentDescription = "Wi-Fi",
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
        }
        if (settings.showVolume) {
            Icon(
                imageVector = when {
                        uiState.volume < 0.01f -> FluentIcon.SpeakerMute
                        uiState.volume < 0.5f  -> FluentIcon.Speaker1
                        else                   -> FluentIcon.Speaker2
                    },
                contentDescription = "Volume",
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
        }
        if (settings.showBattery) {
            Icon(imageVector = FluentIcon.Battery10, contentDescription = "Battery", tint = textColor,
                modifier = Modifier.size(13.dp))
        }
        if (settings.showClock) {
            val nowText by produceState(initialValue = "") {
                while (true) {
                    val cal = Calendar.getInstance()
                    val hour24 = cal.get(Calendar.HOUR_OF_DAY)
                    val minute = cal.get(Calendar.MINUTE)
                    val time = if (uiState.use24HourClock) {
                        "${hour24.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
                    } else {
                        val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
                        val amPm = if (hour24 < 12) "AM" else "PM"
                        "$hour12:${minute.toString().padStart(2, '0')} $amPm"
                    }
                    val date = SimpleDateFormat("M/d/yyyy", Locale.getDefault()).format(cal.time)
                    value = "$time\u0000$date"
                    kotlinx.coroutines.delay(30_000)
                }
            }
            val parts = nowText.split('\u0000', limit = 2)
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 2.dp)
            ) {
                Text(parts.getOrElse(0) { "" }, color = textColor, fontSize = 10.sp,
                    fontWeight = FontWeight.Medium, lineHeight = 12.sp)
                Text(parts.getOrElse(1) { "" }, color = textColor.copy(alpha = 0.55f),
                    fontSize = 8.5.sp, lineHeight = 10.sp)
            }
        }
    }
}
