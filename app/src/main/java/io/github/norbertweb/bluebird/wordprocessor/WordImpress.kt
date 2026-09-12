package io.github.norbertweb.bluebird.wordprocessor

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.norbertweb.bluebird.ui.theme.LocalIsDarkTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class WordMenu { FILE, EDIT, VIEW }

@Composable
fun PhoneScreen(isDark: Boolean, initialPath: String = "") {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val palette = rememberWordFluentPalette()
    val snackbar = remember { SnackbarHostState() }
    val settings = remember { AppSettings() }
    val documents = remember { mutableStateListOf(WordDocument("Document1", settings.defaultPageSettings)) }
    var currentIndex by remember { mutableIntStateOf(0) }
    val doc = documents[currentIndex.coerceIn(0, documents.lastIndex)]
    val editor = remember(doc.id) { SimpleEditorController() }
    var menu by remember { mutableStateOf<WordMenu?>(null) }
    var showFind by remember { mutableStateOf(false) }
    var findQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }
    var showStyleMenu by remember { mutableStateOf(false) }
    var zoom by remember { mutableStateOf(1f) }
    var pdfUri by remember { mutableStateOf<Uri?>(null) }

    fun notify(message: String) {
        scope.launch {
            snackbar.showSnackbar(message)
        }
    }

    fun isPdfUri(uri: Uri): Boolean {
        val mime = context.contentResolver.getType(uri)?.lowercase()
        if (mime == "application/pdf") return true
        val name = runCatching {
            context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            }
        }.getOrNull()
        return name?.endsWith(".pdf", ignoreCase = true) == true
    }

    fun createDocument() {
        documents.add(WordDocument("Document${documents.size + 1}", settings.defaultPageSettings))
        currentIndex = documents.lastIndex
    }

    val openLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        if (isPdfUri(uri)) {
            pdfUri = uri
            return@rememberLauncherForActivityResult
        }
        scope.launch(Dispatchers.IO) {
            val loaded = runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    openAnyDocument(input.readBytes(), "Document")
                }
            }.getOrNull()
            withContext(Dispatchers.Main) {
                if (loaded != null) {
                    loaded.savedUri = uri
                    documents.add(loaded)
                    currentIndex = documents.lastIndex
                } else notify("Could not open the document")
            }
        }
    }

    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            val ok = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { it.write(serializeDocumentZip(doc)) }
                true
            }.getOrDefault(false)
            withContext(Dispatchers.Main) {
                if (ok) {
                    doc.savedUri = uri
                    doc.isDirty = false
                    deleteDraft(context, doc.id)
                    notify("Saved")
                } else notify("Could not save the document")
            }
        }
    }

    fun save() {
        syncEditorTextToDocument(doc, editor.value.text)
        val uri = doc.savedUri
        if (uri == null) {
            saveLauncher.launch("${doc.title.ifBlank { "Document" }}.wdoc")
        } else {
            scope.launch(Dispatchers.IO) {
                val ok = runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(serializeDocumentZip(doc)) }
                    true
                }.getOrDefault(false)
                withContext(Dispatchers.Main) {
                    if (ok) { doc.isDirty = false; deleteDraft(context, doc.id); notify("Saved") }
                    else notify("Could not save the document")
                }
            }
        }
    }

    fun selectedRange(): IntRange? = editor.value.selection.takeIf { !it.collapsed }?.let { it.min until it.max }

    fun paragraphAt(offset: Int): ParagraphBlock? {
        var cursor = 0
        for (p in doc.blocks.filterIsInstance<ParagraphBlock>()) {
            val end = cursor + p.field.text.length
            if (offset in cursor..end) return p
            cursor = end + 2
        }
        return doc.blocks.filterIsInstance<ParagraphBlock>().lastOrNull()
    }

    fun applyInline(transform: (StyleAttrs) -> StyleAttrs) {
        val range = selectedRange() ?: return notify("Select text first")
        var cursor = 0
        doc.blocks.filterIsInstance<ParagraphBlock>().forEach { p ->
            val start = cursor
            val end = cursor + p.field.text.length
            val overlapStart = maxOf(range.first, start)
            val overlapEnd = minOf(range.last + 1, end)
            if (overlapStart < overlapEnd) {
                val base = BuiltInStyles.byId(p.styleId).baseAttrs()
                p.spans = applyStyle(p.spans, overlapStart - start, overlapEnd - start, p.field.text.length, base, transform)
            }
            cursor = end + 2
        }
        doc.isDirty = true
        doc.lastModified = System.currentTimeMillis()
        editor.revision++
    }

    fun applyParagraphStyle(styleId: String) {
        paragraphAt(editor.value.selection.start)?.let {
            it.styleId = styleId
            doc.isDirty = true
            doc.lastModified = System.currentTimeMillis()
            editor.revision++
        }
        showStyleMenu = false
    }

    fun replaceAll() {
        if (findQuery.isBlank()) return
        val replacement = replaceQuery
        val current = editor.value
        val replaced = current.text.replace(findQuery, replacement, ignoreCase = true)
        if (replaced == current.text) return notify("No matches")
        editor.value = TextFieldValue(replaced, TextRange(replaced.length))
        editor.revision++
        doc.isDirty = true
        notify("Replaced")
    }

    LaunchedEffect(initialPath) {
        if (initialPath.isNotBlank()) {
            val uri = runCatching { Uri.parse(initialPath) }.getOrNull() ?: return@LaunchedEffect
            if (isPdfUri(uri)) {
                pdfUri = uri
            } else {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { openAnyDocument(it.readBytes(), "Document") }
                }.getOrNull()?.let {
                    it.savedUri = uri
                    documents.clear(); documents.add(it); currentIndex = 0
                }
            }
        }
    }

    pdfUri?.let { uri ->
        PdfReaderScreen(
            uri = uri,
            isDark = isDark,
            onBack = { pdfUri = null }
        )
        return
    }

    Surface(Modifier.fillMaxSize(), color = palette.appBackground) {
        Column(Modifier.fillMaxSize().navigationBarsPadding()) {
            TitleBar(
                title = doc.title.ifBlank { "Document" } + if (doc.isDirty) " *" else "",
                palette = palette,
                onNew = ::createDocument,
                onOpen = { openLauncher.launch(arrayOf("application/pdf", "text/*", "application/octet-stream", "application/zip")) },
                onSave = ::save,
            )
            TabBar(documents, currentIndex, palette, onSelect = { currentIndex = it }, onNew = ::createDocument)
            MenuBar(menu, palette, onMenu = { menu = if (menu == it) null else it })

            Toolbar(
                palette = palette,
                zoom = zoom,
                styleLabel = BuiltInStyles.byId(paragraphAt(editor.value.selection.start)?.styleId ?: "normal").name,
                showStyleMenu = showStyleMenu,
                onStyleMenu = { showStyleMenu = !showStyleMenu },
                onStyle = ::applyParagraphStyle,
                onUndo = { if (doc.undo()) { editor.value = TextFieldValue(flattenParagraphsForEditor(doc)); editor.revision++ } },
                onRedo = { if (doc.redo()) { editor.value = TextFieldValue(flattenParagraphsForEditor(doc)); editor.revision++ } },
                onBold = { applyInline { it.copy(bold = !it.bold) } },
                onItalic = { applyInline { it.copy(italic = !it.italic) } },
                onUnderline = { applyInline { it.copy(underline = !it.underline) } },
                onFind = { showFind = true },
                onZoom = { zoom = (zoom + it).coerceIn(0.75f, 1.5f) },
            )

            if (menu != null) {
                MenuPanel(menu!!, palette, onDismiss = { menu = null }, onNew = ::createDocument, onOpen = { openLauncher.launch(arrayOf("application/pdf", "text/*", "application/octet-stream", "application/zip")) }, onSave = ::save, onFind = { showFind = true })
            }

            Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = if (isDark) 10.dp else 8.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)).background(palette.pageBackground)
                        .border(1.dp, palette.border, RoundedCornerShape(8.dp))
                ) {
                    SimpleDocumentEditor(doc, Color(0xFF202020), editor, Modifier.fillMaxSize(), onChanged = {})
                }
            }
            StatusBar(doc, zoom, palette, onZoom = { zoom = it })
            SnackbarHost(snackbar)
        }
    }

    if (showFind) {
        FindBar(
            query = findQuery, replacement = replaceQuery,
            onQuery = { findQuery = it }, onReplacement = { replaceQuery = it },
            onReplaceAll = ::replaceAll, onClose = { showFind = false }
        )
    }
}

