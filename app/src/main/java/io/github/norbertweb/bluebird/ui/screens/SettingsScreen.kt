package io.github.norbertweb.bluebird.ui.screens

import io.github.norbertweb.bluebird.ui.components.micaCardOr
import io.github.norbertweb.bluebird.AppLinks
import io.github.norbertweb.bluebird.ui.components.micaLayerOr
import io.github.norbertweb.bluebird.ui.components.micaOr
import io.github.norbertweb.bluebird.i18n.ui
import androidx.annotation.DrawableRes
import io.github.norbertweb.bluebird.R
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.lerp
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
import fluent.ui.system.icons.regular.ArrowClockwise
import fluent.ui.system.icons.regular.ArrowDownload
import fluent.ui.system.icons.regular.Bug
import fluent.ui.system.icons.regular.CameraAdd
import fluent.ui.system.icons.regular.Checkmark
import fluent.ui.system.icons.regular.ChevronDown
import fluent.ui.system.icons.regular.ChevronRight
import fluent.ui.system.icons.regular.Code
import fluent.ui.system.icons.regular.DataTrending
import fluent.ui.system.icons.regular.Dismiss
import fluent.ui.system.icons.regular.Edit
import fluent.ui.system.icons.regular.Shield

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
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
import fluent.ui.system.icons.regular.Apps
import fluent.ui.system.icons.regular.CloudArrowUp
import fluent.ui.system.icons.regular.Info
import fluent.ui.system.icons.regular.Open
import fluent.ui.system.icons.regular.ShieldCheckmark
import fluent.ui.system.icons.regular.TopSpeed

// ─────────────────────────────────────────────────────────────────────────────
// CATEGORY ENUM — replaces raw strings to prevent silent routing bugs
// ─────────────────────────────────────────────────────────────────────────────

// Most categories use Icons8 PNGs (res/drawable/ic_*.png); BPK Apps doesn't
// have a matching PNG yet, so it falls back to a Fluent vector icon via this
// small wrapper. Swap BPK_APPS to CategoryIcon.Png(R.drawable.ic_bpk) once a
// matching Icons8 icon is added, same as the rest.
private sealed class CategoryIcon {
    data class Png(@DrawableRes val res: Int) : CategoryIcon()
    data class Vector(val icon: ImageVector) : CategoryIcon()
}

@Composable
private fun CategoryIconView(icon: CategoryIcon, modifier: Modifier = Modifier, tint: Color = Color.Unspecified) {
    when (icon) {
        is CategoryIcon.Png    -> Image(painter = painterResource(icon.res), contentDescription = null, modifier = modifier)
        is CategoryIcon.Vector -> Icon(icon.icon, null, tint = tint, modifier = modifier)
    }
}

private enum class SettingsCategory(val label: String, val icon: CategoryIcon) {
    // Placeholder globe PNG (res/drawable/ic_language.png) — swap for a Fluent icon later.
    LANGUAGE        ("Language",             CategoryIcon.Png(R.drawable.ic_language)),
    APPEARANCE      ("Appearance",          CategoryIcon.Png(R.drawable.ic_appearance)),
    SYSTEM          ("System",              CategoryIcon.Vector(FluentIcons.Regular.Apps)),
    ACCOUNTS        ("Accounts",            CategoryIcon.Png(R.drawable.ic_user)),
    PRIVACY_SECURITY("Privacy & security",  CategoryIcon.Png(R.drawable.ic_privacy)),
    FEEDBACK_UPDATES("Feedback & updates",  CategoryIcon.Png(R.drawable.ic_feedback)),
    BPK_APPS        ("BPK apps",            CategoryIcon.Png(R.drawable.ic_bpk)),
    ABOUT           ("About",               CategoryIcon.Png(R.drawable.ic_about))
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

    // Single source of truth for the whole transition: width and label
    // opacity used to be two independently-animated values (a tween'd width
    // plus an instant if/else that showed or hid the labels), which is what
    // made the collapse look like it "broke" — the labels were popping in
    // or out at a different moment than the width was still moving. Now
    // both are derived from one progress value, so they're always in step.
    val expandProgress by animateFloatAsState(
        targetValue = if (isNavExpanded) 1f else 0f,
        animationSpec = tween(320, easing = FastOutSlowInEasing),
        label = "navExpandProgress"
    )
    val navWidth = lerp(64.dp, 256.dp, expandProgress)
    // Labels fade out well before the pane finishes narrowing (and fade in
    // only once it's mostly open), so text is never visibly squeezed while
    // still opaque — by the time the row gets cramped it's already invisible.
    val labelAlpha = ((expandProgress - 0.35f) / 0.65f).coerceIn(0f, 1f)

