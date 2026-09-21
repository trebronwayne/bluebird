package io.github.norbertweb.bluebird.ui.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
// FluentIcons.Regular.X is written in code (see usage throughout this file), but — same
// pattern as androidx.compose.material.icons.extended — each icon is a separate
// tree-shakeable extension property that needs its OWN import; a single blanket
// `import ...FluentIcons` is not enough. Package root and per-icon import path below
// per confirmed working example from another file in this project.
import fluent.ui.system.icons.FluentIcons
import fluent.ui.system.icons.regular.Add
import fluent.ui.system.icons.regular.Bug
import fluent.ui.system.icons.regular.Camera
import fluent.ui.system.icons.regular.CameraAdd
import fluent.ui.system.icons.regular.Checkmark
import fluent.ui.system.icons.regular.ChevronDown
import fluent.ui.system.icons.regular.ChevronRight
import fluent.ui.system.icons.regular.DataTrending
import fluent.ui.system.icons.regular.Dismiss
import fluent.ui.system.icons.regular.Edit
import fluent.ui.system.icons.regular.Fingerprint
import fluent.ui.system.icons.regular.Info
import fluent.ui.system.icons.regular.Location
import fluent.ui.system.icons.regular.LockClosedKey
import fluent.ui.system.icons.regular.Mic
import fluent.ui.system.icons.regular.PaintBrush
import fluent.ui.system.icons.regular.Person
import fluent.ui.system.icons.regular.PersonCircle
import fluent.ui.system.icons.regular.Phone
import fluent.ui.system.icons.regular.Search
import fluent.ui.system.icons.regular.Shield

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.github.norbertweb.bluebird.BgAnimationType
import io.github.norbertweb.bluebird.LauncherUiState
import io.github.norbertweb.bluebird.LauncherViewModel
import io.github.norbertweb.bluebird.LiveWallpaperType
import io.github.norbertweb.bluebird.BackgroundEffectsState
// WallpaperTarget import removed with the dead "Home screen"/"Lock screen"
// buttons — see PersonalizationSection for why they were removed.
// Personalization (Background + Effects) now lives in this file instead of its
// own dialog in Desktop.kt — these are the same symbols that file already used,
// exposed here so PersonalizationSection can render identical UI/behavior.
import io.github.norbertweb.bluebird.ui.components.DS
import io.github.norbertweb.bluebird.ui.components.DesktopPreferences
import io.github.norbertweb.bluebird.ui.components.DesktopWallpaperMode
import io.github.norbertweb.bluebird.ui.components.DEFAULT_WALLPAPERS
import io.github.norbertweb.bluebird.ui.components.LiveWallpaperRenderer
import io.github.norbertweb.bluebird.ui.components.bgAnimationEmoji
import io.github.norbertweb.bluebird.ui.components.bgAnimationLabel
import io.github.norbertweb.bluebird.ui.components.wallpaperGradients
import io.github.norbertweb.bluebird.ui.theme.LocalIsDarkTheme
import io.github.norbertweb.bluebird.ui.theme.bluebirdColors
import com.google.accompanist.drawablepainter.DrawablePainter

// ─────────────────────────────────────────────────────────────────────────────
// CATEGORY ENUM — replaces raw strings to prevent silent routing bugs
// ─────────────────────────────────────────────────────────────────────────────

private enum class SettingsCategory(val label: String, val icon: ImageVector) {
    APPEARANCE      ("Appearance",          FluentIcons.Regular.PaintBrush),
    ACCOUNTS        ("Accounts",            FluentIcons.Regular.PersonCircle),
    PRIVACY_SECURITY("Privacy",             FluentIcons.Regular.Shield),
    ABOUT           ("About",               FluentIcons.Regular.Info)
}

