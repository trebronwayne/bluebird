package io.github.norbertweb.bluebird.ui.components

// Icons come from the shared FluentIcon object (FluentIcon.kt), which wraps
// the io.github.niyajali:fluentui-system-icons Compose Multiplatform library.
// Dependency (module build.gradle.kts):
//     implementation("io.github.niyajali:fluentui-system-icons:1.0.1")
import io.github.norbertweb.bluebird.i18n.ui
import androidx.compose.ui.res.stringResource
import io.github.norbertweb.bluebird.R
import android.Manifest
import android.app.AlarmManager
import android.app.AppOpsManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.session.MediaSessionManager
import android.net.TrafficStats
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.geometry.Offset
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import io.github.norbertweb.bluebird.LauncherUiState
import io.github.norbertweb.bluebird.LauncherScreen
import io.github.norbertweb.bluebird.LauncherViewModel
import io.github.norbertweb.bluebird.ui.theme.LocalIsDarkTheme
import io.github.norbertweb.bluebird.ui.theme.LocalTextScale
import io.github.norbertweb.bluebird.ui.theme.bluebirdColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

// ─── Persistence helpers ──────────────────────────────────────────────────────

private fun Context.widgetPrefs() = getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)

private fun saveTodos(context: Context, tasks: List<TodoTask>) {
    val json = org.json.JSONArray().apply {
        tasks.forEach { t ->
            put(JSONObject().apply {
                put("id", t.id)
                put("text", t.text)
                put("done", t.done)
                put("priority", t.priority.name)
                put("dueAt", t.dueAt ?: JSONObject.NULL)
            })
        }
    }
    context.widgetPrefs().edit().putString("todos", json.toString()).apply()
}

private fun loadTodos(context: Context): List<TodoTask> {
    val raw = context.widgetPrefs().getString("todos", null) ?: return emptyList()
    return try {
        val arr = org.json.JSONArray(raw)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            // "id"/"priority"/"dueAt" are new fields — fall back gracefully so
            // to-dos saved by older versions of this widget still load fine.
            TodoTask(
                id       = o.optString("id").ifBlank { UUID.randomUUID().toString() },
                text     = o.getString("text"),
                done     = o.getBoolean("done"),
                priority = try {
                    TaskPriority.valueOf(o.optString("priority", TaskPriority.NONE.name))
                } catch (e: Exception) { TaskPriority.NONE },
                dueAt    = if (o.isNull("dueAt")) null else o.optLong("dueAt").takeIf { it > 0 }
            )
        }
    } catch (e: Exception) { emptyList() }
}

private fun saveTodoSortByPriority(context: Context, enabled: Boolean) {
    context.widgetPrefs().edit().putBoolean("todo_sort_by_priority", enabled).apply()
}

private fun loadTodoSortByPriority(context: Context): Boolean =
    context.widgetPrefs().getBoolean("todo_sort_by_priority", false)

private fun saveTodoFilter(context: Context, filter: String) {
    context.widgetPrefs().edit().putString("todo_filter", filter).apply()
}

private fun loadTodoFilter(context: Context): String =
    context.widgetPrefs().getString("todo_filter", "all") ?: "all"

// ─── Sticky notes ───────────────────────────────────────────────────────────

private fun saveStickyNotes(context: Context, notes: List<StickyNote>) {
    val json = org.json.JSONArray().apply {
        notes.forEach { n ->
            put(JSONObject().apply {
                put("id", n.id)
                put("text", n.text)
                put("color", n.colorHex)
            })
        }
    }
    context.widgetPrefs().edit().putString("sticky_notes", json.toString()).apply()
}

private fun loadStickyNotes(context: Context): List<StickyNote> {
    val raw = context.widgetPrefs().getString("sticky_notes", null) ?: return emptyList()
    return try {
        val arr = org.json.JSONArray(raw)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            StickyNote(
                id       = o.optString("id").ifBlank { UUID.randomUUID().toString() },
                text     = o.getString("text"),
                colorHex = o.optString("color", StickyNoteColors.default)
            )
        }
    } catch (e: Exception) { emptyList() }
}

private fun saveWidgetOrder(context: Context, order: List<String>) {
    context.widgetPrefs().edit().putString("widget_order", order.joinToString(",")).apply()
}

private fun loadWidgetOrder(context: Context): List<String> {
    val default = listOf("clock","photos","todo","notes","alarm","network")
    val saved = context.widgetPrefs().getString("widget_order", null) ?: return default
    val savedList = saved.split(",").filter { it.isNotBlank() }
    val allowedBuiltIns = default.toSet()
    val cleaned = savedList.filter { it.startsWith("ext:") || it in allowedBuiltIns }
    // Keep saved order, remove retired built-ins, and append newly available ones.
    return (cleaned + default.filter { it !in cleaned })
}

// ─── External (third-party) app widgets ────────────────────────────────────
// Bound via AppWidgetHost. Represented in `widget_order` as "ext:<appWidgetId>"
// entries so they can be freely reordered alongside the built-in widgets.

private fun saveExternalWidgetIds(context: Context, ids: Set<Int>) {
    context.widgetPrefs().edit()
        .putStringSet("external_widget_ids", ids.map { it.toString() }.toSet())
        .apply()
}

private fun loadExternalWidgetIds(context: Context): Set<Int> =
    context.widgetPrefs().getStringSet("external_widget_ids", emptySet())
        ?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()

private fun saveHiddenWidgets(context: Context, hidden: Set<String>) {
    context.widgetPrefs().edit().putStringSet("hidden_widgets", hidden).apply()
}

private fun loadHiddenWidgets(context: Context): Set<String> =
    context.widgetPrefs().getStringSet("hidden_widgets", emptySet()) ?: emptySet()

// ─── Pinned widgets ─────────────────────────────────────────────────────────
// Pinned widgets always render first, above the rest of the (still freely
// reorderable) list — the same "pin to top" idea as a pinned Slack channel
// or a pinned home-screen app.

private fun savePinnedWidgets(context: Context, pinned: Set<String>) {
    context.widgetPrefs().edit().putStringSet("pinned_widgets", pinned).apply()
}

private fun loadPinnedWidgets(context: Context): Set<String> =
    context.widgetPrefs().getStringSet("pinned_widgets", emptySet()) ?: emptySet()

// ─── Compact density ────────────────────────────────────────────────────────

private fun saveCompactDensity(context: Context, compact: Boolean) {
    context.widgetPrefs().edit().putBoolean("compact_density", compact).apply()
}

private fun loadCompactDensity(context: Context): Boolean =
    context.widgetPrefs().getBoolean("compact_density", false)

// ─── Dynamic (wallpaper-matched) color ──────────────────────────────────────
// Only meaningful on Android 12+ (S), where the system exposes a
// wallpaper-derived Material You palette. Defaults to on when available.

private fun saveDynamicColorEnabled(context: Context, enabled: Boolean) {
    context.widgetPrefs().edit().putBoolean("dynamic_color_enabled", enabled).apply()
}

private fun loadDynamicColorEnabled(context: Context): Boolean =
    context.widgetPrefs().getBoolean("dynamic_color_enabled", true)

private enum class TaskPriority(val label: String, val color: Color) {
    NONE("No priority", Color(0xFF9E9E9E)),
    LOW("Low", Color(0xFF66BB6A)),
    MEDIUM("Medium", Color(0xFFFFA726)),
    HIGH("High", Color(0xFFEF5350));

    fun next(): TaskPriority = values()[(ordinal + 1) % values().size]
}

private data class TodoTask(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val done: Boolean,
    val priority: TaskPriority = TaskPriority.NONE,
    val dueAt: Long? = null
)

private data class WeatherData(
    val city: String, val condition: String, val temp: Int,
    val high: Int, val low: Int, val forecast: List<DayForecast>
)
private data class DayForecast(val day: String, val icon: String, val high: Int, val low: Int)
private data class StockTicker(val symbol: String, val change: String, val price: String, val isUp: Boolean, val points: List<Float>)
private data class NewsArticle(val title: String, val source: String, val url: String)
private data class CalendarEvent(val title: String, val time: String, val color: Color)

// ─── Sticky notes ───────────────────────────────────────────────────────────

private data class StickyNote(val id: String, val text: String, val colorHex: String)

/** Classic pastel sticky-note palette, referenced by hex so it round-trips through JSON cleanly. */
private object StickyNoteColors {
    val palette = listOf(
        "#FFF9C4", // yellow
        "#F8BBD0", // pink
        "#BBDEFB", // blue
        "#C8E6C9", // green
        "#E1BEE7", // purple
        "#FFE0B2", // orange
    )
    val default get() = palette[0]
}

private fun stickyColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: Exception) { Color(0xFFFFF9C4) }

/** Shared divider tone used inside widget cards. */
private fun widgetDividerColor(textColor: Color): Color = textColor.copy(alpha = 0.10f)

@Composable
private fun WidgetHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    textColor: Color,
    textScale: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.78f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                title,
                color = textColor,
                fontSize = (14 * textScale).sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─── Widget IDs ───────────────────────────────────────────────────────────────

// Built-in widget ids. Third-party app widgets bound via AppWidgetHost are
// represented separately as "ext:<appWidgetId>" entries inside widgetOrder —
// they don't need a fixed slot here since their count is dynamic.
private val ALL_WIDGET_IDS = listOf(
    "clock", "photos", "todo", "notes", "alarm", "network"
)

private val WIDGET_LABELS = mapOf(
    "clock"      to "Clock",
    "photos"     to "Photos",
    "todo"       to "To Do",
    "notes"      to "Sticky Notes",
    "alarm"      to "Next Alarm",
    "network"    to "Network Speed"
)

// A small fixed palette, picked deterministically per widget id so every
// row gets a distinct, consistent identity color across sessions — the
// same trick iOS/Android settings screens use for app icons in a list.
private val WIDGET_SWATCH_HUES = listOf(210f, 265f, 152f, 28f, 338f, 190f, 48f, 300f)
private fun widgetAccentColor(id: String): Color {
    val hue = WIDGET_SWATCH_HUES[Math.floorMod(id.hashCode(), WIDGET_SWATCH_HUES.size)]
    val argb = android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.58f, 0.93f))
    return Color(argb)
}

/** Real app name + icon for a hosted third-party widget, so the rearrange list can show "Google Keep" instead of a generic "App widget". */
private data class ExternalWidgetMeta(val label: String, val icon: android.graphics.Bitmap?)

private fun loadExternalWidgetMeta(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int): ExternalWidgetMeta {
    val pm = context.packageManager
    return try {
        val info = appWidgetManager.getAppWidgetInfo(widgetId)
            ?: return ExternalWidgetMeta("Widget", null)
        val appInfo = pm.getApplicationInfo(info.provider.packageName, 0)
        val appLabel = pm.getApplicationLabel(appInfo).toString()
        val widgetLabel = try { info.loadLabel(pm) } catch (e: Exception) { "" }
        val label = if (widgetLabel.isNotBlank() && !widgetLabel.equals(appLabel, ignoreCase = true))
            "$appLabel \u2022 $widgetLabel" else appLabel
        val icon = try { info.loadIcon(context, 0)?.toBitmap(width = 96, height = 96) }
            catch (e: Exception) { try { appInfo.loadIcon(pm).toBitmap(width = 96, height = 96) } catch (e2: Exception) { null } }
        ExternalWidgetMeta(label, icon)
    } catch (e: Exception) {
        ExternalWidgetMeta("Widget", null)
    }
}

// ─────────────────────────────────────────────────────────
// Main Widget Panel
// ─────────────────────────────────────────────────────────
// A fixed host id for this panel's AppWidgetHost. Any small positive int is
// fine as long as it's unique within the app (only one widget surface hosts
// widgets here, so a constant is fine).
private const val BLUEBIRD_APPWIDGET_HOST_ID = 1042

