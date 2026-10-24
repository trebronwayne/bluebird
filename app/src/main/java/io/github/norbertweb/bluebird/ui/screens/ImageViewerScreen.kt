package io.github.norbertweb.bluebird.ui.screens

import io.github.norbertweb.bluebird.ui.components.BluebirdDropdownMenu
import io.github.norbertweb.bluebird.i18n.ui
import io.github.norbertweb.bluebird.i18n.pluralText
import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import io.github.norbertweb.bluebird.LauncherViewModel
import io.github.norbertweb.bluebird.WallpaperTarget
import io.github.norbertweb.bluebird.R
import io.github.norbertweb.bluebird.ui.components.FluentIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/*
 * Bluebird Photos
 *
 * Windows 11 / desktop-inspired image viewer.
 *
 * IMPORTANT FOR BLUEBIRD DESKTOP + FILE EXPLORER:
 * Pass either:
 *   - a content Uri string: "content://..."
 *   - an absolute image path when available
 * into initialPath.
 *
 * Examples:
 *   ImageViewerScreen(isDark, initialPath = uri.toString())
 *   ImageViewerScreen(isDark, initialPath = file.absolutePath)
 *
 * This keeps the existing navigation contract while making direct-open behavior
 * first-class instead of treating the viewer as a standalone gallery only.
 */

// -----------------------------------------------------------------------------
// Windows 11 visual language
// -----------------------------------------------------------------------------

private object PhotosTheme {
    val Accent = Color(0xFF0F6CBD)
    val AccentHover = Color(0xFF115EA3)
    val AccentSoft = Color(0x1A0F6CBD)

    val DarkBackground = Color(0xFF111111)
    val DarkSurface = Color(0xFF1A1A1A)
    val DarkSurfaceAlt = Color(0xFF202020)
    val DarkBorder = Color(0xFF2B2B2B)
    val DarkText = Color(0xFFF7F7F7)
    val DarkSecondary = Color(0xFFBDBDBD)
    val DarkMuted = Color(0xFF858585)

    val LightBackground = Color(0xFFF7F7F7)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurfaceAlt = Color(0xFFF1F1F1)
    val LightBorder = Color(0xFFE1E1E1)
    val LightText = Color(0xFF202020)
    val LightSecondary = Color(0xFF5F5F5F)
    val LightMuted = Color(0xFF8A8A8A)

    val Danger = Color(0xFFC42B1C)
    val Favorite = Color(0xFFE74856)
}

private fun backgroundColor(isDark: Boolean) =
    if (isDark) PhotosTheme.DarkBackground else PhotosTheme.LightBackground

private fun surfaceColor(isDark: Boolean) =
    if (isDark) PhotosTheme.DarkSurface else PhotosTheme.LightSurface

private fun surfaceAltColor(isDark: Boolean) =
    if (isDark) PhotosTheme.DarkSurfaceAlt else PhotosTheme.LightSurfaceAlt

private fun borderColor(isDark: Boolean) =
    if (isDark) PhotosTheme.DarkBorder else PhotosTheme.LightBorder

private fun textColor(isDark: Boolean) =
    if (isDark) PhotosTheme.DarkText else PhotosTheme.LightText

private fun secondaryColor(isDark: Boolean) =
    if (isDark) PhotosTheme.DarkSecondary else PhotosTheme.LightSecondary

private fun mutedColor(isDark: Boolean) =
    if (isDark) PhotosTheme.DarkMuted else PhotosTheme.LightMuted

// -----------------------------------------------------------------------------
// Models
// -----------------------------------------------------------------------------

data class PhotoItem(
    val uri: Uri,
    val file: java.io.File?,
    val name: String,
    val extension: String,
    val sizeBytes: Long = 0L,
    val lastModified: Long = 0L,
    val relativePath: String = ""
) {
    val key: String get() = uri.toString()
    val parentKey: String get() = relativePath.trimEnd('/')
    val displayPath: String
        get() = file?.parent
            ?: relativePath.trimEnd('/').ifBlank { "Device storage" }
}

data class PhotoAlbum(
    val name: String,
    val key: String,
    val coverUri: Uri?,
    val count: Int
)

private enum class PhotoView {
    HOME,
    GRID,
    VIEWER
}

private enum class GridSize {
    SMALL,
    MEDIUM,
    LARGE
}

private enum class PhotoSort {
    DATE_NEW,
    DATE_OLD,
    NAME,
    SIZE
}

private enum class PhotoFilter {
    ALL,
    FAVORITES
}

private val PHOTO_EXTS = setOf(
    "jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "heif", "tif", "tiff", "avif"
)

private fun formatSize(bytes: Long): String = when {
    bytes < 1024L -> "$bytes B"
    bytes < 1_048_576L -> "%.1f KB".format(Locale.getDefault(), bytes / 1024f)
    bytes < 1_073_741_824L -> "%.1f MB".format(Locale.getDefault(), bytes / 1_048_576f)
    else -> "%.2f GB".format(Locale.getDefault(), bytes / 1_073_741_824f)
}

private fun formatDate(ms: Long): String =
    if (ms <= 0L) "Unknown date"
    else SimpleDateFormat("MMM d, yyyy  HH:mm", Locale.getDefault()).format(Date(ms))

private fun formatMonth(ms: Long): String =
    if (ms <= 0L) "Undated"
    else SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(ms))

// -----------------------------------------------------------------------------
// State
// -----------------------------------------------------------------------------

private class PhotosState {
    var allPhotos by mutableStateOf(emptyList<PhotoItem>())
    var albums by mutableStateOf(emptyList<PhotoAlbum>())
    var favorites by mutableStateOf(emptySet<String>())
    var selected by mutableStateOf(emptySet<String>())

