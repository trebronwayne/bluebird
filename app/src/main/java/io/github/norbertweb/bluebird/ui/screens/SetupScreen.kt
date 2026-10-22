package io.github.norbertweb.bluebird.ui.screens

import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.res.stringResource
import io.github.norbertweb.bluebird.R
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
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
import io.github.norbertweb.bluebird.ui.components.DS
import io.github.norbertweb.bluebird.ui.theme.bluebirdColors
import io.github.norbertweb.bluebird.i18n.BluebirdLanguages
import io.github.norbertweb.bluebird.i18n.BluebirdLanguage
import java.util.Locale
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay

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

private data class StepMeta(val icon: ImageVector, val labelRes: Int, val subtitleRes: Int)

// Setup step indices: 0 Language · 1 Welcome · 2 Permissions · 3 Name · 4 Photo.
// BUG FIX: the auto-request guard used a hard-coded `1` from before the Language step was added,
// so system permission dialogs started firing on the WELCOME screen, before the user ever
// reached the permissions screen. Always compare against this constant.
private const val PERMISSIONS_STEP = 2

private val stepMeta = listOf(
    StepMeta(SetupFI.Settings, R.string.setup_step_language, R.string.setup_step_language_subtitle),
    StepMeta(SetupFI.Home, R.string.setup_step_welcome, R.string.setup_step_welcome_subtitle),
    StepMeta(SetupFI.Lock, R.string.setup_step_access, R.string.setup_step_access_subtitle),
    StepMeta(SetupFI.Person, R.string.setup_step_profile, R.string.setup_step_profile_subtitle),
    StepMeta(SetupFI.Image, R.string.setup_step_finish, R.string.setup_step_finish_subtitle)
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
    val step = uiState.setupStep.coerceIn(0, LauncherViewModel.SETUP_LAST_STEP)
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.smallestScreenWidthDp >= 600

    var setupCompleted by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf<PermissionItem?>(null) }

    // Resolve localized permission copy in composition before constructing the permission list.
    val mediaTitle = stringResource(R.string.setup_permission_media_title)
    val mediaDescription = stringResource(R.string.setup_permission_media_desc)
    val notificationsTitle = stringResource(R.string.setup_permission_notifications_title)
    val notificationsDescription = stringResource(R.string.setup_permission_notifications_desc)
    val filesTitle = stringResource(R.string.setup_permission_files_title)
    val filesDescription = stringResource(R.string.setup_permission_files_desc)

    // Only permissions used by real Bluebird functionality are presented here.
    val permissionItems = remember(mediaTitle, mediaDescription, notificationsTitle, notificationsDescription, filesTitle, filesDescription) {
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
                    title = mediaTitle,
                    description = mediaDescription,
                    icon = SetupFI.FolderOpen,
                    permissions = mediaPerms,
                    optional = true
                )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(
                    PermissionItem(
                        title = notificationsTitle,
                        description = notificationsDescription,
                        icon = SetupFI.Notifications,
                        permissions = listOf(Manifest.permission.POST_NOTIFICATIONS),
                        optional = true
                    )
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add(
                    PermissionItem(
                        title = filesTitle,
                        description = filesDescription,
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

    // NOTE ON PERMISSION TIMING: this effect only *reads* current grant state via
    // ContextCompat.checkSelfPermission — it never calls permLauncher.launch() or
    // otherwise triggers a system permission dialog. Permissions are only ever
    // requested below, either by the auto-request sequence once the user reaches
    // PermissionsStep (PERMISSIONS_STEP), or by an explicit tap on a PermissionCard — and
    // that step is only reachable after Language (0) and Welcome (1). So no permission prompt
    // can appear before the user has actually landed on the permissions screen.
    LaunchedEffect(permissionItems) {
        permissionItems.forEach(::refreshPermissionState)
    }

    var requestedItem by remember { mutableStateOf<PermissionItem?>(null) }

    // Set only while the auto-request sequence (below) is waiting on a result for
    // `requestedItem`. The launcher callback resolves it instead of opening the
    // rationale dialog, so a denial during the sequence doesn't interrupt the flow.
    var autoRequestDeferred by remember { mutableStateOf<CompletableDeferred<Boolean>?>(null) }

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
            val deferred = autoRequestDeferred
            if (deferred != null) {
                autoRequestDeferred = null
                deferred.complete(granted)
            } else if (!granted && item.permissions.isNotEmpty()) {
                showPermissionRationale = item
            }
        }
        requestedItem = null
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        permissionItems.filter { it.requiresSettingsIntent }.forEach(::refreshPermissionState)
    }

    // Which permission card (if any) is currently showing the "requesting" spinner —
    // drives the sequential auto-request flow below and the UI in PermissionCard.
    var autoRequestingItem by remember { mutableStateOf<PermissionItem?>(null) }

    // Auto-drives the permission requests one at a time, the way most onboarding
    // flows do, instead of making the user tap "Allow" on every card individually.
    // Only runs while the user is actually on PermissionsStep (PERMISSIONS_STEP) — never
    // earlier. "All files access" is deliberately excluded: it opens a Settings
    // page rather than a runtime dialog, so it stays a manual, explicit tap.
    LaunchedEffect(step, permissionItems) {
        if (step != PERMISSIONS_STEP) {
            autoRequestingItem = null
            return@LaunchedEffect
        }
        val queue = permissionItems.filter { !it.isManageAllFiles && it.permissions.isNotEmpty() }
        for (item in queue) {
            if (grantedStates[item.title] == true) continue
            autoRequestingItem = item
            delay(1000) // circling indicator — gives the user a moment to read the card first
            if (grantedStates[item.title] == true) continue // granted manually while we waited
            val deferred = CompletableDeferred<Boolean>()
            autoRequestDeferred = deferred
            requestedItem = item
            permLauncher.launch(item.permissions.toTypedArray())
            deferred.await()
        }
        autoRequestingItem = null
    }

    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    val panel = setupPanel(isDark)
    val panelBorder = setupBorder(isDark)
    val primary = setupPrimary()
    val textPrimary = setupTextPrimary(isDark)
    val textSecondary = setupTextSecondary(isDark)
    val rootBackground = panel

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
                            autoRequestingItem = autoRequestingItem,
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
                            autoRequestingItem = autoRequestingItem,
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
                title = { Text(stringResource(R.string.setup_permission_needed, item.title)) },
                text = {
                    Text(
                        if (item.optional)
                            stringResource(R.string.setup_optional_permission_desc, item.title.lowercase())
                        else
                            stringResource(R.string.setup_required_permission_desc, item.title),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showPermissionRationale = null
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        })
                    }) { Text(stringResource(R.string.setup_open_settings)) }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionRationale = null }) { Text(stringResource(R.string.setup_dismiss)) }
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
    autoRequestingItem: PermissionItem?,
    onRequestPermission: (PermissionItem) -> Unit,
    onBack: () -> Unit,
    onSetupCompleted: () -> Unit
) {
    val context = LocalContext.current

    // Step switches instantly now — was AnimatedContent with a 320ms
    // slide+fade transition, out of step with the rest of the app having
    // its motion removed everywhere else.
    when (step) {
        // Language is intentionally the first-run screen so users can read setup
        // in their preferred language before reaching the welcome and permission steps.
        0 -> LanguageSelectionStep(
            onNext = { viewModel.advanceSetupStep() },
            onBack = onBack,
            showBack = false
        )
        1 -> WelcomeStep(onNext = { viewModel.advanceSetupStep() })
        2 -> PermissionsStep(
            permissionItems = permissionItems,
            grantedStates = grantedStates,
            autoRequestingItem = autoRequestingItem,
            onRequestPermission = onRequestPermission,
            onNext = { viewModel.advanceSetupStep() },
            onBack = onBack
        )
        3 -> UsernameStep(
            currentName = uiState.userProfile.userName,
            onNameChange = { viewModel.setUserName(it) },
            onNext = { viewModel.advanceSetupStep() },
            onBack = onBack
        )
        4 -> AvatarStep(
            context = context,
            onAvatarPicked = { viewModel.setProfilePicture(context, it) },
            onNext = { onSetupCompleted() },
            onBack = onBack
        )
        else -> LaunchedEffect(Unit) { onSetupCompleted() }
    }
}

