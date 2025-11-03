package com.dms.flip.domain.usecase.pleasures

import com.dms.flip.domain.model.Pleasure
import com.dms.flip.domain.repository.PleasureRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class UpdatePleasureUseCaseTest {

    private lateinit var pleasureRepository: PleasureRepository
    private lateinit var useCase: UpdatePleasureUseCase

    @Before
    fun setUp() {
        pleasureRepository = mock()
        useCase = UpdatePleasureUseCase(pleasureRepository)
    }

    @Test
    fun `invoke updates pleasure in repository`() = runTest {
        // Given
        val pleasure = Pleasure(id = "id-24", title = "Read a book")

        // When
        useCase(pleasure)

        // Then
        verify(pleasureRepository).update(pleasure)
    }
}