    var loading by mutableStateOf(false)
    var loaded by mutableStateOf(false)

    var view by mutableStateOf(PhotoView.HOME)
    var activeAlbum by mutableStateOf<PhotoAlbum?>(null)
    var viewerIndex by mutableStateOf(0)

    var searchQuery by mutableStateOf("")
    var filter by mutableStateOf(PhotoFilter.ALL)
    var sort by mutableStateOf(PhotoSort.DATE_NEW)
    var gridSize by mutableStateOf(GridSize.MEDIUM)

    var selectionMode by mutableStateOf(false)
    var showSortMenu by mutableStateOf(false)
    var showInfo by mutableStateOf(false)
    var controlsVisible by mutableStateOf(true)
    var showDeleteDialog by mutableStateOf(false)
    var message by mutableStateOf<String?>(null)

    fun toggleFavorite(key: String) {
        favorites = if (key in favorites) favorites - key else favorites + key
    }

    fun toggleSelection(key: String) {
        selected = if (key in selected) selected - key else selected + key
        if (selected.isEmpty()) selectionMode = false
    }

    fun displayedPhotos(): List<PhotoItem> {
        val albumKey = activeAlbum?.key
        val query = searchQuery.trim()

        val filtered = allPhotos.asSequence()
            .filter { albumKey == null || it.parentKey == albumKey }
            .filter { filter == PhotoFilter.ALL || it.key in favorites }
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
            .toList()

        return when (sort) {
            PhotoSort.DATE_NEW -> filtered.sortedByDescending { it.lastModified }
            PhotoSort.DATE_OLD -> filtered.sortedBy { it.lastModified }
            PhotoSort.NAME -> filtered.sortedBy { it.name.lowercase(Locale.getDefault()) }
            PhotoSort.SIZE -> filtered.sortedByDescending { it.sizeBytes }
        }
    }

    fun resetToHome() {
        activeAlbum = null
        searchQuery = ""
        filter = PhotoFilter.ALL
        selectionMode = false
        selected = emptySet()
        showInfo = false
        showSortMenu = false
        view = PhotoView.HOME
    }
}

