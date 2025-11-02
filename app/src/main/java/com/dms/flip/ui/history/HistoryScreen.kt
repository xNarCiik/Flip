package com.dms.flip.ui.history

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dms.flip.R
import com.dms.flip.ui.component.FlipTopBar
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
    val lifecycleOwner = LocalLifecycleOwner.current
    val hasResumedOnce = remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (hasResumedOnce.value) {
                    onEvent(HistoryEvent.OnScreenResumed)
                } else {
                    hasResumedOnce.value = true
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(modifier = modifier.fillMaxSize()) {
        FlipTopBar(title = stringResource(id = R.string.history_title))

        Box(modifier = Modifier.fillMaxSize()) {
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

        AnimatedContent(
            targetState = Pair(completedCount, streakDays),
            transitionSpec = {
                fadeIn(animationSpec = tween(250)) togetherWith
                        fadeOut(animationSpec = tween(200))
            },
            label = "HistoryStatsTransition"
        ) { (count, streak) ->
            WeeklyStatsGrid(
                pleasuresCount = count,
                streakDays = streak
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Weekly pleasures list or state feedback
        val hasHistoryEntries = weeklyDays.any { it.historyEntry != null }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp)
        ) {
            if (error != null && !hasHistoryEntries) {
                Text(
                    text = stringResource(R.string.generic_error_message, error),
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            } else {
                AnimatedContent(
                    targetState = weeklyDays,
                    transitionSpec = {
                        (slideInVertically(
                            animationSpec = tween(350),
                            initialOffsetY = { fullHeight -> fullHeight / 8 }
                        ) + fadeIn(animationSpec = tween(350))) togetherWith
                                (slideOutVertically(
                                    animationSpec = tween(250),
                                    targetOffsetY = { fullHeight -> -fullHeight / 8 }
                                ) + fadeOut(animationSpec = tween(200)))
                    },
                    label = "HistoryListTransition"
                ) { days ->
                    WeeklyPleasuresList(
                        items = days,
                        onCardClicked = { item -> onEvent(HistoryEvent.OnCardClicked(item)) },
                        onDiscoverTodayClicked = navigateToDailyFlip
                    )
                }

                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    visible = error != null,
                    enter = fadeIn(animationSpec = tween(200)) +
                            slideInVertically(
                                animationSpec = tween(200),
                                initialOffsetY = { it / 4 }
                            ),
                    exit = fadeOut(animationSpec = tween(200)) +
                            slideOutVertically(
                                animationSpec = tween(200),
                                targetOffsetY = { it / 4 }
                            )
                ) {
                    Text(
                        text = stringResource(R.string.generic_error_message, error ?: ""),
                        modifier = Modifier
                            .padding(16.dp)
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            if (isLoading) {
                LoadingState(
                    modifier = Modifier.matchParentSize(),
                    backgroundColor = Color.Transparent
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