@Composable
private fun TitleBar(title: String, palette: WordFluentPalette, onNew: () -> Unit, onOpen: () -> Unit, onSave: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(48.dp).background(palette.titleBar).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        FluentIcon("document_text", "Document", Modifier.size(20.dp), Color.White)
        Text(title, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f).padding(start = 8.dp), maxLines = 1)
        IconButton(onClick = onNew, Modifier.size(36.dp)) { FluentIcon("add", "New", Modifier.size(19.dp), Color.White) }
        IconButton(onClick = onOpen, Modifier.size(36.dp)) { FluentIcon("folder_open", "Open", Modifier.size(19.dp), Color.White) }
        IconButton(onClick = onSave, Modifier.size(36.dp)) { FluentIcon("save", "Save", Modifier.size(19.dp), Color.White) }
    }
}

@Composable
private fun TabBar(documents: List<WordDocument>, current: Int, palette: WordFluentPalette, onSelect: (Int) -> Unit, onNew: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(40.dp).background(palette.ribbonBackground).horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.Bottom) {
        documents.forEachIndexed { index, doc ->
            val selected = index == current
            Box(Modifier.padding(start = 6.dp, top = 5.dp, bottom = 0.dp).height(35.dp).clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).background(if (selected) palette.ribbonSurface else Color.Transparent).clickable { onSelect(index) }.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                Text(doc.title.ifBlank { "Untitled" } + if (doc.isDirty) " *" else "", color = palette.text, fontSize = 13.sp, maxLines = 1)
            }
        }
        IconButton(onClick = onNew, Modifier.size(36.dp)) { FluentIcon("add", "New document", Modifier.size(18.dp), palette.text) }
    }
}

