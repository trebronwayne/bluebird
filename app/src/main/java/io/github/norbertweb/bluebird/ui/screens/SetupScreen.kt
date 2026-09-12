package io.github.norbertweb.bluebird.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
// Microsoft Fluent System UI icons (https://github.com/niyajali/fluentui-system-icons) —
// same icon set used across Bluebird OS (see MediaPlayerScreen.kt). FluentIcons.Filled.*
// is used for emphasis/active states, FluentIcons.Regular.* everywhere else.

import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import fluent.ui.system.icons.FluentIcons
import fluent.ui.system.icons.filled.Checkmark
import fluent.ui.system.icons.filled.CheckmarkCircle
import fluent.ui.system.icons.filled.Home
import fluent.ui.system.icons.regular.Alert
import fluent.ui.system.icons.regular.Apps
import fluent.ui.system.icons.regular.AppsList
import fluent.ui.system.icons.regular.ArrowRight
import fluent.ui.system.icons.regular.Camera
import fluent.ui.system.icons.regular.CameraAdd
import fluent.ui.system.icons.regular.ChevronRight
import fluent.ui.system.icons.regular.CloudArrowUp
import fluent.ui.system.icons.regular.DocumentText
import fluent.ui.system.icons.regular.FolderOpen
import fluent.ui.system.icons.regular.Grid
import fluent.ui.system.icons.regular.HandLeft
import fluent.ui.system.icons.regular.HardDrive
import fluent.ui.system.icons.regular.Image
import fluent.ui.system.icons.regular.ImageMultiple
import fluent.ui.system.icons.regular.Info
import fluent.ui.system.icons.regular.LockClosedKey
import fluent.ui.system.icons.regular.Open
import fluent.ui.system.icons.regular.Person
import fluent.ui.system.icons.regular.Settings
import fluent.ui.system.icons.regular.ShieldCheckmark
import fluent.ui.system.icons.regular.TopSpeed
import fluent.ui.system.icons.regular.WeatherMoon
import io.github.norbertweb.bluebird.LauncherViewModel

// ─────────────────────────────────────────────────────────
// Fluent icon set — every icon this screen uses, centralized in one place
// (mirrors the same pattern used in MediaPlayerScreen.kt) so a name that
// doesn't resolve in your installed library version is a one-line fix here
// rather than a hunt through the whole file.
// ─────────────────────────────────────────────────────────
private object SetupFI {
    val Home         = FluentIcons.Filled.Home
    val Lock         = FluentIcons.Regular.LockClosedKey
    val Person       = FluentIcons.Regular.Person
    val Image        = FluentIcons.Regular.Image
    val Description  = FluentIcons.Regular.DocumentText
    val FolderOpen   = FluentIcons.Regular.FolderOpen
    val Storage      = FluentIcons.Regular.HardDrive
    val Notifications = FluentIcons.Regular.Alert
    val ArrowForward = FluentIcons.Regular.ArrowRight
    val Check        = FluentIcons.Filled.Checkmark
    val CheckCircle  = FluentIcons.Filled.CheckmarkCircle
    val ChevronRight = FluentIcons.Regular.ChevronRight
    val Dashboard    = FluentIcons.Regular.AppsList
    val Security     = FluentIcons.Regular.ShieldCheckmark
    val Settings     = FluentIcons.Regular.Settings
    val Speed        = FluentIcons.Regular.TopSpeed
    val AddAPhoto    = FluentIcons.Regular.Image
    val PhotoLibrary = FluentIcons.Regular.ImageMultiple
    val SkipNext     = FluentIcons.Regular.ArrowRight
    val Info         = FluentIcons.Regular.Info
    val OpenInNew    = FluentIcons.Regular.Open
    // Feature-tile icons for the new "Customize your setup" step

    val DarkMode     = FluentIcons.Regular.WeatherMoon
    val Widgets      = FluentIcons.Regular.Grid
    val AppSuggest   = FluentIcons.Regular.Apps
    val Backup       = FluentIcons.Regular.CloudArrowUp
    val Gestures     = FluentIcons.Regular.HandLeft
    val IconPack     = FluentIcons.Regular.Grid
}

// ─────────────────────────────────────────────────────────
// Data Models
// ─────────────────────────────────────────────────────────

private data class PermissionItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val permissions: List<String>,
    val optional: Boolean = false,
    val requiresSettingsIntent: Boolean = false,
    val settingsIntentAction: String? = null,
    val isManageAllFiles: Boolean = false
)

