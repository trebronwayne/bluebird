package io.github.norbertweb.bluebird.editor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.norbertweb.bluebird.editor.editor.core.PremiumEditorState
import io.github.norbertweb.bluebird.editor.ui.theme.EditorColors
import io.github.norbertweb.bluebird.ui.components.FluentIcon

@Composable
fun NotepadTabBar(s: PremiumEditorState, c: EditorColors, onNew: () -> Unit, onClose: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(44.dp).background(c.chrome),
        verticalAlignment = Alignment.Bottom
    ) {
        Row(Modifier.weight(1f).horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.Bottom) {
            s.tabs.forEachIndexed { index, tab ->
                val active = index == s.activeTabIndex
                Row(
                    Modifier.widthIn(min = 132.dp, max = 230.dp).height(40.dp)
                        .background(if (active) c.activeTab else c.chrome, RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp))
                        .clickable { s.selectTab(index) }
                        .padding(start = 12.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(FluentIcon.Document, null, tint = c.secondaryText, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(9.dp))
                    Text(
                        text = tab.fileName + if (tab.isModified) " •" else "",
                        color = c.text,
                        fontSize = 13.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(FluentIcon.Close, "Close", tint = c.secondaryText, modifier = Modifier.size(16.dp).clickable { onClose(index) }.padding(2.dp))
                }
            }
        }
        Icon(FluentIcon.Add, "New tab", tint = c.text, modifier = Modifier.size(40.dp).padding(11.dp).clickable { onNew() })
    }
}

@Composable
fun NotepadMenuBar(
    s: PremiumEditorState,
    c: EditorColors,
    onNew: () -> Unit,
    onOpen: () -> Unit,
    onSave: () -> Unit,
    onSaveAs: () -> Unit,
    onClose: () -> Unit,
    onExit: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onPaste: () -> Unit,
) {
    Row(Modifier.fillMaxWidth().height(42.dp).background(c.chrome), verticalAlignment = Alignment.CenterVertically) {
        Menu("File", c) {
            MenuItem("New tab", FluentIcon.Add, onNew)
            MenuItem("Open", FluentIcon.FolderOpen, onOpen)
            MenuItem("Save", FluentIcon.Save, onSave)
            MenuItem("Save as", FluentIcon.SaveAs, onSaveAs)
            MenuItem("Close tab", FluentIcon.Close, onClose)
            MenuItem("Exit", null, onExit)
        }
        Menu("Edit", c) {
            MenuItem("Undo", FluentIcon.Undo, s::undo)
            MenuItem("Redo", FluentIcon.Redo, s::redo)
            MenuItem("Cut", FluentIcon.ContentCut, onCut)
            MenuItem("Copy", FluentIcon.ContentCopy, onCopy)
            MenuItem("Paste", FluentIcon.ContentPaste, onPaste)
            MenuItem("Find", FluentIcon.Search) { s.findVisible = true }
            MenuItem("Replace", FluentIcon.FindReplace) { s.findVisible = true }
            MenuItem("Select all", FluentIcon.SelectAll, s::selectAll)
        }
        Menu("View", c) {
            MenuCheckItem("Word wrap", s.settings.wordWrap, c) { s.updateSettings { copy(wordWrap = !wordWrap) } }
            MenuCheckItem("Status bar", s.settings.showStatusBar, c) { s.updateSettings { copy(showStatusBar = !showStatusBar) } }
            MenuItem("Zoom in", FluentIcon.ZoomIn) { s.updateSettings { copy(fontSize = (fontSize + 1f).coerceAtMost(32f)) } }
            MenuItem("Zoom out", FluentIcon.ZoomOut) { s.updateSettings { copy(fontSize = (fontSize - 1f).coerceAtLeast(9f)) } }
            MenuItem("Reset zoom", FluentIcon.Restore) { s.updateSettings { copy(fontSize = 14f) } }
        }
        Spacer(Modifier.weight(1f))
        Icon(FluentIcon.Settings, "Settings", tint = c.text, modifier = Modifier.size(40.dp).padding(11.dp).clickable { s.showSettings = true })
    }
}

@Composable
private fun Menu(label: String, c: EditorColors, content: @Composable () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Text(label, color = c.text, fontSize = 13.sp, modifier = Modifier.clickable { expanded = true }.padding(horizontal = 13.dp, vertical = 11.dp))
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            MenuSurface(c) { content() }
        }
    }
}

