package io.github.norbertweb.bluebird.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import io.github.norbertweb.bluebird.R
import androidx.compose.ui.res.stringResource
import io.github.norbertweb.bluebird.ui.theme.LocalIsDarkTheme
import kotlin.math.max

/**
 * Drop-in replacement for Material3 `DropdownMenu` with a Windows 11 acrylic look.
 *
 * Material's DropdownMenu paints an opaque container we cannot make translucent without a newer
 * Material3 API, so this builds the same thing on `Popup`: anchored to its parent like Material's,
 * flipped above the anchor / kept on-screen when there is no room, rounded 8 dp with a hairline
 * border, translucent over a real compositor blur ([PopupWindowBlur]). Same call shape:
 * `expanded`, `onDismissRequest`, optional `modifier` (applied to the inner Column, like Material).
 * Menu items are still the standard `DropdownMenuItem`s.
 */
@Composable
fun BluebirdDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    if (!expanded) return
    val density = LocalDensity.current
    val isDark = LocalIsDarkTheme.current
    val fill = popupMenuFill(isDark)
    val provider = remember(offset, density) {
        AnchoredMenuPositionProvider(
            offset = with(density) { IntOffset(offset.x.roundToPx(), offset.y.roundToPx()) },
            margin = with(density) { 8.dp.roundToPx() }
        )
    }
    Popup(
        popupPositionProvider = provider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true)
    ) {
        PopupWindowBlur(24.dp)
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = fill,
            border = BorderStroke(1.dp, if (isDark) Color(0x33FFFFFF) else Color(0x1F000000)),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = modifier
                    .width(IntrinsicSize.Max)
                    .widthIn(min = 128.dp, max = 300.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 6.dp),
                content = content
            )
        }
    }
}

/** Below the anchor, flipped above if it would overflow; always clamped inside the window. */
private class AnchoredMenuPositionProvider(
    private val offset: IntOffset,
    private val margin: Int
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        var x = anchorBounds.left + offset.x
        var y = anchorBounds.bottom + offset.y
        if (y + popupContentSize.height > windowSize.height - margin) {
            val above = anchorBounds.top - popupContentSize.height - offset.y
            y = if (above >= margin) above else max(margin, windowSize.height - popupContentSize.height - margin)
        }
        x = x.coerceIn(margin, max(margin, windowSize.width - popupContentSize.width - margin))
        return IntOffset(x, y)
    }
}

/**
 * "App info" and "Uninstall" rows for a menu belonging to an INSTALLED Android app. Renders
 * nothing for built-in apps and BPK web apps; Uninstall is hidden where Android won't allow it
 * (system apps, Bluebird itself). Call inside any BluebirdDropdownMenu / DropdownMenu.
 */
@Composable
fun AppSystemMenuItems(
    packageName: String,
    onDismiss: () -> Unit,
    textColor: Color = Color.Unspecified,
    destructiveColor: Color = Color(0xFFE5484D)
) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val isAndroidApp = remember(packageName) { io.github.norbertweb.bluebird.AppActions.isInstalledAndroidApp(ctx, packageName) }
    if (!isAndroidApp) return
    val canUninstall = remember(packageName) { io.github.norbertweb.bluebird.AppActions.canUninstall(ctx, packageName) }
    androidx.compose.material3.Divider(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        color = (if (textColor == Color.Unspecified) Color.Gray else textColor).copy(alpha = 0.18f)
    )
    androidx.compose.material3.DropdownMenuItem(
        text = { androidx.compose.material3.Text(stringResource(R.string.app_info), color = textColor) },
        onClick = { onDismiss(); io.github.norbertweb.bluebird.AppActions.openAppInfo(ctx, packageName) }
    )
    if (canUninstall) {
        androidx.compose.material3.DropdownMenuItem(
            text = { androidx.compose.material3.Text(stringResource(R.string.program_uninstall), color = destructiveColor) },
            onClick = { onDismiss(); io.github.norbertweb.bluebird.AppActions.requestUninstall(ctx, packageName) }
        )
    }
}