    Row(modifier = Modifier.fillMaxSize().background(micaOr(resolvedBg))) {

        // ── Left nav pane ─────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .width(navWidth)
                .fillMaxHeight()
                .background(micaOr(resolvedNav))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { pokeNav() }
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            // Gated by `expandProgress > 0.001f` rather than the raw boolean so this
            // block stays composed (and fading via labelAlpha) for the full width
            // animation, then drops out of layout only once it's already fully
            // invisible — that's what stops the old "pop then catch up" look.
            if (uiState != null && expandProgress > 0.001f) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(labelAlpha)
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
                                contentDescription = stringResource(R.string.profile_picture),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize().background(specialAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(painter = painterResource(R.drawable.ic_user), contentDescription = null, modifier = Modifier.size(20.dp))
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
                        Text(stringResource(R.string.local_account),
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
                        .alpha(labelAlpha)
                        .clip(RoundedCornerShape(18.dp))
                        .background(resolvedBg)
                        .border(1.dp, resolvedText.copy(alpha = 0.1f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Image(painter = painterResource(R.drawable.ic_search), contentDescription = null, modifier = Modifier.size(16.dp))
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
                                if (navSearch.isEmpty()) Text(stringResource(R.string.settings_find), color = resolvedText.copy(alpha = 0.4f), fontSize = (13 * (uiState?.textScale ?: 1f)).sp)
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
                    // Padding and arrangement are now fixed regardless of expand
                    // state (only the label's own alpha + composition change) —
                    // that's what removes the old layout "snap": previously the
                    // padding/arrangement flipped instantly on the isNavExpanded
                    // boolean while the width was still mid-animation, which is
                    // what made the icon visibly jump mid-transition.
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) specialAccent.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { pokeNav(); selectedCategoryState.value = cat }
                        .padding(horizontal = 10.dp, vertical = 12.dp),
                    verticalAlignment  = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Selection indicator — always present (not gated on expand
                    // state) since Icons8 PNGs are full-color and can't be tinted
                    // to show selection the way the old Fluent vectors could; this
                    // bar now does that job at every width, collapsed or open.
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(if (isSelected) 16.dp else 0.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isSelected) specialAccent else Color.Transparent)
                    )
                    CategoryIconView(cat.icon, modifier = Modifier.size(18.dp),
                        tint = if (isSelected) specialAccent else resolvedText.copy(alpha = 0.75f))
                    if (expandProgress > 0.001f) {
                        Text(ui(cat.label),
                            color      = if (isSelected) specialAccent else resolvedText,
                            fontSize   = (13 * (uiState?.textScale ?: 1f)).sp,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            maxLines   = 1, overflow = TextOverflow.Ellipsis,
                            modifier   = Modifier.alpha(labelAlpha))
                    }
                }
                }
            }
        }

        // Crisp pane separation, matching Windows 11's thin divider between
        // its nav rail and content area (previously the two panes only
        // differed by background color, with no hard edge between them).
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(resolvedText.copy(alpha = 0.08f))
        )

        // ── Right content area ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(micaLayerOr(resolvedBg, effectiveDark))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 36.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(specialAccent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    CategoryIconView(selectedCategoryState.value.icon, modifier = Modifier.size(22.dp), tint = specialAccent)
                }
                Text(ui(selectedCategoryState.value.label),
                    style      = MaterialTheme.typography.headlineSmall,
                    color      = resolvedText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = (26 * (uiState?.textScale ?: 1f)).sp)
            }

            val args = ScreenArgs(effectiveDark, resolvedText, micaCardOr(resolvedSurface, effectiveDark), specialAccent, uiState, viewModel, context,
                scale = uiState?.textScale ?: 1f)

            when (selectedCategoryState.value) {
                SettingsCategory.LANGUAGE            -> LanguageSettings(args)
                SettingsCategory.APPEARANCE       -> AppearanceSettings(args)
                SettingsCategory.SYSTEM           -> SystemSettings(args)
                SettingsCategory.ACCOUNTS         -> AccountsSettings(args)
                SettingsCategory.PRIVACY_SECURITY -> PrivacySecuritySettings(args)
                SettingsCategory.FEEDBACK_UPDATES -> FeedbackUpdatesSettings(args)
                SettingsCategory.BPK_APPS         -> BpkAppsSettings(args)
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title.uppercase(),
            color      = a.textColor.copy(alpha = 0.5f),
            fontSize   = (11.5f * (a.scale)).sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.6.sp,
            modifier   = Modifier.padding(start = 4.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = a.surfaceBg),
            shape  = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, a.textColor.copy(alpha = 0.07f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) { Column(modifier = Modifier.padding(4.dp), content = content) }
    }
}