// ─────────────────────────────────────────────────────────────────────────────
// ROOT SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SettingsScreen(
    isDark: Boolean, // kept for source compatibility with existing call sites; ignored — see effectiveDark below
    viewModel: LauncherViewModel? = null,
    // Optional deep-link into a specific category, e.g. "APPEARANCE" or
    // "TASKBAR" — matches SettingsCategory enum names. Passed through
    // openWindow(LauncherScreen.SETTINGS, extras = mapOf("category" to "...")).
    // Falls back to SYSTEM (the old default) when null or unrecognized.
    initialCategory: String? = null
) {
    val context  = LocalContext.current
    val uiState  = viewModel?.uiState?.collectAsState()?.value

    // Single source of truth for light/dark (see Theme.kt / LocalIsDarkTheme)
    // — was falling back through uiState?.isDarkTheme, a flag that no longer
    // drives theme anywhere else in the app.
    val effectiveDark  = LocalIsDarkTheme.current
    // Windows 11 "Mica" palette — softer neutrals than pure black/white, with the
    // nav rail one shade off the content pane like the real Settings app. This is
    // now the only palette Settings uses: the old "Special" purple reskin has been
    // removed along with the custom theme picker below, since the launcher now
    // always follows the system's own light/dark + dynamic-color theme (Theme.kt)
    // rather than offering its own set of skins.
    //
    // Re-toned to match Action Center's exact surface language (same neutral
    // #1F1F1F / #F8F8F8 fill, same bluebirdColors accent) instead of the old
    // bespoke slate/indigo "Mica" palette — Settings and Action Center should
    // read as one consistent design language, not two separately-skinned
    // surfaces. Nav rail stays one shade off content, same three-tone Mica
    // rhythm as before, just recolored to this neutral family.
    val textColor      = if (effectiveDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    val bgColor        = if (effectiveDark) Color(0xFF1F1F1F)          else Color(0xFFF8F8F8)
    val surfaceBg      = if (effectiveDark) Color(0xFF2A2A2A)          else Color(0xFFFFFFFF)
    val navBg          = if (effectiveDark) Color(0xFF242424)          else Color(0xFFEFEFEF)

    val resolvedBg      = bgColor
    val resolvedNav     = navBg
    val resolvedSurface = surfaceBg
    val resolvedText    = textColor
    val specialAccent   = bluebirdColors.AccentBlue

    // Lets callers deep-link straight into a category — e.g. Desktop.kt's
    // right-click "Personalize" now opens Settings at APPEARANCE instead of
    // showing its own dialog, and Taskbar.kt's settings gear opens TASKBAR.
    val selectedCategoryState = remember {
        mutableStateOf<SettingsCategory>(
            initialCategory
                ?.let { name -> SettingsCategory.entries.find { it.name == name } }
                ?: DesktopPreferences(context).consumeSettingsStartCategory()
                    ?.let { name -> SettingsCategory.entries.find { it.name == name } }
                ?: SettingsCategory.APPEARANCE
        )
    }
    var navSearch by remember { mutableStateOf("") }
    val visibleCategories = remember(navSearch) {
        if (navSearch.isBlank()) SettingsCategory.entries.toList()
        else SettingsCategory.entries.filter { it.label.contains(navSearch, ignoreCase = true) }
    }

    Row(modifier = Modifier.fillMaxSize().background(resolvedBg)) {

        // ── Left nav pane ─────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .width(256.dp)
                .fillMaxHeight()
                .background(resolvedNav)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            if (uiState != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { selectedCategoryState.value = SettingsCategory.ACCOUNTS }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.size(40.dp)) {
                        if (uiState.userProfile.profilePicturePath.isNotEmpty()) {
                            // FIX: same Crop fix as the Accounts screen avatar — without it,
                            // non-square photos are letterboxed (Fit) into a thin vertical strip.
                            AsyncImage(
                                model = Uri.parse(uiState.userProfile.profilePicturePath),
                                contentDescription = "Profile picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize().background(specialAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(FluentIcons.Regular.Person, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(uiState.userProfile.userName,
                            style      = MaterialTheme.typography.titleSmall,
                            color      = resolvedText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = (13 * uiState.textScale).sp,
                            maxLines   = 1, overflow = TextOverflow.Ellipsis)
                        Text("Local Account",
                            style    = MaterialTheme.typography.labelSmall,
                            color    = resolvedText.copy(alpha = 0.5f))
                    }
                }
                Spacer(Modifier.height(8.dp))

                // Windows 11 style "Find a setting" search box
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(resolvedBg)
                        .border(1.dp, resolvedText.copy(alpha = 0.1f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Icon(FluentIcons.Regular.Search, null, tint = resolvedText.copy(alpha = 0.45f), modifier = Modifier.size(16.dp))
                    BasicTextField(
                        value = navSearch,
                        onValueChange = { navSearch = it },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = resolvedText, fontSize = (13 * (uiState?.textScale ?: 1f)).sp),
                        modifier = Modifier.weight(1f),
                        // FIX: decorationBox's content isn't auto-wrapped in any layout — the
                        // placeholder Text and inner() were being emitted as bare siblings with
                        // no shared alignment, which is exactly what made the caret render off
                        // from the placeholder/typed text instead of sitting centered on it.
                        // Same root cause as the earlier FileExplorer/MediaPlayer search-box fix,
                        // different code shape (decorationBox vs. an unwrapped Box).
                        decorationBox = { inner ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (navSearch.isEmpty()) Text("Find a setting", color = resolvedText.copy(alpha = 0.4f), fontSize = (13 * (uiState?.textScale ?: 1f)).sp)
                                inner()
                            }
                        }
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(visibleCategories, key = { it.name }) { cat ->
                val isSelected = selectedCategoryState.value == cat
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) specialAccent.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { selectedCategoryState.value = cat }
                        .padding(horizontal = 10.dp, vertical = 11.dp),
                    verticalAlignment  = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Selected-item accent bar, matching the real Settings app's nav rail
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(if (isSelected) 16.dp else 0.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isSelected) specialAccent else Color.Transparent)
                    )
                    Icon(cat.icon, null,
                        tint     = if (isSelected) specialAccent else resolvedText.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp))
                    Text(cat.label,
                        color      = if (isSelected) specialAccent else resolvedText,
                        fontSize   = (13 * (uiState?.textScale ?: 1f)).sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        maxLines   = 1, overflow = TextOverflow.Ellipsis)
                }
                }
            }
        }

        // ── Right content area ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(resolvedBg)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(selectedCategoryState.value.icon, null, tint = resolvedText, modifier = Modifier.size(24.dp))
                Text(selectedCategoryState.value.label,
                    style      = MaterialTheme.typography.headlineSmall,
                    color      = resolvedText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = (24 * (uiState?.textScale ?: 1f)).sp)
            }
            Spacer(Modifier.height(2.dp))

            val args = ScreenArgs(effectiveDark, resolvedText, resolvedSurface, specialAccent, uiState, viewModel, context,
                scale = uiState?.textScale ?: 1f)

            when (selectedCategoryState.value) {
                SettingsCategory.APPEARANCE       -> AppearanceSettings(args)
                SettingsCategory.ACCOUNTS         -> AccountsSettings(args)
                SettingsCategory.PRIVACY_SECURITY -> PrivacySecuritySettings(args)
                SettingsCategory.ABOUT            -> AboutSettings(args)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SHARED ARGS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * `FluentIcons.Regular.X` / `FluentIcons.Filled.X` (from io.github.niyajali:fluentui-system-icons)
 * are plain top-level ImageVector vals — same shape as the old `Icons.Outlined.X` —
 * so every existing `icon: ImageVector` parameter thr