// ─── Custom widget picker ───────────────────────────────────────────────────
// Replaces the raw system ACTION_APPWIDGET_PICK dialog (a plain, unstyled
// list owned entirely by the OS) with our own grid of real widget previews,
// grouped by app — the same shape every mainstream launcher uses. We still
// go through the system exactly once, but only to ask for BIND_APPWIDGET
// permission on the *specific* widget the user already chose in our UI,
// never to browse.

private data class WidgetPickerEntry(
    val provider: android.appwidget.AppWidgetProviderInfo,
    val appLabel: String,
    val widgetLabel: String,
    val previewBitmap: android.graphics.Bitmap?,
    val iconBitmap: android.graphics.Bitmap?,
    val spanCols: Int,
    val spanRows: Int,
)

/** Rough cell size (dp) used only to turn a provider's minWidth/minHeight into an N×M badge. */
private const val WIDGET_GRID_CELL_DP = 72

private fun loadWidgetPickerEntries(context: Context, appWidgetManager: AppWidgetManager): List<WidgetPickerEntry> {
    val pm = context.packageManager
    val densityDpi = context.resources.displayMetrics.densityDpi
    return appWidgetManager.installedProviders.mapNotNull { info ->
        try {
            val appInfo = pm.getApplicationInfo(info.provider.packageName, 0)
            val appLabel = pm.getApplicationLabel(appInfo).toString()
            val widgetLabel = info.loadLabel(pm).ifBlank { appLabel }

            val previewDrawable = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                    try { info.loadPreviewImage(context, densityDpi) } catch (e: Exception) { null }
                else -> null
            } ?: if (info.previewImage != 0) {
                try { pm.getDrawable(info.provider.packageName, info.previewImage, appInfo) } catch (e: Exception) { null }
            } else null

            val iconDrawable = try { info.loadIcon(context, densityDpi) } catch (e: Exception) {
                try { appInfo.loadIcon(pm) } catch (e2: Exception) { null }
            }

            val cellPx = (WIDGET_GRID_CELL_DP * (densityDpi / 160f)).roundToInt().coerceAtLeast(1)
            WidgetPickerEntry(
                provider = info,
                appLabel = appLabel,
                widgetLabel = widgetLabel,
                previewBitmap = previewDrawable?.let { runCatching { it.toBitmap() }.getOrNull() },
                iconBitmap = iconDrawable?.let { runCatching { it.toBitmap(width = 96, height = 96) }.getOrNull() },
                spanCols = (info.minWidth / cellPx).coerceIn(1, 4),
                spanRows = (info.minHeight / cellPx).coerceIn(1, 4),
            )
        } catch (e: Exception) { null }
    }.sortedBy { it.appLabel.lowercase(Locale.getDefault()) }
}

