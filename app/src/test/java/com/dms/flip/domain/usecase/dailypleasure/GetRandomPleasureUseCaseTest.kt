package com.dms.flip.domain.usecase.dailypleasure

import com.dms.flip.data.model.PleasureCategory
import com.dms.flip.domain.model.Pleasure
import com.dms.flip.domain.repository.PleasureRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GetRandomPleasureUseCaseTest {

    private lateinit var pleasureRepository: PleasureRepository
    private lateinit var useCase: GetRandomPleasureUseCase

    @Before
    fun setUp() {
        pleasureRepository = mock()
        useCase = GetRandomPleasureUseCase(pleasureRepository)
    }

    @Test
    fun `invoke returns random pleasure from repository`() = runTest {
        // Given
        val category = PleasureCategory.BODY
        val pleasure = Pleasure(id = "42")
        whenever(pleasureRepository.getRandomPleasure(category)).thenReturn(flowOf(pleasure))

        // When
        val result = useCase(category).first()

        // Then
        assertThat(result).isEqualTo(pleasure)
        verify(pleasureRepository).getRandomPleasure(category)
    }
}
