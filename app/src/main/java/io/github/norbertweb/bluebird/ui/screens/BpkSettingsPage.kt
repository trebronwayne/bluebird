package io.github.norbertweb.bluebird.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fluent.ui.system.icons.FluentIcons
import fluent.ui.system.icons.regular.Add
import fluent.ui.system.icons.regular.ArrowClockwise
import fluent.ui.system.icons.regular.Bug
import fluent.ui.system.icons.regular.Checkmark
import fluent.ui.system.icons.regular.ChevronDown
import fluent.ui.system.icons.regular.ChevronRight
import fluent.ui.system.icons.regular.Code
import fluent.ui.system.icons.regular.DataTrending
import fluent.ui.system.icons.regular.Shield
import io.github.norbertweb.bluebird.ui.components.BpkReminderPrefs
import io.github.norbertweb.bluebird.ui.components.LocalMicaBackdrop
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────────────────────
// SETTINGS → BPK APPS
//
// Goal of this page: a person with NO programming background can open it, describe an app,
// copy one prompt into an AI chat, and end up with an installable .bpk.
//
// Readability fix: the old page drew its text on the translucent Mica card colour, so in blur
// mode (and in dark theme especially) body text sat on a see-through box and became hard to
// read, and its dialog/buttons used Material's own colours which could disagree with Settings'
// effective theme. Every surface here is a near-opaque tonal fill and every text colour comes
// from Settings' own theme, so contrast no longer depends on the wallpaper behind the blur.
// ─────────────────────────────────────────────────────────────────────────────

private class BpkColors(a: ScreenArgs, mica: Boolean) {
    val text: Color = a.textColor
    val textSoft: Color = a.textColor.copy(alpha = 0.80f)   // body copy: never fainter than this
    val textMuted: Color = a.textColor.copy(alpha = 0.66f)  // captions only
    val card: Color = when {
        !mica -> a.surfaceBg
        a.isDark -> Color(0xF0262626)
        else -> Color(0xF5FFFFFF)
    }
    val stroke: Color = if (a.isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.09f)
    val codeBg: Color = if (a.isDark) Color(0xFF181818) else Color(0xFFF1F3F5)
    val codeText: Color = if (a.isDark) Color(0xFFE8E8E8) else Color(0xFF1F2328)
    val dialog: Color = if (a.isDark) Color(0xFF2B2B2B) else Color(0xFFFFFFFF)
    val accent: Color = a.accent
    val accentSoft: Color = a.accent.copy(alpha = if (a.isDark) 0.22f else 0.12f)
}

