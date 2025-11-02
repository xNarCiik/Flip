package com.dms.flip.ui.history

import com.dms.flip.domain.model.PleasureHistory
import java.util.Locale

data class HistoryUiState(
    val isLoading: Boolean = false,
    val weeklyDays: List<WeeklyDay> = emptyList(),
    val error: String? = null,
    val selectedPleasureHistory: PleasureHistory? = null,
    val weekTitle: String = "Cette Semaine",
    val weekDates: String = "",
    val streakDays: Int = 0,
    val weekOffset: Int = 0,
    val canNavigateToNextWeek: Boolean = false
)

data class WeeklyDay(
    val dayName: String,
    val historyEntry: PleasureHistory?,
    val dateMillis: Long
)

sealed interface HistoryEvent {
    data object OnRetryClicked : HistoryEvent
    data class OnCardClicked(val item: PleasureHistory) : HistoryEvent
    data object OnBottomSheetDismissed : HistoryEvent
    data object OnPreviousWeekClicked : HistoryEvent
    data object OnNextWeekClicked : HistoryEvent
    data object OnDiscoverTodayClicked : HistoryEvent
}

fun getDayName(dayOfWeek: Int): String {
    return when (dayOfWeek) {
        1 -> "Lundi"
        2 -> "Mardi"
        3 -> "Mercredi"
        4 -> "Jeudi"
        5 -> "Vendredi"
        6 -> "Samedi"
        7 -> "Dimanche"
        else -> ""
    }
}
