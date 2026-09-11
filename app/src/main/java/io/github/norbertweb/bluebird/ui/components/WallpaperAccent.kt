package io.github.norbertweb.bluebird.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ─────────────────────────────────────────────────────────
// WALLPAPER-DRIVEN ACCENT
//
// Real Windows 11 Mica/Acrylic aren't tinted by a user opacity slider — they
// sample the *desktop's own background* and tint the material with a muted
// version of it, so the shell subtly shifts tone with whatever wallpaper is
// active. This is the equivalent for Bluebird: given Bluebird's own active
// wallpaper selection (from DesktopPreferences — NOT the Android system
// wallpaper, which Bluebird doesn't touch), produce one muted [Color] to
// feed into HazeBlurSetup.kt's Fluent-material builders.
//
// Bluebird's wallpaper has three possible sources (see
// DesktopWallpaperState.kt), each handled differently:
//   APPARENT — a coded gradient preset (wallpaperGradients) → just average
//              the stops, no bitmap work needed at all.
//   DEFAULT  — a bundled drawable (DEFAULT_WALLPAPERS) → decode + Palette.
//   CUSTOM   — a user-picked image URI → decode + Palette.
// ─────────────────────────────────────────────────────────

/**
 * Resolves the current wallpaper accent. Safe to call from a coroutine
 * (does bitmap decoding + Palette generation off the main thread); returns
 * [fallback] if anything about the current wallpaper can't be read.
 */
suspend fun extractWallpaperAccent(
    context: Context,
    mode: DesktopWallpaperMode,
    gradientColors: List<Color>,
    imageResId: Int,
    customUri: String,
    fallback: Color
): Color = withContext(Dispatchers.Default) {
    try {
        when (mode) {
            DesktopWallpaperMode.APPARENT ->
                averageColors(gradientColors) ?: fallback

            DesktopWallpaperMode.DEFAULT -> {
                val bitmap = runCatching {
                    BitmapFactory.decodeResource(context.resources, imageResId)
                }.getOrNull()
                bitmap?.let { mutedAccentFrom(it) } ?: fallback
            }

            DesktopWallpaperMode.CUSTOM -> {
                if (customUri.isBlank()) return@withContext fallback
                val bitmap = runCatching {
                    context.contentResolver.openInputStream(Uri.parse(customUri))?.use {
                        BitmapFactory.decodeStream(it)
                    }
                }.getOrNull()
                bitmap?.let { mutedAccentFrom(it) } ?: fallback
            }
        }
    } catch (_: Exception) {
        fallback
    }
}

/** Simple linear-RGB average of a gradient's stops — cheap and good enough
 *  for a background material tint (no need for Palette on coded colors). */
private fun averageColors(colors: List<Color>): Color? {
    if (colors.isEmpty()) return null
    var r = 0f; var g = 0f; var b = 0f
    colors.forEach { r += it.red; g += it.green; b += it.blue }
    val n = colors.size
    return Color(r / n, g / n, b / n)
}

/**
 * Downscales (Palette only needs a small image and full-res decoding of a
 * wallpaper-sized bitmap is wasteful), then prefers a *muted* swatch over a
 * vibrant one — real Mica/Acrylic use a desaturated tone, not the
 * wallpaper's most saturated color, so text/icons drawn over it stay
 * legible and it doesn't turn into a gaudy color wash.
 */
private fun mutedAccentFrom(original: Bitmap): Color {
    val bitmap = if (original.width > 120) {
        val scale = 120f / original.width
        Bitmap.createScaledBitmap(original, 120, (original.height * scale).toInt().coerceAtLeast(1), true)
    } else original

    val palette = Palette.from(bitmap).generate()
    val swatch = palette.mutedSwatch
        ?: palette.darkMutedSwatch
        ?: palette.lightMutedSwatch
        ?: palette.dominantSwatch
        ?: palette.vibrantSwatch

    val raw = swatch?.let { Color(it.rgb) } ?: return Color(0xFF4C63D9) // DS.accentStart

    // Blend halfway toward mid-gray — further tones down anything Palette's
    // "muted" swatch still left too saturated/bright.
    return lerp(raw, Color(0xFF808080), 0.35f)
}