private fun hasImageAccess(context: Context): Boolean {
    return when {
        Build.VERSION.SDK_INT >= 34 -> {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
                ) == PackageManager.PERMISSION_GRANTED
        }

        Build.VERSION.SDK_INT >= 33 -> {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        }

        else -> {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}

// -----------------------------------------------------------------------------
// Permission gate — images only
// -----------------------------------------------------------------------------

@Composable
private fun PhotoPermissionGate(
    isDark: Boolean,
    onGranted: () -> Unit
) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(hasImageAccess(context)) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        granted = hasImageAccess(context)
        if (granted) onGranted()
    }

    fun requestAccess() {
        when {
            Build.VERSION.SDK_INT >= 34 -> launcher.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
                )
            )

            Build.VERSION.SDK_INT >= 33 -> launcher.launch(
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            )

            else -> launcher.launch(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            )
        }
    }

    LaunchedEffect(Unit) {
        if (!granted) requestAccess()
    }

    val bg = backgroundColor(isDark)
    val surface = surfaceColor(isDark)
    val text = textColor(isDark)
    val secondary = secondaryColor(isDark)

    Box(
        Modifier
            .fillMaxSize()
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            shape = RoundedCornerShape(16.dp),
            color = surface,
            border = BorderStroke(1.dp, borderColor(isDark))
        ) {
            Column(
                Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    Modifier
                        .size(64.dp)
                        .background(PhotosTheme.AccentSoft, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_bluebird_photos),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Text(
                    stringResource(R.string.let_bluebird_photos_access_your),
                    color = text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    stringResource(R.string.bluebird_only_needs_image_access_on),
                    color = secondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = { requestAccess() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PhotosTheme.Accent
                    )
                ) {
                    Text(stringResource(R.string.photos_choose_pictures))
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Entry point
// -----------------------------------------------------------------------------

@Composable
fun ImageViewerScreen(
    isDark: Boolean,
    initialPath: String = "",
    viewModel: LauncherViewModel? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state = remember { PhotosState() }

    var hasAccess by remember { mutableStateOf(hasImageAccess(context)) }

    if (!hasAccess) {
        PhotoPermissionGate(isDark = isDark) {
            hasAccess = true
        }
        return
    }

    val bg = backgroundColor(isDark)

    suspend fun loadPhotos() {
        state.loading = true

        val result = withContext(Dispatchers.IO) {
            val baseProjection = arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DATE_MODIFIED,
                MediaStore.MediaColumns.RELATIVE_PATH,
                MediaStore.MediaColumns.MIME_TYPE
            )

            val projection = if (initialPath.isNotBlank()) {
                baseProjection + MediaStore.MediaColumns.DATA
            } else {
                baseProjection
            }

            val collection =
                if (Build.VERSION.SDK_INT >= 29) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

            val found = LinkedHashMap<String, PhotoItem>()

            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
                val nameCol = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                val dateCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
                val relativeCol = cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)
                val dataCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)

                while (cursor.moveToNext()) {
                    if (idCol < 0) continue

                    val id = cursor.getLong(idCol)
                    val uri = ContentUris.withAppendedId(collection, id)
                    val filename = if (nameCol >= 0) {
                        cursor.getString(nameCol).orEmpty()
                    } else {
                        "Image"
                    }

                    val extension = filename.substringAfterLast('.', "").lowercase()
                    if (extension !in PHOTO_EXTS) continue

                    val size = if (sizeCol >= 0 && !cursor.isNull(sizeCol)) {
                        cursor.getLong(sizeCol)
                    } else {
                        0L
                    }

                    val modified = if (dateCol >= 0 && !cursor.isNull(dateCol)) {
                        cursor.getLong(dateCol) * 1000L
                    } else {
                        0L
                    }

                    val relativePath =
                        if (relativeCol >= 0) cursor.getString(relativeCol).orEmpty() else ""

                    val absolutePath =
                        if (dataCol >= 0 && !cursor.isNull(dataCol)) cursor.getString(dataCol) else null

                    found[uri.toString()] = PhotoItem(
                        uri = uri,
                        file = absolutePath?.let { java.io.File(it) },
                        name = filename.substringBeforeLast('.', filename),
                        extension = extension,
                        sizeBytes = size,
                        lastModified = modified,
                        relativePath = relativePath
                    )
                }
            }

            val photos = found.values.sortedByDescending { it.lastModified }

            val albums = photos
                .groupBy { photo ->
                    photo.parentKey.ifBlank { "Pictures" }
                }
                .map { (key, items) ->
                    val label = key
                        .trimEnd('/')
                        .substringAfterLast('/')
                        .ifBlank { "Pictures" }

                    PhotoAlbum(
                        name = label,
                        key = key,
                        coverUri = items.firstOrNull()?.uri,
                        count = items.size
                    )
                }
                .sortedByDescending { it.count }

            photos to albums
        }

        state.allPhotos = result.first
        state.albums = result.second
        state.loading = false
        state.loaded = true

        val directIndex = findInitialPhotoIndex(
            context = context,
            photos = state.allPhotos,
            initialPath = initialPath
        )

        if (directIndex >= 0) {
            state.activeAlbum = null
            state.searchQuery = ""
            state.filter = PhotoFilter.ALL
            state.viewerIndex = directIndex
            state.view = PhotoView.VIEWER
            state.controlsVisible = true
        }
    }

    // Keyed on initialPath rather than Unit: when this same window instance
    // gets reused for a different photo (WindowManager keeps one Image Viewer
    // window and swaps its extras instead of opening a new window), this needs
    // to re-run and jump to the new photo — not just load once and sit still.
    LaunchedEffect(initialPath) {
        loadPhotos()
    }

    LaunchedEffect(state.message) {
        if (state.message != null) {
            delay(2200)
            state.message = null
        }
    }

    LaunchedEffect(state.controlsVisible, state.view) {
        if (state.view == PhotoView.VIEWER && state.controlsVisible) {
            delay(4500)
            state.controlsVisible = false
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                if (state.view == PhotoView.VIEWER) Color.Black else bg
            )
    ) {
        when (state.view) {
            PhotoView.HOME -> PhotosHome(
                state = state,
                isDark = isDark,
                onOpenAll = {
                    state.activeAlbum = null
                    state.filter = PhotoFilter.ALL
                    state.view = PhotoView.GRID
                },
                onOpenFavorites = {
                    state.activeAlbum = null
                    state.filter = PhotoFilter.FAVORITES
                    state.view = PhotoView.GRID
                },
                onOpenAlbum = { album ->
                    state.activeAlbum = album
                    state.filter = PhotoFilter.ALL
                    state.view = PhotoView.GRID
                },
                onRescan = { scope.launch { loadPhotos() } }
            )

            PhotoView.GRID -> PhotosGrid(
                state = state,
                isDark = isDark,
                onBack = { state.resetToHome() },
                onOpenPhoto = { index ->
                    state.viewerIndex = index
                    state.view = PhotoView.VIEWER
                    state.controlsVisible = true
                },
                onRescan = { scope.launch { loadPhotos() } },
                context = context,
                viewModel = viewModel
            )

            PhotoView.VIEWER -> PhotoViewer(
                state = state,
                isDark = isDark,
                context = context,
                viewModel = viewModel,
                onBack = {
                    state.showInfo = false
                    state.view = PhotoView.GRID
                    state.controlsVisible = true
                }
            )
        }

        state.message?.let { msg ->
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(16.dp),
                shape = RoundedCornerShape(9.dp),
                color = if (isDark) Color(0xFF2A2A2A) else Color(0xFF303030),
                shadowElevation = 8.dp
            ) {
                Text(
                    ui(msg),
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}

private fun findInitialPhotoIndex(
    context: Context,
    photos: List<PhotoItem>,
    initialPath: String
): Int {
    if (initialPath.isBlank()) return -1

    val normalized = initialPath.trim()

    val direct = photos.indexOfFirst { photo ->
        photo.uri.toString() == normalized ||
            photo.file?.absolutePath == normalized
    }

    if (direct >= 0) return direct

    val maybeUri = runCatching { Uri.parse(normalized) }.getOrNull()
    if (maybeUri != null && maybeUri.scheme == "content") {
        val name = runCatching {
            context.contentResolver.query(
                maybeUri,
                arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        }.getOrNull()

        if (!name.isNullOrBlank()) {
            val byName = photos.indexOfFirst {
                it.name.equals(name.substringBeforeLast('.', name), ignoreCase = true)
            }
            if (byName >= 0) return byName
        }
    }

    val filename = normalized.substringAfterLast('/').substringAfterLast('\\')
    if (filename.isNotBlank()) {
        return photos.indexOfFirst {
            "${it.name}.${it.extension}".equals(filename, ignoreCase = true)
        }
    }

    return -1
}

// -----------------------------------------------------------------------------
// Home — Windows 11 / desktop style
// -----------------------------------------------------------------------------

@Composable
private fun PhotosHome(
    state: PhotosState,
    isDark: Boolean,
    onOpenAll: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenAlbum: (PhotoAlbum) -> Unit,
    onRescan: () -> Unit
) {
    val surface = surfaceColor(isDark)
    val surfaceAlt = surfaceAltColor(isDark)
    val text = textColor(isDark)
    val secondary = secondaryColor(isDark)
    val muted = mutedColor(isDark)

    Column(Modifier.fillMaxSize()) {
        PhotosTopBar(
            title = stringResource(R.string.photos_2),
            isDark = isDark,
            onBack = null,
            actions = {
                PhotosIconButton(
                    icon = FluentIcon.Refresh,
                    contentDescription = stringResource(R.string.common_refresh),
                    tint = text,
                    onClick = onRescan
                )
            }
        )

        if (state.loading) {
            PhotoLoadingState(isDark)
            return
        }

        if (!state.loaded || state.allPhotos.isEmpty()) {
            EmptyLibraryState(
                isDark = isDark,
                onRefresh = onRescan
            )
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            item {
                Column(
                    Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        stringResource(R.string.your_photos),
                        color = text,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        pluralText(R.plurals.plural_photos, state.allPhotos.size),
                        color = secondary,
                        fontSize = 13.sp
                    )
                }
            }

            item {
                PhotosSearchBar(
                    value = state.searchQuery,
                    onValueChange = {
                        state.searchQuery = it
                        state.activeAlbum = null
                        if (state.view == PhotoView.HOME) {
                            state.view = PhotoView.GRID
                        }
                    },
                    isDark = isDark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 4.dp)
                )
            }

            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeActionCard(
                        title = stringResource(R.string.all_photos),
                        subtitle = pluralText(R.plurals.plural_items, state.allPhotos.size),
                        icon = FluentIcon.ImageMultiple,
                        iconTint = PhotosTheme.Accent,
                        isDark = isDark,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenAll
                    )

                    HomeActionCard(
                        title = stringResource(R.string.favorites),
                        subtitle = pluralText(R.plurals.plural_items, state.favorites.size),
                        icon = FluentIcon.Favorite,
                        iconTint = PhotosTheme.Favorite,
                        isDark = isDark,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenFavorites
                    )
                }
            }

            item {
                HomeSectionHeader(
                    title = stringResource(R.string.recent),
                    actionText = stringResource(R.string.view_all),
                    isDark = isDark,
                    onAction = onOpenAll
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(154.dp)
                ) {
                    items(
                        state.allPhotos.take(12),
                        key = { it.key }
                    ) { photo ->
                        RecentPhotoCard(
                            photo = photo,
                            isDark = isDark,
                            onClick = {
                                val index = state.allPhotos.indexOf(photo)
                                if (index >= 0) {
                                    state.viewerIndex = index
                                    state.view = PhotoView.VIEWER
                                    state.controlsVisible = true
                                }
                            }
                        )
                    }
                }
            }

            item {
                HomeSectionHeader(
                    title = stringResource(R.string.albums),
                    actionText = "${state.albums.size}",
                    isDark = isDark,
                    onAction = {}
                )
            }

            items(
                state.albums.chunked(2),
                key = { row -> row.joinToString("|") { it.key } }
            ) { row ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { album ->
                        AlbumCard(
                            album = album,
                            isDark = isDark,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenAlbum(album) }
                        )
                    }
                    if (row.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = surface,
                    border = BorderStroke(1.dp, borderColor(isDark))
                ) {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            FluentIcon.Info,
                            contentDescription = null,
                            tint = PhotosTheme.Accent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.bluebird_photos),
                                color = text,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                stringResource(R.string.a_focused_image_viewer_with_desktop),
                                color = muted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    isDark: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = surfaceColor(isDark),
        border = BorderStroke(1.dp, borderColor(isDark))
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                Modifier
                    .size(34.dp)
                    .background(
                        iconTint.copy(alpha = 0.12f),
                        RoundedCornerShape(9.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Text(
                title,
                color = textColor(isDark),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                subtitle,
                color = mutedColor(isDark),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun PhotosSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val surface = surfaceAltColor(isDark)
    val text = textColor(isDark)
    val muted = mutedColor(isDark)

    Row(
        modifier
            .height(38.dp)
            .clip(RoundedCornerShape(19.dp))
            .background(surface)
            .border(
                BorderStroke(1.dp, borderColor(isDark)),
                RoundedCornerShape(19.dp)
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_bluebird_search),
            contentDescription = stringResource(R.string.search),
            tint = Color.Unspecified,
            modifier = Modifier.size(17.dp)
        )

        Spacer(Modifier.width(8.dp))

        Box(
            Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    stringResource(R.string.search_your_photos),
                    color = muted,
                    fontSize = 12.sp
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = text,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                cursorBrush = SolidColor(PhotosTheme.Accent),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.CenterVertically)
            )
        }

        if (value.isNotEmpty()) {
            PhotosIconButton(
                icon = FluentIcon.Close,
                contentDescription = stringResource(R.string.clear_search),
                tint = muted,
                onClick = { onValueChange("") },
                size = 28.dp,
                iconSize = 15.dp
            )
        }
    }
}

@Composable
private fun HomeSectionHeader(
    title: String,
    actionText: String,
    isDark: Boolean,
    onAction: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            color = textColor(isDark),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.weight(1f))
        if (actionText.isNotBlank()) {
            Text(
                actionText,
                color = PhotosTheme.Accent,
                fontSize = 12.sp,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onAction
                )
            )
        }
    }
}

