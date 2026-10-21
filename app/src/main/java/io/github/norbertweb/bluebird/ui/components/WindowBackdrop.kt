package io.github.norbertweb.bluebird.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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

/** Screens that opt in to the Mica backdrop (their roots are transparent-aware). */
fun screenUsesMica(screen: io.github.norbertweb.bluebird.LauncherScreen): Boolean = when (screen) {
    io.github.norbertweb.bluebird.LauncherScreen.FILE_EXPLORER,
    io.github.norbertweb.bluebird.LauncherScreen.SETTINGS,
    io.github.norbertweb.bluebird.LauncherScreen.PremiumTextEditorScreen -> true
    else -> false
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
): Modifier = this
    .clip(shape)
    .hazeEffect(state = hazeState, style = windowsMica(accent, isDark))