@Composable
internal fun BpkAppsSettings(a: ScreenArgs) {
    val c = BpkColors(a, LocalMicaBackdrop.current)
    val ctx = a.ctx
    val clipboard = LocalClipboardManager.current
    var idea by remember { mutableStateOf("") }
    var copiedKey by remember { mutableStateOf<String?>(null) }
    var showPreview by remember { mutableStateOf(false) }
    var remindersOn by remember { mutableStateOf(BpkReminderPrefs.isEnabled(ctx)) }

    LaunchedEffect(copiedKey) {
        if (copiedKey != null) { delay(2200); copiedKey = null }
    }
    fun copy(key: String, text: String) { clipboard.setText(AnnotatedString(text)); copiedKey = key }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

        // ── Hero: describe → copy → paste into AI ───────────────────────────
        BpkCard(c) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(c.accentSoft), contentAlignment = Alignment.Center) {
                        Icon(FluentIcons.Regular.Code, null, tint = c.accent, modifier = Modifier.size(24.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Build your own app with AI", color = c.text, fontSize = (18 * a.scale).sp, fontWeight = FontWeight.Bold)
                        Text("No coding needed. Describe it, copy, paste into any AI.", color = c.textSoft, fontSize = (12.5f * a.scale).sp)
                    }
                }

                Text("1  Describe your app", color = c.text, fontSize = (13 * a.scale).sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = idea,
                    onValueChange = { idea = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text("e.g. A habit tracker that shows my streak and works offline", fontSize = (13 * a.scale).sp) },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = c.codeBg, unfocusedContainerColor = c.codeBg,
                        focusedBorderColor = c.accent, unfocusedBorderColor = c.stroke,
                        focusedTextColor = c.text, unfocusedTextColor = c.text,
                        cursorColor = c.accent,
                        focusedPlaceholderColor = c.textMuted, unfocusedPlaceholderColor = c.textMuted
                    )
                )
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BpkAiPrompt.EXAMPLE_IDEAS.forEach { example ->
                        Box(
                            Modifier.clip(RoundedCornerShape(50)).background(c.accentSoft)
                                .clickable { idea = example }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) { Text(example, color = c.text, fontSize = (12 * a.scale).sp, fontWeight = FontWeight.Medium) }
                    }
                }

                Text("2  Copy, then paste into ChatGPT, Claude, Gemini or any AI chat", color = c.text, fontSize = (13 * a.scale).sp, fontWeight = FontWeight.SemiBold)
                Button(
                    onClick = { copy("prompt", BpkAiPrompt.build(idea)) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = c.accent, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(if (copiedKey == "prompt") FluentIcons.Regular.Checkmark else FluentIcons.Regular.Code, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (copiedKey == "prompt") "Copied. Now paste it into your AI" else "Copy AI prompt", fontSize = (14 * a.scale).sp, fontWeight = FontWeight.SemiBold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BpkOutlineButton("Preview prompt", c, Modifier.weight(1f)) { showPreview = true }
                    BpkOutlineButton(if (copiedKey == "guide") "Copied" else "Copy guide only", c, Modifier.weight(1f)) { copy("guide", BpkAiPrompt.referenceOnly()) }
                }
            }
        }

        // ── How it works ────────────────────────────────────────────────────
        BpkSection("How it works", "From idea to installed app in five steps", FluentIcons.Regular.ArrowClockwise, c, a, initiallyOpen = true) {
            BpkStep("1", "Describe", "Write what the app should do, or tap an example.", c, a)
            BpkStep("2", "Copy", "Tap Copy AI prompt. It already contains the rules Bluebird needs.", c, a)
            BpkStep("3", "Paste into an AI", "The AI replies with every file plus a script named build_bpk.py.", c, a)
            BpkStep("4", "Build the package", "Run python3 build_bpk.py on a computer or in Termux. It creates your .bpk file.", c, a)
            BpkStep("5", "Install", "Open the .bpk from Bluebird File Explorer, follow the installer, then launch it from Start.", c, a)
        }

        BpkSection("Package structure", "What is inside a .bpk", FluentIcons.Regular.Add, c, a) {
            BpkText("A .bpk is a ZIP archive renamed to .bpk. manifest.json must sit at the archive root, never inside an extra parent folder.", c, a)
            BpkCode("my-app.bpk", "tree", c, a, copiedKey) { k, t -> copy(k, t) }
        }

        BpkSection("manifest.json", "The identity card of your app", FluentIcons.Regular.DataTrending, c, a) {
            BpkCode(BpkSnippets.MANIFEST, "manifest", c, a, copiedKey) { k, t -> copy(k, t) }
            BpkField("id", "Unique and stable. Letters, numbers, dots, underscores, hyphens.", c, a)
            BpkField("name", "Name shown in Start and on the window.", c, a)
            BpkField("version", "Your app version, for example 1.0.0.", c, a)
            BpkField("publisher", "Who made it.", c, a)
            BpkField("entry", "Path to the first HTML file inside the package.", c, a)
            BpkField("icon", "Path to a valid PNG inside the package.", c, a)
            BpkText("description, homepage and runtime are optional. Entry and icon files must exist in the package.", c, a)
        }

        BpkSection("Capabilities", "Ask only for what your app needs", FluentIcons.Regular.Shield, c, a) {
            BpkField("app_storage", "Read and write the app's own private key/value data.", c, a)
            BpkField("internet", "Network access. Grant only to apps that need it.", c, a)
            BpkField("file_picker", "Open the system file picker.", c, a)
            BpkField("media_picker", "Open the system media picker.", c, a)
            BpkField("logs", "Write diagnostic messages.", c, a)
            BpkField("audio_playback", "Flag for audio playback.", c, a)
            BpkField("video_playback", "Flag for video playback.", c, a)
            BpkField("brightness_control", "Request a brightness change.", c, a)
            BpkText("A permission toggle is not a substitute for safe code. Never expose the Bluebird bridge to arbitrary remote websites.", c, a)
        }

        BpkSection("JavaScript bridge", "Talk to Bluebird from your app", FluentIcons.Regular.Code, c, a) {
            BpkText("Inside an installed app a global object named Bluebird is available. Always check that it exists so the app also works in a normal browser while you test.", c, a)
            BpkCode(BpkSnippets.BRIDGE, "bridge", c, a, copiedKey) { k, t -> copy(k, t) }
            BpkText("Storage keys may use letters, numbers, dots, underscores, spaces and hyphens. Each value is limited to about 256 KiB.", c, a)
        }

        BpkSection("Packaging by hand", "If you prefer not to use the script", FluentIcons.Regular.Add, c, a) {
            BpkStep("1", "Zip the contents", "Select manifest.json, icon and app and zip them. Do not zip the parent folder.", c, a)
            BpkStep("2", "Rename", "Change the extension from .zip to .bpk.", c, a)
            BpkStep("3", "Install and test", "Open it from File Explorer, then launch and resize the window.", c, a)
        }

        BpkSection("Updates and safety", "Shipping new versions", FluentIcons.Regular.Bug, c, a) {
            BpkText("Update feeds are HTTPS JSON files containing id, version, packageUrl and sha256. The id must match the installed app and sha256 must be the 64-character hash of the exact .bpk file. Bluebird asks before applying an update.", c, a)
            BpkText("SHA-256 proves the file was not changed. It does not prove who published it.", c, a)
        }

        BpkSection("Test checklist", "Before you share your app", FluentIcons.Regular.Checkmark, c, a) {
            listOf(
                "Name, version, icon and entry file are correct",
                "The app launches, resizes and closes cleanly",
                "Each capability works when granted and fails gracefully when denied",
                "Missing files and network failures show a friendly message",
                "Text is readable in light and dark mode"
            ).forEach { BpkBullet(it, c, a) }
        }

        // ── Reminders ───────────────────────────────────────────────────────
        SettingsGroup("Reminders", a) {
            SToggle(
                icon = FluentIcons.Regular.ArrowClockwise,
                label = "BPK tips and reminders",
                sub = "Occasional reminders that you can build apps with AI. Tap one to open this page.",
                a = a,
                checked = remindersOn,
                onToggle = { remindersOn = !remindersOn; BpkReminderPrefs.setEnabled(ctx, remindersOn) }
            )
        }
    }

    if (showPreview) {
        AlertDialog(
            onDismissRequest = { showPreview = false },
            containerColor = c.dialog,
            titleContentColor = c.text,
            textContentColor = c.text,
            title = { Text("AI prompt preview", fontWeight = FontWeight.SemiBold) },
            text = {
                Column(Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState())) {
                    Text(
                        BpkAiPrompt.build(idea),
                        color = c.codeText, fontFamily = FontFamily.Monospace,
                        fontSize = 11.5f.sp, lineHeight = 16.sp,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(c.codeBg).padding(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { copy("prompt", BpkAiPrompt.build(idea)); showPreview = false }, colors = ButtonDefaults.textButtonColors(contentColor = c.accent)) { Text("Copy prompt") }
            },
            dismissButton = {
                TextButton(onClick = { showPreview = false }, colors = ButtonDefaults.textButtonColors(contentColor = c.textSoft)) { Text("Close") }
            }
        )
    }
}