private data class StepMeta(val icon: ImageVector, val label: String, val subtitle: String)

private val stepMeta = listOf(
    StepMeta(SetupFI.Home, "Welcome", "Meet Bluebird"),
    StepMeta(SetupFI.Lock, "Access", "Permissions & storage"),
    StepMeta(SetupFI.Person, "Profile", "Your identity"),
    StepMeta(SetupFI.Image, "Finish", "Make it yours")
)

private enum class StepState { DONE, ACTIVE, UPCOMING }

private fun areAllPermissionsGranted(
    context: android.content.Context,
    permissions: List<String>
): Boolean = permissions.all {
    ContextCompat.checkSelfPermission(context, it) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
}

// ─────────────────────────────────────────────────────────
// Root Setup Screen
// ─────────────────────────────────────────────────────────

@Composable
fun SetupScreen(
    viewModel: LauncherViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val step = uiState.setupStep.coerceIn(0, 3)
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.smallestScreenWidthDp >= 600

    var setupCompleted by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf<PermissionItem?>(null) }

    // Only permissions used by real Bluebird functionality are presented here.
    val permissionItems = remember {
        val mediaPerms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        buildList {
            add(
                PermissionItem(
                    title = "Photos, videos & audio",
                    description = "Lets Bluebird apps browse media when you choose to use those features.",
                    icon = SetupFI.FolderOpen,
                    permissions = mediaPerms,
                    optional = true
                )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(
                    PermissionItem(
                        title = "Notifications",
                        description = "Allows Bluebird to deliver its own notifications when notification features are enabled.",
                        icon = SetupFI.Notifications,
                        permissions = listOf(Manifest.permission.POST_NOTIFICATIONS),
                        optional = true
                    )
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add(
                    PermissionItem(
                        title = "All files access",
                        description = "Optional advanced storage access for desktop-style file management and local file operations.",
                        icon = SetupFI.Storage,
                        permissions = emptyList(),
                        optional = true,
                        requiresSettingsIntent = true,
                        settingsIntentAction = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        isManageAllFiles = true
                    )
                )
            }
        }
    }

    val grantedStates = remember {
        mutableStateMapOf<String, Boolean>()
    }

    fun refreshPermissionState(item: PermissionItem) {
        grantedStates[item.title] = if (item.isManageAllFiles && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            areAllPermissionsGranted(context, item.permissions)
        }
    }

    LaunchedEffect(permissionItems) {
        permissionItems.forEach(::refreshPermissionState)
    }

    var requestedItem by remember { mutableStateOf<PermissionItem?>(null) }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        requestedItem?.let { item ->
            val granted = if (item.isManageAllFiles && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                item.permissions.all { results[it] == true }
            }
            grantedStates[item.title] = granted
            if (!granted && item.permissions.isNotEmpty()) showPermissionRationale = item
        }
        requestedItem = null
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        permissionItems.filter { it.requiresSettingsIntent }.forEach(::refreshPermissionState)
    }

    LaunchedEffect(Unit) {
        permissionItems.forEach(::refreshPermissionState)
    }

    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    val panel = if (isDark) Color(0xE61F1F1F) else Color(0xE6F8F8F8)
    val panelBorder = if (isDark) Color.White.copy(alpha = 0.09f) else Color.Black.copy(alpha = 0.08f)
    val primary = Color(0xFF0078D4)
    val textPrimary = if (isDark) Color.White else Color(0xFF1A1A1A)
    val textSecondary = if (isDark) Color.White.copy(alpha = 0.68f) else Color.Black.copy(alpha = 0.62f)
    val rootBackground = if (isDark) Color(0xFF181818) else Color(0xFFF2F2F2)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(rootBackground)
    ) {
        if (setupCompleted) {
            LaunchedEffect(Unit) { viewModel.completeSetup() }
        } else {
            if (isTablet) {
                // ── Tablet: sidebar + content ──
                Row(modifier = Modifier.fillMaxSize()) {
                    LeftSidebar(
                        currentStep = step,
                        modifier = Modifier
                            .width(260.dp)
                            .fillMaxHeight()
                    )
                    VerticalDivider(
                        modifier = Modifier.fillMaxHeight(),
                        thickness = 1.dp,
                        color = panelBorder
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        StepContent(
                            step = step,
                            viewModel = viewModel,
                            uiState = uiState,
                            permissionItems = permissionItems,
                            grantedStates = grantedStates,
                            onRequestPermission = { item ->
                                if (item.isManageAllFiles && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    val intent = Intent(item.settingsIntentAction).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    settingsLauncher.launch(intent)
                                } else if (item.permissions.isNotEmpty()) {
                                    requestedItem = item
                                    permLauncher.launch(item.permissions.toTypedArray())
                                }
                            },
                            onBack = { viewModel.decrementSetupStep() },
                            onSetupCompleted = { setupCompleted = true }
                        )
                    }
                }
            } else {
                // ── Phone: top stepper + content ──
                Column(modifier = Modifier.fillMaxSize()) {
                    TopStepBar(currentStep = step, total = stepMeta.size)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        StepContent(
                            step = step,
                            viewModel = viewModel,
                            uiState = uiState,
                            permissionItems = permissionItems,
                            grantedStates = grantedStates,
                            onRequestPermission = { item ->
                                if (item.isManageAllFiles && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    val intent = Intent(item.settingsIntentAction).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    settingsLauncher.launch(intent)
                                } else if (item.permissions.isNotEmpty()) {
                                    requestedItem = item
                                    permLauncher.launch(item.permissions.toTypedArray())
                                }
                            },
                            onBack = { viewModel.decrementSetupStep() },
                            onSetupCompleted = { setupCompleted = true }
                        )
                    }
                }
            }
        }

        // Permission rationale dialog
        showPermissionRationale?.let { item ->
            AlertDialog(
                onDismissRequest = { showPermissionRationale = null },
                icon = { Icon(item.icon, contentDescription = null, tint = primary) },
                title = { Text("${item.title} permission needed") },
                text = {
                    Text(
                        if (item.optional)
                            "This permission is optional. Without it, some features related to ${item.title.lowercase()} will not be available. You can grant it later in Android Settings."
                        else
                            "${item.title} access is required for core features. Please grant it in Android Settings to continue.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showPermissionRationale = null
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        })
                    }) { Text("Open Settings") }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionRationale = null }) { Text("Dismiss") }
                }
            )
        }

    }
}