// Small Windows-11-style icon badge: icon sitting inside a soft rounded
// square, used consistently across every settings row for a less "plain
// list" look.
@Composable
private fun SIconBadge(icon: ImageVector, tint: Color, bg: Color) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(17.dp))
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
            .clip(RoundedCornerShape(10.dp))
            .clickable { onToggle() }           // ← FIX: whole row tappable
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment  = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SIconBadge(icon, tint = a.textColor.copy(alpha = 0.8f), bg = a.textColor.copy(alpha = 0.055f))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = a.textColor, fontSize = (14 * (a.scale)).sp, fontWeight = FontWeight.Medium)
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
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment  = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SIconBadge(icon, tint = a.textColor.copy(alpha = 0.8f), bg = a.textColor.copy(alpha = 0.055f))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = a.textColor, fontSize = (14 * (a.scale)).sp, fontWeight = FontWeight.Medium)
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
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            SIconBadge(icon, tint = a.textColor.copy(alpha = 0.8f), bg = a.textColor.copy(alpha = 0.055f))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = a.textColor, fontSize = (14 * (a.scale)).sp, fontWeight = FontWeight.Medium)
                if (sub.isNotEmpty()) Text(sub, color = a.textColor.copy(alpha = 0.55f), fontSize = (12 * (a.scale)).sp)
            }
            Text(valueLabel, color = a.textColor.copy(alpha = 0.6f), fontSize = (12 * (a.scale)).sp)
        }
        Slider(
            value         = value,
            onValueChange = onValueChange,
            valueRange    = range,
            colors        = SliderDefaults.colors(thumbColor = a.accent, activeTrackColor = a.accent),
            modifier      = Modifier.padding(start = 48.dp)
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
            .clip(RoundedCornerShape(10.dp))
            .clickable { expanded = true }
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment  = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SIconBadge(icon, tint = a.textColor.copy(alpha = 0.8f), bg = a.textColor.copy(alpha = 0.055f))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = a.textColor, fontSize = (14 * (a.scale)).sp, fontWeight = FontWeight.Medium)
            if (sub.isNotEmpty()) Text(sub, color = a.textColor.copy(alpha = 0.55f), fontSize = (12 * (a.scale)).sp)
        }
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(a.textColor.copy(alpha = 0.05f))
                    .padding(horizontal = 12.dp, vertical = 7.dp)
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

