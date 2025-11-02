package com.dms.flip.ui.dailyflip

import com.dms.flip.data.model.PleasureCategory
import com.dms.flip.domain.model.Pleasure
import com.dms.flip.domain.model.UserInfo

data class DailyFlipUiState(
    val screenState: DailyFlipScreenState = DailyFlipScreenState.Loading,
    val headerMessage: String = "",
    val userInfo: UserInfo? = null,
)

sealed interface DailyFlipScreenState {
    data object Loading : DailyFlipScreenState
    data class Error(val message: String) : DailyFlipScreenState

    data class SetupRequired(val pleasureCount: Int) : DailyFlipScreenState

    data class Ready(
        val availableCategories: List<PleasureCategory> = emptyList(),
        val selectedCategory: PleasureCategory = PleasureCategory.ALL,
        val dailyPleasure: Pleasure? = null,
        val isCardFlipped: Boolean = false
    ) : DailyFlipScreenState

    data object Completed : DailyFlipScreenState
}

sealed interface DailyFlipEvent {
    data object Reload : DailyFlipEvent
    data class OnCategorySelected(val category: PleasureCategory) : DailyFlipEvent
    data object OnCardClicked : DailyFlipEvent
    data object OnCardFlipped : DailyFlipEvent
    data object OnCardMarkedAsDone : DailyFlipEvent
}
