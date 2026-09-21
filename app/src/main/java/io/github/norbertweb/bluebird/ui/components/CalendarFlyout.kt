package io.github.norbertweb.bluebird.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.norbertweb.bluebird.ui.theme.LocalIsDarkTheme
import io.github.norbertweb.bluebird.ui.theme.LocalTextScale
import io.github.norbertweb.bluebird.ui.theme.bluebirdColors
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Standalone calendar flyout. Opened by tapping the clock/date in the
// system tray — separate from Action Center, which only owns wifi/volume/
// brightness quick settings. One consistent panel: a header with today's
// full date, a body with a month you can page through (any month, any
// year), and a footer with a live, seconds-accurate clock.
@Composable
fun CalendarFlyout(
    hazeState: dev.chrisbanes.haze.HazeState,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val textScale = LocalTextScale.current
    val textColor = if (isDark) bluebirdColors.TextPrimary else bluebirdColors.TextPrimaryLight

    // Ticks once a second. Drives the header's date and the footer's clock,
    // and is also the "what day is today" reference for the grid highlight.
    var now by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = Calendar.getInstance()
            delay(1000L)
        }
    }

    // The month/year currently paged to in the grid. Independent of `now`
    // so browsing to another month (or year — add() rolls over correctly
    // in either direction) never disturbs the live clock above.
    var displayed by remember { mutableStateOf(Calendar.getInstance()) }

    // One consistent, mostly-opaque panel - the same Acrylic tint Start
    // Menu, Action Center and the Power Menu all use - instead of three
    // separately tinted bands. The header/footer used to sit at 2-5%
    // opacity, so almost all of the live blur showed through there while
    // the body (at ~90% opaque) read as solid; that mismatch is what made
    // it look glassy and cheap next to the others. Now the whole card is
    // one solid surface, and hairline dividers do the section separation
    // instead of swinging opacity up and down.
    val panelBg = if (isDark) Color(0xE61F1F1F) else Color(0xE6F8F8F8)
    val dividerColor = if (isDark) DS.borderDark else DS.borderLight
    val headerTint = accent.copy(alpha = if (isDark) 0.10f else 0.06f)

    AcrylicSurface(
        modifier = modifier
            .widthIn(min = 252.dp, max = 296.dp)
            .wrapContentHeight(),
        isDark = isDark,
        accent = accent,
        cornerRadius = 12.dp,
        hazeState = hazeState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(12.dp))
                .background(panelBg)
        ) {
            CalendarHeader(
                now = now,
                textColor = textColor,
                textScale = textScale,
                background = headerTint
            )

            HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

            val isViewingCurrentMonth = displayed.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                displayed.get(Calendar.MONTH) == now.get(Calendar.MONTH)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                MonthNavRow(
                    displayed = displayed,
                    textColor = textColor,
                    textScale = textScale,
                    onPrev = { displayed = (displayed.clone() as Calendar).apply { add(Calendar.MONTH, -1) } },
                    onNext = { displayed = (displayed.clone() as Calendar).apply { add(Calendar.MONTH, 1) } }
                )
                if (!isViewingCurrentMonth) {
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(accent.copy(alpha = if (isDark) 0.16f else 0.10f))
                            .clickable { displayed = Calendar.getInstance() }
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Jump to today",
                            color = bluebirdColors.AccentBlue,
                            fontSize = (8.5f * textScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                MiniCalendarGrid(
                    displayed = displayed,
                    today = now,
                    textColor = textColor,
                    accent = bluebirdColors.AccentBlue,
                    textScale = textScale
                )
            }

            HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

            CalendarFooter(
                now = now,
                textColor = textColor,
                textScale = textScale
            )
        }
    }
}

@Composable
private fun CalendarHeader(
    now: Calendar,
    textColor: Color,
    textScale: Float,
    background: Color
) {
    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        // A touch lighter than the rest of the flyout's bold-everywhere
        // type, plus a hair of tracking, so the date reads as a header
        // instead of blending into the nav row and grid below it.
        Text(
            dateFormat.format(now.time),
            color = textColor,
            fontSize = (12.5f * textScale).sp,
            fontWeight = FontWeight.SemiBold,
            style = TextStyle(letterSpacing = 0.2.sp)
        )
    }
}

