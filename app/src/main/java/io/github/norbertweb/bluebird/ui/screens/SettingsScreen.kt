package io.github.norbertweb.bluebird.ui.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import fluent.ui.system.icons.regular.CameraAdd
import fluent.ui.system.icons.regular.Checkmark
import fluent.ui.system.icons.regular.ChevronDown
import fluent.ui.system.icons.regular.ChevronRight
import fluent.ui.system.icons.regular.DataTrending
import fluent.ui.system.icons.regular.Dismiss
import fluent.ui.system.icons.regular.Edit
import fluent.ui.system.icons.regular.Info
import fluent.ui.system.icons.regular.PaintBrush
import fluent.ui.system.icons.regular.Person
import fluent.ui.system.icons.regular.PersonCircle
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

// ─────────────────────────────────────────────────────────────────────────────
// CATEGORY ENUM — replaces raw strings to prevent silent routing bugs
// ─────────────────────────────────────────────────────────────────────────────

private enum class SettingsCategory(val label: String, val icon: ImageVector) {
    APPEARANCE      ("Appearance",          FluentIcons.Regular.PaintBrush),
    ACCOUNTS        ("Accounts",            FluentIcons.Regular.PersonCircle),
    PRIVACY_SECURITY("Privacy & security",  FluentIcons.Regular.Shield),
    FEEDBACK_UPDATES("Feedback & updates",  FluentIcons.Regular.Bug),
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

    // ── Auto-collapsing nav rail ────────────────────────────────────────────
    // The nav pane shrinks to an icon-only rail 3s after the user stops
    // touching it, freeing width for the content pane. It expands instantly
    // on any interaction (tap, search typing, or scrolling the category
    // list) and the collapse timer doesn't run at all while mid-scroll.
    val navListState = rememberLazyListState()
    var navInteractionTick by remember { mutableStateOf(0) }
    var isNavExpanded by remember { mutableStateOf(true) }
    // Bump this from any tap/typing in the nav pane to keep it open and
    // restart the 3s countdown.
    val pokeNav: () -> Unit = { navInteractionTick++ }

    LaunchedEffect(navInteractionTick, navListState.isScrollInProgress) {
        isNavExpanded = true
        if (!navListState.isScrollInProgress) {
            delay(3000)
            isNavExpanded = false
        }
    }
    val navWidth by animateDpAsState(
        targetValue = if (isNavExpanded) 256.dp else 64.dp,
        animationSpec = tween(260),
        label = "navWidth"
    )

    Row(modifier = Modifier.fillMaxSize().background(resolvedBg)) {

        // ── Left nav pane ─────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .width(navWidth)
                .fillMaxHeight()
                .background(resolvedNav)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { pokeNav() }
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            if (uiState != null && isNavExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { pokeNav(); selectedCategoryState.value = SettingsCategory.ACCOUNTS }
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
                        onValueChange = { navSearch = it; pokeNav() },
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

            LazyColumn(state = navListState, modifier = Modifier.weight(1f)) {
                items(visibleCategories, key = { it.name }) { cat ->
                val isSelected = selectedCategoryState.value == cat
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) specialAccent.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { pokeNav(); selectedCategoryState.value = cat }
                        .padding(horizontal = if (isNavExpanded) 10.dp else 0.dp, vertical = 11.dp),
                    verticalAlignment  = Alignment.CenterVertically,
                    horizontalArrangement = if (isNavExpanded) Arrangement.spacedBy(12.dp) else Arrangement.Center
                ) {
                    // Selected-item accent bar, matching the real Settings app's nav rail
                    // — only shown in the expanded state; the collapsed icon-only rail
                    // uses the icon's own tint to indicate selection instead.
                    if (isNavExpanded) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(if (isSelected) 16.dp else 0.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isSelected) specialAccent else Color.Transparent)
                        )
                    }
                    Icon(cat.icon, null,
                        tint     = if (isSelected) specialAccent else resolvedText.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp))
                    if (isNavExpanded) {
                        Text(cat.label,
                            color      = if (isSelected) specialAccent else resolvedText,
                            fontSize   = (13 * (uiState?.textScale ?: 1f)).sp,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            maxLines   = 1, overflow = TextOverflow.Ellipsis)
                    }
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
                SettingsCategory.FEEDBACK_UPDATES -> FeedbackUpdatesSettings(args)
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
 * so every existing `icon: ImageVector` parameter throughout this file keeps working
 * unchanged, and there's no @Composable/resource-loading indirection needed (unlike
 * the drawable-resource approach this replaced).
 *
 * NOTE: the exact names below were derived by hand from Fluent's icon-slug naming
 * convention and were not verified against a compiler — if Android Studio underlines
 * one in red, autocomplete on `FluentIcons.Regular.` to find the closest real name;
 * it's a one-line fix wherever it appears. The import path is also a best guess —
 * adjust it to whatever your IDE resolves the library's actual package to.
 */

internal data class ScreenArgs(
    val isDark    : Boolean,
    val textColor : Color,
    val surfaceBg : Color,
    val accent    : Color,
    val uiState   : LauncherUiState?,
    val vm        : LauncherViewModel?,
    val ctx       : Context,
    // Computed once per screen instead of re-deriving `uiState?.textScale ?: 1f`
    // (a null-check + boxing) at every one of the ~150 call sites that need it.
    val scale     : Float = uiState?.textScale ?: 1f
)

