package com.dms.flip.ui.dailyflip

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dms.flip.R
import com.dms.flip.data.model.PleasureCategory
import com.dms.flip.domain.usecase.GetRandomDailyMessageUseCase
import com.dms.flip.domain.usecase.dailypleasure.GetRandomPleasureUseCase
import com.dms.flip.domain.usecase.history.GetTodayHistoryEntryUseCase
import com.dms.flip.domain.usecase.history.SaveHistoryEntryUseCase
import com.dms.flip.domain.usecase.pleasures.GetPleasuresUseCase
import com.dms.flip.domain.usecase.user.GetUserInfoUseCase
import com.dms.flip.ui.util.FlipFeedbackPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

const val MinimumPleasuresCount = 7

@HiltViewModel
class DailyFlipViewModel @Inject constructor(
    private val resources: Resources,
    private val getRandomDailyMessageUseCase: GetRandomDailyMessageUseCase,
    private val getPleasuresUseCase: GetPleasuresUseCase,
    private val getRandomPleasureUseCase: GetRandomPleasureUseCase,
    private val saveHistoryEntryUseCase: SaveHistoryEntryUseCase,
    private val getTodayHistoryEntryUseCase: GetTodayHistoryEntryUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val flipFeedbackPlayer: FlipFeedbackPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyFlipUiState())
    val uiState: StateFlow<DailyFlipUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(screenState = DailyFlipScreenState.Loading)

            combine(
                getPleasuresUseCase(),
                getTodayHistoryEntryUseCase(),
                flow { emit(getRandomDailyMessageUseCase()) },
                getUserInfoUseCase()
            ) { pleasures, todayHistory, randomMessage, userInfo ->

                val enabledCount = pleasures.count { it.isEnabled }
                val isSetupRequired = enabledCount < MinimumPleasuresCount

                when {
                    isSetupRequired -> DailyFlipUiState(
                        screenState = DailyFlipScreenState.SetupRequired(enabledCount),
                        userInfo = userInfo
                    )

                    todayHistory?.completed == true -> DailyFlipUiState(
                        screenState = DailyFlipScreenState.Completed,
                        headerMessage = "",
                        userInfo = userInfo
                    )

                    else -> DailyFlipUiState(
                        screenState = DailyFlipScreenState.Ready(
                            availableCategories = PleasureCategory.entries,
                            dailyPleasure = todayHistory?.toPleasureOrNull(),
                            isCardFlipped = todayHistory != null
                        ),
                        headerMessage = if (todayHistory == null)
                            randomMessage
                        else
                            resources.getString(R.string.your_flip_daily),
                        userInfo = userInfo
                    )
                }
            }
                .catch { e ->
                    _uiState.value = DailyFlipUiState(
                        screenState = DailyFlipScreenState.Error(
                            e.message ?: resources.getString(R.string.generic_error_message)
                        ),
                        userInfo = _uiState.value.userInfo
                    )
                }
                .collectLatest { newState ->
                    _uiState.value = newState
                }
        }
    }

    fun onEvent(event: DailyFlipEvent) {
        when (event) {
            is DailyFlipEvent.Reload -> observeData()
            is DailyFlipEvent.OnCategorySelected -> handleCategorySelection(event.category)
            is DailyFlipEvent.OnCardClicked -> handleDrawCard()
            is DailyFlipEvent.OnCardFlipped -> handleCardFlipped()
            is DailyFlipEvent.OnCardMarkedAsDone -> handleCardMarkedAsDone()
        }
    }

    private fun handleCategorySelection(category: PleasureCategory) {
        val current = _uiState.value.screenState
        if (current is DailyFlipScreenState.Ready) {
            _uiState.update {
                it.copy(screenState = current.copy(selectedCategory = category))
            }
        }
    }

    private fun handleDrawCard() = viewModelScope.launch {
        val current = _uiState.value.screenState
        if (current is DailyFlipScreenState.Ready && current.dailyPleasure == null) {
            try {
                val randomPleasure = getRandomPleasureUseCase(current.selectedCategory).first()
                _uiState.update {
                    it.copy(screenState = current.copy(dailyPleasure = randomPleasure))
                }
            } catch (_: Exception) {
                _uiState.value = DailyFlipUiState(
                    screenState = DailyFlipScreenState.Error(resources.getString(R.string.generic_error_message)),
                    userInfo = _uiState.value.userInfo
                )
            }
        }
    }

    private fun handleCardFlipped() = viewModelScope.launch {
        val current = _uiState.value.screenState
        if (current is DailyFlipScreenState.Ready) {
            current.dailyPleasure?.let {
                saveHistoryEntryUseCase(it)
                flipFeedbackPlayer.playFlipFeedback()
            }
        }
    }

    private fun handleCardMarkedAsDone() = viewModelScope.launch {
        val current = _uiState.value.screenState
        if (current is DailyFlipScreenState.Ready) {
            try {
                current.dailyPleasure?.let { pleasure ->
                    saveHistoryEntryUseCase(pleasure, markAsCompleted = true)
                }
            } catch (_: Exception) {
                _uiState.value = DailyFlipUiState(
                    screenState = DailyFlipScreenState.Error(resources.getString(R.string.generic_error_message)),
                    userInfo = _uiState.value.userInfo
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        flipFeedbackPlayer.release()
    }
}
