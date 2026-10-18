package io.github.norbertweb.bluebird.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File

/** Add this screen as File Explorer's virtual "Bluebird > App Data" location. */
@Composable
fun BpkPrivateDataBrowserScreen(isDark: Boolean, appId: String? = null, onBack: () -> Unit = {}) {
    val context = LocalContext.current
    var refresh by remember { mutableStateOf(0) }
    var selected by remember { mutableStateOf<File?>(null) }
    var preview by remember { mutableStateOf<String?>(null) }
    var deleteTarget by remember { mutableStateOf<File?>(null) }
    val root = remember(refresh, appId) { if (appId == null) BluebirdStorage.appDataRoot(context) else BluebirdStorage.appData(context, appId) }
    val files = remember(refresh, root) { root.walkTopDown().filter { it.isFile }.toList() }
    val text = if (isDark) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color(0xFF202020)
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(if (appId == null) "Bluebird App Data" else "App Data: $appId", style = MaterialTheme.typography.titleLarge, color = text)
                Text("Private app sandbox • ${files.size} files", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onBack) { Text("Back") }
        }
        Text("Stored inside Bluebird's private data. Other apps cannot browse this folder; use this screen to inspect or delete files.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(files, key = { it.absolutePath }) { file ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f).clickable {
                            selected = file
                            preview = if (file.length() <= 128 * 1024 && file.extension.lowercase() in setOf("txt", "json", "html", "css", "js", "md", "log")) runCatching { file.readText().take(12000) }.getOrDefault("Unable to read file") else "Preview is only available for small text files."
                        }) {
                            Text(file.relativeTo(root).path, color = text, style = MaterialTheme.typography.bodyMedium)
                            Text("${file.length()} bytes", style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = { deleteTarget = file }) { Text("Delete") }
                    }
                }
            }
        }
    }
    if (selected != null && preview != null) AlertDialog(
        onDismissRequest = { selected = null; preview = null },
        title = { Text(selected?.name ?: "File preview") },
        text = { Text(preview.orEmpty(), style = MaterialTheme.typography.bodySmall) },
        confirmButton = { TextButton(onClick = { selected = null; preview = null }) { Text("Close") } }
    )
    deleteTarget?.let { file -> AlertDialog(
        onDismissRequest = { deleteTarget = null }, title = { Text("Delete private data?") },
        text = { Text("Delete ${file.name}? This cannot be undone.") },
        confirmButton = { TextButton(onClick = { file.delete(); deleteTarget = null; refresh++ }) { Text("Delete") } },
        dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } }
    ) }
}
