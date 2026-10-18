package io.github.norbertweb.bluebird.ui.screens

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.key
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider

import io.github.norbertweb.bluebird.LauncherViewModel
import io.github.norbertweb.bluebird.R
import io.github.norbertweb.bluebird.ui.components.BluebirdExecutable
import io.github.norbertweb.bluebird.ui.components.BpkPackageIcon
import io.github.norbertweb.bluebird.ui.components.BluebirdStorage
import io.github.norbertweb.bluebird.ui.components.PngIcon
import io.github.norbertweb.bluebird.ui.components.fileIconRes
import io.github.norbertweb.bluebird.LauncherScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fluent.ui.system.icons.FluentIcons
import fluent.ui.system.icons.regular.Apps
import fluent.ui.system.icons.regular.ArrowClockwise
import fluent.ui.system.icons.regular.ArrowDownload
import fluent.ui.system.icons.regular.ArrowLeft
import fluent.ui.system.icons.regular.ArrowUp
import fluent.ui.system.icons.regular.Camera
import fluent.ui.system.icons.regular.ChevronDown
import fluent.ui.system.icons.regular.ChevronRight
import fluent.ui.system.icons.regular.ChevronUp
import fluent.ui.system.icons.regular.ClipboardPaste
import fluent.ui.system.icons.regular.Code
import fluent.ui.system.icons.regular.Copy
import fluent.ui.system.icons.regular.Cut
import fluent.ui.system.icons.regular.Delete
import fluent.ui.system.icons.regular.Desktop
import fluent.ui.system.icons.regular.DiamondDismiss
import fluent.ui.system.icons.regular.Document
import fluent.ui.system.icons.regular.DocumentOnePage
import fluent.ui.system.icons.regular.DocumentPdf
import fluent.ui.system.icons.regular.Eye
import fluent.ui.system.icons.regular.EyeOff
import fluent.ui.system.icons.regular.Folder
import fluent.ui.system.icons.regular.FolderAdd
//import fluent.ui.system.icons.regular.FolderOff
import fluent.ui.system.icons.regular.FolderOpen
import fluent.ui.system.icons.regular.FolderZip
import fluent.ui.system.icons.regular.Globe
import fluent.ui.system.icons.regular.Grid
import fluent.ui.system.icons.regular.HardDrive
import fluent.ui.system.icons.regular.Image
import fluent.ui.system.icons.regular.Info
import fluent.ui.system.icons.regular.Link

import fluent.ui.system.icons.regular.MusicNote2

import fluent.ui.system.icons.regular.Phone
import fluent.ui.system.icons.regular.Rename
import fluent.ui.system.icons.regular.Search

import fluent.ui.system.icons.regular.SelectAllOn
import fluent.ui.system.icons.regular.SlideLayout
import fluent.ui.system.icons.regular.Table
import fluent.ui.system.icons.regular.VideoClip
import fluent.ui.system.icons.filled.Play
import fluent.ui.system.icons.regular.FolderProhibited
import fluent.ui.system.icons.regular.List
import fluent.ui.system.icons.regular.Open
import io.github.norbertweb.bluebird.ui.theme.bluebirdColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import io.github.norbertweb.bluebird.wordprocessor.formatDate
import io.github.norbertweb.bluebird.wordprocessor.formatFileSize

// ────────────────────────────────────────────────────────
// Windows 11 Fluent palette — tuned to match real Explorer's Mica layering:
// a slightly translucent-feeling background, a marginally lighter/darker
// "card" surface for panes and the command surfaces, and a crisp accent
// with distinct hover/pressed/selected states (Fluent uses 4 states, not 2).
// ────────────────────────────────────────────────────────
private val ExplorerLightBackground = Color(0xFFFAFAFA)   // Mica base
private val ExplorerLightNav = Color(0xFFF3F3F3)          // Nav pane (slightly recessed)
private val ExplorerLightSurface = Color(0xFFF9F9F9)      // Ribbon / header strip
private val ExplorerLightField = Color(0xFFFFFFFF)        // Text fields, search box
private val ExplorerLightBorder = Color(0xFFE5E5E5)
private val ExplorerLightText = Color(0xFF1A1A1A)
private val ExplorerLightSecondaryText = Color(0xFF5F5F5F)
private val ExplorerAccentBlue = Color(0xFF0067C0)        // Win11 system accent (default blue)
private val ExplorerAccentBlueLight = Color(0xFF2899F5)
private val ExplorerSelectionBlue = Color(0xFFDDEBFA)
private val ExplorerHoverLight = Color(0xFF000000)        // used with low alpha for hover
private val ExplorerPressedLight = Color(0xFF000000)      // used with slightly higher alpha

private val ExplorerDarkBackground = Color(0xFF202020)    // Win11 dark Mica base
private val ExplorerDarkNav = Color(0xFF272727)
private val ExplorerDarkSurface = Color(0xFF2C2C2C)
private val ExplorerDarkField = Color(0xFF1E1E1E)
private val ExplorerDarkBorder = Color(0xFF3D3D3D)
private val ExplorerDarkText = Color(0xFFFFFFFF)
private val ExplorerDarkSecondaryText = Color(0xFFC5C5C5)
private val ExplorerDarkAccent = Color(0xFF60CDFF)        // Win11 dark-mode accent
private val ExplorerDarkAccentLight = Color(0xFF98D9FF)
private val ExplorerDarkSelection = Color(0xFF2B4256)
private val ExplorerHoverDark = Color(0xFFFFFFFF)
private val ExplorerPressedDark = Color(0xFFFFFFFF)

// Fluent's four interaction-state alphas, shared across light/dark since only the
// base hover/pressed color (black vs white) differs.
private const val FluentHoverAlpha = 0.06f
private const val FluentPressedAlpha = 0.09f
private const val FluentSelectedHoverAlpha = 0.16f

// ────────────────────────────────────────────────────────
// Data & Helpers
// ────────────────────────────────────────────────────────

data class RealFileItem(
    val file: File,
    val name: String = file.name,
    val isDirectory: Boolean = file.isDirectory,
    val size: Long = if (file.isFile) file.length() else 0L,
    val lastModified: Long = file.lastModified(),
    val extension: String = file.extension.lowercase(),
    val iconBitmap: Bitmap? = null
)


private data class ExplorerNavDestination(
    val label: String,
    val directory: File,
    val icon: Int
)



fun getFileIcon(item: RealFileItem): ImageVector = when {
    item.isDirectory -> FluentIcons.Regular.Folder
    item.extension in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp") -> FluentIcons.Regular.Image
    item.extension in listOf("mp4", "mkv", "avi", "mov", "webm", "3gp") -> FluentIcons.Regular.VideoClip
    item.extension in listOf("mp3", "wav", "ogg", "flac", "aac", "m4a") -> FluentIcons.Regular.MusicNote2
    item.extension in listOf("pdf") -> FluentIcons.Regular.DocumentPdf
    item.extension in listOf("txt", "log", "md", "xml", "json", "csv") -> FluentIcons.Regular.Document
    item.extension in listOf("zip", "rar", "7z", "tar", "gz") -> FluentIcons.Regular.FolderZip
    item.extension in listOf("apk") -> FluentIcons.Regular.Phone
    item.extension in listOf("doc", "docx") -> FluentIcons.Regular.DocumentOnePage
    item.extension in listOf("xls", "xlsx") -> FluentIcons.Regular.Table
    item.extension in listOf("ppt", "pptx") -> FluentIcons.Regular.SlideLayout
    item.extension in listOf("html", "htm") -> FluentIcons.Regular.Code
    item.extension == "webapp" -> FluentIcons.Regular.Globe
    item.extension == "bpk" -> FluentIcons.Regular.FolderZip
    item.extension == "exe" -> FluentIcons.Regular.Apps
    item.extension == "io.github.norbertweb.io.github.norbertweb.bluebird" -> FluentIcons.Regular.Apps
    else -> FluentIcons.Regular.Document
}

fun getFileIconColor(item: RealFileItem): Color = when {
    item.isDirectory -> ExplorerAccentBlue
    item.extension in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp") -> Color(0xFF168FE5)
    item.extension in listOf("mp4", "mkv", "avi", "mov", "webm", "3gp") -> Color(0xFF6F58D9)
    item.extension in listOf("mp3", "wav", "ogg", "flac", "aac", "m4a") -> Color(0xFFE26596)
    item.extension == "pdf" -> Color(0xFFD83B01)
    item.extension == "apk" -> Color(0xFF107C10)
    item.extension in listOf("doc", "docx") -> Color(0xFF2B88D8)
    item.extension in listOf("xls", "xlsx") -> Color(0xFF107C10)
    item.extension in listOf("webapp", "bpk", "exe", "io.github.norbertweb.io.github.norbertweb.bluebird") -> ExplorerAccentBlue
    else -> Color(0xFF667A90)
}

fun getMimeType(file: File): String {
    val ext = file.extension.lowercase()
    return when (ext) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "bmp" -> "image/bmp"
        "webp" -> "image/webp"
        "mp4" -> "video/mp4"
        "mkv" -> "video/x-matroska"
        "avi" -> "video/x-msvideo"
        "mov" -> "video/quicktime"
        "mp3" -> "audio/mpeg"
        "wav" -> "audio/wav"
        "ogg" -> "audio/ogg"
        "flac" -> "audio/flac"
        "aac" -> "audio/aac"
        "pdf" -> "application/pdf"
        "txt", "log", "md" -> "text/plain"
        "html", "htm" -> "text/html"
        "xml" -> "text/xml"
        "json" -> "application/json"
        "csv" -> "text/csv"
        "zip" -> "application/zip"
        "rar" -> "application/x-rar-compressed"
        "7z" -> "application/x-7z-compressed"
        "apk" -> "application/vnd.android.package-archive"
        "doc" -> "application/msword"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "xls" -> "application/vnd.ms-excel"
        "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        "ppt" -> "application/vnd.ms-powerpoint"
        "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        else -> "*/*"
    }
}

// ────────────────────────────────────────────────────────
// Permission Helper
// ────────────────────────────────────────────────────────

private fun hasStorageAccess(context: android.content.Context): Boolean {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
            Environment.isExternalStorageManager()
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
            Environment.isExternalStorageManager() ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        else ->
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}

// ────────────────────────────────────────────────────────
// State Holder  ← KEY FIX: moves all mutableStateOf out of
// the root composable so the compiler emits far fewer
// registers in FileExplorerScreen itself.
// ────────────────────────────────────────────────────────

private class FileExplorerState(initialDir: File) {
    private val externalRoot = Environment.getExternalStorageDirectory()
    var currentDir by mutableStateOf(initialDir)
    var isHomeDashboard by mutableStateOf(initialDir.absolutePath == externalRoot.absolutePath)
    var isDeviceDashboard by mutableStateOf(false)
    var pathHistory by mutableStateOf(listOf<File>())
    var allFiles by mutableStateOf(listOf<RealFileItem>())

    /**
     * Derived listing: search/sort never touches the filesystem.
     *
     * Memoized because Compose can ask for `files` many times while a lazy list/grid
     * is measuring. Previously a large folder could be sorted repeatedly during
     * recomposition, making an otherwise fast directory feel sluggish.
     */
    private var cachedAllFiles: List<RealFileItem>? = null
    private var cachedQuery = ""
    private var cachedSortBy = ""
    private var cachedSortAscending = true
    private var cachedFiles: List<RealFileItem> = emptyList()

    val files: List<RealFileItem>
        get() {
            val source = allFiles
            val query = searchQuery.trim()
            if (source === cachedAllFiles && query == cachedQuery &&
                sortBy == cachedSortBy && sortAscending == cachedSortAscending) {
                return cachedFiles
            }

            val filtered = if (query.isEmpty()) source else source.filter {
                it.name.contains(query, ignoreCase = true)
            }
            val sorted = when (sortBy) {
                "name" -> filtered.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
                "size" -> filtered.sortedBy { it.size }
                "date" -> filtered.sortedBy { it.lastModified }
                "type" -> filtered.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.extension })
                else -> filtered.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
            }
            val dirs = sorted.filter { it.isDirectory }
            val filesOnly = sorted.filter { it.file.isFile }
            cachedFiles = if (sortAscending) dirs + filesOnly else dirs.reversed() + filesOnly.reversed()

            cachedAllFiles = source
            cachedQuery = query
            cachedSortBy = sortBy
            cachedSortAscending = sortAscending
            return cachedFiles
        }
    var isLoading by mutableStateOf(true)

    // UI toggles
    var isGridView by mutableStateOf(false)
    var searchQuery by mutableStateOf("")
    var selectedFiles by mutableStateOf(setOf<String>())
    var sortBy by mutableStateOf("name")
    var sortAscending by mutableStateOf(true)
    var showHidden by mutableStateOf(false)

    // Dialogs
    var contextMenuFile by mutableStateOf<RealFileItem?>(null)
    var contextMenuOffset by mutableStateOf(Offset.Zero)
    var showBackgroundContextMenu by mutableStateOf(false)
    var showDeleteDialog by mutableStateOf(false)
    var propertiesTarget by mutableStateOf<RealFileItem?>(null)

    // Inline rename — like real Windows 11 Explorer, F2/"Rename" turns the item's own
    // label in the list/grid into an editable field in place, instead of popping a
    // separate dialog. Tracked by path so ListFileItem/GridFileItem can each check
    // "is it me?" without needing the whole RealFileItem passed back down.
    var renamingPath by mutableStateOf<String?>(null)
    // Clipboard now lives in the ViewModel (shared with Desktop) — see vmUiState.clipboardFiles

    fun navigateTo(dir: File) {
        isHomeDashboard = false
        isDeviceDashboard = false
        if (dir != currentDir) {
            pathHistory = pathHistory + currentDir
            currentDir = dir
            renamingPath = null
        }
    }

    fun navigateHome() {
        val root = externalRoot
        isHomeDashboard = true
        isDeviceDashboard = false
        pathHistory = emptyList()
        currentDir = root
        searchQuery = ""
        selectedFiles = emptySet()
        renamingPath = null
    }

    fun navigateDevice() {
        isHomeDashboard = false
        isDeviceDashboard = true
        pathHistory = emptyList()
        currentDir = externalRoot
        searchQuery = ""
        selectedFiles = emptySet()
        renamingPath = null
    }

    fun goBack() {
        if (isDeviceDashboard) {
            navigateHome()
            return
        }
        if (pathHistory.isNotEmpty()) {
            isHomeDashboard = false
            isDeviceDashboard = false
            currentDir = pathHistory.last()
            pathHistory = pathHistory.dropLast(1)
            renamingPath = null
        }
    }

    fun goUp() {
        if (isDeviceDashboard) return
        val parent = currentDir.parentFile
        if (parent != null && parent != currentDir) {
            isHomeDashboard = false
            isDeviceDashboard = false
            pathHistory = pathHistory + currentDir
            currentDir = parent
            renamingPath = null
        }
    }
}