// ─────────────────────────────────────────────────────────
// Step Content Router
// ─────────────────────────────────────────────────────────

@Composable
private fun StepContent(
    step: Int,
    viewModel: LauncherViewModel,
    uiState: io.github.norbertweb.bluebird.LauncherUiState,
    permissionItems: List<PermissionItem>,
    grantedStates: Map<String, Boolean>,
    onRequestPermission: (PermissionItem) -> Unit,
    onBack: () -> Unit,
    onSetupCompleted: () -> Unit
) {
    val context = LocalContext.current

    // Step switches instantly now — was AnimatedContent with a 320ms
    // slide+fade transition, out of step with the rest of the app having
    // its motion removed everywhere else.
    when (step) {
        0 -> WelcomeStep(onNext = { viewModel.advanceSetupStep() })
        1 -> PermissionsStep(
            permissionItems = permissionItems,
            grantedStates = grantedStates,
            onRequestPermission = onRequestPermission,
            onNext = { viewModel.advanceSetupStep() },
            onBack = onBack
        )
        2 -> UsernameStep(
            currentName = uiState.userProfile.userName,
            onNameChange = { viewModel.setUserName(it) },
            onNext = { viewModel.advanceSetupStep() },
            onBack = onBack
        )
        3 -> AvatarStep(
            context = context,
            onAvatarPicked = { viewModel.setProfilePicture(context, it) },
            onNext = { onSetupCompleted() },
            onBack = onBack
        )
        else -> LaunchedEffect(Unit) { onSetupCompleted() }
    }
}

private fun setupPanel(isDark: Boolean): Color = if (isDark) Color(0xE61F1F1F) else Color(0xE6F8F8F8)
private fun setupBorder(isDark: Boolean): Color = if (isDark) Color.White.copy(alpha = 0.09f) else Color.Black.copy(alpha = 0.08f)
private fun setupPrimary(): Color = Color(0xFF0078D4)
private fun setupTextPrimary(isDark: Boolean): Color = if (isDark) Color.White else Color(0xFF1A1A1A)
private fun setupTextSecondary(isDark: Boolean): Color = if (isDark) Color.White.copy(alpha = 0.68f) else Color.Black.copy(alpha = 0.62f)

// ─────────────────────────────────────────────────────────
// Phone Top Step Bar
// ─────────────────────────────────────────────────────────

