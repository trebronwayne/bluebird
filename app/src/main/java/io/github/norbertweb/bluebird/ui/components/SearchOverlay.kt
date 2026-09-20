package io.github.norbertweb.bluebird.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import io.github.norbertweb.bluebird.AppInfo
import io.github.norbertweb.bluebird.LauncherUiState
import io.github.norbertweb.bluebird.LauncherViewModel
import io.github.norbertweb.bluebird.R
import io.github.norbertweb.bluebird.ui.theme.LocalIsDarkTheme
import io.github.norbertweb.bluebird.ui.theme.bluebirdColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private enum class SearchFilter(val label: String, val icon: ImageVector) {
    All("All", FluentIcon.Apps),
    Apps("Apps", FluentIcon.Grid),
    Web("Web", FluentIcon.Globe)
}

private const val SEARCH_OVERLAY_PREFS = "search_overlay_prefs"

private fun loadRecentSearches(context: Context): List<String> {
    val prefs = context.getSharedPreferences(SEARCH_OVERLAY_PREFS, Context.MODE_PRIVATE)
    val raw = prefs.getString("recent_searches", null) ?: return emptyList()
    return raw.split("\u0001").filter(String::isNotBlank)
}

private fun saveRecentSearches(context: Context, searches: List<String>) {
    context.getSharedPreferences(SEARCH_OVERLAY_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString("recent_searches", searches.joinToString("\u0001"))
        .apply()
}

@Composable
fun SearchOverlay(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel,
    hazeState: HazeState,
    accent: Color,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) { viewModel.ensureInstalledAppsLoaded() }

    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val configuration = LocalConfiguration.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    val mutedText = textColor.copy(alpha = 0.58f)
    val subtleText = textColor.copy(alpha = 0.42f)

    // Search always starts blank and owns its own text independently of
    // Start's search box. Do NOT seed this from uiState.searchQuery: both
    // Search and Start used to read/write that same shared field, so
    // whichever one you typed in last would leak its text into the other
    // the moment the field was focused.
    var localSearchQuery by rememberSaveable { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(SearchFilter.All) }
    val recentSearches = remember {
        mutableStateListOf<String>().apply { addAll(loadRecentSearches(context)) }
    }

    // One-way only: still publish to the shared ViewModel field in case other
    // UI (e.g. a taskbar indicator) wants to observe it, but never pull it
    // back into our own local state - that pull is what caused the leak.
    LaunchedEffect(localSearchQuery) {
        delay(100)
        if (localSearchQuery != uiState.searchQuery) {
            viewModel.updateSearchQuery(localSearchQuery)
        }
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
        runCatching { keyboardController?.show() }
    }

    // Reset the shared field when Search closes so a stale query can't leak
    // into Start (or back into Search) the next time either one opens.
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { viewModel.updateSearchQuery("") }
    }

    val searchQuery = localSearchQuery.trim()
    val normalizedQuery = searchQuery.lowercase()
    val isWideSearchLayout = configuration.screenWidthDp >= 560

    val appResults = remember(uiState.installedApps, normalizedQuery) {
        if (normalizedQuery.isEmpty()) {
            uiState.installedApps
        } else {
            uiState.installedApps.filter {
                it.name.contains(normalizedQuery, ignoreCase = true) ||
                    it.packageName.contains(normalizedQuery, ignoreCase = true)
            }
        }
    }

    // Shared by both the idle "Top apps" grid and, now, the results view's
    // "Recent" section - so recently-opened apps don't just vanish the
    // moment you start typing.
    val topApps = remember(uiState.installedApps) {
        uiState.installedApps
            .map { it to getAppOpenCount(context, it.packageName) }
            .sortedWith(
                compareByDescending<Pair<AppInfo, Int>> { it.second }
                    .thenBy { it.first.name.lowercase() }
            )
            .map { it.first }
            .take(8)
            .ifEmpty { uiState.installedApps.take(8) }
    }

    // Last 6 installed apps, same PackageManager.firstInstallTime technique
    // Start's own "Recently installed" list already uses. Note: this can
    // only surface an app once it shows up in uiState.installedApps - if the
    // app list itself is stale, a just-installed app won't appear here
    // either until that list refreshes.
    val recentlyInstalledApps by produceState(
        initialValue = emptyList<AppInfo>(),
        key1 = uiState.installedApps
    ) {
        value = withContext(Dispatchers.IO) {
            val pm = context.packageManager
            uiState.installedApps
                .sortedByDescending { app ->
                    try {
                        pm.getPackageInfo(app.packageName, 0).firstInstallTime
                    } catch (_: Exception) {
                        0L
                    }
                }
                .take(6)
        }
    }

    fun performWebSearch(query: String) {
        if (query.isBlank()) return
        if (!recentSearches.contains(query)) {
            recentSearches.add(0, query)
            if (recentSearches.size > 12) recentSearches.removeLast()
            saveRecentSearches(context, recentSearches)
        }
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")
        )
        context.startActivity(intent)
    }

    // Windows Search is a single floating Acrylic surface. There is deliberately
    // no second card, no size-picker control, and no inner blur region.
    // Sized identically to the Start Menu shell (see StartMenu's sizing
    // modifiers) rather than its own wider/taller calculation.
    val radius = 12.dp
    val panelShape = RoundedCornerShape(radius)

    Box(
        modifier = modifier
            .fillMaxWidth(0.94f)
            .fillMaxHeight(0.82f)
            .widthIn(min = 320.dp, max = 640.dp)
            .heightIn(min = 520.dp, max = 760.dp)
            .imePadding()
            .softShadow(
                cornerRadius = 12.dp,
                blurRadius = 34.dp,
                alpha = 0.30f,
                offsetY = 10.dp
            )
            .hazeMaterial(hazeState, windowsAcrylic(accent, isDark), panelShape)
            .background(
                if (isDark) Color(0xE61F1F1F) else Color(0xE6F8F8F8),
                panelShape
            )
            .border(
                1.dp,
                if (isDark) Color.White.copy(alpha = 0.09f) else Color.Black.copy(alpha = 0.08f),
                panelShape
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Keep one continuous Acrylic surface, exactly like Start.
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Windows-style search field: same visual family as the polished
            // Start Menu, with the Bluebird colored search XML asset.
            // Elevated header: a soft shadow lifts the bar off the panel and
            // a faint accent tint gives it a touch of color elevation
            // without overpowering the field.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .softShadow(
                        cornerRadius = 18.dp,
                        blurRadius = 12.dp,
                        alpha = 0.14f,
                        offsetY = 2.dp
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                accent.copy(alpha = if (isDark) 0.07f else 0.05f),
                                if (isDark) Color.White.copy(alpha = 0.075f)
                                else Color.White.copy(alpha = 0.82f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        if (isDark) Color.White.copy(alpha = 0.10f)
                        else Color.Black.copy(alpha = 0.10f),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { runCatching { focusRequester.requestFocus() } }
                    .padding(horizontal = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.ic_bluebird_start_search),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                BasicTextField(
                    value = localSearchQuery,
                    onValueChange = { localSearchQuery = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                        .focusRequester(focusRequester),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = textColor,
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(bluebirdColors.AccentBlue),
                    decorationBox = { inner ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (localSearchQuery.isEmpty()) {
                                androidx.compose.material3.Text(
                                    "Search apps, settings, and documents",
                                    color = textColor.copy(alpha = 0.48f),
                                    fontSize = 14.sp
                                )
                            }
                            inner()
                        }
                    }
                )

                if (localSearchQuery.isNotEmpty()) {
                    IconButton(
                        icon = FluentIcon.Dismiss,
                        tint = mutedText,
                        size = 15.dp,
                        onClick = { localSearchQuery = "" }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Keep the idle Search home visually quiet. Category filters appear
            // once the user has an actual query, matching Windows Search's flow.
            if (searchQuery.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(SearchFilter.values().toList()) { filter ->
                        SearchFilterChip(
                            filter = filter,
                            selected = activeFilter == filter,
                            isDark = isDark,
                            onClick = { activeFilter = filter }
                        )
                    }
                }

                Spacer(Modifier.height(11.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                if (searchQuery.isEmpty()) {
                    IdleSearchContent(
                        uiState = uiState,
                        recentSearches = recentSearches,
                        topApps = topApps,
                        recentlyInstalledApps = recentlyInstalledApps,
                        isDark = isDark,
                        textColor = textColor,
                        isWideLayout = isWideSearchLayout,
                        onAppClick = { viewModel.openApp(context, it) },
                        onQuickSearch = { localSearchQuery = it },
                        onRecentClick = { localSearchQuery = it },
                        onClearRecent = {
                            recentSearches.clear()
                            saveRecentSearches(context, emptyList())
                        }
                    )
                } else {
                    SearchResultsContent(
                        query = searchQuery,
                        appResults = appResults,
                        activeFilter = activeFilter,
                        isDark = isDark,
                        textColor = textColor,
                        recentSearches = recentSearches,
                        recentApps = topApps,
                        recentlyInstalledApps = recentlyInstalledApps,
                        onAppClick = { viewModel.openApp(context, it) },
                        onWebSearch = { performWebSearch(it) },
                        onRecentQueryClick = { localSearchQuery = it },
                        onClearRecent = {
                            recentSearches.clear()
                            saveRecentSearches(context, emptyList())
                        }
                    )
                }
            }

            // Full-width structural boundary, matching the Start surface.
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        if (isDark) Color.White.copy(alpha = 0.07f)
                        else Color.Black.copy(alpha = 0.07f)
                    )
            )
            Spacer(Modifier.height(2.dp))
        }
    }
}

@Composable
private fun RecentSection(
    recentSearches: List<String>,
    topApps: List<AppInfo>,
    recentlyInstalledApps: List<AppInfo>,
    isDark: Boolean,
    textColor: Color,
    modifier: Modifier = Modifier,
    onRecentClick: (String) -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onClearRecent: () -> Unit
) {
    Column(modifier = modifier) {
        SectionHeader(
            title = "Recent",
            icon = FluentIcon.List,
            textColor = textColor,
            action = if (recentSearches.isNotEmpty()) "Clear" to onClearRecent else null
        )
        Spacer(Modifier.height(7.dp))

        if (recentSearches.isNotEmpty()) {
            recentSearches.take(6).forEach { query ->
                RecentSearchRow(
                    query = query,
                    textColor = textColor,
                    onClick = { onRecentClick(query) }
                )
            }
        } else if (topApps.isNotEmpty()) {
            // There is no separate Windows-style local file index in
            // Bluebird yet, so a fresh install shows real installed apps
            // rather than a fabricated recent-file list.
            topApps.take(4).forEach { app ->
                SuggestedAppRow(
                    app = app,
                    isDark = isDark,
                    textColor = textColor,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onAppClick(app) }
                )
            }
        } else {
            Text(
                "No recent searches yet",
                color = textColor.copy(alpha = 0.42f),
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (recentlyInstalledApps.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            SectionHeader("Recently installed", FluentIcon.Apps, textColor)
            Spacer(Modifier.height(6.dp))
            recentlyInstalledApps.take(6).forEach { app ->
                SuggestedAppRow(
                    app = app,
                    isDark = isDark,
                    textColor = textColor,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onAppClick(app) }
                )
            }
        }
    }
}

@Composable
private fun IdleSearchContent(
    uiState: LauncherUiState,
    recentSearches: List<String>,
    topApps: List<AppInfo>,
    recentlyInstalledApps: List<AppInfo>,
    isDark: Boolean,
    textColor: Color,
    isWideLayout: Boolean,
    onAppClick: (AppInfo) -> Unit,
    onQuickSearch: (String) -> Unit,
    onRecentClick: (String) -> Unit,
    onClearRecent: () -> Unit
) {
    // Windows Search reference hierarchy: Recent on the left; Quick searches
    // at upper-right; Top apps directly underneath. Everything stays inside
    // the same continuous Bluebird Acrylic surface as Start.

    @Composable
    fun RecentColumn(modifier: Modifier = Modifier) {
        RecentSection(
            recentSearches = recentSearches,
            topApps = topApps,
            recentlyInstalledApps = recentlyInstalledApps,
            isDark = isDark,
            textColor = textColor,
            modifier = modifier,
            onRecentClick = onRecentClick,
            onAppClick = onAppClick,
            onClearRecent = onClearRecent
        )
    }

    @Composable
    fun RightColumn(modifier: Modifier = Modifier) {
        Column(modifier = modifier) {
            SectionHeader("Quick searches", FluentIcon.Flash, textColor)
            Spacer(Modifier.height(7.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                quickSearchItems.take(6).chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        pair.forEach { (label, icon) ->
                            QuickSearchChip(
                                label = label,
                                icon = icon,
                                isDark = isDark,
                                modifier = Modifier.weight(1f),
                                onClick = { onQuickSearch(label) }
                            )
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            SectionHeader("Top apps", FluentIcon.Apps, textColor)
            Spacer(Modifier.height(7.dp))

            if (topApps.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    topApps.chunked(4).forEach { rowApps ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowApps.forEach { app ->
                                TopAppItem(
                                    app = app,
                                    isDark = isDark,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onAppClick(app) }
                                )
                            }
                            // Pad a short final row so tiles stay a fixed
                            // width instead of stretching to fill the row.
                            repeat(4 - rowApps.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            } else {
                Text(
                    "No apps available",
                    color = textColor.copy(alpha = 0.42f),
                    fontSize = 12.sp
                )
            }
        }
    }

    if (isWideLayout) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(22.dp),
            verticalAlignment = Alignment.Top
        ) {
            RecentColumn(modifier = Modifier.weight(0.9f))
            RightColumn(modifier = Modifier.weight(1.35f))
        }
    } else {
        // Portrait phones preserve the exact section order while stacking
        // columns so none of the reference content becomes cramped.
        RecentColumn(modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(18.dp))
        RightColumn(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun SearchResultsContent(
    query: String,
    appResults: List<AppInfo>,
    activeFilter: SearchFilter,
    isDark: Boolean,
    textColor: Color,
    recentSearches: List<String>,
    recentApps: List<AppInfo>,
    recentlyInstalledApps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onWebSearch: (String) -> Unit,
    onRecentQueryClick: (String) -> Unit,
    onClearRecent: () -> Unit
) {
    val showApps = activeFilter == SearchFilter.All || activeFilter == SearchFilter.Apps
    val showWeb = activeFilter == SearchFilter.All || activeFilter == SearchFilter.Web

    if (showApps) {
        SectionHeader("Best match", FluentIcon.Apps, textColor)
        Spacer(Modifier.height(6.dp))

        if (appResults.isNotEmpty()) {
            BestMatchItem(
                app = appResults.first(),
                isDark = isDark,
                textColor = textColor,
                onClick = { onAppClick(appResults.first()) }
            )

            Spacer(Modifier.height(10.dp))

            if (appResults.size > 1) {
                appResults.drop(1).take(8).forEach { app ->
                    AppResultRow(
                        app = app,
                        query = query,
                        textColor = textColor,
                        onClick = { onAppClick(app) }
                    )
                }
            }
        } else {
            EmptySearchState(query = query, textColor = textColor)
        }
    }

    if (showWeb) {
        Spacer(Modifier.height(12.dp))
        SectionHeader("Search the web", FluentIcon.Globe, textColor)

        Spacer(Modifier.height(6.dp))

        WebSearchRow(
            query = query,
            textColor = textColor,
            onClick = { onWebSearch(query) }
        )
    }

    // Recent no longer lives only in the idle screen or only under "Search
    // the web" - it's the same persistent block shown here regardless of
    // what's typed or which filter is active, exactly like the idle state,
    // instead of vanishing the moment a query comes in.
    Spacer(Modifier.height(14.dp))
    RecentSection(
        recentSearches = recentSearches,
        topApps = recentApps,
        recentlyInstalledApps = recentlyInstalledApps,
        isDark = isDark,
        textColor = textColor,
        onRecentClick = onRecentQueryClick,
        onAppClick = onAppClick,
        onClearRecent = onClearRecent
    )
}

@Composable
private fun EmptySearchState(query: String, textColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(R.drawable.ic_bluebird_start_search),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(8.dp))
        androidx.compose.material3.Text(
            "No apps found for \"$query\"",
            color = textColor.copy(alpha = 0.48f),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun BestMatchItem(
    app: AppInfo,
    isDark: Boolean,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DS.controlFill(isDark, 0.82f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppIconSmall(
            drawable = app.icon,
            contentDescription = app.name,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
        )

        Column(modifier = Modifier.weight(1f)) {
            androidx.compose.material3.Text(
                app.name,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            androidx.compose.material3.Text(
                "App",
                color = textColor.copy(alpha = 0.48f),
                fontSize = 11.sp
            )
        }

        TextButton(
            label = "Open",
            tint = bluebirdColors.AccentBlue,
            onClick = onClick
        )
    }
}

@Composable
private fun AppResultRow(
    app: AppInfo,
    query: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AppIconSmall(
            drawable = app.icon,
            contentDescription = app.name,
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Column(modifier = Modifier.weight(1f)) {
            val lower = app.name.lowercase()
            val index = lower.indexOf(query.lowercase())
            val name = buildAnnotatedString {
                if (index >= 0) {
                    append(app.name.substring(0, index))
                    withStyle(
                        SpanStyle(
                            color = bluebirdColors.AccentBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        append(app.name.substring(index, index + query.length))
                    }
                    append(app.name.substring(index + query.length))
                } else {
                    append(app.name)
                }
            }

            androidx.compose.material3.Text(
                name,
                color = textColor,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            androidx.compose.material3.Text(
                "App",
                color = textColor.copy(alpha = 0.42f),
                fontSize = 10.sp
            )
        }

        Icon(FluentIcon.ChevronRight, null, tint = textColor.copy(alpha = 0.26f), modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun SuggestedAppRow(
    app: AppInfo,
    isDark: Boolean,
    textColor: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppIconSmall(
            drawable = app.icon,
            contentDescription = app.name,
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Column(modifier = Modifier.weight(1f)) {
            androidx.compose.material3.Text(
                app.name,
                color = textColor,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            androidx.compose.material3.Text(
                "Suggested",
                color = textColor.copy(alpha = 0.40f),
                fontSize = 9.5.sp
            )
        }
    }
}

@Composable
private fun RecentSearchRow(
    query: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Icon(FluentIcon.List, null, tint = textColor.copy(alpha = 0.42f), modifier = Modifier.size(14.dp))
        androidx.compose.material3.Text(
            query,
            color = textColor.copy(alpha = 0.78f),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Icon(FluentIcon.ArrowReply, null, tint = textColor.copy(alpha = 0.22f), modifier = Modifier.size(12.dp))
    }
}

@Composable
private fun TopAppItem(
    app: AppInfo,
    isDark: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight

    // Windows 11 "All apps" tile: a real bounded surface (fill + hairline
    // border) rather than a bare icon+label floating in the grid.
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DS.controlFill(isDark, 0.90f))
            .border(
                1.dp,
                if (isDark) Color.White.copy(alpha = 0.07f) else Color.Black.copy(alpha = 0.07f),
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppIconSmall(
            drawable = app.icon,
            contentDescription = app.name,
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(Modifier.height(5.dp))
        androidx.compose.material3.Text(
            app.name,
            color = textColor.copy(alpha = 0.80f),
            fontSize = 9.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun QuickSearchChip(
    label: String,
    icon: ImageVector,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(DS.controlFill(isDark, 0.82f))
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, null, tint = bluebirdColors.AccentBlue, modifier = Modifier.size(13.dp))
        androidx.compose.material3.Text(
            label,
            color = textColor.copy(alpha = 0.82f),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun WebSearchRow(
    query: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(DS.controlFill(LocalIsDarkTheme.current, 0.95f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(FluentIcon.Globe, null, tint = bluebirdColors.AccentBlue, modifier = Modifier.size(16.dp))
        androidx.compose.material3.Text(
            buildAnnotatedString {
                append("Search the web for ")
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = textColor)) {
                    append("\"$query\"")
                }
            },
            color = textColor.copy(alpha = 0.76f),
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
        Icon(FluentIcon.Open, null, tint = textColor.copy(alpha = 0.3f), modifier = Modifier.size(13.dp))
    }
}

@Composable
private fun WebSuggestionRow(
    suggestion: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Icon(FluentIcon.ArrowTrendingUp, null, tint = textColor.copy(alpha = 0.30f), modifier = Modifier.size(13.dp))
        androidx.compose.material3.Text(
            suggestion,
            color = textColor.copy(alpha = 0.70f),
            fontSize = 11.sp,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(FluentIcon.ArrowReply, null, tint = textColor.copy(alpha = 0.20f), modifier = Modifier.size(12.dp))
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    textColor: Color,
    action: Pair<String, () -> Unit>? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Text(
            title,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.weight(1f))

        if (action != null) {
            androidx.compose.material3.Text(
                action.first,
                color = bluebirdColors.AccentBlue,
                fontSize = 11.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { action.second() }
                    .padding(horizontal = 5.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun SearchFilterChip(
    filter: SearchFilter,
    selected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(17.dp))
            .background(
                if (selected) bluebirdColors.AccentBlue.copy(alpha = 0.16f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            filter.icon,
            null,
            tint = if (selected) bluebirdColors.AccentBlue else textColor.copy(alpha = 0.56f),
            modifier = Modifier.size(12.dp)
        )
        androidx.compose.material3.Text(
            filter.label,
            color = if (selected) bluebirdColors.AccentBlue else textColor.copy(alpha = 0.68f),
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun TextButton(
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    androidx.compose.material3.Text(
        label,
        color = tint,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp)
    )
}

@Composable
private fun IconButton(
    icon: ImageVector,
    tint: Color,
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(15.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(size))
    }
}

private val quickSearchItems = listOf(
    "Weather" to FluentIcon.WeatherSunny,
    "News" to FluentIcon.DocumentText,
    "Calculator" to FluentIcon.Calculator,
    "Maps" to FluentIcon.Location,
    "Music" to FluentIcon.MusicNote2,
    "Photos" to FluentIcon.ImageMultiple
)
