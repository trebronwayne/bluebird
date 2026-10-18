package io.github.norbertweb.bluebird.ui.components

import android.content.Context
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import io.github.norbertweb.bluebird.LauncherScreen
import io.github.norbertweb.bluebird.R
import io.github.norbertweb.bluebird.WindowIconKey
import java.io.File

// ─────────────────────────────────────────────────────────────────────────────
// PngIcons.kt — single source of truth for every colored PNG icon in the shell.
// Desktop, File Explorer and Taskbar all resolve icons through these helpers,
// so one extension always maps to the same drawable everywhere.
// ─────────────────────────────────────────────────────────────────────────────

/** Draws a colored PNG without any tint (Compose's Icon() would flatten it to one color). */
@Composable
fun PngIcon(
    @DrawableRes res: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit
) {
    Image(
        painter = painterResource(id = res),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}

/** Built-in app icon, or null when the screen has no dedicated icon. */
@DrawableRes
fun appIconRes(screen: LauncherScreen?, recycleBinFull: Boolean = false): Int? = when (screen) {
    LauncherScreen.FILE_EXPLORER          -> R.drawable.ic_app_file_explorer
    LauncherScreen.SETTINGS               -> R.drawable.ic_app_settings
    LauncherScreen.BROWSER                -> R.drawable.ic_app_browser
    LauncherScreen.CALCULATOR             -> R.drawable.ic_app_calculator
    LauncherScreen.CALENDAR               -> R.drawable.ic_app_calendar
    LauncherScreen.MEDIA_PLAYER           -> R.drawable.ic_app_media_player
    LauncherScreen.IMAGE_VIEWER           -> R.drawable.ic_app_image_viewer
    LauncherScreen.WORD_IMPRESS           -> R.drawable.ic_app_word_impress
    LauncherScreen.PremiumTextEditorScreen -> R.drawable.ic_app_text_editor
    LauncherScreen.BLUEBIRD_STORE         -> R.drawable.ic_app_bluebird_store
    LauncherScreen.PROGRAM_MANAGER        -> R.drawable.ic_app_program_manager
    LauncherScreen.COPY_PROGRESS          -> R.drawable.ic_app_copy_progress
    LauncherScreen.WEB_APP_VIEWER         -> R.drawable.ic_app_web_app
    LauncherScreen.TERMINAL               -> R.drawable.ic_app_terminal
    LauncherScreen.RECYCLE_BIN            ->
        if (recycleBinFull) R.drawable.ic_app_recycle_bin_full else R.drawable.ic_app_recycle_bin_empty
    else -> null
}

/**
 * Icon for a running window (taskbar strip, window title bar). Resolves from
 * WindowState.iconKey, so it always agrees with the desktop/Start app icons.
 * Web apps ("bpk:<id>") normally show their favicon bitmap; this is the fallback.
 */
@DrawableRes
fun windowIconRes(key: String): Int = when {
    key == WindowIconKey.PremiumTextEditorScreen -> R.drawable.ic_app_text_editor
    key == WindowIconKey.SETTINGS                -> R.drawable.ic_app_settings
    key == WindowIconKey.FILE_EXPLORER           -> R.drawable.ic_app_file_explorer
    key == WindowIconKey.BROWSER                 -> R.drawable.ic_app_browser
    key == WindowIconKey.CALCULATOR              -> R.drawable.ic_app_calculator
    key == WindowIconKey.CALENDAR                -> R.drawable.ic_app_calendar
    key == WindowIconKey.MEDIA_PLAYER            -> R.drawable.ic_app_media_player
    key == WindowIconKey.IMAGE_VIEWER            -> R.drawable.ic_app_image_viewer
    key == WindowIconKey.WORD_IMPRESS            -> R.drawable.ic_app_word_impress
    key == WindowIconKey.BLUEBIRD_STORE          -> R.drawable.ic_app_bluebird_store
    key == WindowIconKey.RECYCLE_BIN             -> R.drawable.ic_app_recycle_bin_empty
    key == WindowIconKey.TERMINAL                -> R.drawable.ic_app_terminal
    key == WindowIconKey.PROGRAM_MANAGER         -> R.drawable.ic_app_program_manager
    key == WindowIconKey.COPY_PROGRESS           -> R.drawable.ic_app_copy_progress
    key == WindowIconKey.WEB_APP || key.startsWith("bpk:") -> R.drawable.ic_app_web_app
    else                                         -> R.drawable.ic_app_generic
}

/** Icon for a file or folder, chosen from its extension. */
@DrawableRes
fun fileIconRes(isDirectory: Boolean, extension: String): Int =
    if (isDirectory) R.drawable.ic_folder else fileIconResForExtension(extension)

@DrawableRes
fun fileIconRes(file: File): Int = fileIconRes(file.isDirectory, file.extension)

@DrawableRes
fun fileIconResForExtension(extension: String): Int = when (extension.lowercase()) {
    // Documents & text
    "pdf"                                   -> R.drawable.ic_file_pdf
    "txt", "log"                            -> R.drawable.ic_file_txt
    "doc", "docx", "rtf", "odt"             -> R.drawable.ic_file_doc
    "xls", "xlsx", "ods"                    -> R.drawable.ic_file_xls
    "csv"                                   -> R.drawable.ic_file_csv
    "ppt", "pptx", "odp"                    -> R.drawable.ic_file_ppt
    "md"                                    -> R.drawable.ic_file_md
    "json"                                  -> R.drawable.ic_file_json
    "xml"                                   -> R.drawable.ic_file_xml
    "html", "htm"                           -> R.drawable.ic_file_html
    "js", "ts", "py", "kt", "java", "css",
    "c", "cpp", "h", "cs", "sh", "php"      -> R.drawable.ic_file_code
    "wdoc"                                  -> R.drawable.ic_file_wdoc

    // Images
    "jpg", "jpeg"                           -> R.drawable.ic_file_jpg
    "png"                                   -> R.drawable.ic_file_png
    "gif"                                   -> R.drawable.ic_file_gif
    "webp"                                  -> R.drawable.ic_file_webp
    "bmp"                                   -> R.drawable.ic_file_bmp
    "svg"                                   -> R.drawable.ic_file_svg
    "heic", "heif", "ico", "tif", "tiff", "avif" -> R.drawable.ic_file_image

    // Audio
    "mp3"                                   -> R.drawable.ic_file_mp3
    "wav"                                   -> R.drawable.ic_file_wav
    "flac"                                  -> R.drawable.ic_file_flac
    "ogg", "aac", "m4a", "opus", "wma", "amr", "mid", "midi" -> R.drawable.ic_file_audio

    // Video
    "mp4"                                   -> R.drawable.ic_file_mp4
    "mkv"                                   -> R.drawable.ic_file_mkv
    "avi"                                   -> R.drawable.ic_file_avi
    "mov"                                   -> R.drawable.ic_file_mov
    "webm", "3gp", "wmv", "m4v", "flv"      -> R.drawable.ic_file_video

    // Archives
    "zip"                                   -> R.drawable.ic_file_zip
    "rar"                                   -> R.drawable.ic_file_rar
    "7z"                                    -> R.drawable.ic_file_7z
    "tar", "gz", "tgz", "bz2", "xz"         -> R.drawable.ic_file_archive
    "iso"                                   -> R.drawable.ic_file_iso

    // Apps, shortcuts & system
    "apk", "apks", "xapk"                   -> R.drawable.ic_file_apk
    "exe", "msi", "bluebird"                -> R.drawable.ic_file_exe
    "bpk"                                   -> R.drawable.ic_file_bpk
    "webapp"                                -> R.drawable.ic_file_webapp
    "desktop", "lnk"                        -> R.drawable.ic_file_shortcut
    "ttf", "otf", "woff", "woff2"           -> R.drawable.ic_file_font
    "db", "sqlite"                          -> R.drawable.ic_file_database

    else                                    -> R.drawable.ic_file_unknown
}

/** Icon for a content:// shortcut target, chosen from its MIME type. */
@DrawableRes
fun uriShortcutIconRes(context: Context, uri: Uri, isFolder: Boolean): Int {
    if (isFolder) return R.drawable.ic_folder
    val mime = context.contentResolver.getType(uri).orEmpty().lowercase()
    return when {
        mime == "application/pdf"        -> R.drawable.ic_file_pdf
        mime.startsWith("audio/")        -> R.drawable.ic_file_audio
        mime.startsWith("video/")        -> R.drawable.ic_file_video
        mime.startsWith("image/")        -> R.drawable.ic_file_image
        mime.contains("html")            -> R.drawable.ic_file_html
        mime.startsWith("text/")         -> R.drawable.ic_file_txt
        else                             -> R.drawable.ic_file_unknown
    }
}
