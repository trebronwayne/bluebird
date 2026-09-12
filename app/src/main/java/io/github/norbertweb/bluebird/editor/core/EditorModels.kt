package io.github.norbertweb.bluebird.editor.core

import androidx.compose.ui.text.input.TextFieldValue
import java.nio.charset.Charset
import java.util.UUID

enum class FileEncoding(val label: String, val charset: Charset) {
    UTF8("UTF-8", Charsets.UTF_8)
}

enum class LineEnding(val label: String, val chars: String) {
    LF("Unix (LF)", "\n"),
    CRLF("Windows (CRLF)", "\r\n")
}

enum class EditorTheme(val label: String) { SYSTEM("System") }

enum class IndentStyle(val label: String) {
    SPACES_2("2 Spaces"), SPACES_4("4 Spaces"), TAB("Tab")
}

data class CursorPosition(val line: Int, val col: Int, val offset: Int)

data class HistoryEntry(val value: TextFieldValue, val timestamp: Long = System.currentTimeMillis())

data class TabData(
    val id: String = UUID.randomUUID().toString(),
    val filePath: String = "",
    val fileName: String = "Untitled.txt",
    val content: TextFieldValue = TextFieldValue(""),
    val isSaved: Boolean = false,
    val isModified: Boolean = false,
    val encoding: FileEncoding = FileEncoding.UTF8,
    val lineEnding: LineEnding = LineEnding.LF,
    val lastSavedContent: String = "",
    val undoStack: List<HistoryEntry> = emptyList(),
    val redoStack: List<HistoryEntry> = emptyList(),
)

data class FindResult(
    val range: IntRange,
    val lineNumber: Int,
    val lineText: String,
    val matchText: String,
)

data class EditorSettings(
    val theme: EditorTheme = EditorTheme.SYSTEM,
    val fontSize: Float = 14f,
    val wordWrap: Boolean = true,
    val showStatusBar: Boolean = true,
)

fun detectLineEnding(text: String): LineEnding =
    if (text.contains("\r\n")) LineEnding.CRLF else LineEnding.LF

class LineIndex(private val text: String) {
    fun lineForOffset(offset: Int): Int = text.take(offset.coerceIn(0, text.length)).count { it == '\n' } + 1
    fun lineStart(line: Int): Int {
        if (line <= 1) return 0
        var current = 1
        var index = 0
        while (current < line) {
            val next = text.indexOf('\n', index)
            if (next < 0) return text.length
            index = next + 1
            current++
        }
        return index
    }
    fun lineEnd(line: Int): Int {
        val start = lineStart(line)
        val end = text.indexOf('\n', start)
        return if (end < 0) text.length else end
    }
    val lineCount: Int get() = text.count { it == '\n' } + 1
}
