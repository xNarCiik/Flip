package com.dms.flip.ui.history

import com.dms.flip.domain.model.community.PleasureCategory
import com.dms.flip.domain.model.PleasureHistory
import com.dms.flip.domain.usecase.weekly.GetWeeklyHistoryUseCase
import com.dms.flip.testing.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getWeeklyHistoryUseCase: GetWeeklyHistoryUseCase

    @Before
    fun setUp() {
        getWeeklyHistoryUseCase = mock()
    }

    @Test
    fun `init loads current week history`() = runTest {
        val mondayEntry = historyForWeek(weekOffset = 0, dayOffset = 0, completed = false)
        val saturdayEntry = historyForWeek(weekOffset = 0, dayOffset = 5, completed = true)
        val sundayEntry = historyForWeek(weekOffset = 0, dayOffset = 6, completed = true)
        whenever(getWeeklyHistoryUseCase.invoke()).thenReturn(
            flowOf(listOf(mondayEntry, saturdayEntry, sundayEntry))
        )

        val viewModel = createViewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
        assertThat(state.weekOffset).isEqualTo(0)
        assertThat(state.weeklyDays).hasSize(7)
        assertThat(state.weeklyDays.first().historyEntry).isEqualTo(mondayEntry)
        assertThat(state.streakDays).isEqualTo(2)
        assertThat(state.canNavigateToNextWeek).isFalse()
        assertThat(state.weekTitle).isEqualTo("Cette Semaine")
        assertThat(state.weekDates).isNotEmpty()
    }

    @Test
    fun `navigating between weeks updates offset and available entries`() = runTest {
        val currentWeekEntries = listOf(
            historyForWeek(weekOffset = 0, dayOffset = 1, completed = false),
            historyForWeek(weekOffset = 0, dayOffset = 6, completed = true)
        )
        val previousWeekEntry = historyForWeek(weekOffset = -1, dayOffset = 3, completed = true)
        whenever(getWeeklyHistoryUseCase.invoke()).thenReturn(
            flowOf(currentWeekEntries + previousWeekEntry)
        )

        val viewModel = createViewModel()

        advanceUntilIdle()

        viewModel.onEvent(HistoryEvent.OnPreviousWeekClicked)
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertThat(state.weekOffset).isEqualTo(-1)
        assertThat(state.canNavigateToNextWeek).isTrue()
        assertThat(state.weeklyDays[3].historyEntry).isEqualTo(previousWeekEntry)

        viewModel.onEvent(HistoryEvent.OnNextWeekClicked)
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertThat(state.weekOffset).isEqualTo(0)
        assertThat(state.canNavigateToNextWeek).isFalse()
    }

    @Test
    fun `selecting and dismissing a history entry updates selection`() = runTest {
        val entry = historyForWeek(weekOffset = 0, dayOffset = 2, completed = true)
        whenever(getWeeklyHistoryUseCase.invoke()).thenReturn(flowOf(listOf(entry)))

        val viewModel = createViewModel()

        advanceUntilIdle()

        viewModel.onEvent(HistoryEvent.OnCardClicked(entry))
        assertThat(viewModel.uiState.value.selectedPleasureHistory).isEqualTo(entry)

        viewModel.onEvent(HistoryEvent.OnBottomSheetDismissed)
        assertThat(viewModel.uiState.value.selectedPleasureHistory).isNull()
    }

    @Test
    fun `retry reloads the current week`() = runTest {
        val entry = historyForWeek(weekOffset = 0, dayOffset = 0, completed = true)
        whenever(getWeeklyHistoryUseCase.invoke()).thenReturn(flowOf(listOf(entry)))

        val viewModel = createViewModel()

        advanceUntilIdle()

        viewModel.onEvent(HistoryEvent.OnRetryClicked)
        advanceUntilIdle()

        verify(getWeeklyHistoryUseCase, times(2)).invoke()
    }

    private fun createViewModel(): HistoryViewModel {
        return HistoryViewModel(
            getWeeklyHistoryUseCase = getWeeklyHistoryUseCase
        )
    }

    private fun historyForWeek(
        weekOffset: Int,
        dayOffset: Int,
        completed: Boolean
    ): PleasureHistory {
        val calendar = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            add(Calendar.WEEK_OF_YEAR, weekOffset)
            add(Calendar.DAY_OF_YEAR, dayOffset)
        }

        return PleasureHistory(
            id = "id_${weekOffset}_${dayOffset}",
            dateDrawn = calendar.timeInMillis,
            completed = completed,
            pleasureTitle = "P${dayOffset}",
            pleasureCategory = PleasureCategory.WELLNESS,
            pleasureDescription = "Description $dayOffset",
            completedAt = if (completed) calendar.timeInMillis else null
        )
    }
}
