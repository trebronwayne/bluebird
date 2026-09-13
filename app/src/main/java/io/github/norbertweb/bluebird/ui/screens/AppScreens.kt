package io.github.norbertweb.bluebird.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import io.github.norbertweb.bluebird.ui.components.FluentIcon
import kotlin.math.abs

// ─────────────────────────────────────────────────────────────────────────────
//  DESIGN TOKENS  (Windows 11 / Fluent-inspired compact palette)
// ─────────────────────────────────────────────────────────────────────────────

private object Mac {
    val surfaceDark   = Color(0xFF202020)
    val surfaceLight  = Color(0xFFF3F3F3)
    val panelDark     = Color(0xFF2B2B2B)
    val panelLight    = Color(0xFFFFFFFF)
    val sidebarDark   = Color(0xFF252525)
    val sidebarLight  = Color(0xFFF7F7F7)

    val borderDark    = Color(0x22FFFFFF)
    val borderLight   = Color(0x33000000)

    val blue          = Color(0xFF007AFF)
    val green         = Color(0xFF107C10)
    val orange        = Color(0xFFD77800)
    val red           = Color(0xFFC42B1C)
    val yellow        = Color(0xFFFFC83D)
    val purple        = Color(0xFF8764B8)
    val pink          = Color(0xFFC239B3)
    val teal          = Color(0xFF3AA8F0)

    val textPrimaryDark    = Color(0xFFFFFFFF)
    val textPrimaryLight   = Color(0xFF252525)
    val textSecondaryDark  = Color(0xFF8E8E93)
    val textSecondaryLight = Color(0xFF6C6C70)
}

// Screens narrower than this get the compact/mobile layout; at or above it
// they get the roomier, desktop-like multi-pane layout.
private val WIDE_LAYOUT_BREAKPOINT = 640.dp

// ─────────────────────────────────────────────────────────────────────────────
//  SHARED COMPOSABLES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GlassCard(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val bg = if (isDark) Color(0xFF2C2C2E) else Color(0xFFFFFFFF)
    val border = if (isDark) Mac.borderDark else Mac.borderLight
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(bg)
            .border(0.5.dp, border, RoundedCornerShape(cornerRadius)),
        content = content
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  CALCULATOR  — simple, correct, mobile-first with a roomier desktop layout
// ─────────────────────────────────────────────────────────────────────────────

/** Formats a Double for display: whole numbers with no trailing ".0", otherwise trimmed decimals. */
private fun formatCalcValue(value: Double): String {
    if (value.isNaN()) return "Error"
    if (value.isInfinite()) return if (value > 0) "∞" else "-∞"
    return if (value == value.toLong().toDouble() && abs(value) < 1e15) {
        value.toLong().toString()
    } else {
        // Enough precision to feel accurate, trimmed of trailing zeros.
        var s = "%.9f".format(value)
        s = s.trimEnd('0').trimEnd('.')
        s
    }
}

private fun applyCalcOp(a: Double, op: String, b: Double): Double = when (op) {
    "+" -> a + b
    "−" -> a - b
    "×" -> a * b
    "÷" -> if (b != 0.0) a / b else Double.NaN
    else -> b
}

/**
 * Minimal, deliberately simple calculator engine.
 *
 * The key fix vs. the old implementation: the running total lives in its own
 * nullable `accumulator` field, completely separate from what's on screen.
 * An operator button only ever folds the *previous* pending operation into
 * the accumulator, then stores the new operator — it never re-reads a
 * display value that's already been consumed. That's what made chained
 * operations (e.g. 2 + 3 + 4, or repeated ×) drop everything after the
 * first step before.
 */
private class CalcEngine {
    var display by mutableStateOf("0")
        private set
    var expression by mutableStateOf("")
        private set
    var history by mutableStateOf(listOf<Pair<String, String>>())
        private set

    private var accumulator: Double? = null
    private var pendingOp: String? = null
    private var lastOperand: Double? = null
    private var lastOp: String? = null
    private var awaitingNewNumber = true
    private var justEvaluated = false

    private fun currentValue(): Double = display.toDoubleOrNull() ?: 0.0

    fun inputDigit(d: String) {
        if (justEvaluated) {
            display = "0"; expression = ""; justEvaluated = false
        }
        display = when {
            awaitingNewNumber -> if (d == "0") "0" else d
            display == "0" -> d
            else -> display + d
        }
        awaitingNewNumber = false
    }

