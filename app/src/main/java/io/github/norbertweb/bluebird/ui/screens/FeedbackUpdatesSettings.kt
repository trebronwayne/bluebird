package io.github.norbertweb.bluebird.ui.screens

// ─────────────────────────────────────────────────────────────────────────────
// NOTE ON ScreenArgs: your SettingsScreen.kt file was uploaded truncated, so
// the ScreenArgs class definition wasn't visible to me. Below I'm assuming it
// exposes fields named `textColor`, `surfaceColor`, `accentColor`, `context`
// and `scale` (inferred from how it's constructed:
// ScreenArgs(effectiveDark, resolvedText, resolvedSurface, specialAccent,
// uiState, viewModel, context, scale = ...)). If your actual field names
// differ, just rename the `args.xxx` references below to match.
// ─────────────────────────────────────────────────────────────────────────────

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import fluent.ui.system.icons.FluentIcons
import fluent.ui.system.icons.regular.ArrowDownload
import fluent.ui.system.icons.regular.Bug
import fluent.ui.system.icons.regular.CheckmarkCircle
import io.github.norbertweb.bluebird.BuildConfig

// ─────────────────────────────────────────────────────────────────────────────
// FEEDBACK & UPDATES SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FeedbackUpdatesSettings(args: ScreenArgs) {
    val textColor = args.textColor
    val surfaceColor = args.surfaceColor
    val accentColor = args.accentColor
    val context = args.context
    val scale = args.scale

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        FeedbackSection(textColor, surfaceColor, accentColor, scale)
        Spacer(Modifier.height(4.dp))
        UpdateCheckSection(textColor, surfaceColor, accentColor, scale)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FEEDBACK — writes to Firestore collection "feedback"
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FeedbackSection(
    textColor: androidx.compose.ui.graphics.Color,
    surfaceColor: androidx.compose.ui.graphics.Color,
    accentColor: androidx.compose.ui.graphics.Color,
    scale: Float
) {
    var message by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitResult by remember { mutableStateOf<Boolean?>(null) } // null = idle, true = success, false = error

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(FluentIcons.Regular.Bug, null, tint = accentColor, modifier = Modifier.height(18.dp))
                Text(
                    "Send Feedback",
                    color = textColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = (15 * scale).sp
                )
            }
            Text(
                "Found a bug or have a suggestion? Let us know below.",
                color = textColor.copy(alpha = 0.6f),
                fontSize = (12 * scale).sp
            )

            OutlinedTextField(
                value = message,
                onValueChange = { message = it; submitResult = null },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Describe the issue or idea…") },
                minLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedBorderColor = accentColor
                )
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; submitResult = null },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Email (optional, for follow-up)") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedBorderColor = accentColor
                )
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    enabled = message.isNotBlank() && !isSubmitting,
                    onClick = {
                        isSubmitting = true
                        submitResult = null
                        val feedback = hashMapOf(
                            "message" to message.trim(),
                            "email" to email.trim().ifBlank { null },
                            "appVersion" to BuildConfig.VERSION_NAME,
                            "appVersionCode" to BuildConfig.VERSION_CODE,
                            "deviceModel" to Build.MODEL,
                            "deviceManufacturer" to Build.MANUFACTURER,
                            "androidSdk" to Build.VERSION.SDK_INT,
                            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                        )
                        FirebaseFirestore.getInstance()
                            .collection("feedback")
                            .add(feedback)
                            .addOnSuccessListener {
                                isSubmitting = false
                                submitResult = true
                                message = ""
                                email = ""
                            }
                            .addOnFailureListener {
                                isSubmitting = false
                                submitResult = false
                            }
                    }
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.height(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Submit")
                    }
                }

                when (submitResult) {
                    true -> Text("Thanks — feedback sent!", color = androidx.compose.ui.graphics.Color(0xFF4CAF50), fontSize = (12 * scale).sp)
                    false -> Text("Couldn't send — check your connection and try again.", color = androidx.compose.ui.graphics.Color(0xFFE57373), fontSize = (12 * scale).sp)
                    null -> {}
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// UPDATE CHECK — reads from Firebase Remote Config
//
// Remote Config keys to set up in the Firebase Console (Remote Config tab):
//   latest_version_name   (String)  e.g. "2.1"
//   latest_version_code   (Number)  e.g. 14
//   release_notes         (String)  e.g. "Bug fixes and performance improvements"
//   download_url          (String)  direct link to the APK (e.g. a GitHub
//                                   Release asset URL, public or private repo
//                                   doesn't matter here since this isn't
//                                   calling GitHub's API at all)
// ─────────────────────────────────────────────────────────────────────────────

private data class UpdateInfo(
    val versionName: String,
    val versionCode: Long,
    val releaseNotes: String,
    val downloadUrl: String
)

@Composable
private fun UpdateCheckSection(
    textColor: androidx.compose.ui.graphics.Color,
    surfaceColor: androidx.compose.ui.graphics.Color,
    accentColor: androidx.compose.ui.graphics.Color,
    scale: Float
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isChecking by remember { mutableStateOf(false) }
    var checkError by remember { mutableStateOf(false) }
    var upToDate by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(FluentIcons.Regular.ArrowDownload, null, tint = accentColor, modifier = Modifier.height(18.dp))
                Text(
                    "Check for Updates",
                    color = textColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = (15 * scale).sp
                )
            }
            Text(
                "Current version: ${BuildConfig.VERSION_NAME}",
                color = textColor.copy(alpha = 0.6f),
                fontSize = (12 * scale).sp
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    enabled = !isChecking,
                    onClick = {
                        isChecking = true
                        checkError = false
                        upToDate = false
                        updateInfo = null

                        val remoteConfig = Firebase.remoteConfig
                        remoteConfig.setConfigSettingsAsync(
                            remoteConfigSettings { minimumFetchIntervalInSeconds = 3600 }
                        )
                        remoteConfig.fetchAndActivate()
                            .addOnCompleteListener { task ->
                                isChecking = false
                                if (!task.isSuccessful) {
                                    checkError = true
                                    return@addOnCompleteListener
                                }
                                val latestCode = remoteConfig.getLong("latest_version_code")
                                if (latestCode > BuildConfig.VERSION_CODE) {
                                    updateInfo = UpdateInfo(
                                        versionName = remoteConfig.getString("latest_version_name"),
                                        versionCode = latestCode,
                                        releaseNotes = remoteConfig.getString("release_notes"),
                                        downloadUrl = remoteConfig.getString("download_url")
                                    )
                                } else {
                                    upToDate = true
                                }
                            }
                    }
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(modifier = Modifier.height(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Check Now")
                    }
                }

                when {
                    checkError -> Text("Couldn't check — try again later.", color = androidx.compose.ui.graphics.Color(0xFFE57373), fontSize = (12 * scale).sp)
                    upToDate -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(FluentIcons.Regular.CheckmarkCircle, null, tint = androidx.compose.ui.graphics.Color(0xFF4CAF50), modifier = Modifier.height(14.dp))
                        Text("You're up to date", color = textColor.copy(alpha = 0.7f), fontSize = (12 * scale).sp)
                    }
                }
            }
        }
    }

    // Update-available dialog
    updateInfo?.let { info ->
        AlertDialog(
            onDismissRequest = { updateInfo = null },
            title = { Text("Update available: v${info.versionName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("A new version of Bluebird is ready to download.")
                    if (info.releaseNotes.isNotBlank()) {
                        Text(info.releaseNotes, fontSize = 13.sp, color = textColor.copy(alpha = 0.7f))
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(info.downloadUrl)))
                    updateInfo = null
                }) {
                    Text("Download")
                }
            },
            dismissButton = {
                TextButton(onClick = { updateInfo = null }) {
                    Text("Later")
                }
            }
        )
    }
}