private fun setupPanel(isDark: Boolean): Color = if (isDark) DS.surfaceDark else DS.surfaceLight
private fun setupBorder(isDark: Boolean): Color = if (isDark) DS.borderDark else DS.borderLight
private fun setupPrimary(): Color = DS.accentStart
private fun setupTextPrimary(isDark: Boolean): Color = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
private fun setupTextSecondary(isDark: Boolean): Color = setupTextPrimary(isDark).copy(alpha = 0.65f)

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
                    Text(stringResource(R.string.setup_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = textPrimary)
                    Text(stringResource(stepMeta[currentStep].labelRes), style = MaterialTheme.typography.labelSmall, color = textSecondary)
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
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = textPrimary)
                    Text(stringResource(R.string.setup_title), style = MaterialTheme.typography.labelSmall, color = textSecondary)
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
            Text(stringResource(R.string.bluebird), style = MaterialTheme.typography.labelSmall, color = textPrimary)
            Text(stringResource(R.string.setup_brand_subtitle), style = MaterialTheme.typography.labelSmall, color = textSecondary)
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
            Text(stringResource(meta.labelRes), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = if (state == StepState.UPCOMING) textSecondary else textPrimary)
            Text(stringResource(meta.subtitleRes), style = MaterialTheme.typography.labelSmall, color = textSecondary)
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
private fun LanguageSelectionStep(onNext: () -> Unit, onBack: () -> Unit, showBack: Boolean = true) {
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()
    val textPrimary = setupTextPrimary(dark)
    val textSecondary = setupTextSecondary(dark)
    val panel = setupPanel(dark)
    val border = setupBorder(dark)
    val savedTag = BluebirdLanguages.selectedTag(context)
    var selectedTag by remember(savedTag) { mutableStateOf(savedTag ?: "system") }
    var query by rememberSaveable { mutableStateOf("") }

    // Apply the language IMMEDIATELY on tap instead of waiting for "Continue": persist it and
    // recreate the activity so the whole wizard re-renders in the chosen language right now.
    // The ViewModel survives recreation, so the user stays on this step. No-op if unchanged.
    fun applyLanguage(tag: String) {
        selectedTag = tag
        val newTag = tag.takeUnless { it == "system" }
        if (newTag != BluebirdLanguages.selectedTag(context)) {
            BluebirdLanguages.saveSelection(context, newTag)
            context.findSetupActivity()?.recreate()
        }
    }
    val filteredLanguages = remember(query) {
        BluebirdLanguages.all.filter { language ->
            language.nativeName.contains(query, ignoreCase = true) ||
                language.englishName.contains(query, ignoreCase = true) ||
                language.tag.contains(query, ignoreCase = true)
        }
    }
    val systemLocale = Locale.getDefault()
    val systemLanguageName = systemLocale.getDisplayName(systemLocale).replaceFirstChar { it.uppercase(systemLocale) }

    StepContainer(
        title = stringResource(R.string.setup_language_title),
        subtitle = stringResource(R.string.setup_language_subtitle),
        icon = SetupFI.Settings
    ) {
        Text(
            stringResource(R.string.setup_language_system_hint, systemLanguageName),
            style = MaterialTheme.typography.bodyMedium,
            color = textSecondary
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(R.string.setup_language_search)) }
        )
        Column(
            modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LanguageSetupOption(
                title = stringResource(R.string.language_system_default),
                subtitle = stringResource(R.string.setup_language_system_auto, systemLanguageName),
                selected = selectedTag == "system",
                panel = panel,
                border = border,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                onClick = { applyLanguage("system") }
            )
            filteredLanguages.forEach { language ->
                LanguageSetupOption(
                    title = language.nativeName,
                    subtitle = language.englishName,
                    selected = selectedTag == language.tag,
                    panel = panel,
                    border = border,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    onClick = { applyLanguage(language.tag) }
                )
            }
            if (filteredLanguages.isEmpty()) {
                Text(
                    stringResource(R.string.language_no_results),
                    modifier = Modifier.padding(12.dp),
                    color = textSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (showBack) {
                TextButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.setup_back))
                }
            }
            PrimaryButton(
                text = stringResource(R.string.setup_language_continue),
                icon = SetupFI.ArrowForward,
                onClick = {
                    // Already applied on tap; Continue just moves on.
                    BluebirdLanguages.saveSelection(context, selectedTag.takeUnless { it == "system" })
                    onNext()
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LanguageSetupOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    panel: Color,
    border: Color,
    textPrimary: Color,
    textSecondary: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .background(if (selected) setupPrimary().copy(alpha = 0.10f) else panel)
            .border(if (selected) 1.dp else 0.7.dp, if (selected) setupPrimary().copy(alpha = 0.65f) else border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = textPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
            Text(subtitle, color = textSecondary, style = MaterialTheme.typography.bodySmall)
        }
        RadioButton(selected = selected, onClick = onClick)
    }
}

