package com.dms.flip.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dms.flip.domain.model.PleasureHistory
import com.dms.flip.domain.usecase.weekly.GetWeeklyHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getWeeklyHistoryUseCase: GetWeeklyHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadWeeklyHistory(weekOffset = 0)
    }

    fun onEvent(event: HistoryEvent) {
        when (event) {
            is HistoryEvent.OnRetryClicked -> retry()
            is HistoryEvent.OnCardClicked -> selectPleasure(event.item)
            is HistoryEvent.OnBottomSheetDismissed -> dismissBottomSheet()
            is HistoryEvent.OnPreviousWeekClicked -> navigateToPreviousWeek()
            is HistoryEvent.OnNextWeekClicked -> navigateToNextWeek()
            is HistoryEvent.OnDiscoverTodayClicked -> discoverToday()
            is HistoryEvent.OnScreenResumed -> refreshCurrentWeek()
        }
    }

    private fun retry() {
        loadWeeklyHistory(_uiState.value.weekOffset)
    }

    private fun selectPleasure(item: PleasureHistory) {
        _uiState.update { it.copy(selectedPleasureHistory = item) }
    }

    private fun dismissBottomSheet() {
        _uiState.update { it.copy(selectedPleasureHistory = null) }
    }

    private fun navigateToPreviousWeek() {
        val nextOffset = _uiState.value.weekOffset - 1
        loadWeeklyHistory(nextOffset)
    }

    private fun navigateToNextWeek() {
        val currentOffset = _uiState.value.weekOffset
        if (currentOffset >= 0) return
        val nextOffset = currentOffset + 1
        loadWeeklyHistory(nextOffset)
    }

    private fun discoverToday() {

    }

    private var loadHistoryJob: Job? = null

    private fun refreshCurrentWeek() {
        val currentOffset = _uiState.value.weekOffset
        loadWeeklyHistory(currentOffset, showLoading = false)
    }

    private fun loadWeeklyHistory(weekOffset: Int, showLoading: Boolean = true) {
        loadHistoryJob?.cancel()
        loadHistoryJob = viewModelScope.launch {
            val weekBounds = calculateWeekBounds(weekOffset)
            val weekLabels = calculateWeekLabels(weekOffset, weekBounds)

            getWeeklyHistoryUseCase()
                .onStart {
                    _uiState.update {
                        it.copy(
                            isLoading = showLoading,
                            error = null,
                            weekOffset = weekOffset,
                            weekTitle = weekLabels.title,
                            weekDates = weekLabels.dates,
                            canNavigateToNextWeek = weekOffset < 0
                        )
                    }
                }
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Une erreur inconnue est survenue"
                        )
                    }
                }
                .collect { allEntries ->
                    val filteredEntries = filterEntriesForWeek(allEntries, weekBounds)
                    val weeklyDays = generateWeeklyDays(weekBounds.start, filteredEntries)
                    val streak = calculateStreak(filteredEntries, weekBounds.end)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            weeklyDays = weeklyDays,
                            streakDays = streak,
                            error = null
                        )
                    }
                }
        }
    }

    // ========== Date Utils ==========

    private data class WeekBounds(val start: Long, val end: Long)
    private data class WeekLabels(val title: String, val dates: String)

    private fun calculateWeekBounds(weekOffset: Int): WeekBounds {
        val calendar = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            add(Calendar.WEEK_OF_YEAR, weekOffset)
        }

        val start = calendar.timeInMillis
        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        val end = calendar.timeInMillis

        return WeekBounds(start, end)
    }

    private fun calculateWeekLabels(weekOffset: Int, bounds: WeekBounds): WeekLabels {
        val title = when {
            weekOffset == 0 -> "Cette Semaine"
            weekOffset == -1 -> "Semaine dernière"
            weekOffset == 1 -> "Semaine prochaine"
            weekOffset < 0 -> "Il y a ${-weekOffset} semaines"
            else -> "Dans $weekOffset semaines"
        }

        val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
        val calendar = Calendar.getInstance()

        calendar.timeInMillis = bounds.start
        val startDate = dateFormat.format(calendar.time)

        calendar.timeInMillis = bounds.end - 1
        val endDate = dateFormat.format(calendar.time)

        val dates = "$startDate — $endDate"

        return WeekLabels(title, dates)
    }

    private fun filterEntriesForWeek(
        allEntries: List<PleasureHistory>,
        bounds: WeekBounds
    ): List<PleasureHistory> {
        return allEntries.filter { entry ->
            entry.dateDrawn in bounds.start until bounds.end
        }
    }

    private fun generateWeeklyDays(
        weekStartMillis: Long,
        historyEntries: List<PleasureHistory>
    ): List<WeeklyDay> {
        return (0 until 7).map { dayOffset ->
            val dayCalendar = Calendar.getInstance().apply {
                timeInMillis = weekStartMillis
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }

            val dayName = getDayName(dayOffset + 1)
            val entryForDay = historyEntries.find { entry ->
                isSameDay(entry.dateDrawn, dayCalendar.timeInMillis)
            }

            WeeklyDay(
                dayName = dayName,
                historyEntry = entryForDay,
                dateMillis = dayCalendar.timeInMillis
            )
        }
    }

    private fun calculateStreak(
        entriesOfWeek: List<PleasureHistory>,
        weekEnd: Long
    ): Int {
        if (entriesOfWeek.isEmpty()) return 0

        val calendar = Calendar.getInstance().apply {
            timeInMillis = weekEnd - 1
        }

        var streak = 0
        repeat(7) {
            val hasCompletedThatDay = entriesOfWeek.any { entry ->
                entry.completed && isSameDay(entry.dateDrawn, calendar.timeInMillis)
            }

            if (hasCompletedThatDay) {
                streak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                return streak
            }
        }

        return streak
    }

    private fun isSameDay(timestampA: Long, timestampB: Long): Boolean {
        val calendarA = Calendar.getInstance().apply { timeInMillis = timestampA }
        val calendarB = Calendar.getInstance().apply { timeInMillis = timestampB }

        return calendarA.get(Calendar.YEAR) == calendarB.get(Calendar.YEAR) &&
                calendarA.get(Calendar.DAY_OF_YEAR) == calendarB.get(Calendar.DAY_OF_YEAR)
    }
}
