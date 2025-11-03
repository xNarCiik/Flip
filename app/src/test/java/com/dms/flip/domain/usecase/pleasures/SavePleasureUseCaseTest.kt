package com.dms.flip.domain.usecase.pleasures

import com.dms.flip.domain.model.Pleasure
import com.dms.flip.domain.repository.PleasureRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class SavePleasureUseCaseTest {

    private lateinit var pleasureRepository: PleasureRepository
    private lateinit var useCase: SavePleasureUseCase

    @Before
    fun setUp() {
        pleasureRepository = mock()
        useCase = SavePleasureUseCase(pleasureRepository)
    }

    @Test
    fun `invoke forwards pleasure to repository`() = runTest {
        // Given
        val pleasure = Pleasure(id = "id-42")

        // When
        useCase(pleasure)

        // Then
        verify(pleasureRepository).insert(pleasure)
    }
}
