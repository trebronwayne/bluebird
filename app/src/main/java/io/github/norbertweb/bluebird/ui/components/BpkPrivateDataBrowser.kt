package io.github.norbertweb.bluebird.ui.components

import io.github.norbertweb.bluebird.i18n.pluralText
import androidx.compose.ui.res.stringResource
import io.github.norbertweb.bluebird.R
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
                Text(if (appId == null) stringResource(R.string.bluebird_app_data) else "App Data: $appId", style = MaterialTheme.typography.titleLarge, color = text)
                Text(pluralText(R.plurals.plural_files, files.size), style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onBack) { Text(stringResource(R.string.setup_back)) }
        }
        Text(stringResource(R.string.stored_inside_bluebirds_private_data), style = MaterialTheme.typography.bodySmall)
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
                            Text(stringResource(R.string.bytes, file.length()), style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = { deleteTarget = file }) { Text(stringResource(R.string.delete)) }
                    }
                }
            }
        }
    }
    if (selected != null && preview != null) AlertDialog(
        onDismissRequest = { selected = null; preview = null },
        title = { Text(selected?.name ?: stringResource(R.string.file_preview)) },
        text = { Text(preview.orEmpty(), style = MaterialTheme.typography.bodySmall) },
        confirmButton = { TextButton(onClick = { selected = null; preview = null }) { Text(stringResource(R.string.close)) } }
    )
    deleteTarget?.let { file -> AlertDialog(
        onDismissRequest = { deleteTarget = null }, title = { Text(stringResource(R.string.delete_private_data)) },
        text = { Text(stringResource(R.string.delete_this_cannot_be_undone, file.name)) },
        confirmButton = { TextButton(onClick = { file.delete(); deleteTarget = null; refresh++ }) { Text(stringResource(R.string.delete)) } },
        dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text(stringResource(R.string.cancel)) } }
    ) }
}