@Composable
private fun RecentPhotoCard(
    photo: PhotoItem,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .width(144.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        SubcomposeAsyncImage(
            model = photo.uri,
            contentDescription = photo.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(surfaceAltColor(isDark))
                )
            },
            error = {
                PhotoBrokenState(isDark)
            }
        )

        Box(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                    )
                )
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                photo.name,
                color = Color.White,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AlbumCard(
    album: PhotoAlbum,
    isDark: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = surfaceColor(isDark),
        border = BorderStroke(1.dp, borderColor(isDark))
    ) {
        Column(Modifier.padding(8.dp)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.65f)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                if (album.coverUri != null) {
                    AsyncImage(
                        model = album.coverUri,
                        contentDescription = album.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    PhotoBrokenState(isDark)
                }

                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .background(
                            Color.Black.copy(alpha = 0.55f),
                            RoundedCornerShape(5.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        "${album.count}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                album.name,
                color = textColor(isDark),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                pluralText(R.plurals.plural_items, album.count),
                color = mutedColor(isDark),
                fontSize = 10.sp
            )
        }
    }
}

// -----------------------------------------------------------------------------
// Grid
// -----------------------------------------------------------------------------

@Composable
private fun PhotosGrid(
    state: PhotosState,
    isDark: Boolean,
    onBack: () -> Unit,
    onOpenPhoto: (Int) -> Unit,
    onRescan: () -> Unit,
    context: Context,
    viewModel: LauncherViewModel?
) {
    val photos = state.displayedPhotos()
    val columns = when (state.gridSize) {
        GridSize.SMALL -> 5
        GridSize.MEDIUM -> 4
        GridSize.LARGE -> 3
    }

    val gridState = rememberLazyGridState()
    val grouped = photos.withIndex().groupBy { (_, photo) ->
        formatMonth(photo.lastModified)
    }

    Column(Modifier.fillMaxSize()) {
        PhotosTopBar(
            title = state.activeAlbum?.name ?: when (state.filter) {
                PhotoFilter.ALL -> stringResource(R.string.all_photos)
                PhotoFilter.FAVORITES -> stringResource(R.string.favorites)
            },
            isDark = isDark,
            onBack = onBack,
            actions = {
                PhotosSearchBar(
                    value = state.searchQuery,
                    onValueChange = { state.searchQuery = it },
                    isDark = isDark,
                    modifier = Modifier.width(180.dp)
                )

                PhotosIconButton(
                    icon = when (state.gridSize) {
                        GridSize.SMALL -> FluentIcon.GridSmall
                        GridSize.MEDIUM -> FluentIcon.GridMedium
                        GridSize.LARGE -> FluentIcon.GridLarge
                    },
                    contentDescription = stringResource(R.string.grid_size),
                    tint = textColor(isDark),
                    onClick = {
                        state.gridSize = when (state.gridSize) {
                            GridSize.SMALL -> GridSize.MEDIUM
                            GridSize.MEDIUM -> GridSize.LARGE
                            GridSize.LARGE -> GridSize.SMALL
                        }
                    }
                )

                Box {
                    PhotosIconButton(
                        icon = FluentIcon.Sort,
                        contentDescription = stringResource(R.string.sort),
                        tint = textColor(isDark),
                        onClick = { state.showSortMenu = true }
                    )

                    BluebirdDropdownMenu(
                        expanded = state.showSortMenu,
                        onDismissRequest = { state.showSortMenu = false }
                    ) {
                        listOf(
                            PhotoSort.DATE_NEW to stringResource(R.string.newest_first),
                            PhotoSort.DATE_OLD to stringResource(R.string.oldest_first),
                            PhotoSort.NAME to stringResource(R.string.name),
                            PhotoSort.SIZE to stringResource(R.string.size)
                        ).forEach { (sort, label) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        label,
                                        color = if (state.sort == sort) PhotosTheme.Accent else textColor(isDark),
                                        fontSize = 13.sp
                                    )
                                },
                                leadingIcon = {
                                    if (state.sort == sort) {
                                        Icon(
                                            FluentIcon.Checkmark,
                                            null,
                                            tint = PhotosTheme.Accent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    state.sort = sort
                                    state.showSortMenu = false
                                }
                            )
                        }
                    }
                }

                PhotosIconButton(
                    icon = FluentIcon.Refresh,
                    contentDescription = stringResource(R.string.common_refresh),
                    tint = textColor(isDark),
                    onClick = onRescan
                )
            }
        )

        if (state.selectionMode) {
            SelectionBar(
                count = state.selected.size,
                isDark = isDark,
                onShare = {
                    sharePhotos(
                        context,
                        state.allPhotos.filter { it.key in state.selected }.map { it.uri }
                    )
                },
                onDelete = { state.showDeleteDialog = true },
                onCancel = {
                    state.selected = emptySet()
                    state.selectionMode = false
                }
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                pluralText(R.plurals.plural_items, photos.size),
                color = mutedColor(isDark),
                fontSize = 11.sp
            )
            Spacer(Modifier.weight(1f))
            if (state.filter == PhotoFilter.FAVORITES) {
                FilterPill(
                    label = stringResource(R.string.favorites),
                    icon = FluentIcon.Favorite,
                    active = true,
                    isDark = isDark,
                    onClick = {
                        state.filter = PhotoFilter.ALL
                    }
                )
            }
        }

        if (photos.isEmpty()) {
            PhotoNoResults(
                isDark = isDark,
                hasSearch = state.searchQuery.isNotBlank()
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                state = gridState,
                contentPadding = PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    bottom = 26.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                grouped.forEach { (month, itemsForMonth) ->
                    item(span = { GridItemSpan(columns) }) {
                        Text(
                            month,
                            color = secondaryColor(isDark),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(
                                start = 10.dp,
                                top = 10.dp,
                                bottom = 5.dp
                            )
                        )
                    }

                    items(
                        itemsForMonth,
                        key = { it.value.key }
                    ) { indexed ->
                        val photo = indexed.value
                        GridPhotoCell(
                            photo = photo,
                            isDark = isDark,
                            isSelected = photo.key in state.selected,
                            isFavorite = photo.key in state.favorites,
                            selectionMode = state.selectionMode,
                            onClick = {
                                if (state.selectionMode) {
                                    state.toggleSelection(photo.key)
                                } else {
                                    onOpenPhoto(photos.indexOf(photo).coerceAtLeast(0))
                                }
                            },
                            onLongPress = {
                                state.selectionMode = true
                                state.toggleSelection(photo.key)
                            }
                        )
                    }
                }
            }
        }
    }

    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { state.showDeleteDialog = false },
            shape = RoundedCornerShape(14.dp),
            containerColor = surfaceColor(isDark),
            title = {
                Text(
                    pluralText(R.plurals.plural_delete_photos, state.selected.size),
                    color = textColor(isDark),
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    stringResource(R.string.the_selected_photos_will_be_removed),
                    color = secondaryColor(isDark),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val deleted = state.selected.toSet()
                        deleted.forEach { key ->
                            state.allPhotos.firstOrNull { it.key == key }?.let { photo ->
                                deletePhoto(context, viewModel, photo)
                            }
                        }

                        state.allPhotos = state.allPhotos.filterNot { it.key in deleted }
                        state.albums = rebuildAlbums(state.allPhotos)
                        state.favorites = state.favorites - deleted
                        state.selected = emptySet()
                        state.selectionMode = false
                        state.showDeleteDialog = false
                        state.message = "Deleted"
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PhotosTheme.Danger
                    ),
                    shape = RoundedCornerShape(7.dp)
                ) {
                    Text(stringResource(R.string.common_delete))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { state.showDeleteDialog = false },
                    shape = RoundedCornerShape(7.dp)
                ) {
                    Text(stringResource(R.string.common_cancel), color = textColor(isDark))
                }
            }
        )
    }
}

