package com.dms.flip.domain.usecase.pleasures

import com.dms.flip.domain.repository.PleasureRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class DeletePleasuresUseCaseTest {

    private lateinit var pleasureRepository: PleasureRepository
    private lateinit var useCase: DeletePleasuresUseCase

    @Before
    fun setUp() {
        pleasureRepository = mock()
        useCase = DeletePleasuresUseCase(pleasureRepository)
    }

    @Test
    fun `invoke deletes pleasures by ids`() = runTest {
        // Given
        val ids = listOf("1", "2", "3")

        // When
        useCase(ids)

        // Then
        verify(pleasureRepository).delete(ids)
    }
}