    fun inputDecimal() {
        if (justEvaluated) {
            display = "0"; expression = ""; justEvaluated = false
        }
        if (awaitingNewNumber) {
            display = "0."
            awaitingNewNumber = false
        } else if (!display.contains(".")) {
            display += "."
        }
    }

    fun clearAll() {
        display = "0"; expression = ""
        accumulator = null; pendingOp = null
        lastOperand = null; lastOp = null
        awaitingNewNumber = true; justEvaluated = false
    }

    fun backspace() {
        if (awaitingNewNumber) return
        display = if (display.length <= 1 || (display.length == 2 && display.startsWith("-"))) {
            "0"
        } else {
            display.dropLast(1)
        }
        if (display.isEmpty()) display = "0"
    }

    fun toggleSign() {
        display = when {
            display == "0" -> "0"
            display.startsWith("-") -> display.substring(1)
            else -> "-$display"
        }
    }

    fun percent() {
        display = formatCalcValue(currentValue() / 100.0)
        awaitingNewNumber = true
    }

    fun setOperator(op: String) {
        val current = currentValue()
        if (accumulator != null && pendingOp != null && !awaitingNewNumber) {
            val result = applyCalcOp(accumulator!!, pendingOp!!, current)
            accumulator = result
            display = formatCalcValue(result)
        } else {
            accumulator = current
        }
        pendingOp = op
        expression = "${formatCalcValue(accumulator!!)} $op"
        awaitingNewNumber = true
        justEvaluated = false
    }

    fun equals() {
        val current = currentValue()
        val op = pendingOp
        val acc = accumulator

        val result: Double
        val exprText: String

        if (op != null && acc != null) {
            result = applyCalcOp(acc, op, current)
            exprText = "${formatCalcValue(acc)} $op ${formatCalcValue(current)} ="
            lastOp = op
            lastOperand = current
        } else if (lastOp != null && lastOperand != null) {
            // Pressing "=" again repeats the last operation, like most
            // calculators do — e.g. 5 =  →  10  →  15 ...
            result = applyCalcOp(current, lastOp!!, lastOperand!!)
            exprText = "${formatCalcValue(current)} $lastOp ${formatCalcValue(lastOperand!!)} ="
        } else {
            return
        }

        history = (listOf(exprText to formatCalcValue(result)) + history).take(20)
        expression = exprText
        display = formatCalcValue(result)
        accumulator = null
        pendingOp = null
        awaitingNewNumber = true
        justEvaluated = true
    }

    fun clearHistory() {
        history = emptyList()
    }
}

@Composable
fun CalculatorScreen(isDark: Boolean) {
    val bg     = if (isDark) Mac.surfaceDark  else Mac.surfaceLight
    val panel  = if (isDark) Mac.panelDark    else Mac.panelLight
    val text   = if (isDark) Mac.textPrimaryDark else Mac.textPrimaryLight
    val subtle = if (isDark) Mac.textSecondaryDark else Mac.textSecondaryLight

    val engine = remember { CalcEngine() }
    var showHistory by remember { mutableStateOf(false) }

    fun handleInput(value: String) {
        when (value) {
            "AC" -> engine.clearAll()
            "⌫"  -> engine.backspace()
            "+/-" -> engine.toggleSign()
            "%"  -> engine.percent()
            "+", "−", "×", "÷" -> engine.setOperator(value)
            "=" -> engine.equals()
            "." -> engine.inputDecimal()
            else -> engine.inputDigit(value)
        }
    }

    val fontSize = when {
        engine.display.length > 12 -> 32.sp
        engine.display.length > 9  -> 42.sp
        engine.display.length > 6  -> 52.sp
        else                       -> 64.sp
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(bg)) {
        val isWide = maxWidth >= WIDE_LAYOUT_BREAKPOINT

        if (isWide) {
            Row(Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = showHistory,
                    enter = slideInHorizontally() + fadeIn(),
                    exit = slideOutHorizontally() + fadeOut()
                ) {
                    CalcHistoryPanel(
                        isDark = isDark,
                        history = engine.history,
                        onClear = { engine.clearHistory() },
                        modifier = Modifier.width(220.dp).fillMaxHeight()
                    )
                }

                // Centered calculator card so it doesn't stretch awkwardly on
                // wide/desktop-like screens.
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier
                            .width(380.dp)
                            .fillMaxHeight(0.92f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(panel)
                    ) {
                        CalcToolbar(isDark, subtle, showHistory) { showHistory = !showHistory }
                        CalcDisplay(engine.expression, engine.display, fontSize, text, subtle, Modifier.weight(0.32f))
                        CalcKeypad(isDark, text, Modifier.weight(0.68f), ::handleInput)
                    }
                }
            }
        } else {
            // Compact / mobile layout: full-bleed, edge-to-edge like a phone
            // calculator app. History collapses into a small pull-down strip.
            Column(Modifier.fillMaxSize()) {
                CalcToolbar(isDark, subtle, showHistory) { showHistory = !showHistory }

                AnimatedVisibility(visible = showHistory) {
                    CalcHistoryStrip(isDark, engine.history, onClear = { engine.clearHistory() })
                }

                CalcDisplay(engine.expression, engine.display, fontSize, text, subtle, Modifier.weight(0.34f))
                CalcKeypad(isDark, text, Modifier.weight(0.66f), ::handleInput)
            }
        }
    }
}