internal fun divColor(a: ScreenArgs) = a.textColor.copy(alpha = 0.05f)

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

    SettingsGroup(stringResource(R.string.personalization), a) {
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
                                ui(label), fontSize = (12 * scale).sp,
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
                    Text(stringResource(R.string.wallpaper_type), color = tcm, fontSize = (11 * scale).sp, fontWeight = FontWeight.Medium)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            DesktopWallpaperMode.APPARENT to stringResource(R.string.apparent),
                            DesktopWallpaperMode.DEFAULT  to stringResource(R.string.default_label),
                            DesktopWallpaperMode.CUSTOM   to stringResource(R.string.custom)
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
                            Text(stringResource(R.string.colour_scheme), color = tcm, fontSize = (11 * scale).sp, fontWeight = FontWeight.Medium)
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
                                            ui(gradientNames.getOrElse(idx) { stringResource(R.string.preset_n, idx + 1) }),
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
                            Text(stringResource(R.string.wallpaper_image), color = tcm, fontSize = (11 * scale).sp, fontWeight = FontWeight.Medium)
                            Text(
                                stringResource(R.string.select_one_or_more_with_more_than_one),
                                color = tcm.copy(alpha = 0.7f), fontSize = (10 * scale).sp
                            )
                            if (DEFAULT_WALLPAPERS.all { it == 0 }) {
                                Box(
                                    Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(8.dp))
                                        .background(a.textColor.copy(alpha = 0.06f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        stringResource(R.string.add_wallpapers_to_res_drawable_nas),
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
                            Text(stringResource(R.string.your_wallpapers), color = tc, fontSize = (11 * scale).sp, fontWeight = FontWeight.Medium)
                            Text(
                                stringResource(R.string.tap_one_to_use_it_or_add_another_from),
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
                        Text(stringResource(R.string.background_animation), color = tcm, fontSize = (11 * scale).sp, fontWeight = FontWeight.Medium)
                        Text(stringResource(R.string.select_one_or_more_to_mix_them), color = tcm.copy(alpha = 0.7f), fontSize = (10 * scale).sp)
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
                                            ui(bgAnimationLabel(type)), fontSize = (11.5 * scale).sp,
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
                            Text(stringResource(R.string.turn_off_animations), fontSize = (11 * scale).sp, color = acc)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(R.string.intensity), color = tcm, fontSize = (11 * scale).sp, fontWeight = FontWeight.Medium)
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
                        Text(stringResource(R.string.live_wallpaper), color = tcm, fontSize = (11 * scale).sp, fontWeight = FontWeight.Medium)
                        Text(
                            stringResource(R.string.replaces_the_static_wallpaper_and),
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
                            Text(stringResource(R.string.off), fontSize = (10 * scale).sp, color = if (offActive) acc else tcm)
                        }
                        listOf(
                            LiveWallpaperType.AURORA to stringResource(R.string.aurora),
                            LiveWallpaperType.NEBULA to stringResource(R.string.nebula),
                            LiveWallpaperType.WAVES  to stringResource(R.string.waves),
                            LiveWallpaperType.BOKEH  to stringResource(R.string.bokeh)
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

    // ── Effects & performance ───────────────────────────────────────────────
    // Global switch for every Acrylic/Mica blur (windows, taskbar, Start, flyouts). Off = flat,
    // opaque surfaces and no blur sampling at all — the fastest option on slower devices.
    SettingsGroup(stringResource(R.string.effects_and_performance), a) {
        val transparencyOn = a.uiState?.transparencyEffects ?: true
        SToggle(
            icon = FluentIcons.Regular.TopSpeed,
            label = stringResource(R.string.transparency_effects),
            sub = stringResource(R.string.transparency_effects_desc),
            a = a,
            checked = transparencyOn
        ) { a.vm?.setTransparencyEffects(!transparencyOn) }
    }

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

    SettingsGroup(stringResource(R.string.your_account), a) {
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
                            Image(painter = painterResource(R.drawable.ic_user), contentDescription = null, modifier = Modifier.size(32.dp))
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(a.uiState.userProfile.userName, color = a.textColor, fontSize = (16 * scale).sp, fontWeight = FontWeight.Medium)
                    Text(stringResource(R.string.local_account), color = a.textColor.copy(alpha = 0.5f), fontSize = (12 * scale).sp)
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
                        placeholder = { Text(stringResource(R.string.setup_display_name)) },
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
                    TextButton(enabled = editedName.isNotBlank(), onClick = { commit() }) { Text(stringResource(R.string.save)) }
                    TextButton(onClick = { isEditingName = false }) { Text(stringResource(R.string.cancel)) }
                }
            } else {
                SNav(FluentIcons.Regular.Edit, stringResource(R.string.change_username), stringResource(R.string.update_your_display_name), a = a) {
                    editedName = a.uiState.userProfile.userName
                    isEditingName = true
                }
            }

            Divider(color = divColor(a), modifier = Modifier.padding(horizontal = 12.dp))
            SNav(FluentIcons.Regular.CameraAdd, stringResource(R.string.change_profile_picture), stringResource(R.string.pick_a_photo_from_your_gallery),   a = a) {
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
    SettingsGroup(stringResource(R.string.data_diagnostics), a) {
        SToggle(FluentIcons.Regular.DataTrending, stringResource(R.string.usage_diagnostics),
            stringResource(R.string.send_anonymous_crash_reports_and_usage),
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
                if (isSuccess) stringResource(R.string.sent) else stringResource(R.string.failed_retry),
                color = tint, fontSize = (11.5f * scale).sp, fontWeight = FontWeight.Medium
            )
        }
    }
}

// Small colored pill used for update status ("Checking…", "Up to date",
// "Update available", "Couldn't check") — same visual language as
// AnimatedStatusChip above, reused here so both cards feel consistent.
@Composable
private fun StatusPill(icon: ImageVector?, text: String, tint: Color, scale: Float, spinning: Boolean = false) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(tint.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (spinning) {
            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = tint)
        } else if (icon != null) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(13.dp))
        }
        Text(text, color = tint, fontSize = (11.5f * scale).sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FeedbackUpdatesSettings(a: ScreenArgs) {
    val scale = a.scale

    // ── Send feedback ──────────────────────────────────────────────────────
    // Collapsed by default — only the compact header row shows until the
    // user taps it. The full form (and the always-open look it had before)
    // isn't shown unless they actually want to write something.
    var feedbackExpanded by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitResult by remember { mutableStateOf<Boolean?>(null) } // null=idle, true=ok, false=error

    // After a successful send, collapse back to the compact row on its own
    // shortly after — the person can still see the "Sent" chip for a beat
    // first, so it doesn't feel like the form just vanished.
    LaunchedEffect(submitResult) {
        if (submitResult == true) {
            delay(1400)
            feedbackExpanded = false
        }
    }

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
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            // Header — always visible, tap to expand/collapse the form.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { feedbackExpanded = !feedbackExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(a.accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(painter = painterResource(R.drawable.ic_feedback), contentDescription = null, modifier = Modifier.size(19.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.send_feedback),
                        color = a.textColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = (15 * scale).sp)
                    Text(stringResource(R.string.report_a_bug_or_share_an_idea_it_goes),
                        color = a.textColor.copy(alpha = 0.55f),
                        fontSize = (12 * scale).sp)
                }
                Icon(
                    if (feedbackExpanded) FluentIcons.Regular.ChevronDown else FluentIcons.Regular.ChevronRight,
                    null, tint = a.textColor.copy(alpha = 0.4f), modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = feedbackExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = message,
                        onValueChange = { if (it.length <= 1000) { message = it; submitResult = null } },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.whats_on_your_mind)) },
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
                        placeholder = { Text(stringResource(R.string.email_optional_only_if_youd_like_a)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = feedbackFieldColors
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
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
                                Text(stringResource(R.string.send_feedback), fontSize = (13 * scale).sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Check for updates ──────────────────────────────────────────────────
    // Fully inline now — no dialog. Checks once automatically when this
    // screen opens (so an outdated version is indicated without the person
    // needing to tap anything), with a persistent status pill and a
    // "Check again" row for a manual re-check; update details expand in
    // place below instead of interrupting with a popup.
    var isChecking by remember { mutableStateOf(false) }
    var checkError by remember { mutableStateOf(false) }
    var upToDate by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }

    fun runUpdateCheck() {
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

    LaunchedEffect(Unit) { runUpdateCheck() }

    SettingsGroup(stringResource(R.string.check_for_updates), a) {
        val pkgInfo = remember(a.ctx) {
            try { a.ctx.packageManager.getPackageInfo(a.ctx.packageName, 0) } catch (_: Exception) { null }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.current_version), color = a.textColor.copy(alpha = 0.6f), fontSize = (12 * scale).sp)
                Text(pkgInfo?.versionName ?: "1.0", color = a.textColor, fontSize = (13 * scale).sp, fontWeight = FontWeight.Medium)
            }
            when {
                isChecking  -> StatusPill(null, stringResource(R.string.checking), a.textColor.copy(alpha = 0.55f), scale, spinning = true)
                checkError  -> StatusPill(FluentIcons.Regular.Dismiss, stringResource(R.string.couldnt_check), Color(0xFFE57373), scale)
                updateInfo != null -> StatusPill(FluentIcons.Regular.ArrowDownload, stringResource(R.string.update_available), a.accent, scale)
                upToDate    -> StatusPill(FluentIcons.Regular.Checkmark, stringResource(R.string.up_to_date), Color(0xFF4CAF50), scale)
            }
        }

        Divider(color = divColor(a), modifier = Modifier.padding(horizontal = 12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable(enabled = !isChecking) { runUpdateCheck() }
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(FluentIcons.Regular.ArrowClockwise, null, tint = a.accent, modifier = Modifier.size(16.dp))
            Text(stringResource(R.string.check_again), color = a.accent, fontSize = (13 * scale).sp, fontWeight = FontWeight.Medium)
        }

        // Update details — expand in place, no popup.
        AnimatedVisibility(visible = updateInfo != null) {
            updateInfo?.let { info ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Divider(color = divColor(a))
                    Spacer(Modifier.height(2.dp))
                    Text(stringResource(R.string.version_is_available, info.versionName),
                        color = a.textColor, fontWeight = FontWeight.Medium, fontSize = (13 * scale).sp)
                    if (info.releaseNotes.isNotBlank()) {
                        Text(info.releaseNotes, color = a.textColor.copy(alpha = 0.6f), fontSize = (12 * scale).sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = a.accent),
                            onClick = {
                                a.ctx.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(info.downloadUrl)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        ) { Text(stringResource(R.string.download), fontSize = (12.5f * scale).sp) }
                        TextButton(onClick = { updateInfo = null }) {
                            Text(stringResource(R.string.common_dismiss), fontSize = (12.5f * scale).sp)
                        }
                    }
                }
            }
        }
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
// BPK APPS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BpkAppsSettings(a: ScreenArgs) {
    val scale = a.scale
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    var showFullGuide by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(2200)
            copied = false
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Hero panel: make it clear that BPK is Bluebird's installable web-app format.
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = a.accent.copy(alpha = if (a.isDark) 0.20f else 0.10f)),
            border = BorderStroke(1.dp, a.accent.copy(alpha = 0.24f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier.size(52.dp).clip(RoundedCornerShape(15.dp)).background(a.accent.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(FluentIcons.Regular.Code, null, tint = a.accent, modifier = Modifier.size(27.dp)) }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(stringResource(R.string.bluebird_package_kit), color = a.textColor, fontSize = (19 * scale).sp, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.installable_web_apps_bpk), color = a.textColor.copy(alpha = 0.68f), fontSize = (12.5f * scale).sp)
                    }
                }
                Text(
                    stringResource(R.string.bpk_turns_a_web_project_into_an),
                    color = a.textColor.copy(alpha = 0.86f), fontSize = (13.2f * scale).sp, lineHeight = (19 * scale).sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    BpkTag("HTML / CSS / JS", a)
                    BpkTag(stringResource(R.string.version_1), a)
                    BpkTag(stringResource(R.string.zip_based), a)
                }
            }
        }

        SettingsGroup(stringResource(R.string.what_you_can_do), a) {
            BpkFeatureRow(FluentIcons.Regular.Add, stringResource(R.string.install_and_launch), stringResource(R.string.install_a_bpk_package_and_launch_it_as), a)
            Divider(color = a.textColor.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 12.dp))
            BpkFeatureRow(FluentIcons.Regular.Shield, stringResource(R.string.app_permissions), stringResource(R.string.review_each_apps_capabilities_in), a)
            Divider(color = a.textColor.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 12.dp))
            BpkFeatureRow(FluentIcons.Regular.DataTrending, stringResource(R.string.private_app_data), stringResource(R.string.apps_can_keep_small_key_value_settings), a)
            Divider(color = a.textColor.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 12.dp))
            BpkFeatureRow(FluentIcons.Regular.Bug, stringResource(R.string.logs_and_troubleshooting), stringResource(R.string.apps_can_write_diagnostic_messages_and), a)
            Divider(color = a.textColor.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 12.dp))
            BpkFeatureRow(FluentIcons.Regular.ArrowClockwise, stringResource(R.string.updates_and_reinstall), stringResource(R.string.the_bpk_update_client_can_check_an), a)
        }

        SettingsGroup(stringResource(R.string.package_structure), a) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.a_bpk_is_a_zip_archive_renamed_with), color = a.textColor.copy(alpha = 0.78f), fontSize = (12.8f * scale).sp)
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(a.textColor.copy(alpha = 0.045f)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    BpkTreeLine("my-app.bpk/", true, a)
                    BpkTreeLine("manifest.json", false, a)
                    BpkTreeLine("icon/icon.png", false, a)
                    BpkTreeLine("app/index.html", false, a)
                    BpkTreeLine("app/styles.css", false, a)
                    BpkTreeLine("app/main.js", false, a)
                    BpkTreeLine("installer/  (optional custom installer assets)", false, a)
                }
                Text(stringResource(R.string.the_manifest_entry_must_point_to_a), color = a.textColor.copy(alpha = 0.64f), fontSize = (12 * scale).sp, lineHeight = (17 * scale).sp)
            }
        }

        SettingsGroup(stringResource(R.string.capabilities), a) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                BpkCapabilityLine(stringResource(R.string.app_storage), stringResource(R.string.read_write_the_apps_own_key_value_data), a)
                BpkCapabilityLine(stringResource(R.string.internet), stringResource(R.string.network_access_only_grant_this_to_apps), a)
                BpkCapabilityLine(stringResource(R.string.file_picker), stringResource(R.string.request_the_system_file_picker_the), a)
                BpkCapabilityLine(stringResource(R.string.media_picker), stringResource(R.string.request_the_system_media_picker_the), a)
                BpkCapabilityLine(stringResource(R.string.logs), stringResource(R.string.allow_the_app_to_send_diagnostic), a)
                BpkCapabilityLine(stringResource(R.string.audio_video_playback), stringResource(R.string.capability_flags_for_media_playback), a)
                BpkCapabilityLine(stringResource(R.string.brightness_control), stringResource(R.string.request_a_brightness_change_android), a)
                Text(stringResource(R.string.important_a_permission_toggle_is_not_a), color = a.textColor.copy(alpha = 0.72f), fontSize = (12 * scale).sp, lineHeight = (17 * scale).sp)
            }
        }

        SettingsGroup(stringResource(R.string.build_a_bpk_with_an_ai_assistant), a) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.you_can_ask_an_ai_to_create_a_complete), color = a.textColor.copy(alpha = 0.76f), fontSize = (12.8f * scale).sp, lineHeight = (18 * scale).sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { clipboardManager.setText(AnnotatedString(BPK_SETTINGS_DEVELOPER_GUIDE)); copied = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = a.accent),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(if (copied) FluentIcons.Regular.Checkmark else FluentIcons.Regular.Code, null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(Modifier.width(7.dp))
                        Text(if (copied) stringResource(R.string.guide_copied) else stringResource(R.string.copy_developer_guide), fontSize = (12.5f * scale).sp)
                    }
                    OutlinedButton(onClick = { showFullGuide = true }, shape = RoundedCornerShape(14.dp)) {
                        Text(stringResource(R.string.preview), fontSize = (12.5f * scale).sp)
                    }
                }
            }
        }

        SettingsGroup(stringResource(R.string.recommended_workflow), a) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                BpkStep("1", stringResource(R.string.create), stringResource(R.string.build_the_app_in_html_css_and), a)
                BpkStep("2", "Describe", "Fill in manifest.json with a unique id, app name, version, publisher, entry file, icon, and description.", a)
                BpkStep("3", "Package", "Zip the package contents so manifest.json is at the archive root, then rename the ZIP to .bpk.", a)
                BpkStep("4", stringResource(R.string.install_and_test), stringResource(R.string.install_it_in_bluebird_test_launch_and), a)
                BpkStep("5", stringResource(R.string.release_carefully), stringResource(R.string.use_https_for_update_feeds_and_verify), a)
            }
        }
    }

    if (showFullGuide) {
        AlertDialog(
            onDismissRequest = { showFullGuide = false },
            title = { Text(stringResource(R.string.bpk_developer_guide)) },
            text = {
                Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(BPK_SETTINGS_DEVELOPER_GUIDE, color = a.textColor, fontSize = (12 * scale).sp, lineHeight = (17 * scale).sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { clipboardManager.setText(AnnotatedString(BPK_SETTINGS_DEVELOPER_GUIDE)); copied = true; showFullGuide = false }) { Text(stringResource(R.string.copy_guide)) }
            },
            dismissButton = { TextButton(onClick = { showFullGuide = false }) { Text(stringResource(R.string.close)) } }
        )
    }
}

