package com.dms.flip.ui.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dms.flip.R
import com.dms.flip.ui.component.LoadingState
import com.dms.flip.ui.history.component.WeekNavigationHeader
import com.dms.flip.ui.history.component.WeeklyPleasuresList
import com.dms.flip.ui.history.component.WeeklyStatsGrid
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview
import com.dms.flip.ui.util.previewWeeklyDays

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    uiState: HistoryUiState,
    onEvent: (HistoryEvent) -> Unit,
    navigateToDailyFlip: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        HistoryContent(
            weeklyDays = uiState.weeklyDays,
            weekTitle = uiState.weekTitle,
            weekDates = uiState.weekDates,
            streakDays = uiState.streakDays,
            canNavigateToNextWeek = uiState.canNavigateToNextWeek,
            isLoading = uiState.isLoading,
            error = uiState.error,
            onEvent = onEvent,
            navigateToDailyFlip = navigateToDailyFlip
        )
    }
}

@Composable
private fun HistoryContent(
    modifier: Modifier = Modifier,
    weeklyDays: List<WeeklyDay>,
    weekTitle: String,
    weekDates: String,
    streakDays: Int,
    canNavigateToNextWeek: Boolean,
    isLoading: Boolean,
    error: String?,
    onEvent: (HistoryEvent) -> Unit,
    navigateToDailyFlip: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Header navigation
        WeekNavigationHeader(
            weekTitle = weekTitle,
            weekDates = weekDates,
            onPreviousWeekClick = { onEvent(HistoryEvent.OnPreviousWeekClicked) },
            onNextWeekClick = { onEvent(HistoryEvent.OnNextWeekClicked) },
            isNextEnabled = canNavigateToNextWeek
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Stats grid
        val history = weeklyDays.mapNotNull { it.historyEntry }
        val completedCount = history.count { it.completed }

        WeeklyStatsGrid(
            pleasuresCount = completedCount,
            streakDays = streakDays
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Weekly pleasures list or state feedback
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp)
                ) {
                    LoadingState(modifier = Modifier.align(Alignment.Center))
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp)
                ) {
                    Text(
                        text = stringResource(R.string.generic_error_message, error),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            else -> {
                WeeklyPleasuresList(
                    items = weeklyDays,
                    onCardClicked = { item -> onEvent(HistoryEvent.OnCardClicked(item)) },
                    onDiscoverTodayClicked = navigateToDailyFlip
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun HistoryScreenPreview() {
    FlipTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            HistoryScreen(
                uiState = HistoryUiState(weeklyDays = previewWeeklyDays),
                onEvent = {},
                navigateToDailyFlip = {}
            )
        }
    }
}