@Composable
private fun WidgetPickerSheet(
    isDark: Boolean,
    textColor: Color,
    textScale: Float,
    onDismiss: () -> Unit,
    onPick: (android.appwidget.AppWidgetProviderInfo) -> Unit,
) {
    val context = LocalContext.current
    val appWidgetManager = remember { AppWidgetManager.getInstance(context) }
    val bg = if (isDark) bluebirdColors.WidgetBg else bluebirdColors.WidgetBgLight
    val border = if (isDark) DS.borderDark else DS.borderLight
    var query by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf<List<WidgetPickerEntry>?>(null) }

    LaunchedEffect(Unit) {
        entries = withContext(Dispatchers.IO) { loadWidgetPickerEntries(context, appWidgetManager) }
    }

    val grouped: Map<String, List<WidgetPickerEntry>> = remember(entries, query) {
        val list = entries ?: emptyList()
        val filtered = if (query.isBlank()) list else list.filter {
            it.appLabel.contains(query, ignoreCase = true) || it.widgetLabel.contains(query, ignoreCase = true)
        }
        filtered.groupBy { it.appLabel }.toSortedMap(compareBy { it.lowercase(Locale.getDefault()) })
    }

    // Scrim + sheet, drawn as the topmost layer of the panel.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize()
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {}
                .shadow(elevation = 24.dp, shape = RoundedCornerShape(PANEL_CORNER))
                .clip(RoundedCornerShape(PANEL_CORNER))
                .background(bg)
                .border(1.dp, border, RoundedCornerShape(PANEL_CORNER))
        ) {
            // Header: title + close.
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.add_a_widget), color = textColor, fontSize = (17 * textScale).sp, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                    Icon(imageVector = FluentIcon.Dismiss, contentDescription = stringResource(R.string.close), tint = textColor.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                }
            }

            // Search field, styled like a real search bar rather than a bare OutlinedTextField.
            Row(
                modifier = Modifier
                    .padding(horizontal = 18.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(999.dp))
                    .background(textColor.copy(alpha = 0.06f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(stringResource(R.string.search_widgets_or_apps), color = textColor.copy(alpha = 0.4f), fontSize = (13 * textScale).sp)
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = textColor, fontSize = (13 * textScale).sp),
                        cursorBrush = Brush.verticalGradient(listOf(LocalWidgetAccent.current, LocalWidgetAccent.current)),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (query.isNotEmpty()) {
                    Icon(
                        imageVector = FluentIcon.Dismiss, contentDescription = stringResource(R.string.clear_search),
                        tint = textColor.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp).clickable { query = "" }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            when {
                entries == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LocalWidgetAccent.current, modifier = Modifier.size(28.dp))
                }
                grouped.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_widgets_found), color = textColor.copy(alpha = 0.5f), fontSize = (13 * textScale).sp)
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    grouped.forEach { (appLabel, widgets) ->
                        item(key = "header:$appLabel") {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                widgets.first().iconBitmap?.let {
                                    Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.size(18.dp).clip(RoundedCornerShape(4.dp)))
                                }
                                Text(appLabel, color = textColor, fontSize = (13 * textScale).sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        item(key = "row:$appLabel") {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(widgets, key = { it.provider.provider.flattenToString() }) { entry ->
                                    WidgetPickerCard(entry, isDark, textColor, textScale) { onPick(entry.provider) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetPickerCard(
    entry: WidgetPickerEntry,
    isDark: Boolean,
    textColor: Color,
    textScale: Float,
    onClick: () -> Unit,
) {
    val cardBg = if (isDark) bluebirdColors.WidgetBgLight.copy(alpha = 0.06f) else Color.White
    val cardBorder = if (isDark) DS.borderDark else DS.borderLight

    Column(
        modifier = Modifier
            .width((WIDGET_GRID_CELL_DP * entry.spanCols + 24).dp.coerceAtMost(220.dp))
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(WIDGET_CORNER + 3.dp), ambientColor = Color.Black.copy(alpha = 0.12f), spotColor = Color.Black.copy(alpha = 0.12f))
            .clip(RoundedCornerShape(WIDGET_CORNER + 3.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(WIDGET_CORNER + 3.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((WIDGET_GRID_CELL_DP * entry.spanRows).dp.coerceAtMost(140.dp))
                .clip(RoundedCornerShape(WIDGET_CORNER))
                .background(if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.03f)),
            contentAlignment = Alignment.Center
        ) {
            if (entry.previewBitmap != null) {
                Image(
                    bitmap = entry.previewBitmap.asImageBitmap(),
                    contentDescription = entry.widgetLabel,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(WIDGET_CORNER)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            } else if (entry.iconBitmap != null) {
                Image(bitmap = entry.iconBitmap.asImageBitmap(), contentDescription = entry.widgetLabel, modifier = Modifier.size(40.dp))
            } else {
                Icon(imageVector = FluentIcon.Image, contentDescription = null, tint = textColor.copy(alpha = 0.3f), modifier = Modifier.size(28.dp))
            }
            // Size badge, e.g. "4×2" — exactly what Android's own S+ widget
            // picker overlays on every preview, so this reads as native.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text("${entry.spanCols}\u00D7${entry.spanRows}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            entry.widgetLabel, color = textColor, fontSize = (11 * textScale).sp, fontWeight = FontWeight.Medium,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun WidgetsPanel(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel,
    hazeState: dev.chrisbanes.haze.HazeState,
    accent: Color,
    modifier: Modifier = Modifier,
    // Defaults to actually opening the photo — openWindow() already reuses
    // an existing Image Viewer window instead of stacking a new one, so this
    // works out of the box without the caller needing to wire anything up.
    onOpenPhoto: (Uri) -> Unit = { uri ->
        viewModel.openWindow(LauncherScreen.IMAGE_VIEWER, extras = mapOf("filePath" to uri.toString()))
    }
) {
    val context    = LocalContext.current
    // Follows the system's light/dark setting directly instead of the app's
    // own theme toggle, so this panel always matches the rest of Android.
    val isDark     = LocalIsDarkTheme.current
    val textColor  = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    val scope      = rememberCoroutineScope()
    val textScale  = LocalTextScale.current

    var isRefreshing  by remember { mutableStateOf(false) }
    var editMode      by remember { mutableStateOf(false) }
    var showWidgetPicker by remember { mutableStateOf(false) }
    var configurePendingId by remember { mutableStateOf(-1) }
    var widgetOrder   by remember { mutableStateOf(loadWidgetOrder(context)) }
    var hiddenWidgets by remember { mutableStateOf(loadHiddenWidgets(context).filter { it in ALL_WIDGET_IDS || it.startsWith("ext:") }.toSet()) }
    var externalIds   by remember { mutableStateOf(loadExternalWidgetIds(context)) }
    // Looked up once per set of external widgets so the edit panel can show
    // "Google Keep" instead of a generic "App widget" label.
    val externalMeta = remember(externalIds) {
        externalIds.associateWith { id -> loadExternalWidgetMeta(context, AppWidgetManager.getInstance(context), id) }
    }
    var pinnedWidgets by remember { mutableStateOf(loadPinnedWidgets(context).filter { it in ALL_WIDGET_IDS || it.startsWith("ext:") }.toSet()) }
    // Widgets stay on Bluebird's own Fluent accent so system wallpaper colors
    // cannot unexpectedly recolor the shell's tonal hierarchy.
    val accentColor = bluebirdColors.AccentBlue
    val density = WidgetDensity.COMPACT

    // ── AppWidgetHost: lets this panel embed widgets from other installed apps ──
    val appWidgetManager = remember { AppWidgetManager.getInstance(context) }
    val appWidgetHost = remember { AppWidgetHost(context, BLUEBIRD_APPWIDGET_HOST_ID) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> appWidgetHost.startListening()
                Lifecycle.Event.ON_STOP  -> appWidgetHost.stopListening()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        appWidgetHost.startListening()
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            appWidgetHost.stopListening()
        }
    }

    fun addExternalWidget(id: Int) {
        externalIds = externalIds + id
        saveExternalWidgetIds(context, externalIds)
        val newOrder = widgetOrder + "ext:$id"
        widgetOrder = newOrder
        saveWidgetOrder(context, newOrder)
    }

    fun removeExternalWidget(id: Int) {
        appWidgetHost.deleteAppWidgetId(id)
        externalIds = externalIds - id
        saveExternalWidgetIds(context, externalIds)
        val newOrder = widgetOrder.filter { it != "ext:$id" }
        widgetOrder = newOrder
        saveWidgetOrder(context, newOrder)
    }

    // Step 2b: the widget's own configuration activity (e.g. "pick a
    // calendar", "pick a photo album") — only added to the panel once the
    // provider itself reports success, same as a real home screen.
    val configureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val id = configurePendingId
        configurePendingId = -1
        if (id != -1) {
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                addExternalWidget(id)
            } else {
                appWidgetHost.deleteAppWidgetId(id)
            }
        }
    }

    // Whether some part of the flow still needs the provider's own setup
    // screen (e.g. a calendar widget asking which calendar to show) before
    // it's actually added to the panel.
    fun finishAdding(id: Int, provider: android.appwidget.AppWidgetProviderInfo?) {
        if (provider?.configure != null) {
            configurePendingId = id
            configureLauncher.launch(
                Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                    component = provider.configure
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                }
            )
        } else {
            addExternalWidget(id)
        }
    }

    // Step 2 (only reached if the provider needed explicit bind approval):
    // the system asked the user a single yes/no "allow?" question scoped to
    // the exact widget already chosen in our own picker UI below — never a
    // browsing list.
    val bindLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val id = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
        if (result.resultCode == android.app.Activity.RESULT_OK && id != -1) {
            finishAdding(id, appWidgetManager.getAppWidgetInfo(id))
        } else if (id != -1) {
            appWidgetHost.deleteAppWidgetId(id)
        }
    }

    // Step 1: our own picker (grid of real previews, grouped by app) has
    // already chosen a specific provider. Try to bind it directly — most
    // widgets are pre-approved for a launcher and this succeeds silently,
    // with zero system UI at all. Only fall back to asking the system for
    // permission when bindAppWidgetIdIfAllowed refuses.
    fun addProviderWidget(provider: android.appwidget.AppWidgetProviderInfo) {
        val newId = appWidgetHost.allocateAppWidgetId()
        val bound = try {
            appWidgetManager.bindAppWidgetIdIfAllowed(newId, provider.provider)
        } catch (e: SecurityException) { false }
        if (bound) {
            finishAdding(newId, provider)
        } else {
            bindLauncher.launch(
                Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, newId)
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider.provider)
                }
            )
        }
        showWidgetPicker = false
    }

    // Refresh trigger — only widgets that support an explicit refresh observe it.
    var refreshTick by remember { mutableStateOf(0) }
    val visibleWidgetIds = remember(widgetOrder, hiddenWidgets, pinnedWidgets) {
        val visible = widgetOrder.filter { it !in hiddenWidgets }
        val pinned  = visible.filter { it in pinnedWidgets }
        val rest    = visible.filter { it !in pinnedWidgets }
        pinned + rest
    }
    val effectiveTextScale = textScale * density.textScaleMultiplier

    CompositionLocalProvider(
        LocalWidgetAccent provides accentColor,
        LocalWidgetDensity provides density
    ) {
    // Panel width scales with the available window instead of a fixed 380dp,
    // so widget cards (and any hosted third-party widget) get more breathing
    // room on larger phones/foldables, clamped to a sane range either way.
    BoxWithConstraints(modifier.fillMaxHeight()) {
        val panelWidth = if (maxWidth > 900.dp) (maxWidth * 0.50f).coerceIn(PANEL_MIN_WIDTH, PANEL_MAX_WIDTH) else (maxWidth * 0.92f).coerceIn(PANEL_MIN_WIDTH, PANEL_MAX_WIDTH)
        AcrylicSurface(
            modifier = Modifier.width(panelWidth).fillMaxHeight(),
            isDark = isDark, accent = accent, cornerRadius = PANEL_CORNER,
            hazeState = hazeState
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(density.itemSpacing)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.widgets),
                    color = textColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Refresh — a single determinate spin, not a looping animation.
                    val rotation = remember { Animatable(0f) }
                    IconButton(onClick = {
                        if (!isRefreshing) {
                            isRefreshing = true
                            refreshTick++
                            scope.launch {
                                rotation.snapTo(0f)
                                rotation.animateTo(360f, tween(450, easing = FastOutSlowInEasing))
                                isRefreshing = false
                            }
                        }
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = FluentIcon.ArrowSync, contentDescription = stringResource(R.string.refresh_widgets),
                            tint = textColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp).rotate(rotation.value))
                    }
                    // Edit / Done
                    IconButton(onClick = { editMode = !editMode },
                        modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (editMode) FluentIcon.Checkmark else FluentIcon.Edit,
                            contentDescription = if (editMode) stringResource(R.string.done_editing) else stringResource(R.string.edit_widgets),
                            tint = if (editMode) LocalWidgetAccent.current else textColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ── Edit mode: visibility toggles + drag-to-reorder + add widget ──
            AnimatedVisibility(visible = editMode) {
                WidgetEditPanel(
                    order       = widgetOrder,
                    hidden      = hiddenWidgets,
                    pinned      = pinnedWidgets,
                    isDark      = isDark,
                    textColor   = textColor,
                    textScale   = textScale,
                    externalMeta = externalMeta,
                    onToggle    = { id ->
                        hiddenWidgets = if (id in hiddenWidgets)
                            hiddenWidgets - id else hiddenWidgets + id
                        saveHiddenWidgets(context, hiddenWidgets)
                    },
                    onTogglePin = { id ->
                        pinnedWidgets = if (id in pinnedWidgets)
                            pinnedWidgets - id else pinnedWidgets + id
                        savePinnedWidgets(context, pinnedWidgets)
                    },
                    onReorder   = { newOrder ->
                        widgetOrder = newOrder
                        saveWidgetOrder(context, newOrder)
                    },
                    onRemoveExternal = { id -> removeExternalWidget(id) },
                    onAddWidget = { showWidgetPicker = true }
                )
            }

            // ── Render widgets in saved order (pinned first) ──────────────────
            visibleWidgetIds.forEachIndexed { index, id ->
                key(id) {
                    when {
                        id == "clock"      -> ClockWidget(isDark, context, effectiveTextScale)
                        id == "photos"     -> PhotosWidget(isDark, context, effectiveTextScale, onOpenPhoto = onOpenPhoto)
                        id == "todo"       -> TodoWidget(isDark, context, effectiveTextScale)
                        id == "notes"      -> StickyNotesWidget(isDark, context, effectiveTextScale)
                        id == "alarm"      -> AlarmWidget(isDark, context, effectiveTextScale)
                        id == "network"    -> NetworkSpeedWidget(isDark, effectiveTextScale)
                        id.startsWith("ext:") -> {
                            val widgetId = id.removePrefix("ext:").toIntOrNull()
                            if (widgetId != null) {
                                ExternalAppWidget(widgetId, appWidgetHost, appWidgetManager, isDark)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
        }

        if (showWidgetPicker) {
            WidgetPickerSheet(
                isDark = isDark,
                textColor = textColor,
                textScale = textScale,
                onDismiss = { showWidgetPicker = false },
                onPick = { provider -> addProviderWidget(provider) }
            )
        }
    }
    }
}

// ─────────────────────────────────────────────────────────
// External app widget host — renders a widget picked from another app,
// wrapped in the same card language as the built-in widgets.
// ─────────────────────────────────────────────────────────
@Composable
private fun ExternalAppWidget(
    widgetId: Int,
    host: AppWidgetHost,
    manager: AppWidgetManager,
    isDark: Boolean
) {
    val info = remember(widgetId) { manager.getAppWidgetInfo(widgetId) } ?: return
    val density = LocalDensity.current

    // Most third-party widgets already ship their own opaque background,
    // shape, and often their own corner radius — wrapping that in a second
    // card (our bg + border + WIDGET_CORNER on top of the widget's own
    // radius) is what produced the "double frame" / mismatched-corner look.
    // We now only *clip* the hosted view to our shared radius so it sits
    // flush with the rest of the panel, and let the widget's own chrome
    // show through untouched — the same approach a home-screen launcher
    // takes with third-party widgets.
    var slotWidthPx by remember { mutableStateOf(0) }
    val minHeightDp = remember(info) { info.minHeight }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeightDp.dp)
            .onSizeChanged { slotWidthPx = it.width }
            .clip(RoundedCornerShape(WIDGET_CORNER))
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth().heightIn(min = minHeightDp.dp),
            factory = { ctx ->
                host.createView(ctx, widgetId, info).apply {
                    setPadding(0, 0, 0, 0)
                }
            },
            // Without this, RemoteViews never learn how much room they
            // actually have and frequently fall back to a cramped default
            // layout — this is the most common cause of a bound widget
            // rendering squished or oddly proportioned inside a host.
            update = { view ->
                if (slotWidthPx > 0) {
                    val widthDp = with(density) { slotWidthPx.toDp().value.toInt() }
                    view.updateAppWidgetSize(
                        Bundle(),
                        widthDp, minHeightDp,
                        widthDp, minHeightDp
                    )
                }
            }
        )
    }
}

// ─── Edit Panel ───────────────────────────────────────────────────────────────

@Composable
private fun WidgetEditPanel(
    order: List<String>,
    hidden: Set<String>,
    pinned: Set<String>,
    isDark: Boolean,
    textColor: Color,
    textScale: Float,
    externalMeta: Map<Int, ExternalWidgetMeta>,
    onToggle: (String) -> Unit,
    onTogglePin: (String) -> Unit,
    onReorder: (List<String>) -> Unit,
    onRemoveExternal: (Int) -> Unit,
    onAddWidget: () -> Unit,
) {
    val bg     = if (isDark) bluebirdColors.WidgetBg else bluebirdColors.WidgetBgLight
    val border = if (isDark) DS.borderDark else DS.borderLight
    val haptic = LocalHapticFeedback.current

    // ── Manual drag-to-reorder state ──
    // Rows report their measured height; while dragging we track how far the
    // pointer has moved and swap the dragged row past a neighbor once it
    // crosses that neighbor's midpoint — the same interaction used by most
    // native "rearrange your home screen" pickers. A light haptic tick fires
    // on pickup and again on every swap, mirroring how the system home
    // screen's own rearrange mode feels.
    var localOrder by remember(order) { mutableStateOf(order) }
    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val rowHeightPx = remember { mutableStateOf(1f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(WIDGET_CORNER), ambientColor = Color.Black.copy(alpha = if (isDark) 0.3f else 0.05f), spotColor = Color.Black.copy(alpha = if (isDark) 0.3f else 0.05f))
            .clip(RoundedCornerShape(WIDGET_CORNER))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(WIDGET_CORNER))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.edit_widgets),
                color      = LocalWidgetAccent.current,
                fontSize   = (13 * textScale).sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                stringResource(R.string.hold_the_grip_to_drag),
                color      = textColor.copy(alpha = 0.4f),
                fontSize   = (10 * textScale).sp,
                fontWeight = FontWeight.Medium
            )
        }

        localOrder.forEachIndexed { index, id ->
            key(id) {
                val isExternal = id.startsWith("ext:")
                val externalId = if (isExternal) id.removePrefix("ext:").toIntOrNull() else null
                val meta = externalId?.let { externalMeta[it] }
                val label = meta?.label ?: ui(WIDGET_LABELS[id] ?: id)
                val visible = id !in hidden
                val isPinned = id in pinned
                val isDragged = id == draggingId
                val accent = widgetAccentColor(id)

                // ── FLIP-style reflow: when a swap moves this row to a new
                // index, it should visibly slide into its new slot instead
                // of teleporting there — the difference between a settings
                // list and something that feels physically grabbable.
                val flipOffset = remember { Animatable(0f) }
                var lastIndex by remember { mutableStateOf(index) }
                LaunchedEffect(index) {
                    if (!isDragged && lastIndex != index) {
                        val delta = (lastIndex - index) * rowHeightPx.value
                        lastIndex = index
                        flipOffset.snapTo(delta)
                        flipOffset.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow))
                    } else {
                        lastIndex = index
                    }
                }

                // A held row lifts: elevation and a faint accent glow ramp up,
                // and it very slightly scales up — the same "picked up off
                // the table" cue as iOS/Android's own home-screen edit mode.
                val liftT by animateFloatAsState(if (isDragged) 1f else 0f, animationSpec = spring(stiffness = Spring.StiffnessMedium), label = "liftT")

                // While *any* row is being dragged, every other (non-dragged,
                // non-hidden-behind-drag) row gets a subtle iOS-style jiggle
                // so edit mode reads as "alive" rather than static — but it
                // pauses on the row you're actually holding.
                val jiggle = rememberInfiniteTransition(label = "jiggle$id")
                val jiggleAngle by jiggle.animateFloat(
                    initialValue = -1.4f, targetValue = 1.4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(180 + (index % 3) * 40, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ), label = "jiggleAngle"
                )

                // Each row's own accent tints its background very faintly and
                // its border more visibly — the "polished colors" pass, so
                // every widget reads as its own distinct card rather than a
                // uniform gray list, while staying calm enough not to fight
                // the switch/label content.
                val baseBg = if (isDark) bluebirdColors.WidgetBgLight.copy(alpha = 0.05f) else Color.White
                val rowBg = androidx.compose.ui.graphics.lerp(baseBg, accent, if (isDark) 0.10f else 0.055f)
                val restBorder = accent.copy(alpha = if (isDark) 0.30f else 0.22f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { rowHeightPx.value = it.size.height.toFloat() + 10f }
                        .zIndex(if (isDragged) 1f else 0f)
                        .graphicsLayer {
                            translationY = if (isDragged) dragOffset else flipOffset.value
                            val scale = 1f + liftT * 0.04f
                            scaleX = scale; scaleY = scale
                            rotationZ = if (isDragged) 0f else if (draggingId != null) 0f else jiggleAngle
                        }
                        .shadow(
                            elevation = (3f + liftT * 16f).dp,
                            shape = RoundedCornerShape(18.dp),
                            ambientColor = accent.copy(alpha = 0.35f),
                            spotColor = accent.copy(alpha = 0.35f)
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .background(rowBg)
                        .border(
                            if (isDragged) 1.6.dp else 1.dp,
                            androidx.compose.ui.graphics.lerp(restBorder, accent, liftT),
                            RoundedCornerShape(18.dp)
                        )
                        .padding(vertical = 14.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Grip handle: bigger, with a dot grid rather than a
                    // bare icon — gives the drag target real weight.
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (isDragged) accent.copy(alpha = 0.20f) else textColor.copy(alpha = 0.05f))
                            // Keyed on the row's own stable id, NOT on
                            // localOrder — detectDragGesturesAfterLongPress
                            // already loops forever internally to handle one
                            // drag after another. Keying this on localOrder
                            // (which mutates on every swap) tore the gesture
                            // coroutine down and rebuilt it mid-drag, which
                            // cancelled the drag you were actively doing the
                            // instant it crossed the first swap threshold.
                            .pointerInput(id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggingId = id; dragOffset = 0f
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    onDragEnd = { draggingId = null; dragOffset = 0f; onReorder(localOrder) },
                                    onDragCancel = { draggingId = null; dragOffset = 0f },
                                    onDrag = { change, delta ->
                                        change.consume()
                                        dragOffset += delta.y
                                        val rowH = rowHeightPx.value.coerceAtLeast(1f)
                                        val currentIdx = localOrder.indexOf(id)
                                        val targetIdx = (currentIdx + (dragOffset / rowH).roundToInt())
                                            .coerceIn(0, localOrder.lastIndex)
                                        if (targetIdx != currentIdx) {
                                            localOrder = localOrder.toMutableList().apply {
                                                add(targetIdx, removeAt(currentIdx))
                                            }
                                            dragOffset -= (targetIdx - currentIdx) * rowH
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val dotColor = if (isDragged) accent else textColor.copy(alpha = 0.4f)
                        Canvas(modifier = Modifier.size(18.dp, 20.dp)) {
                            val r = 1.8.dp.toPx()
                            val colXs = listOf(size.width * 0.28f, size.width * 0.72f)
                            val rowYs = listOf(size.height * 0.12f, size.height * 0.5f, size.height * 0.88f)
                            for (cx in colXs) for (cy in rowYs) {
                                drawCircle(color = dotColor, radius = r, center = Offset(cx, cy))
                            }
                        }
                    }

                    // Identity avatar: the real app icon for a hosted
                    // third-party widget, or a colored initial swatch for a
                    // built-in one — this is what makes "App widget" become
                    // recognizable at a glance instead of anonymous.
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(accent.copy(alpha = if (isDark) 0.28f else 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (meta?.icon != null) {
                            Image(
                                bitmap = meta.icon.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.size(26.dp).clip(RoundedCornerShape(7.dp))
                            )
                        } else {
                            Text(
                                label.take(1).uppercase(Locale.getDefault()),
                                color = accent,
                                fontSize = (15 * textScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Switch(
                        checked  = visible,
                        onCheckedChange = { onToggle(id) },
                        colors   = SwitchDefaults.colors(checkedThumbColor = accent),
                        modifier = Modifier.size(40.dp, 23.dp)
                    )
                    Text(
                        label, color = textColor,
                        fontSize = (15 * textScale).sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onTogglePin(id) }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = FluentIcon.Pin,
                            contentDescription = if (isPinned) stringResource(R.string.unpin_widget) else stringResource(R.string.pin_widget_to_top),
                            tint = if (isPinned) accent else textColor.copy(alpha = 0.3f),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    if (isExternal) {
                        IconButton(
                            onClick = { externalId?.let(onRemoveExternal) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = FluentIcon.Delete, contentDescription = stringResource(R.string.remove_widget),
                                tint = bluebirdColors.Error.copy(alpha = 0.75f), modifier = Modifier.size(19.dp))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = widgetDividerColor(textColor), thickness = 0.5.dp
        )

        // ── Add a widget from any installed app ──
        // Styled to match every other surface in the panel (WIDGET_CORNER +
        // shadow + border) instead of the old flat 11dp tinted row, which
        // read as a generic settings list item rather than a card action.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(WIDGET_CORNER),
                    ambientColor = Color.Black.copy(alpha = if (isDark) 0.25f else 0.05f),
                    spotColor = Color.Black.copy(alpha = if (isDark) 0.25f else 0.05f)
                )
                .clip(RoundedCornerShape(WIDGET_CORNER))
                .background(LocalWidgetAccent.current.copy(alpha = 0.08f))
                .border(1.dp, LocalWidgetAccent.current.copy(alpha = 0.25f), RoundedCornerShape(WIDGET_CORNER))
                .clickable { onAddWidget() }
                .padding(vertical = 12.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(LocalWidgetAccent.current.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = FluentIcon.Add, contentDescription = null,
                    tint = LocalWidgetAccent.current, modifier = Modifier.size(15.dp))
            }
            Text(
                stringResource(R.string.add_widget_from_an_app),
                color = LocalWidgetAccent.current,
                fontSize = (13 * textScale).sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = widgetDividerColor(textColor), thickness = 0.5.dp
        )
    }
}

// Shared corner radius for every widget-panel surface (cards, edit sheet,
// external widget frames) so the whole panel reads as one consistent shape
// language instead of the previous 12–14dp mismatch.
private val WIDGET_CORNER = 7.dp

// Outer panel shape/sizing. The panel used to be a hard 380dp with a square
// (0dp) outer boundary — square against the rest of a UI that's otherwise
// built entirely out of rounded cards reads as an unstyled leftover, and
// 380dp starts feeling tight once a hosted third-party widget is in the mix.
// PANEL_CORNER is intentionally a bit larger than WIDGET_CORNER: the outer
// silhouette of a docked panel reads better slightly more rounded than the
// cards sitting inside it (same relationship as a phone's home-screen widget
// tray vs. the widgets inside it).
private val PANEL_CORNER = 12.dp
private val PANEL_MIN_WIDTH = 420.dp
private val PANEL_MAX_WIDTH = 640.dp

// Fixed Bluebird Fluent accent shared by the widget shell.
private val LocalWidgetAccent = staticCompositionLocalOf { bluebirdColors.AccentBlue }

// ── Compact/comfortable density, same CompositionLocal approach. Comfortable
// matches the original spacing; compact tightens card padding for people who
// want more widgets visible at once.
private enum class WidgetDensity { COMFORTABLE, COMPACT }
private val LocalWidgetDensity = staticCompositionLocalOf { WidgetDensity.COMFORTABLE }
private val WidgetDensity.cardPadding get() = if (this == WidgetDensity.COMPACT) 12.dp else 18.dp
private val WidgetDensity.itemSpacing get() = if (this == WidgetDensity.COMPACT) 10.dp else 14.dp

/** True density-scale multiplier applied on top of the user's accessibility text scale. */
private val WidgetDensity.textScaleMultiplier get() = if (this == WidgetDensity.COMPACT) 0.92f else 1f

// ─── Shared card wrapper ──────────────────────────────────────────────────────

@Composable
private fun WidgetCard(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val border = if (isDark) Color.White.copy(alpha = 0.09f) else Color.Black.copy(alpha = 0.08f)
    val shape = RoundedCornerShape(7.dp)
    val density = LocalWidgetDensity.current
    val surfaceColor = if (isDark) ShellCardDark else ShellCardLight

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(surfaceColor)
            .border(1.dp, border, shape)
    ) {
        Column(modifier = Modifier.padding(density.cardPadding), content = content)
    }
}

/**
 * A small "Show more / Show less" affordance for widgets whose list is
 * capped by default (Markets, News, …). Keeps the panel scannable while
 * still letting the full data be reached without a separate screen.
 */
@Composable
private fun ExpandToggle(expanded: Boolean, textScale: Float, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onToggle)
            .padding(top = 6.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (expanded) stringResource(R.string.show_less) else stringResource(R.string.show_more),
            color = LocalWidgetAccent.current,
            fontSize = (11 * textScale).sp,
            fontWeight = FontWeight.Medium
        )
        Icon(
            imageVector = if (expanded) FluentIcon.ChevronUp else FluentIcon.ChevronDown,
            contentDescription = null,
            tint = LocalWidgetAccent.current,
            modifier = Modifier.size(14.dp).padding(start = 2.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────
// 1. Clock Widget (NEW)
// ─────────────────────────────────────────────────────────
@Composable
private fun ClockWidget(isDark: Boolean, context: Context, textScale: Float) {
    val textColor  = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    val windowRuntime = LocalWindowRuntime.current

    // A hidden widget does not need second-by-second Compose invalidations.
    // Keep the clock correct when restored, but sleep cheaply while minimized.
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(windowRuntime.isMinimized) {
        while (isActive) {
            now = System.currentTimeMillis()
            delay(if (windowRuntime.isMinimized) 5000 else 1000)
        }
    }

    val localTime = remember(now) {
        SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(Date(now))
    }
    val localDate = remember(now) {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date(now))
    }

    WidgetCard(isDark) {
        // Large local clock
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                localTime,
                fontSize   = (38 * textScale).sp,
                fontWeight = FontWeight.Light,
                color      = textColor,
                letterSpacing = 2.sp
            )
            Text(
                localDate,
                fontSize = (12 * textScale).sp,
                color    = textColor.copy(alpha = 0.6f)
            )
        }


    }
}

// ─────────────────────────────────────────────────────────
// 2. Weather Widget (real API via OpenWeatherMap free tier)
// ─────────────────────────────────────────────────────────
@Composable
private fun WeatherWidget(isDark: Boolean, context: Context, textScale: Float, refreshTick: Int) {
    var weatherData by remember { mutableStateOf<WeatherData?>(null) }
    var loading     by remember { mutableStateOf(true) }
    var error       by remember { mutableStateOf(false) }

    // ── 1. Track whether we have location permission ──────────────────────────
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    // ── 2. Permission launcher ────────────────────────────────────────────────
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasLocationPermission = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // ── 3. Ask for permission on first composition if not granted ─────────────
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // ── 4. Fetch weather once we have permission (or on refresh) ──────────────
    LaunchedEffect(refreshTick, hasLocationPermission) {
        if (!hasLocationPermission) { loading = false; return@LaunchedEffect }

        loading = true; error = false

        // Resolve current lat/lon on IO thread
        val location: Pair<Double, Double>? = withContext(Dispatchers.IO) {
            try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
                val provider = when {
                    lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)     -> android.location.LocationManager.GPS_PROVIDER
                    lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER) -> android.location.LocationManager.NETWORK_PROVIDER
                    else -> null
                } ?: return@withContext null

                @Suppress("MissingPermission")
                val loc = lm.getLastKnownLocation(provider) ?: return@withContext null
                Pair(loc.latitude, loc.longitude)
            } catch (e: Exception) { null }
        }

        if (location == null) { error = true; loading = false; return@LaunchedEffect }

        val (lat, lon) = location

        withContext(Dispatchers.IO) {
            try {
                val apiKey = "42b87f7284bf8558f040e20a112874b9"   // ← your key
                val url  = "https://api.openweathermap.org/data/2.5/forecast" +
                        "?lat=$lat&lon=$lon&units=imperial&cnt=5&appid=$apiKey"
                val raw  = URL(url).readText()
                val json = JSONObject(raw)
                val city = json.getJSONObject("city").getString("name")
                val list = json.getJSONArray("list")
                val current = list.getJSONObject(0)
                val temp    = current.getJSONObject("main").getDouble("temp").toInt()
                val high    = current.getJSONObject("main").getDouble("temp_max").toInt()
                val low     = current.getJSONObject("main").getDouble("temp_min").toInt()
                val cond    = current.getJSONArray("weather").getJSONObject(0).getString("main")
                val forecast = (0 until minOf(5, list.length())).map { i ->
                    val item  = list.getJSONObject(i)
                    val dt    = item.getLong("dt") * 1000
                    val day   = SimpleDateFormat("EEE", Locale.getDefault()).format(Date(dt))
                    val ic    = item.getJSONArray("weather").getJSONObject(0).getString("main")
                    val hi    = item.getJSONObject("main").getDouble("temp_max").toInt()
                    val lo    = item.getJSONObject("main").getDouble("temp_min").toInt()
                    val emoji = when {
                        ic.contains("Clear")   -> "☀️"
                        ic.contains("Cloud")   -> "⛅"
                        ic.contains("Rain")    -> "🌧"
                        ic.contains("Snow")    -> "❄️"
                        ic.contains("Thunder") -> "⛈"
                        else                   -> "🌤"
                    }
                    DayForecast(day, emoji, hi, lo)
                }
                withContext(Dispatchers.Main) {
                    weatherData = WeatherData(city, cond, temp, high, low, forecast)
                    loading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { error = true; loading = false }
            }
        }
    }

    // ── 5. UI — identical to before ──────────────────────────────────────────
    val gradientColors = when {
        weatherData?.condition?.contains("Clear") == true ||
                weatherData?.condition?.contains("Sunny") == true ->
            listOf(Color(0xFF1565C0), Color(0xFF42A5F5))
        weatherData?.condition?.contains("Cloud") == true ->
            listOf(Color(0xFF546E7A), Color(0xFF90A4AE))
        weatherData?.condition?.contains("Rain") == true ->
            listOf(Color(0xFF455A64), Color(0xFF607D8B))
        weatherData?.condition?.contains("Snow") == true ->
            listOf(Color(0xFF78909C), Color(0xFFB0BEC5))
        else -> listOf(Color(0xFF1565C0), Color(0xFF42A5F5))
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(gradientColors))
                .padding(16.dp)
        ) {
            when {
                !hasLocationPermission -> Box(
                    Modifier.fillMaxWidth().height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = FluentIcon.LocationOff, contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(28.dp))
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(R.string.location_permission_needed),
                            color = Color.White.copy(alpha = 0.7f), fontSize = (11 * textScale).sp)
                    }
                }
                loading -> Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                }
                error -> Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = FluentIcon.CloudOff, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(28.dp))
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(R.string.could_not_load_weather), color = Color.White.copy(alpha = 0.7f), fontSize = (11 * textScale).sp)
                    }
                }
                weatherData != null -> {
                    val data = weatherData!!
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column {
                                Text(data.city, color = Color.White, fontSize = (15 * textScale).sp, fontWeight = FontWeight.SemiBold)
                                Text(data.condition, color = Color.White.copy(alpha = 0.8f), fontSize = (12 * textScale).sp)
                                Spacer(Modifier.height(8.dp))
                                Text("${data.temp}°", fontSize = (48 * textScale).sp, color = Color.White, fontWeight = FontWeight.Light)
                                Text(stringResource(R.string.weather_high_low, data.high, data.low), color = Color.White.copy(alpha = 0.7f), fontSize = (11 * textScale).sp)
                            }
                            Icon(
                                when {
                                    data.condition.contains("Clear") || data.condition.contains("Sunny") -> FluentIcon.WeatherSunny
                                    data.condition.contains("Cloud") -> FluentIcon.WeatherCloudy
                                    data.condition.contains("Rain")  -> FluentIcon.WeatherRain
                                    else -> FluentIcon.WeatherSunny
                                },
                                null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(56.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            items(data.forecast) { day ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 12.dp)) {
                                    Text(day.day, color = Color.White.copy(alpha = 0.7f), fontSize = (11 * textScale).sp)
                                    Text(day.icon, fontSize = (16 * textScale).sp)
                                    Text("${day.high}°", color = Color.White, fontSize = (11 * textScale).sp)
                                    Text("${day.low}°", color = Color.White.copy(alpha = 0.5f), fontSize = (10 * textScale).sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
// ─────────────────────────────────────────────────────────
// 3. Now Playing Widget (MediaSession) — NEW
// ─────────────────────────────────────────────────────────
@Composable
private fun NowPlayingWidget(isDark: Boolean, context: Context, textScale: Float) {
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight

    var title    by remember { mutableStateOf<String?>(null) }
    var artist   by remember { mutableStateOf<String?>(null) }
    var albumArt by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var controller by remember { mutableStateOf<android.media.session.MediaController?>(null) }

    // Check notification listener permission (needed for MediaSessionManager).
    // This widget observes the active-session list instead of taking a one-time snapshot.
    val hasPermission = remember {
        val enabled = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: ""
        enabled.contains(context.packageName)
    }

    DisposableEffect(hasPermission) {
        if (!hasPermission) {
            onDispose { }
        } else {
            val msm = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            if (msm == null) {
                onDispose { }
            } else {
                val component = android.content.ComponentName(
                    context,
                    io.github.norbertweb.bluebird.data.NotificationListener::class.java
                )

                var registeredController: android.media.session.MediaController? = null

                val controllerCallback = object : android.media.session.MediaController.Callback() {
                    override fun onMetadataChanged(metadata: android.media.MediaMetadata?) {
                        title = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE)
                        artist = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST)
                        albumArt = metadata?.getBitmap(android.media.MediaMetadata.METADATA_KEY_ALBUM_ART)
                    }

                    override fun onPlaybackStateChanged(
                        state: android.media.session.PlaybackState?
                    ) {
                        isPlaying = state?.state == android.media.session.PlaybackState.STATE_PLAYING
                    }
                }

                fun attach(active: android.media.session.MediaController?) {
                    if (registeredController === active) return
                    registeredController?.unregisterCallback(controllerCallback)
                    registeredController = active
                    controller = active
                    active?.registerCallback(controllerCallback)

                    val metadata = active?.metadata
                    title = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE)
                    artist = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST)
                    albumArt = metadata?.getBitmap(android.media.MediaMetadata.METADATA_KEY_ALBUM_ART)
                    isPlaying = active?.playbackState?.state ==
                        android.media.session.PlaybackState.STATE_PLAYING
                }

                val listener = MediaSessionManager.OnActiveSessionsChangedListener { sessions ->
                    attach(sessions?.firstOrNull())
                }

                try {
                    attach(msm.getActiveSessions(component).firstOrNull())
                    msm.addOnActiveSessionsChangedListener(listener, component)
                } catch (_: SecurityException) {
                    // Permission can disappear while the settings screen is open.
                } catch (_: IllegalArgumentException) {
                    // Listener registration can fail if the notification service is unavailable.
                }

                onDispose {
                    try { msm.removeOnActiveSessionsChangedListener(listener) } catch (_: Exception) {}
                    registeredController?.unregisterCallback(controllerCallback)
                    registeredController = null
                    controller = null
                    albumArt = null
                }
            }
        }
    }

    val albumImage = remember(albumArt) { albumArt?.asImageBitmap() }

    WidgetCard(isDark) {
        WidgetHeader(stringResource(R.string.now_playing), FluentIcon.MusicNote2, textColor, textScale)
        Spacer(Modifier.height(10.dp))

        if (!hasPermission) {
            TextButton(onClick = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }) { Text(stringResource(R.string.grant_notification_access), fontSize = (12 * textScale).sp) }
        } else if (title == null) {
            Text(stringResource(R.string.nothing_playing), color = textColor.copy(alpha = 0.5f), fontSize = (13 * textScale).sp, fontWeight = FontWeight.Medium)
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Album art
                if (albumArt != null) {
                    Image(
                        bitmap = albumImage!!,
                        contentDescription = null,
                        modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp))
                            .background(LocalWidgetAccent.current.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(imageVector = FluentIcon.MusicNote2, contentDescription = null, tint = LocalWidgetAccent.current, modifier = Modifier.size(24.dp)) }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(title ?: "", color = textColor, fontSize = (13 * textScale).sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(artist ?: "", color = textColor.copy(alpha = 0.6f), fontSize = (11 * textScale).sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(10.dp))
            // Playback controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val ctrl = controller
                IconButton(onClick = { ctrl?.transportControls?.skipToPrevious() }) {
                    Icon(imageVector = FluentIcon.SkipBack, contentDescription = null, tint = textColor, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier.size(42.dp).clip(CircleShape)
                        .background(LocalWidgetAccent.current)
                        .clickable {
                            if (isPlaying) ctrl?.transportControls?.pause()
                            else ctrl?.transportControls?.play()
                            isPlaying = !isPlaying
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = if (isPlaying) FluentIcon.Pause else FluentIcon.Play, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { ctrl?.transportControls?.skipToNext() }) {
                    Icon(imageVector = FluentIcon.SkipForward, contentDescription = null, tint = textColor, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
// 4. Stock / Markets Widget (real quotes via Stooq's public CSV endpoint —
//    no API key required; delayed data, refreshed on demand)
// ─────────────────────────────────────────────────────────
private val TRACKED_SYMBOLS = listOf("msft.us", "aapl.us", "googl.us", "amzn.us", "tsla.us", "nvda.us", "meta.us", "nflx.us")
private const val STOCKS_COLLAPSED_COUNT = 5

@Composable
private fun StockWidget(isDark: Boolean, textScale: Float, refreshTick: Int) {
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    var stocks   by remember { mutableStateOf<List<StockTicker>>(emptyList()) }
    var loading  by remember { mutableStateOf(true) }
    var error    by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(refreshTick) {
        loading = true
        val result = withContext(Dispatchers.IO) {
            try {
                val symbols = TRACKED_SYMBOLS.joinToString(",")
                val url = "https://stooq.com/q/l/?s=$symbols&f=sd2t2ohlc&h&e=csv"
                val lines = URL(url).readText().lines().drop(1).filter { it.isNotBlank() }
                lines.mapNotNull { line ->
                    val cols = line.split(",")
                    if (cols.size < 7) return@mapNotNull null
                    val symbol = cols[0].removeSuffix(".US")
                    val open  = cols[3].toDoubleOrNull() ?: return@mapNotNull null
                    val high  = cols[4].toDoubleOrNull() ?: open
                    val low   = cols[5].toDoubleOrNull() ?: open
                    val close = cols[6].toDoubleOrNull() ?: open
                    val changePct = if (open != 0.0) (close - open) / open * 100.0 else 0.0
                    // Real open/low/high/close shape (4 points) rather than a
                    // fabricated multi-day history — honest about what we have.
                    val lo = minOf(open, low, high, close)
                    val hi = maxOf(open, low, high, close).coerceAtLeast(lo + 0.01)
                    val points = listOf(open, low, high, close).map { ((it - lo) / (hi - lo)).toFloat() }
                    StockTicker(
                        symbol   = symbol.uppercase(),
                        change   = "${if (changePct >= 0) "+" else ""}${"%.2f".format(changePct)}%",
                        price    = "$" + "%.2f".format(close),
                        isUp     = changePct >= 0,
                        points   = points
                    )
                }
            } catch (e: Exception) { null }
        }
        if (result.isNullOrEmpty()) { error = stocks.isEmpty() } else { stocks = result; error = false }
        loading = false
    }

    WidgetCard(isDark) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.markets), color = textColor, fontSize = (15 * textScale).sp, fontWeight = FontWeight.SemiBold)
            if (loading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = LocalWidgetAccent.current)
            else Icon(imageVector = FluentIcon.ArrowTrendingUp, contentDescription = null, tint = LocalWidgetAccent.current, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(10.dp))
        when {
            error && stocks.isEmpty() -> Text(stringResource(R.string.could_not_load_markets), color = textColor.copy(alpha = 0.5f), fontSize = (12 * textScale).sp, fontWeight = FontWeight.Medium)
            else -> {
                val shown = if (expanded) stocks else stocks.take(STOCKS_COLLAPSED_COUNT)
                shown.forEach { stock ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(0.25f)) {
                        Text(stock.symbol, color = textColor, fontSize = (13 * textScale).sp, fontWeight = FontWeight.SemiBold)
                        Text(stock.price, color = textColor.copy(alpha = 0.5f), fontSize = (10 * textScale).sp, fontWeight = FontWeight.Medium)
                    }
                    Canvas(modifier = Modifier.weight(0.4f).height(22.dp)) {
                        val pts  = stock.points
                        val path = Path()
                        pts.forEachIndexed { i, yRatio ->
                            val x = i * (size.width / (pts.size - 1))
                            val y = size.height * (1 - yRatio)
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(path,
                            color = if (stock.isUp) bluebirdColors.SuccessGreen else bluebirdColors.DangerRed,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                    }
                    Text(
                        stock.change,
                        color      = if (stock.isUp) bluebirdColors.SuccessGreen else bluebirdColors.DangerRed,
                        fontSize   = (12 * textScale).sp,
                        fontWeight = FontWeight.Medium,
                        modifier   = Modifier.weight(0.25f)
                    )
                }
                }
                if (stocks.size > STOCKS_COLLAPSED_COUNT) {
                    ExpandToggle(expanded, textScale) { expanded = !expanded }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
// 6. News Widget (real headlines via BBC News' public RSS feed)
// ─────────────────────────────────────────────────────────
private const val NEWS_RSS_URL = "https://feeds.bbci.co.uk/news/rss.xml"

private const val NEWS_COLLAPSED_COUNT = 4

@Composable
private fun NewsWidget(isDark: Boolean, textScale: Float, refreshTick: Int) {
    val context   = LocalContext.current
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    var articles  by remember { mutableStateOf<List<NewsArticle>>(emptyList()) }
    var loading   by remember { mutableStateOf(true) }
    var expanded  by remember { mutableStateOf(false) }

    LaunchedEffect(refreshTick) {
        loading = true
        val fetched = withContext(Dispatchers.IO) {
            try { parseRssFeed(URL(NEWS_RSS_URL).readText()) } catch (e: Exception) { null }
        }
        if (fetched.isNullOrEmpty()) {
            if (articles.isEmpty()) { articles = emptyList() }
        } else {
            articles = fetched
        }
        loading = false
    }

    WidgetCard(isDark) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.news), color = textColor, fontSize = (15 * textScale).sp, fontWeight = FontWeight.SemiBold)
            if (loading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = LocalWidgetAccent.current)
            else Icon(imageVector = FluentIcon.DocumentText, contentDescription = null, tint = LocalWidgetAccent.current, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(10.dp))
        val shownArticles = if (expanded) articles.take(8) else articles.take(NEWS_COLLAPSED_COUNT)
        shownArticles.forEach { article ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(article.url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }.padding(vertical = 6.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp)).background(LocalWidgetAccent.current.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Icon(imageVector = FluentIcon.DocumentText, contentDescription = null, tint = LocalWidgetAccent.current, modifier = Modifier.size(22.dp)) }
                Column(modifier = Modifier.weight(1f)) {
                    Text(article.title, color = textColor, fontWeight = FontWeight.Medium,
                        fontSize = (12 * textScale).sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(2.dp))
                    Text(article.source, fontSize = (10 * textScale).sp, color = textColor.copy(alpha = 0.5f), fontWeight = FontWeight.Medium)
                }
            }
        }
        if (articles.size > NEWS_COLLAPSED_COUNT) {
            ExpandToggle(expanded, textScale) { expanded = !expanded }
        }
    }
}

/** Minimal RSS 2.0 parser — good enough for standard <item><title>/<link> feeds. */
private fun parseRssFeed(xml: String): List<NewsArticle> {
    val items = xml.split("<item>").drop(1)
    return items.mapNotNull { item ->
        val title = Regex("<title>(.*?)</title>", RegexOption.DOT_MATCHES_ALL)
            .find(item)?.groupValues?.get(1)
            ?.replace("<![CDATA[", "")?.replace("]]>", "")?.trim() ?: return@mapNotNull null
        val link = Regex("<link>(.*?)</link>", RegexOption.DOT_MATCHES_ALL)
            .find(item)?.groupValues?.get(1)?.trim() ?: return@mapNotNull null
        NewsArticle(title = title, source = "BBC News", url = link)
    }
}



// ─────────────────────────────────────────────────────────
// 7. Calendar Widget (real events, fixed permission refresh)
// ─────────────────────────────────────────────────────────
@Composable
private fun CalendarWidget(isDark: Boolean, context: Context, textScale: Float) {
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight

    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED)
    }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }

    var events by remember { mutableStateOf<List<CalendarEvent>>(emptyList()) }
    LaunchedEffect(hasPermission) {
        if (!hasPermission) return@LaunchedEffect
        events = withContext(Dispatchers.IO) { getUpcomingEvents(context, 5) }
    }

    WidgetCard(isDark) {
        WidgetHeader(stringResource(R.string.appname_calendar), FluentIcon.Calendar, textColor, textScale)
        Spacer(Modifier.height(10.dp))
        when {
            !hasPermission -> TextButton(onClick = { permLauncher.launch(Manifest.permission.READ_CALENDAR) }) {
                Text(stringResource(R.string.grant_calendar_access), fontSize = (12 * textScale).sp)
            }
            events.isEmpty() -> Text(stringResource(R.string.no_upcoming_events), color = textColor.copy(alpha = 0.5f), fontSize = (12 * textScale).sp, fontWeight = FontWeight.Medium)
            else -> events.forEach { event ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.width(3.dp).height(28.dp).background(event.color, RoundedCornerShape(2.dp)))
                    Column {
                        Text(event.title, color = textColor, fontSize = (12 * textScale).sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                        Text(event.time, color = textColor.copy(alpha = 0.55f), fontSize = (10 * textScale).sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

private fun getUpcomingEvents(context: Context, maxResults: Int): List<CalendarEvent> {
    val events = mutableListOf<CalendarEvent>()
    val projection = arrayOf(CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART, CalendarContract.Events.EVENT_COLOR)
    val now = System.currentTimeMillis()
    val selection = "(${CalendarContract.Events.DTSTART} >= ?) AND (${CalendarContract.Events.DELETED} = 0)"
    try {
        context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI, projection, selection, arrayOf(now.toString()),
            "${CalendarContract.Events.DTSTART} ASC LIMIT $maxResults"
        )?.use {
            while (it.moveToNext()) {
                val title = it.getString(0) ?: "Event"
                val start = it.getLong(1)
                val color = try { Color(it.getInt(2)) } catch (e: Exception) { bluebirdColors.AccentBlue }
                events.add(CalendarEvent(title, SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(start)), color))
            }
        }
    } catch (e: SecurityException) { }
    return events
}

// ─────────────────────────────────────────────────────────
// 8. Photos Widget (fixed permission refresh)
// ─────────────────────────────────────────────────────────
@Composable
private fun PhotosWidget(
    isDark: Boolean,
    context: Context,
    textScale: Float,
    onOpenPhoto: (Uri) -> Unit = {}
) {
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight

    // Mirrors ImageViewerScreen's hasImageAccess(): on API 34+ a user can grant
    // "Select photos" (READ_MEDIA_VISUAL_USER_SELECTED) instead of full access.
    // Checking only READ_MEDIA_IMAGES meant this widget got stuck on the
    // permission prompt (or silently returned nothing) for anyone who picked
    // that option in the viewer, even though the viewer itself works fine.
    fun hasPhotoAccess(): Boolean = when {
        Build.VERSION.SDK_INT >= 34 ->
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED") ==
                PackageManager.PERMISSION_GRANTED
        Build.VERSION.SDK_INT >= 33 ->
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) ==
                PackageManager.PERMISSION_GRANTED
        else ->
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
    }

    var hasPermission by remember { mutableStateOf(hasPhotoAccess()) }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        hasPermission = hasPhotoAccess()
    }
    var photos by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val windowRuntime = LocalWindowRuntime.current
    LaunchedEffect(hasPermission) {
        if (!hasPermission) return@LaunchedEffect
        // Keep refreshing while the panel is visible so a newly taken/imported
        // photo actually shows up here instead of the list freezing at
        // whatever was recent the first time permission was granted.
        while (isActive) {
            photos = withContext(Dispatchers.IO) { getRecentPhotos(context, 10) }
            delay(if (windowRuntime.isMinimized) 15000 else 4000)
        }
    }

    WidgetCard(isDark) {
        WidgetHeader(stringResource(R.string.photos_2), FluentIcon.Image, textColor, textScale)
        Spacer(Modifier.height(10.dp))
        when {
            !hasPermission -> TextButton(onClick = {
                val perms = when {
                    Build.VERSION.SDK_INT >= 34 -> arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
                    )
                    Build.VERSION.SDK_INT >= 33 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
                    else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                permLauncher.launch(perms)
            }) {
                Text(stringResource(R.string.grant_photo_access), fontSize = (12 * textScale).sp)
            }
            photos.isEmpty() -> Text(stringResource(R.string.no_recent_photos), color = textColor.copy(alpha = 0.5f), fontSize = (12 * textScale).sp, fontWeight = FontWeight.Medium)
            else -> LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(photos) { uri ->
                    AsyncImage(
                        model = uri, contentDescription = null,
                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .clickable { onOpenPhoto(uri) }
                    )
                }
            }
        }
    }
}

private fun getRecentPhotos(context: Context, maxCount: Int): List<Uri> {
    val uris       = mutableListOf<Uri>()
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    try {
        context.contentResolver.query(
            collection,
            arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_MODIFIED),
            null, null,
            "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            val col = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            var n = 0
            while (cursor.moveToNext() && n < maxCount) {
                uris.add(ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getLong(col)))
                n++
            }
        }
    } catch (e: SecurityException) { }
    return uris
}

