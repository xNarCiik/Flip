package com.dms.flip.domain.usecase.statistics

import com.dms.flip.domain.repository.StatisticsRepository
import com.dms.flip.ui.settings.statistics.StatisticsUiState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetStatisticsUseCaseTest {

    private lateinit var statisticsRepository: StatisticsRepository
    private lateinit var useCase: GetStatisticsUseCase

    @Before
    fun setUp() {
        statisticsRepository = mock()
        useCase = GetStatisticsUseCase(statisticsRepository)
    }

    @Test
    fun `invoke returns statistics flow from repository`() = runTest {
        // Given
        val uiState = StatisticsUiState(isLoading = false)
        whenever(statisticsRepository.getStatistics()).thenReturn(flowOf(uiState))

        // When
        val result = useCase().first()

        // Then
        assertThat(result).isEqualTo(uiState)
    }
}