@Composable
private fun MenuSurface(c: EditorColors, content: @Composable () -> Unit) {
    Column(Modifier.widthIn(min = 190.dp).background(c.chrome)) { content() }
}

@Composable
private fun MenuItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector?, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label, fontSize = 13.sp) },
        onClick = onClick,
        leadingIcon = icon?.let { { Icon(it, null, Modifier.size(17.dp)) } }
    )
}

@Composable
private fun MenuCheckItem(label: String, checked: Boolean, c: EditorColors, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(if (checked) "✓  $label" else "    $label", fontSize = 13.sp, color = c.text) },
        onClick = onClick
    )
}

@Composable
fun FindBar(s: PremiumEditorState, c: EditorColors, onDismiss: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(c.chrome).border(1.dp, c.border).padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.OutlinedTextField(
            value = s.findQuery,
            onValueChange = { s.findQuery = it },
            singleLine = true,
            placeholder = { Text("Find") },
            modifier = Modifier.widthIn(min = 170.dp, max = 300.dp).weight(1f)
        )
        Spacer(Modifier.width(6.dp))
        androidx.compose.material3.OutlinedTextField(
            value = s.replaceQuery,
            onValueChange = { s.replaceQuery = it },
            singleLine = true,
            placeholder = { Text("Replace") },
            modifier = Modifier.widthIn(min = 150.dp, max = 260.dp).weight(1f)
        )
        Icon(FluentIcon.NavigateBefore, "Previous", tint = c.text, modifier = Modifier.size(36.dp).padding(8.dp).clickable { s.findPrevious() })
        Icon(FluentIcon.NavigateNext, "Next", tint = c.text, modifier = Modifier.size(36.dp).padding(8.dp).clickable { s.findNext() })
        TextButton(onClick = s::replaceAll) { Text("Replace all", fontSize = 12.sp) }
        Icon(FluentIcon.Close, "Close", tint = c.text, modifier = Modifier.size(36.dp).padding(9.dp).clickable(onClick = onDismiss))
    }
}

@Composable
fun NotepadStatusBar(s: PremiumEditorState, c: EditorColors) {
    if (!s.settings.showStatusBar) return
    Row(Modifier.fillMaxWidth().height(32.dp).background(c.status).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Ln ${s.currentLine}, Col ${s.currentColumn}", color = c.secondaryText, fontSize = 11.sp)
        Spacer(Modifier.weight(1f))
        Text("${s.settings.fontSize.toInt()}%", color = c.secondaryText, fontSize = 11.sp)
        Spacer(Modifier.width(18.dp))
        Text("${s.activeTab.lineEnding.label}", color = c.secondaryText, fontSize = 11.sp)
        Spacer(Modifier.width(18.dp))
        Text(s.activeTab.encoding.label, color = c.secondaryText, fontSize = 11.sp)
    }
}

@Composable
fun SettingsDialog(s: PremiumEditorState, c: EditorColors) {
    if (!s.showSettings) return
    AlertDialog(
        onDismissRequest = { s.showSettings = false },
        title = { Text("Notepad settings", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Font size", fontSize = 13.sp)
                Slider(value = s.settings.fontSize, onValueChange = { s.updateSettings { copy(fontSize = it) } }, valueRange = 9f..32f, steps = 22)
                Text("${s.settings.fontSize.toInt()} sp", fontSize = 12.sp)
                TextButton(onClick = { s.updateSettings { copy(wordWrap = !wordWrap) } }) { Text("Word wrap: ${if (s.settings.wordWrap) "On" else "Off"}") }
                TextButton(onClick = { s.updateSettings { copy(showStatusBar = !showStatusBar) } }) { Text("Status bar: ${if (s.settings.showStatusBar) "On" else "Off"}") }
            }
        },
        confirmButton = { TextButton(onClick = { s.showSettings = false }) { Text("Done") } }
    )
}

@Composable
fun UnsavedChangesDialog(s: PremiumEditorState, onSave: () -> Unit, onDiscard: () -> Unit, onCancel: () -> Unit) {
    if (!s.showUnsavedDialog) return
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Save changes?") },
        text = { Text("Your changes to ${s.activeTab.fileName} haven't been saved.") },
        confirmButton = { TextButton(onClick = onSave) { Text("Save") } },
        dismissButton = { Row { TextButton(onClick = onDiscard) { Text("Don't save") }; TextButton(onClick = onCancel) { Text("Cancel") } } }
    )
}
