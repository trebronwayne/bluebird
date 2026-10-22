package io.github.norbertweb.bluebird.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.FluentMaterials

// ─────────────────────────────────────────────────────────
// REAL BACKDROP BLUR — Windows 11 Mica & Acrylic
//
// First pass at this wired a translucent DS.glass(isDark, opacity) straight
// into HazeStyle.backgroundColor. That was the wrong half of Haze's API:
// backgroundColor is meant to be an opaque backstop, and the actual tint
// wash is supposed to go through `tints` using blend modes — which is also
// what made real Mica/Acrylic not look "too bright": the Luminosity-blend
// tint actively normalizes brightness regardless of what's behind it,
// where a plain low-alpha background just lets the raw blur show through
// unmodified.
//
// This now uses dev.chrisbanes.haze.materials.FluentMaterials — Haze's own
// implementation of Windows 11's Mica/Acrylic, values taken directly from
// Microsoft's published WinUI 3 Figma file — plus a small custom wrapper
// (windowsMica/windowsAcrylic) that adds the one thing FluentMaterials
// doesn't expose: a dynamic accent so Mica/Acrylic can follow Bluebird's
// own wallpaper (see WallpaperAccent.kt) the way real Windows 11 follows
// the desktop background.
//
// Dark/light theme must stay the dominant signal, though — the wallpaper
// accent is only ever a hint layered on top of it, never a replacement for
// it. subtleAccent() below blends the raw wallpaper color into the
// theme's own neutral base (DS.surfaceDark/Light) at a low fixed fraction
// before it ever reaches the Fluent formula, so:
//   - A vivid/saturated wallpaper can't wash out or fight with dark/light
//     mode — switching themes still produces the same large luminance
//     jump it always did, wallpaper or not.
//   - What actually changes with the wallpaper is subtle: a faint color
//     cast, the way Windows 11's own Mica shift is subtle in practice
//     despite technically "sampling the background."
// Feeding the *raw* accent straight into containerColor/accentColor (the
// first version of this) let the wallpaper's hue dominate the material
// entirely, which is what made it feel like it was overriding the theme
// rather than sitting quietly on top of it.
//
// TWO MATERIALS, matching how Windows 11 actually splits these:
//   Mica    — the taskbar itself: persistent shell chrome, near-opaque,
//             huge blur radius (240dp — this is correct, not a typo; the
//             tint/luminosity blend is what keeps it from looking like a
//             blown-out white/black smear), no noise.
//   Acrylic — everything that "flies out" over the shell: Start Menu,
//             Search, Action Center, Widgets, Power Menu, and the
//             taskbar's own popups (overflow icons, hidden tray,
//             settings). More translucent, smaller blur radius (60dp),
//             a whisper of noise (0.02) so it doesn't read as a flat glow.
// This mirrors real Windows 11 and also restores the tonal boundary
// between "the shell" and "things floating on top of it" that got lost
// when every surface shared one identical translucent tint.
// ─────────────────────────────────────────────────────────

@Composable
fun rememberBluebirdHazeState(): HazeState = remember { HazeState() }

/**
 * Global "Transparency effects" switch (Settings → Appearance). When false every Acrylic/Mica
 * style becomes a flat, fully opaque fill with no blur: nothing is sampled or blurred, which is
 * the performance-friendly mode (also chosen by the setup "Smooth" preset).
 */
val LocalTransparencyEnabled = androidx.compose.runtime.compositionLocalOf { true }

private fun opaqueFallbackStyle(base: Color): HazeStyle {
    val solid = base.copy(alpha = 1f)
    return HazeStyle(blurRadius = 0.dp, noiseFactor = 0f, backgroundColor = solid, tints = emptyList(), fallbackTint = HazeTint(solid))
}

/** Blends [accent] into the current theme's own neutral surface color at a
 *  low, fixed fraction — see the file doc above for why this exists. Mica
 *  gets a slightly stronger hint than Acrylic (12% vs 9%) since Mica is a
 *  single flat persistent surface with nothing else going on, while
 *  Acrylic panels have more foreground content that a strong tint would
 *  fight with for legibility. */
private fun subtleAccent(accent: Color, isDark: Boolean, strength: Float): Color {
    val neutral = if (isDark) DS.surfaceDark else DS.surfaceLight
    return lerp(neutral, accent, strength)
}

/** Windows 11 Mica, tinted by a muted hint of Bluebird's wallpaper accent
 *  instead of FluentMaterials.mica()'s fixed neutral — see file doc above. */
@Composable
@ReadOnlyComposable
fun windowsMica(accent: Color, isDark: Boolean): HazeStyle {
    val containerColor = subtleAccent(accent, isDark, strength = 0.12f)
    if (!LocalTransparencyEnabled.current) return opaqueFallbackStyle(containerColor)
    // Same opacities Haze's own hazeMicaMaterial() uses (WinUI 3 Figma spec)
    // — only the input color is dynamic here, not the recipe.
    val tintOpacity = if (isDark) 0.8f else 0.5f
    val luminosityOpacity = 1f
    return HazeStyle(
        blurRadius = 240.dp,
        noiseFactor = 0f,
        backgroundColor = containerColor,
        tints = listOf(
            HazeTint(color = containerColor.copy(alpha = tintOpacity), blendMode = BlendMode.Color),
            HazeTint(color = containerColor.copy(alpha = luminosityOpacity), blendMode = BlendMode.Luminosity),
        ),
        fallbackTint = HazeTint(containerColor)
    )
}

/** Windows 11 Acrylic, tinted by a muted hint of the wallpaper accent —
 *  thin wrapper around Haze's own accent-aware Fluent material so callers
 *  don't need the `@OptIn`/import at every call site. */
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
@ReadOnlyComposable
fun windowsAcrylic(accent: Color, isDark: Boolean): HazeStyle {
    if (!LocalTransparencyEnabled.current) return opaqueFallbackStyle(subtleAccent(accent, isDark, strength = 0.09f))
    return FluentMaterials.accentAcrylicDefault(
        accentColor = subtleAccent(accent, isDark, strength = 0.09f),
        isDark = isDark
    )
}


/**
 * Legacy Start-body style kept for compatibility with callers that may still
 * reference it. The Start surface itself no longer applies a nested Haze
 * effect: nesting the same HazeState inside its own blurred panel can expose
 * intermediate transparent layers and produce the broken centre seen on both
 * themes. The outer Start Acrylic is the single backdrop-blur source.
 */
@Composable
@ReadOnlyComposable
fun windowsStartBodyHaze(accent: Color, isDark: Boolean): HazeStyle {
    val base = subtleAccent(accent, isDark, strength = 0.04f)
    return HazeStyle(
        blurRadius = 0.dp,
        noiseFactor = 0f,
        backgroundColor = base,
        tints = emptyList(),
        fallbackTint = HazeTint(base)
    )
}

/** Applies a Fluent [HazeStyle] (see [windowsMica]/[windowsAcrylic] above)
 *  to this panel, clipped to [shape]. Drop-in replacement for what used to
 *  be `.clip(shape).background(flatTintColor)`. */
fun Modifier.hazeMaterial(
    hazeState: HazeState,
    style: HazeStyle,
    shape: Shape = RectangleShape
): Modifier = this
    .clip(shape)
    .hazeEffect(state = hazeState, style = style)
