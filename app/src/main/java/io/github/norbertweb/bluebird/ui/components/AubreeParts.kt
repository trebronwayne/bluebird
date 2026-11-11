package io.github.norbertweb.bluebird.ui.components

import kotlinx.coroutines.delay
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.trebronwayne.aubree.AubreeState
import com.trebronwayne.aubree.compose.Aubree
import io.github.norbertweb.bluebird.i18n.ui
import io.github.norbertweb.bluebird.ui.theme.bluebirdColors

/**
 * The one-time "Meet Aubree" welcome. Shown the first time the desktop appears after Aubree arrives,
 * so it covers both new installs and people who update.
 *
 * The three sentences go through ui(): until they are added to the translation files they show in
 * English in every language, which is how ui() treats text it does not know.
 */
@Composable
fun MeetAubreeDialog(isDark: Boolean, onDone: () -> Unit) {
    val text = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 360.dp),
            shape = RoundedCornerShape(12.dp),
            color = if (isDark) Color(0xFF2B2B2B) else Color(0xFFF9F9F9),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Aubree(state = AubreeState.WELCOME, size = 180.dp)
                Text(
                    ui("Meet Aubree \uD83E\uDD79"),
                    color = text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    ui("Aubree is Bluebird\u2019s new companion. She waits with you, cheers when things go well, and keeps you company when a screen is empty."),
                    color = text.copy(alpha = 0.72f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onDone,
                    colors = ButtonDefaults.buttonColors(containerColor = bluebirdColors.AccentBlue, contentColor = Color.White)
                ) {
                    Text(ui("Say hi"))
                }
            }
        }
    }
}

/** Lives as long as the app process: tells whether this start has already been looked at. */
object WelcomeBackState {
    @Volatile var handled = false
}

/**
 * The small "Welcome back" card that slides down from the top when Bluebird starts fresh. [trigger] is a timestamp: each new value shows the card again. It hides itself after a few seconds,
 * or when tapped. "Welcome back" goes through ui(), so it is translated wherever the text already exists.
 */
@Composable
fun WelcomeBackCard(trigger: Long, enabled: Boolean, isDark: Boolean) {
    val text = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight
    var shown by remember { mutableStateOf(false) }
    var greeting by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf(AubreeState.WELCOME) }

    LaunchedEffect(trigger) {
        if (trigger == 0L || !enabled) return@LaunchedEffect
        val pick = TimeGreetings.pick()
        greeting = pick.text
        mood = pick.aubree
        shown = true
        delay(4500)
        shown = false
    }

    Box(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(top = 12.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = shown,
            enter = slideInVertically { -it - 48 } + fadeIn(),
            exit = slideOutVertically { -it - 48 } + fadeOut()
        ) {
            AcrylicSurface(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .widthIn(max = 340.dp)
                    .clickable { shown = false },
                isDark = isDark,
                cornerRadius = 16.dp
            ) {
                Row(
                    modifier = Modifier.padding(start = 10.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Aubree(state = mood, size = 52.dp, contentDescription = "")
                    Column {
                        Text(ui("Welcome back"), color = text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(ui(greeting), color = text.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
