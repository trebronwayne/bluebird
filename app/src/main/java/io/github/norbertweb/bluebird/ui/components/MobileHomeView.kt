package io.github.norbertweb.bluebird.ui.components

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.norbertweb.bluebird.AppInfo
import io.github.norbertweb.bluebird.LauncherScreen
import io.github.norbertweb.bluebird.LauncherUiState
import io.github.norbertweb.bluebird.LauncherViewModel
import io.github.norbertweb.bluebird.ui.theme.bluebirdColors

// ─────────────────────────────────────────────────────────
// MOBILE HOME VIEW — v4
//
// v3 painted a near-opaque flat color across the whole screen
// (`background(lerp(surface, accent, 0.12f))`) which completely hid the
// real Acrylic blur the parent StartMenu Box already applies via
// hazeMaterial()/windowsAcrylic() — mobile mode was rendering on top of a
// blurred panel but you'd never know it. That flat background is gone.
// This view is now transparent by default and lets the parent's blur show
// through, the same way the desktop Start Menu body does; icon tiles use
// DS.controlFill() (a translucent scrim over the blur) instead of a flat
// opaque surface color, for the same "same material, different depth"
// reason described in FluentDesignSystem.kt. No gradients anywhere here —
// tone comes from alpha layering only, not from a manufactured light source.
//
// New: a "Recent" row at the top of the list, showing up to 6 apps ranked
// by open count — reuses the same open-count tracking StartMenu.kt's
// desktop "Recent" tab already reads (getAppOpenCount, now `internal` so
// this file can call it), so the two "Recent"s agree. (Not scroll-sticky —
// this project's Compose Foundation version doesn't resolve stickyHeader —
// so it stays as the first section rather than floating over the rest.)
//
// Sections, top to bottom:
//   • Recent    — max 6, most-launched apps (new)
//   • Pinned    — pinned apps + pinned system apps
//   • System    — Bluebird's built-in apps
//   • All Apps  — every installed app, alphabetically grouped
// ─────────────────────────────────────────────────────────
@Composable
fun MobileHomeView(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel,
    isDark: Boolean,
    context: Context,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val sortedApps = remember(uiState.installedApps) {
        uiState.installedApps.sortedBy { it.name.lowercase() }
    }
    val grouped = remember(sortedApps) {
        sortedApps.groupBy { it.name.firstOrNull()?.uppercaseChar() ?: '#' }
    }

    var pinnedBuiltInNames by remember { mutableStateOf(getPinnedBuiltInAppNames(context)) }
    val pinnedBuiltInApps = remember(pinnedBuiltInNames) {
        builtInApps.filter { it.first in pinnedBuiltInNames }
    }
    val pinnedApps = uiState.pinnedTaskbarApps

    // Most-launched, capped at 6 — recalculated when the app list or open
    // counts change (open counts aren't observable state, so this re-reads
    // on every open-count bump via the `recentTick` trick below).
    var recentTick by remember { mutableStateOf(0) }
    val recentApps = remember(uiState.installedApps, recentTick) {
        uiState.installedApps
            .map { it to getAppOpenCount(context, it.packageName) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(6)
            .map { it.first }
    }

    fun openApp(app: AppInfo) {
        incrementMobileHomeOpenCount(context, app.packageName)
        viewModel.openApp(context, app)
        recentTick++
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (recentApps.isNotEmpty()) {
            item(key = "recent_sticky") {
                MobileRecentBar(recentApps, isDark, onClick = { openApp(it) })
            }
        }

        if (pinnedApps.isNotEmpty() || pinnedBuiltInApps.isNotEmpty()) {
            item { MobileSectionHeader("Pinned", isDark) }
            items(pinnedApps.chunked(4), key = { "pinned_row_" + it.joinToString { a -> a.packageName } }) { row ->
                MobileAppRow(row, isDark, onClick = { openApp(it) })
            }
            if (pinnedBuiltInApps.isNotEmpty()) {
                items(pinnedBuiltInApps.chunked(4), key = { "pinned_sys_row_" + it.joinToString { s -> s.first } }) { row ->
                    MobileBuiltInAppRow(row, isDark, onClick = { viewModel.openWindow(it) })
                }
            }
            item { Spacer(Modifier.height(10.dp)) }
        }

        item { MobileSectionHeader("System", isDark) }
        items(builtInApps.chunked(4), key = { "sys_row_" + it.joinToString { s -> s.first } }) { row ->
            MobileBuiltInAppRow(row, isDark, onClick = { viewModel.openWindow(it) })
        }
        item { Spacer(Modifier.height(10.dp)) }

        item { MobileSectionHeader("All Apps", isDark) }
        grouped.forEach { (letter, apps) ->
            item(key = "letter_$letter") {
                Text(
                    letter.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DS.accentStart,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            items(apps.chunked(4), key = { "all_row_" + it.joinToString { a -> a.packageName } }) { row ->
                MobileAppRow(row, isDark, onClick = { openApp(it) })
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ─────────────────────────────────────────────────────────
// Recent — sticky top bar, max 6 apps by open count
// ─────────────────────────────────────────────────────────
@Composable
private fun MobileRecentBar(apps: List<AppInfo>, isDark: Boolean, onClick: (AppInfo) -> Unit) {
    val textPrimary = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    // Its own background (not just the parent's blur) so it reads as a
    // distinct "Recent" strip up top — controlFill() (the same "recessed
    // control" scrim the desktop search bar uses) at a slightly stronger
    // strength gives it enough separation without a hard edge.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DS.controlFill(isDark, strength = 1.15f), RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp)
    ) {
        Text(
            "Recent",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary.copy(alpha = 0.7f),
            letterSpacing = 0.4.sp,
            modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            apps.forEach { app ->
                Box(modifier = Modifier.weight(1f)) {
                    MobileAppIcon(app = app, isDark = isDark, iconSize = 48, onClick = { onClick(app) })
                }
            }
            repeat(6 - apps.size) { Spacer(modifier = Modifier.weight(1f)) }
        }
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun MobileSectionHeader(title: String, isDark: Boolean) {
    val textPrimary = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    Text(
        title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = textPrimary.copy(alpha = 0.7f),
        letterSpacing = 0.4.sp,
        modifier = Modifier.padding(top = 6.dp, bottom = 6.dp)
    )
}

@Composable
private fun MobileAppRow(apps: List<AppInfo>, isDark: Boolean, onClick: (AppInfo) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        apps.forEach { app ->
            Box(modifier = Modifier.weight(1f)) {
                MobileAppIcon(app = app, isDark = isDark, onClick = { onClick(app) })
            }
        }
        repeat(4 - apps.size) { Spacer(modifier = Modifier.weight(1f)) }
    }
}

@Composable
private fun MobileBuiltInAppRow(
    apps: List<Triple<String, ImageVector, LauncherScreen>>,
    isDark: Boolean,
    onClick: (LauncherScreen) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        apps.forEach { (name, icon, screen) ->
            Box(modifier = Modifier.weight(1f)) {
                MobileBuiltInAppIcon(name = name, icon = icon, isDark = isDark, onClick = { onClick(screen) })
            }
        }
        repeat(4 - apps.size) { Spacer(modifier = Modifier.weight(1f)) }
    }
}

@Composable
private fun MobileAppIcon(
    app: AppInfo,
    isDark: Boolean,
    showLabel: Boolean = true,
    iconSize: Int = 56,
    onClick: () -> Unit
) {
    val textPrimary = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    var pressed by remember { mutableStateOf(false) }
    val bitmap = rememberAppIconBitmap(app) // off-main-thread cached decode — fixes stutter

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .pointerInput(app.packageName) {
                detectTapGestures(
                    onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                    onTap = { onClick() }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .size(iconSize.dp)
                .clip(RoundedCornerShape(16.dp))
                // Was a flat opaque DS.surfaceDark/Light — same fix as the
                // desktop search bar: a translucent scrim over the blur
                // instead of a solid color that hides it.
                .background(if (pressed) DS.controlFillFocused(isDark) else DS.controlFill(isDark)),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(bitmap = bitmap, contentDescription = app.name, modifier = Modifier.size((iconSize - 12).dp))
            } else {
                Icon(FluentIcon.Apps, null, tint = DS.accentStart, modifier = Modifier.size((iconSize / 2.5).dp))
            }
        }
        if (showLabel) {
            Spacer(Modifier.height(4.dp))
            Text(
                app.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.width((iconSize + 16).dp)
            )
        }
    }
}

@Composable
private fun MobileBuiltInAppIcon(
    name: String,
    icon: ImageVector,
    isDark: Boolean,
    iconSize: Int = 56,
    onClick: () -> Unit
) {
    val textPrimary = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    var pressed by remember { mutableStateOf(false) }
    val customIconResId = rememberBuiltInIconResourceId(name)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .pointerInput(name) {
                detectTapGestures(
                    onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                    onTap = { onClick() }
                )
            }
    ) {
        if (customIconResId != 0) {
            Box(
                modifier = Modifier
                    .size(iconSize.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (pressed) DS.controlFillFocused(isDark) else DS.controlFill(isDark)),
                contentAlignment = Alignment.Center
            ) {
                BuiltInAppIcon(
                    appName = name,
                    fallback = icon,
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(iconSize.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DS.accentStart),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, name, tint = Color.White, modifier = Modifier.size((iconSize / 2.5).dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            name,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width((iconSize + 16).dp)
        )
    }
}

private fun incrementMobileHomeOpenCount(context: Context, packageName: String) {
    val prefs = context.getSharedPreferences("start_menu_usage_prefs", Context.MODE_PRIVATE)
    val current = prefs.getInt("open_cnt_$packageName", 0)
    prefs.edit().putInt("open_cnt_$packageName", current + 1).apply()
}