@Composable
private fun rememberFileExplorerState(initialDir: File = Environment.getExternalStorageDirectory()): FileExplorerState {
    return remember(initialDir.absolutePath) { FileExplorerState(initialDir) }
}

// ────────────────────────────────────────────────────────
// Main Entry Point — now tiny, delegates everything
// ────────────────────────────────────────────────────────

@Composable
fun FileExplorerScreen(
    isDark: Boolean,
    viewModel: LauncherViewModel? = null,
    // Optional starting folder — e.g. when opened by double-clicking a folder on the
    // Desktop, this is that folder's path instead of always defaulting to the storage root.
    startPath: String? = null
) {
    val context = LocalContext.current
    var hasStoragePermission by remember { mutableStateOf(hasStorageAccess(context)) }

    if (!hasStoragePermission) {
        PermissionGate(
            isDark = isDark,
            onPermissionGranted = { hasStoragePermission = true }
        )
        return
    }

    val startDir = remember(startPath, viewModel) {
        val resolved = startPath?.let { raw ->
            if (raw.startsWith("content://")) {
                viewModel?.resolveSafUriToFilePath(context, Uri.parse(raw))
            } else raw
        }
        val f = resolved?.let { File(it) }
        if (f != null && f.isDirectory) f else Environment.getExternalStorageDirectory()
    }
    FileExplorerContent(isDark = isDark, viewModel = viewModel, startDir = startDir)
}

// ────────────────────────────────────────────────────────
// Permission Gate — extracted composable
// ────────────────────────────────────────────────────────