@Composable
private fun GridPhotoCell(
    photo: PhotoItem,
    isDark: Boolean,
    isSelected: Boolean,
    isFavorite: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    Box(
        Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .pointerInput(photo.key) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongPress() }
                )
            }
    ) {
        SubcomposeAsyncImage(
            model = photo.uri,
            contentDescription = photo.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(surfaceAltColor(isDark))
                )
            },
            error = {
                PhotoBrokenState(isDark)
            }
        )

        if (isFavorite && !selectionMode) {
            Icon(
                painter = painterResource(R.drawable.ic_bluebird_favorite),
                contentDescription = stringResource(R.string.favorite),
                tint = Color.Unspecified,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(7.dp)
                    .size(17.dp)
            )
        }

        if (selectionMode) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        if (isSelected) PhotosTheme.Accent.copy(alpha = 0.28f) else Color.Transparent
                    )
            )

            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(7.dp)
                    .size(22.dp)
                    .background(
                        if (isSelected) PhotosTheme.Accent else Color.Black.copy(alpha = 0.45f),
                        CircleShape
                    )
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        FluentIcon.Checkmark,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    icon: ImageVector,
    active: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (active) PhotosTheme.Accent else surfaceAltColor(isDark),
        border = if (active) null else BorderStroke(1.dp, borderColor(isDark))
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                icon,
                null,
                tint = if (active) Color.White else textColor(isDark),
                modifier = Modifier.size(14.dp)
            )
            Text(
                label,
                color = if (active) Color.White else textColor(isDark),
                fontSize = 11.sp
            )
        }
    }
}

