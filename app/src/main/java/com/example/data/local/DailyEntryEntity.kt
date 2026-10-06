package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DailyEntry

@Entity(tableName = "daily_entries")
data class DailyEntryEntity(
    @PrimaryKey
    val date: String, // "yyyy-MM-dd"
    val sleepHours: Double,
    val screenTimeHours: Double,
    val studyHours: Double,
    val exerciseMinutes: Double,
    val mood: Double,
    val productivity: Double,
    val timestamp: Long,
    val notes: String
) {
    fun toDailyEntry(): DailyEntry {
        return DailyEntry(
            date = date,
            sleepHours = sleepHours,
            screenTimeHours = screenTimeHours,
            studyHours = studyHours,
            exerciseMinutes = exerciseMinutes,
            mood = mood,
            productivity = productivity,
            timestamp = timestamp,
            notes = notes
        )
    }

    companion object {
        fun fromDailyEntry(entry: DailyEntry): DailyEntryEntity {
            return DailyEntryEntity(
                date = entry.date,
                sleepHours = entry.sleepHours,
                screenTimeHours = entry.screenTimeHours,
                studyHours = entry.studyHours,
                exerciseMinutes = entry.exerciseMinutes,
                mood = entry.mood,
                productivity = entry.productivity,
                timestamp = entry.timestamp,
                notes = entry.notes
            )
        }
    }
}
