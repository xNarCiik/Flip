package com.dms.flip.domain.usecase.history

import com.dms.flip.domain.model.Pleasure
import com.dms.flip.domain.repository.PleasureRepository
import com.dms.flip.ui.util.getTodayDayIdentifier
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class SaveHistoryEntryUseCase @Inject constructor(
    private val pleasureRepository: PleasureRepository
) {
    suspend operator fun invoke(pleasure: Pleasure, markAsCompleted: Boolean = false) {
        val dayIdentifier = getTodayDayIdentifier()
        val existingEntry = pleasureRepository.getPleasureHistory(dayIdentifier).firstOrNull()

        if (existingEntry == null) {
            val entry = pleasure.toPleasureHistory(id = dayIdentifier)
            pleasureRepository.createPleasureHistoryEntry(entry)
            if (markAsCompleted) {
                pleasureRepository.markPleasureHistoryCompleted(dayIdentifier)
            }
        } else if (markAsCompleted && !existingEntry.completed) {
            pleasureRepository.markPleasureHistoryCompleted(existingEntry.id)
        }
    }
}
