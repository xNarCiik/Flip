package com.dms.flip.ui.settings.manage

import com.dms.flip.domain.model.community.PleasureCategory
import com.dms.flip.domain.model.Pleasure

sealed interface ManagePleasuresEvent {
    data class OnPleasureToggled(val pleasure: Pleasure) : ManagePleasuresEvent
    data object OnAddPleasureClicked : ManagePleasuresEvent
    data object OnBottomSheetDismissed : ManagePleasuresEvent
    data class OnTitleChanged(val title: String) : ManagePleasuresEvent
    data class OnDescriptionChanged(val description: String) : ManagePleasuresEvent
    data class OnCategoryChanged(val category: PleasureCategory) : ManagePleasuresEvent
    data object OnSavePleasureClicked : ManagePleasuresEvent
    data object OnDeleteMultiplePleasuresClicked : ManagePleasuresEvent
    data object OnDeleteConfirmed : ManagePleasuresEvent
    data object OnDeleteCancelled : ManagePleasuresEvent
    data object OnRetryClicked : ManagePleasuresEvent
    data object OnEnterSelectionMode : ManagePleasuresEvent
    data object OnLeaveSelectionMode : ManagePleasuresEvent
    data class OnPleasureSelected(val pleasureId: String) : ManagePleasuresEvent
}
