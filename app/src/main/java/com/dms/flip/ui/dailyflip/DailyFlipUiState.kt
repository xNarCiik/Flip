package com.dms.flip.ui.dailyflip

import android.net.Uri
import com.dms.flip.data.model.PleasureCategory
import com.dms.flip.domain.model.Pleasure
import com.dms.flip.domain.model.UserInfo

data class DailyFlipUiState(
    val screenState: DailyFlipScreenState = DailyFlipScreenState.Loading,
    val headerMessage: String = "",
    val userInfo: UserInfo? = null,
    val showShareBottomSheet: Boolean = false,
    val shareComment: String = "",
    val sharePhotoUri: Uri? = null,
    val isSharing: Boolean = false,
    val shareError: String? = null,
    val lastShareCompleted: Boolean = false
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

    data class Completed(val dailyPleasure: Pleasure? = null) : DailyFlipScreenState
}

sealed interface DailyFlipEvent {
    data object Reload : DailyFlipEvent
    data object OnShareSnackbarShown : DailyFlipEvent
    data class OnCategorySelected(val category: PleasureCategory) : DailyFlipEvent
    data object OnCardClicked : DailyFlipEvent
    data object OnCardFlipped : DailyFlipEvent
    data object OnCardMarkedAsDone : DailyFlipEvent
    data object OnShareClicked : DailyFlipEvent
    data object OnShareDismissed : DailyFlipEvent
    data class OnShareCommentChanged(val comment: String) : DailyFlipEvent
    data class OnSharePhotoSelected(val uri: Uri) : DailyFlipEvent
    data object OnSharePhotoRemoved : DailyFlipEvent
    data object OnShareSubmit : DailyFlipEvent
}
