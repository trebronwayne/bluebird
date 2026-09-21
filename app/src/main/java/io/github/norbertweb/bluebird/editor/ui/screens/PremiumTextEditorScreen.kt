package io.github.norbertweb.bluebird.editor.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontFamily
import java.io.File
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import io.github.norbertweb.bluebird.editor.core.EditorSettings
import io.github.norbertweb.bluebird.editor.editor.core.PremiumEditorState
import io.github.norbertweb.bluebird.editor.ui.components.FindBar
import io.github.norbertweb.bluebird.editor.ui.components.NotepadMenuBar
import io.github.norbertweb.bluebird.editor.ui.components.NotepadStatusBar
import io.github.norbertweb.bluebird.editor.ui.components.NotepadTabBar
import io.github.norbertweb.bluebird.editor.ui.components.SettingsDialog
import io.github.norbertweb.bluebird.editor.ui.components.UnsavedChangesDialog
import io.github.norbertweb.bluebird.editor.ui.theme.EdThemes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PremiumTextEditorScreen(
    @Suppress("UNUSED_PARAMETER") isDark: Boolean = false,
    filePath: String = "",
    initialContent: String = "",
    savedSettings: EditorSettings = EditorSettings(),
    onBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val systemDark = isSystemInDarkTheme()
    val state = remember(filePath) { PremiumEditorState(filePath, initialContent, savedSettings, systemDark) }
    LaunchedEffect(systemDark) { state.setSystemTheme(systemDark) }

    // This screen used to just display whatever `initialContent` it was handed and never
    // actually read `filePath` itself. If the caller (file explorer / desktop "open") only
    // passes a path — no content — the editor opens with the right title but an empty body.
    // Load the file ourselves whenever we have a path but no content to show for it.
    LaunchedEffect(filePath) {
        if (filePath.isNotBlank() && initialContent.isBlank()) {
            val text = withContext(Dispatchers.IO) {
                runCatching {
                    if (filePath.startsWith("content://") || filePath.startsWith("file://")) {
                        context.contentResolver.openInputStream(android.net.Uri.parse(filePath))
                            ?.bufferedReader()?.use { it.readText() }
                    } else {
                        File(filePath).readText()
                    }
                }.getOrNull()
            }
            if (text != null) {
                state.updateContent(TextFieldValue(text, TextRange(0)), recordUndo = false)
                state.markSaved(filePath, filePath.substringAfterLast('/').substringAfterLast('\\'))
            } else {
                state.toastMsg = "Couldn't open the file"
            }
        }
    }
    val c = EdThemes.system(state.isDark)
    val scroll = rememberScrollState()

    val saveAsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val ok = writeUri(context, uri.toString(), state.content.text)
            if (ok) {
                state.markSaved(uri.toString(), uri.lastPathSegment?.substringAfterLast('/') ?: state.activeTab.fileName)
                state.pendingCloseIndex?.let { index -> state.pendingCloseIndex = null; state.closeTab(index) }
                state.showUnsavedDialog = false
            } else state.toastMsg = "Couldn't save the file"
        }
    }
    val openLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: "" }
            }
            result.onSuccess { text ->
                val name = uri.lastPathSegment?.substringAfterLast('/')?.substringAfterLast(':').orEmpty().ifBlank { "Untitled.txt" }
                state.newTab(uri.toString(), text)
                state.markSaved(uri.toString(), name)
            }.onFailure { state.toastMsg = "Couldn't open the file" }
        }
    }

    fun save() {
        val path = state.activeTab.filePath
        if (path.isBlank()) {
            saveAsLauncher.launch(state.activeTab.fileName)
            return
        }
        scope.launch {
            val ok = withContext(Dispatchers.IO) { writeUriOrFile(context, path, state.content.text) }
            if (ok) {
                state.markSaved()
                state.pendingCloseIndex?.let { index -> state.pendingCloseIndex = null; state.closeTab(index) }
                state.showUnsavedDialog = false
            } else state.toastMsg = "Couldn't save the file"
        }
    }
    fun closeTab(index: Int = state.activeTabIndex) {
        if (state.closeTab(index)) return
        state.pendingCloseIndex = index
        state.showUnsavedDialog = true
    }
    fun discardAndClose() {
        val index = state.pendingCloseIndex ?: return
        val tabs = state.tabs.toMutableList()
        tabs.removeAt(index)
        if (tabs.isEmpty()) state.newTab() else {
            state.tabs = tabs
            state.activeTabIndex = state.activeTabIndex.coerceAtMost(tabs.lastIndex)
        }
        state.pendingCloseIndex = null
        state.showUnsavedDialog = false
    }
    fun confirmSaveAndClose() {
        val index = state.pendingCloseIndex ?: return
        if (index != state.activeTabIndex) { state.selectTab(index) }
        state.showUnsavedDialog = false
        state.pendingCloseIndex = null
        save()
    }

    BackHandler {
        if (state.findVisible) state.findVisible = false
        else if (state.activeTab.isModified) { state.pendingCloseIndex = state.activeTabIndex; state.showUnsavedDialog = true }
        else onBack()
    }

    Column(Modifier.fillMaxSize().background(c.window)) {
        NotepadTabBar(state, c, onNew = { state.newTab() }, onClose = ::closeTab)
        NotepadMenuBar(
            s = state, c = c,
            onNew = { state.newTab() },
            onOpen = { openLauncher.launch(arrayOf("text/*", "application/json", "application/octet-stream")) },
            onSave = ::save,
            onSaveAs = { saveAsLauncher.launch(state.activeTab.fileName) },
            onClose = { closeTab() },
            onExit = { if (state.activeTab.isModified) { state.pendingCloseIndex = state.activeTabIndex; state.showUnsavedDialog = true } else onBack() },
            onCopy = { clipboard.setText(AnnotatedString(state.copySelection())) },
            onCut = { val text = state.cutSelection(); if (text.isNotEmpty()) clipboard.setText(AnnotatedString(text)) },
            onPaste = { clipboard.getText()?.text?.let(state::paste) },
        )
        if (state.findVisible) FindBar(state, c) { state.findVisible = false }

        Box(
            Modifier.weight(1f).fillMaxWidth().background(c.editor).verticalScroll(scroll)
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            BasicTextField(
                value = state.content,
                onValueChange = state::updateContent,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = c.text, fontSize = state.settings.fontSize.sp, fontFamily = FontFamily.SansSerif, lineHeight = (state.settings.fontSize * 1.55f).sp),
                cursorBrush = SolidColor(c.accent),
                singleLine = false,
                maxLines = Int.MAX_VALUE,
                decorationBox = { inner ->
                    Box(Modifier.fillMaxWidth().border(0.dp, c.editor, RoundedCornerShape(0.dp))) { inner() }
                }
            )
        }
        NotepadStatusBar(state, c)
    }

    SettingsDialog(state, c)
    UnsavedChangesDialog(state, onSave = { confirmSaveAndClose() }, onDiscard = ::discardAndClose, onCancel = { state.showUnsavedDialog = false })
}

private suspend fun writeUri(context: Context, uriString: String, text: String): Boolean = withContext(Dispatchers.IO) {
    runCatching {
        context.contentResolver.openOutputStream(android.net.Uri.parse(uriString), "wt")?.bufferedWriter()?.use { it.write(text) }
            ?: throw IllegalStateException("No output stream")
        true
    }.getOrDefault(false)
}

private suspend fun writeUriOrFile(context: Context, path: String, text: String): Boolean = withContext(Dispatchers.IO) {
    runCatching {
        if (path.startsWith("content://")) {
            context.contentResolver.openOutputStream(android.net.Uri.parse(path), "wt")?.bufferedWriter()?.use { it.write(text) }
                ?: throw IllegalStateException("No output stream")
        } else File(path).writeText(text)
        true
    }.getOrDefault(false)
}