@Composable
private fun BpkTag(label: String, a: ScreenArgs) {
    Box(Modifier.clip(RoundedCornerShape(50)).background(a.surfaceBg.copy(alpha = 0.82f)).padding(horizontal = 9.dp, vertical = 5.dp)) {
        Text(label, color = a.textColor.copy(alpha = 0.78f), fontSize = (10.5f * a.scale).sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun BpkFeatureRow(icon: ImageVector, title: String, description: String, a: ScreenArgs) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SIconBadge(icon, tint = a.accent, bg = a.accent.copy(alpha = 0.10f))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = a.textColor, fontSize = (13.5f * a.scale).sp, fontWeight = FontWeight.SemiBold)
            Text(description, color = a.textColor.copy(alpha = 0.66f), fontSize = (12 * a.scale).sp, lineHeight = (17 * a.scale).sp)
        }
    }
}

@Composable
private fun BpkCapabilityLine(title: String, description: String, a: ScreenArgs) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, color = a.textColor, fontSize = (12.8f * a.scale).sp, fontWeight = FontWeight.SemiBold)
        Text(description, color = a.textColor.copy(alpha = 0.65f), fontSize = (11.8f * a.scale).sp, lineHeight = (16.5f * a.scale).sp)
    }
}

@Composable
private fun BpkTreeLine(value: String, isRoot: Boolean, a: ScreenArgs) {
    Text(value, color = if (isRoot) a.accent else a.textColor.copy(alpha = 0.78f), fontSize = (12 * a.scale).sp, fontWeight = if (isRoot) FontWeight.SemiBold else FontWeight.Normal)
}

