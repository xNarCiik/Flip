package com.dms.flip.domain.usecase.pleasures

import com.dms.flip.data.model.PleasureCategory
import com.dms.flip.domain.model.Pleasure
import com.dms.flip.domain.repository.PleasureRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class AddPleasureUseCaseTest {

    private lateinit var pleasureRepository: PleasureRepository
    private lateinit var useCase: AddPleasureUseCase

    @Before
    fun setUp() {
        pleasureRepository = mock()
        useCase = AddPleasureUseCase(pleasureRepository)
    }

    @Test
    fun `invoke creates enabled pleasure and inserts it`() = runTest {
        // Given
        val title = "Yoga"
        val description = "Relaxing morning routine"
        val category = PleasureCategory.OUTDOOR
        val pleasureCaptor = argumentCaptor<Pleasure>()

        // When
        useCase(title = title, description = description, category = category)

        // Then
        verify(pleasureRepository).insert(pleasureCaptor.capture())
        val createdPleasure = pleasureCaptor.firstValue
        assertThat(createdPleasure.title).isEqualTo(title)
        assertThat(createdPleasure.description).isEqualTo(description)
        assertThat(createdPleasure.category).isEqualTo(category)
        assertThat(createdPleasure.isEnabled).isTrue()
        assertThat(createdPleasure.id).isEmpty()
    }
}
