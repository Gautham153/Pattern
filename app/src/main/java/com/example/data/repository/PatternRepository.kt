package com.example.data.repository

import com.example.data.local.DailyEntryDao
import com.example.data.local.DailyEntryEntity
import com.example.data.model.DailyEntry
import com.example.data.sample.SampleDataGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PatternRepository(
    private val dao: DailyEntryDao
) {
    val allEntries: Flow<List<DailyEntry>> = dao.getAllEntriesFlow().map { entities ->
        entities.map { it.toDailyEntry() }
    }

    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        val count = dao.getCount()
        if (count == 0) {
            val sampleDays = SampleDataGenerator.generateSampleDays()
            dao.insertAll(sampleDays.map { DailyEntryEntity.fromDailyEntry(it) })
        }
    }

    suspend fun saveEntry(entry: DailyEntry) = withContext(Dispatchers.IO) {
        dao.insertOrUpdate(DailyEntryEntity.fromDailyEntry(entry))
    }

    suspend fun getEntryByDate(date: String): DailyEntry? = withContext(Dispatchers.IO) {
        dao.getEntryByDate(date)?.toDailyEntry()
    }

    suspend fun deleteEntry(date: String) = withContext(Dispatchers.IO) {
        dao.deleteByDate(date)
    }

    suspend fun resetToSampleData() = withContext(Dispatchers.IO) {
        dao.deleteAll()
        val sampleDays = SampleDataGenerator.generateSampleDays()
        dao.insertAll(sampleDays.map { DailyEntryEntity.fromDailyEntry(it) })
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        dao.deleteAll()
    }
}
