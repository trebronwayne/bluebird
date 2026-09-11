package io.github.norbertweb.bluebird.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import io.github.norbertweb.bluebird.ui.components.FluentIcon
import io.github.norbertweb.bluebird.ui.components.LocalWindowRuntime
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.*

// ─────────────────────────────────────────────────────────────────────────────
//  DESIGN TOKENS  (Windows 11 / Fluent-inspired compact palette)
// ─────────────────────────────────────────────────────────────────────────────

private object Mac {
    // Backgrounds
    val surfaceDark   = Color(0xFF202020)
    val surfaceLight  = Color(0xFFF3F3F3)
    val panelDark     = Color(0xFF2B2B2B)
    val panelLight    = Color(0xFFFFFFFF)
    val sidebarDark   = Color(0xFF252525)
    val sidebarLight  = Color(0xFFF7F7F7)

    // Glass
    val glassDark     = Color(0x44FFFFFF)
    val glassLight    = Color(0x88FFFFFF)
    val borderDark    = Color(0x22FFFFFF)
    val borderLight   = Color(0x33000000)

    // Accent
    val blue          = Color(0xFF007AFF)
    val blueSoft      = Color(0xFF3AA8F0)
    val green         = Color(0xFF107C10)
    val orange        = Color(0xFFD77800)
    val red           = Color(0xFFC42B1C)
    val yellow        = Color(0xFFFFC83D)
    val purple        = Color(0xFF8764B8)
    val pink          = Color(0xFFC239B3)
    val teal          = Color(0xFF3AA8F0)

    // Text
    val textPrimaryDark    = Color(0xFFFFFFFF)
    val textPrimaryLight   = Color(0xFF252525)
    val textSecondaryDark  = Color(0xFF8E8E93)
    val textSecondaryLight = Color(0xFF6C6C70)
    val textTertiaryDark   = Color(0xFF48484A)
    val textTertiaryLight  = Color(0xFFAEAEB2)
}

// ─────────────────────────────────────────────────────────────────────────────
//  SHARED COMPOSABLES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GlassCard(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp,
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