@Composable
private fun CalcToolbar(isDark: Boolean, subtle: Color, historyOpen: Boolean, onToggleHistory: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(if (isDark) Mac.panelDark else Mac.panelLight)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onToggleHistory, modifier = Modifier.size(32.dp)) {
            Icon(
                FluentIcon.History, null,
                tint = if (historyOpen) Mac.blue else subtle,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.weight(1f))
        Text("Calculator", style = MaterialTheme.typography.labelMedium, color = subtle)
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.size(32.dp)) // balances the icon button so the title stays centered
    }
}

@Composable
private fun CalcDisplay(
    expression: String,
    display: String,
    fontSize: TextUnit,
    text: Color,
    subtle: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom
    ) {
        if (expression.isNotEmpty()) {
            Text(
                expression,
                style = MaterialTheme.typography.bodyMedium,
                color = subtle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End
            )
            Spacer(Modifier.height(4.dp))
        }
        Text(
            text = display,
            style = MaterialTheme.typography.displayMedium.copy(fontSize = fontSize, fontWeight = FontWeight.Light),
            color = text,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun CalcKeypad(
    isDark: Boolean,
    text: Color,
    modifier: Modifier = Modifier,
    onPress: (String) -> Unit
) {
    val numBg = if (isDark) Color(0xFF3A3A3C) else Color(0xFFFFFFFF)
    val funcBg = if (isDark) Color(0xFF48484A) else Color(0xFFD1D1D6)
    val opBg = Mac.orange

    val rows: List<List<Pair<String, Color>>> = listOf(
        listOf("AC" to funcBg, "+/-" to funcBg, "%" to funcBg, "÷" to opBg),
        listOf("7" to numBg, "8" to numBg, "9" to numBg, "×" to opBg),
        listOf("4" to numBg, "5" to numBg, "6" to numBg, "−" to opBg),
        listOf("1" to numBg, "2" to numBg, "3" to numBg, "+" to opBg),
        listOf("0" to numBg, "." to numBg, "⌫" to numBg, "=" to opBg),
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isDark) Color(0xFF252525) else Color(0xFFF3F3F3))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (label, bg) ->
                    val isOp = bg == opBg
                    CalcButton(
                        label = label,
                        bg = bg,
                        textColor = if (isOp) Color.White else text,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onPress(label) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalcButton(
    label: String,
    bg: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f, spring(stiffness = Spring.StiffnessHigh))

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CalcHistoryPanel(
    isDark: Boolean,
    history: List<Pair<String, String>>,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.background(if (isDark) Mac.sidebarDark else Mac.sidebarLight)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("History", fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleSmall,
                color = if (isDark) Mac.textPrimaryDark else Mac.textPrimaryLight)
            if (history.isNotEmpty()) {
                TextButton(onClick = onClear, contentPadding = PaddingValues(4.dp)) {
                    Text("Clear", style = MaterialTheme.typography.labelSmall, color = Mac.blue)
                }
            }
        }
        if (history.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("No history", style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Mac.textSecondaryDark else Mac.textSecondaryLight)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(history) { (expr, result) ->
                    GlassCard(isDark, Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(10.dp)) {
                            Text(expr, style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) Mac.textSecondaryDark else Mac.textSecondaryLight,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(result, style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (isDark) Mac.textPrimaryDark else Mac.textPrimaryLight, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

/** A compact horizontal strip used instead of a full sidebar on narrow screens. */
@Composable
private fun CalcHistoryStrip(isDark: Boolean, history: List<Pair<String, String>>, onClear: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(max = 140.dp)
            .background(if (isDark) Mac.sidebarDark else Mac.sidebarLight)
    ) {
        if (history.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
                Text("No history yet", style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Mac.textSecondaryDark else Mac.textSecondaryLight)
            }
        } else {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("History", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Mac.textSecondaryDark else Mac.textSecondaryLight)
                TextButton(onClick = onClear, contentPadding = PaddingValues(2.dp)) {
                    Text("Clear", style = MaterialTheme.typography.labelSmall, color = Mac.blue)
                }
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                history.take(5).forEach { (expr, result) ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(expr, style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Mac.textSecondaryDark else Mac.textSecondaryLight,
                            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        Text(result, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium,
                            color = if (isDark) Mac.textPrimaryDark else Mac.textPrimaryLight)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  CALENDAR  — month navigation, event creation; compact layout on phones,
//  multi-pane layout on wider/desktop-like screens
// ─────────────────────────────────────────────────────────────────────────────

data class CalEvent(
    val id: Int,
    val title: String,
    val time: String,
    val endTime: String,
    val color: Color,
    val day: Int,
    val month: Int,
    val year: Int,
    val isAllDay: Boolean = false,
    val location: String = ""
)

private class CalendarState {
    val systemCal: java.util.Calendar = java.util.Calendar.getInstance()
    var displayMonth by mutableStateOf(systemCal.get(java.util.Calendar.MONTH))
    var displayYear by mutableStateOf(systemCal.get(java.util.Calendar.YEAR))
    var selectedDay by mutableStateOf(systemCal.get(java.util.Calendar.DAY_OF_MONTH))
}

/** Rotating accent palette assigned to newly created events. */
private val eventColorPalette = listOf(
    Mac.blue, Mac.green, Mac.orange, Mac.purple, Mac.pink, Mac.teal, Mac.red, Mac.yellow
)

private data class NewEventInput(
    val title: String,
    val startTime: String,
    val endTime: String,
    val isAllDay: Boolean,
    val location: String
)

@Composable
fun CalendarScreen(isDark: Boolean) {
    val bg     = if (isDark) Mac.surfaceDark  else Mac.surfaceLight
    val panel  = if (isDark) Mac.panelDark    else Mac.panelLight
    val text   = if (isDark) Mac.textPrimaryDark else Mac.textPrimaryLight
    val subtle = if (isDark) Mac.textSecondaryDark else Mac.textSecondaryLight

    val state = remember { CalendarState() }
    var showAddEvent by remember { mutableStateOf(false) }

    val today = remember { java.util.Calendar.getInstance() }
    val todayDay = today.get(java.util.Calendar.DAY_OF_MONTH)
    val todayMonth = today.get(java.util.Calendar.MONTH)
    val todayYear = today.get(java.util.Calendar.YEAR)

    val monthNames = remember {
        listOf("January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December")
    }
    val dayHeaders = remember { listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat") }

    // Starts empty — no placeholder/sample events. Users add their own.
    var events by remember { mutableStateOf(listOf<CalEvent>()) }

    val selectedEvents = events.filter {
        it.day == state.selectedDay && it.month == state.displayMonth && it.year == state.displayYear
    }

    val firstDayOfWeek = remember(state.displayMonth, state.displayYear) {
        java.util.Calendar.getInstance().apply { set(state.displayYear, state.displayMonth, 1) }
            .get(java.util.Calendar.DAY_OF_WEEK) - 1
    }
    val daysInMonth = remember(state.displayMonth, state.displayYear) {
        java.util.Calendar.getInstance().apply { set(state.displayYear, state.displayMonth, 1) }
            .getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    }
    val cells = remember(firstDayOfWeek, daysInMonth) {
        (0 until firstDayOfWeek).map { null } +
            (1..daysInMonth).map { it } +
            (0 until (7 - (firstDayOfWeek + daysInMonth) % 7) % 7).map { null }
    }

    fun eventDotsForDay(day: Int): List<Color> = events
        .filter { it.day == day && it.month == state.displayMonth && it.year == state.displayYear && !it.isAllDay }
        .take(3).map { it.color }

    fun allDayForDay(day: Int): CalEvent? = events
        .firstOrNull { it.day == day && it.month == state.displayMonth && it.year == state.displayYear && it.isAllDay }

    fun goToToday() {
        state.displayMonth = todayMonth; state.displayYear = todayYear; state.selectedDay = todayDay
    }

    fun prevMonth() {
        if (state.displayMonth == 0) { state.displayMonth = 11; state.displayYear-- } else state.displayMonth--
    }

    fun nextMonth() {
        if (state.displayMonth == 11) { state.displayMonth = 0; state.displayYear++ } else state.displayMonth++
    }

    fun addEvent(input: NewEventInput) {
        if (input.title.isNotBlank()) {
            val newId = (events.maxOfOrNull { it.id } ?: 0) + 1
            val color = eventColorPalette[events.size % eventColorPalette.size]
            events = events + CalEvent(
                id = newId,
                title = input.title.trim(),
                time = input.startTime,
                endTime = input.endTime,
                color = color,
                day = state.selectedDay,
                month = state.displayMonth,
                year = state.displayYear,
                isAllDay = input.isAllDay,
                location = input.location.trim()
            )
        }
    }

    if (showAddEvent) {
        AddEventDialog(isDark, onDismiss = { showAddEvent = false }, onAdd = {
            addEvent(it); showAddEvent = false
        })
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(bg)) {
        val isWide = maxWidth >= WIDE_LAYOUT_BREAKPOINT

        if (isWide) {
            WideCalendarLayout(
                isDark = isDark, panel = panel, text = text, subtle = subtle,
                state = state, monthNames = monthNames, dayHeaders = dayHeaders, cells = cells,
                todayDay = todayDay, todayMonth = todayMonth, todayYear = todayYear,
                selectedEvents = selectedEvents,
                eventDotsForDay = ::eventDotsForDay, allDayForDay = ::allDayForDay,
                onToday = ::goToToday, onPrev = ::prevMonth, onNext = ::nextMonth,
                onAddEventClick = { showAddEvent = true }
            )
        } else {
            CompactCalendarLayout(
                isDark = isDark, panel = panel, text = text, subtle = subtle,
                state = state, monthNames = monthNames, dayHeaders = dayHeaders, cells = cells,
                todayDay = todayDay, todayMonth = todayMonth, todayYear = todayYear,
                selectedEvents = selectedEvents,
                eventDotsForDay = ::eventDotsForDay, allDayForDay = ::allDayForDay,
                onToday = ::goToToday, onPrev = ::prevMonth, onNext = ::nextMonth,
                onAddEventClick = { showAddEvent = true }
            )
        }
    }
}

@Composable
private fun WideCalendarLayout(
    isDark: Boolean, panel: Color, text: Color, subtle: Color,
    state: CalendarState, monthNames: List<String>, dayHeaders: List<String>, cells: List<Int?>,
    todayDay: Int, todayMonth: Int, todayYear: Int,
    selectedEvents: List<CalEvent>,
    eventDotsForDay: (Int) -> List<Color>, allDayForDay: (Int) -> CalEvent?,
    onToday: () -> Unit, onPrev: () -> Unit, onNext: () -> Unit, onAddEventClick: () -> Unit
) {
    Row(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).fillMaxHeight()) {
            CalendarHeader(panel, text, subtle, state, monthNames, onToday, onPrev, onNext)
            DayOfWeekRow(panel, subtle, dayHeaders)
            HorizontalDivider(color = if (isDark) Mac.borderDark else Mac.borderLight, thickness = 0.5.dp)
            MonthGrid(
                isDark = isDark, text = text, cells = cells, state = state,
                todayDay = todayDay, todayMonth = todayMonth, todayYear = todayYear,
                eventDotsForDay = eventDotsForDay, allDayForDay = allDayForDay,
                compact = false, modifier = Modifier.weight(1f)
            )
        }

        Column(
            modifier = Modifier.width(240.dp).fillMaxHeight().background(panel).padding(16.dp)
        ) {
            AgendaPanel(isDark, text, subtle, state, monthNames, todayDay, todayMonth, todayYear, selectedEvents)
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = if (isDark) Mac.borderDark else Mac.borderLight, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onAddEventClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(FluentIcon.Add, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add Event", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun CompactCalendarLayout(
    isDark: Boolean, panel: Color, text: Color, subtle: Color,
    state: CalendarState, monthNames: List<String>, dayHeaders: List<String>, cells: List<Int?>,
    todayDay: Int, todayMonth: Int, todayYear: Int,
    selectedEvents: List<CalEvent>,
    eventDotsForDay: (Int) -> List<Color>, allDayForDay: (Int) -> CalEvent?,
    onToday: () -> Unit, onPrev: () -> Unit, onNext: () -> Unit, onAddEventClick: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        // Top bar: month/year + nav + quick add (replaces the fixed sidebar)
        Row(
            modifier = Modifier.fillMaxWidth().height(52.dp).background(panel).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAddEventClick, modifier = Modifier.size(36.dp)) {
                Icon(FluentIcon.Add, null, tint = Mac.blue, modifier = Modifier.size(20.dp))
            }
            Text(
                "${monthNames[state.displayMonth]} ${state.displayYear}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = text,
                modifier = Modifier.padding(start = 4.dp).weight(1f),
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            TextButton(onClick = onToday) {
                Text("Today", style = MaterialTheme.typography.labelMedium, color = Mac.blue)
            }
            IconButton(onClick = onPrev, modifier = Modifier.size(36.dp)) {
                Icon(FluentIcon.ChevronLeft, null, tint = subtle, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onNext, modifier = Modifier.size(36.dp)) {
                Icon(FluentIcon.ChevronRight, null, tint = subtle, modifier = Modifier.size(18.dp))
            }
        }

        DayOfWeekRow(panel, subtle, dayHeaders)
        HorizontalDivider(color = if (isDark) Mac.borderDark else Mac.borderLight, thickness = 0.5.dp)

        // Grid gets a natural (non-stretched) height on phones so the agenda
        // below always has room, instead of a fixed side panel eating space.
        MonthGrid(
            isDark = isDark, text = text, cells = cells, state = state,
            todayDay = todayDay, todayMonth = todayMonth, todayYear = todayYear,
            eventDotsForDay = eventDotsForDay, allDayForDay = allDayForDay,
            compact = true, modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider(color = if (isDark) Mac.borderDark else Mac.borderLight, thickness = 0.5.dp)

        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(panel)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            AgendaPanel(isDark, text, subtle, state, monthNames, todayDay, todayMonth, todayYear, selectedEvents)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onAddEventClick,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Mac.blue)
            ) {
                Icon(FluentIcon.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Add Event")
            }
        }
    }
}

@Composable
private fun CalendarHeader(
    panel: Color, text: Color, subtle: Color, state: CalendarState, monthNames: List<String>,
    onToday: () -> Unit, onPrev: () -> Unit, onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp).background(panel).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "${monthNames[state.displayMonth]} ${state.displayYear}",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            color = text
        )
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onToday) {
            Text("Today", style = MaterialTheme.typography.labelMedium, color = Mac.blue)
        }
        IconButton(onClick = onPrev, modifier = Modifier.size(34.dp)) {
            Icon(FluentIcon.ChevronLeft, null, tint = subtle, modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = onNext, modifier = Modifier.size(34.dp)) {
            Icon(FluentIcon.ChevronRight, null, tint = subtle, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun DayOfWeekRow(panel: Color, subtle: Color, dayHeaders: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth().height(28.dp).background(panel).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        dayHeaders.forEach { d ->
            Text(d, style = MaterialTheme.typography.labelSmall, color = subtle,
                modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun MonthGrid(
    isDark: Boolean, text: Color, cells: List<Int?>, state: CalendarState,
    todayDay: Int, todayMonth: Int, todayYear: Int,
    eventDotsForDay: (Int) -> List<Color>, allDayForDay: (Int) -> CalEvent?,
    compact: Boolean, modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(if (isDark) Color(0xFF1A1A1A) else Color(0xFFF9F9F9))
    ) {
        cells.chunked(7).forEach { week ->
            Row(
                modifier = if (compact) Modifier.fillMaxWidth() else Modifier.weight(1f).fillMaxWidth()
            ) {
                week.forEach { day ->
                    val isToday = day == todayDay && state.displayMonth == todayMonth && state.displayYear == todayYear
                    val isSelected = day == state.selectedDay && day != null
                    val dots = if (day != null) eventDotsForDay(day) else emptyList()
                    val allDay = if (day != null) allDayForDay(day) else null

                    val cellModifier = Modifier
                        .weight(1f)
                        .let { if (compact) it.aspectRatio(0.85f) else it.fillMaxHeight() }
                        .border(0.3.dp, if (isDark) Color(0x18FFFFFF) else Color(0x18000000))
                        .background(if (isSelected && !isToday) Mac.blue.copy(alpha = 0.08f) else Color.Transparent)
                        .pointerInput(day) { detectTapGestures(onTap = { if (day != null) state.selectedDay = day }) }
                        .padding(4.dp)

                    Box(cellModifier) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (day != null) {
                                Box(
                                    modifier = Modifier
                                        .size(if (compact) 24.dp else 26.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isToday -> Mac.blue
                                                isSelected -> Mac.blue.copy(alpha = 0.15f)
                                                else -> Color.Transparent
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        day.toString(),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = when {
                                            isToday -> Color.White
                                            isSelected -> Mac.blue
                                            else -> text
                                        }
                                    )
                                }
                            }

                            if (!compact && allDay != null) {
                                Spacer(Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(allDay.color.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(allDay.title, style = MaterialTheme.typography.labelSmall,
                                        color = allDay.color, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                        fontSize = 9.sp)
                                }
                            }

                            if (dots.isNotEmpty()) {
                                Spacer(Modifier.height(3.dp))
                                if (compact) {
                                    // Just one small indicator dot — there's no room for more on a phone.
                                    Box(Modifier.size(5.dp).clip(CircleShape).background(dots.first()))
                                } else {
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        dots.forEach { c -> Box(Modifier.size(5.dp).clip(CircleShape).background(c)) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AgendaPanel(
    isDark: Boolean, text: Color, subtle: Color, state: CalendarState, monthNames: List<String>,
    todayDay: Int, todayMonth: Int, todayYear: Int, selectedEvents: List<CalEvent>
) {
    Text(
        text = if (state.selectedDay == todayDay && state.displayMonth == todayMonth && state.displayYear == todayYear)
            "Today, ${monthNames[state.displayMonth]} ${state.selectedDay}"
        else "${monthNames[state.displayMonth]} ${state.selectedDay}, ${state.displayYear}",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = text
    )
    Spacer(Modifier.height(12.dp))

    if (selectedEvents.isEmpty()) {
        Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
            Text("No events", style = MaterialTheme.typography.bodySmall, color = subtle)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            selectedEvents.forEach { ev ->
                GlassCard(isDark, Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.width(3.dp).height(40.dp).clip(RoundedCornerShape(2.dp)).background(ev.color))
                        Column {
                            Text(ev.title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = text, maxLines = 1)
                            if (ev.isAllDay) {
                                Text("All day", style = MaterialTheme.typography.labelSmall, color = subtle)
                            } else {
                                Text("${ev.time} – ${ev.endTime}", style = MaterialTheme.typography.labelSmall, color = subtle)
                            }
                            if (ev.location.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Icon(FluentIcon.Location, null, modifier = Modifier.size(10.dp), tint = subtle)
                                    Text(ev.location, style = MaterialTheme.typography.labelSmall, color = subtle)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddEventDialog(isDark: Boolean, onDismiss: () -> Unit, onAdd: (NewEventInput) -> Unit) {
    var title by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:00") }
    var isAllDay by remember { mutableStateOf(false) }
    var location by remember { mutableStateOf("") }

    val panel = if (isDark) Mac.panelDark else Mac.panelLight
    val text = if (isDark) Mac.textPrimaryDark else Mac.textPrimaryLight
    val subtle = if (isDark) Mac.textSecondaryDark else Mac.textSecondaryLight

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(panel)
                .padding(20.dp)
        ) {
            Text("New Event", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = text)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Event title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("All day", style = MaterialTheme.typography.bodyMedium, color = text)
                Switch(
                    checked = isAllDay,
                    onCheckedChange = { isAllDay = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = Mac.blue)
                )
            }

            if (!isAllDay) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                placeholder = { Text("Location (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            if (title.isBlank()) {
                Spacer(Modifier.height(6.dp))
                Text("Title is required", style = MaterialTheme.typography.labelSmall, color = subtle)
            }

            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onAdd(NewEventInput(title, startTime, endTime, isAllDay, location))
                    },
                    modifier = Modifier.weight(1f),
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Mac.blue)
                ) {
                    Text("Add")
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared formatting helper (kept for any external references)
// ─────────────────────────────────────────────────────────────────────────────
fun formatResult(value: Double): String = formatCalcValue(value)