// Sports and Traffic widgets were removed: both only ever rendered static,
// hardcoded sample data (fake scores, a fixed "25 min to home"), and neither
// has a real data source available without a paid/keyed API (live sports
// scores, and Google's Directions/Traffic API respectively). Rather than
// ship something that looks live but never changes, they're gone. If you
// get an API key for either later (e.g. TheSportsDB, Google Maps Platform),
// they can follow the same real-fetch pattern now used by Weather/Stocks/News.

// ─────────────────────────────────────────────────────────
// 11. To‑Do Widget (persistent, swipe-to-delete, priorities, due dates)
// ─────────────────────────────────────────────────────────

private enum class TodoFilter(val label: String) { ALL("All"), ACTIVE("Active"), DONE("Done") }

/** Calendar-day granularity due-date helpers — no time-of-day, just "which day". */
private fun startOfDay(millis: Long = System.currentTimeMillis()): Long =
    Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

private fun daysFromToday(dueAt: Long): Int {
    val today = startOfDay()
    val due = startOfDay(dueAt)
    return ((due - today) / (24 * 60 * 60 * 1000L)).toInt()
}

@Composable
private fun dueLabel(dueAt: Long): String = when (val d = daysFromToday(dueAt)) {
    0 -> stringResource(R.string.today)
    1 -> stringResource(R.string.tomorrow)
    -1 -> stringResource(R.string.yesterday)
    in -6..-2 -> stringResource(R.string.overdue_days, -d)
    in 2..6 -> SimpleDateFormat("EEE", Locale.getDefault()).format(Date(dueAt))
    else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(dueAt))
}

