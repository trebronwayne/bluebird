package io.github.norbertweb.bluebird.ui.screens

import androidx.compose.ui.res.stringResource
import io.github.norbertweb.bluebird.R
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import fluent.ui.system.icons.FluentIcons
import fluent.ui.system.icons.filled.Home

/**
 * "Set Bluebird as your default launcher" prompt.
 *
 * IMPORTANT — where this gets called from: this is intentionally NOT shown by
 * SetupScreen. It's meant to appear once the user has actually landed on the
 * desktop/home screen after setup finishes, not while they're still looking at
 * the setup flow. Wire it up from your Desktop/Home composable, gated on a flag
 * that your ViewModel sets once (e.g. inside completeSetup()) and this prompt
 * clears via [onDismiss]. Sketch:
 *
 *   val uiState by viewModel.uiState.collectAsState()
 *   // ... your normal desktop content ...
 *   if (uiState.showDefaultLauncherPrompt) {
 *       DefaultLauncherPrompt(onDismiss = { viewModel.dismissDefaultLauncherPrompt() })
 *   }
 *
 * That flag/dismiss function need to exist on LauncherUiState / LauncherViewModel —
 * they aren't added here since this file doesn't have visibility into either.
 */
@Composable
fun DefaultLauncherPrompt(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()
    val panel = if (dark) Color(0xE61F1F1F) else Color(0xE6F8F8F8)
    val border = if (dark) Color.White.copy(alpha = 0.09f) else Color.Black.copy(alpha = 0.08f)
    val primary = Color(0xFF0078D4)
    val textPrimary = if (dark) Color.White else Color(0xFF1A1A1A)
    val textSecondary = if (dark) Color.White.copy(alpha = 0.68f) else Color.Black.copy(alpha = 0.62f)

    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Whatever the user picked in the system dialog/settings page, we're done.
        onDismiss()
    }

    fun requestDefaultLauncherRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            ) {
                roleLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME))
                return
            }
        } else {
            // Pre-Q there's no role picker; send the user to the system "Home app"
            // chooser instead.
            roleLauncher.launch(Intent(Settings.ACTION_HOME_SETTINGS))
            return
        }
        // Already the default, or the role isn't available on this device.
        onDismiss()
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(panel)
                .border(1.dp, border, RoundedCornerShape(18.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(FluentIcons.Filled.Home, null, tint = primary, modifier = Modifier.size(28.dp))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    stringResource(R.string.youre_all_set_up),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    stringResource(R.string.make_bluebird_your_default_launcher_so),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { requestDefaultLauncherRole() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    Text(stringResource(R.string.set_as_default), fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(R.string.maybe_later))
                }
            }
        }
    }
}