// -----------------------------------------------------------------------------
// Full-screen viewer
// -----------------------------------------------------------------------------

@Composable
private fun PhotoViewer(
    state: PhotosState,
    isDark: Boolean,
    context: Context,
    viewModel: LauncherViewModel?,
    onBack: () -> Unit
) {
    val photos = state.displayedPhotos()

    if (photos.isEmpty()) {
        onBack()
        return
    }

    state.viewerIndex = state.viewerIndex.coerceIn(0, photos.lastIndex)

    val photo = photos[state.viewerIndex]

    var scale by remember(photo.key) { mutableStateOf(1f) }
    var offsetX by remember(photo.key) { mutableStateOf(0f) }
    var offsetY by remember(photo.key) { mutableStateOf(0f) }
    var rotation by remember(photo.key) { mutableStateOf(0f) }

    val resetView = {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
        rotation = 0f
    }

    fun navigate(delta: Int) {
        val next = (state.viewerIndex + delta).coerceIn(0, photos.lastIndex)
        if (next != state.viewerIndex) {
            state.viewerIndex = next
            resetView()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(photo.key) {
                    detectTapGestures(
                        onTap = {
                            state.controlsVisible = !state.controlsVisible
                        },
                        onDoubleTap = {
                            if (scale > 1.5f) resetView()
                            else {
                                scale = 2.5f
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    )
                }
                .pointerInput(photo.key) {
                    detectTransformGestures { _, pan, zoom, rotationChange ->
                        scale = (scale * zoom).coerceIn(1f, 8f)
                        if (scale > 1f) {
                            offsetX += pan.x
                            offsetY += pan.y
                        }
                        rotation += rotationChange
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = photo.uri,
                contentDescription = photo.name,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offsetX
                        translationY = offsetY
                        rotationZ = rotation
                    },
                contentScale = ContentScale.Fit
            )
        }

        AnimatedVisibility(
            visible = state.controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(Modifier.fillMaxSize()) {
                ViewerTopBar(
                    photo = photo,
                    index = state.viewerIndex,
                    total = photos.size,
                    isDark = true,
                    isFavorite = photo.key in state.favorites,
                    scale = scale,
                    onBack = onBack,
                    onFavorite = {
                        state.toggleFavorite(photo.key)
                    },
                    onInfo = {
                        state.showInfo = !state.showInfo
                    },
                    onResetZoom = resetView
                )

                if (state.viewerIndex > 0) {
                    ViewerSideButton(
                        icon = FluentIcon.ChevronLeft,
                        contentDescription = stringResource(R.string.previous),
                        modifier = Modifier.align(Alignment.CenterStart),
                        onClick = { navigate(-1) }
                    )
                }

                if (state.viewerIndex < photos.lastIndex) {
                    ViewerSideButton(
                        icon = FluentIcon.ChevronRight,
                        contentDescription = stringResource(R.string.next),
                        modifier = Modifier.align(Alignment.CenterEnd),
                        onClick = { navigate(1) }
                    )
                }

                ViewerBottomBar(
                    photo = photo,
                    isDark = true,
                    onRotateLeft = { rotation -= 90f },
                    onRotateRight = { rotation += 90f },
                    onShare = {
                        sharePhoto(context, photo.uri)
                    },
                    onWallpaper = {
                        viewModel?.setCustomWallpaper(
                            photo.uri.toString(),
                            WallpaperTarget.HOME,
                            context
                        )
                        state.message = "Wallpaper updated"
                    },
                    onOpenWith = {
                        openWith(context, photo.uri)
                    },
                    onDelete = {
                        val deleted = deletePhoto(context, viewModel, photo)
                        if (deleted) {
                            state.allPhotos = state.allPhotos.filterNot { it.key == photo.key }
                            state.albums = rebuildAlbums(state.allPhotos)
                            state.favorites = state.favorites - photo.key

                            if (state.allPhotos.isEmpty()) {
                                state.view = PhotoView.HOME
                            } else {
                                state.viewerIndex = state.viewerIndex.coerceAtMost(
                                    state.allPhotos.lastIndex
                                )
                            }

                            state.message = "Deleted"
                        }
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = state.showInfo,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            PhotoInfoPanel(
                photo = photo,
                zoom = scale,
                isDark = true,
                onClose = { state.showInfo = false }
            )
        }
    }
}

@Composable
private fun ViewerTopBar(
    photo: PhotoItem,
    index: Int,
    total: Int,
    isDark: Boolean,
    isFavorite: Boolean,
    scale: Float,
    onBack: () -> Unit,
    onFavorite: () -> Unit,
    onInfo: () -> Unit,
    onResetZoom: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Black.copy(alpha = 0.78f),
                        Color.Transparent
                    )
                )
            )
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PhotosIconButton(
            icon = FluentIcon.ArrowBack,
            contentDescription = stringResource(R.string.setup_back),
            tint = Color.White,
            onClick = onBack
        )

        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                photo.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${index + 1} of $total",
                color = Color.White.copy(alpha = 0.66f),
                fontSize = 11.sp
            )
        }

        if (scale != 1f) {
            Surface(
                modifier = Modifier
                    .padding(end = 4.dp)
                    .clickable(onClick = onResetZoom),
                shape = RoundedCornerShape(6.dp),
                color = Color.White.copy(alpha = 0.14f)
            ) {
                Text(
                    "${(scale * 100).roundToInt()}%",
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
            }
        }

        PhotosIconButton(
            icon = if (isFavorite) FluentIcon.Favorite else FluentIcon.FavoriteBorder,
            contentDescription = stringResource(R.string.favorite),
            tint = if (isFavorite) PhotosTheme.Favorite else Color.White,
            onClick = onFavorite
        )

        PhotosIconButton(
            icon = FluentIcon.Info,
            contentDescription = stringResource(R.string.photo_information),
            tint = Color.White,
            onClick = onInfo
        )
    }
}