private fun dueColor(dueAt: Long, done: Boolean, textColor: Color): Color {
    if (done) return textColor.copy(alpha = 0.35f)
    val d = daysFromToday(dueAt)
    return when {
        d < 0  -> bluebirdColors.Error
        d == 0 -> TaskPriority.MEDIUM.color
        else   -> textColor.copy(alpha = 0.5f)
    }
}

@Composable
private fun TodoWidget(isDark: Boolean, context: Context, textScale: Float) {
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    var tasks         by remember { mutableStateOf(loadTodos(context)) }
    var newTaskText   by remember { mutableStateOf("") }
    var showAddField  by remember { mutableStateOf(false) }
    var filter        by remember {
        mutableStateOf(
            try { TodoFilter.valueOf(loadTodoFilter(context).uppercase()) }
            catch (e: Exception) { TodoFilter.ALL }
        )
    }
    var sortByPriority by remember { mutableStateOf(loadTodoSortByPriority(context)) }
    var menuForTaskId by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }

    fun save(updated: List<TodoTask>) { tasks = updated; saveTodos(context, updated) }
    fun submitNewTask() {
        if (newTaskText.isNotBlank()) {
            save(tasks + TodoTask(text = newTaskText.trim(), done = false))
            newTaskText = ""
        }
    }

    LaunchedEffect(showAddField) {
        if (showAddField) { delay(80); focusRequester.requestFocus() }
    }

    val displayedTasks = remember(tasks, filter, sortByPriority) {
        val filtered = when (filter) {
            TodoFilter.ALL    -> tasks
            TodoFilter.ACTIVE -> tasks.filterNot { it.done }
            TodoFilter.DONE   -> tasks.filter { it.done }
        }
        if (sortByPriority) {
            filtered.sortedWith(
                compareBy<TodoTask> { it.done }
                    .thenByDescending { it.priority.ordinal }
                    .thenBy { it.dueAt ?: Long.MAX_VALUE }
            )
        } else {
            filtered.sortedBy { it.done }
        }
    }

    WidgetCard(isDark) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.to_do), color = textColor, fontSize = (15 * textScale).sp, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                val done = tasks.count { it.done }
                if (tasks.isNotEmpty()) {
                    Text("$done/${tasks.size}", color = textColor.copy(alpha = 0.45f), fontSize = (11 * textScale).sp,
                        modifier = Modifier.padding(end = 4.dp))
                }
                // Priority-sort toggle — sorts by priority (high first) then due
                // date, with completed tasks always sinking to the bottom.
                IconButton(
                    onClick = {
                        sortByPriority = !sortByPriority
                        saveTodoSortByPriority(context, sortByPriority)
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = FluentIcon.ArrowSort,
                        contentDescription = if (sortByPriority) stringResource(R.string.sorting_by_priority) else stringResource(R.string.sort_by_priority),
                        tint = if (sortByPriority) LocalWidgetAccent.current else textColor.copy(alpha = 0.4f),
                        modifier = Modifier.size(15.dp)
                    )
                }
                IconButton(onClick = { showAddField = !showAddField }, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (showAddField) FluentIcon.Dismiss else FluentIcon.Add,
                        contentDescription = if (showAddField) stringResource(R.string.cancel) else stringResource(R.string.add_task),
                        tint = LocalWidgetAccent.current, modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        if (tasks.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (tasks.isEmpty()) 0f else tasks.count { it.done }.toFloat() / tasks.size },
                modifier   = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color      = LocalWidgetAccent.current,
                trackColor = widgetDividerColor(textColor)
            )
            Spacer(Modifier.height(10.dp))

            // ── Filter tabs: All / Active / Done ──
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TodoFilter.values().forEach { f ->
                    val selected = f == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) LocalWidgetAccent.current.copy(alpha = 0.16f) else Color.Transparent)
                            .clickable {
                                filter = f
                                saveTodoFilter(context, f.name)
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            ui(f.label),
                            color = if (selected) LocalWidgetAccent.current else textColor.copy(alpha = 0.5f),
                            fontSize = (11 * textScale).sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))

        if (tasks.isEmpty() && !showAddField) {
            Text(stringResource(R.string.nothing_on_your_list_tap_to_add_a_task), color = textColor.copy(alpha = 0.45f), fontSize = (12 * textScale).sp, fontWeight = FontWeight.Medium)
        } else if (displayedTasks.isEmpty()) {
            Text(
                if (filter == TodoFilter.DONE) stringResource(R.string.no_completed_tasks_yet) else stringResource(R.string.nothing_active_nice_work),
                color = textColor.copy(alpha = 0.45f), fontSize = (12 * textScale).sp, fontWeight = FontWeight.Medium
            )
        }

        displayedTasks.forEach { task ->
            var offsetX   by remember(task.id) { mutableFloatStateOf(0f) }
            var dismissed by remember(task.id) { mutableStateOf(false) }
            if (dismissed) {
                LaunchedEffect(task.id) { save(tasks.filterNot { it.id == task.id }) }
                return@forEach
            }
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                // Delete bg
                if (offsetX < -30f) {
                    Box(
                        modifier = Modifier.matchParentSize().clip(RoundedCornerShape(10.dp))
                            .background(bluebirdColors.Error.copy(alpha = (-offsetX / 200f).coerceIn(0f, 1f))),
                        contentAlignment = Alignment.CenterEnd
                    ) { Icon(imageVector = FluentIcon.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.padding(end = 12.dp).size(16.dp)) }
                }
                Row(
                    modifier = Modifier
                        .offset { IntOffset(offsetX.toInt(), 0) }
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (task.done) Color.Transparent else (if (isDark) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.02f)))
                        .pointerInput(task.id) {
                            detectHorizontalDragGestures(
                                onDragEnd = { if (offsetX < -160f) dismissed = true else offsetX = 0f },
                                onHorizontalDrag = { _, delta -> offsetX = (offsetX + delta).coerceAtMost(0f) }
                            )
                        }
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Priority bar — tap to cycle NONE → Low → Medium → High.
                    // A thin color bar (rather than a badge) keeps the row
                    // scannable at a glance, the same language Things/Todoist use.
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(28.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (task.priority == TaskPriority.NONE) widgetDividerColor(textColor) else task.priority.color)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { save(tasks.map { if (it.id == task.id) it.copy(priority = it.priority.next()) else it }) }
                    )
                    Checkbox(
                        checked = task.done,
                        onCheckedChange = { checked -> save(tasks.map { if (it.id == task.id) it.copy(done = checked) else it }) },
                        colors   = CheckboxDefaults.colors(checkedColor = LocalWidgetAccent.current, checkmarkColor = Color.White),
                        modifier = Modifier.size(21.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            task.text,
                            color          = if (task.done) textColor.copy(alpha = 0.35f) else textColor,
                            fontSize       = (13 * textScale).sp,
                            fontWeight     = FontWeight.Medium,
                            textDecoration = if (task.done) TextDecoration.LineThrough else null,
                            maxLines = 2, overflow = TextOverflow.Ellipsis
                        )
                        if (task.dueAt != null) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Icon(
                                    imageVector = FluentIcon.Calendar, contentDescription = null,
                                    tint = dueColor(task.dueAt, task.done, textColor), modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    dueLabel(task.dueAt),
                                    color = dueColor(task.dueAt, task.done, textColor),
                                    fontSize = (10 * textScale).sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    Box {
                        // Using a plain "⋮" glyph rather than an overflow icon here —
                        // FluentIcon.MoreVertical (or equivalent) isn't used anywhere
                        // else in this file, so its exact name in your FluentIcon
                        // wrapper is unverified. Swap this Text for
                        // Icon(imageVector = FluentIcon.<YourOverflowIcon>, ...)
                        // once you've confirmed the right name.
                        IconButton(onClick = { menuForTaskId = task.id }, modifier = Modifier.size(24.dp)) {
                            Text(
                                "\u22EE",
                                color = textColor.copy(alpha = 0.35f),
                                fontSize = (15 * textScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        BluebirdDropdownMenu(expanded = menuForTaskId == task.id, onDismissRequest = { menuForTaskId = null }) {
                            Text(
                                stringResource(R.string.priority), color = textColor.copy(alpha = 0.4f), fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                            TaskPriority.values().forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(ui(p.label), fontSize = 13.sp) },
                                    leadingIcon = {
                                        Box(Modifier.size(10.dp).clip(CircleShape).background(if (p == TaskPriority.NONE) widgetDividerColor(textColor) else p.color))
                                    },
                                    onClick = {
                                        save(tasks.map { if (it.id == task.id) it.copy(priority = p) else it })
                                        menuForTaskId = null
                                    }
                                )
                            }
                            HorizontalDivider(color = widgetDividerColor(textColor), thickness = 0.5.dp)
                            Text(
                                stringResource(R.string.due_date), color = textColor.copy(alpha = 0.4f), fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                            val dueOptions = listOf(
                                "Today" to 0, "Tomorrow" to 1, "Next week" to 7
                            )
                            dueOptions.forEach { (label, offsetDays) ->
                                DropdownMenuItem(
                                    text = { Text(ui(label), fontSize = 13.sp) },
                                    onClick = {
                                        val due = startOfDay() + offsetDays * 24L * 60 * 60 * 1000
                                        save(tasks.map { if (it.id == task.id) it.copy(dueAt = due) else it })
                                        menuForTaskId = null
                                    }
                                )
                            }
                            if (task.dueAt != null) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.clear_due_date), fontSize = 13.sp, color = textColor.copy(alpha = 0.6f)) },
                                    onClick = {
                                        save(tasks.map { if (it.id == task.id) it.copy(dueAt = null) else it })
                                        menuForTaskId = null
                                    }
                                )
                            }
                            HorizontalDivider(color = widgetDividerColor(textColor), thickness = 0.5.dp)
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.delete_task), fontSize = 13.sp, color = bluebirdColors.Error) },
                                onClick = {
                                    save(tasks.filterNot { it.id == task.id })
                                    menuForTaskId = null
                                }
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = showAddField) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = newTaskText, onValueChange = { newTaskText = it },
                    modifier = Modifier.weight(1f).focusRequester(focusRequester), singleLine = true,
                    placeholder = { Text(stringResource(R.string.new_task), fontSize = (12 * textScale).sp) },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = (13 * textScale).sp, color = textColor),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = {
                        submitNewTask(); showAddField = false
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LocalWidgetAccent.current,
                        unfocusedBorderColor = widgetDividerColor(textColor)
                    )
                )
                IconButton(onClick = { submitNewTask(); showAddField = false }, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = FluentIcon.Checkmark, contentDescription = stringResource(R.string.save_task), tint = bluebirdColors.SuccessGreen)
                }
            }
        }

        if (tasks.any { it.done }) {
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.clear_completed),
                color = textColor.copy(alpha = 0.45f),
                fontSize = (10 * textScale).sp,
                modifier = Modifier.clickable { save(tasks.filterNot { it.done }) }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────
