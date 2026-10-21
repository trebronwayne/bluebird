package io.github.norbertweb.bluebird.browser.ui.panels

import androidx.compose.ui.res.stringResource
import io.github.norbertweb.bluebird.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.norbertweb.bluebird.browser.model.Bookmark
import io.github.norbertweb.bluebird.browser.model.BrowserPanel
import io.github.norbertweb.bluebird.browser.model.BrowserSettings
import io.github.norbertweb.bluebird.browser.model.DownloadItem
import io.github.norbertweb.bluebird.browser.model.DownloadStatus
import io.github.norbertweb.bluebird.browser.model.HistoryEntry
import io.github.norbertweb.bluebird.browser.model.SearchEngine
import io.github.norbertweb.bluebird.browser.ui.components.FluentIcon
import io.github.norbertweb.bluebird.browser.ui.components.FluentIcons

@Composable
fun BrowserPanelView(
    panel: BrowserPanel,
    bookmarks: List<Bookmark>,
    history: List<HistoryEntry>,
    downloads: List<DownloadItem>,
    settings: BrowserSettings,
    isDark: Boolean,
    onNavigate: (String) -> Unit,
    onBookmarkDelete: (Bookmark) -> Unit,
    onClearHistory: () -> Unit,
    onSettingsChange: (BrowserSettings) -> Unit,
    onDownloadOpen: (DownloadItem) -> Unit,
    onDownloadRemove: (DownloadItem) -> Unit,
    onClose: () -> Unit
) {
    val text = if (isDark) Color(0xFFF3F3F3) else Color(0xFF202020)
    val muted = text.copy(alpha = .58f)
    Surface(modifier = Modifier.fillMaxSize(), color = if (isDark) Color(0xFF202020) else Color(0xFFF7F7F7)) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    when (panel) {
                        BrowserPanel.BOOKMARKS -> stringResource(R.string.browser_bookmarks)
                        BrowserPanel.HISTORY -> stringResource(R.string.browser_history)
                        BrowserPanel.DOWNLOADS -> stringResource(R.string.browser_downloads)
                        BrowserPanel.SETTINGS -> stringResource(R.string.media_settings)
                        else -> ""
                    },
                    color = text,
                    fontSize = 14.sp
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onClose) {
                    FluentIcon(FluentIcons.Close, stringResource(R.string.close), tint = text, modifier = Modifier.size(18.dp))
                }
            }
            HorizontalDivider()
            when (panel) {
                BrowserPanel.BOOKMARKS -> {
                    if (bookmarks.isEmpty()) {
                        EmptyPanel(stringResource(R.string.no_bookmarks_yet), muted)
                    } else {
                        LazyColumn(Modifier.fillMaxSize()) {
                            items(bookmarks, key = { it.id }) { bookmark ->
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            bookmark.title.ifBlank { bookmark.url },
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    supportingContent = {
                                        Text(bookmark.url, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    },
                                    leadingContent = {
                                        FluentIcon(FluentIcons.Star, null, tint = text, modifier = Modifier.size(18.dp))
                                    },
                                    trailingContent = {
                                        IconButton(onClick = { onBookmarkDelete(bookmark) }) {
                                            FluentIcon(FluentIcons.Delete, stringResource(R.string.delete), tint = text, modifier = Modifier.size(18.dp))
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().clickable { onNavigate(bookmark.url) }
                                )
                            }
                        }
                    }
                }
                BrowserPanel.HISTORY -> {
                    if (history.isEmpty()) {
                        EmptyPanel(stringResource(R.string.no_history_yet), muted)
                    } else {
                        Column(Modifier.fillMaxSize()) {
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = onClearHistory) { Text(stringResource(R.string.clear_history)) }
                            }
                            LazyColumn(Modifier.fillMaxSize()) {
                                items(history.asReversed(), key = { it.id }) { entry ->
                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                entry.title.ifBlank { stringResource(R.string.web_page) },
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        supportingContent = {
                                            Text(entry.url, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        },
                                        leadingContent = {
                                            FluentIcon(FluentIcons.History, null, tint = text, modifier = Modifier.size(18.dp))
                                        },
                                        modifier = Modifier.fillMaxWidth().clickable { onNavigate(entry.url) }
                                    )
                                }
                            }
                        }
                    }
                }
                BrowserPanel.DOWNLOADS -> {
                    if (downloads.isEmpty()) {
                        EmptyPanel(stringResource(R.string.no_downloads_in_this_session), muted)
                    } else {
                        LazyColumn(Modifier.fillMaxSize()) {
                            items(downloads, key = { it.id }) { download ->
                                ListItem(
                                    headlineContent = {
                                        Text(download.fileName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    },
                                    supportingContent = {
                                        Column {
                                            Text(
                                                download.status.name.lowercase().replace('_', ' '),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (download.status == DownloadStatus.DOWNLOADING) {
                                                Spacer(Modifier.height(4.dp))
                                                LinearProgressIndicator(
                                                    progress = { download.progress.coerceIn(0f, 1f) },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    },
                                    leadingContent = {
                                        FluentIcon(
                                            if (download.status == DownloadStatus.COMPLETED) FluentIcons.CheckCircle else FluentIcons.Download,
                                            null,
                                            tint = text,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    trailingContent = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (download.status == DownloadStatus.COMPLETED) {
                                                IconButton(onClick = { onDownloadOpen(download) }) {
                                                    FluentIcon(FluentIcons.Delete, stringResource(R.string.open), tint = text, modifier = Modifier.size(18.dp))
                                               // temporary icon needs to be taken back to fluentiocns documents
                                                }
                                            }
                                            IconButton(onClick = { onDownloadRemove(download) }) {
                                                FluentIcon(FluentIcons.Delete, stringResource(R.string.remove), tint = text, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                BrowserPanel.SETTINGS -> SettingsContent(settings, text, muted, onSettingsChange)
                else -> Unit
            }
        }
    }
}

@Composable
private fun EmptyPanel(message: String, muted: Color) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(message, color = muted, fontSize = 12.sp)
    }
}

@Composable
private fun SettingsContent(
    settings: BrowserSettings,
    text: Color,
    muted: Color,
    onChange: (BrowserSettings) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.search_engine), color = text, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        SearchEngine.values().forEach { engine ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = settings.searchEngine == engine,
                    onClick = { onChange(settings.copy(searchEngine = engine)) }
                )
                Text(engine.label, color = text, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(stringResource(R.string.browser), color = text, fontSize = 12.sp)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.show_bookmarks_bar), color = text, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Switch(
                checked = settings.showBookmarksBar,
                onCheckedChange = { onChange(settings.copy(showBookmarksBar = it)) }
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.bluebird_browser_stays_intentionally),
            color = muted,
            fontSize = 10.sp
        )
    }
}