@Composable
private fun SectionHeader(text: String, isDark: Boolean, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp
        ),
        color = if (isDark) Mac.textSecondaryDark else Mac.textSecondaryLight,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  CALCULATOR  — Real functional with history, scientific mode, animations
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CalculatorScreen(isDark: Boolean) {
    val bg     = if (isDark) Mac.surfaceDark  else Mac.surfaceLight
    val panel  = if (isDark) Mac.panelDark    else Mac.panelLight
    val text   = if (isDark) Mac.textPrimaryDark else Mac.textPrimaryLight
    val subtle = if (isDark) Mac.textSecondaryDark else Mac.textSecondaryLight

    // ── State ──────────────────────────────────────────────────────────────
    var display     by remember { mutableStateOf("0") }
    var expression  by remember { mutableStateOf("") }
    var firstNum    by remember { mutableStateOf(0.0) }
    var secondNum   by remember { mutableStateOf(0.0) }
    var pendingOp   by remember { mutableStateOf("") }
    var newNumber   by remember { mutableStateOf(true) }
    var justEvaled  by remember { mutableStateOf(false) }
    var history     by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var isScientific by remember { mutableStateOf(false) }
    var isDegrees   by remember { mutableStateOf(true) }
    var showHistory by remember { mutableStateOf(true) }
    var memory      by remember { mutableStateOf(0.0) }
    var hasMemory   by remember { mutableStateOf(false) }

    // ── Display size ───────────────────────────────────────────────────────
    val fontSize = when {
        display.length > 14 -> 22.sp
        display.length > 10 -> 30.sp
        display.length > 7  -> 38.sp
        else                -> 52.sp
    }

    // ── Core logic ─────────────────────────────────────────────────────────
    fun toRad(deg: Double) = if (isDegrees) Math.toRadians(deg) else deg

    fun applyOp(a: Double, op: String, b: Double): Double = when (op) {
        "+"  -> a + b
        "-"  -> a - b
        "×"  -> a * b
        "÷"  -> if (b != 0.0) a / b else Double.NaN
        "%"  -> a % b
        "xʸ" -> a.pow(b)
        else -> b
    }

    fun applyUnary(op: String, a: Double): Double = when (op) {
        "sin"   -> sin(toRad(a))
        "cos"   -> cos(toRad(a))
        "tan"   -> tan(toRad(a))
        "sin⁻¹" -> if (isDegrees) Math.toDegrees(asin(a)) else asin(a)
        "cos⁻¹" -> if (isDegrees) Math.toDegrees(acos(a)) else acos(a)
        "tan⁻¹" -> if (isDegrees) Math.toDegrees(atan(a)) else atan(a)
        "ln"    -> ln(a)
        "log₁₀" -> log10(a)
        "log₂"  -> log2(a)
        "√"     -> sqrt(a)
        "∛"     -> a.pow(1.0 / 3)
        "x²"    -> a * a
        "x³"    -> a * a * a
        "1/x"   -> if (a != 0.0) 1.0 / a else Double.NaN
        "n!"    -> { var r = 1.0; for (i in 2..a.toInt()) r *= i; r }
        "eˣ"    -> exp(a)
        "10ˣ"   -> 10.0.pow(a)
        "+/-"   -> -a
        "%"     -> a / 100
        "π"     -> Math.PI
        "e"     -> Math.E
        else    -> a
    }

    fun fmt(v: Double): String {
        if (v.isNaN()) return "Error"
        if (v.isInfinite()) return if (v > 0) "∞" else "-∞"
        return if (v == v.toLong().toDouble() && abs(v) < 1e15) v.toLong().toString()
        else "%.10g".format(v).trimEnd('0').trimEnd('.')
    }

    fun handleInput(value: String) {
        when (value) {
            // ── Clear / back ──────────────────────────────────────────────
            "AC" -> {
                display = "0"; expression = ""; firstNum = 0.0
                pendingOp = ""; newNumber = true; justEvaled = false
            }
            "C"  -> { display = "0"; newNumber = true }
            "⌫"  -> {
                if (!newNumber && display != "0") {
                    display = if (display.length == 1 || (display.length == 2 && display.startsWith("-")))
                        "0" else display.dropLast(1)
                }
            }
            // ── Memory ────────────────────────────────────────────────────
            "MC" -> { memory = 0.0; hasMemory = false }
            "MR" -> { display = fmt(memory); newNumber = true }
            "M+" -> { memory += display.toDoubleOrNull() ?: 0.0; hasMemory = true }
            "M-" -> { memory -= display.toDoubleOrNull() ?: 0.0; hasMemory = true }
            "MS" -> { memory = display.toDoubleOrNull() ?: 0.0; hasMemory = true }
            // ── Constants ─────────────────────────────────────────────────
            "π", "e" -> {
                display = fmt(applyUnary(value, 0.0))
                newNumber = true
            }
            // ── Unary ops ─────────────────────────────────────────────────
            "sin","cos","tan","sin⁻¹","cos⁻¹","tan⁻¹",
            "ln","log₁₀","log₂","√","∛","x²","x³",
            "1/x","n!","eˣ","10ˣ","+/-","%" -> {
                val a = display.toDoubleOrNull() ?: 0.0
                val result = applyUnary(value, a)
                val exprStr = "$value($display)"
                if (justEvaled) expression = "$exprStr ="
                display = fmt(result)
                newNumber = true
                if (!result.isNaN() && !result.isInfinite())
                    history = (listOf(exprStr to display) + history).take(30)
            }
            // ── Binary ops ────────────────────────────────────────────────
            "+", "-", "×", "÷", "%", "xʸ" -> {
                val a = display.toDoubleOrNull() ?: 0.0
                if (pendingOp.isNotEmpty() && !newNumber) {
                    val result = applyOp(firstNum, pendingOp, a)
                    display = fmt(result); firstNum = result
                } else {
                    firstNum = a
                }
                pendingOp = value
                expression = "$display $value"
                newNumber = true; justEvaled = false
            }
            // ── Equals ────────────────────────────────────────────────────
            "=" -> {
                val b = display.toDoubleOrNull() ?: 0.0
                secondNum = b
                val result = applyOp(firstNum, pendingOp, b)
                val expr = "$expression $display ="
                history = (listOf(expr to fmt(result)) + history).take(30)
                expression = expr
                display = fmt(result)
                firstNum = result
                newNumber = true; justEvaled = true
            }
            // ── Decimal ───────────────────────────────────────────────────
            "." -> {
                if (newNumber) { display = "0."; newNumber = false }
                else if (!display.contains(".")) display += "."
            }
            // ── Digits ───────────────────────────────────────────────────
            else -> {
                if (newNumber) { display = value; newNumber = false }
                else display = if (display == "0") value else display + value
                if (justEvaled) { expression = ""; justEvaled = false }
            }
        }
    }

    // ── Layout ─────────────────────────────────────────────────────────────
    Row(modifier = Modifier.fillMaxSize().background(bg)) {

        // History sidebar
        AnimatedVisibility(
            visible = showHistory,
            enter = slideInHorizontally() + fadeIn(),
            exit = slideOutHorizontally() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .width(200.dp)
                    .fillMaxHeight()
                    .background(if (isDark) Mac.sidebarDark else Mac.sidebarLight)
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("History", fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleSmall, color = text)
                    if (history.isNotEmpty()) {
                        TextButton(onClick = { history = emptyList() }, contentPadding = PaddingValues(4.dp)) {
                            Text("Clear", style = MaterialTheme.typography.labelSmall, color = Mac.blue)
                        }
                    }
                }
                if (history.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No history", style = MaterialTheme.typography.bodySmall, color = subtle)
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(history) { (expr, result) ->
                            GlassCard(isDark, Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(10.dp)) {
                                    Text(expr, style = MaterialTheme.typography.labelSmall,
                                        color = subtle, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text(result, style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium, color = text, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Main calculator
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {

            // Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(if (isDark) Mac.panelDark else Mac.panelLight)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { showHistory = !showHistory }, modifier = Modifier.size(32.dp)) {
                    Icon(FluentIcon.History, null, tint = subtle, modifier = Modifier.size(18.dp))
                }
                Text(if (isScientific) "Scientific" else "Standard",
                    style = MaterialTheme.typography.labelMedium, color = subtle)
                Spacer(Modifier.weight(1f))
                if (isDegrees || isScientific) {
                    if (isScientific) {
                        TextButton(onClick = { isDegrees = !isDegrees }, contentPadding = PaddingValues(6.dp)) {
                            Text(if (isDegrees) "DEG" else "RAD",
                                style = MaterialTheme.typography.labelSmall,
                                color = Mac.blue, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                TextButton(onClick = { isScientific = !isScientific }, contentPadding = PaddingValues(6.dp)) {
                    Text(if (isScientific) "Standard" else "Scientific",
                        style = MaterialTheme.typography.labelSmall, color = Mac.blue)
                }
            }

            // Display
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) Color(0xFF252525) else Color(0xFFF3F3F3))
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (hasMemory) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Mac.blue)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("M", style = MaterialTheme.typography.labelSmall,
                                color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (expression.isNotEmpty()) {
                    Text(expression, style = MaterialTheme.typography.bodySmall,
                        color = subtle, maxLines = 2, textAlign = TextAlign.End)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = display,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = fontSize, fontWeight = FontWeight.Light
                    ),
                    color = text,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(color = if (isDark) Mac.borderDark else Mac.borderLight, thickness = 0.5.dp)

            // Keypad
            val numBg = if (isDark) Color(0xFF3A3A3C) else Color(0xFFFFFFFF)
            val opBg  = if (isDark) Color(0xFF2C2C2E) else Color(0xFFD1D1D6)
            val acBg  = if (isDark) Color(0xFF48484A) else Color(0xFFAEAEB2)

            val standardRows = listOf(
                listOf("MC" to acBg, "MR" to acBg, "M+" to acBg, "M-" to acBg, "MS" to acBg),
                listOf("AC" to acBg, "+/-" to acBg, "%" to acBg, "÷" to Mac.orange),
                listOf("7" to numBg, "8" to numBg, "9" to numBg, "×" to Mac.orange),
                listOf("4" to numBg, "5" to numBg, "6" to numBg, "-" to Mac.orange),
                listOf("1" to numBg, "2" to numBg, "3" to numBg, "+" to Mac.orange),
                listOf("0" to numBg, "." to numBg, "⌫" to numBg, "=" to Mac.orange),
            )

            val scientificRows = listOf(
                listOf("sin" to opBg, "cos" to opBg, "tan" to opBg, "sin⁻¹" to opBg, "cos⁻¹" to opBg, "tan⁻¹" to opBg),
                listOf("ln" to opBg, "log₁₀" to opBg, "log₂" to opBg, "√" to opBg, "∛" to opBg, "xʸ" to opBg),
                listOf("n!" to opBg, "eˣ" to opBg, "10ˣ" to opBg, "x²" to opBg, "x³" to opBg, "1/x" to opBg),
                listOf("π" to opBg, "e" to opBg, "MC" to acBg, "MR" to acBg, "M+" to acBg, "MS" to acBg),
            )

            val rows = if (isScientific) scientificRows + standardRows.drop(1) else standardRows

            Column(
                modifier = Modifier.weight(1f).fillMaxWidth()
                    .background(if (isDark) Color(0xFF252525) else Color(0xFFF3F3F3))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row.forEach { (label, bg) ->
                            val isSpecial = bg == Mac.orange
                            CalcButton(
                                label = label,
                                bg = bg,
                                textColor = if (isSpecial) Color.White else text,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                onClick = { handleInput(label) }
                            )
                        }
                    }
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
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, spring(stiffness = Spring.StiffnessHigh))

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
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
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  CALENDAR  — Real month navigation, event creation, week view
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

@Composable
fun CalendarScreen(isDark: Boolean) {
    val bg     = if (isDark) Mac.surfaceDark  else Mac.surfaceLight
    val panel  = if (isDark) Mac.panelDark    else Mac.panelLight
    val text   = if (isDark) Mac.textPrimaryDark else Mac.textPrimaryLight
    val subtle = if (isDark) Mac.textSecondaryDark else Mac.textSecondaryLight
    val sidebar= if (isDark) Mac.sidebarDark  else Mac.sidebarLight

    val systemCal = remember { java.util.Calendar.getInstance() }
    var displayMonth by remember { mutableStateOf(systemCal.get(java.util.Calendar.MONTH)) }
    var displayYear  by remember { mutableStateOf(systemCal.get(java.util.Calendar.YEAR)) }
    var selectedDay  by remember { mutableStateOf(systemCal.get(java.util.Calendar.DAY_OF_MONTH)) }
    var viewMode     by remember { mutableStateOf("Month") } // Month | Week | Day
    var showAddEvent by remember { mutableStateOf(false) }
    var newEventTitle by remember { mutableStateOf("") }

    val today = java.util.Calendar.getInstance()
    val todayDay   = today.get(java.util.Calendar.DAY_OF_MONTH)
    val todayMonth = today.get(java.util.Calendar.MONTH)
    val todayYear  = today.get(java.util.Calendar.YEAR)

    val monthNames = listOf("January","February","March","April","May","June",
        "July","August","September","October","November","December")
    val dayHeaders = listOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat")

    var events by remember {
        mutableStateOf(
            listOf(
                CalEvent(1, "Team Standup",      "9:00",  "9:30",  Mac.blue,   todayDay, todayMonth, todayYear),
                CalEvent(2, "Design Review",     "11:00", "12:00", Mac.purple, todayDay, todayMonth, todayYear),
                CalEvent(3, "Lunch Break",       "12:30", "13:30", Mac.green,  todayDay, todayMonth, todayYear, location = "Cafeteria"),
                CalEvent(4, "Sprint Planning",   "14:00", "15:30", Mac.orange, todayDay, todayMonth, todayYear),
                CalEvent(5, "1:1 with Manager",  "16:00", "16:30", Mac.pink,   todayDay, todayMonth, todayYear),
                CalEvent(6, "Project Deadline",  "",      "",      Mac.red,    (todayDay + 2).coerceIn(1, 28), todayMonth, todayYear, isAllDay = true),
                CalEvent(7, "Team Offsite",      "",      "",      Mac.teal,   (todayDay + 5).coerceIn(1, 28), todayMonth, todayYear, isAllDay = true),
                CalEvent(8, "Q3 Planning",       "10:00", "11:00", Mac.yellow, (todayDay + 1).coerceIn(1, 28), todayMonth, todayYear),
            )
        )
    }

    val selectedEvents = events.filter {
        it.day == selectedDay && it.month == displayMonth && it.year == displayYear
    }

    // Grid helpers
    val firstDayOfWeek = java.util.Calendar.getInstance().apply {
        set(displayYear, displayMonth, 1)
    }.get(java.util.Calendar.DAY_OF_WEEK) - 1

    val daysInMonth = java.util.Calendar.getInstance().apply {
        set(displayYear, displayMonth, 1)
    }.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)

    val cells = (0 until firstDayOfWeek).map { null } +
            (1..daysInMonth).map { it } +
            (0 until (7 - (firstDayOfWeek + daysInMonth) % 7) % 7).map { null }

    fun eventDotsForDay(day: Int): List<Color> = events
        .filter { it.day == day && it.month == displayMonth && it.year == displayYear && !it.isAllDay }
        .take(3).map { it.color }

    fun allDayForDay(day: Int): CalEvent? = events
        .firstOrNull { it.day == day && it.month == displayMonth && it.year == displayYear && it.isAllDay }

    Row(modifier = Modifier.fillMaxSize().background(bg)) {

        // ── Left sidebar: mini-calendar + calendars list ──────────────────
        Column(
            modifier = Modifier
                .width(220.dp)
                .fillMaxHeight()
                .background(sidebar)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Add event button
            Button(
                onClick = { showAddEvent = true },
                modifier = Modifier.fillMaxWidth().height(34.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Mac.blue),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(FluentIcon.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("New Event", style = MaterialTheme.typography.labelMedium)
            }

            // View mode
            GlassCard(isDark, Modifier.fillMaxWidth()) {
                Row(Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    listOf("Month", "Week", "Day").forEach { mode ->
                        val active = viewMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (active) Mac.blue else Color.Transparent)
                                .pointerInput(Unit) { detectTapGestures(onTap = { viewMode = mode }) }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(mode, style = MaterialTheme.typography.labelSmall,
                                color = if (active) Color.White else subtle)
                        }
                    }
                }
            }

            // Calendar sources
            SectionHeader("My Calendars", isDark, Modifier.padding(0.dp))
            listOf(
                Triple("Personal", Mac.blue,   true),
                Triple("Work",     Mac.green,  true),
                Triple("Birthdays",Mac.orange, true),
                Triple("Holidays", Mac.red,    false),
            ).forEach { (name, color, checked) ->
                var isChecked by remember { mutableStateOf(checked) }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isChecked) color else Color.Transparent)
                            .border(1.5.dp, color, RoundedCornerShape(3.dp))
                            .pointerInput(Unit) { detectTapGestures(onTap = { isChecked = !isChecked }) }
                    )
                    Text(name, style = MaterialTheme.typography.bodySmall, color = text)
                }
            }
        }

        // ── Main calendar area ────────────────────────────────────────────
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(panel)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "${monthNames[displayMonth]} $displayYear",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = text
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = {
                    displayMonth = todayMonth; displayYear = todayYear; selectedDay = todayDay
                }) {
                    Text("Today", style = MaterialTheme.typography.labelMedium, color = Mac.blue)
                }
                Row {
                    IconButton(onClick = {
                        if (displayMonth == 0) { displayMonth = 11; displayYear-- } else displayMonth--
                    }, modifier = Modifier.size(34.dp)) {
                        Icon(FluentIcon.ChevronLeft, null, tint = subtle, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = {
                        if (displayMonth == 11) { displayMonth = 0; displayYear++ } else displayMonth++
                    }, modifier = Modifier.size(34.dp)) {
                        Icon(FluentIcon.ChevronRight, null, tint = subtle, modifier = Modifier.size(18.dp))
                    }
                }
            }

            HorizontalDivider(color = if (isDark) Mac.borderDark else Mac.borderLight, thickness = 0.5.dp)

            // Day-of-week headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(panel)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                dayHeaders.forEach { d ->
                    Text(d, style = MaterialTheme.typography.labelSmall,
                        color = subtle, modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center)
                }
            }

            HorizontalDivider(color = if (isDark) Mac.borderDark else Mac.borderLight, thickness = 0.5.dp)

            // Calendar grid
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth()
                    .background(if (isDark) Color(0xFF1A1A1A) else Color(0xFFF9F9F9))
            ) {
                cells.chunked(7).forEach { week ->
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) {
                        week.forEach { day ->
                            val isToday   = day == todayDay && displayMonth == todayMonth && displayYear == todayYear
                            val isSelected = day == selectedDay && day != null
                            val dots      = if (day != null) eventDotsForDay(day) else emptyList()
                            val allDay    = if (day != null) allDayForDay(day) else null

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .border(0.3.dp, if (isDark) Color(0x18FFFFFF) else Color(0x18000000))
                                    .background(
                                        when {
                                            isSelected && !isToday -> Mac.blue.copy(alpha = 0.08f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .pointerInput(day) {
                                        detectTapGestures(onTap = { if (day != null) selectedDay = day })
                                    }
                                    .padding(4.dp)
                            ) {
                                Column {
                                    // Day number
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isToday    -> Mac.blue
                                                    isSelected -> Mac.blue.copy(alpha = 0.15f)
                                                    else       -> Color.Transparent
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (day != null) {
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

                                    // All-day event pill
                                    if (allDay != null) {
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

                                    // Event dots
                                    if (dots.isNotEmpty()) {
                                        Spacer(Modifier.height(3.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            dots.forEach { c ->
                                                Box(Modifier.size(5.dp).clip(CircleShape).background(c))
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

        // ── Right panel: selected day events ─────────────────────────────
        Column(
            modifier = Modifier
                .width(240.dp)
                .fillMaxHeight()
                .background(if (isDark) Mac.panelDark else Mac.panelLight)
                .padding(16.dp)
        ) {
            Text(
                text = if (selectedDay == todayDay && displayMonth == todayMonth && displayYear == todayYear)
                    "Today, ${monthNames[displayMonth]} $selectedDay"
                else "${monthNames[displayMonth]} $selectedDay, $displayYear",
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
                                Box(
                                    Modifier.width(3.dp).height(40.dp)
                                        .clip(RoundedCornerShape(2.dp)).background(ev.color)
                                )
                                Column {
                                    Text(ev.title, style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold), color = text, maxLines = 1)
                                    if (ev.isAllDay) {
                                        Text("All day", style = MaterialTheme.typography.labelSmall, color = subtle)
                                    } else {
                                        Text("${ev.time} – ${ev.endTime}",
                                            style = MaterialTheme.typography.labelSmall, color = subtle)
                                    }
                                    if (ev.location.isNotEmpty()) {
                                        Row(verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Icon(FluentIcon.Location, null,
                                                modifier = Modifier.size(10.dp), tint = subtle)
                                            Text(ev.location, style = MaterialTheme.typography.labelSmall, color = subtle)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = if (isDark) Mac.borderDark else Mac.borderLight, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            // Add event inline
            if (showAddEvent) {
                GlassCard(isDark, Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("New Event", style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold), color = text)
                        OutlinedTextField(
                            value = newEventTitle,
                            onValueChange = { newEventTitle = it },
                            placeholder = { Text("Event title", style = MaterialTheme.typography.bodySmall) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                            singleLine = true
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TextButton(
                                onClick = { showAddEvent = false; newEventTitle = "" },
                                modifier = Modifier.weight(1f)
                            ) { Text("Cancel", style = MaterialTheme.typography.labelSmall) }
                            Button(
                                onClick = {
                                    if (newEventTitle.isNotEmpty()) {
                                        val newId = (events.maxOfOrNull { it.id } ?: 0) + 1
                                        events = events + CalEvent(
                                            newId, newEventTitle, "12:00", "13:00",
                                            Mac.blue, selectedDay, displayMonth, displayYear
                                        )
                                        newEventTitle = ""
                                        showAddEvent = false
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(6.dp)
                            ) { Text("Add", style = MaterialTheme.typography.labelSmall) }
                        }
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { showAddEvent = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    Icon(FluentIcon.Add, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Event", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Shared formatting helper
// ─────────────────────────────────────────────────────────────────────────────
// Kept helper for old references
fun formatResult(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString()
    else "%.10f".format(value).trimEnd('0').trimEnd('.')
