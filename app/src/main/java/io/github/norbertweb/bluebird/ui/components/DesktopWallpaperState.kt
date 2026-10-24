package io.github.norbertweb.bluebird.ui.components

import io.github.norbertweb.bluebird.R

// ─────────────────────────────────────────────────────────────────
// Wallpaper mode
// APPARENT  – animated gradient colours (5 presets, cycles automatically)
// DEFAULT   – bundled bitmap wallpaper(s) from res/drawable. With a single
//             wallpaper selected it's static; if the user selects more than
//             one from Settings, they rotate automatically every hour (fixed
//             interval, not user-configurable — see WALLPAPER_CYCLE_MS in
//             Desktop.kt and DesktopPreferences.defaultWallpaperSelection).
// CUSTOM    – user-chosen image from gallery / file picker (URI stored in prefs)
// ─────────────────────────────────────────────────────────────────
enum class DesktopWallpaperMode { APPARENT, DEFAULT, CUSTOM }

// ─────────────────────────────────────────────────────────────────
// Default wallpaper drawable resource IDs.
// Only one bundled wallpaper ships by default; more can be added back to this
// list later the same way (each one just needs a matching drawable resource).
// ─────────────────────────────────────────────────────────────────
val DEFAULT_WALLPAPERS: List<Int> = listOf(
    R.drawable.desktop_wp_1, R.drawable.desktop_wp_2, R.drawable.desktop_wp_1
)