@Composable
private fun TopStepBar(currentStep: Int, total: Int) {
    val dark = isSystemInDarkTheme()
    val panel = setupPanel(dark)
    val border = setupBorder(dark)
    val primary = setupPrimary()
    val textPrimary = setupTextPrimary(dark)
    val textSecondary = setupTextSecondary(dark)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(panel)
            .border(0.5.dp, border)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                Box(Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(primary.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                    Icon(SetupFI.Home, null, tint = primary, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text("Bluebird setup", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = textPrimary)
                    Text(stepMeta[currentStep].label, style = MaterialTheme.typography.labelSmall, color = textSecondary)
                }
            }
            Text("${currentStep + 1} / $total", style = MaterialTheme.typography.labelMedium, color = textSecondary)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(total) { index ->
                Box(Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(if (index <= currentStep) primary else border))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
// Tablet Left Sidebar
// ─────────────────────────────────────────────────────────

@Composable
private fun LeftSidebar(currentStep: Int, modifier: Modifier = Modifier) {
    val dark = isSystemInDarkTheme()
    val panel = setupPanel(dark)
    val border = setupBorder(dark)
    val primary = setupPrimary()
    val textPrimary = setupTextPrimary(dark)
    val textSecondary = setupTextSecondary(dark)

    Column(
        modifier = modifier
            .background(panel)
            .border(0.5.dp, border)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(42.dp).clip(RoundedCornerShape(11.dp)).background(primary.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                    Icon(SetupFI.Home, null, tint = primary, modifier = Modifier.size(24.dp))
                }
                Column {
                    Text("Bluebird", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = textPrimary)
                    Text("OS setup", style = MaterialTheme.typography.labelSmall, color = textSecondary)
                }
            }
            Spacer(Modifier.height(28.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                stepMeta.forEachIndexed { index, meta ->
                    SidebarStepItem(index, meta, when { index < currentStep -> StepState.DONE; index == currentStep -> StepState.ACTIVE; else -> StepState.UPCOMING })
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            HorizontalDivider(color = border, thickness = 0.5.dp, modifier = Modifier.padding(bottom = 10.dp))
            Text("Bluebird OS", style = MaterialTheme.typography.labelSmall, color = textPrimary)
            Text("A desktop experience for Android", style = MaterialTheme.typography.labelSmall, color = textSecondary)
        }
    }
}

@Composable
private fun SidebarStepItem(index: Int, meta: StepMeta, state: StepState) {
    val dark = isSystemInDarkTheme()
    val primary = setupPrimary()
    val panel = setupPanel(dark)
    val border = setupBorder(dark)
    val textPrimary = setupTextPrimary(dark)
    val textSecondary = setupTextSecondary(dark)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(if (state == StepState.ACTIVE) primary.copy(alpha = 0.10f) else Color.Transparent)
            .border(if (state == StepState.ACTIVE) 1.dp else 0.5.dp, if (state == StepState.ACTIVE) primary.copy(alpha = 0.25f) else Color.Transparent, RoundedCornerShape(9.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(Modifier.size(28.dp).clip(CircleShape).background(if (state == StepState.UPCOMING) panel else primary.copy(alpha = 0.12f)).border(1.dp, if (state == StepState.UPCOMING) border else primary, CircleShape), contentAlignment = Alignment.Center) {
            if (state == StepState.DONE) Icon(SetupFI.Check, null, tint = primary, modifier = Modifier.size(14.dp))
            else Text("${index + 1}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = if (state == StepState.UPCOMING) textSecondary else primary)
        }
        Column(Modifier.weight(1f)) {
            Text(meta.label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = if (state == StepState.UPCOMING) textSecondary else textPrimary)
            Text(meta.subtitle, style = MaterialTheme.typography.labelSmall, color = textSecondary)
        }
        if (state == StepState.ACTIVE) Icon(SetupFI.ChevronRight, null, tint = primary, modifier = Modifier.size(16.dp))
    }
}

// ─────────────────────────────────────────────────────────
// Step Container
// ─────────────────────────────────────────────────────────

@Composable
private fun StepContainer(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    val dark = isSystemInDarkTheme()
    val panel = setupPanel(dark)
    val border = setupBorder(dark)
    val textPrimary = setupTextPrimary(dark)
    val textSecondary = setupTextSecondary(dark)
    Column(
        modifier = Modifier
            .widthIn(max = 600.dp)
            .padding(horizontal = 24.dp, vertical = 28.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(panel)
                .border(1.dp, border, RoundedCornerShape(14.dp))
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(setupPrimary().copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = setupPrimary(), modifier = Modifier.size(24.dp))
                }
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = textPrimary)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = textSecondary, lineHeight = 22.sp)
            }
        }
        content()
    }
}

// ─────────────────────────────────────────────────────────
// Step 0 — Welcome
// ─────────────────────────────────────────────────────────

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    val dark = isSystemInDarkTheme()
    val panel = setupPanel(dark)
    val border = setupBorder(dark)
    val textPrimary = setupTextPrimary(dark)
    val textSecondary = setupTextSecondary(dark)
    StepContainer(
        title = "Welcome to Bluebird",
        subtitle = "A modern desktop-style experience for Android, tuned for a clean and responsive first run.",
        icon = SetupFI.Home
    ) {
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(panel).border(1.dp,border,RoundedCornerShape(14.dp)).padding(18.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("A few things to get ready", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = textPrimary)
                Text("Bluebird will guide you through access and profile settings. You stay in control of every permission.", style = MaterialTheme.typography.bodySmall, color = textSecondary, lineHeight = 19.sp)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        SetupFI.Dashboard to "Windows-inspired desktop shell",
                        SetupFI.FolderOpen to "Real file and media access",
                        SetupFI.Person to "A personal Bluebird profile"
                    ).forEach { (icon, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(icon, null, tint = setupPrimary(), modifier = Modifier.size(18.dp))
                            Text(label, style = MaterialTheme.typography.bodySmall, color = textPrimary)
                        }
                    }
                }
            }
        }
        PrimaryButton("Get started", SetupFI.ArrowForward, onClick = onNext)
    }
}