// ─────────────────────────────────────────────────────────────────────────────
// REUSABLE COMPONENTS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun SettingsGroup(title: String, a: ScreenArgs, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title,
            color      = a.textColor.copy(alpha = 0.65f),
            fontSize   = (12 * (a.scale)).sp,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.padding(start = 2.dp, bottom = 2.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = a.surfaceBg),
            shape  = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, a.textColor.copy(alpha = 0.08f))
        ) { Column(modifier = Modifier.padding(2.dp), content = content) }
    }
}

// BUG FIX: entire row is now clickable, not just the Switch thumb
@Composable
internal fun SToggle(
    icon     : ImageVector,
    label    : String,
    sub      : String = "",
    a        : ScreenArgs,
    checked  : Boolean,
    onToggle : () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable { onToggle() }           // ← FIX: whole row tappable
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment  = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, null, tint = a.textColor.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = a.textColor, fontSize = (14 * (a.scale)).sp)
            if (sub.isNotEmpty()) Text(sub, color = a.textColor.copy(alpha = 0.55f), fontSize = (12 * (a.scale)).sp)
        }
        Switch(
            checked         = checked,
            onCheckedChange = { onToggle() },
            colors          = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = a.accent)
        )
    }
}

@Composable
internal fun SNav(
    icon    : ImageVector,
    label   : String,
    sub     : String = "",
    value   : String = "",
    a       : ScreenArgs,
    onClick : () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment  = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, null, tint = a.textColor.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = a.textColor, fontSize = (14 * (a.scale)).sp)
            if (sub.isNotEmpty()) Text(sub, color = a.textColor.copy(alpha = 0.55f), fontSize = (12 * (a.scale)).sp)
        }
        if (value.isNotEmpty()) Text(value, color = a.textColor.copy(alpha = 0.5f), fontSize = (12 * (a.scale)).sp)
        Icon(FluentIcons.Regular.ChevronRight, null, tint = a.textColor.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
    }
}

@Composable
internal fun SSlider(
    icon          : ImageVector,
    label         : String,
    sub           : String = "",
    value         : Float,
    range         : ClosedFloatingPointRange<Float> = 0f..1f,
    valueLabel    : String = "",
    a             : ScreenArgs,
    onValueChange : (Float) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Icon(icon, null, tint = a.textColor.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = a.textColor, fontSize = (14 * (a.scale)).sp)
                if (sub.isNotEmpty()) Text(sub, color = a.textColor.copy(alpha = 0.55f), fontSize = (12 * (a.scale)).sp)
            }
            Text(valueLabel, color = a.textColor.copy(alpha = 0.6f), fontSize = (12 * (a.scale)).sp)
        }
        Slider(
            value         = value,
            onValueChange = onValueChange,
            valueRange    = range,
            colors        = SliderDefaults.colors(thumbColor = a.accent, activeTrackColor = a.accent),
            modifier      = Modifier.padding(start = 32.dp)
        )
    }
}

@Composable
internal fun SDropdown(
    icon     : ImageVector,
    label    : String,
    sub      : String = "",
    options  : List<String>,
    selected : String,
    a        : ScreenArgs,
    onSelect : (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable { expanded = true }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment  = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, null, tint = a.textColor.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = a.textColor, fontSize = (14 * (a.scale)).sp)
            if (sub.isNotEmpty()) Text(sub, color = a.textColor.copy(alpha = 0.55f), fontSize = (12 * (a.scale)).sp)
        }
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, a.textColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                    .background(a.textColor.copy(alpha = 0.04f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(selected, color = a.textColor, fontSize = (12 * (a.scale)).sp)
                Spacer(Modifier.width(4.dp))
                Icon(FluentIcons.Regular.ChevronDown, null, tint = a.textColor.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text        = { Text(option, fontSize = (13 * (a.scale)).sp) },
                        onClick     = { onSelect(option); expanded = false },
                        leadingIcon = if (option == selected) ({
                            Icon(FluentIcons.Regular.Checkmark, null, tint = a.accent, modifier = Modifier.size(14.dp))
                        }) else null
                    )
                }
            }
        }
    }
}

internal fun divColor(a: ScreenArgs) = a.textColor.copy(alpha = 0.06f)