@Composable
private fun BpkStep(number: String, title: String, detail: String, a: ScreenArgs) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Box(Modifier.size(25.dp).clip(CircleShape).background(a.accent.copy(alpha = 0.13f)), contentAlignment = Alignment.Center) {
            Text(number, color = a.accent, fontSize = (11 * a.scale).sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = a.textColor, fontSize = (12.8f * a.scale).sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = a.textColor.copy(alpha = 0.66f), fontSize = (11.8f * a.scale).sp, lineHeight = (16.5f * a.scale).sp)
        }
    }
}

private const val BPK_SETTINGS_DEVELOPER_GUIDE = """
BLUEBIRD BPK DEVELOPER GUIDE — FORMAT V1

PURPOSE
A .bpk is a ZIP archive containing a web app for Bluebird. Build the app with HTML, CSS and JavaScript. Keep it self-contained where practical and design for small screens and resizable app windows.

REQUIRED ARCHIVE LAYOUT
manifest.json
icon/icon.png
app/index.html (or the entry path declared in manifest.json)
app/other files...
installer/ (optional custom installer assets)

MANIFEST FIELDS
{
  "id": "org.example.myapp",
  "name": "My App",
  "version": "1.0.0",
  "publisher": "Example Publisher",
  "entry": "app/index.html",
  "icon": "icon/icon.png",
  "description": "A short description",
  "homepage": "https://example.org/",
  "runtime": "web"
}

The required identity fields are id, name, version, publisher, entry and icon. Use a stable, unique id made from letters, numbers, dots, underscores or hyphens. Entry and icon paths must exist inside the package. The homepage and description are optional. Keep manifest.json at the archive root.

PACKAGING
1. Put manifest.json, icon/, app/ and optional installer/ at the top level of the ZIP. Do not zip an extra parent folder around them.
2. Ensure the icon is a valid PNG and the entry HTML exists.
3. Zip the contents and rename the resulting .zip to .bpk.
4. Install in Bluebird and test on the target device. A file extension alone does not make an invalid archive installable.

JAVASCRIPT BRIDGE
When Bluebird exposes the bridge to trusted local app content, the global object is named Bluebird. Calls return JSON strings unless otherwise noted:
- Bluebird.getAppInfo() — returns the current app id and bridge API version.
- Bluebird.capabilityGranted(name) — returns whether the named capability is granted.
- Bluebird.storageGet(key) — reads this app's private key/value data.
- Bluebird.storageSet(key, value) — writes this app's private key/value data.
- Bluebird.log(level, message) — writes a diagnostic message when Logs capability is granted.
- Bluebird.requestFilePicker() — asks the host to open the file picker if File Picker capability is granted.
- Bluebird.requestMediaPicker() — asks the host to open the media picker if Media Picker capability is granted.
- Bluebird.setBrightness(value) — requests brightness from 1 to 255 if granted and supported by the host and Android settings.

Example:
const info = JSON.parse(Bluebird.getAppInfo());
if (Bluebird.capabilityGranted('app_storage')) {
  const saved = JSON.parse(Bluebird.storageGet('display-name'));
  if (saved.ok && saved.value) console.log(saved.value);
  Bluebird.storageSet('display-name', 'Guest');
}
Bluebird.log('info', 'App started');

Storage keys may contain letters, numbers, dots, underscores, spaces and hyphens; they cannot contain slashes or backslashes. Each value is limited to approximately 256 KiB. The bridge returns objects with ok/value/error fields encoded as JSON for storage and picker/brightness methods. File and media picker selection events must be handled according to the host runtime's documented event integration; do not assume a picker result is synchronously returned by requestFilePicker().

CAPABILITIES
logs, app_storage, internet, file_picker, media_picker, audio_playback, video_playback, brightness_control. Request only the capabilities needed. Bluebird permission grants are not a substitute for safe coding or validation; actual enforcement can vary by host integration. Never expose the native JavaScript bridge to arbitrary remote websites. Restrict navigation and use safe local-content loading. Treat downloaded app content as untrusted until reviewed.

UPDATES
The update client expects an HTTPS JSON feed containing id, version, packageUrl and sha256. The app id must match the installed app, the package URL must use HTTPS, and sha256 must be a 64-character hexadecimal SHA-256 digest of the exact downloaded .bpk bytes. The package is staged and validated before install. Ask the user before applying an update. SHA-256 verifies file integrity against the supplied digest; it does not authenticate the publisher. For stronger publisher identity, add a signed manifest/package and verify it against a trusted public key.

TEST CHECKLIST
- Install a valid package and reject malformed packages.
- Confirm name, version, icon and entry file are correct.
- Test launch, window resizing, navigation and closing.
- Test every capability both granted and denied.
- Test missing files, invalid paths, oversized values and network failures.
- Verify logs are useful and do not include passwords, tokens or private user data.
- Test updates with correct and incorrect hashes and mismatched app ids.
- Test uninstall and confirm the expected behavior for retained app data.
- Never claim a capability is secure unless it is enforced in the actual runtime.
"""