@Composable
private fun ViewerSideButton(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier
            .padding(horizontal = 10.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.42f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun BoxScope.ViewerBottomBar(
    photo: PhotoItem,
    isDark: Boolean,
    onRotateLeft: () -> Unit,
    onRotateRight: () -> Unit,
    onShare: () -> Unit,
    onWallpaper: () -> Unit,
    onOpenWith: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.9f)
                    )
                )
            )
            .padding(top = 42.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(
                    rememberScrollState()
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ViewerAction(
                icon = FluentIcon.RotateLeft,
                label = stringResource(R.string.rotate),
                onClick = onRotateLeft
            )
            ViewerAction(
                icon = FluentIcon.RotateRight,
                label = stringResource(R.string.rotate),
                onClick = onRotateRight
            )
            ViewerAction(
                icon = FluentIcon.Share,
                label = stringResource(R.string.share),
                onClick = onShare
            )
            ViewerAction(
                icon = FluentIcon.WallpaperIcon,
                label = stringResource(R.string.wallpaper),
                onClick = onWallpaper
            )
            ViewerAction(
                icon = FluentIcon.OpenWith,
                label = stringResource(R.string.open_with),
                onClick = onOpenWith
            )
            ViewerAction(
                icon = FluentIcon.Delete,
                label = stringResource(R.string.delete),
                tint = PhotosTheme.Danger,
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun ViewerAction(
    icon: ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Text(
            label,
            color = tint.copy(alpha = 0.78f),
            fontSize = 9.sp
        )
    }
}

@Composable
private fun PhotoInfoPanel(
    photo: PhotoItem,
    zoom: Float,
    isDark: Boolean,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(end = 12.dp)
            .width(255.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xEE1A1A1A),
        shadowElevation = 16.dp
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    FluentIcon.Info,
                    null,
                    tint = PhotosTheme.Accent,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.photo_information),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                PhotosIconButton(
                    icon = FluentIcon.Close,
                    contentDescription = stringResource(R.string.close),
                    tint = Color.White.copy(alpha = 0.72f),
                    onClick = onClose,
                    size = 28.dp,
                    iconSize = 15.dp
                )
            }

            androidx.compose.material3.HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f)
            )

            PhotoInfoRow(stringResource(R.string.name), photo.name)
            PhotoInfoRow(stringResource(R.string.type), photo.extension.uppercase(Locale.getDefault()))
            PhotoInfoRow(stringResource(R.string.size), formatSize(photo.sizeBytes))
            PhotoInfoRow(stringResource(R.string.modified), formatDate(photo.lastModified))
            PhotoInfoRow(stringResource(R.string.location), photo.displayPath)
            PhotoInfoRow(stringResource(R.string.zoom), "${(zoom * 100).roundToInt()}%")
        }
    }
}