// ─────────────────────────────────────────────────────────
// Step 1 — Permissions
// ─────────────────────────────────────────────────────────
@Composable
private fun PermissionCard(
    icon: ImageVector,
    title: String,
    desc: String,
    isGranted: Boolean,
    isOptional: Boolean,
    grantLabel: String,
    onGrant: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val panel = setupPanel(dark)
    val border = setupBorder(dark)
    val primary = setupPrimary()
    val textPrimary = setupTextPrimary(dark)
    val textSecondary = setupTextSecondary(dark)

    val cardShape = RoundedCornerShape(12.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(panel)
            .border(1.dp, border, cardShape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(
                    if (isGranted) {
                        primary.copy(alpha = 0.14f)
                    } else {
                        textSecondary.copy(alpha = 0.08f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isGranted) SetupFI.Check else icon,
                contentDescription = null,
                tint = if (isGranted) primary else textPrimary,
                modifier = Modifier.size(21.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = textPrimary
            )

            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = textSecondary,
                lineHeight = 17.sp
            )

            if (isOptional) {
                Text(
                    text = "Optional",
                    style = MaterialTheme.typography.labelSmall,
                    color = textSecondary
                )
            }
        }

        if (isGranted) {
            Icon(
                imageVector = SetupFI.CheckCircle,
                contentDescription = "Granted",
                tint = primary,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(primary.copy(alpha = 0.12f))
                    .clickable(onClick = onGrant)
                    .padding(horizontal = 11.dp, vertical = 8.dp)
            ) {
                Text(
                    text = grantLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = primary
                )
            }
        }
    }
}
@Composable
private fun PermissionsStep(
    permissionItems: List<PermissionItem>,
    grantedStates: Map<String, Boolean>,
    onRequestPermission: (PermissionItem) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    StepContainer(
        title = "Give Bluebird the access it needs",
        subtitle = "Nothing is requested silently. Optional access can be skipped and enabled later from Android Settings.",
        icon = SetupFI.Lock
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            permissionItems.forEach { item ->
                PermissionCard(
                    icon = item.icon,
                    title = item.title,
                    desc = item.description,
                    isGranted = grantedStates[item.title] == true,
                    isOptional = item.optional,
                    grantLabel = when {
                        grantedStates[item.title] == true -> "Granted"
                        item.isManageAllFiles -> "Open settings"
                        else -> "Allow"
                    },
                    onGrant = { onRequestPermission(item) }
                )
            }
        }
        PrimaryButton("Continue", SetupFI.ArrowForward, onClick = onNext)
        SecondaryButton("Back", onClick = onBack, modifier = Modifier.fillMaxWidth())
    }
}

