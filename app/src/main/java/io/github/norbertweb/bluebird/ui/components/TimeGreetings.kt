package io.github.norbertweb.bluebird.ui.components

import com.trebronwayne.aubree.AubreeState
import java.util.Calendar
import kotlin.random.Random

/** A greeting and the Aubree mood that goes with it. [text] is English; show it through ui(). */
data class TimeGreeting(val text: String, val aubree: AubreeState)

/**
 * Friendly greetings for the time of day: Night owl, Late night, Good evening and many more.
 * Each hour of the day has its own pool, and weekdays add a few of their own (Monday, Friday, the weekend).
 */
object TimeGreetings {

    private val lateNight = listOf(
        "Night owl", "Burning the midnight oil", "Still up?", "Midnight hour", "The quiet hours",
        "Late night", "Moonlight mode", "Past midnight", "Night shift", "The stars are out"
    )
    private val deepNight = listOf(
        "Can\u2019t sleep?", "Deep night", "The world is asleep", "Wide awake", "Very late",
        "The early hours", "Night owl", "Dreamland is calling", "Nocturnal mode"
    )
    private val dawn = listOf(
        "Early bird", "Rise and shine", "Up with the sun", "Dawn patrol", "First light",
        "Fresh and early", "Bright and early", "A new day begins"
    )
    private val morning = listOf(
        "Good morning", "Morning!", "Hello, sunshine", "Fresh start", "Coffee time?",
        "Top of the morning", "Ready for today?", "A bright new day", "Let\u2019s get going"
    )
    private val lateMorning = listOf(
        "Good morning", "Making progress", "Mid-morning", "Almost lunchtime",
        "Productive morning", "Keep it rolling", "A smooth morning"
    )
    private val midday = listOf(
        "Good afternoon", "Lunch time", "Midday", "High noon", "Fuel up", "Halfway through", "The sun is high"
    )
    private val afternoon = listOf(
        "Good afternoon", "Afternoon", "Keep going", "Afternoon stretch", "Steady does it", "Power hour", "An easy afternoon"
    )
    private val sunset = listOf(
        "Golden hour", "Good evening", "Winding down", "The day is almost done", "The sun is setting", "Evening is near"
    )
    private val evening = listOf(
        "Good evening", "Evening", "Evening vibes", "Time to unwind", "A cosy evening",
        "Relax mode", "Dinner time?", "Slow down a little"
    )
    private val night = listOf(
        "Good evening", "Late evening", "Night mode", "Almost bedtime", "A calm night",
        "Night owl", "Winding down", "Late night", "Good night soon"
    )

    private val monday = listOf("A fresh week", "Monday, let\u2019s go", "New week, new start")
    private val friday = listOf("Happy Friday", "Almost the weekend", "Friday feeling")
    private val weekend = listOf("Weekend mode", "Happy weekend", "No rush today", "Enjoy your weekend")

    private fun poolFor(hour: Int): List<String> = when (hour) {
        0, 1 -> lateNight
        2, 3, 4 -> deepNight
        5, 6 -> dawn
        7, 8, 9 -> morning
        10, 11 -> lateMorning
        12, 13 -> midday
        14, 15, 16 -> afternoon
        17 -> sunset
        18, 19, 20 -> evening
        else -> night
    }

    /** Picks a greeting for [now]. Random, so the same hour can greet differently each time. */
    fun pick(now: Calendar = Calendar.getInstance(), random: Random = Random.Default): TimeGreeting {
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val pool = poolFor(hour).toMutableList()
        // Daytime hours also get the day's own greetings, so Mondays, Fridays and weekends feel different.
        if (hour in 5..21) {
            when (now.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> if (hour <= 16) pool += monday
                Calendar.FRIDAY -> pool += friday
                Calendar.SATURDAY, Calendar.SUNDAY -> pool += weekend
            }
        }
        val mood = if (hour >= 22 || hour <= 4) AubreeState.SLEEPY else AubreeState.WELCOME
        return TimeGreeting(pool.random(random), mood)
    }
}