@Composable
private fun PhotoInfoRow(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            label,
            color = Color.White.copy(alpha = 0.46f),
            fontSize = 10.sp
        )
        Text(
            value.ifBlank { "—" },
            color = Color.White,
            fontSize = 12.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// -----------------------------------------------------------------------------
// Empty/loading states
// -----------------------------------------------------------------------------

@Composable
private fun PhotoLoadingState(isDark: Boolean) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_bluebird_photos),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(34.dp)
            )
            Text(
                stringResource(R.string.loading_your_photos),
                color = textColor(isDark),
                fontSize = 14.sp
            )
            Text(
                stringResource(R.string.building_your_photo_library),
                color = mutedColor(isDark),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun EmptyLibraryState(
    isDark: Boolean,
    onRefresh: () -> Unit
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_bluebird_photos),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(46.dp)
            )
            Text(
                stringResource(R.string.no_photos_found),
                color = textColor(isDark),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                stringResource(R.string.add_pictures_to_your_device_then),
                color = secondaryColor(isDark),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRefresh,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PhotosTheme.Accent
                )
            ) {
                Text(stringResource(R.string.common_refresh))
            }
        }
    }
}

@Composable
private fun PhotoNoResults(
    isDark: Boolean,
    hasSearch: Boolean
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Icon(
                FluentIcon.Search,
                null,
                tint = mutedColor(isDark),
                modifier = Modifier.size(38.dp)
            )
            Text(
                if (hasSearch) stringResource(R.string.no_matching_photos) else stringResource(R.string.no_photos_in_this_view),
                color = textColor(isDark),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            if (hasSearch) {
                Text(
                    stringResource(R.string.try_another_search_term),
                    color = mutedColor(isDark),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun PhotoBrokenState(isDark: Boolean) {
    Box(
        Modifier
            .fillMaxSize()
            .background(surfaceAltColor(isDark)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            FluentIcon.BrokenImage,
            null,
            tint = mutedColor(isDark),
            modifier = Modifier.size(24.dp)
        )
    }
}

// -----------------------------------------------------------------------------
// Shared UI
// -----------------------------------------------------------------------------

@Composable
private fun PhotosTopBar(
    title: String,
    isDark: Boolean,
    onBack: (() -> Unit)?,
    actions: @Composable RowScope.() -> Unit
) {
    val surface = surfaceColor(isDark)

    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(surface)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (onBack != null) {
                PhotosIconButton(
                    icon = FluentIcon.ArrowBack,
                    contentDescription = stringResource(R.string.setup_back),
                    tint = textColor(isDark),
                    onClick = onBack
                )
            } else {
                Box(
                    Modifier
                        .size(30.dp)
                        .background(PhotosTheme.Accent, RoundedCornerShape(7.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_bluebird_photos),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                title,
                color = textColor(isDark),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            actions()
        }

        androidx.compose.material3.HorizontalDivider(
            color = borderColor(isDark)
        )
    }
}

@Composable
private fun PhotosIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp = 34.dp,
    iconSize: androidx.compose.ui.unit.Dp = 18.dp
) {
    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(7.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
private fun SelectionBar(
    count: Int,
    isDark: Boolean,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(PhotosTheme.Accent)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.selected, count),
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.weight(1f))

        PhotosIconButton(
            icon = FluentIcon.Share,
            contentDescription = stringResource(R.string.share_selected),
            tint = Color.White,
            onClick = onShare
        )
        PhotosIconButton(
            icon = FluentIcon.Delete,
            contentDescription = stringResource(R.string.delete_selected),
            tint = Color.White,
            onClick = onDelete
        )
        PhotosIconButton(
            icon = FluentIcon.Close,
            contentDescription = stringResource(R.string.cancel_selection),
            tint = Color.White,
            onClick = onCancel
        )
    }
}

// -----------------------------------------------------------------------------
// Actions / helpers
// -----------------------------------------------------------------------------

private fun rebuildAlbums(photos: List<PhotoItem>): List<PhotoAlbum> {
    return photos
        .groupBy { it.parentKey.ifBlank { "Pictures" } }
        .map { (key, items) ->
            PhotoAlbum(
                name = key.trimEnd('/').substringAfterLast('/').ifBlank { "Pictures" },
                key = key,
                coverUri = items.firstOrNull()?.uri,
                count = items.size
            )
        }
        .sortedByDescending { it.count }
}

private fun deletePhoto(
    context: Context,
    viewModel: LauncherViewModel?,
    photo: PhotoItem
): Boolean {
    return runCatching {
        if (photo.file != null && viewModel != null) {
            viewModel.deleteToRecycleBin(photo.file.absolutePath)
            true
        } else {
            context.contentResolver.delete(photo.uri, null, null) > 0
        }
    }.getOrDefault(false)
}

private fun sharePhoto(context: Context, uri: Uri) {
    runCatching {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(send, ui(context, "Share photo")).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
        )
    }
}

private fun sharePhotos(context: Context, uris: List<Uri>) {
    if (uris.isEmpty()) return

    runCatching {
        val send = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "image/*"
            putParcelableArrayListExtra(
                Intent.EXTRA_STREAM,
                ArrayList(uris)
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(send, ui(context, "Share photos")).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
        )
    }
}

private fun openWith(context: Context, uri: Uri) {
    runCatching {
        val open = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(open, ui(context, "Open with")).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
        )
    }
}