private tailrec fun android.content.Context.findSetupActivity(): android.app.Activity? = when (this) {
    is android.app.Activity -> this
    is android.content.ContextWrapper -> baseContext.findSetupActivity()
    else -> null
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    val dark = isSystemInDarkTheme()
    val panel = setupPanel(dark)
    val border = setupBorder(dark)
    val textPrimary = setupTextPrimary(dark)
    val textSecondary = setupTextSecondary(dark)
    StepContainer(
        title = stringResource(R.string.setup_welcome_title),
        subtitle = stringResource(R.string.setup_welcome_subtitle),
        icon = SetupFI.Home
    ) {
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(panel).border(1.dp,border,RoundedCornerShape(14.dp)).padding(18.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(stringResource(R.string.setup_get_ready), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = textPrimary)
                Text(stringResource(R.string.setup_control_permissions), style = MaterialTheme.typography.bodySmall, color = textSecondary, lineHeight = 19.sp)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        SetupFI.Dashboard to stringResource(R.string.setup_feature_desktop),
                        SetupFI.FolderOpen to stringResource(R.string.setup_feature_media),
                        SetupFI.Person to stringResource(R.string.setup_feature_profile)
                    ).forEach { (icon, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(icon, null, tint = setupPrimary(), modifier = Modifier.size(18.dp))
                            Text(label, style = MaterialTheme.typography.bodySmall, color = textPrimary)
                        }
                    }
                }
            }
        }
        PrimaryButton(stringResource(R.string.setup_get_started), SetupFI.ArrowForward, onClick = onNext)
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
    isRequesting: Boolean,
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

            if (isRequesting) {
                Text(
                    text = stringResource(R.string.setup_requesting),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = primary,
                    letterSpacing = 0.2.sp
                )
            } else if (isOptional) {
                Text(
                    text = stringResource(R.string.setup_optional),
                    style = MaterialTheme.typography.labelSmall,
                    color = textSecondary
                )
            }
        }

        if (isGranted) {
            Icon(
                imageVector = SetupFI.CheckCircle,
                contentDescription = stringResource(R.string.setup_granted),
                tint = primary,
                modifier = Modifier.size(22.dp)
            )
        } else if (isRequesting) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = primary
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
    autoRequestingItem: PermissionItem?,
    onRequestPermission: (PermissionItem) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    StepContainer(
        title = stringResource(R.string.setup_access_title),
        subtitle = stringResource(R.string.setup_access_subtitle),
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
                    isRequesting = autoRequestingItem?.title == item.title,
                    grantLabel = when {
                        grantedStates[item.title] == true -> stringResource(R.string.setup_granted)
                        item.isManageAllFiles -> stringResource(R.string.setup_open_settings_short)
                        else -> stringResource(R.string.setup_allow)
                    },
                    onGrant = {
                        // While the auto-sequence is driving requests, block manual
                        // taps on runtime-permission cards so we never have two
                        // concurrent permLauncher.launch() calls in flight. The
                        // settings-intent item (All files access) isn't part of the
                        // sequence, so it stays tappable at any time.
                        if (item.isManageAllFiles || autoRequestingItem == null) {
                            onRequestPermission(item)
                        }
                    }
                )
            }
        }
        PrimaryButton(stringResource(R.string.setup_continue), SetupFI.ArrowForward, onClick = onNext)
        SecondaryButton(stringResource(R.string.setup_back), onClick = onBack, modifier = Modifier.fillMaxWidth())
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
        title = stringResource(R.string.setup_name_title),
        subtitle = stringResource(R.string.setup_name_subtitle),
        icon = SetupFI.Person
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                stringResource(R.string.setup_display_name),
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
                        Text(stringResource(R.string.setup_name_example), color = colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    },
                    singleLine = true,
                    leadingIcon = {
                        Icon(SetupFI.Person, null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (name.isNotEmpty()) {
                            Icon(SetupFI.CheckCircle, contentDescription = stringResource(R.string.setup_name_entered), tint = colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                )
            }

            Text(
                stringResource(R.string.setup_name_later),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryButton(
                text = stringResource(R.string.setup_continue),
                icon = SetupFI.ArrowForward,
                enabled = name.isNotBlank(),
                onClick = {
                    onNameChange(name.trim().ifBlank { "User" })
                    onNext()
                }
            )
            SecondaryButton(stringResource(R.string.setup_back), onClick = onBack, modifier = Modifier.fillMaxWidth())
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
        title = stringResource(R.string.setup_photo_title),
        subtitle = stringResource(R.string.setup_photo_subtitle),
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
                    AsyncImage(model = selectedUri, contentDescription = stringResource(R.string.setup_photo_selected), modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(SetupFI.PhotoLibrary, null, tint = setupPrimary(), modifier = Modifier.size(28.dp))
                        Text(stringResource(R.string.setup_choose_image), style = MaterialTheme.typography.labelSmall, color = setupPrimary())
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
                Text(stringResource(R.string.setup_choose_gallery), style = MaterialTheme.typography.labelMedium, color = colorScheme.onSurface)
            }
        }
        Spacer(Modifier.height(4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryButton(text = if (selectedUri != null) stringResource(R.string.setup_continue) else stringResource(R.string.setup_skip_now), icon = if (selectedUri != null) SetupFI.Check else SetupFI.SkipNext, onClick = onNext)
            SecondaryButton(stringResource(R.string.setup_back), onClick = onBack, modifier = Modifier.fillMaxWidth())
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
