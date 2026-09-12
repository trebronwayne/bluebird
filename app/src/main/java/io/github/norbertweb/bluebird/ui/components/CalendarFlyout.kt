package io.github.norbertweb.bluebird.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.norbertweb.bluebird.ui.theme.LocalIsDarkTheme
import io.github.norbertweb.bluebird.ui.theme.LocalTextScale
import io.github.norbertweb.bluebird.ui.theme.bluebirdColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Standalone calendar flyout. Opened by tapping the clock/date in the
// system tray — separate from Action Center, which now only owns
// wifi/volume/battery quick settings. Shares Action Center's exact
// AcrylicSurface + translucent fill + corner radius, just sized to fit
// nothing but the month grid.
@Composable
fun CalendarFlyout(
    hazeState: dev.chrisbanes.haze.HazeState,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val textScale = LocalTextScale.current
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight

    AcrylicSurface(
        modifier = modifier
            .widthIn(min = 236.dp, max = 264.dp)
            .heightIn(min = 190.dp, max = 230.dp),
        isDark = isDark,
        accent = accent,
        cornerRadius = 12.dp,
        hazeState = hazeState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color(0xE61F1F1F) else Color(0xE6F8F8F8), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 11.dp)
        ) {
            MiniCalendarGrid(
                isDark = isDark,
                textColor = textColor,
                accent = bluebirdColors.AccentBlue,
                textScale = textScale
            )
        }
    }
}

@Composable
private fun MiniCalendarGrid(
    isDark: Boolean,
    textColor: Color,
    accent: Color,
    textScale: Float
) {
    val today = remember { Calendar.getInstance() }
    val monthName = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(today.time) }
    val first = remember {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    val firstDayOffset = remember {
        // Monday-first, matching the compact Windows calendar rhythm.
        ((first.get(Calendar.DAY_OF_WEEK) + 5) % 7)
    }
    val daysInMonth = remember { today.getActualMaximum(Calendar.DAY_OF_MONTH) }
    val weekDays = listOf("M", "T", "W", "T", "F", "S", "S")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(monthName, color = textColor, fontSize = (11f * textScale).sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("Today", color = accent, fontSize = (9f * textScale).sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(day, color = textColor.copy(alpha = 0.42f), fontSize = (8f * textScale).sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        Spacer(Modifier.height(2.dp))

        val totalCells = ((firstDayOffset + daysInMonth + 6) / 7) * 7
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            repeat(totalCells / 7) { rowIndex ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(7) { colIndex ->
                        val cell = rowIndex * 7 + colIndex
                        val day = cell - firstDayOffset + 1
                        val isCurrent = day == today.get(Calendar.DAY_OF_MONTH) && day in 1..daysInMonth
                        Box(
                            modifier = Modifier.weight(1f).height(19.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day in 1..daysInMonth) {
                                Box(
                                    modifier = if (isCurrent) Modifier.size(17.dp).clip(CircleShape).background(accent) else Modifier.size(17.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        day.toString(),
                                        color = if (isCurrent) Color.White else textColor.copy(alpha = 0.78f),
                                        fontSize = (8.5f * textScale).sp,
                                        fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