@Composable
private fun CalendarFooter(
    now: Calendar,
    textColor: Color,
    textScale: Float
) {
    // h:mm carries the eye; seconds + AM/PM are real but secondary, so they
    // sit a step dimmer instead of matching weight with the main time.
    val mainFormat = remember { SimpleDateFormat("h:mm", Locale.getDefault()) }
    val suffixFormat = remember { SimpleDateFormat(":ss a", Locale.getDefault()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            mainFormat.format(now.time),
            color = textColor,
            fontSize = (11.5f * textScale).sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            suffixFormat.format(now.time),
            color = textColor.copy(alpha = 0.55f),
            fontSize = (9.5f * textScale).sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 0.5.dp)
        )
    }
}

@Composable
private fun MonthNavRow(
    displayed: Calendar,
    textColor: Color,
    textScale: Float,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val monthYear = remember(displayed.get(Calendar.MONTH), displayed.get(Calendar.YEAR)) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(displayed.time)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MonthNavArrow(icon = FluentIcon.ChevronLeft, contentDescription = "Previous month", textColor = textColor, onClick = onPrev)
        Text(monthYear, color = textColor, fontSize = (11f * textScale).sp, fontWeight = FontWeight.Bold)
        MonthNavArrow(icon = FluentIcon.ChevronRight, contentDescription = "Next month", textColor = textColor, onClick = onNext)
    }
}

@Composable
private fun MonthNavArrow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(textColor.copy(alpha = 0.08f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = textColor, modifier = Modifier.size(13.dp))
    }
}

@Composable
private fun MiniCalendarGrid(
    displayed: Calendar,
    today: Calendar,
    textColor: Color,
    accent: Color,
    textScale: Float
) {
    val year = displayed.get(Calendar.YEAR)
    val month = displayed.get(Calendar.MONTH)

    val first = remember(year, month) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    val firstDayOffset = remember(year, month) {
        // Monday-first, matching the compact Windows calendar rhythm.
        ((first.get(Calendar.DAY_OF_WEEK) + 5) % 7)
    }
    val daysInMonth = remember(year, month) { first.getActualMaximum(Calendar.DAY_OF_MONTH) }
    val weekDays = listOf("M", "T", "W", "T", "F", "S", "S")

    val isCurrentMonth = year == today.get(Calendar.YEAR) && month == today.get(Calendar.MONTH)
    val todayDayOfMonth = today.get(Calendar.DAY_OF_MONTH)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEachIndexed { index, day ->
                val isWeekend = index == 5 || index == 6
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        day,
                        color = if (isWeekend) accent.copy(alpha = 0.75f) else textColor.copy(alpha = 0.55f),
                        fontSize = (8f * textScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))

        val totalCells = ((firstDayOffset + daysInMonth + 6) / 7) * 7
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(totalCells / 7) { rowIndex ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(7) { colIndex ->
                        val cell = rowIndex * 7 + colIndex
                        val day = cell - firstDayOffset + 1
                        val isCurrent = isCurrentMonth && day == todayDayOfMonth && day in 1..daysInMonth
                        val isWeekend = colIndex == 5 || colIndex == 6
                        Box(
                            modifier = Modifier.weight(1f).height(19.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day in 1..daysInMonth) {
                                Box(
                                    modifier = if (isCurrent) {
                                        Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(accent)
                                            .border(1.dp, accent.copy(alpha = 0.55f), CircleShape)
                                    } else {
                                        Modifier.size(18.dp)
                                    },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        day.toString(),
                                        color = when {
                                            isCurrent -> Color.White
                                            isWeekend -> accent.copy(alpha = 0.80f)
                                            else -> textColor.copy(alpha = 0.82f)
                                        },
                                        fontSize = (8.5f * textScale).sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold
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
