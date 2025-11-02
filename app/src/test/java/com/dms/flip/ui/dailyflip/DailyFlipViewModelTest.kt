package com.dms.flip.ui.dailyflip

import android.content.res.Resources
import com.dms.flip.R
import com.dms.flip.data.model.PleasureCategory
import com.dms.flip.domain.model.Pleasure
import com.dms.flip.domain.model.PleasureHistory
import com.dms.flip.domain.model.UserInfo
import com.dms.flip.domain.repository.DailyMessageRepository
import com.dms.flip.domain.repository.PleasureRepository
import com.dms.flip.domain.repository.UserRepository
import com.dms.flip.domain.usecase.GetRandomDailyMessageUseCase
import com.dms.flip.domain.usecase.dailypleasure.GetRandomPleasureUseCase
import com.dms.flip.domain.usecase.history.GetTodayHistoryEntryUseCase
import com.dms.flip.domain.usecase.history.SaveHistoryEntryUseCase
import com.dms.flip.domain.usecase.pleasures.GetPleasuresUseCase
import com.dms.flip.domain.usecase.user.GetUserInfoUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DailyFlipViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var resources: Resources
    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        resources = mock()
        whenever(resources.getString(R.string.your_flip_daily)).thenReturn("your flip")
        whenever(resources.getString(R.string.generic_error_message)).thenReturn("error")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `history emission keeps card flipped state`() = runTest {
        val pleasuresFlow = MutableStateFlow(
            List(MinimumPleasuresCount) { index ->
                Pleasure(id = index.toString(), title = "Pleasure $index")
            }
        )
        val historyFlow = MutableStateFlow<PleasureHistory?>(null)
        val userInfoFlow = MutableStateFlow(UserInfo(id = "user", username = "Flip"))
        val randomPleasureFlow = MutableStateFlow(
            Pleasure(id = "random", title = "Random", category = PleasureCategory.ALL)
        )

        val pleasureRepository = FakePleasureRepository(
            pleasuresFlow = pleasuresFlow,
            randomPleasureFlow = randomPleasureFlow,
            historyFlow = historyFlow
        )
        val userRepository = FakeUserRepository(userInfoFlow)
        val dailyMessageRepository = FakeDailyMessageRepository("hello")

        val viewModel = DailyFlipViewModel(
            resources = resources,
            getRandomDailyMessageUseCase = GetRandomDailyMessageUseCase(dailyMessageRepository),
            getPleasuresUseCase = GetPleasuresUseCase(pleasureRepository),
            getRandomPleasureUseCase = GetRandomPleasureUseCase(pleasureRepository),
            saveHistoryEntryUseCase = SaveHistoryEntryUseCase(pleasureRepository),
            getTodayHistoryEntryUseCase = GetTodayHistoryEntryUseCase(pleasureRepository),
            getUserInfoUseCase = GetUserInfoUseCase(userRepository)
        )

        advanceUntilIdle()

        val initialState = viewModel.uiState.value.screenState as DailyFlipScreenState.Ready
        assertThat(initialState.dailyPleasure).isNull()
        assertThat(initialState.isCardFlipped).isFalse()

        val history = PleasureHistory(
            id = "today",
            dateDrawn = 123L,
            completed = false,
            pleasureTitle = "Yoga",
            pleasureCategory = PleasureCategory.SPORT,
            pleasureDescription = "Stretch"
        )
        historyFlow.value = history

        advanceUntilIdle()

        val updatedState = viewModel.uiState.value.screenState as DailyFlipScreenState.Ready
        assertThat(updatedState.dailyPleasure?.title).isEqualTo("Yoga")
        assertThat(updatedState.isCardFlipped).isTrue()
    }

    @Test
    fun `card stays unflipped until acknowledged`() = runTest {
        val pleasuresFlow = MutableStateFlow(
            List(MinimumPleasuresCount) { index ->
                Pleasure(id = index.toString(), title = "Pleasure $index")
            }
        )
        val randomPleasure = Pleasure(
            id = "random",
            title = "Random",
            category = PleasureCategory.ALL
        )
        val historyFlow = MutableStateFlow<PleasureHistory?>(null)
        val userInfoFlow = MutableStateFlow(UserInfo(id = "user", username = "Flip"))

        val pleasureRepository = FakePleasureRepository(
            pleasuresFlow = pleasuresFlow,
            randomPleasureFlow = MutableStateFlow(randomPleasure),
            historyFlow = historyFlow
        )
        val userRepository = FakeUserRepository(userInfoFlow)
        val dailyMessageRepository = FakeDailyMessageRepository("hello")

        val viewModel = DailyFlipViewModel(
            resources = resources,
            getRandomDailyMessageUseCase = GetRandomDailyMessageUseCase(dailyMessageRepository),
            getPleasuresUseCase = GetPleasuresUseCase(pleasureRepository),
            getRandomPleasureUseCase = GetRandomPleasureUseCase(pleasureRepository),
            saveHistoryEntryUseCase = SaveHistoryEntryUseCase(pleasureRepository),
            getTodayHistoryEntryUseCase = GetTodayHistoryEntryUseCase(pleasureRepository),
            getUserInfoUseCase = GetUserInfoUseCase(userRepository)
        )

        advanceUntilIdle()

        viewModel.onEvent(DailyFlipEvent.OnCardClicked)

        advanceUntilIdle()

        val afterDraw = viewModel.uiState.value.screenState as DailyFlipScreenState.Ready
        assertThat(afterDraw.dailyPleasure?.id).isEqualTo(randomPleasure.id)
        assertThat(afterDraw.isCardFlipped).isFalse()

        viewModel.onEvent(DailyFlipEvent.OnCardFlipped)

        advanceUntilIdle()

        val afterFlip = viewModel.uiState.value.screenState as DailyFlipScreenState.Ready
        assertThat(afterFlip.isCardFlipped).isTrue()
    }

    private class FakePleasureRepository(
        private val pleasuresFlow: MutableStateFlow<List<Pleasure>>,
        private val randomPleasureFlow: MutableStateFlow<Pleasure>,
        private val historyFlow: MutableStateFlow<PleasureHistory?>
    ) : PleasureRepository {
        override fun getPleasures(): Flow<List<Pleasure>> = pleasuresFlow

        override fun getPleasuresCount(): Flow<Int> = pleasuresFlow.map { pleasures ->
            pleasures.count { it.isEnabled }
        }

        override fun getRandomPleasure(category: PleasureCategory?): Flow<Pleasure> = randomPleasureFlow

        override suspend fun insert(pleasure: Pleasure) = Unit

        override suspend fun update(pleasure: Pleasure) = Unit

        override suspend fun delete(pleasuresId: List<String>) = Unit

        override suspend fun upsertPleasureHistory(pleasureHistory: PleasureHistory) {
            historyFlow.value = pleasureHistory
        }

        override suspend fun getPleasureHistory(id: String): PleasureHistory? = historyFlow.value

        override fun observePleasureHistory(id: String): Flow<PleasureHistory?> = historyFlow
    }

    private class FakeUserRepository(
        private val userInfoFlow: MutableStateFlow<UserInfo?>
    ) : UserRepository {
        override fun getUserInfo(): Flow<UserInfo?> = userInfoFlow
    }

    private class FakeDailyMessageRepository(
        private val message: String
    ) : DailyMessageRepository {
        override fun getDailyMessages(): List<String> = listOf(message)

        override fun getRandomDailyMessage(): String = message
    }
}
