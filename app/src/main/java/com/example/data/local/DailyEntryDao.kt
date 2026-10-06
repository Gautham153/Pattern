package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyEntryDao {

    @Query("SELECT * FROM daily_entries ORDER BY date DESC")
    fun getAllEntriesFlow(): Flow<List<DailyEntryEntity>>

    @Query("SELECT * FROM daily_entries ORDER BY date DESC")
    suspend fun getAllEntries(): List<DailyEntryEntity>

    @Query("SELECT * FROM daily_entries WHERE date = :date LIMIT 1")
    suspend fun getEntryByDate(date: String): DailyEntryEntity?

    @Query("SELECT * FROM daily_entries WHERE date = :date LIMIT 1")
    fun getEntryByDateFlow(date: String): Flow<DailyEntryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: DailyEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<DailyEntryEntity>)

    @Query("DELETE FROM daily_entries WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("DELETE FROM daily_entries")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM daily_entries")
    suspend fun getCount(): Int
}