// ── Building blocks ─────────────────────────────────────────────────────────

@Composable
private fun BpkCard(c: BpkColors, content: @Composable () -> Unit) {
    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(c.card)
            .border(BorderStroke(1.dp, c.stroke), RoundedCornerShape(10.dp))
    ) { content() }
}

@Composable
private fun BpkOutlineButton(label: String, c: BpkColors, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, c.text.copy(alpha = 0.28f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = c.text)
    ) { Text(label, fontSize = 12.5f.sp, maxLines = 1) }
}

/** Collapsible documentation card: tidy header, readable body, one section open at a time is not forced. */
@Composable
private fun BpkSection(
    title: String, subtitle: String, icon: ImageVector,
    c: BpkColors, a: ScreenArgs, initiallyOpen: Boolean = false,
    content: @Composable () -> Unit
) {
    var open by remember { mutableStateOf(initiallyOpen) }
    BpkCard(c) {
        Column {
            Row(
                Modifier.fillMaxWidth().clickable { open = !open }.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(icon, null, tint = c.accent, modifier = Modifier.size(20.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, color = c.text, fontSize = (14.5f * a.scale).sp, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, color = c.textMuted, fontSize = (12 * a.scale).sp)
                }
                Icon(
                    if (open) FluentIcons.Regular.ChevronDown else FluentIcons.Regular.ChevronRight,
                    null, tint = c.textMuted, modifier = Modifier.size(16.dp)
                )
            }
            AnimatedVisibility(open, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Column(
                    Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) { content() }
            }
        }
    }
}

