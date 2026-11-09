package io.github.norbertweb.bluebird.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.norbertweb.bluebird.R
import io.github.norbertweb.bluebird.ui.theme.bluebirdColors

/**
 * The single Windows 11 search field used by both Start and Search.
 *
 * What changed vs. the old per-screen copies: no black drop-shadow halo and no gradient (those
 * made the bar look dark and muddy); the fill is a light translucent scrim lifted ABOVE the
 * Acrylic (white in dark theme, like Fluent's ControlFillColorDefault); it is taller (48dp);
 * and focus is shown like WinUI does, with a 2dp accent line along the bottom edge.
 */
@Composable
fun Win11SearchBox(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    isDark: Boolean,
    accent: Color,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    onSearch: (() -> Unit)? = null
) {
    var focused by remember { mutableStateOf(false) }
    val textPrimary = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    val shape = RoundedCornerShape(24.dp)

    val fill = when {
        isDark && focused -> Color.White.copy(alpha = 0.13f)
        isDark -> Color.White.copy(alpha = 0.09f)
        focused -> Color.White.copy(alpha = 0.95f)
        else -> Color.White.copy(alpha = 0.72f)
    }
    val stroke = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.07f)
    val focusLine = if (focused) accent else Color.Transparent
    val localFocus = focusRequester ?: remember { FocusRequester() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(shape)
            .background(fill)
            .drawBehind {
                // Bottom accent line, clipped by the pill so its ends curve like WinUI's.
                val h = 2.dp.toPx()
                drawRect(focusLine, topLeft = Offset(0f, size.height - h), size = Size(size.width, h))
            }
            .border(1.dp, stroke, shape)
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ) { runCatching { localFocus.requestFocus() } }
            .padding(start = 16.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_bluebird_start_search),
            contentDescription = stringResource(R.string.search),
            modifier = Modifier.size(20.dp),
            contentScale = ContentScale.Fit
        )

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = TextStyle(
                color = textPrimary,
                fontSize = 15.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            ),
            cursorBrush = SolidColor(accent),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke() }),
            modifier = Modifier
                .weight(1f)
                .focusRequester(localFocus)
                .onFocusChanged { focused = it.isFocused },
            decorationBox = { inner ->
                Box(Modifier.fillMaxWidth().padding(vertical = 14.dp), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(placeholder, color = textPrimary.copy(alpha = 0.60f), fontSize = 15.sp, maxLines = 1)
                    }
                    inner()
                }
            }
        )

        if (query.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .clickable { onQueryChange("") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    FluentIcon.Dismiss,
                    contentDescription = stringResource(R.string.clear_search),
                    tint = textPrimary.copy(alpha = 0.70f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
