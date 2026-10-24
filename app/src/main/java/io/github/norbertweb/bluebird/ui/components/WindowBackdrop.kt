package io.github.norbertweb.bluebird.ui.components

import androidx.compose.ui.graphics.isSpecified
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect

// ─────────────────────────────────────────────────────────────────────────────
// WINDOWS 11 MICA FOR APP WINDOWS
//
// Real Windows 11 draws ONE Mica backdrop per window (a heavily blurred, tinted
// sample of the desktop wallpaper — never of other windows) and lets the app's own
// chrome (title bar, nav pane, command bar, status bar) sit TRANSPARENTLY on top of
// it. Only the main content surface gets a translucent "layer on Mica" fill.
// Inactive windows drop back to a flat fallback colour.
//
// The previous implementation applied a separate full-radius blur to every panel
// inside File Explorer (3+ blur passes per window) and, because WindowManager never
// received a HazeState, it never actually ran. It also tried to blur inside Compose
// Popups, which are separate windows and cannot see the wallpaper source.
//
// Now:
//   * WindowManager applies a single [windowMicaBackdrop] to the window frame.
//   * It publishes [LocalMicaBackdrop] = true to the window's content.
//   * Apps read it via [micaOr] / [micaLayerOr] and go transparent / translucent.
//   * Popups and menus never use haze: they keep their opaque fallback colours.
// ─────────────────────────────────────────────────────────────────────────────

/** True while the surrounding window is painting a live Mica backdrop behind its content. */
val LocalMicaBackdrop = compositionLocalOf { false }

/** WinUI "LayerOnMicaBaseAlt" fill: the translucent surface main content sits on. */
fun micaLayerColor(isDark: Boolean): Color =
    if (isDark) Color(0x4D3A3A3A) else Color(0x80FFFFFF)

/** Transparent over Mica (chrome, nav panes, bars); [solid] when there is no Mica backdrop. */
@Composable
fun micaOr(solid: Color): Color = if (LocalMicaBackdrop.current) Color.Transparent else solid

/** Translucent Mica layer for main content; [solid] when there is no Mica backdrop. */
@Composable
fun micaLayerOr(solid: Color, isDark: Boolean): Color =
    if (LocalMicaBackdrop.current) micaLayerColor(isDark) else solid

/**
 * Every app window gets the Mica frame + Mica title bar (Windows 11 does the same for all apps).
 * Apps whose content is Mica-aware (Explorer, Settings, Text Editor) also render their chrome
 * transparently so the backdrop shows through; others keep their own opaque content surface and
 * simply show Mica in the title bar and window border.
 */
@Suppress("UNUSED_PARAMETER")
fun screenUsesMica(screen: io.github.norbertweb.bluebird.LauncherScreen): Boolean = true

/** Win11 "CardBackground" fill: a faint layer over Mica (light: 70% white, dark: 10% white). */
@Composable
fun micaCardOr(solid: Color, isDark: Boolean): Color =
    if (LocalMicaBackdrop.current) (if (isDark) Color(0x1AFFFFFF) else Color(0xB3FFFFFF)) else solid

/**
 * Legibility scrim painted over a shell panel's own Acrylic. The panels used to paint a 90%-opaque
 * fill (0xE6…) over their blur, which hid it completely; a light scrim keeps text readable while
 * the blurred wallpaper stays clearly visible.
 */
val ShellScrimDark = Color(0x331F1F1F)
val ShellScrimLight = Color(0x33F8F8F8)

/** Semi-translucent fill for individual cards that sit ON an acrylic panel (widgets etc.). */
val ShellCardDark = Color(0x8C1F1F1F)
val ShellCardLight = Color(0xB3F8F8F8)

/**
 * Real backdrop blur for Compose `Popup`s (context menus, flyouts). A Popup is a separate OS
 * window, so Haze cannot sample the wallpaper across that boundary; instead we ask the system
 * compositor to blur whatever is behind the popup window (Android 12+, when the device allows
 * cross-window blur). Call once inside the Popup content and give the menu surface a
 * translucent fill. No-op below API 31, when blur is disabled, or with Transparency effects off.
 */
@Composable
fun PopupWindowBlur(radius: androidx.compose.ui.unit.Dp = 30.dp) {
    val view = androidx.compose.ui.platform.LocalView.current
    val enabled = LocalTransparencyEnabled.current
    val radiusPx = with(androidx.compose.ui.platform.LocalDensity.current) { radius.roundToPx() }
    androidx.compose.runtime.LaunchedEffect(view, enabled, radiusPx) {
        if (!enabled || android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) return@LaunchedEffect
        // The popup window is attached a frame or two after first composition: wait for it.
        repeat(8) {
            val root = view.rootView
            val lp = root.layoutParams as? android.view.WindowManager.LayoutParams
            val wm = root.context.getSystemService(android.content.Context.WINDOW_SERVICE) as? android.view.WindowManager
            if (root.isAttachedToWindow && lp != null && wm != null) {
                if (wm.isCrossWindowBlurEnabled) {
                    lp.flags = lp.flags or android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND
                    lp.blurBehindRadius = radiusPx
                    try { wm.updateViewLayout(root, lp) } catch (_: Exception) { }
                }
                return@LaunchedEffect
            }
            androidx.compose.runtime.withFrameNanos { }
        }
    }
}

/**
 * Paints the window's Mica backdrop (wallpaper-sampled, accent-tinted) over this node.
 * Call after `.clip(shape)`; the effect itself is clipped to [shape] as well.
 */
@Composable
fun Modifier.windowMicaBackdrop(
    hazeState: HazeState,
    accent: Color,
    isDark: Boolean,
    shape: Shape
): Modifier = if (LocalTransparencyEnabled.current)
    this.clip(shape).hazeEffect(state = hazeState, style = windowsMica(accent, isDark))
else
    // Transparency off: never leave a window with no backdrop (Haze draws nothing without sources).
    this.clip(shape).background(windowsMica(accent, isDark).backgroundColor.let { if (it.isSpecified) it.copy(alpha = 1f) else if (isDark) Color(0xFF202020) else Color(0xFFF3F3F3) })


/** True when the device can blur what is behind a popup window (Android 12+, not disabled). */
@Composable
fun rememberBlurBehindSupported(): Boolean {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    return androidx.compose.runtime.remember {
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && try {
            (ctx.getSystemService(android.content.Context.WINDOW_SERVICE) as android.view.WindowManager).isCrossWindowBlurEnabled
        } catch (_: Exception) { false }
    }
}

/**
 * Fill for popup menus. Translucent ONLY when there is a real blur behind it (so text stays
 * legible); otherwise nearly opaque. Fully opaque when Transparency effects is off.
 */
@Composable
fun popupMenuFill(isDark: Boolean): Color {
    val transparency = LocalTransparencyEnabled.current
    val blur = rememberBlurBehindSupported()
    return when {
        !transparency -> if (isDark) Color(0xFF202020) else Color(0xFFF8F8F8)
        blur          -> if (isDark) Color(0xB81F1F1F) else Color(0xB8F8F8F8)
        else          -> if (isDark) Color(0xF01F1F1F) else Color(0xF0F8F8F8)
    }
}