// ─────────────────────────────────────────────────────────────────────────────
// Personalization — ported from Desktop.kt's WallpaperPersonalisePanel
//
// This used to be its own AlertDialog, opened only via right-click →
// Personalize on the desktop. It's now an ordinary Settings section
// (Background + Effects, kept as sub-tabs since they're genuinely different
// concerns) so personalization lives in one place like Windows 11's
// Settings > Personalization, instead of a separate floating dialog most
// users would never think to look in Settings for.
//
// Reuses the exact same state shape and ViewModel calls as the original
// panel (DesktopPreferences, DesktopWallpaperMode, BgAnimationType,
// LiveWallpaperType, wallpaperGradients, LiveWallpaperRenderer,
// bgAnimationLabel/Emoji) so both entry points stay perfectly in sync —
// there's only one copy of this logic now, just a new place to reach it.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PersonalizationSection(a: ScreenArgs) {
    val tc  = a.textColor
    val tcm = a.textColor.copy(alpha = 0.6f)
    val acc = a.accent
    val scale = a.scale

    val prefs = remember { DesktopPreferences(a.ctx) }
    var wallpaperMode by remember { mutableStateOf(prefs.wallpaperMode) }
    var gradientIdx   by remember { mutableStateOf(prefs.wallpaperGradientIndex) }
    var imageIdx      by remember { mutableStateOf(prefs.wallpaperImageIndex) }
    // Which bundled wallpapers are selected (multi-select). More than one
    // selected means Desktop.kt rotates through them automatically every hour —
    // a fixed interval, not something the user can change.
    var imageSelection by remember { mutableStateOf(prefs.defaultWallpaperSelection) }
    // Every custom image the user has picked from the gallery, plus which one
    // is currently active — lets Settings show "which wallpapers already
    // exist" and switch between them, instead of only ever showing one with
    // no way to tell what it is or pick a previous one back.
    var customHistory by remember { mutableStateOf(prefs.customWallpaperHistory) }
    var activeCustomUri by remember { mutableStateOf(prefs.customWallpaperUri) }

    val gradientNames = remember {
        listOf("Ocean Depth", "Midnight Blue", "Carbon", "Sunset Tricolor", "Forest Lime")
    }

    var activeTab by remember { mutableStateOf(0) } // 0 = Background, 1 = Effects
    val effectsState = a.uiState
    val bgEffects = effectsState?.backgroundEffects ?: BackgroundEffectsState()

    // FIX: GetContent() opens the device's photo picker / gallery app directly,
    // instead of OpenDocument()'s generic Storage Access Framework browser which
    // lets people navigate the whole filesystem. "Browse Gallery" should actually
    // open the gallery.
    //
    // GetContent() (unlike OpenDocument()) doesn't reliably support persistable
    // URI permission grants, so a raw content:// URI can stop resolving after
    // the app restarts. The picked image is copied into app-private storage
    // immediately — the same pattern LauncherViewModel already uses for the
    // profile picture and the Home/Lock screen wallpaper pickers — so the
    // custom wallpaper keeps working across restarts regardless of grant support.
    //
    // FIX: the destination used a single fixed filename ("desktop_custom_wallpaper.jpg"),
    // so every new pick silently overwrote the last one — there was never any way to
    // build a history or tell which images had already been chosen. Each pick now
    // gets its own timestamped file and is APPENDED to customWallpaperHistory rather
    // than replacing it, so past choices stay picked and selectable as tiles below.
    val scope = rememberCoroutineScope()
    val customWallpaperLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val destFile = File(a.ctx.filesDir, "desktop_custom_wallpaper_${System.currentTimeMillis()}.jpg")
                    a.ctx.contentResolver.openInputStream(uri)?.use { input ->
                        destFile.outputStream().use { output -> input.copyTo(output) }
                    }
                    val persistedUri = Uri.fromFile(destFile).toString()
                    withContext(Dispatchers.Main) {
                        val updatedHistory = prefs.customWallpaperHistory + persistedUri
                        prefs.customWallpaperHistory = updatedHistory
                        prefs.customWallpaperUri = persistedUri
                        prefs.wallpaperMode = DesktopWallpaperMode.CUSTOM
                        prefs.wallpaperModeEverSet = true
                        customHistory = updatedHistory
                        activeCustomUri = persistedUri
                        wallpaperMode = DesktopWallpaperMode.CUSTOM
                    }
                } catch (_: Exception) { /* keep previous wallpaper on failure */ }
            }
        }
    }

    val pickCustomWallpaper = {
        customWallpaperLauncher.launch("image/*")
    }

    // Switch the active custom wallpaper to a previously-picked one, without
    // opening the gallery again.
    val selectCustomWallpaper: (String) -> Unit = { uriString ->
        prefs.customWallpaperUri = uriString
        activeCustomUri = uriString
    }

    // Remove a picked image from history (and its backing file). If it was the
    // active one, fall back to the most recent remaining entry.
    val removeCustomWallpaper: (String) -> Unit = { uriString ->
        val updatedHistory = customHistory - uriString
        prefs.customWallpaperHistory = updatedHistory
        customHistory = updatedHistory
        runCatching { Uri.parse(uriString).path?.let { File(it).delete() } }
        if (activeCustomUri == uriString) {
            val fallback = updatedHistory.lastOrNull() ?: ""
            prefs.customWallpaperUri = fallback
            activeCustomUri = fallback
        }
    }

    SettingsGroup("Personalization", a) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ── Sub-tabs ──
            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Background" to 0, "Effects" to 1).forEach { (label, idx) ->
                    val selected = activeTab == idx
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(6.dp),
                        color    = if (selected) acc.copy(alpha = 0.15f) else Color.Transparent
                    ) {
                        Box(
                            Modifier.clickable { activeTab = idx }.padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label, fontSize = (12 * scale).sp,
                                color = if (selected) acc else tcm,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            if (activeTab == 0) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ── Mode selector ──
                    Text("Wallpaper type", color = tcm, fontSize = (11 * scale).sp, fontWeight = FontWeight.Medium)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            DesktopWallpaperMode.APPARENT to "Apparent",
                            DesktopWallpaperMode.DEFAULT  to "Default",
                            DesktopWallpaperMode.CUSTOM   to "Custom"
                        ).forEach { (mode, label) ->
                            val selected = wallpaperMode == mode
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape    = RoundedCornerShape(6.dp),
                                color    = if (selected) acc else a.textColor.copy(alpha = 0.06f),
                                border   = if (selected) null else BorderStroke(1.dp, a.textColor.copy(alpha = 0.15f))
                            ) {
                                Box(
                                    Modifier
                                        .clickable {
                                            wallpaperMode = mode
                                            prefs.wallpaperMode = mode
                                            prefs.wallpaperModeEverSet = true
                                            // Matches the original panel: switching away from
                                            // Custom clears the stored custom image so a stale
                                            // URI doesn't linger once the user picks something else.
                                            if (mode != DesktopWallpaperMode.CUSTOM) {
                                                prefs.customWallpaperUri = ""
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        label,
                                        color      = if (selected) Color.White else tc,
                                        fontSize   = (12 * scale).sp,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                        textAlign  = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // ── Apparent gradient picker — shown/hidden instantly ──
                    if (wallpaperMode == DesktopWallpaperMode.APPARENT) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Colour scheme", color = tcm, fontSize = (11 * scale).sp, fontWeight = FontWeight.Medium)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                wallpaperGradients.forEachIndexed { idx, gradient ->
                                    val isActive = idx == gradientIdx
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isActive) acc.copy(alpha = 0.12f) else Color.Transparent)
                                            .clickable { gradientIdx = idx; prefs.wallpaperGradientIndex = idx }
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            Modifier
                                                .size(36.dp, 22.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Brush.linearGradient(gradient, start = Offset(0f, 0f), end = Offset(200f, 100f)))
                                        )
                                        Text(
                                            gradientNames.getOrElse(idx) { "Preset ${idx + 1}" },
                                            color = tc, fontSize = (12.5 * scale).sp, modifier = Modifier.weight(1f)
                                        )
                                        if (isActive) {
                                            Icon(FluentIcons.Regular.Checkmark, null, tint = acc, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── Default image picker — shown/hidden instantly ──
                    if (wallpaperMode == DesktopWallpaperMode.DEFAULT) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Wallpaper image", color = tcm, fontSize = (11 * scale).sp, fontWeight = FontWeight.Medium)
                            Text(
                                "Select one or more. With more than one selected, they rotate automatically every hour.",
                                color = tcm.copy(alpha = 0.7f), fontSize = (10 * scale).sp
                            )
                            if (DEFAULT_WALLPAPERS.all { it == 0 }) {
                                Box(
                                    Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(8.dp))
                                        .background(a.textColor.copy(alpha = 0.06f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Add wallpapers to res/drawable\nas desktop_wp_1.png, desktop_wp_2.png, …",
                                        color = tcm, fontSize = (11 * scale).sp, textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Row(
                                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    DEFAULT_WALLPAPERS.forEachIndexed { idx, resId ->
                                        val isSelected = idx in imageSelection
                                        Box(
                                            Modifier
                                                .size(72.dp, 48.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .border(
                                                    width = if (isSelected) 2.dp else 0.dp,
                                                    color = if (isSelected) acc else Color.Transparent,
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .clickable {
                                                    // Toggle membership, but never allow the last
                                                    // selection to be removed — DEFAULT mode always
                                                    // needs at least one wallpaper to show.
                                                    val updated = if (isSelected) {
                                                        if (imageSelection.size > 1) imageSelection - idx else imageSelection
                                                    } else {
                                                        imageSelection + idx
                                                    }
                                                    imageSelection = updated
                                                    prefs.defaultWallpaperSelection = updated
                                                    // Keep the single "current" index valid and pointed
                                                    // at a selected wallpaper so DEFAULT mode always has
                                                    // something to render even before the next rotation.
                                                    if (imageIdx !in updated) {
                                                        val first = updated.minOrNull() ?: 0
                                                        imageIdx = first
                                                        prefs.wallpaperImageIndex = first
                                                    }
                                                }
                                        ) {
                                            if (resId != 0) {
                                                Image(
                                                    painter = painterResource(id = resId),
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(Modifier.fillMaxSize().background(Color(0xFF1A1A2E)))
                                            }
                                            if (isSelected) {
                                                Box(
                                                    Modifier.fillMaxSize().background(acc.copy(alpha = 0.25f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(FluentIcons.Regular.Checkmark, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── Custom wallpaper — shown/hidden instantly ──
                    if (wallpaperMode == DesktopWallpaperMode.CUSTOM) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Your wallpapers", color = tc, fontSize = (11 * scale).sp, fontWeight = FontWeight.Medium)
                            Text(
                                "Tap one to use it, or add another from your gallery.",
                                color = tcm, fontSize = (10 * scale).sp
                            )
                            Row(
                                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                customHistory.forEach { uriString ->
                                    val isActive = uriString == activeCustomUri
                                    Box(
                                        Modifier
                                            .size(72.dp, 48.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(
                                                width = if (isActive) 2.dp else 0.dp,
                                                color = if (isActive) acc else Color.Transparent,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .clickable { selectCustomWallpaper(uriString) }
                                    ) {
                                        AsyncImage(
                                            model = Uri.parse(uriString),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        if (isActive) {
                                            Box(
                                                Modifier.fillMaxSize().background(acc.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(FluentIcons.Regular.Checkmark, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        // Delete affordance — only when more than a single
                                        // wallpaper exists, so Custom mode always has one to fall
                                        // back on rather than being left with nothing to show.
                                        if (customHistory.size > 1) {
                                            Box(
                                                Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(2.dp)
                                                    .size(16.dp)
                                                    .clip(RoundedCornerShape(50))
                                                    .background(Color.Black.copy(alpha = 0.55f))
                                                    .clickable { removeCustomWallpaper(uriString) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(FluentIcons.Regular.Dismiss, null, tint = Color.White, modifier = Modifier.size(10.dp))
                                            }
                                        }
                                    }
                                }
                                Box(
                                    Modifier
                                        .size(72.dp, 48.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(1.dp, tcm, RoundedCornerShape(6.dp))
                                        .clickable { pickCustomWallpaper() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(FluentIcons.Regular.Add, null, tint = tcm, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    Divider(color = tcm.copy(alpha = 0.15f))

                    // The old "Wallpaper slideshow" toggle + "Change every" interval
                    // dropdown only persisted flags that nothing ever read — no timer
                    // actually rotated the wallpaper, so the switch did nothing. Real
                    // automatic rotation now happens whenever more than one default
                    // wallpaper is selected above, on a fixed 1-hour interval that
                    // isn't user-configurable, so there's nothing to toggle here.
                    //
                    // The "Home screen" / "Lock screen" buttons that used to sit here
                    // called vm.openWallpaperPicker(...), which drove a second, entirely
                    // separate wallpaper system (WallpaperState / setBuiltInWallpaper /
                    // setCustomWallpaper) that never talked to the actual Bluebird desktop
                    // wallpaper this whole section configures (DesktopPreferences). It
                    // opened its own picker with its own bundled colours/images, so picking
                    // something there had no visible effect on the desktop and just
                    // duplicated (and conflicted with) the wallpaper controls above.
                    // Removed rather than left as a second, disconnected wallpaper picker.
                }
            } else {
                // ── Effects tab: particle animations (mix-able) + live wallpapers ──
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Background animation", color = tcm, fontSize = (11 * scale).sp, fontWeight = FontWeight.Medium)
                        Text("Select one or more to mix them together", color = tcm.copy(alpha = 0.7f), fontSize = (10 * scale).sp)
                    }

                    val liveActive = bgEffects.liveWallpaper != LiveWallpaperType.NONE
                    BgAnimationType.entries.toList().chunked(2).forEach { rowTypes ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowTypes.forEach { type ->
                                val active = type in bgEffects.activeAnimations
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape    = RoundedCornerShape(6.dp),
                                    color    = if (active) acc else a.textColor.copy(alpha = 0.06f),
                                    border   = if (active) null else BorderStroke(1.dp, a.textColor.copy(alpha = 0.15f))
                                ) {
                                    Row(
                                        Modifier
                                            .clickable(enabled = !liveActive) { a.vm?.toggleBgAnimation(type) }
                                            .padding(vertical = 8.dp, horizontal = 8.dp)
                                            .alpha(if (liveActive) 0.4f else 1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(bgAnimationEmoji(type), fontSize = (14 * scale).sp)
                                        Text(
                                            bgAnimationLabel(type), fontSize = (11.5 * scale).sp,
                                            color = if (active) Color.White else tc,
                                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                            if (rowTypes.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }

                    if (bgEffects.activeAnimations.isNotEmpty()) {
                        TextButton(onClick = { a.vm?.clearBgAnimations() }) {
                            Text("Turn off all animations", fontSize = (11 * scale).sp, color = acc)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Intensity", color = tcm, fontSize = (11 * scale).sp, fontWeight = FontWeight.Medium)
                        Slider(
                            value = bgEffects.intensity.toFloat(),
                            onValueChange = { a.vm?.setBgAnimationIntensity(it.toInt()) },
                            valueRange = 10f..100f,
                            enabled = !liveActive,
                            colors = SliderDefaults.colors(thumbColor = acc, activeTrackColor = acc)
                        )
                    }

                    Divider(color = tcm.copy(alpha = 0.15f))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Live wallpaper", color = tcm, fontSize = (11 * scale).sp, fontWeight = FontWeight.Medium)
                        Text(
                            "Replaces the static wallpaper and turns off particle animations",
                            color = tcm.copy(alpha = 0.7f), fontSize = (10 * scale).sp
                        )
                    }
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val offActive = bgEffects.liveWallpaper == LiveWallpaperType.NONE
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                Modifier
                                    .size(72.dp, 48.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(a.textColor.copy(alpha = 0.06f))
                                    .border(
                                        width = if (offActive) 2.dp else 0.dp,
                                        color = if (offActive) acc else Color.Transparent,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { a.vm?.setLiveWallpaper(LiveWallpaperType.NONE) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(FluentIcons.Regular.Dismiss, null, tint = tcm, modifier = Modifier.size(16.dp))
                            }
                            Text("Off", fontSize = (10 * scale).sp, color = if (offActive) acc else tcm)
                        }
                        listOf(
                            LiveWallpaperType.AURORA to "Aurora",
                            LiveWallpaperType.NEBULA to "Nebula",
                            LiveWallpaperType.WAVES  to "Waves",
                            LiveWallpaperType.BOKEH  to "Bokeh"
                        ).forEach { (lw, label) ->
                            val isActive = bgEffects.liveWallpaper == lw
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    Modifier
                                        .size(72.dp, 48.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(
                                            width = if (isActive) 2.dp else 0.dp,
                                            color = if (isActive) acc else Color.Transparent,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable { a.vm?.setLiveWallpaper(lw) }
                                ) {
                                    LiveWallpaperRenderer(type = lw, modifier = Modifier.fillMaxSize(), animated = false)
                                    if (isActive) {
                                        Box(
                                            Modifier.fillMaxSize().background(acc.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(FluentIcons.Regular.Checkmark, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                Text(label, fontSize = (10 * scale).sp, color = if (isActive) acc else tcm)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// APPEARANCE
//
// Trimmed to what's actually real: wallpaper personalization and the system
// font picker. The old Accent color / Text size / Icon size / Shape & corners
// / Visual effects / Layout groups below this were never wired to anything
// that rendered differently elsewhere in the app — removed rather than left
// as controls that silently do nothing.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AppearanceSettings(a: ScreenArgs) {

    // ── Personalization ─────────────────────────────────────────────────────
    // Everything that used to live in Desktop.kt's own right-click "Personalise"
    // dialog (WallpaperPersonalisePanel) now lives here instead, laid out as a
    // normal Settings section rather than a popup dialog — matching how Windows
    // 11 folds Personalization straight into Settings rather than giving it its
    // own separate surface. Right-click → Personalize on the desktop now opens
    // Settings directly to this category (see Desktop.kt's onPersonalize).
    PersonalizationSection(a)

    // "System font" removed — launcherFont was only ever written back to itself
    // (persisted, exported, imported, reset) but never read anywhere to actually
    // change any rendered font, so the dropdown had no visible effect.
}

// ─────────────────────────────────────────────────────────────────────────────
// ACCOUNTS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AccountsSettings(a: ScreenArgs) {
    val scale = a.scale
    // FIX: "Change username" opened a full AlertDialog popup for what's a
    // one-line edit — replaced with an inline text field that swaps in for
    // the row itself, matching how the rest of Settings edits values in place
    // (e.g. the rename-in-place pattern desktop icons already use) instead of
    // interrupting with a modal.
    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    val nameFocusRequester = remember { FocusRequester() }

    // FIX: "Change profile picture" previously only called
    // vm.openProfilePicturePicker(), which flips a one-shot StateFlow that
    // nothing observed or acted on — no picker was ever launched, so tapping
    // the row did nothing and the picture never changed. GetContent() opens
    // the system photo picker directly from here (rememberLauncherForActivityResult
    // must be called from a composable, so it lives in this screen), and its
    // result is handed straight to the already-working vm.setProfilePicture(),
    // which copies the image into app storage and persists the path.
    val profilePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            a.vm?.setProfilePicture(a.ctx, uri)
        }
    }

    SettingsGroup("Your account", a) {
        if (a.uiState != null && a.vm != null) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(56.dp)) {
                    if (a.uiState.userProfile.profilePicturePath.isNotEmpty()) {
                        // FIX: ContentScale.Crop so a portrait (or any non-square) photo
                        // fills the circular frame instead of being letterboxed inside it
                        // (Coil's default Fit scaling), which is what made pictures look
                        // squeezed into a thin vertical strip.
                        AsyncImage(model = Uri.parse(a.uiState.userProfile.profilePicturePath),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape))
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(a.accent, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(FluentIcons.Regular.Person, null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(a.uiState.userProfile.userName, color = a.textColor, fontSize = (16 * scale).sp, fontWeight = FontWeight.Medium)
                    Text("Local Account", color = a.textColor.copy(alpha = 0.5f), fontSize = (12 * scale).sp)
                }
            }
            Divider(color = divColor(a), modifier = Modifier.padding(horizontal = 12.dp))

            if (isEditingName) {
                LaunchedEffect(Unit) {
                    delay(80)
                    try { nameFocusRequester.requestFocus() } catch (_: Exception) {}
                }
                fun commit() {
                    val trimmed = editedName.trim()
                    if (trimmed.isNotEmpty()) a.vm.setUserName(trimmed)
                    isEditingName = false
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(FluentIcons.Regular.Edit, null, tint = a.textColor.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        singleLine = true,
                        placeholder = { Text("Display name") },
                        colors = OutlinedTextFieldDefaults.colors(),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(nameFocusRequester)
                            .onKeyEvent { event ->
                                if (event.type == KeyEventType.KeyUp && event.key == Key.Enter) {
                                    commit(); true
                                } else false
                            }
                    )
                    TextButton(enabled = editedName.isNotBlank(), onClick = { commit() }) { Text("Save") }
                    TextButton(onClick = { isEditingName = false }) { Text("Cancel") }
                }
            } else {
                SNav(FluentIcons.Regular.Edit, "Change username", "Update your display name", a = a) {
                    editedName = a.uiState.userProfile.userName
                    isEditingName = true
                }
            }

            Divider(color = divColor(a), modifier = Modifier.padding(horizontal = 12.dp))
            SNav(FluentIcons.Regular.CameraAdd, "Change profile picture", "Pick a photo from your gallery",   a = a) {
                profilePictureLauncher.launch("image/*")
            }
        }
    }

    // "Sign-in options" and "Linked accounts" removed — none of it was backed
    // by real app state. PIN/Fingerprint redirected to Android's own security
    // settings, and Screen pattern/Password/Face unlock/Microsoft account had
    // no onClick at all — tapping them did nothing.
}

// ─────────────────────────────────────────────────────────────────────────────
// PRIVACY & SECURITY
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PrivacySecuritySettings(a: ScreenArgs) {
    // "Device security" (Screen lock, Biometric unlock, Install unknown apps)
    // and "App permissions" (Location, Camera, Microphone access) groups
    // removed — none of these toggles were backed by real enforcement; they
    // just flipped a stored boolean that nothing else in the app read.
    SettingsGroup("Data & diagnostics", a) {
        SToggle(FluentIcons.Regular.DataTrending, "Usage & diagnostics",
            "Send anonymous crash reports and usage data",
            a, a.uiState?.usageDiagnostics ?: true) { a.vm?.setUsageDiagnostics(!(a.uiState?.usageDiagnostics ?: true)) }
    }

    // "Privacy dashboard" group removed — both rows (View permission usage,
    // Clear app data) only opened Android's own settings screens rather than
    // anything Bluebird itself manages.
}

// ─────────────────────────────────────────────────────────────────────────────
// FEEDBACK & UPDATES
// ─────────────────────────────────────────────────────────────────────────────

// Small pill-shaped status indicator used by the feedback card — replaces
// the old plain red/green text with a subtle icon + colored chip.
@Composable
private fun AnimatedStatusChip(result: Boolean?, scale: Float) {
    AnimatedVisibility(visible = result != null) {
        val isSuccess = result == true
        val tint = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFE57373)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(tint.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                if (isSuccess) FluentIcons.Regular.Checkmark else FluentIcons.Regular.Dismiss,
                null, tint = tint, modifier = Modifier.size(13.dp)
            )
            Text(
                if (isSuccess) "Sent" else "Failed — retry",
                color = tint, fontSize = (11.5f * scale).sp, fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FeedbackUpdatesSettings(a: ScreenArgs) {
    val scale = a.scale

    // ── Send feedback ──────────────────────────────────────────────────────
    var message by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitResult by remember { mutableStateOf<Boolean?>(null) } // null=idle, true=ok, false=error

    val feedbackFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor   = a.textColor.copy(alpha = 0.04f),
        unfocusedContainerColor = a.textColor.copy(alpha = 0.03f),
        focusedBorderColor      = a.accent,
        unfocusedBorderColor    = Color.Transparent,
        focusedTextColor        = a.textColor,
        unfocusedTextColor      = a.textColor,
        cursorColor             = a.accent,
        focusedPlaceholderColor   = a.textColor.copy(alpha = 0.35f),
        unfocusedPlaceholderColor = a.textColor.copy(alpha = 0.35f)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = a.surfaceBg),
        shape  = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, a.textColor.copy(alpha = 0.07f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: accent-tinted icon badge + title/subtitle
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(a.accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(FluentIcons.Regular.Bug, null, tint = a.accent, modifier = Modifier.size(19.dp))
                }
                Column {
                    Text("Send feedback",
                        color = a.textColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = (15 * scale).sp)
                    Text("Report a bug or share an idea — it goes straight to the team",
                        color = a.textColor.copy(alpha = 0.55f),
                        fontSize = (12 * scale).sp)
                }
            }

            OutlinedTextField(
                value = message,
                onValueChange = { if (it.length <= 1000) { message = it; submitResult = null } },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("What's on your mind?") },
                minLines = 4,
                shape = RoundedCornerShape(12.dp),
                colors = feedbackFieldColors
            )
            Text(
                "${message.length}/1000",
                color = a.textColor.copy(alpha = 0.35f),
                fontSize = (10.5f * scale).sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; submitResult = null },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Email (optional — only if you'd like a reply)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = feedbackFieldColors
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Inline status chip — replaces the old plain red/green text
                AnimatedStatusChip(submitResult, scale)

                Button(
                    enabled = message.isNotBlank() && !isSubmitting,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = a.accent),
                    onClick = {
                        isSubmitting = true
                        submitResult = null
                        val feedback = hashMapOf(
                            "message" to message.trim(),
                            "email" to email.trim().ifBlank { null },
                            "appVersion" to (a.ctx.packageManager.getPackageInfo(a.ctx.packageName, 0).versionName ?: "unknown"),
                            "deviceModel" to Build.MODEL,
                            "deviceManufacturer" to Build.MANUFACTURER,
                            "androidSdk" to Build.VERSION.SDK_INT,
                            "timestamp" to FieldValue.serverTimestamp()
                        )
                        FirebaseFirestore.getInstance().collection("feedback").add(feedback)
                            .addOnSuccessListener {
                                isSubmitting = false; submitResult = true
                                message = ""; email = ""
                            }
                            .addOnFailureListener {
                                isSubmitting = false; submitResult = false
                            }
                    }
                ) {
                    if (isSubmitting) {
                        LinearProgressIndicator(
                            modifier = Modifier.width(52.dp).height(3.dp),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    } else {
                        Text("Send feedback", fontSize = (13 * scale).sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }

    // ── Check for updates ──────────────────────────────────────────────────
    var isChecking by remember { mutableStateOf(false) }
    var checkError by remember { mutableStateOf(false) }
    var upToDate by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }

    SettingsGroup("Check for updates", a) {
        val pkgInfo = remember(a.ctx) {
            try { a.ctx.packageManager.getPackageInfo(a.ctx.packageName, 0) } catch (_: Exception) { null }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text("Current version", color = a.textColor.copy(alpha = 0.6f), fontSize = (12 * scale).sp, modifier = Modifier.width(140.dp))
            Text(pkgInfo?.versionName ?: "1.0", color = a.textColor, fontSize = (12 * scale).sp)
        }
        Divider(color = divColor(a), modifier = Modifier.padding(horizontal = 12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                enabled = !isChecking,
                onClick = {
                    isChecking = true; checkError = false; upToDate = false; updateInfo = null
                    val currentCode = try {
                        a.ctx.packageManager.getPackageInfo(a.ctx.packageName, 0).let {
                            if (Build.VERSION.SDK_INT >= 28) it.longVersionCode else it.versionCode.toLong()
                        }
                    } catch (_: Exception) { 0L }

                    val remoteConfig = Firebase.remoteConfig
                    remoteConfig.setConfigSettingsAsync(remoteConfigSettings { minimumFetchIntervalInSeconds = 3600 })
                    remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                        isChecking = false
                        if (!task.isSuccessful) { checkError = true; return@addOnCompleteListener }
                        val latestCode = remoteConfig.getLong("latest_version_code")
                        if (latestCode > currentCode) {
                            updateInfo = UpdateInfo(
                                versionName = remoteConfig.getString("latest_version_name"),
                                releaseNotes = remoteConfig.getString("release_notes"),
                                downloadUrl = remoteConfig.getString("download_url")
                            )
                        } else {
                            upToDate = true
                        }
                    }
                }
            ) {
                if (isChecking) LinearProgressIndicator(modifier = Modifier.width(60.dp)) else Text("Check now")
            }
            when {
                checkError -> Text("Couldn't check — try again later.", color = Color(0xFFE57373), fontSize = (12 * scale).sp)
                upToDate -> Text("You're up to date", color = a.textColor.copy(alpha = 0.6f), fontSize = (12 * scale).sp)
            }
        }
    }

    updateInfo?.let { info ->
        AlertDialog(
            onDismissRequest = { updateInfo = null },
            title = { Text("Update available: v${info.versionName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("A new version of Bluebird is ready to download.")
                    if (info.releaseNotes.isNotBlank()) {
                        Text(info.releaseNotes, fontSize = 13.sp, color = a.textColor.copy(alpha = 0.7f))
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    a.ctx.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(info.downloadUrl)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                    updateInfo = null
                }) { Text("Download") }
            },
            dismissButton = { TextButton(onClick = { updateInfo = null }) { Text("Later") } }
        )
    }
}

// Remote Config keys to set in Firebase Console → Remote Config:
//   latest_version_name  (String)  e.g. "2.1"
//   latest_version_code  (Number)  e.g. 14  — must match versionCode in build.gradle.kts
//   release_notes        (String)  e.g. "Bug fixes and performance improvements"
//   download_url         (String)  direct link to the APK asset
private data class UpdateInfo(
    val versionName: String,
    val releaseNotes: String,
    val downloadUrl: String
)

// ─────────────────────────────────────────────────────────────────────────────
// ABOUT
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AboutSettings(a: ScreenArgs) {
    val scale   = a.scale
    val pm      = a.ctx.packageManager
    // PERF FIX: getPackageInfo() is a Binder IPC call to a system service. It was
    // previously run directly in the composable body, so every single recomposition
    // of this screen (e.g. from an unrelated state read) re-hit it. `remember` runs
    // it once and reuses the result.
    // Build number and package name rows were removed — end users have no use for
    // internal build metadata, and RAM stats were removed from this screen (a
    // per-app "About" page isn't the right place to show whole-device memory usage,
    // and it duplicated the device's own Settings > About).
    val pkgInfo = remember(a.ctx) { try { pm.getPackageInfo(a.ctx.packageName, 0) } catch (_: Exception) { null } }

    SettingsGroup("Device", a) {
        listOf(
            "Device name"     to Build.MODEL,
            "Manufacturer"    to Build.MANUFACTURER,
            "Android version" to Build.VERSION.RELEASE,
            "API level"       to Build.VERSION.SDK_INT.toString(),
            "Processor"       to Build.HARDWARE
        ).forEachIndexed { i, (label, value) ->
            if (i > 0) Divider(color = a.textColor.copy(alpha = 0.04f), modifier = Modifier.padding(horizontal = 12.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(label, color = a.textColor.copy(alpha = 0.6f), fontSize = (12 * scale).sp, modifier = Modifier.width(140.dp))
                Text(value, color = a.textColor, fontSize = (12 * scale).sp)
            }
        }
    }

    SettingsGroup("Bluebird Launcher", a) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text("Version", color = a.textColor.copy(alpha = 0.6f), fontSize = (12 * scale).sp, modifier = Modifier.width(140.dp))
            Text(pkgInfo?.versionName ?: "1.0", color = a.textColor, fontSize = (12 * scale).sp)
        }
        Divider(color = divColor(a), modifier = Modifier.padding(horizontal = 12.dp))
        // "Rate this launcher" removed — there's no store listing to rate. Bug
        // reports now go straight to the GitHub issue tracker instead of email,
        // so reports are visible, searchable, and trackable rather than landing
        // in a personal inbox.
        SNav(FluentIcons.Regular.Bug, "Report a bug", "Open an issue on GitHub", a = a) {
            a.ctx.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/trebronwayne/bluebird-releases/issues"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
        Divider(color = divColor(a), modifier = Modifier.padding(horizontal = 12.dp))
        // Not a nav row — it's a one-line privacy statement, not a link to
        // anything, so it shouldn't look clickable.
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Icon(FluentIcons.Regular.Shield, null, tint = a.textColor.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
            Text("All data is stored locally on your device", color = a.textColor, fontSize = (13 * scale).sp)
        }
        // "Open source licenses" row removed — it had no content or onClick
        // behind it.
    }
}
