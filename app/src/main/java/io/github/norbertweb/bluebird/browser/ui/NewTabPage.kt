package io.github.norbertweb.bluebird.browser.ui

import androidx.compose.ui.res.stringResource
import io.github.norbertweb.bluebird.R
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.norbertweb.bluebird.browser.ui.components.FluentIcon
import io.github.norbertweb.bluebird.browser.ui.components.FluentIcons

/** Lightweight offline new-tab page. No RSS, remote wallpaper, or background network fetches. */
@Composable
fun NewTabPage(
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val text = if (isDark) Color(0xFFF3F3F3) else Color(0xFF202020)
    val muted = text.copy(alpha = .58f)
    Column(modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        FluentIcon(FluentIcons.Language, stringResource(R.string.browser), tint = text, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(14.dp))
        Text(stringResource(R.string.bluebird_browser), color = text, fontSize = 22.sp)
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.a_simple_browser_built_into_bluebird), color = muted, fontSize = 13.sp)
    }
}
