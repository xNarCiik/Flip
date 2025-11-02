package com.dms.flip.domain.usecase.history

import com.dms.flip.domain.model.PleasureHistory
import com.dms.flip.domain.repository.PleasureRepository
import com.dms.flip.ui.util.getTodayDayIdentifier
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTodayHistoryEntryUseCase @Inject constructor(
    private val pleasureRepository: PleasureRepository
) {
    operator fun invoke(): Flow<PleasureHistory?> {
        return pleasureRepository.observePleasureHistory(getTodayDayIdentifier())
    }
}