// ─────────────────────────────────────────────────────────────────────────────
// ABOUT
// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// SYSTEM — default launcher
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SystemSettings(a: ScreenArgs) {
    val ctx = a.ctx
    var isDefault by remember { mutableStateOf(AppLinks.isDefaultLauncher(ctx)) }

    // Re-check whenever we come back from the system role dialog / Home settings.
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) isDefault = AppLinks.isDefaultLauncher(ctx)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        isDefault = AppLinks.isDefaultLauncher(ctx)
    }

    SettingsGroup(stringResource(R.string.default_launcher), a) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                if (isDefault) FluentIcons.Regular.ShieldCheckmark else FluentIcons.Regular.Info, null,
                tint = if (isDefault) Color(0xFF3FB950) else a.textColor.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(if (isDefault) R.string.default_launcher_active else R.string.default_launcher_inactive),
                    color = a.textColor, fontSize = (14 * a.scale).sp, fontWeight = FontWeight.Medium
                )
                Text(
                    stringResource(if (isDefault) R.string.default_launcher_active_desc else R.string.default_launcher_inactive_desc),
                    color = a.textColor.copy(alpha = 0.55f), fontSize = (12 * a.scale).sp
                )
            }
        }
        Divider(color = divColor(a), modifier = Modifier.padding(horizontal = 12.dp))
        if (!isDefault) {
            SNav(FluentIcons.Regular.Apps, stringResource(R.string.set_default_launcher), stringResource(R.string.set_default_launcher_desc), a = a) {
                try { roleLauncher.launch(AppLinks.defaultLauncherIntent(ctx, forceChooser = false)) }
                catch (e: Exception) { AppLinks.openDefaultLauncherSettings(ctx) }
            }
            Divider(color = divColor(a), modifier = Modifier.padding(horizontal = 12.dp))
        }
        SNav(FluentIcons.Regular.Open, stringResource(R.string.change_default_launcher), stringResource(R.string.change_default_launcher_desc), a = a) {
            AppLinks.openDefaultLauncherSettings(ctx, forceChooser = true)
        }
    }
}

