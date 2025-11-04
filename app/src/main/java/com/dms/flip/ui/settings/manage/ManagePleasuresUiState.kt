package com.dms.flip.ui.settings.manage

import com.dms.flip.domain.model.community.PleasureCategory
import com.dms.flip.domain.model.Pleasure

data class ManagePleasuresUiState(
    val isLoading: Boolean = false,
    val pleasures: List<Pleasure> = emptyList(),
    val showAddDialog: Boolean = false,
    val newPleasureTitle: String = "",
    val newPleasureDescription: String = "",
    val newPleasureCategory: PleasureCategory = PleasureCategory.ALL,
    val titleError: String? = null,
    val descriptionError: String? = null,
    val showDeleteConfirmation: Boolean = false,
    val isSelectionMode: Boolean = false,
    val selectedPleasures: List<String> = emptyList(),
    val error: String? = null
)