@Composable
private fun BpkText(text: String, c: BpkColors, a: ScreenArgs) {
    Text(text, color = c.textSoft, fontSize = (13 * a.scale).sp, lineHeight = (19 * a.scale).sp)
}

@Composable
private fun BpkBullet(text: String, c: BpkColors, a: ScreenArgs) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Icon(FluentIcons.Regular.Checkmark, null, tint = c.accent, modifier = Modifier.padding(top = 2.dp).size(15.dp))
        Text(text, color = c.textSoft, fontSize = (13 * a.scale).sp, lineHeight = (19 * a.scale).sp)
    }
}

@Composable
private fun BpkStep(number: String, title: String, detail: String, c: BpkColors, a: ScreenArgs) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Box(Modifier.size(26.dp).clip(CircleShape).background(c.accentSoft), contentAlignment = Alignment.Center) {
            Text(number, color = c.accent, fontSize = (12 * a.scale).sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = c.text, fontSize = (13.5f * a.scale).sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = c.textSoft, fontSize = (12.8f * a.scale).sp, lineHeight = (18 * a.scale).sp)
        }
    }
}

/** A name/description row: monospace name on a tinted chip, plain description beside it. */
@Composable
private fun BpkField(name: String, description: String, c: BpkColors, a: ScreenArgs) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.clip(RoundedCornerShape(5.dp)).background(c.accentSoft).padding(horizontal = 8.dp, vertical = 3.dp)) {
            Text(name, color = c.text, fontFamily = FontFamily.Monospace, fontSize = (12 * a.scale).sp, fontWeight = FontWeight.SemiBold)
        }
        Text(description, color = c.textSoft, fontSize = (12.8f * a.scale).sp, lineHeight = (18 * a.scale).sp)
    }
}

/** Monospace block on an opaque fill, horizontally scrollable, with its own Copy action. */
@Composable
private fun BpkCode(
    text: String, key: String, c: BpkColors, a: ScreenArgs,
    copiedKey: String?, onCopy: (String, String) -> Unit
) {
    val body = if (key == "tree") BpkSnippets.TREE else text
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(c.codeBg)) {
        Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(key, color = c.textMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
            TextButton(onClick = { onCopy(key, body) }, colors = ButtonDefaults.textButtonColors(contentColor = c.accent)) {
                Text(if (copiedKey == key) "Copied" else "Copy", fontSize = 12.sp)
            }
        }
        Text(
            body, color = c.codeText, fontFamily = FontFamily.Monospace,
            fontSize = (12 * a.scale).sp, lineHeight = (17 * a.scale).sp,
            modifier = Modifier.horizontalScroll(rememberScrollState()).padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
        )
    }
}

private object BpkSnippets {
    val TREE = "my-app.bpk\n├── manifest.json\n├── icon/\n│   └── icon.png\n├── app/\n│   ├── index.html\n│   ├── styles.css\n│   └── main.js\n└── installer/   (optional custom installer)"

    val MANIFEST = "{\n  \"id\": \"org.example.myapp\",\n  \"name\": \"My App\",\n  \"version\": \"1.0.0\",\n  \"publisher\": \"Your Name\",\n  \"entry\": \"app/index.html\",\n  \"icon\": \"icon/icon.png\",\n  \"description\": \"A short description\",\n  \"runtime\": \"web\"\n}"

    val BRIDGE = "if (typeof Bluebird !== 'undefined') {\n  const info = JSON.parse(Bluebird.getAppInfo());\n  if (Bluebird.capabilityGranted('app_storage')) {\n    Bluebird.storageSet('display-name', 'Guest');\n    const saved = JSON.parse(Bluebird.storageGet('display-name'));\n    if (saved.ok) console.log(saved.value);\n  }\n  Bluebird.log('info', 'App started');\n}\n\nOther calls:\nBluebird.requestFilePicker()\nBluebird.requestMediaPicker()\nBluebird.setBrightness(1..255)"
}