@Composable
private fun PermissionGate(
    isDark: Boolean,
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    val bgColor = if (isDark) Color(0xFF1C1C1C) else Color(0xFFFAFAFA)

    val legacyPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted || hasStorageAccess(context)) onPermissionGranted()
    }

    val mediaPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms.values.any { it } || hasStorageAccess(context)) onPermissionGranted()
    }

    fun requestPermission() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                val intent = Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                mediaPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_AUDIO
                    )
                )
            }
            else -> legacyPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    LaunchedEffect(Unit) { requestPermission() }

    Box(Modifier.fillMaxSize().background(bgColor), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
PngIcon(res = R.drawable.ic_folder_locked, modifier = Modifier.size(48.dp))
            Text("Storage access required", color = textColor, style = MaterialTheme.typography.titleMedium)
            Text(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                    "Tap below to grant 'All files access' in Settings"
                else
                    "Tap below to grant storage permission",
                color = textColor.copy(alpha = 0.6f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Button(onClick = { requestPermission() }) {
                Text(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) "Open Settings"
                    else "Grant Storage Access"
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                OutlinedButton(onClick = {
                    if (hasStorageAccess(context)) onPermissionGranted()
                }) {
                    Text("I've granted access — retry")
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────
// Main Content — extracted composable
// ────────────────────────────────────────────────────────

@Composable
private fun FileExplorerContent(
    isDark: Boolean,
    viewModel: LauncherViewModel?,
    startDir: File = Environment.getExternalStorageDirectory()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val appDataRoot = remember(context) { BluebirdStorage.appDataRoot(context) }
    val state = rememberFileExplorerState(startDir)
    // Shared clipboard state (same one Desktop reads/writes) — collected here so the
    // Ribbon's paste button reacts live to a cut/copy made on Desktop or in another window.
    val vmUiState = viewModel?.uiState?.collectAsStateWithLifecycle()?.value

    val textColor = if (isDark) ExplorerDarkText else ExplorerLightText
    val bgColor = if (isDark) ExplorerDarkBackground else ExplorerLightBackground
    val surfaceBg = if (isDark) ExplorerDarkSurface else ExplorerLightSurface
    val navBg = if (isDark) ExplorerDarkNav else ExplorerLightNav
    val contentBorder = textColor.copy(alpha = if (isDark) 0.10f else 0.08f)

    // ── Auto-collapsing nav pane ────────────────────────────────────────────
    // Same pattern as the Settings screen's nav rail: one progress value
    // drives both the width and the label fade, so they're always in sync —
    // two independently-timed animations (an instant show/hide plus a
    // separately-tweened width) is what causes a collapse to visibly
    // "snap" partway through. This stays fully open while the pane itself
    // is being scrolled, and re-opens instantly on any tap inside it.
    val navScrollState = rememberScrollState()
    var navInteractionTick by remember { mutableStateOf(0) }
    var isNavExpanded by remember { mutableStateOf(true) }
    val pokeNav: () -> Unit = { navInteractionTick++ }
    LaunchedEffect(navInteractionTick, navScrollState.isScrollInProgress) {
        isNavExpanded = true
        if (!navScrollState.isScrollInProgress) {
            delay(3000)
            isNavExpanded = false
        }
    }
    val navExpandProgress by animateFloatAsState(
        targetValue = if (isNavExpanded) 1f else 0f,
        animationSpec = tween(320, easing = FastOutSlowInEasing),
        label = "explorerNavExpandProgress"
    )
    val navWidth = lerp(64.dp, 214.dp, navExpandProgress)
    // Labels fade out before the pane is visibly cramped, and only fade back
    // in once it's mostly re-opened, so text is never shown squeezed.
    val navLabelAlpha = ((navExpandProgress - 0.35f) / 0.65f).coerceIn(0f, 1f)

    val explorerFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var ctrlMouseSelection by remember { mutableStateOf(false) }
    var shiftMouseSelection by remember { mutableStateOf(false) }
    var selectionAnchor by remember { mutableStateOf<String?>(null) }

    // Desktop keyboard commands. The explorer owns these only while its root has focus;
    // text fields such as Search keep their normal editing shortcuts.
    fun selectedItems(): List<RealFileItem> = state.files.filter { it.file.absolutePath in state.selectedFiles }
    fun primarySelection(): RealFileItem? = selectedItems().firstOrNull()
    fun openSelection() {
        primarySelection()?.let {
            if (it.isDirectory) state.navigateTo(it.file) else openFileFromExplorer(it, context, viewModel)
        }
    }
    fun beginRename() {
        primarySelection()?.let {
            state.renamingPath = it.file.absolutePath
            state.contextMenuFile = null
        }
    }
    fun beginDelete() {
        if (state.selectedFiles.isNotEmpty()) {
            state.contextMenuFile = null
            state.showDeleteDialog = true
        }
    }

    fun moveSelection(delta: Int, extend: Boolean = false) {
        val items = state.files
        if (items.isEmpty()) return
        val current = state.selectedFiles.firstOrNull()?.let { path ->
            items.indexOfFirst { it.file.absolutePath == path }
        } ?: -1
        val next = when {
            current < 0 -> if (delta >= 0) 0 else items.lastIndex
            else -> (current + delta).coerceIn(0, items.lastIndex)
        }
        val nextPath = items[next].file.absolutePath
        if (extend) {
            val anchor = selectionAnchor ?: state.selectedFiles.firstOrNull() ?: nextPath
            val anchorIndex = items.indexOfFirst { it.file.absolutePath == anchor }.takeIf { it >= 0 } ?: next
            val range = if (anchorIndex <= next) anchorIndex..next else next..anchorIndex
            state.selectedFiles = range.mapTo(linkedSetOf()) { items[it].file.absolutePath }
            selectionAnchor = anchor
        } else {
            state.selectedFiles = setOf(nextPath)
            selectionAnchor = nextPath
        }
    }

    // FIX: the explorer root requests focus so hardware-keyboard shortcuts (Ctrl+C,
    // Delete, arrow-key navigation) work without needing a click first — but focusing
    // a plain non-text-editable node like this can still make Android pop the on-screen
    // keyboard on some devices, since focus and IME visibility aren't strictly coupled
    // to "is this actually a text field". Explicitly hiding it right after keeps the
    // shortcut-handling focus while stopping the keyboard from appearing uninvited.
    // The Search field is unaffected — it only shows the keyboard when the user
    // actually taps into it, same as Media Player's search box already does.
    LaunchedEffect(Unit) {
        explorerFocusRequester.requestFocus()
        keyboardController?.hide()
    }

    val quickAccess = remember {
        listOf(
            "Downloads" to Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "Documents" to Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "Pictures" to Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "Music" to Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            "Movies" to Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            "DCIM" to Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "Internal Storage" to Environment.getExternalStorageDirectory()
        ).filter { (_, dir) -> dir.exists() }
    }

    // ── File Loading ──
    // Windows 11 Explorer never blanks the pane to a spinner when you create,
    // delete, or rename something inside the folder you're already looking at —
    // the list just gains or loses a row in place. It only shows a loading state
    // when you *navigate* into a folder it hasn't read yet. `silent = true` is
    // used for refreshes triggered by our own file operations (new folder, delete,
    // rename, paste) so `state.isLoading` — which drives the full-pane spinner in
    // FileListArea — is left untouched; `silent = false` (the default, used by the
    // navigation LaunchedEffect below) shows it normally.
    var loadGeneration by remember { mutableIntStateOf(0) }
    val loadFiles: (File, Boolean) -> Unit = { dir, silent ->
        val generation = loadGeneration + 1
        loadGeneration = generation
        scope.launch {
            if (!silent) state.isLoading = true
            withContext(Dispatchers.IO) {
                try {
                    if (!dir.exists() || !dir.canRead()) {
                        withContext(Dispatchers.Main.immediate) { state.allFiles = emptyList() }
                        return@withContext
                    }
                    // Metadata-only scan. Expensive .exe/.bpk artwork is deliberately
                    // not decoded here; package icons are loaded lazily by visible tiles.
                    val rawFiles = dir.listFiles()
                    val loaded = if (rawFiles == null) {
                        emptyList()
                    } else {
                        rawFiles.asSequence()
                            .filter { state.showHidden || !it.name.startsWith(".") }
                            .map { file -> RealFileItem(file) }
                            .toList()
                    }
                    withContext(Dispatchers.Main.immediate) {
                        if (generation == loadGeneration && dir == state.currentDir) {
                            // Publish one immutable snapshot. Search and sorting are derived locally.
                            state.allFiles = loaded
                            val loadedPaths = loaded.asSequence().map { it.file.absolutePath }.toHashSet()
                            state.selectedFiles = state.selectedFiles.filterTo(linkedSetOf()) { it in loadedPaths }
                            // An inline rename field that's still open for a file which just
                            // vanished from disk (deleted elsewhere, or the rename's own
                            // renameTo() already succeeded) would otherwise be left floating
                            // with nothing backing it — drop it along with the stale selection.
                            if (state.renamingPath != null && state.renamingPath !in loadedPaths) {
                                state.renamingPath = null
                            }
                        }
                    }
                } catch (e: SecurityException) {
                    withContext(Dispatchers.Main.immediate) { state.allFiles = emptyList() }
                } finally {
                    withContext(Dispatchers.Main.immediate) {
                        if (generation == loadGeneration && dir == state.currentDir) {
                            state.isLoading = false
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(state.currentDir, state.showHidden) {
        loadFiles(state.currentDir, false)
    }

    // Windows 11 Explorer silently re-reads the folder whenever its window regains
    // focus, so deleting an app (or a file) from outside Explorer — e.g. finishing
    // an uninstall confirmation, or a share-sheet action that deletes a temp copy —
    // is reflected the moment you're looking at the list again, with no manual
    // refresh needed. ON_RESUME is the Android equivalent of "window regained focus".
    // This is also silent: focus regaining shouldn't blank the list either.
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, state.currentDir) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                loadFiles(state.currentDir, true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Auto-refresh this folder's listing once a copy/move job that targets it finishes —
    // paste/copy now runs asynchronously in the shared engine instead of the old
    // synchronous copyTo() that could refresh inline right after.
    LaunchedEffect(vmUiState?.copyJobs, state.currentDir) {
        val justFinished = vmUiState?.copyJobs?.any {
            it.destDir == state.currentDir.absolutePath &&
                it.status != io.github.norbertweb.bluebird.CopyJobStatus.RUNNING &&
                it.status != io.github.norbertweb.bluebird.CopyJobStatus.SCANNING
        } ?: false
        if (justFinished) loadFiles(state.currentDir, true)
    }

    // Real Windows 11 Explorer never asks "what do you want to call it?" up front —
    // New Folder appears instantly as "New folder" (auto-numbered if that name is
    // taken) and is immediately in rename mode so you can just start typing over
    // it. No modal, no extra tap to confirm a name you're likely to change anyway.
    fun createNewFolder() {
        var name = "New folder"
        var suffix = 2
        while (File(state.currentDir, name).exists()) {
            name = "New folder ($suffix)"
            suffix++
        }
        val folder = File(state.currentDir, name)
        if (folder.mkdir()) {
            state.selectedFiles = setOf(folder.absolutePath)
            state.renamingPath = folder.absolutePath
            loadFiles(state.currentDir, true)
        } else {
            android.widget.Toast.makeText(context, "Could not create folder", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // ── Layout ──
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .focusRequester(explorerFocusRequester)
            .onKeyEvent { event ->
                // Keep modifier state available to pointer clicks so Ctrl/Shift-click
                // behaves like a desktop file manager. Key events are delivered while
                // this root owns focus, including when the pointer is over a file tile.
                ctrlMouseSelection = event.nativeKeyEvent.isCtrlPressed
                shiftMouseSelection = event.nativeKeyEvent.isShiftPressed
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                val ctrl = event.nativeKeyEvent.isCtrlPressed
                val shift = event.nativeKeyEvent.isShiftPressed
                val alt = event.nativeKeyEvent.isAltPressed
                when {
                    ctrl && event.key == Key.A -> {
                        state.selectedFiles = state.files.mapTo(linkedSetOf()) { it.file.absolutePath }
                        selectionAnchor = state.files.firstOrNull()?.file?.absolutePath
                        true
                    }
                    ctrl && event.key == Key.C -> {
                        if (state.selectedFiles.isNotEmpty()) viewModel?.setClipboard(state.selectedFiles.map { File(it) }, cut = false)
                        true
                    }
                    ctrl && event.key == Key.X -> {
                        if (state.selectedFiles.isNotEmpty()) viewModel?.setClipboard(state.selectedFiles.map { File(it) }, cut = true)
                        true
                    }
                    ctrl && event.key == Key.V -> {
                        viewModel?.pasteClipboard(state.currentDir)
                        true
                    }
                    event.key == Key.F2 -> { beginRename(); true }
                    event.key == Key.Delete -> { beginDelete(); true }
                    event.key == Key.Enter -> { openSelection(); true }
                    event.key == Key.DirectionDown || event.key == Key.DirectionRight -> {
                        moveSelection(1, extend = shift); true
                    }
                    event.key == Key.DirectionUp || event.key == Key.DirectionLeft -> {
                        moveSelection(-1, extend = shift); true
                    }
                    event.key == Key.MoveHome -> {
                        state.files.firstOrNull()?.let { state.selectedFiles = setOf(it.file.absolutePath); selectionAnchor = it.file.absolutePath }; true
                    }
                    event.key == Key.MoveEnd -> {
                        state.files.lastOrNull()?.let { state.selectedFiles = setOf(it.file.absolutePath); selectionAnchor = it.file.absolutePath }; true
                    }
                    event.key == Key.Escape -> {
                        if (state.renamingPath != null) {
                            // Windows 11: Escape while renaming only cancels the rename
                            // and keeps the item selected — it doesn't also clear selection.
                            state.renamingPath = null
                        } else {
                            state.contextMenuFile = null
                            state.selectedFiles = emptySet()
                            state.showDeleteDialog = false
                            state.propertiesTarget = null
                        }
                        true
                    }
                    alt && event.key == Key.DirectionUp -> { state.goUp(); true }
                    event.key == Key.Backspace && !ctrl && !shift -> { state.goBack(); true }
                    else -> false
                }
            }
    ) {

        CommandBar(
            onBack = { state.goBack() },
            onUp = { state.goUp() },
            onRefresh = { loadFiles(state.currentDir, true) },
            pathParts = when {
                state.isHomeDashboard -> listOf("Home" to Environment.getExternalStorageDirectory())
                state.isDeviceDashboard -> listOf("This Device" to Environment.getExternalStorageDirectory())
                else -> remember(state.currentDir, appDataRoot.absolutePath) {
                    val currentPath = state.currentDir.absolutePath
                    val appDataPath = appDataRoot.absolutePath
                    val externalRoot = Environment.getExternalStorageDirectory()
                    val parts = mutableListOf<Pair<String, File>>()
                    val breadcrumbRoot: File
                    val breadcrumbLabel: String
                    if (currentPath == appDataPath || currentPath.startsWith(appDataPath + File.separator)) {
                        breadcrumbRoot = appDataRoot
                        breadcrumbLabel = "App Data"
                    } else {
                        breadcrumbRoot = externalRoot
                        breadcrumbLabel = "Home"
                    }
                    parts.add(breadcrumbLabel to breadcrumbRoot)
                    val relative = currentPath.removePrefix(breadcrumbRoot.absolutePath).trimStart(File.separatorChar)
                    var curr = breadcrumbRoot
                    relative.split(File.separatorChar).filter { it.isNotEmpty() }.forEach { part ->
                        curr = File(curr, part)
                        parts.add(part to curr)
                    }
                    parts
                }
            },
            onNavigate = { dir ->
                if (state.isDeviceDashboard && dir == Environment.getExternalStorageDirectory()) state.navigateDevice()
                else state.navigateTo(dir)
            },
            isGridView = state.isGridView,
            onToggleView = { state.isGridView = !state.isGridView },
            searchQuery = state.searchQuery,
            onSearchChange = { state.searchQuery = it },
            searchHome = state.isHomeDashboard || state.isDeviceDashboard,
            textColor = textColor,
            surfaceBg = surfaceBg,
            isDark = isDark
        )

        Ribbon(
            onNewFolder = { createNewFolder() },
            onCut = {
                if (state.selectedFiles.isNotEmpty() && viewModel != null) {
                    viewModel.setClipboard(state.selectedFiles.map { File(it) }, cut = true)
                }
            },
            onCopy = {
                if (state.selectedFiles.isNotEmpty() && viewModel != null) {
                    viewModel.setClipboard(state.selectedFiles.map { File(it) }, cut = false)
                }
            },
            onPaste = {
                viewModel?.pasteClipboard(state.currentDir)
            },
            onRename = { beginRename() },
            onDelete = {
                if (state.selectedFiles.isNotEmpty()) {
                    state.showDeleteDialog = true
                }
            },
            onSelectAll = {
                state.selectedFiles = if (state.selectedFiles.size == state.files.size) emptySet()
                else state.files.mapTo(linkedSetOf()) { it.file.absolutePath }
            },
            sortBy = state.sortBy,
            sortAscending = state.sortAscending,
            onSortChange = { by, asc -> state.sortBy = by; state.sortAscending = asc },
            showHidden = state.showHidden,
            onToggleHidden = { state.showHidden = !state.showHidden },
            hasSelection = state.selectedFiles.isNotEmpty(),
            isSingleSelection = state.selectedFiles.size == 1,
            clipboardActive = vmUiState?.clipboardFiles?.isNotEmpty() ?: false,
            textColor = textColor,
            surfaceBg = surfaceBg,
            isDark = isDark,
            itemCount = state.files.size
        )

        Row(modifier = Modifier
            .fillMaxWidth()
            .weight(1f) ) {
            NavigationPane(
                quickAccess = quickAccess,
                currentDir = state.currentDir,
                isHomeDashboard = state.isHomeDashboard,
                isDeviceDashboard = state.isDeviceDashboard,
                appDataRoot = appDataRoot,
                onNavigate = { state.navigateTo(it) },
                onHome = { state.navigateHome() },
                onDevice = { state.navigateDevice() },
                textColor = textColor,
                navBg = navBg,
                surfaceBg = surfaceBg,
                isDark = isDark,
                navWidth = navWidth,
                expandProgress = navExpandProgress,
                labelAlpha = navLabelAlpha,
                scrollState = navScrollState,
                onInteract = pokeNav
            )

            if (state.isHomeDashboard) {
                HomeDashboard(
                    quickAccess = quickAccess,
                    textColor = textColor,
                    bgColor = bgColor,
                    surfaceBg = surfaceBg,
                    isDark = isDark,
                    context = context,
                    viewModel = viewModel,
                    onNavigate = { state.navigateTo(it) }
                )
            } else if (state.isDeviceDashboard) {
                DeviceStorageDashboard(
                    textColor = textColor,
                    bgColor = bgColor,
                    surfaceBg = surfaceBg,
                    isDark = isDark,
                    onNavigate = { state.navigateTo(it) }
                )
            } else {
                FileListArea(
                    state = state,
                    bgColor = bgColor,
                    surfaceBg = surfaceBg,
                    textColor = textColor,
                    isDark = isDark,
                    context = context,
                    viewModel = viewModel,
                    onBackgroundContextMenu = { offset ->
                        state.contextMenuOffset = offset
                        state.showBackgroundContextMenu = true
                        state.contextMenuFile = null
                    },
                    onReload = { loadFiles(state.currentDir, true) }
                )
            }
        }

        StatusBar(
            textColor = textColor,
            itemCount = when {
                state.isHomeDashboard -> quickAccess.size
                state.isDeviceDashboard -> 1
                else -> state.files.size
            },
            selectedCount = if (state.isHomeDashboard || state.isDeviceDashboard) 0 else state.selectedFiles.size
        )
    }

    // ── Overlays (dialogs, menus) — extracted ──
    FileExplorerDialogs(
        state = state,
        viewModel = viewModel,
        isDark = isDark,
        context = context,
        clipboardActive = vmUiState?.clipboardFiles?.isNotEmpty() == true,
        onReload = { loadFiles(state.currentDir, true) },
        onCreateNewFolder = { createNewFolder() }
    )
}

// ────────────────────────────────────────────────────────
// Home Dashboard — Bluebird-native landing page
// ────────────────────────────────────────────────────────
private data class HomeQuickLink(
    val label: String,
    val directory: File,
    val icon: Int
)

@Composable
private fun HomeDashboard(
    quickAccess: List<Pair<String, File>>,
    textColor: Color,
    bgColor: Color,
    surfaceBg: Color,
    isDark: Boolean,
    context: android.content.Context,
    viewModel: LauncherViewModel?,
    onNavigate: (File) -> Unit
) {
    val homeLinks = remember(quickAccess) {
        val iconFor = mapOf(
            "Downloads" to R.drawable.ic_explorer_downloads,
            "Documents" to R.drawable.ic_explorer_documents,
            "Pictures" to R.drawable.ic_explorer_pictures,
            "Music" to R.drawable.ic_explorer_music,
            "Movies" to R.drawable.ic_explorer_videos,
            "DCIM" to R.drawable.ic_explorer_pictures,
            "Internal Storage" to R.drawable.ic_explorer_storage
        )
        quickAccess
            .filter { it.first != "DCIM" && it.first != "Internal Storage" }
            .mapNotNull { (label, dir) ->
                if (!dir.exists()) null else HomeQuickLink(label, dir, iconFor[label] ?: R.drawable.ic_explorer_storage)
            }
    }

    var recentFiles by remember { mutableStateOf<List<RealFileItem>>(emptyList()) }
    val recentRootsKey = remember(quickAccess) { quickAccess.map { it.second.absolutePath } }

    LaunchedEffect(recentRootsKey) {
        recentFiles = withContext(Dispatchers.IO) {
            quickAccess
                .filter { it.second.exists() && it.second.isDirectory }
                .flatMap { (_, dir) ->
                    runCatching {
                        dir.listFiles()
                            ?.asSequence()
                            ?.filter { it.isFile && !it.name.startsWith(".") }
                            ?.map { RealFileItem(it) }
                            ?.toList()
                            ?: emptyList()
                    }.getOrDefault(emptyList())
                }
                .sortedByDescending { it.lastModified }
                .take(6)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .verticalScroll(rememberScrollState())
    ) {
        val columns = if (maxWidth >= 760.dp) 3 else 2
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) ExplorerDarkSurface else Color(0xFFF0F5FB),
                border = BorderStroke(1.dp, if (isDark) ExplorerDarkBorder else ExplorerLightBorder)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(13.dp),
                        color = ExplorerAccentBlue.copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.ic_explorer_home),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Welcome to Bluebird Explorer",
                            color = textColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "Quick access to your folders and recent files",
                            color = textColor.copy(alpha = 0.58f),
                            fontSize = 12.5.sp
                        )
                    }
                }
            }

            Text(
                "Quick access",
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            homeLinks.chunked(columns).forEach { rowLinks ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowLinks.forEach { link ->
                        Surface(
                            modifier = Modifier.weight(1f).clickable { onNavigate(link.directory) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) ExplorerDarkSurface else ExplorerLightField,
                            border = BorderStroke(1.dp, if (isDark) ExplorerDarkBorder else ExplorerLightBorder)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(11.dp)
                            ) {
                                Icon(
                                    painter = painterResource(link.icon),
                                    contentDescription = link.label,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(25.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        link.label,
                                        color = textColor,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "This Device",
                                        color = textColor.copy(alpha = 0.48f),
                                        fontSize = 10.5.sp,
                                        maxLines = 1
                                    )
                                }
                                Icon(
                                    FluentIcons.Regular.ChevronRight,
                                    contentDescription = null,
                                    tint = textColor.copy(alpha = 0.35f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                    repeat(columns - rowLinks.size) { Spacer(Modifier.weight(1f)) }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Recent files",
                    color = textColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (recentFiles.size > 0) {
                    Text(
                        "${recentFiles.size} items",
                        color = textColor.copy(alpha = 0.45f),
                        fontSize = 11.sp
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) ExplorerDarkSurface else ExplorerLightField,
                border = BorderStroke(1.dp, if (isDark) ExplorerDarkBorder else ExplorerLightBorder)
            ) {
                if (recentFiles.isEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recent files will appear here",
                            color = textColor.copy(alpha = 0.48f),
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        recentFiles.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (item.isDirectory) onNavigate(item.file)
                                        else openFileFromExplorer(item, context, viewModel)
                                    }
                                    .padding(horizontal = 13.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
PngIcon(res = fileIconRes(item.isDirectory, item.extension), modifier = Modifier.size(22.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        item.name,
                                        color = textColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        item.file.parentFile?.name ?: "This Device",
                                        color = textColor.copy(alpha = 0.45f),
                                        fontSize = 10.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    formatDate(item.lastModified),
                                    color = textColor.copy(alpha = 0.42f),
                                    fontSize = 10.5.sp,
                                    maxLines = 1
                                )
                            }
                            if (index < recentFiles.lastIndex) {
                                HorizontalDivider(color = textColor.copy(alpha = 0.055f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────
// File List Area — extracted composable
// ────────────────────────────────────────────────────────

// A Compose Popup's PopupPositionProvider always works in *window-relative*
// coordinates (its `windowSize` param is the window's size, not the screen's).
// The previous helper added the view's on-screen location on top of that,
// double-counting the window's own screen offset — every context menu opened
// with a fixed, wrong offset whenever this Explorer window wasn't drawn at the
// screen's (0,0), which is normal for a windowed/multi-window desktop shell.
// Plain positionInWindow() is the correct, sufficient value.
private fun LayoutCoordinates.windowRelativePosition(): Offset = positionInWindow()

@Composable
private fun FileListArea(
    state: FileExplorerState,
    bgColor: Color,
    surfaceBg: Color,
    textColor: Color,
    isDark: Boolean,
    context: android.content.Context,
    viewModel: LauncherViewModel?,
    onBackgroundContextMenu: (Offset) -> Unit,
    onReload: () -> Unit
) {
    var listOrigin by remember { mutableStateOf(Offset.Zero) }

    // Shared by both the grid and list rename fields: validates + performs the
    // rename, updates selection/renaming state, and reloads the folder so the
    // sorted listing (and any icon it affects, e.g. extension change) is correct.
    fun commitRename(fileItem: RealFileItem, newName: String): Boolean {
        val target = fileItem.file
        return commitInlineRename(target, newName, context) { dest ->
            state.renamingPath = null
            state.selectedFiles = state.selectedFiles.mapTo(linkedSetOf()) {
                if (it == target.absolutePath) dest.absolutePath else it
            }
            onReload()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxHeight()
            .background(bgColor)
            .onGloballyPositioned { coords ->
                listOrigin = coords.windowRelativePosition()
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onBackgroundContextMenu(listOrigin + it) }
                )
            }
    ) {
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
PngIcon(res = R.drawable.ic_folder_open, modifier = Modifier.size(34.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Loading", color = textColor.copy(alpha = 0.5f), fontSize = 12.5.sp)
                    }
                }
            }
            state.files.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = surfaceBg.copy(alpha = 0.55f)
                        ) {
                            if (state.searchQuery.isNotBlank()) {
                                Icon(
                                    FluentIcons.Regular.Search,
                                    null,
                                    tint = textColor.copy(alpha = 0.25f),
                                    modifier = Modifier.padding(14.dp).size(30.dp)
                                )
                            } else {
                                PngIcon(res = R.drawable.ic_folder_empty, modifier = Modifier.padding(14.dp).size(34.dp))
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            if (state.searchQuery.isNotBlank()) "No matching files" else "This folder is empty",
                            color = textColor.copy(alpha = 0.55f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (state.searchQuery.isNotBlank()) {
                            Text(
                                "Try a different search",
                                color = textColor.copy(alpha = 0.35f),
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }
            }
            state.isGridView -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 128.dp),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(state.files, key = { it.file.absolutePath }) { fileItem ->
                        GridFileItem(
                            item = fileItem,
                            isSelected = fileItem.file.absolutePath in state.selectedFiles,
                            isRenaming = fileItem.file.absolutePath == state.renamingPath,
                            textColor = textColor,
                            isDark = isDark,
                            onClick = {
                                val path = fileItem.file.absolutePath
                                val index = state.files.indexOfFirst { it.file.absolutePath == path }
                                // GridFileItem is an extracted composable and does not own
                                // the Explorer keyboard-modifier state. Keep pointer selection
                                // deterministic here; Ctrl/Shift range selection remains available
                                // through the Explorer keyboard navigation path.
                                state.selectedFiles = setOf(path)
                            },
                            onDoubleClick = {
                                if (fileItem.isDirectory) state.navigateTo(fileItem.file)
                                else openFileFromExplorer(fileItem, context, viewModel)
                            },
                            onLongPress = { offset -> state.contextMenuOffset = offset; state.contextMenuFile = fileItem },
                            onCommitRename = { newName -> commitRename(fileItem, newName) },
                            onCancelRename = { state.renamingPath = null }
                        )
                    }
                }
            }
            else -> {
                Column {
                    FileListHeader(surfaceBg = surfaceBg, textColor = textColor)
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(state.files, key = { it.file.absolutePath }) { fileItem ->
                            ListFileItem(
                                item = fileItem,
                                isSelected = fileItem.file.absolutePath in state.selectedFiles,
                                isRenaming = fileItem.file.absolutePath == state.renamingPath,
                                textColor = textColor,
                                isDark = isDark,
                                onClick = {
                                    val path = fileItem.file.absolutePath
                                    state.selectedFiles = setOf(path)
                                },
                                onDoubleClick = {
                                    if (fileItem.isDirectory) state.navigateTo(fileItem.file)
                                    else openFileFromExplorer(fileItem, context, viewModel)
                                },
                                onLongPress = { offset -> state.contextMenuOffset = offset; state.contextMenuFile = fileItem },
                                onCommitRename = { newName -> commitRename(fileItem, newName) },
                                onCancelRename = { state.renamingPath = null }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FileListHeader(surfaceBg: Color, textColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .background(surfaceBg)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Name", color = textColor.copy(alpha = 0.62f), fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(16.dp))
        Text("Date modified", color = textColor.copy(alpha = 0.58f), fontSize = 11.sp, modifier = Modifier.width(120.dp))
        Spacer(Modifier.width(12.dp))
        Text("Type", color = textColor.copy(alpha = 0.58f), fontSize = 11.sp, modifier = Modifier.width(64.dp))
        Spacer(Modifier.width(12.dp))
        Text("Size", color = textColor.copy(alpha = 0.58f), fontSize = 11.sp, modifier = Modifier.width(62.dp), textAlign = TextAlign.End)
    }
}

// ────────────────────────────────────────────────────────
// All Dialogs — extracted composable
// ────────────────────────────────────────────────────────

@Composable
private fun FileExplorerDialogs(
    state: FileExplorerState,
    viewModel: LauncherViewModel?,
    isDark: Boolean,
    context: android.content.Context,
    clipboardActive: Boolean,
    onReload: () -> Unit,
    onCreateNewFolder: () -> Unit
) {
    // Context Menu
    state.contextMenuFile?.let { fileItem ->
        val isPartOfMultiSelection = fileItem.file.absolutePath in state.selectedFiles && state.selectedFiles.size > 1
        val selectionCount = if (isPartOfMultiSelection) state.selectedFiles.size else 1
        FileContextMenu(
            fileItem = fileItem,
            selectionCount = selectionCount,
            offset = state.contextMenuOffset,
            isDark = isDark,
            onDismiss = { state.contextMenuFile = null },
            onOpen = {
                state.contextMenuFile = null
                openFileFromExplorer(fileItem, context, viewModel)
            },
            onCopy = {
                val targets = if (fileItem.file.absolutePath in state.selectedFiles && state.selectedFiles.size > 1)
                    state.selectedFiles.map { File(it) } else listOf(fileItem.file)
                viewModel?.setClipboard(targets, cut = false)
                state.contextMenuFile = null
            },
            onCut = {
                val targets = if (fileItem.file.absolutePath in state.selectedFiles && state.selectedFiles.size > 1)
                    state.selectedFiles.map { File(it) } else listOf(fileItem.file)
                viewModel?.setClipboard(targets, cut = true)
                state.contextMenuFile = null
            },
            onPaste = if (fileItem.isDirectory && clipboardActive) ({
                viewModel?.pasteClipboard(fileItem.file)
                state.contextMenuFile = null
            }) else null,
            onRename = { state.renamingPath = fileItem.file.absolutePath; state.contextMenuFile = null },
            onDelete = { state.showDeleteDialog = true },
            onCreateShortcut = {
                viewModel?.addDesktopShortcutFromFile(fileItem.file.absolutePath, fileItem.name)
                state.contextMenuFile = null
            },
            onProperties = {
                state.propertiesTarget = fileItem
                state.contextMenuFile = null
            }
        )
    }

    if (state.showBackgroundContextMenu) {
        ExplorerBackgroundContextMenu(
            offset = state.contextMenuOffset,
            isDark = isDark,
            hasPaste = clipboardActive,
            onDismiss = { state.showBackgroundContextMenu = false },
            onRefresh = { onReload() },
            onPaste = { viewModel?.pasteClipboard(state.currentDir) },
            onNewFolder = { onCreateNewFolder() },
            onNewTextFile = {
                var index = 0
                var name: String
                do {
                    name = if (index == 0) "New Text Document.txt" else "New Text Document ($index).txt"
                    index++
                } while (File(state.currentDir, name).exists())
                val created = File(state.currentDir, name)
                if (runCatching { created.createNewFile() }.getOrDefault(false)) {
                    // Same "land straight in rename mode" treatment as New Folder —
                    // Explorer doesn't make you confirm a name you'll likely retype anyway.
                    state.selectedFiles = setOf(created.absolutePath)
                    state.renamingPath = created.absolutePath
                }
                onReload()
            },
            onSelectAll = { state.selectedFiles = state.files.mapTo(linkedSetOf()) { it.file.absolutePath } },
            onToggleHidden = { state.showHidden = !state.showHidden }
        )
    }

    // Delete Dialog — deletes the right-clicked file (or the whole multi-selection, if
    // the right-clicked file is part of it), matching real Explorer's selection semantics.
    val deleteTargets: List<File> = if (state.showDeleteDialog) {
        val ctx = state.contextMenuFile
        if (ctx != null) {
            if (ctx.file.absolutePath in state.selectedFiles && state.selectedFiles.size > 1)
                state.selectedFiles.map { File(it) } else listOf(ctx.file)
        } else {
            state.selectedFiles.map { File(it) }
        }
    } else emptyList()

    if (state.showDeleteDialog && deleteTargets.isNotEmpty()) {
        DeleteDialog(
            targets = deleteTargets,
            isDark = isDark,
            onConfirm = {
                deleteTargets.forEach { viewModel?.deleteToRecycleBin(it.absolutePath) }
                val label = if (deleteTargets.size == 1) "Deleted \"${deleteTargets[0].name}\"" else "Deleted ${deleteTargets.size} items"
                viewModel?.showUndoAction(label) {
                    deleteTargets.forEach { viewModel.restoreFromRecycleBinByOriginalPath(it.absolutePath) }
                }
                state.showDeleteDialog = false
                state.contextMenuFile = null
                onReload()
            },
            onDismiss = { state.showDeleteDialog = false; state.contextMenuFile = null }
        )
    }

    state.propertiesTarget?.let { target ->
        FilePropertiesDialog(
            target = target,
            isDark = isDark,
            onDismiss = { state.propertiesTarget = null }
        )
    }

}

// ────────────────────────────────────────────────────────
// Individual Dialog Composables
// ────────────────────────────────────────────────────────

@Composable
private fun DeleteDialog(
    targets: List<File>,
    isDark: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = rememberWin11DialogColors(isDark)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.bg,
        shape = RoundedCornerShape(8.dp),
        title = { Text("Delete", color = colors.text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp) },
        text = {
            Text(
                if (targets.size == 1) "Move '${targets[0].name}' to Recycle Bin?"
                else "Move ${targets.size} items to Recycle Bin?",
                color = colors.text.copy(alpha = 0.85f),
                fontSize = 13.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = bluebirdColors.DangerRed)
            ) { Text("Delete") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(4.dp)) { Text("Cancel") }
        }
    )
}

// ────────────────────────────────────────────────────────
// Shared Win11-style dialog chrome — rounded corners, accent-colored focus
// ring/buttons, and a consistent surface color instead of Material3's defaults.
// ────────────────────────────────────────────────────────

private data class Win11DialogColors(val bg: Color, val text: Color, val accent: Color, val fieldBg: Color, val border: Color)

@Composable
private fun rememberWin11DialogColors(isDark: Boolean): Win11DialogColors {
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    val bg = if (isDark) ExplorerDarkSurface else ExplorerLightSurface
    val fieldBg = if (isDark) ExplorerDarkField else Color(0xFFFEFEFE)
    val border = textColor.copy(alpha = if (isDark) 0.16f else 0.18f)
    return Win11DialogColors(bg, textColor, bluebirdColors.AccentBlue, fieldBg, border)
}

@Composable
private fun Win11Dialog(
    isDark: Boolean,
    title: String,
    onDismissRequest: () -> Unit,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    content: @Composable (Win11DialogColors) -> Unit
) {
    val colors = rememberWin11DialogColors(isDark)
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = colors.bg,
        shape = RoundedCornerShape(8.dp),
        title = { Text(title, color = colors.text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp) },
        text = { content(colors) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
            ) { Text(confirmLabel) }
        },
        dismissButton = {
            OutlinedButton(onClick = onCancel, shape = RoundedCornerShape(4.dp)) { Text("Cancel") }
        }
    )
}

@Composable
private fun Win11TextField(
    value: androidx.compose.ui.text.input.TextFieldValue,
    onValueChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit,
    colors: Win11DialogColors,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(fontSize = 13.5.sp, fontWeight = FontWeight.Medium),
        shape = RoundedCornerShape(4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.accent,
            unfocusedBorderColor = colors.border,
            focusedTextColor = colors.text,
            unfocusedTextColor = colors.text,
            cursorColor = colors.accent,
            focusedContainerColor = colors.fieldBg,
            unfocusedContainerColor = colors.fieldBg
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun FilePropertiesDialog(
    target: RealFileItem,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val file = target.file
    val type = when {
        target.isDirectory -> "Folder"
        target.extension.isBlank() -> "File"
        else -> "${target.extension.uppercase()} file"
    }
    val modified = remember(target.lastModified) {
        if (target.lastModified > 0L) formatDate(target.lastModified) else "Unknown"
    }
    val location = file.parentFile?.absolutePath ?: file.absolutePath

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(14.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        PngIcon(res = fileIconRes(target.isDirectory, target.extension), modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(target.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                    Text(type, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PropertyRow("Location", location)
                PropertyRow("Size", if (target.isDirectory) "Folder" else formatFileSize(target.size))
                PropertyRow("Modified", modified)
                PropertyRow("Path", file.absolutePath)
                PropertyRow("Readable", if (file.canRead()) "Yes" else "No")
                PropertyRow("Writable", if (file.canWrite()) "Yes" else "No")
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun PropertyRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

// ────────────────────────────────────────────────────────
// Inline Rename Commit — shared by ListFileItem/GridFileItem's inline text field
// ────────────────────────────────────────────────────────

/**
 * Validates and applies a rename typed into an inline text field, matching the
 * same rules the old Rename dialog enforced (no blank/"."/".."/path separators,
 * no clobbering an existing different file). Returns true on success so the
 * caller can drop out of edit mode; on failure it toasts and stays in edit mode,
 * exactly like Windows 11 Explorer keeps the field open and re-selects the text
 * on an invalid name instead of silently closing.
 */
private fun commitInlineRename(
    target: File,
    newName: String,
    context: android.content.Context,
    onSuccess: (File) -> Unit
): Boolean {
    val cleanName = newName.trim()
    if (cleanName == target.name) return true // unchanged — treat as a silent success
    if (cleanName.isBlank() || cleanName == "." || cleanName == ".." || cleanName.contains(File.separatorChar)) {
        android.widget.Toast.makeText(context, "Invalid file name", android.widget.Toast.LENGTH_SHORT).show()
        return false
    }
    val dest = File(target.parentFile, cleanName)
    if (dest.exists() && !dest.absolutePath.equals(target.absolutePath, ignoreCase = true)) {
        android.widget.Toast.makeText(context, "A file with that name already exists", android.widget.Toast.LENGTH_SHORT).show()
        return false
    }
    if (!target.renameTo(dest)) {
        android.widget.Toast.makeText(context, "Rename failed", android.widget.Toast.LENGTH_SHORT).show()
        return false
    }
    onSuccess(dest)
    return true
}

// ────────────────────────────────────────────────────────
// File Opening Logic
// ────────────────────────────────────────────────────────

private fun openFileFromExplorer(
    item: RealFileItem,
    context: android.content.Context,
    viewModel: LauncherViewModel?
) {
    if (item.isDirectory) return

    if (item.file.extension.equals("exe", ignoreCase = true) && viewModel != null) {
        val descriptor = BluebirdExecutable.read(item.file)
        if (descriptor != null) {
            val root = runCatching { BluebirdExecutable.resolveSourceRoot(item.file, descriptor) }.getOrNull()
            val entry = runCatching { BluebirdExecutable.resolveEntry(item.file, descriptor) }.getOrNull()
            if (root != null && entry != null &&
                (entry.path == root.path || entry.path.startsWith(root.path + File.separator)) &&
                entry.isFile
            ) {
                val iconFile = BluebirdExecutable.resolveIcon(item.file, descriptor)
                viewModel.openWindow(
                    screen = LauncherScreen.WEB_APP_VIEWER,
                    extras = mapOf(
                        "bpkAppId" to descriptor.appId,
                        "bpkAppName" to descriptor.name,
                        "bpkAppLocalDir" to root.absolutePath,
                        "bpkAppEntry" to entry.relativeTo(root).path
                    ),
                    customIconPath = iconFile.takeIf { it.isFile }?.absolutePath
                )
                return
            }
        }
    }

    if (item.file.extension.equals("bpk", ignoreCase = true) && viewModel != null) {
        val packageIcon = BpkPackageIcon.decode(item.file)
        val iconCache = File(context.cacheDir, "bluebird/bpk-icons")
        val packageIconFile = BpkPackageIcon.cache(item.file, iconCache)
        viewModel.openWindow(
            screen = LauncherScreen.PROGRAM_MANAGER,
            extras = buildMap {
                put("bpkPath", item.file.absolutePath)
                put("windowTitle", "Install ${item.file.nameWithoutExtension}")
            },
            customIconPath = packageIconFile?.absolutePath
        )
        return
    }

    if (viewModel?.openFileInternally(context, item.file.absolutePath) == true) return

    // Unsupported formats still get normal Android handling.
    viewModel?.openFileWithSystem(context, item.file.absolutePath) ?: run {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", item.file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, getMimeType(item.file))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Open with"))
        } catch (_: Exception) {
            android.widget.Toast.makeText(context, "No app can open ${item.name}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}

// ────────────────────────────────────────────────────────
// UI Composables (unchanged from original)
// ────────────────────────────────────────────────────────

@Composable
private fun CommandBar(
    onBack: () -> Unit,
    onUp: () -> Unit,
    onRefresh: () -> Unit,
    pathParts: List<Pair<String, File>>,
    onNavigate: (File) -> Unit,
    isGridView: Boolean,
    onToggleView: () -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    searchHome: Boolean,
    textColor: Color,
    surfaceBg: Color,
    isDark: Boolean
) {
    val controlBg = if (isDark) ExplorerDarkSurface else ExplorerLightSurface
    val fieldBg = if (isDark) ExplorerDarkField else ExplorerLightField
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isDark) ExplorerDarkBackground else ExplorerLightField,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().height(38.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                listOf(
                    Triple(FluentIcons.Regular.ArrowLeft, "Back", onBack),
                    Triple(FluentIcons.Regular.ArrowUp, "Up", onUp),
                    Triple(FluentIcons.Regular.ArrowClockwise, "Refresh", onRefresh)
                ).forEach { (icon, label, action) ->
                    Surface(
                        shape = RoundedCornerShape(9.dp),
                        color = controlBg,
                        border = BorderStroke(1.dp, textColor.copy(alpha = 0.045f))
                    ) {
                        IconButton(onClick = action, modifier = Modifier.size(34.dp)) {
                            Icon(icon, label, tint = textColor.copy(alpha = 0.82f), modifier = Modifier.size(17.dp))
                        }
                    }
                }

                BreadcrumbBar(
                    parts = pathParts,
                    onNavigate = onNavigate,
                    textColor = textColor,
                    surfaceBg = fieldBg,
                    modifier = Modifier.weight(1f).height(36.dp)
                )

                Surface(
                    modifier = Modifier.width(250.dp).height(36.dp),
                    shape = RoundedCornerShape(9.dp),
                    color = fieldBg,
                    border = BorderStroke(1.dp, textColor.copy(alpha = 0.07f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            FluentIcons.Regular.Search,
                            contentDescription = "Search",
                            tint = textColor.copy(alpha = 0.48f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(7.dp))
                        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    if (searchHome) "Search Home" else "Search this folder",
                                    color = textColor.copy(alpha = 0.40f),
                                    fontSize = 12.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = onSearchChange,
                                singleLine = true,
                                textStyle = TextStyle(color = textColor, fontSize = 12.5.sp, fontWeight = FontWeight.Medium),
                                cursorBrush = SolidColor(bluebirdColors.AccentBlue),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }, modifier = Modifier.size(22.dp)) {
                                Icon(
                                    FluentIcons.Regular.DiamondDismiss,
                                    contentDescription = "Clear search",
                                    tint = textColor.copy(alpha = 0.50f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(9.dp),
                    color = if (isGridView) bluebirdColors.AccentBlue.copy(alpha = 0.12f) else controlBg,
                    border = BorderStroke(1.dp, textColor.copy(alpha = 0.045f))
                ) {
                    IconButton(onClick = onToggleView, modifier = Modifier.size(34.dp)) {
                        Icon(
                            if (isGridView) FluentIcons.Regular.List else FluentIcons.Regular.Grid,
                            if (isGridView) "List view" else "Grid view",
                            tint = if (isGridView) bluebirdColors.AccentBlue else textColor.copy(alpha = 0.82f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BreadcrumbBar(
    parts: List<Pair<String, File>>,
    onNavigate: (File) -> Unit,
    textColor: Color,
    surfaceBg: Color,
    modifier: Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(9.dp),
        color = surfaceBg.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.07f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            parts.forEachIndexed { index, (label, dir) ->
                if (index > 0) {
                    Icon(
                        FluentIcons.Regular.ChevronRight,
                        null,
                        tint = textColor.copy(alpha = 0.28f),
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    label,
                    color = if (index == parts.lastIndex) textColor else bluebirdColors.AccentBlueLight,
                    fontSize = 12.5.sp,
                    fontWeight = if (index == parts.lastIndex) FontWeight.Medium else FontWeight.Normal,
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .clickable { onNavigate(dir) }
                        .padding(horizontal = 5.dp, vertical = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun Ribbon(
    onNewFolder: () -> Unit,
    onCut: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onSelectAll: () -> Unit,
    sortBy: String,
    sortAscending: Boolean,
    onSortChange: (String, Boolean) -> Unit,
    showHidden: Boolean,
    onToggleHidden: () -> Unit,
    hasSelection: Boolean,
    isSingleSelection: Boolean,
    clipboardActive: Boolean,
    textColor: Color,
    surfaceBg: Color,
    isDark: Boolean,
    itemCount: Int
) {
    val barBg = if (isDark) ExplorerDarkNav else ExplorerLightSurface
    val controlBg = if (isDark) ExplorerDarkSurface else Color(0xFFFEFEFE)
    // Fluent dims a command's icon+label together rather than just disabling the
    // click target, so "nothing selected" or "clipboard empty" reads instantly.
    val activeTint = textColor
    val dimmedTint = textColor.copy(alpha = 0.32f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = barBg,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.045f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(42.dp).padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CommandPill("New", FluentIcons.Regular.FolderAdd, onNewFolder, activeTint, controlBg)

            // Cut/Copy/Delete need at least one selected item to act on, exactly
            // like Explorer's ribbon greys these out over an empty selection.
            CommandPill(
                "Cut", FluentIcons.Regular.Cut, onCut,
                if (hasSelection) activeTint else dimmedTint, controlBg, enabled = hasSelection
            )
            CommandPill(
                "Copy", FluentIcons.Regular.Copy, onCopy,
                if (hasSelection) activeTint else dimmedTint, controlBg, enabled = hasSelection
            )
            CommandPill(
                "Paste",
                FluentIcons.Regular.ClipboardPaste,
                onPaste,
                if (clipboardActive) bluebirdColors.AccentBlue else dimmedTint,
                controlBg,
                enabled = clipboardActive
            )
            // Rename only ever targets exactly one item — Explorer disables it the
            // moment a second item joins the selection instead of renaming "the first one".
            CommandPill(
                "Rename", FluentIcons.Regular.Rename, onRename,
                if (isSingleSelection) activeTint else dimmedTint, controlBg, enabled = isSingleSelection
            )
            CommandPill(
                "Delete", FluentIcons.Regular.Delete, onDelete,
                if (hasSelection) activeTint else dimmedTint, controlBg, enabled = hasSelection
            )

            Spacer(Modifier.width(3.dp))
            VerticalDivider(modifier = Modifier.height(22.dp), color = textColor.copy(alpha = 0.09f))
            Spacer(Modifier.width(3.dp))

            SortMenuButton(
                sortBy = sortBy,
                sortAscending = sortAscending,
                onSortChange = onSortChange,
                textColor = textColor,
                controlBg = controlBg
            )

            CommandPill("Select all", FluentIcons.Regular.SelectAllOn, onSelectAll, activeTint, controlBg)
            CommandPill(
                if (showHidden) "Hide hidden" else "Hidden items",
                if (showHidden) FluentIcons.Regular.EyeOff else FluentIcons.Regular.Eye,
                onToggleHidden,
                activeTint,
                controlBg
            )

            Spacer(Modifier.weight(1f))
            Text(
                "$itemCount items",
                color = textColor.copy(alpha = 0.40f),
                fontSize = 10.5.sp,
                modifier = Modifier.padding(end = 3.dp)
            )
        }
    }
}

@Composable
private fun CommandPill(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    tint: Color,
    background: Color,
    enabled: Boolean = true
) {
    val hoverInteractionSource = remember { MutableInteractionSource() }
    val isHovered by hoverInteractionSource.collectIsHoveredAsState()
    // Explorer's ribbon buttons brighten very slightly in place on hover — a soft
    // neutral lift, not a tint toward the accent color — well before any click.
    val fillColor = when {
        !enabled -> background.copy(alpha = 0.55f)
        isHovered -> androidx.compose.ui.graphics.lerp(background, tint, 0.06f)
        else -> background
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = fillColor,
        border = BorderStroke(1.dp, tint.copy(alpha = if (!enabled) 0.03f else if (isHovered) 0.12f else 0.055f))
    ) {
        TextButton(
            onClick = onClick,
            enabled = enabled,
            interactionSource = hoverInteractionSource,
            contentPadding = PaddingValues(horizontal = 8.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(5.dp))
            Text(label, color = tint, fontSize = 11.sp, maxLines = 1)
        }
    }
}

private val sortFields = listOf("name" to "Name", "date" to "Date modified", "type" to "Type", "size" to "Size")

@Composable
private fun SortMenuButton(
    sortBy: String,
    sortAscending: Boolean,
    onSortChange: (String, Boolean) -> Unit,
    textColor: Color,
    controlBg: Color
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = controlBg,
            border = BorderStroke(1.dp, textColor.copy(alpha = 0.055f))
        ) {
            TextButton(
                onClick = { expanded = true },
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(
                    FluentIcons.Regular.ArrowUp,
                    "Sort",
                    tint = textColor.copy(alpha = 0.78f),
                    modifier = Modifier
                        .size(15.dp)
                        .rotate(if (sortAscending) 0f else 180f)
                )
                Spacer(Modifier.width(5.dp))
                Text("Sort", color = textColor.copy(alpha = 0.84f), fontSize = 11.sp)
                Spacer(Modifier.width(2.dp))
                Icon(FluentIcons.Regular.ChevronDown, null, tint = textColor.copy(alpha = 0.50f), modifier = Modifier.size(13.dp))
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(196.dp)
        ) {
            // Section 1: which field to sort by — its own radio group, independent
            // of direction, exactly like Explorer's "Sort by" flyout.
            SortMenuSectionLabel("Sort by")
            sortFields.forEach { (key, label) ->
                SortRadioItem(
                    label = label,
                    selected = sortBy == key,
                    onClick = {
                        onSortChange(key, sortAscending)
                        expanded = false
                    }
                )
            }
            Divider(Modifier.padding(vertical = 4.dp), color = textColor.copy(alpha = 0.08f))
            // Section 2: direction — a second, independent radio group. Explorer never
            // makes re-clicking the same field flip direction; direction has its own row.
            SortRadioItem(
                label = "Ascending",
                selected = sortAscending,
                onClick = {
                    onSortChange(sortBy, true)
                    expanded = false
                }
            )
            SortRadioItem(
                label = "Descending",
                selected = !sortAscending,
                onClick = {
                    onSortChange(sortBy, false)
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun SortMenuSectionLabel(label: String) {
    Text(
        label,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        fontSize = 10.5.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
    )
}

@Composable
private fun SortRadioItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val accent = bluebirdColors.AccentBlue
    val onSurface = MaterialTheme.colorScheme.onSurface
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // A real radio dot rather than an arrow glyph standing in for two
                // different meanings — Fluent's own Sort by menu uses radio buttons.
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .border(
                            width = 1.4.dp,
                            color = if (selected) accent else onSurface.copy(alpha = 0.35f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(accent, CircleShape)
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    label,
                    fontSize = 13.sp,
                    color = onSurface,
                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                )
            }
        },
        onClick = onClick
    )
}

@Composable
private fun ExplorerAction(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    tint: Color,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(30.dp)
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun DeviceStorageDashboard(
    textColor: Color,
    bgColor: Color,
    surfaceBg: Color,
    isDark: Boolean,
    onNavigate: (File) -> Unit
) {
    val internalStorage = Environment.getExternalStorageDirectory()
    val removableStorage = remember {
        File("/storage").listFiles()
            ?.filter { it.isDirectory && it.name !in setOf("emulated", "self") && it.canRead() }
            ?.sortedBy { it.name.lowercase() }
            ?: emptyList()
    }

    val storageEntries = buildList {
        add("Internal Storage" to internalStorage)
        removableStorage.forEach { add("${it.name}" to it) }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                "This Device",
                color = textColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Storage available on this device",
                color = if (isDark) ExplorerDarkSecondaryText else ExplorerLightSecondaryText,
                fontSize = 12.5.sp
            )

            storageEntries.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    row.forEach { (label, directory) ->
                        val stat = remember(directory.absolutePath) { runCatching { StatFs(directory.path) }.getOrNull() }
                        val totalBytes = stat?.totalBytes ?: 0L
                        val freeBytes = stat?.availableBytes ?: 0L
                        val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)
                        val fraction = if (totalBytes > 0L) (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f
                        val icon = if (directory.absolutePath == internalStorage.absolutePath) R.drawable.ic_explorer_storage else R.drawable.ic_explorer_sd_card

                        Surface(
                            modifier = Modifier.weight(1f).clickable { onNavigate(directory) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isDark) ExplorerDarkSurface else Color(0xFFFEFEFE),
                            border = BorderStroke(1.dp, if (isDark) ExplorerDarkBorder else ExplorerLightBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        modifier = Modifier.size(42.dp),
                                        shape = RoundedCornerShape(11.dp),
                                        color = (if (directory.absolutePath == internalStorage.absolutePath) ExplorerAccentBlue else ExplorerLightSecondaryText).copy(alpha = if (isDark) 0.16f else 0.10f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                painter = painterResource(icon),
                                                contentDescription = null,
                                                tint = Color.Unspecified,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(11.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(label, color = textColor, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
                                        Text(
                                            formatFileSize(freeBytes) + " free of " + formatFileSize(totalBytes),
                                            color = if (isDark) ExplorerDarkSecondaryText else ExplorerLightSecondaryText,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                    Icon(
                                        FluentIcons.Regular.ChevronRight,
                                        contentDescription = null,
                                        tint = textColor.copy(alpha = 0.34f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { fraction },
                                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                                    color = if (isDark) ExplorerDarkAccent else ExplorerAccentBlue,
                                    trackColor = textColor.copy(alpha = 0.10f)
                                )
                            }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun NavigationPane(
    quickAccess: List<Pair<String, File>>,
    currentDir: File,
    isHomeDashboard: Boolean,
    isDeviceDashboard: Boolean,
    appDataRoot: File,
    onNavigate: (File) -> Unit,
    onHome: () -> Unit,
    onDevice: () -> Unit,
    textColor: Color,
    navBg: Color,
    surfaceBg: Color,
    isDark: Boolean,
    navWidth: Dp,
    expandProgress: Float,
    labelAlpha: Float,
    scrollState: ScrollState,
    onInteract: () -> Unit
) {
    val homeDir = Environment.getExternalStorageDirectory()
    val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val documents = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    val pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    val music = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
    val videos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
    val desktop = File(homeDir, "Desktop")

    // "Gallery" (under Home) and "Pictures" (under Folders) used to both point at the
    // same DIRECTORY_PICTURES folder, so the same folder showed up twice in the nav
    // pane — confusing, and it made two separate rows highlight together whenever you
    // were inside Pictures. Gallery is the canonical entry for this folder now; it's
    // dropped from Folders so each real folder appears exactly once.
    val primary = listOf(
        ExplorerNavDestination("Home", homeDir, R.drawable.ic_explorer_home),
        ExplorerNavDestination("Gallery", pictures, R.drawable.ic_explorer_gallery)
    )
    val folders = listOf(
        ExplorerNavDestination("Desktop", desktop, R.drawable.ic_explorer_desktop),
        ExplorerNavDestination("Downloads", downloads, R.drawable.ic_explorer_downloads),
        ExplorerNavDestination("Documents", documents, R.drawable.ic_explorer_documents),
        ExplorerNavDestination("Music", music, R.drawable.ic_explorer_music),
        ExplorerNavDestination("Videos", videos, R.drawable.ic_explorer_videos)
    ).filter { it.directory.exists() }

    Column(
        modifier = Modifier
            .width(navWidth)
            .fillMaxHeight()
            .background(navBg)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onInteract() }
            .verticalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        NavSectionHeader("Home", textColor, expandProgress, labelAlpha)
        primary.forEach { item ->
            NavItem(
                label = item.label,
                iconRes = item.icon,
                isSelected = when (item.label) {
                    "Home" -> isHomeDashboard
                    "Gallery" -> !isHomeDashboard && currentDir == item.directory
                    else -> false
                },
                textColor = textColor,
                selectionBackground = if (isDark) ExplorerDarkSelection else ExplorerSelectionBlue,
                accentColor = if (isDark) ExplorerDarkAccent else ExplorerAccentBlue,
                expandProgress = expandProgress,
                labelAlpha = labelAlpha,
                onClick = {
                    onInteract()
                    if (item.label == "Home") onHome() else onNavigate(item.directory)
                }
            )
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 6.dp), color = textColor.copy(alpha = 0.07f))
        Spacer(Modifier.height(8.dp))

        NavSectionHeader("Folders", textColor, expandProgress, labelAlpha)
        folders.forEach { item ->
            NavItem(
                label = item.label,
                iconRes = item.icon,
                isSelected = currentDir == item.directory,
                textColor = textColor,
                selectionBackground = if (isDark) ExplorerDarkSelection else ExplorerSelectionBlue,
                accentColor = if (isDark) ExplorerDarkAccent else ExplorerAccentBlue,
                expandProgress = expandProgress,
                labelAlpha = labelAlpha,
                onClick = { onInteract(); onNavigate(item.directory) }
            )
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 6.dp), color = textColor.copy(alpha = 0.07f))
        Spacer(Modifier.height(8.dp))

        NavSectionHeader("Storage", textColor, expandProgress, labelAlpha)

        NavItem(
            label = "This Device",
            iconRes = R.drawable.ic_explorer_device,
            isSelected = isDeviceDashboard,
            textColor = textColor,
            selectionBackground = if (isDark) ExplorerDarkSelection else ExplorerSelectionBlue,
            accentColor = if (isDark) ExplorerDarkAccent else ExplorerAccentBlue,
            expandProgress = expandProgress,
            labelAlpha = labelAlpha,
            onClick = { onInteract(); onDevice() }
        )

        NavItem(
            label = "App Data",
            iconRes = R.drawable.ic_explorer_storage,
            isSelected = !isHomeDashboard && !isDeviceDashboard &&
                (currentDir.absolutePath == appDataRoot.absolutePath ||
                 currentDir.absolutePath.startsWith(appDataRoot.absolutePath + File.separator)),
            textColor = textColor,
            selectionBackground = if (isDark) ExplorerDarkSelection else ExplorerSelectionBlue,
            accentColor = if (isDark) ExplorerDarkAccent else ExplorerAccentBlue,
            expandProgress = expandProgress,
            labelAlpha = labelAlpha,
            onClick = { onInteract(); onNavigate(appDataRoot) }
        )
    }
}

// Section headers ("Home" / "Folders" / "Storage") have no icon counterpart
// in the collapsed icon-only rail, so they fade out early and drop out of
// composition once fully invisible — same threshold/curve as the item
// labels, so nothing in the pane fades out of step with anything else.
@Composable
private fun NavSectionHeader(label: String, textColor: Color, expandProgress: Float, labelAlpha: Float) {
    if (expandProgress > 0.001f) {
        Text(
            label,
            color = textColor.copy(alpha = 0.45f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .alpha(labelAlpha)
                .padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun NavItem(
    label: String,
    iconRes: Int,
    isSelected: Boolean,
    textColor: Color,
    selectionBackground: Color,
    accentColor: Color,
    expandProgress: Float = 1f,
    labelAlpha: Float = 1f,
    onClick: () -> Unit
) {
    Row(
        // Padding/arrangement are fixed regardless of collapse state — only the
        // label's own composition + alpha change — so the icon never jumps
        // position mid-transition the way it would if these were swapped on
        // the isNavExpanded boolean instead of following the same progress
        // curve as the width.
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) selectionBackground
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = label,
            tint = Color.Unspecified,
            modifier = Modifier.size(18.dp)
        )
        if (expandProgress > 0.001f) {
            Text(
                label,
                color = if (isSelected) accentColor else textColor.copy(alpha = 0.88f),
                fontSize = 12.5.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.alpha(labelAlpha)
            )
        }
    }
}

@Composable
private fun StatusBar(textColor: Color, itemCount: Int, selectedCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().height(26.dp)
            .background(textColor.copy(alpha = 0.045f))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("$itemCount items", color = textColor.copy(alpha = 0.45f), fontSize = 11.sp)
            if (selectedCount > 0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = bluebirdColors.AccentBlue.copy(alpha = 0.12f)
                ) {
                    Text(
                        "$selectedCount selected",
                        color = bluebirdColors.AccentBlue,
                        fontSize = 10.5.sp,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Text("Bluebird Explorer", color = textColor.copy(alpha = 0.25f), fontSize = 10.5.sp)
    }
}

// ────────────────────────────────────────────────────────
// List & Grid Items
// ────────────────────────────────────────────────────────


// Ask Android's media provider for small video thumbnails. Bluebird never persists
// video frames or copies video files into app storage.
private suspend fun loadExplorerVideoThumbnail(context: android.content.Context, file: File): Bitmap? {
    if (!file.isFile || file.length() <= 0L || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
    return try {
        val uri = context.contentResolver.query(
            android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            arrayOf(android.provider.MediaStore.Video.Media._ID),
            "${android.provider.MediaStore.Video.Media.DATA} = ?",
            arrayOf(file.absolutePath),
            null
        )?.use { c ->
            if (c.moveToFirst()) {
                android.content.ContentUris.withAppendedId(
                    android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    c.getLong(0)
                )
            } else null
        } ?: return null
        context.contentResolver.loadThumbnail(uri, android.util.Size(160, 90), null)
    } catch (_: Exception) {
        null
    }
}

@Composable
private fun FileExplorerThumbnail(
    item: RealFileItem,
    modifier: Modifier,
    iconSize: Dp
) {
    val context = LocalContext.current
    val extension = item.extension
    val isImage = !item.isDirectory && extension in setOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
    val isVideo = !item.isDirectory && extension in setOf("mp4", "mkv", "avi", "mov", "webm", "3gp")
    val needsPackageIcon = !item.isDirectory && extension in setOf("exe", "bpk")

    // Custom package artwork is now loaded only for visible Explorer tiles. This keeps
    // opening a directory with hundreds/thousands of files responsive.
    val lazyPackageIcon by produceState<Bitmap?>(
        initialValue = item.iconBitmap,
        item.file.absolutePath,
        item.lastModified,
        item.size,
        needsPackageIcon
    ) {
        if (!needsPackageIcon || value != null) return@produceState
        value = withContext(Dispatchers.IO) {
            when (extension) {
                "exe" -> runCatching {
                    BluebirdExecutable.read(item.file)?.let { descriptor ->
                        BluebirdExecutable.resolveIcon(item.file, descriptor)
                            .takeIf { it.isFile }
                            ?.let { BitmapFactory.decodeFile(it.absolutePath) }
                    }
                }.getOrNull()
                "bpk" -> runCatching { BpkPackageIcon.decode(item.file) }.getOrNull()
                else -> null
            }
        }
    }
    val videoThumbnail by produceState<Bitmap?>(
        initialValue = null,
        item.file.absolutePath,
        item.lastModified,
        item.size,
        isVideo
    ) {
        if (!isVideo) return@produceState
        value = withContext(Dispatchers.IO) { loadExplorerVideoThumbnail(context, item.file) }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when {
            isImage -> {
                // Coil handles decoding/downsampling off the UI thread. This is a
                // thumbnail only; Explorer never renders the file itself.
                coil.compose.AsyncImage(
                    model = item.file,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
            isVideo && videoThumbnail != null -> {
                androidx.compose.foundation.Image(
                    bitmap = videoThumbnail!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.55f),
                    modifier = Modifier.size(iconSize.coerceAtMost(28.dp))
                ) {
                    Icon(FluentIcons.Filled.Play, null, tint = Color.White, modifier = Modifier.padding(4.dp))
                }
            }
            lazyPackageIcon != null -> {
                Image(
                    bitmap = lazyPackageIcon!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(iconSize).clip(RoundedCornerShape(6.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
            else -> {
                PngIcon(res = fileIconRes(item.isDirectory, item.extension), modifier = Modifier.size(iconSize))
            }
        }
    }
}

// ────────────────────────────────────────────────────────
// Inline Rename Field — the actual "type over the name in place" editor,
// matching Windows 11 desktop/Explorer: the base name is pre-selected (not the
// extension), Enter commits, Escape/focus-loss cancels, invalid names re-open
// the field with the text re-selected instead of silently reverting.
// ────────────────────────────────────────────────────────
@Composable
private fun InlineRenameField(
    item: RealFileItem,
    isDark: Boolean,
    isGrid: Boolean,
    modifier: Modifier = Modifier,
    onCommit: (String) -> Boolean,
    onCancel: () -> Unit
) {
    val dotIndex = item.name.lastIndexOf('.')
    val baseNameEnd = if (!item.isDirectory && dotIndex > 0) dotIndex else item.name.length
    var value by remember(item.file.absolutePath) {
        mutableStateOf(
            androidx.compose.ui.text.input.TextFieldValue(
                text = item.name,
                selection = androidx.compose.ui.text.TextRange(0, baseNameEnd)
            )
        )
    }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(item.file.absolutePath) { focusRequester.requestFocus() }

    val accent = if (isDark) ExplorerDarkAccent else ExplorerAccentBlue
    val fieldBg = if (isDark) ExplorerDarkField else Color.White
    val textColor = if (isDark) ExplorerDarkText else ExplorerLightText

    fun commit() {
        if (!onCommit(value.text)) {
            // Re-select on failure so the next keystroke overwrites, same as real Explorer.
            value = value.copy(selection = androidx.compose.ui.text.TextRange(0, value.text.length))
        }
    }

    BasicTextField(
        value = value,
        onValueChange = { value = it },
        singleLine = true,
        textStyle = TextStyle(
            color = textColor,
            fontSize = if (isGrid) 12.5.sp else 13.5.sp,
            fontWeight = FontWeight.Medium,
            textAlign = if (isGrid) TextAlign.Center else TextAlign.Start
        ),
        cursorBrush = SolidColor(accent),
        modifier = modifier
            .background(fieldBg, RoundedCornerShape(3.dp))
            .border(1.dp, accent, RoundedCornerShape(3.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
            .focusRequester(focusRequester)
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (event.key) {
                    Key.Enter, Key.NumPadEnter -> { commit(); true }
                    Key.Escape -> { onCancel(); true }
                    else -> false
                }
            }
    )
}

@Composable
private fun ListFileItem(
    item: RealFileItem,
    isSelected: Boolean,
    isRenaming: Boolean,
    textColor: Color,
    isDark: Boolean,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    onLongPress: (Offset) -> Unit,
    onCommitRename: (String) -> Boolean = { true },
    onCancelRename: () -> Unit = {}
) {
    // Same fix as GridFileItem: report this row's own window position so the
    // context menu opens exactly under the long-press, not near the top of the list.
    var rowOrigin by remember { mutableStateOf(Offset.Zero) }
    // Real Windows 11 Explorer highlights a row the instant a mouse crosses it,
    // well before any click — that subtle pre-click feedback is a big part of why
    // it reads as "premium" rather than flat. hoverable() only fires for an actual
    // pointer (mouse/stylus/trackpad); plain touch taps don't trigger a hover state,
    // so this is purely additive on touch-only devices.
    val hoverInteractionSource = remember { MutableInteractionSource() }
    val isHovered by hoverInteractionSource.collectIsHoveredAsState()
    val hoverTint = if (isDark) ExplorerHoverDark else ExplorerHoverLight
    // Fluent uses a slightly stronger fill while renaming to make clear the row is
    // "in edit mode", on top of the same selection tint it already had.
    val rowColor = when {
        isRenaming -> bluebirdColors.AccentBlue.copy(alpha = if (isDark) 0.22f else 0.15f)
        isSelected && isHovered -> bluebirdColors.AccentBlue.copy(alpha = if (isDark) FluentSelectedHoverAlpha + 0.06f else FluentSelectedHoverAlpha)
        isSelected -> bluebirdColors.AccentBlue.copy(alpha = if (isDark) 0.18f else 0.12f)
        isHovered -> hoverTint.copy(alpha = FluentHoverAlpha)
        else -> Color.Transparent
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(rowColor)
            .hoverable(hoverInteractionSource)
            .onGloballyPositioned { coords -> rowOrigin = coords.windowRelativePosition() }
            .pointerInput(item.file.absolutePath, isRenaming) {
                if (isRenaming) return@pointerInput
                detectTapGestures(
                    onTap = { onClick() },
                    onDoubleTap = { onDoubleClick() },
                    onLongPress = { offset -> onLongPress(rowOrigin + offset) }
                )
            }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FileExplorerThumbnail(
            item = item,
            modifier = Modifier.size(30.dp).clip(RoundedCornerShape(7.dp)),
            iconSize = 19.dp
        )
        Spacer(Modifier.width(6.dp))
        if (isRenaming) {
            InlineRenameField(
                item = item,
                isDark = isDark,
                isGrid = false,
                modifier = Modifier.weight(1f),
                onCommit = onCommitRename,
                onCancel = onCancelRename
            )
        } else {
            Text(
                item.name,
                color = textColor,
                fontSize = 13.5.sp,
                fontWeight = if (item.isDirectory) FontWeight.SemiBold else FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            formatDate(item.lastModified),
            color = textColor.copy(alpha = 0.48f),
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 16.dp).width(120.dp),
            maxLines = 1
        )
        Text(
            if (item.isDirectory) "Folder" else item.extension.uppercase().ifEmpty { "File" },
            color = textColor.copy(alpha = 0.48f),
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 12.dp).width(64.dp),
            maxLines = 1
        )
        Text(
            if (item.isDirectory) "" else formatFileSize(item.size),
            color = textColor.copy(alpha = 0.48f),
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 12.dp).width(62.dp),
            maxLines = 1,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun GridFileItem(
    item: RealFileItem,
    isSelected: Boolean,
    isRenaming: Boolean,
    textColor: Color,
    isDark: Boolean,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    onLongPress: (Offset) -> Unit,
    onCommitRename: (String) -> Boolean = { true },
    onCancelRename: () -> Unit = {}
) {
    // The tap offset from detectTapGestures is local to THIS tile, not the list.
    // The context menu needs a window-relative point, so we track this tile's own
    // window position and add the local tap offset to it — previously the caller
    // added only the list area's origin, which ignored where in the (scrolled) grid
    // this particular tile actually was, so the menu opened in the wrong place for
    // any item that wasn't right at the top-left of the visible list.
    var tileOrigin by remember { mutableStateOf(Offset.Zero) }
    val hoverInteractionSource = remember { MutableInteractionSource() }
    val isHovered by hoverInteractionSource.collectIsHoveredAsState()
    val hoverTint = if (isDark) ExplorerHoverDark else ExplorerHoverLight
    val cardColor = when {
        isRenaming -> bluebirdColors.AccentBlue.copy(alpha = if (isDark) 0.22f else 0.15f)
        isSelected && isHovered -> bluebirdColors.AccentBlue.copy(alpha = if (isDark) FluentSelectedHoverAlpha + 0.06f else FluentSelectedHoverAlpha)
        isSelected -> bluebirdColors.AccentBlue.copy(alpha = if (isDark) 0.20f else 0.13f)
        isHovered -> hoverTint.copy(alpha = FluentHoverAlpha)
        else -> Color.Transparent
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 108.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(cardColor)
            .hoverable(hoverInteractionSource)
            .onGloballyPositioned { coords -> tileOrigin = coords.windowRelativePosition() }
            .pointerInput(item.file.absolutePath, isRenaming) {
                if (isRenaming) return@pointerInput
                detectTapGestures(
                    onTap = { onClick() },
                    onDoubleTap = { onDoubleClick() },
                    onLongPress = { offset -> onLongPress(tileOrigin + offset) }
                )
            }
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) bluebirdColors.AccentBlue.copy(alpha = 0.10f) else textColor.copy(alpha = 0.045f)
        ) {
            FileExplorerThumbnail(
                item = item,
                modifier = Modifier.padding(7.dp).size(56.dp),
                iconSize = 33.dp
            )
        }
        Spacer(Modifier.height(6.dp))
        if (isRenaming) {
            InlineRenameField(
                item = item,
                isDark = isDark,
                isGrid = true,
                modifier = Modifier.fillMaxWidth(),
                onCommit = onCommitRename,
                onCancel = onCancelRename
            )
        } else {
            Text(
                item.name,
                color = textColor,
                fontSize = 12.5.sp,
                fontWeight = if (item.isDirectory) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )
        }
        if (!item.isDirectory && !isRenaming) {
            Text(
                formatFileSize(item.size),
                color = textColor.copy(alpha = 0.38f),
                fontSize = 10.5.sp,
                maxLines = 1
            )
        }
    }
}

// ────────────────────────────────────────────────────────
// Context Menu
// ────────────────────────────────────────────────────────

/**
 * Real Windows 11 varies this menu by what's actually selected:
 *  - A single file shows "Open" (using its associated app) as the bold default action.
 *  - A folder never shows "Open" as a separate command — double-click/Enter already
 *    opens it — but folders are the only targets that can receive a Paste.
 *  - Video/audio/image files show a media-appropriate open verb ("Play"/"Preview")
 *    instead of the generic "Open", the same way Explorer's default verb changes
 *    per file association.
 *  - Multi-selection drops single-file-only actions (Rename, Create shortcut,
 *    Properties for a mixed set) since they don't have one unambiguous target.
 * Every command that doesn't apply stays visible but dimmed, matching Explorer's
 * own "always show the row, greyed out if it can't run" behavior, rather than the
 * menu changing shape (and item count) with every different selection.
 */
@Composable
private fun FileContextMenu(
    fileItem: RealFileItem,
    selectionCount: Int,
    offset: Offset,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onPaste: (() -> Unit)? = null,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onCreateShortcut: () -> Unit,
    onProperties: () -> Unit
) {
    val isMultiSelect = selectionCount > 1
    val openLabel = when {
        fileItem.isDirectory -> "Open"
        fileItem.extension in setOf("mp4", "mkv", "avi", "mov", "webm", "3gp") -> "Play"
        fileItem.extension in setOf("mp3", "wav", "ogg", "flac", "aac", "m4a") -> "Play"
        fileItem.extension in setOf("jpg", "jpeg", "png", "gif", "webp", "bmp") -> "Preview"
        else -> "Open"
    }
    ExplorerContextPopup(
        offset = offset,
        isDark = isDark,
        onDismiss = onDismiss
    ) {
        if (isMultiSelect) {
            ContextMenuHeader("$selectionCount items selected")
        }
        // Folders open via double-click/Enter already, so "Open" only makes sense
        // as an explicit command for files — and only for a single target.
        if (!fileItem.isDirectory) {
            ContextMenuItem(openLabel, FluentIcons.Regular.Open, onOpen, enabled = !isMultiSelect)
        }
        // Paste only ever applies to a folder, and only when there's something on
        // the clipboard — dimmed rather than removed so its position is predictable.
        ContextMenuItem(
            "Paste",
            FluentIcons.Regular.ClipboardPaste,
            onPaste ?: {},
            enabled = onPaste != null
        )
        Divider(Modifier.padding(vertical = 2.dp))
        ContextMenuItem("Cut", FluentIcons.Regular.Cut, onCut)
        ContextMenuItem("Copy", FluentIcons.Regular.Copy, onCopy)
        ContextMenuItem("Rename", FluentIcons.Regular.Rename, onRename, enabled = !isMultiSelect)
        ContextMenuItem(
            "Create shortcut",
            FluentIcons.Regular.Link,
            onCreateShortcut,
            enabled = !isMultiSelect
        )
        ContextMenuItem("Delete", FluentIcons.Regular.Delete, onDelete, tint = bluebirdColors.DangerRed)
        Divider(Modifier.padding(vertical = 2.dp))
        ContextMenuItem("Properties", FluentIcons.Regular.Info, onProperties, enabled = !isMultiSelect)
    }
}

@Composable
private fun ContextMenuHeader(label: String) {
    Text(
        label,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
    )
    Divider(Modifier.padding(vertical = 2.dp))
}

@Composable
private fun ExplorerContextPopup(
    offset: Offset,
    isDark: Boolean,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val position = IntOffset(offset.x.roundToInt(), offset.y.roundToInt())
    Popup(
        popupPositionProvider = object : PopupPositionProvider {
            override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize, layoutDirection: LayoutDirection, popupContentSize: IntSize): IntOffset {
                // Real Windows 11 opens the menu with its top-left corner pinned to the
                // cursor/tap point, then flips to the opposite side of that point — never
                // just slides along an edge — whenever the default placement would run
                // off the window. Flipping keeps the menu anchored to where you clicked;
                // clamping (the old behavior) can leave it looking unrelated to the tap.
                val margin = 8
                val fitsRight = position.x + popupContentSize.width <= windowSize.width - margin
                val x = if (fitsRight) {
                    position.x
                } else {
                    // Open to the left of the tap point instead of past the right edge.
                    (position.x - popupContentSize.width).coerceAtLeast(margin)
                }

                val fitsBelow = position.y + popupContentSize.height <= windowSize.height - margin
                val y = if (fitsBelow) {
                    position.y
                } else {
                    // Open above the tap point instead of past the bottom edge.
                    (position.y - popupContentSize.height).coerceAtLeast(margin)
                }

                return IntOffset(
                    x.coerceIn(margin, (windowSize.width - popupContentSize.width - margin).coerceAtLeast(margin)),
                    y.coerceIn(margin, (windowSize.height - popupContentSize.height - margin).coerceAtLeast(margin))
                )
            }
        },
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true, dismissOnClickOutside = true)
    ) {
        // Fluent flyout: layered Mica-style surface with a soft border and shadow,
        // matching Windows 11's context-menu chrome rather than a flat card.
        val bg = if (isDark) Color(0xFA2C2C2C) else Color(0xFCFCFCFC)
        val border = if (isDark) ExplorerDarkBorder.copy(alpha = 0.7f) else ExplorerLightBorder
        Surface(
            modifier = Modifier.width(212.dp),
            shape = RoundedCornerShape(8.dp),
            color = bg,
            shadowElevation = 16.dp,
            border = BorderStroke(1.dp, border)
        ) {
            Column(Modifier.padding(vertical = 4.dp), content = content)
        }
    }
}

@Composable
private fun ExplorerBackgroundContextMenu(
    offset: Offset,
    isDark: Boolean,
    hasPaste: Boolean,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onPaste: () -> Unit,
    onNewFolder: () -> Unit,
    onNewTextFile: () -> Unit,
    onSelectAll: () -> Unit,
    onToggleHidden: () -> Unit
) {
    ExplorerContextPopup(offset, isDark, onDismiss) {
        ContextMenuItem("Refresh", FluentIcons.Regular.ArrowClockwise, { onRefresh(); onDismiss() })
        ContextMenuItem(
            "Paste",
            FluentIcons.Regular.ClipboardPaste,
            { onPaste(); onDismiss() },
            enabled = hasPaste
        )
        Divider(Modifier.padding(vertical = 2.dp))
        ContextMenuItem("New folder", FluentIcons.Regular.FolderAdd, { onNewFolder(); onDismiss() })
        ContextMenuItem("New text document", FluentIcons.Regular.Document, { onNewTextFile(); onDismiss() })
        ContextMenuItem("Select all", FluentIcons.Regular.SelectAllOn, { onSelectAll(); onDismiss() })
        ContextMenuItem("Show hidden files", FluentIcons.Regular.Eye, { onToggleHidden(); onDismiss() })
    }
}

@Composable
private fun ContextMenuItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true
) {
    // Windows 11 keeps inapplicable commands visible but greyed out and inert —
    // e.g. Paste always shows in the menu, just disabled when the clipboard is
    // empty — rather than making the menu jump around by hiding rows outright.
    val effectiveTint = if (enabled) tint else tint.copy(alpha = 0.35f)
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, null, tint = effectiveTint, modifier = Modifier.size(17.dp))
        Text(
            label,
            fontSize = 13.5.sp,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}
