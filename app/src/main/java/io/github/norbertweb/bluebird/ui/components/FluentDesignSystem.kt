package io.github.norbertweb.bluebird.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────
// FLUENT DESIGN SYSTEM
// Single shared token set for every Windows-11-style surface in the app
// (Start Menu, Search Overlay, Taskbar, …). Previously StartMenu.kt and
// SearchOverlay.kt each defined their own colors/corner radii, which is
// why they visually drifted apart. Everything now reads from here.
//
// Glass/material tinting used to live here too, as a user-controllable
// "surface opacity" slider (Settings > Personalization > Transparency).
// That's gone — no UI ever actually called its setter, so in practice
// every panel was just a fixed 0.85 alpha anyway, and a flat alpha over
// raw blurred wallpaper is exactly what made the old blur look "too
// bright"/washed out with no relation to the actual wallpaper's tone.
// Materials are now real Fluent Mica/Acrylic (see HazeBlurSetup.kt),
// tinted dynamically from Bluebird's own active wallpaper (see
// WallpaperAccent.kt) instead of a manual slider.
// ─────────────────────────────────────────────────────────
object DS {
    // ── Premium palette — deep slate + refined indigo ──
    // Replaces the old plain "Windows blue" (#0078D4) accent and the
    // washed-out translucent-white/black borders. Borders are now real,
    // slightly-tinted slate/gray tones so panels read as separated
    // surfaces instead of one flat plane with a faint outline — this is
    // what gives light and dark mode each their own distinct, deliberate
    // tone instead of one palette with alpha tweaks.
    val surfaceDark  = Color(0xFF202020)
    val surfaceLight = Color(0xFFF7F7F7)
    val borderDark   = Color(0xFF3A3A3A)
    val borderLight  = Color(0xFFD8D8D8)
    val accentStart  = Color(0xFF0078D4)
    val accentEnd    = Color(0xFF005FB8)
    val hoverDark    = Color(0x1AFFFFFF)
    val hoverLight   = Color(0x0A1B1E26)
    val pressedDark  = Color(0x26FFFFFF)
    val pressedLight = Color(0x141B1E26)
    val badgeRed     = Color(0xFFD6564C)
    val successGreen = Color(0xFF3EA66D)

    val menuWidthCompact   = 560.dp
    val menuWidthExpanded  = 780.dp
    val menuHeightCompact  = 660.dp
    val menuHeightExpanded = 840.dp

    // Shared corner language — SearchOverlay previously used 16dp while
    // Start Menu used 8dp for the same kind of flyout surface. Unified here.
    val cornerRadius  = 8.dp   // Windows 11 flyout/menu corner radius
    val sectionCorner = 8.dp
    val tileCorner    = 6.dp   // Fluent 2 control-corner-radius, small icon tiles
    val chipCorner    = 6.dp
    val overlayCorner = 20.dp  // Shared shell flyout radius

    val accentBrushValue: Brush = Brush.linearGradient(
        colors = listOf(accentStart, accentEnd),
        start = Offset(0f, 0f), end = Offset(160f, 160f)
    )
    fun accentBrush() = accentBrushValue

    // ── Tonal elevation — "same material, different depth" ────────────
    // This is the piece Start Menu was missing. Real Fluent 2 never gives a
    // zone (search box, card, footer) its own flat color — every one of
    // those "fill" tokens (ControlFillColorDefault, CardBackgroundFillColor,
    // …) is a translucent white-or-black scrim layered ON TOP OF whatever
    // Mica/Acrylic is already there. Dark theme lifts a surface by adding a
    // hint of WHITE (nothing to reflect, so light = "closer to camera");
    // light theme recedes a surface by adding a hint of BLACK (nothing to
    // absorb, so dark = "further from camera"). Same base material either
    // way — only the direction and strength of the scrim change.
    //
    // Use these as a `.background(...)` painted on top of a parent that
    // already has `.hazeMaterial(...)` applied — never as a replacement for
    // it, and never as an opaque color. A zone with no scrim at all (alpha
    // 0) is exactly as "elevated" as the raw panel — that's `flat` below,
    // used for the body/content area which should feel like it's sitting
    // directly on the Acrylic, not floating above or sunk below it.
    fun controlFill(isDark: Boolean, strength: Float = 1f): Color =
        if (isDark) Color.White.copy(alpha = 0.16f * strength)
        else Color.Black.copy(alpha = 0.06f * strength)

    /** Focused/active state of [controlFill] — same direction, ~1.4x stronger. */
    fun controlFillFocused(isDark: Boolean): Color = controlFill(isDark, strength = 1.4f)

    /** For the body/content zone — deliberately zero, see note above. */
    val surfaceFlat: Color = Color.Transparent

    /** Footer/bottom-bar zone. Unlike controlFill, this is BLACK in both
     *  themes, not white-in-dark — a footer is structural chrome receding
     *  into shadow (like Windows 11's taskbar, which is denser Mica, not
     *  the lighter Acrylic flyouts float on), not an interactive control
     *  lifting toward the user. Strong enough in dark theme that the
     *  wallpaper blur underneath mostly disappears — that's intentional,
     *  it's what reads as "a different, denser material" rather than "the
     *  same panel with a tint." */
    fun footerFill(isDark: Boolean): Color =
        if (isDark) Color.Black.copy(alpha = 0.55f)
        else Color.Black.copy(alpha = 0.15f)

    // ── Spacing scale ────────────────────────────────────────────────
    // Fluent 2 uses a 4px base grid. Before this, padding across the
    // shell was ad hoc — e.g. Taskbar alone mixed 4/6/8/10/12/14dp for
    // paddings serving the same visual role (icon inset, row padding,
    // panel edge) depending on which line happened to write it. This is
    // the same 4/8/12/16/24 rhythm Windows 11's own Fluent components use;
    // new code should reach for these instead of a bare `.dp` literal.
    object Spacing {
        val xxs = 2.dp   // hairline gaps (icon-to-badge, chip internals)
        val xs  = 4.dp   // tight icon padding, dense list rows
        val sm  = 8.dp   // default gap between related controls
        val md  = 12.dp  // row padding, standard control inset
        val lg  = 16.dp  // section padding, panel edge inset
        val xl  = 24.dp  // panel outer margin, major section gaps
        val xxl = 32.dp  // page-level margins (Settings, full-screen panels)
    }
}

