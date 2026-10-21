package io.github.norbertweb.bluebird.i18n

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.norbertweb.bluebird.R

/**
 * Built-in app names are IDENTITY keys in Bluebird (custom icon lookup via
 * builtInAppDisplayName(), saved desktop shortcut labels, window icon keys).
 * They stay English in code and in persisted data. Use [localizedAppName] ONLY at
 * display time to show the user's language.
 */
@StringRes
fun builtInAppNameRes(name: String): Int? = when (name) {
    "Settings" -> R.string.appname_settings
    "Calculator" -> R.string.appname_calculator
    "Calendar" -> R.string.appname_calendar
    "Bluebird Store" -> R.string.appname_store
    "Word Impress" -> R.string.appname_word_impress
    "Files" -> R.string.appname_files
    "Browser" -> R.string.appname_browser
    "Media Player" -> R.string.appname_media_player
    "Recycle Bin" -> R.string.appname_recycle_bin
    "Image Viewer" -> R.string.appname_image_viewer
    "Text Editor" -> R.string.appname_text_editor
    "Terminal" -> R.string.appname_terminal
    "Program Manager" -> R.string.appname_program_manager
    "This PC" -> R.string.appname_this_pc
    else -> null
}

@Composable
fun localizedAppName(name: String): String =
    builtInAppNameRes(name)?.let { stringResource(it) } ?: name
