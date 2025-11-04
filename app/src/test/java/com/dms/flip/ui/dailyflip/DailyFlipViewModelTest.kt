package com.dms.flip.ui.dailyflip

import android.content.res.Resources
import com.dms.flip.R
import com.dms.flip.domain.model.community.PleasureCategory
import com.dms.flip.domain.model.Pleasure
import com.dms.flip.domain.model.PleasureHistory
import com.dms.flip.domain.model.UserInfo
import com.dms.flip.domain.usecase.GetRandomDailyMessageUseCase
import com.dms.flip.domain.usecase.dailypleasure.GetRandomPleasureUseCase
import com.dms.flip.domain.usecase.history.GetTodayHistoryEntryUseCase
import com.dms.flip.domain.usecase.history.SaveHistoryEntryUseCase
import com.dms.flip.domain.usecase.pleasures.GetPleasuresUseCase
import com.dms.flip.domain.usecase.user.GetUserInfoUseCase
import com.dms.flip.testing.MainDispatcherRule
import com.dms.flip.ui.util.FlipFeedbackPlayer
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DailyFlipViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var resources: Resources
    private lateinit var getRandomDailyMessageUseCase: GetRandomDailyMessageUseCase
    private lateinit var getPleasuresUseCase: GetPleasuresUseCase
    private lateinit var getRandomPleasureUseCase: GetRandomPleasureUseCase
    private lateinit var saveHistoryEntryUseCase: SaveHistoryEntryUseCase
    private lateinit var getTodayHistoryEntryUseCase: GetTodayHistoryEntryUseCase
    private lateinit var getUserInfoUseCase: GetUserInfoUseCase
    private lateinit var flipFeedbackPlayer: FlipFeedbackPlayer

    private val userInfo = UserInfo(id = "user", username = "Ada")

    @Before
    fun setUp() {
        resources = mock()
        getRandomDailyMessageUseCase = mock()
        getPleasuresUseCase = mock()
        getRandomPleasureUseCase = mock()
        saveHistoryEntryUseCase = mock()
        getTodayHistoryEntryUseCase = mock()
        getUserInfoUseCase = mock()
        flipFeedbackPlayer = mock()

        whenever(resources.getString(R.string.your_flip_daily)).thenReturn("Ton flip du jour")
        whenever(resources.getString(R.string.generic_error_message)).thenReturn("Une erreur est survenue")
        whenever(getRandomDailyMessageUseCase.invoke()).thenReturn("Bonne journée !")
        whenever(getUserInfoUseCase.invoke()).thenReturn(flowOf(userInfo))
        whenever(getTodayHistoryEntryUseCase.invoke()).thenReturn(flowOf(null as PleasureHistory?))
    }

    @Test
    fun `init with insufficient pleasures shows setup required`() = runTest {
        val pleasures = List(3) { index ->
            Pleasure(id = index.toString(), isEnabled = index % 2 == 0)
        }
        whenever(getPleasuresUseCase.invoke()).thenReturn(flowOf(pleasures))

        val viewModel = createViewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.userInfo).isEqualTo(userInfo)
        val screenState = state.screenState as DailyFlipScreenState.SetupRequired
        assertThat(screenState.pleasureCount).isEqualTo(2)
    }

    @Test
    fun `drawing a card saves history and exposes the drawn pleasure`() = runTest {
        val pleasures = List(MinimumPleasuresCount) { index ->
            Pleasure(id = index.toString(), isEnabled = true)
        }
        val drawnPleasure = Pleasure(id = "daily", title = "Meditate", category = PleasureCategory.WELLNESS)
        whenever(getPleasuresUseCase.invoke()).thenReturn(flowOf(pleasures))
        whenever(getRandomPleasureUseCase.invoke(any())).thenReturn(flowOf(drawnPleasure))

        val viewModel = createViewModel()

        advanceUntilIdle()

        viewModel.onEvent(DailyFlipEvent.OnCardClicked)
        advanceUntilIdle()

        val readyState = viewModel.uiState.value.screenState as DailyFlipScreenState.Ready
        assertThat(readyState.dailyPleasure).isEqualTo(drawnPleasure)
        verify(saveHistoryEntryUseCase).invoke(drawnPleasure)
    }

    @Test
    fun `marking the card as done stores completion and moves to completed state`() = runTest {
        val pleasures = List(MinimumPleasuresCount) { index ->
            Pleasure(id = index.toString(), isEnabled = true)
        }
        val drawnPleasure = Pleasure(id = "daily", title = "Read")
        whenever(getPleasuresUseCase.invoke()).thenReturn(flowOf(pleasures))
        whenever(getRandomPleasureUseCase.invoke(any())).thenReturn(flowOf(drawnPleasure))

        val viewModel = createViewModel()

        advanceUntilIdle()

        viewModel.onEvent(DailyFlipEvent.OnCardClicked)
        advanceUntilIdle()
        clearInvocations(saveHistoryEntryUseCase)

        viewModel.onEvent(DailyFlipEvent.OnCardMarkedAsDone)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.screenState).isEqualTo(DailyFlipScreenState.Completed)
        verify(saveHistoryEntryUseCase).invoke(drawnPleasure, true)
    }

    @Test
    fun `flipping the card updates header message`() = runTest {
        val pleasures = List(MinimumPleasuresCount) { index ->
            Pleasure(id = index.toString(), isEnabled = true)
        }
        whenever(getPleasuresUseCase.invoke()).thenReturn(flowOf(pleasures))
        whenever(getRandomPleasureUseCase.invoke(any())).thenReturn(flowOf(Pleasure(id = "1")))

        val viewModel = createViewModel()

        advanceUntilIdle()

        viewModel.onEvent(DailyFlipEvent.OnCardClicked)
        advanceUntilIdle()

        viewModel.onEvent(DailyFlipEvent.OnCardFlipped)

        val readyState = viewModel.uiState.value.screenState as DailyFlipScreenState.Ready
        assertThat(readyState.isCardFlipped).isTrue()
        assertThat(viewModel.uiState.value.headerMessage).isEqualTo("Ton flip du jour")
    }

    @Test
    fun `failing to draw a card surfaces an error state`() = runTest {
        val pleasures = List(MinimumPleasuresCount) { index ->
            Pleasure(id = index.toString(), isEnabled = true)
        }
        whenever(getPleasuresUseCase.invoke()).thenReturn(flowOf(pleasures))
        whenever(getRandomPleasureUseCase.invoke(any())).thenReturn(flow {
            throw IllegalStateException("pas de plaisir")
        })

        val viewModel = createViewModel()

        advanceUntilIdle()

        viewModel.onEvent(DailyFlipEvent.OnCardClicked)
        advanceUntilIdle()

        val errorState = viewModel.uiState.value.screenState as DailyFlipScreenState.Error
        assertThat(errorState.message).isEqualTo("Impossible de tirer une carte : pas de plaisir")
    }

    private fun createViewModel(): DailyFlipViewModel {
        return DailyFlipViewModel(
            resources = resources,
            getRandomDailyMessageUseCase = getRandomDailyMessageUseCase,
            getPleasuresUseCase = getPleasuresUseCase,
            getRandomPleasureUseCase = getRandomPleasureUseCase,
            saveHistoryEntryUseCase = saveHistoryEntryUseCase,
            getTodayHistoryEntryUseCase = getTodayHistoryEntryUseCase,
            getUserInfoUseCase = getUserInfoUseCase,
            flipFeedbackPlayer = flipFeedbackPlayer
        )
    }
}