// 11b. Sticky Notes Widget — NEW
// ─────────────────────────────────────────────────────────
@Composable
private fun StickyNotesWidget(isDark: Boolean, context: Context, textScale: Float) {
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    var notes        by remember { mutableStateOf(loadStickyNotes(context)) }
    var showAdd      by remember { mutableStateOf(false) }
    var newNoteText  by remember { mutableStateOf("") }
    var newNoteColor by remember { mutableStateOf(StickyNoteColors.default) }
    val focusRequester = remember { FocusRequester() }

    fun save(updated: List<StickyNote>) { notes = updated; saveStickyNotes(context, updated) }

    LaunchedEffect(showAdd) {
        if (showAdd) { delay(80); focusRequester.requestFocus() }
    }

    WidgetCard(isDark) {
        // Reusing FluentIcon.Pin here rather than introducing a new icon —
        // it's already proven to exist in this icon set and "pinned note"
        // is a reasonable read; swap for a dedicated notepad glyph if one's
        // available in your FluentIcon wrapper.
        WidgetHeader(stringResource(R.string.sticky_notes), FluentIcon.Pin, textColor, textScale)
        Spacer(Modifier.height(10.dp))

        if (notes.isEmpty() && !showAdd) {
            Text(stringResource(R.string.no_notes_yet_tap_to_jot_something_down), color = textColor.copy(alpha = 0.45f), fontSize = (12 * textScale).sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
        }

        // Simple 2-column grid, built with chunked() rather than a Flow layout
        // so this compiles against older Compose foundation versions too.
        // Alternating tilt direction per note (based on a stable global index,
        // not per-row position) is what sells the "scattered on a corkboard"
        // feel rather than a rigid, uniform grid.
        notes.withIndex().toList().chunked(2).forEach { rowNotes ->
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowNotes.forEach { (globalIndex, note) ->
                    StickyNoteCard(
                        note = note,
                        tilt = if (globalIndex % 2 == 0) -2f else 2f,
                        textScale = textScale,
                        onDelete = { save(notes.filterNot { it.id == note.id }) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowNotes.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }

        if (showAdd) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(stickyColor(newNoteColor))
                    .padding(10.dp)
            ) {
                BasicTextField(
                    value = newNoteText,
                    onValueChange = { newNoteText = it },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).heightIn(min = 48.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = (13 * textScale).sp, color = Color(0xFF3E3A2E), fontWeight = FontWeight.Medium),
                    decorationBox = { inner ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (newNoteText.isEmpty()) {
                                Text(stringResource(R.string.write_a_note), fontSize = (13 * textScale).sp, color = Color(0xFF3E3A2E).copy(alpha = 0.45f))
                            }
                            inner()
                        }
                    }
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StickyNoteColors.palette.forEach { hex ->
                            val selected = hex == newNoteColor
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(stickyColor(hex))
                                    .then(
                                        if (selected) Modifier.border(2.dp, Color(0xFF3E3A2E).copy(alpha = 0.5f), CircleShape)
                                        else Modifier
                                    )
                                    .clickable { newNoteColor = hex }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { showAdd = false; newNoteText = "" }, modifier = Modifier.size(26.dp)) {
                            Icon(imageVector = FluentIcon.Dismiss, contentDescription = stringResource(R.string.cancel), tint = Color(0xFF3E3A2E).copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                        }
                        IconButton(
                            onClick = {
                                if (newNoteText.isNotBlank()) {
                                    save(notes + StickyNote(UUID.randomUUID().toString(), newNoteText.trim(), newNoteColor))
                                }
                                showAdd = false; newNoteText = ""; newNoteColor = StickyNoteColors.default
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(imageVector = FluentIcon.Checkmark, contentDescription = stringResource(R.string.save_note), tint = Color(0xFF3E3A2E))
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LocalWidgetAccent.current.copy(alpha = 0.08f))
                    .border(1.dp, LocalWidgetAccent.current.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .clickable { showAdd = true }
                    .padding(vertical = 9.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = FluentIcon.Add, contentDescription = null, tint = LocalWidgetAccent.current, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.new_note), color = LocalWidgetAccent.current, fontSize = (12 * textScale).sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/** A single pastel sticky note — slight rotation and drop shadow for a physical, tactile feel. */
@Composable
private fun StickyNoteCard(
    note: StickyNote,
    tilt: Float,
    textScale: Float,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ink = Color(0xFF3E3A2E)
    Box(
        modifier = modifier
            .graphicsLayer { rotationZ = tilt }
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(4.dp), ambientColor = Color.Black.copy(alpha = 0.25f), spotColor = Color.Black.copy(alpha = 0.25f))
            .clip(RoundedCornerShape(4.dp))
            .background(stickyColor(note.colorHex))
            .heightIn(min = 70.dp)
            .padding(8.dp)
    ) {
        Text(
            note.text,
            color = ink,
            fontSize = (11 * textScale).sp,
            fontWeight = FontWeight.Medium,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            lineHeight = (14 * textScale).sp
        )
        Icon(
            imageVector = FluentIcon.Dismiss,
            contentDescription = stringResource(R.string.delete_note),
            tint = ink.copy(alpha = 0.4f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(14.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDelete() }
        )
    }
}

// ─────────────────────────────────────────────────────────
// 12. Upcoming Alarm Widget — NEW
// ─────────────────────────────────────────────────────────
@Composable
private fun AlarmWidget(isDark: Boolean, context: Context, textScale: Float) {
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight

    val nextAlarm = remember {
        try {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.nextAlarmClock?.triggerTime?.let { triggerMs ->
                SimpleDateFormat("h:mm a, EEE MMM d", Locale.getDefault()).format(Date(triggerMs))
            }
        } catch (e: Exception) { null }
    }

    WidgetCard(isDark) {
        WidgetHeader(stringResource(R.string.next_alarm), FluentIcon.Alarm, textColor, textScale)
        Spacer(Modifier.height(10.dp))
        if (nextAlarm == null) {
            Text(stringResource(R.string.no_alarm_set), color = textColor.copy(alpha = 0.5f), fontSize = (12 * textScale).sp, fontWeight = FontWeight.Medium)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = FluentIcon.Alarm, contentDescription = null, tint = LocalWidgetAccent.current, modifier = Modifier.size(20.dp))
                Text(nextAlarm, color = textColor, fontSize = (14 * textScale).sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
// 13. Network Speed Widget — NEW
// ─────────────────────────────────────────────────────────
@Composable
private fun NetworkSpeedWidget(isDark: Boolean, textScale: Float) {
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    val windowRuntime = LocalWindowRuntime.current
    var rxSpeed by remember { mutableStateOf("— KB/s") }
    var txSpeed by remember { mutableStateOf("— KB/s") }

    LaunchedEffect(Unit) {
        var prevRx = TrafficStats.getTotalRxBytes()
        var prevTx = TrafficStats.getTotalTxBytes()
        while (isActive) {
            // Hidden widgets should not poll at dashboard cadence.
            delay(if (windowRuntime.isMinimized) 10000 else 2000)
            val curRx = TrafficStats.getTotalRxBytes()
            val curTx = TrafficStats.getTotalTxBytes()
            val diffRx = (curRx - prevRx).coerceAtLeast(0)
            val diffTx = (curTx - prevTx).coerceAtLeast(0)
            rxSpeed = formatSpeed(diffRx)
            txSpeed = formatSpeed(diffTx)
            prevRx = curRx; prevTx = curTx
        }
    }

    WidgetCard(isDark) {
        WidgetHeader(stringResource(R.string.network_speed), FluentIcon.TopSpeed, textColor, textScale)
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = FluentIcon.ArrowDownload, contentDescription = null, tint = LocalWidgetAccent.current, modifier = Modifier.size(14.dp))
                    Text(stringResource(R.string.download), color = textColor.copy(alpha = 0.55f), fontSize = (10 * textScale).sp, fontWeight = FontWeight.Medium)
                }
                Text(rxSpeed, color = textColor, fontSize = (15 * textScale).sp, fontWeight = FontWeight.SemiBold)
            }
            Box(modifier = Modifier.width(1.dp).height(36.dp).background(widgetDividerColor(textColor)))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = FluentIcon.ArrowUpload, contentDescription = null, tint = bluebirdColors.Success, modifier = Modifier.size(14.dp))
                    Text(stringResource(R.string.upload), color = textColor.copy(alpha = 0.55f), fontSize = (10 * textScale).sp, fontWeight = FontWeight.Medium)
                }
                Text(txSpeed, color = textColor, fontSize = (15 * textScale).sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun formatSpeed(bytesPerSec: Long): String {
    return when {
        bytesPerSec < 1024      -> "${bytesPerSec} B/s"
        bytesPerSec < 1024*1024 -> "${"%.1f".format(bytesPerSec/1024f)} KB/s"
        else                    -> "${"%.1f".format(bytesPerSec/1024f/1024f)} MB/s"
    }
}

// ─────────────────────────────────────────────────────────
// 14. Screen Time Widget — NEW
// ─────────────────────────────────────────────────────────
@Composable
private fun ScreenTimeWidget(isDark: Boolean, context: Context, textScale: Float) {
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight

    val hasPermission = remember {
        try {
            val ops = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
            } else {
                @Suppress("DEPRECATION")
                ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) { false }
    }

    data class AppUsage(val name: String, val minutes: Long, val fraction: Float)

    var topApps by remember { mutableStateOf<List<AppUsage>>(emptyList()) }

    LaunchedEffect(hasPermission) {
        if (!hasPermission) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            try {
                val usm   = context.getSystemService(Context.USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
                val end   = System.currentTimeMillis()
                val start = end - 24 * 60 * 60 * 1000L
                val stats = usm.queryUsageStats(android.app.usage.UsageStatsManager.INTERVAL_DAILY, start, end)
                    .filter { it.totalTimeInForeground > 0 }
                    .sortedByDescending { it.totalTimeInForeground }
                    .take(4)
                val total = stats.sumOf { it.totalTimeInForeground }.coerceAtLeast(1)
                val result = stats.map { s ->
                    val name = try {
                        context.packageManager.getApplicationLabel(
                            context.packageManager.getApplicationInfo(s.packageName, 0)
                        ).toString()
                    } catch (e: Exception) { s.packageName.substringAfterLast('.') }
                    AppUsage(name, s.totalTimeInForeground / 60000, s.totalTimeInForeground.toFloat() / total)
                }
                withContext(Dispatchers.Main) { topApps = result }
            } catch (e: Exception) { }
        }
    }

    WidgetCard(isDark) {
        WidgetHeader(stringResource(R.string.screen_time), FluentIcon.PhoneAndroid, textColor, textScale)
        Spacer(Modifier.height(10.dp))
        if (!hasPermission) {
            TextButton(onClick = {
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }) { Text(stringResource(R.string.grant_usage_access), fontSize = (12 * textScale).sp) }
        } else if (topApps.isEmpty()) {
            Text(stringResource(R.string.no_data_yet), color = textColor.copy(alpha = 0.5f), fontSize = (12 * textScale).sp, fontWeight = FontWeight.Medium)
        } else {
            topApps.forEach { app ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(app.name, color = textColor, fontSize = (12 * textScale).sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(90.dp))
                    LinearProgressIndicator(
                        progress = { app.fraction },
                        modifier = Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)),
                        color      = LocalWidgetAccent.current,
                        trackColor = widgetDividerColor(textColor)
                    )
                    Text("${app.minutes}m", color = textColor.copy(alpha = 0.55f), fontSize = (10 * textScale).sp,
                        modifier = Modifier.width(32.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                }
            }
        }
    }
}