// ─────────────────────────────────────────────────────────
// Step 2 — Username
// ─────────────────────────────────────────────────────────

@Composable
private fun UsernameStep(
    currentName: String,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    // Keep in sync if ViewModel updates externally
    LaunchedEffect(currentName) { if (name.isBlank()) name = currentName }
    var isFocused by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    StepContainer(
        title = "What's your name?",
        subtitle = "This appears on your lock screen and home screen greeting. You can change it later in Settings.",
        icon = SetupFI.Person
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Display name",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurface
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(setupPanel(isSystemInDarkTheme()))
                    .border(
                        1.dp,
                        if (isFocused) colorScheme.primary else colorScheme.outlineVariant,
                        RoundedCornerShape(10.dp)
                    )
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = colorScheme.onSurface,
                        unfocusedTextColor = colorScheme.onSurface,
                        cursorColor = colorScheme.primary,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                    placeholder = {
                        Text("e.g. Mirembe Comfort", color = colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    },
                    singleLine = true,
                    leadingIcon = {
                        Icon(SetupFI.Person, null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (name.isNotEmpty()) {
                            Icon(SetupFI.CheckCircle, contentDescription = "Name entered", tint = colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                )
            }

            Text(
                "Can be changed later in Settings",
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryButton(
                text = "Continue",
                icon = SetupFI.ArrowForward,
                enabled = name.isNotBlank(),
                onClick = {
                    onNameChange(name.trim().ifBlank { "User" })
                    onNext()
                }
            )
            SecondaryButton("Back", onClick = onBack, modifier = Modifier.fillMaxWidth())
        }
    }
}

// ─────────────────────────────────────────────────────────
// Step 3 — Avatar
// ─────────────────────────────────────────────────────────

@Composable
private fun AvatarStep(
    context: android.content.Context,
    onAvatarPicked: (Uri) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedUri = it
            onAvatarPicked(it)
        }
    }

    StepContainer(
        title = "Add a profile photo",
        subtitle = "Choose an image from your gallery. Bluebird does not need camera access for your profile picture.",
        icon = SetupFI.Image
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier.size(112.dp).clip(CircleShape)
                    .background(if (selectedUri == null) setupPrimary().copy(alpha = 0.14f) else Color.Transparent)
                    .border(2.dp, if (selectedUri != null) colorScheme.primary else colorScheme.outlineVariant, CircleShape)
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedUri != null) {
                    AsyncImage(model = selectedUri, contentDescription = "Selected profile photo", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(SetupFI.PhotoLibrary, null, tint = setupPrimary(), modifier = Modifier.size(28.dp))
                        Text("Choose image", style = MaterialTheme.typography.labelSmall, color = setupPrimary())
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .background(setupPanel(isSystemInDarkTheme()))
            .border(0.5.dp, colorScheme.outlineVariant, RoundedCornerShape(10.dp))
            .clickable { galleryLauncher.launch("image/*") }
            .padding(vertical = 13.dp), contentAlignment = Alignment.Center) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(SetupFI.PhotoLibrary, null, tint = colorScheme.primary, modifier = Modifier.size(19.dp))
                Text("Choose from gallery", style = MaterialTheme.typography.labelMedium, color = colorScheme.onSurface)
            }
        }
        Spacer(Modifier.height(4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryButton(text = if (selectedUri != null) "Continue" else "Skip for now", icon = if (selectedUri != null) SetupFI.Check else SetupFI.SkipNext, onClick = onNext)
            SecondaryButton("Back", onClick = onBack, modifier = Modifier.fillMaxWidth())
        }
    }
}

// ─────────────────────────────────────────────────────────
// Button Components
// ─────────────────────────────────────────────────────────

@Composable
private fun PrimaryButton(
    text: String,
    icon: ImageVector,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (enabled) setupPrimary() else colorScheme.onSurface.copy(alpha = 0.12f))
            .semantics { role = Role.Button; contentDescription = text }
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) colorScheme.onPrimary else colorScheme.onSurface.copy(alpha = 0.38f)
            )
            Icon(
                icon,
                null,
                tint = if (enabled) colorScheme.onPrimary else colorScheme.onSurface.copy(alpha = 0.38f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(0.5.dp, colorScheme.outline, RoundedCornerShape(10.dp))
            .semantics { role = Role.Button; contentDescription = text }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            color = colorScheme.onSurfaceVariant
        )
    }
}