@Composable
private fun AboutSettings(a: ScreenArgs) {
    val scale   = a.scale

    // ── Support Bluebird: share the website, view the open-source repo ──────
    val shareTitle = stringResource(R.string.share_bluebird)
    SettingsGroup(stringResource(R.string.support_bluebird), a) {
        SNav(FluentIcons.Regular.CloudArrowUp, shareTitle, stringResource(R.string.share_bluebird_desc), a = a) {
            AppLinks.shareApp(a.ctx, shareTitle)
        }
        Divider(color = divColor(a), modifier = Modifier.padding(horizontal = 12.dp))
        SNav(FluentIcons.Regular.Code, stringResource(R.string.open_source_repo), stringResource(R.string.open_source_repo_desc), a = a) {
            AppLinks.openRepo(a.ctx)
        }
        Divider(color = divColor(a), modifier = Modifier.padding(horizontal = 12.dp))
        SNav(FluentIcons.Regular.Info, stringResource(R.string.show_a_tip), stringResource(R.string.show_a_tip_desc), a = a) {
            a.vm?.showTipNow()
        }
    }

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

    SettingsGroup(stringResource(R.string.device), a) {
        listOf(
            "Device name"     to Build.MODEL,
            "Manufacturer"    to Build.MANUFACTURER,
            "Android version" to Build.VERSION.RELEASE,
            "API level"       to Build.VERSION.SDK_INT.toString(),
            "Processor"       to Build.HARDWARE
        ).forEachIndexed { i, (label, value) ->
            if (i > 0) Divider(color = a.textColor.copy(alpha = 0.04f), modifier = Modifier.padding(horizontal = 12.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(ui(label), color = a.textColor.copy(alpha = 0.6f), fontSize = (12 * scale).sp, modifier = Modifier.width(140.dp))
                Text(value, color = a.textColor, fontSize = (12 * scale).sp)
            }
        }
    }

    SettingsGroup(stringResource(R.string.bluebird_launcher), a) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(stringResource(R.string.version), color = a.textColor.copy(alpha = 0.6f), fontSize = (12 * scale).sp, modifier = Modifier.width(140.dp))
            Text(pkgInfo?.versionName ?: "1.0", color = a.textColor, fontSize = (12 * scale).sp)
        }
        Divider(color = divColor(a), modifier = Modifier.padding(horizontal = 12.dp))
        // "Rate this launcher" removed — there's no store listing to rate. Bug
        // reports now go straight to the GitHub issue tracker instead of email,
        // so reports are visible, searchable, and trackable rather than landing
        // in a personal inbox.
        SNav(FluentIcons.Regular.Bug, stringResource(R.string.report_a_bug), stringResource(R.string.open_an_issue_on_github), a = a) {
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
            Text(stringResource(R.string.all_data_local), color = a.textColor, fontSize = (13 * scale).sp)
        }
        // "Open source licenses" row removed — it had no content or onClick
        // behind it.
    }
}
