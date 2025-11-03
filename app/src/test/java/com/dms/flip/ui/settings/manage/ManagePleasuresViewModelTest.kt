package com.dms.flip.ui.settings.manage

import android.content.res.Resources
import com.dms.flip.R
import com.dms.flip.data.model.PleasureCategory
import com.dms.flip.domain.model.Pleasure
import com.dms.flip.domain.usecase.pleasures.AddPleasureUseCase
import com.dms.flip.domain.usecase.pleasures.DeletePleasuresUseCase
import com.dms.flip.domain.usecase.pleasures.GetPleasuresUseCase
import com.dms.flip.domain.usecase.pleasures.UpdatePleasureUseCase
import com.dms.flip.testing.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ManagePleasuresViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var resources: Resources
    private lateinit var getPleasuresUseCase: GetPleasuresUseCase
    private lateinit var updatePleasureUseCase: UpdatePleasureUseCase
    private lateinit var addPleasureUseCase: AddPleasureUseCase
    private lateinit var deletePleasuresUseCase: DeletePleasuresUseCase

    @Before
    fun setUp() {
        resources = mock()
        getPleasuresUseCase = mock()
        updatePleasureUseCase = mock()
        addPleasureUseCase = mock()
        deletePleasuresUseCase = mock()
        whenever(resources.getString(R.string.generic_error_message)).thenReturn("Une erreur est survenue")
    }

    @Test
    fun `init loads pleasures and updates state`() = runTest {
        val pleasures = listOf(
            Pleasure(
                id = "1",
                title = "Read a book",
                description = "Spend 30 minutes reading",
                category = PleasureCategory.LEARNING,
                isEnabled = true
            )
        )
        whenever(getPleasuresUseCase.invoke()).thenReturn(flowOf(pleasures))

        val viewModel = createViewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.pleasures).isEqualTo(pleasures)
        assertThat(state.error).isNull()
        verify(getPleasuresUseCase).invoke()
    }

    @Test
    fun `toggling pleasure updates repository and state`() = runTest {
        val pleasures = listOf(
            Pleasure(
                id = "1",
                title = "Read",
                description = "Desc",
                category = PleasureCategory.LEARNING,
                isEnabled = true
            )
        )
        whenever(getPleasuresUseCase.invoke()).thenReturn(flowOf(pleasures))
        whenever(updatePleasureUseCase.invoke(any())).thenReturn(Unit)

        val viewModel = createViewModel()

        advanceUntilIdle()

        viewModel.onEvent(ManagePleasuresEvent.OnPleasureToggled(pleasures.first()))

        advanceUntilIdle()

        val updatedPleasure = pleasures.first().copy(isEnabled = false)
        verify(updatePleasureUseCase).invoke(updatedPleasure)
        assertThat(viewModel.uiState.value.pleasures).containsExactly(updatedPleasure)
    }

    @Test
    fun `saving pleasure with blank fields sets validation errors`() = runTest {
        whenever(getPleasuresUseCase.invoke()).thenReturn(flowOf(emptyList()))

        val viewModel = createViewModel()

        advanceUntilIdle()

        viewModel.onEvent(ManagePleasuresEvent.OnSavePleasureClicked)

        val state = viewModel.uiState.value
        assertThat(state.titleError).isNotNull()
        assertThat(state.descriptionError).isNotNull()
    }

    @Test
    fun `saving valid pleasure delegates to use case and resets form`() = runTest {
        whenever(getPleasuresUseCase.invoke()).thenReturn(flowOf(emptyList()))
        whenever(addPleasureUseCase.invoke(any(), any(), any())).thenReturn(Unit)

        val viewModel = createViewModel()

        advanceUntilIdle()

        viewModel.onEvent(ManagePleasuresEvent.OnAddPleasureClicked)
        viewModel.onEvent(ManagePleasuresEvent.OnTitleChanged("  Yoga  "))
        viewModel.onEvent(ManagePleasuresEvent.OnDescriptionChanged("  Morning session  "))
        viewModel.onEvent(ManagePleasuresEvent.OnCategoryChanged(PleasureCategory.WELLNESS))

        viewModel.onEvent(ManagePleasuresEvent.OnSavePleasureClicked)

        advanceUntilIdle()

        verify(addPleasureUseCase).invoke("Yoga", "Morning session", PleasureCategory.WELLNESS)
        val state = viewModel.uiState.value
        assertThat(state.showAddDialog).isFalse()
        assertThat(state.newPleasureTitle).isEmpty()
        assertThat(state.newPleasureDescription).isEmpty()
        assertThat(state.newPleasureCategory).isEqualTo(PleasureCategory.FOOD)
    }

    private fun createViewModel(): ManagePleasuresViewModel {
        return ManagePleasuresViewModel(
            resources = resources,
            getPleasuresUseCase = getPleasuresUseCase,
            updatePleasureUseCase = updatePleasureUseCase,
            addPleasureUseCase = addPleasureUseCase,
            deletePleasuresUseCase = deletePleasuresUseCase
        )
    }
}
