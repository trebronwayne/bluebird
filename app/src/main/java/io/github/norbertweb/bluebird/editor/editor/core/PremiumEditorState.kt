package io.github.norbertweb.bluebird.editor.editor.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import io.github.norbertweb.bluebird.editor.core.EditorSettings
import io.github.norbertweb.bluebird.editor.core.HistoryEntry
import io.github.norbertweb.bluebird.editor.core.LineIndex
import io.github.norbertweb.bluebird.editor.core.TabData
import io.github.norbertweb.bluebird.editor.core.detectLineEnding

class PremiumEditorState(initialPath: String = "", initialContent: String = "", savedSettings: EditorSettings = EditorSettings(), initialIsDark: Boolean = false) {
    var settings by mutableStateOf(savedSettings)
    var isDark by mutableStateOf(initialIsDark); private set
    var tabs by mutableStateOf(listOf(initialTab(initialPath, initialContent)))
    var activeTabIndex by mutableStateOf(0)
    var findQuery by mutableStateOf("")
    var replaceQuery by mutableStateOf("")
    var findVisible by mutableStateOf(false)
    var toastMsg by mutableStateOf<String?>(null)
    var showSettings by mutableStateOf(false)
    var showUnsavedDialog by mutableStateOf(false)
    var pendingCloseIndex by mutableStateOf<Int?>(null)

    val activeTab get() = tabs.getOrElse(activeTabIndex) { TabData() }
    val content get() = activeTab.content
    val currentLine get() = LineIndex(content.text).lineForOffset(content.selection.start)
    val currentColumn get() = content.selection.start - LineIndex(content.text).lineStart(currentLine) + 1

    fun setSystemTheme(dark: Boolean) { isDark = dark }
    fun updateSettings(block: EditorSettings.() -> EditorSettings) { settings = settings.block() }
    fun selectTab(index: Int) { if (index in tabs.indices) activeTabIndex = index }
    fun updateContent(value: TextFieldValue, recordUndo: Boolean = true) {
        if (value.text == content.text && value.selection == content.selection) return
        val old = content
        val tab = activeTab
        replaceActive(tab.copy(content = value, isModified = value.text != tab.lastSavedContent,
            undoStack = if (recordUndo && value.text != old.text) (tab.undoStack + HistoryEntry(old)).takeLast(100) else tab.undoStack,
            redoStack = if (recordUndo && value.text != old.text) emptyList() else tab.redoStack))
    }
    fun undo() { activeTab.undoStack.lastOrNull()?.let { replaceActive(activeTab.copy(content = it.value, isModified = it.value.text != activeTab.lastSavedContent, undoStack = activeTab.undoStack.dropLast(1), redoStack = activeTab.redoStack + HistoryEntry(content))) } }
    fun redo() { activeTab.redoStack.lastOrNull()?.let { replaceActive(activeTab.copy(content = it.value, isModified = it.value.text != activeTab.lastSavedContent, redoStack = activeTab.redoStack.dropLast(1), undoStack = activeTab.undoStack + HistoryEntry(content))) } }
    fun newTab(path: String = "", text: String = "") { tabs = tabs + initialTab(path, text); activeTabIndex = tabs.lastIndex }
    fun closeTab(index: Int = activeTabIndex): Boolean {
        if (index !in tabs.indices || tabs[index].isModified) return false
        if (tabs.size == 1) { tabs = listOf(TabData()); activeTabIndex = 0 } else { tabs = tabs.filterIndexed { i, _ -> i != index }; activeTabIndex = activeTabIndex.coerceAtMost(tabs.lastIndex) }
        return true
    }
    fun markSaved(path: String? = null, name: String? = null) { replaceActive(activeTab.copy(filePath = path ?: activeTab.filePath, fileName = name ?: activeTab.fileName, isSaved = true, isModified = false, lastSavedContent = content.text, lineEnding = detectLineEnding(content.text), undoStack = emptyList(), redoStack = emptyList())) }
    fun selectAll() { replaceActive(activeTab.copy(content = content.copy(selection = TextRange(0, content.text.length)))) }
    fun setSelection(start: Int, end: Int) { replaceActive(activeTab.copy(content = content.copy(selection = TextRange(start.coerceIn(0, content.text.length), end.coerceIn(0, content.text.length))))) }
    fun copySelection(): String = if (content.selection.collapsed) "" else content.text.substring(content.selection.min, content.selection.max)
    fun cutSelection(): String { val text = copySelection(); if (text.isNotEmpty()) updateContent(content.copy(text = content.text.removeRange(content.selection.min, content.selection.max), selection = TextRange(content.selection.min))); return text }
    fun paste(text: String) { val r = content.selection; val a = r.min; val b = r.max; updateContent(content.copy(text = content.text.substring(0, a) + text + content.text.substring(b), selection = TextRange(a + text.length))) }
    fun findNext() { if (findQuery.isNotBlank()) { val from = if (content.selection.collapsed) content.selection.start else content.selection.end; val i = content.text.indexOf(findQuery, from, true).let { if (it >= 0) it else content.text.indexOf(findQuery, 0, true) }; if (i >= 0) setSelection(i, i + findQuery.length) } }
    fun findPrevious() { if (findQuery.isNotBlank()) { val end = (content.selection.min - 1).coerceAtLeast(0); val i = content.text.substring(0, end + 1).lastIndexOf(findQuery, ignoreCase = true); if (i >= 0) setSelection(i, i + findQuery.length) } }
    fun replaceAll() { if (findQuery.isNotBlank()) { val result = content.text.replace(findQuery, replaceQuery); if (result != content.text) updateContent(content.copy(text = result, selection = TextRange(0))) } }
    private fun replaceActive(tab: TabData) { tabs = tabs.toMutableList().also { it[activeTabIndex] = tab } }
    companion object { private fun initialTab(path: String, text: String): TabData { val name = path.substringAfterLast('/').substringAfterLast('\\').ifBlank { "Untitled.txt" }; return TabData(filePath = path, fileName = name, content = TextFieldValue(text), isSaved = path.isNotBlank(), isModified = false, lastSavedContent = text, lineEnding = detectLineEnding(text)) } }
}