@Composable
private fun MenuBar(menu: WordMenu?, palette: WordFluentPalette, onMenu: (WordMenu) -> Unit) {
    Row(Modifier.fillMaxWidth().height(38.dp).background(palette.ribbonBackground), verticalAlignment = Alignment.CenterVertically) {
        listOf(WordMenu.FILE to "File", WordMenu.EDIT to "Edit", WordMenu.VIEW to "View").forEach { (m, label) ->
            TextButton(onClick = { onMenu(m) }, modifier = Modifier.height(34.dp)) { Text(label, color = palette.text, fontSize = 13.sp) }
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = { }) { FluentIcon("settings", "Settings", Modifier.size(18.dp), palette.text) }
    }
    HorizontalDivider(color = palette.border)
}

@Composable
private fun Toolbar(
    palette: WordFluentPalette, zoom: Float, styleLabel: String, showStyleMenu: Boolean,
    onStyleMenu: () -> Unit, onStyle: (String) -> Unit, onUndo: () -> Unit, onRedo: () -> Unit,
    onBold: () -> Unit, onItalic: () -> Unit, onUnderline: () -> Unit, onFind: () -> Unit, onZoom: (Float) -> Unit
) {
    Row(Modifier.fillMaxWidth().height(46.dp).background(palette.ribbonSurface).horizontalScroll(rememberScrollState()).padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        ToolIcon("arrow_undo", onUndo, palette)
        ToolIcon("arrow_redo", onRedo, palette)
        Box {
            TextButton(onClick = onStyleMenu, modifier = Modifier.width(108.dp)) { Text(styleLabel, color = palette.text, maxLines = 1) }
            DropdownMenu(showStyleMenu, onDismissRequest = onStyleMenu) {
                listOf("normal", "title", "subtitle", "heading1", "heading2", "heading3", "quote").forEach { id ->
                    DropdownMenuItem(text = { Text(BuiltInStyles.byId(id).name) }, onClick = { onStyle(id) })
                }
            }
        }
        ToolIcon("text_bold", onBold, palette)
        ToolIcon("text_italic", onItalic, palette)
        ToolIcon("text_underline", onUnderline, palette)
        ToolIcon("search", onFind, palette)
        Spacer(Modifier.width(4.dp))
        ToolIcon("zoom_out", { onZoom(-0.1f) }, palette)
        Text("${(zoom * 100).toInt()}%", color = palette.text, fontSize = 12.sp)
        ToolIcon("zoom_in", { onZoom(0.1f) }, palette)
    }
}

@Composable
private fun ToolIcon(icon: String, onClick: () -> Unit, palette: WordFluentPalette) {
    IconButton(onClick = onClick, Modifier.size(36.dp)) { FluentIcon(icon, null, Modifier.size(18.dp), palette.text) }
}

@Composable
private fun MenuPanel(menu: WordMenu, palette: WordFluentPalette, onDismiss: () -> Unit, onNew: () -> Unit, onOpen: () -> Unit, onSave: () -> Unit, onFind: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(palette.ribbonBackground).padding(6.dp)) {
        when (menu) {
            WordMenu.FILE -> listOf("New" to onNew, "Open" to onOpen, "Save" to onSave).forEach { (label, action) -> TextButton(onClick = { action(); onDismiss() }) { Text(label, color = palette.text) } }
            WordMenu.EDIT -> listOf("Find / Replace" to onFind).forEach { (label, action) -> TextButton(onClick = { action(); onDismiss() }) { Text(label, color = palette.text) } }
            WordMenu.VIEW -> TextButton(onClick = onDismiss) { Text("Zoom and display controls are on the toolbar", color = palette.secondaryText) }
        }
    }
}

@Composable
private fun StatusBar(doc: WordDocument, zoom: Float, palette: WordFluentPalette, onZoom: (Float) -> Unit) {
    val text = doc.blocks.filterIsInstance<ParagraphBlock>().sumOf { it.field.text.length }
    Row(Modifier.fillMaxWidth().height(30.dp).background(palette.ribbonSurface).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("${text} characters", color = palette.secondaryText, fontSize = 11.sp, modifier = Modifier.weight(1f))
        Text("A4", color = palette.secondaryText, fontSize = 11.sp)
        Spacer(Modifier.width(14.dp))
        Text("${(zoom * 100).toInt()}%", color = palette.secondaryText, fontSize = 11.sp)
    }
}

@Composable
private fun FindBar(query: String, replacement: String, onQuery: (String) -> Unit, onReplacement: (String) -> Unit, onReplaceAll: () -> Unit, onClose: () -> Unit) {
    // Kept as a small bottom-sheet-like surface rather than a large modal editor dialog.
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.18f)), contentAlignment = Alignment.BottomCenter) {
        Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(12.dp)) {
            Text("Find and replace", fontSize = 15.sp)
            androidx.compose.material3.OutlinedTextField(query, onQuery, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Find") })
            androidx.compose.material3.OutlinedTextField(replacement, onReplacement, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Replace with") })
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onClose) { Text("Close") }
                TextButton(onClick = onReplaceAll) { Text("Replace all") }
            }
        }
    }
}
