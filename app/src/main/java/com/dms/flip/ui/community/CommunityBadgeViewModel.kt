package com.dms.flip.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dms.flip.domain.usecase.community.ObservePendingReceivedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class CommunityBadgeViewModel @Inject constructor(
    observePendingReceivedUseCase: ObservePendingReceivedUseCase
) : ViewModel() {

    private val _pendingRequestsCount = MutableStateFlow(0)
    val pendingRequestsCount: StateFlow<Int> = _pendingRequestsCount.asStateFlow()

    init {
        observePendingReceivedUseCase()
            .catch { /* Ignore errors for badge */ }
            .onEach { requests ->
                _pendingRequestsCount.value = requests.size
            }
            .launchIn(viewModelScope)
    }
}
