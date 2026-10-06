package com.example.data.sample

import com.example.data.model.DailyEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object SampleDataGenerator {

    fun generateSampleDays(): List<DailyEntry> {
        val list = mutableListOf<DailyEntry>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        // Generate 28 days of realistic baseline data ending today
        // Base normal means: Sleep ~7.1h, Screen ~4.6h, Study ~3.2h, Exercise ~38m, Mood ~7.5, Prod ~7.2
        val baseTemplates = listOf(
            // Day 0 (Today)
            DailyEntryTemplate(7.2, 4.8, 3.1, 35.0, 8.0, 7.0, "Felt well balanced today."),
            // Day -1
            DailyEntryTemplate(6.8, 5.2, 2.8, 20.0, 7.0, 6.0, "Busy afternoon with screen work."),
            // Day -2 (Anomaly: Very High Screen Time)
            DailyEntryTemplate(9.1, 8.2, 3.0, 50.0, 8.0, 8.0, "Late movie marathon and intense coding."),
            // Day -3
            DailyEntryTemplate(7.6, 4.0, 4.2, 60.0, 9.0, 8.0, "Great run in the morning and focused study."),
            // Day -4
            DailyEntryTemplate(6.2, 3.8, 2.5, 30.0, 6.0, 5.0, "Felt slightly tired."),
            // Day -5
            DailyEntryTemplate(7.0, 4.5, 3.0, 40.0, 7.0, 7.0, "Solid routine."),
            // Day -6 (Anomaly: Very Low Study)
            DailyEntryTemplate(5.8, 6.2, 0.5, 20.0, 5.0, 4.0, "Travel day, skipped study block."),
            // Day -7
            DailyEntryTemplate(8.3, 5.1, 3.8, 45.0, 8.0, 7.0, "Good rest over the weekend."),
            // Day -8
            DailyEntryTemplate(7.1, 4.7, 3.2, 35.0, 7.0, 6.0, "Standard day."),
            // Day -9
            DailyEntryTemplate(6.9, 4.9, 2.9, 40.0, 7.0, 7.0, "Steady progress."),
            // Day -10 (Anomaly: Low Mood)
            DailyEntryTemplate(6.0, 5.8, 1.5, 10.0, 3.0, 4.0, "Felt under the weather."),
            // Day -11
            DailyEntryTemplate(7.4, 4.3, 3.5, 45.0, 8.0, 8.0, "Recovered energy and productive."),
            // Day -12
            DailyEntryTemplate(7.0, 4.2, 3.0, 30.0, 7.0, 7.0, "Consistent focus."),
            // Day -13
            DailyEntryTemplate(7.5, 4.6, 3.4, 50.0, 8.0, 8.0, "Good workout session."),
            // Day -14 (Anomaly: High Exercise)
            DailyEntryTemplate(7.8, 3.5, 2.0, 110.0, 9.0, 7.0, "Long weekend hike outdoors."),
            // Day -15
            DailyEntryTemplate(8.0, 4.0, 3.5, 25.0, 8.0, 7.0, "Rest day with reading."),
            // Day -16
            DailyEntryTemplate(6.7, 4.8, 3.1, 40.0, 7.0, 7.0, "Standard workflow."),
            // Day -17
            DailyEntryTemplate(7.2, 4.4, 3.6, 45.0, 8.0, 8.0, "High energy day."),
            // Day -18 (Anomaly: Low Sleep)
            DailyEntryTemplate(4.5, 6.5, 1.8, 15.0, 4.0, 3.0, "Insomnia night."),
            // Day -19
            DailyEntryTemplate(8.5, 4.1, 3.0, 30.0, 7.0, 6.0, "Catching up on sleep."),
            // Day -20
            DailyEntryTemplate(7.0, 4.3, 3.3, 40.0, 8.0, 8.0, "Back on track."),
            // Day -21
            DailyEntryTemplate(6.8, 4.7, 3.0, 35.0, 7.0, 7.0, "Normal day."),
            // Day -22
            DailyEntryTemplate(7.3, 4.5, 3.8, 50.0, 8.0, 8.0, "Effective study session."),
            // Day -23
            DailyEntryTemplate(7.1, 4.2, 3.2, 40.0, 7.0, 7.0, "Stable metrics."),
            // Day -24
            DailyEntryTemplate(6.9, 5.0, 2.7, 25.0, 6.0, 6.0, "Slightly busy."),
            // Day -25
            DailyEntryTemplate(7.7, 3.9, 4.0, 60.0, 9.0, 9.0, "Peak productivity day."),
            // Day -26
            DailyEntryTemplate(7.0, 4.6, 3.1, 35.0, 7.0, 7.0, "Consistent routine."),
            // Day -27
            DailyEntryTemplate(6.8, 4.8, 2.9, 30.0, 7.0, 6.0, "Initial log day.")
        )

        for (i in baseTemplates.indices) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = dateFormat.format(cal.time)
            val template = baseTemplates[i]

            list.add(
                DailyEntry(
                    date = dateStr,
                    sleepHours = template.sleep,
                    screenTimeHours = template.screenTime,
                    studyHours = template.study,
                    exerciseMinutes = template.exercise,
                    mood = template.mood,
                    productivity = template.productivity,
                    timestamp = cal.timeInMillis,
                    notes = template.notes
                )
            )
        }

        return list.sortedByDescending { it.date }
    }

    private data class DailyEntryTemplate(
        val sleep: Double,
        val screenTime: Double,
        val study: Double,
        val exercise: Double,
        val mood: Double,
        val productivity: Double,
        val notes: String = ""
    )
}
