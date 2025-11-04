package com.dms.flip.ui.settings.manage

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dms.flip.R
import com.dms.flip.domain.model.community.PleasureCategory
import com.dms.flip.domain.model.Pleasure
import com.dms.flip.domain.usecase.pleasures.AddPleasureUseCase
import com.dms.flip.domain.usecase.pleasures.DeletePleasuresUseCase
import com.dms.flip.domain.usecase.pleasures.GetPleasuresUseCase
import com.dms.flip.domain.usecase.pleasures.UpdatePleasureUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManagePleasuresViewModel @Inject constructor(
    private val resources: Resources,
    private val getPleasuresUseCase: GetPleasuresUseCase,
    private val updatePleasureUseCase: UpdatePleasureUseCase,
    private val addPleasureUseCase: AddPleasureUseCase,
    private val deletePleasuresUseCase: DeletePleasuresUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManagePleasuresUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPleasures()
    }

    private fun loadPleasures() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }

        getPleasuresUseCase()
            .catch { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: resources.getString(R.string.generic_error_message)
                    )
                }
            }
            .collectLatest { pleasures ->
                _uiState.update {
                    it.copy(
                        pleasures = pleasures,
                        isLoading = false,
                        error = null
                    )
                }
            }
    }

    fun onEvent(event: ManagePleasuresEvent) {
        when (event) {
            is ManagePleasuresEvent.OnPleasureToggled -> togglePleasure(event.pleasure)
            is ManagePleasuresEvent.OnAddPleasureClicked -> _uiState.update { it.copy(showAddDialog = true) }
            is ManagePleasuresEvent.OnBottomSheetDismissed -> dismissBottomSheet()
            is ManagePleasuresEvent.OnTitleChanged -> _uiState.update {
                it.copy(
                    newPleasureTitle = event.title,
                    titleError = null
                )
            }

            is ManagePleasuresEvent.OnDescriptionChanged -> _uiState.update {
                it.copy(
                    newPleasureDescription = event.description,
                    descriptionError = null
                )
            }

            is ManagePleasuresEvent.OnCategoryChanged -> _uiState.update {
                it.copy(
                    newPleasureCategory = event.category
                )
            }

            is ManagePleasuresEvent.OnSavePleasureClicked -> savePleasure()
            is ManagePleasuresEvent.OnDeleteMultiplePleasuresClicked -> {
                _uiState.update {
                    it.copy(
                        showDeleteConfirmation = true
                    )
                }
            }

            is ManagePleasuresEvent.OnDeleteConfirmed -> confirmDelete()
            is ManagePleasuresEvent.OnDeleteCancelled -> cancelDelete()
            is ManagePleasuresEvent.OnRetryClicked -> loadPleasures()
            is ManagePleasuresEvent.OnEnterSelectionMode -> _uiState.update { it.copy(isSelectionMode = true) }
            is ManagePleasuresEvent.OnLeaveSelectionMode -> _uiState.update { it.copy(isSelectionMode = false, selectedPleasures = emptyList()) }
            is ManagePleasuresEvent.OnPleasureSelected -> {
                val selectedPleasures = _uiState.value.selectedPleasures.toMutableList()
                if (selectedPleasures.contains(event.pleasureId)) {
                    selectedPleasures.remove(event.pleasureId)
                } else {
                    selectedPleasures.add(event.pleasureId)
                }
                _uiState.update { it.copy(selectedPleasures = selectedPleasures) }
            }
        }
    }

    private fun togglePleasure(pleasure: Pleasure) = viewModelScope.launch {
        try {
            val updatedPleasure = pleasure.copy(isEnabled = !pleasure.isEnabled)
            updatePleasureUseCase(updatedPleasure)
            _uiState.update {
                it.copy(pleasures = it.pleasures.map { p -> if (p.id == updatedPleasure.id) updatedPleasure else p })
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    error = e.message ?: resources.getString(R.string.generic_error_message)
                )
            }
        }
    }

    private fun dismissBottomSheet() {
        _uiState.update {
            it.copy(
                showAddDialog = false,
                newPleasureTitle = "",
                newPleasureDescription = "",
                newPleasureCategory = PleasureCategory.FOOD,
                titleError = null,
                descriptionError = null
            )
        }
    }

    private fun savePleasure() {
        val state = _uiState.value
        var hasError = false

        if (state.newPleasureTitle.isBlank()) {
            _uiState.update { it.copy(titleError = "Le titre ne peut pas être vide") }
            hasError = true
        }

        if (state.newPleasureDescription.isBlank()) {
            _uiState.update { it.copy(descriptionError = "La description ne peut pas être vide") }
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            try {
                addPleasureUseCase(
                    title = state.newPleasureTitle.trim(),
                    description = state.newPleasureDescription.trim(),
                    category = state.newPleasureCategory
                )
                dismissBottomSheet()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: resources.getString(R.string.generic_error_message)
                    )
                }
            }
        }
    }

    private fun confirmDelete() {
        viewModelScope.launch {
            try {
                val pleasuresToDelete = _uiState.value.selectedPleasures
                deletePleasuresUseCase(pleasuresToDelete)
                _uiState.update {
                    it.copy(
                        showDeleteConfirmation = false,
                        selectedPleasures = emptyList(),
                        isSelectionMode = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: resources.getString(R.string.generic_error_message),
                        showDeleteConfirmation = false,
                    )
                }
            }
        }
    }

    private fun cancelDelete() {
        _uiState.update {
            it.copy(
                showDeleteConfirmation = false,
            )
        }
    }
}
