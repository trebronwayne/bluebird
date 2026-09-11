package io.github.norbertweb.bluebird.browser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fluent.ui.system.icons.filled.ArrowDownload
import fluent.ui.system.icons.filled.Star
import fluent.ui.system.icons.regular.Add
import fluent.ui.system.icons.regular.ArrowClockwise
import fluent.ui.system.icons.regular.ArrowDownload
import fluent.ui.system.icons.regular.ArrowLeft
import fluent.ui.system.icons.regular.ArrowRight
import fluent.ui.system.icons.regular.Bookmark
import fluent.ui.system.icons.regular.CheckmarkCircle
import fluent.ui.system.icons.regular.ChevronDown
import fluent.ui.system.icons.regular.ChevronUp
import fluent.ui.system.icons.regular.Delete
import fluent.ui.system.icons.regular.Dismiss
import fluent.ui.system.icons.regular.Document
import fluent.ui.system.icons.regular.Edit
import fluent.ui.system.icons.regular.ErrorCircle
import fluent.ui.system.icons.regular.EyeOff
import fluent.ui.system.icons.regular.Globe
import fluent.ui.system.icons.regular.History
import fluent.ui.system.icons.regular.Home
import fluent.ui.system.icons.regular.Info
import fluent.ui.system.icons.regular.Location
import fluent.ui.system.icons.regular.LockClosed
import fluent.ui.system.icons.regular.MoreVertical
import fluent.ui.system.icons.regular.Pause
import fluent.ui.system.icons.regular.Pin
import fluent.ui.system.icons.regular.Print
import fluent.ui.system.icons.regular.Prohibited
import fluent.ui.system.icons.regular.Search
import fluent.ui.system.icons.regular.Settings
import fluent.ui.system.icons.regular.Share
import fluent.ui.system.icons.regular.Shield
import fluent.ui.system.icons.regular.Sparkle
import fluent.ui.system.icons.regular.Star
import fluent.ui.system.icons.regular.Subtract
import fluent.ui.system.icons.regular.Warning
import fluent.ui.system.icons.regular.ZoomIn
import fluent.ui.system.icons.FluentIcons as NiyajaliFluentIcons

/**
 * Bluebird's single Fluent System Icons gateway.
 *
 * The browser UI depends only on these semantic names and never reaches into
 * the icon library directly. This keeps the whole OS icon system replaceable
 * without rewriting individual screens.
 */
object FluentIcons {
    val Add: ImageVector get() = NiyajaliFluentIcons.Regular.Add
    val ArrowBack: ImageVector get() = NiyajaliFluentIcons.Regular.ArrowLeft
    val ArrowForward: ImageVector get() = NiyajaliFluentIcons.Regular.ArrowRight
    val Article: ImageVector get() = NiyajaliFluentIcons.Regular.Document
    val BookmarkBorder: ImageVector get() = NiyajaliFluentIcons.Regular.Bookmark
    val Blocked: ImageVector get() = NiyajaliFluentIcons.Regular.Prohibited
    val CheckCircle: ImageVector get() = NiyajaliFluentIcons.Regular.CheckmarkCircle
    val Close: ImageVector get() = NiyajaliFluentIcons.Regular.Dismiss
    val Delete: ImageVector get() = NiyajaliFluentIcons.Regular.Delete
    val Download: ImageVector get() = NiyajaliFluentIcons.Regular.ArrowDownload
    val Downloading: ImageVector get() = NiyajaliFluentIcons.Filled.ArrowDownload
    val Edit: ImageVector get() = NiyajaliFluentIcons.Regular.Edit
    val Error: ImageVector get() = NiyajaliFluentIcons.Regular.ErrorCircle
    val FindInPage: ImageVector get() = NiyajaliFluentIcons.Regular.Search
    val History: ImageVector get() = NiyajaliFluentIcons.Regular.History
    val Home: ImageVector get() = NiyajaliFluentIcons.Regular.Home
    val Info: ImageVector get() = NiyajaliFluentIcons.Regular.Info
    val KeyboardArrowDown: ImageVector get() = NiyajaliFluentIcons.Regular.ChevronDown
    val KeyboardArrowUp: ImageVector get() = NiyajaliFluentIcons.Regular.ChevronUp
    val Language: ImageVector get() = NiyajaliFluentIcons.Regular.Globe
    val LocationOn: ImageVector get() = NiyajaliFluentIcons.Regular.Location
    val Lock: ImageVector get() = NiyajaliFluentIcons.Regular.LockClosed
    val LockClosed: ImageVector get() = NiyajaliFluentIcons.Regular.LockClosed
    val MoreVert: ImageVector get() = NiyajaliFluentIcons.Regular.MoreVertical
    val Pause: ImageVector get() = NiyajaliFluentIcons.Regular.Pause
    val Print: ImageVector get() = NiyajaliFluentIcons.Regular.Print
    val PushPin: ImageVector get() = NiyajaliFluentIcons.Regular.Pin
    val Refresh: ImageVector get() = NiyajaliFluentIcons.Regular.ArrowClockwise
    val Remove: ImageVector get() = NiyajaliFluentIcons.Regular.Subtract
    val Search: ImageVector get() = NiyajaliFluentIcons.Regular.Search
    val Security: ImageVector get() = NiyajaliFluentIcons.Regular.Shield
    val Settings: ImageVector get() = NiyajaliFluentIcons.Regular.Settings
    val Share: ImageVector get() = NiyajaliFluentIcons.Regular.Share
    val Star: ImageVector get() = NiyajaliFluentIcons.Filled.Star
    val StarBorder: ImageVector get() = NiyajaliFluentIcons.Regular.Star
    val Sparkle: ImageVector get() = NiyajaliFluentIcons.Regular.Sparkle
    val VisibilityOff: ImageVector get() = NiyajaliFluentIcons.Regular.EyeOff
    val Warning: ImageVector get() = NiyajaliFluentIcons.Regular.Warning
    val ZoomIn: ImageVector get() = NiyajaliFluentIcons.Regular.ZoomIn
}


@Composable
fun FluentIcon(
    icon: ImageVector,
    contentDescription: String? = null,
    tint: Color = Color.Unspecified,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier
    )
}


/** Compact address/search field used by the lightweight browser chrome. */
@Composable
fun BrowserTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    textColor: Color,
    modifier: Modifier = Modifier,
    onFocusRequest: () -> Unit = {},
    onImeGo: () -> Unit = {},
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(36.dp),
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(color = textColor, fontSize = fontSize),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
        keyboardActions = KeyboardActions(onGo = { onImeGo() }),
        decorationBox = { innerTextField ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(textColor.copy(alpha = 0.06f), RoundedCornerShape(18.dp))
                    .border(1.dp, textColor.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 13.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isBlank()) {
                    androidx.compose.material3.Text(
                        placeholder,
                        color = textColor.copy(alpha = 0.48f),
                        fontSize = fontSize,
                        maxLines = 1
                    )
                }
                innerTextField()
            }
        }
    )
}

/** Small Fluent-icon navigation button for browser chrome. */
@Composable
fun NavBtn(
    icon: ImageVector,
    enabled: Boolean = true,
    tint: Color = Color.Unspecified,
    contentDescription: String?,
    onClick: () -> Unit
) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(32.dp)
    ) {
        FluentIcon(
            icon = icon,
            contentDescription = contentDescription,
            tint = if (enabled) tint else tint.copy(alpha = 0.32f),
            modifier = Modifier.size(17.dp)
        )
    }
}
