package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.analytics.PatternAnalytics
import com.example.data.local.PatternDatabase
import com.example.data.model.AnomalyRecord
import com.example.data.model.CorrelationResult
import com.example.data.model.DailyEntry
import com.example.data.model.MetricStats
import com.example.data.model.MetricType
import com.example.data.repository.PatternRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class AppTab {
    HOME,
    LOG,
    INSIGHTS,
    TRENDS,
    HISTORY
}

enum class InsightsTab {
    OVERVIEW,
    BASELINE,
    CORRELATIONS
}

enum class TrendRange(val days: Int, val label: String) {
    DAYS_7(7, "7 days"),
    DAYS_14(14, "14 days"),
    DAYS_30(30, "30 days")
}

data class LogFormState(
    val date: String = DailyEntry.todayDateString(),
    val sleepHours: Double = 7.0,
    val screenTimeHours: Double = 4.5,
    val studyHours: Double = 3.0,
    val exerciseMinutes: Double = 30.0,
    val mood: Double = 7.0,
    val productivity: Double = 7.0,
    val notes: String = "",
    val isEditing: Boolean = false,
    val errorMessage: String? = null
)

data class ControlsState(
    val currentTab: AppTab = AppTab.HOME,
    val selectedInsightsTab: InsightsTab = InsightsTab.OVERVIEW,
    val selectedTrendMetric: MetricType = MetricType.SLEEP,
    val selectedTrendRange: TrendRange = TrendRange.DAYS_7,
    val historySearchQuery: String = "",
    val historyAnomaliesOnly: Boolean = false,
    val selectedAnomalyForDetails: AnomalyRecord? = null,
    val showOnboardingHero: Boolean = false,
    val logForm: LogFormState = LogFormState(),
    val userName: String = "Gautham"
)

data class PatternUiState(
    val allEntries: List<DailyEntry> = emptyList(),
    val currentTab: AppTab = AppTab.HOME,
    val userName: String = "Gautham",
    val patternScore: Int = 100,
    val weeklyAnomaliesCount: Int = 0,
    val todayEntry: DailyEntry? = null,
    val allAnomalies: List<AnomalyRecord> = emptyList(),
    val recentAnomalies: List<AnomalyRecord> = emptyList(),
    val statsMap: Map<MetricType, MetricStats> = emptyMap(),
    val correlations: List<CorrelationResult> = emptyList(),
    val selectedInsightsTab: InsightsTab = InsightsTab.OVERVIEW,
    val selectedTrendMetric: MetricType = MetricType.SLEEP,
    val selectedTrendRange: TrendRange = TrendRange.DAYS_7,
    val historySearchQuery: String = "",
    val historyAnomaliesOnly: Boolean = false,
    val selectedAnomalyForDetails: AnomalyRecord? = null,
    val showOnboardingHero: Boolean = false,
    val logForm: LogFormState = LogFormState()
)

class PatternViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PatternRepository
    private val _controls = MutableStateFlow(ControlsState())

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    init {
        val database = PatternDatabase.getDatabase(application)
        repository = PatternRepository(database.dailyEntryDao())

        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    val uiState: StateFlow<PatternUiState> = combine(
        repository.allEntries,
        _controls
    ) { entries: List<DailyEntry>, controls: ControlsState ->
        val sortedEntries = entries.sortedByDescending { it.date }
        val todayStr = DailyEntry.todayDateString()
        val todayEntry = sortedEntries.find { it.date == todayStr }

        val statsMap = PatternAnalytics.calculateAllStats(sortedEntries)
        val allAnomalies = PatternAnalytics.getAllAnomalies(sortedEntries)

        val sevenDaysAgoCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val sevenDaysAgoStr = dateFormat.format(sevenDaysAgoCal.time)

        val weeklyAnomalies = allAnomalies.filter { it.date >= sevenDaysAgoStr }
        val recent7Entries = sortedEntries.filter { it.date >= sevenDaysAgoStr }
        val patternScore = PatternAnalytics.calculatePatternScore(recent7Entries, sortedEntries)
        val correlations = PatternAnalytics.generateCorrelations(sortedEntries)

        PatternUiState(
            allEntries = sortedEntries,
            currentTab = controls.currentTab,
            userName = controls.userName,
            patternScore = patternScore,
            weeklyAnomaliesCount = weeklyAnomalies.size,
            todayEntry = todayEntry,
            allAnomalies = allAnomalies,
            recentAnomalies = allAnomalies.take(10),
            statsMap = statsMap,
            correlations = correlations,
            selectedInsightsTab = controls.selectedInsightsTab,
            selectedTrendMetric = controls.selectedTrendMetric,
            selectedTrendRange = controls.selectedTrendRange,
            historySearchQuery = controls.historySearchQuery,
            historyAnomaliesOnly = controls.historyAnomaliesOnly,
            selectedAnomalyForDetails = controls.selectedAnomalyForDetails,
            showOnboardingHero = controls.showOnboardingHero,
            logForm = controls.logForm
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PatternUiState()
    )

    fun navigateToTab(tab: AppTab) {
        _controls.update { it.copy(currentTab = tab) }
    }

    fun setInsightsTab(tab: InsightsTab) {
        _controls.update { it.copy(selectedInsightsTab = tab) }
    }

    fun setTrendMetric(metric: MetricType) {
        _controls.update { it.copy(selectedTrendMetric = metric) }
    }

    fun setTrendRange(range: TrendRange) {
        _controls.update { it.copy(selectedTrendRange = range) }
    }

    fun setHistorySearchQuery(query: String) {
        _controls.update { it.copy(historySearchQuery = query) }
    }

    fun toggleHistoryAnomaliesOnly() {
        _controls.update { it.copy(historyAnomaliesOnly = !it.historyAnomaliesOnly) }
    }

    fun selectAnomalyForDetails(anomaly: AnomalyRecord?) {
        _controls.update { it.copy(selectedAnomalyForDetails = anomaly) }
    }

    fun setShowOnboardingHero(show: Boolean) {
        _controls.update { it.copy(showOnboardingHero = show) }
    }

    fun openLogScreenForNewDay(prefillDate: String = DailyEntry.todayDateString()) {
        val existing = uiState.value.allEntries.find { it.date == prefillDate }
        if (existing != null) {
            openEditScreenForEntry(existing)
        } else {
            val stats = uiState.value.statsMap
            val defaultForm = LogFormState(
                date = prefillDate,
                sleepHours = if ((stats[MetricType.SLEEP]?.mean ?: 0.0) > 0.0) roundToOneDec(stats[MetricType.SLEEP]!!.mean) else 7.0,
                screenTimeHours = if ((stats[MetricType.SCREEN_TIME]?.mean ?: 0.0) > 0.0) roundToOneDec(stats[MetricType.SCREEN_TIME]!!.mean) else 4.5,
                studyHours = if ((stats[MetricType.STUDY]?.mean ?: 0.0) > 0.0) roundToOneDec(stats[MetricType.STUDY]!!.mean) else 3.0,
                exerciseMinutes = if ((stats[MetricType.EXERCISE]?.mean ?: 0.0) > 0.0) stats[MetricType.EXERCISE]!!.mean.toInt().toDouble() else 30.0,
                mood = if ((stats[MetricType.MOOD]?.mean ?: 0.0) > 0.0) stats[MetricType.MOOD]!!.mean.toInt().toDouble() else 7.0,
                productivity = if ((stats[MetricType.PRODUCTIVITY]?.mean ?: 0.0) > 0.0) stats[MetricType.PRODUCTIVITY]!!.mean.toInt().toDouble() else 7.0,
                notes = "",
                isEditing = false,
                errorMessage = null
            )
            _controls.update {
                it.copy(
                    logForm = defaultForm,
                    currentTab = AppTab.LOG
                )
            }
        }
    }

    fun openEditScreenForEntry(entry: DailyEntry) {
        val editForm = LogFormState(
            date = entry.date,
            sleepHours = entry.sleepHours,
            screenTimeHours = entry.screenTimeHours,
            studyHours = entry.studyHours,
            exerciseMinutes = entry.exerciseMinutes,
            mood = entry.mood,
            productivity = entry.productivity,
            notes = entry.notes,
            isEditing = true,
            errorMessage = null
        )
        _controls.update {
            it.copy(
                logForm = editForm,
                currentTab = AppTab.LOG
            )
        }
    }

    fun updateFormMetric(metric: MetricType, value: Double) {
        val clamped = value.coerceIn(metric.minAllowed, metric.maxAllowed)
        _controls.update { state ->
            val current = state.logForm
            val updatedForm = when (metric) {
                MetricType.SLEEP -> current.copy(sleepHours = roundToOneDec(clamped))
                MetricType.SCREEN_TIME -> current.copy(screenTimeHours = roundToOneDec(clamped))
                MetricType.STUDY -> current.copy(studyHours = roundToOneDec(clamped))
                MetricType.EXERCISE -> current.copy(exerciseMinutes = clamped.toInt().toDouble())
                MetricType.MOOD -> current.copy(mood = clamped.toInt().toDouble())
                MetricType.PRODUCTIVITY -> current.copy(productivity = clamped.toInt().toDouble())
            }
            state.copy(logForm = updatedForm)
        }
    }

    fun updateFormDate(dateStr: String) {
        val existing = uiState.value.allEntries.find { it.date == dateStr }
        _controls.update { state ->
            val updatedForm = if (existing != null) {
                state.logForm.copy(
                    date = dateStr,
                    sleepHours = existing.sleepHours,
                    screenTimeHours = existing.screenTimeHours,
                    studyHours = existing.studyHours,
                    exerciseMinutes = existing.exerciseMinutes,
                    mood = existing.mood,
                    productivity = existing.productivity,
                    notes = existing.notes,
                    isEditing = true
                )
            } else {
                state.logForm.copy(
                    date = dateStr,
                    isEditing = false
                )
            }
            state.copy(logForm = updatedForm)
        }
    }

    fun updateFormNotes(notes: String) {
        _controls.update { state ->
            state.copy(logForm = state.logForm.copy(notes = notes))
        }
    }

    fun saveCurrentLogForm() {
        val form = uiState.value.logForm
        if (form.date.isBlank()) {
            _controls.update { it.copy(logForm = form.copy(errorMessage = "Please select a valid date")) }
            return
        }

        viewModelScope.launch {
            val entry = DailyEntry(
                date = form.date,
                sleepHours = form.sleepHours,
                screenTimeHours = form.screenTimeHours,
                studyHours = form.studyHours,
                exerciseMinutes = form.exerciseMinutes,
                mood = form.mood,
                productivity = form.productivity,
                timestamp = System.currentTimeMillis(),
                notes = form.notes.trim()
            )

            repository.saveEntry(entry)
            _snackbarMessage.emit(if (form.isEditing) "Changes saved for ${entry.formattedDateShort()}" else "Day logged for ${entry.formattedDateShort()}")
            _controls.update { it.copy(currentTab = AppTab.HOME) }
        }
    }

    fun deleteCurrentLogEntry() {
        val date = uiState.value.logForm.date
        viewModelScope.launch {
            repository.deleteEntry(date)
            _snackbarMessage.emit("Entry deleted for $date")
            _controls.update { it.copy(currentTab = AppTab.HOME) }
        }
    }

    fun deleteEntryByDate(date: String) {
        viewModelScope.launch {
            repository.deleteEntry(date)
            _snackbarMessage.emit("Entry deleted for $date")
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetToSampleData()
            _snackbarMessage.emit("Restored 28-day sample baseline data")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _snackbarMessage.emit("All logged data cleared")
        }
    }

    private fun roundToOneDec(num: Double): Double {
        return Math.round(num * 10.0) / 10.0
    }
}
