package io.github.norbertweb.bluebird.browser.ui

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.norbertweb.bluebird.R
import io.github.norbertweb.bluebird.browser.data.BrowserRepository
import io.github.norbertweb.bluebird.browser.model.*
import io.github.norbertweb.bluebird.browser.ui.components.FluentIcon
import io.github.norbertweb.bluebird.browser.ui.components.FluentIcons
import io.github.norbertweb.bluebird.browser.ui.components.BrowserTextField
import io.github.norbertweb.bluebird.browser.ui.components.NavBtn
import io.github.norbertweb.bluebird.browser.ui.panels.BrowserPanelView
import io.github.norbertweb.bluebird.browser.ui.webview.BrowserWebView
import io.github.norbertweb.bluebird.browser.utils.DownloadHelper
import io.github.norbertweb.bluebird.browser.utils.UrlUtils

/**
 * Lightweight browser for Bluebird Launcher.
 * It intentionally provides browsing, tabs, bookmarks, history and downloads only.
 */
@Composable
fun BrowserScreen(isDark1: Boolean) {
    val context = LocalContext.current
    val repo = remember { BrowserRepository(context.applicationContext) }
    val downloader = remember { DownloadHelper(context.applicationContext) }
    val initialTabs = remember { repo.loadTabs() }
    var settings by remember { mutableStateOf(repo.loadSettings()) }
    var tabs by remember { mutableStateOf(initialTabs.first.toMutableList()) }
    var activeId by remember { mutableStateOf(initialTabs.second) }
    var bookmarks by remember { mutableStateOf(repo.loadBookmarks()) }
    var history by remember { mutableStateOf(repo.loadHistory()) }
    var downloads by remember { mutableStateOf(emptyList<DownloadItem>()) }
    var panel by remember { mutableStateOf(BrowserPanel.NONE) }
    var address by remember { mutableStateOf("") }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }

    if (tabs.isEmpty()) tabs = mutableStateListOf(BrowserTab()).toMutableList()
    val activeIndex = tabs.indexOfFirst { it.id == activeId }.let { if (it >= 0) it else 0 }
    val activeTab = tabs[activeIndex]
    val isDark = isDark1
    val surface = if (isDark) Color(0xFF1F1F1F) else Color(0xFFF7F7F7)
    val bar = if (isDark) Color(0xFF292929) else Color(0xFFFFFFFF)
    val text = if (isDark) Color(0xFFF3F3F3) else Color(0xFF202020)
    val muted = text.copy(.58f)

    fun persist(activeTabId: String = activeTab.id) {
        repo.saveTabs(tabs, activeTabId)
        repo.saveBookmarks(bookmarks)
        repo.saveHistory(history)
        repo.saveSettings(settings)
    }
    fun navigate(raw: String) {
        val resolved = UrlUtils.resolveUrl(raw, settings.searchEngine)
        if (resolved == NEWTAB_URL) return
        tabs[activeIndex].apply { url = resolved; displayUrl = UrlUtils.displayUrl(resolved); lastVisited = System.currentTimeMillis() }
        tabs = tabs.toMutableList()
        address = UrlUtils.displayUrl(resolved)
        errorMessage = null
        persist()
    }
    fun newTab() {
        if (tabs.size >= MAX_TABS) return
        val tab = BrowserTab()
        tabs = (tabs + tab).toMutableList()
        activeId = tab.id
        address = ""
        panel = BrowserPanel.NONE
        persist(tab.id)
    }
    fun closeTab(tab: BrowserTab) {
        if (tabs.size == 1) {
            val replacement = BrowserTab()
            tabs = mutableListOf(replacement)
            activeId = replacement.id
            address = ""
        } else {
            tabs = tabs.filterNot { it.id == tab.id }.toMutableList()
            if (activeId == tab.id) {
                val replacement = tabs.last()
                activeId = replacement.id
                address = UrlUtils.displayUrl(replacement.url)
            }
        }
        persist(activeId)
    }
    fun toggleBookmark() {
        if (!UrlUtils.isWebUrl(activeTab.url)) return
        val existing = bookmarks.firstOrNull { it.url == activeTab.url }
        bookmarks = if (existing != null) bookmarks.filterNot { it.id == existing.id } else bookmarks + Bookmark(title=activeTab.title, url=activeTab.url, faviconUrl=activeTab.faviconUrl, faviconColor=activeTab.faviconColor)
        persist()
    }

    DisposableEffect(Unit) { onDispose { downloader.destroy(); webView?.destroy() } }
    LaunchedEffect(activeTab.url) { address = UrlUtils.displayUrl(activeTab.url) }

    Column(Modifier.fillMaxSize().background(surface)) {
        LazyRow(Modifier.fillMaxWidth().height(38.dp).background(bar).padding(horizontal=4.dp), verticalAlignment=Alignment.CenterVertically) {
            items(tabs, key={it.id}) { tab ->
                Surface(color=if(tab.id==activeTab.id) surface else Color.Transparent, shape=RoundedCornerShape(7.dp), modifier=Modifier.padding(2.dp).clickable { activeId=tab.id; panel=BrowserPanel.NONE }) {
                    Row(Modifier.widthIn(min=90.dp,max=170.dp).height(30.dp).padding(horizontal=8.dp), verticalAlignment=Alignment.CenterVertically) {
                        Text(tab.title.ifBlank { stringResource(R.string.new_tab_2) }, color=text, fontSize=10.sp, maxLines=1, overflow=TextOverflow.Ellipsis, modifier=Modifier.weight(1f))
                        IconButton(onClick={closeTab(tab)}, modifier=Modifier.size(22.dp)) { FluentIcon(FluentIcons.Close,stringResource(R.string.close_tab),tint=muted,modifier=Modifier.size(13.dp)) }
                    }
                }
            }
            item { IconButton(onClick={::newTab}, modifier=Modifier.size(30.dp)) { FluentIcon(FluentIcons.Add,stringResource(R.string.new_tab),tint=text,modifier=Modifier.size(17.dp)) } }
        }

        Row(Modifier.fillMaxWidth().height(48.dp).background(bar).padding(horizontal=7.dp), verticalAlignment=Alignment.CenterVertically) {
            NavBtn(FluentIcons.ArrowBack, enabled=webView?.canGoBack()==true, tint=text, contentDescription=stringResource(R.string.setup_back)) { webView?.goBack() }
            NavBtn(FluentIcons.ArrowForward, enabled=webView?.canGoForward()==true, tint=text, contentDescription=stringResource(R.string.forward)) { webView?.goForward() }
            NavBtn(if(activeTab.isLoading) FluentIcons.Close else FluentIcons.Refresh, tint=text, contentDescription=stringResource(R.string.reload)) { if(activeTab.isLoading) webView?.stopLoading() else webView?.reload() }
            BrowserTextField(value=address, onValueChange={address=it}, placeholder=stringResource(R.string.search_or_enter_web_address), textColor=text, modifier=Modifier.weight(1f).padding(horizontal=7.dp), onFocusRequest={}, onImeGo={navigate(address)}, fontSize=12.sp)
            IconButton(onClick={::toggleBookmark}, modifier=Modifier.size(30.dp)) { FluentIcon(if(bookmarks.any{it.url==activeTab.url}) FluentIcons.Star else FluentIcons.StarBorder,stringResource(R.string.bookmark),tint=text,modifier=Modifier.size(17.dp)) }
            IconButton(onClick={::newTab}, modifier=Modifier.size(30.dp)) { FluentIcon(FluentIcons.Add,stringResource(R.string.new_tab),tint=text,modifier=Modifier.size(17.dp)) }
            Box {
                IconButton(onClick={ menuExpanded=true }, modifier=Modifier.size(30.dp)) { FluentIcon(FluentIcons.MoreVert,stringResource(R.string.more),tint=text,modifier=Modifier.size(17.dp)) }
                DropdownMenu(expanded=menuExpanded, onDismissRequest={menuExpanded=false}) {
                    DropdownMenuItem(text={Text(stringResource(R.string.browser_bookmarks))}, leadingIcon={FluentIcon(FluentIcons.Star,null,tint=text,modifier=Modifier.size(18.dp))}, onClick={menuExpanded=false; panel=BrowserPanel.BOOKMARKS})
                    DropdownMenuItem(text={Text(stringResource(R.string.browser_history))}, leadingIcon={FluentIcon(FluentIcons.History,null,tint=text,modifier=Modifier.size(18.dp))}, onClick={menuExpanded=false; panel=BrowserPanel.HISTORY})
                    DropdownMenuItem(text={Text(stringResource(R.string.browser_downloads))}, leadingIcon={FluentIcon(FluentIcons.Download,null,tint=text,modifier=Modifier.size(18.dp))}, onClick={menuExpanded=false; panel=BrowserPanel.DOWNLOADS})
                    DropdownMenuItem(text={Text(stringResource(R.string.browser_settings))}, leadingIcon={FluentIcon(FluentIcons.Settings,null,tint=text,modifier=Modifier.size(18.dp))}, onClick={menuExpanded=false; panel=BrowserPanel.SETTINGS})
                }
            }
        }

        if (settings.showBookmarksBar && bookmarks.isNotEmpty()) {
            LazyRow(Modifier.fillMaxWidth().height(32.dp).background(bar).padding(horizontal=8.dp), verticalAlignment=Alignment.CenterVertically) {
                items(bookmarks.take(20), key={it.id}) { b -> TextButton(onClick={navigate(b.url)}, contentPadding=PaddingValues(horizontal=8.dp,vertical=0.dp)) { Text(b.title.ifBlank { b.url }, color=text, fontSize=10.sp, maxLines=1, overflow=TextOverflow.Ellipsis) } }
            }
        }

        Box(Modifier.fillMaxSize()) {
            if (activeTab.url == NEWTAB_URL || activeTab.url == BLANK_URL) {
                NewTabPage(isDark = isDark, modifier = Modifier.fillMaxSize())
            } else {
                BrowserWebView(
                    url=activeTab.url, tab=activeTab, modifier=Modifier.fillMaxSize(),
                    onWebViewReady={ webView=it },
                    onPageStarted={ url -> tabs[activeIndex].isLoading=true; tabs[activeIndex].url=url; tabs=tabs.toMutableList() },
                    onProgressChanged={ p -> tabs[activeIndex].loadProgress=p/100f; tabs=tabs.toMutableList() },
                    onPageFinished={ url,title ->
                        tabs[activeIndex].apply { isLoading=false; loadProgress=1f; this.url=url; this.displayUrl=UrlUtils.displayUrl(url); this.title=title ?: UrlUtils.host(url).orEmpty().ifBlank { "Web page" }; lastVisited=System.currentTimeMillis() }
                        tabs=tabs.toMutableList(); address=UrlUtils.displayUrl(url)
                        if(UrlUtils.isWebUrl(url)) {
                            val entry = HistoryEntry(title=tabs[activeIndex].title, url=url, faviconUrl=tabs[activeIndex].faviconUrl, faviconColor=tabs[activeIndex].faviconColor)
                            history = (history.filterNot { it.url == url } + entry).takeLast(MAX_HISTORY_ENTRIES)
                            repo.saveHistory(history)
                            repo.saveTabs(tabs, activeTab.id)
                        }
                    },
                    onPageError={ _, message -> tabs[activeIndex].isLoading=false; tabs=tabs.toMutableList(); errorMessage=message },
                    onTitleChanged={ title -> tabs[activeIndex].title=title.ifBlank { "Web page" }; tabs=tabs.toMutableList() },
                    onUrlChanged={ url -> if(UrlUtils.isWebUrl(url)) address=UrlUtils.displayUrl(url) },
                    onFaviconChanged={ icon -> if(icon!=null){ tabs[activeIndex].faviconUrl=UrlUtils.faviconUrl(activeTab.url); tabs[activeIndex].faviconColor=UrlUtils.colorForUrl(activeTab.url); tabs=tabs.toMutableList() } },
                    onDownloadStart={ url,ua,cd,mime,len ->
                        val item=downloader.enqueue(url,ua,cd,mime,len,{progress,bytes -> downloads=downloads.map{if(it.url==url) it.copy(progress=progress,bytesDownloaded=bytes) else it}}, {status -> downloads=downloads.map{if(it.url==url) it.copy(status=status,progress=if(status==DownloadStatus.COMPLETED)1f else it.progress) else it}})
                        downloads=(downloads+item).takeLast(MAX_DOWNLOAD_ENTRIES)
                    }
                )
            }
            errorMessage?.let { msg ->
                Surface(Modifier.align(Alignment.Center).padding(24.dp), color=bar, shape=RoundedCornerShape(10.dp), tonalElevation=4.dp) { Column(Modifier.padding(18.dp), horizontalAlignment=Alignment.CenterHorizontally) { FluentIcon(FluentIcons.Warning,"Error",tint=text,modifier=Modifier.size(28.dp)); Spacer(Modifier.height(8.dp)); Text(msg,color=text,fontSize=12.sp); TextButton(onClick={errorMessage=null}){Text(stringResource(R.string.common_dismiss))} } }
            }
            if(panel!=BrowserPanel.NONE) {
                Surface(Modifier.align(Alignment.CenterEnd).fillMaxHeight().widthIn(max=330.dp), color=surface, shadowElevation=8.dp) {
                    BrowserPanelView(
                        panel = panel,
                        bookmarks = bookmarks,
                        history = history,
                        downloads = downloads,
                        settings = settings,
                        isDark = isDark,
                        onNavigate = { url -> panel = BrowserPanel.NONE; navigate(url) },
                        onBookmarkDelete = { b -> bookmarks = bookmarks.filterNot { it.id == b.id }; persist() },
                        onClearHistory = { history = emptyList(); repo.clearHistory() },
                        onSettingsChange = { s -> settings = s; repo.saveSettings(s) },
                        onDownloadOpen = { item -> downloader.open(item.downloadManagerId) },
                        onDownloadRemove = { item ->
                            downloader.remove(item.downloadManagerId)
                            downloads = downloads.filterNot { it.id == item.id }
                        },
                        onClose = { panel = BrowserPanel.NONE }
                    )
                }
            }
        }
    }
}
