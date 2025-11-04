package com.dms.flip.domain.usecase.pleasures

import com.dms.flip.domain.model.community.PleasureCategory
import com.dms.flip.domain.model.Pleasure
import com.dms.flip.domain.repository.PleasureRepository
import javax.inject.Inject

class AddPleasureUseCase @Inject constructor(
    private val pleasureRepository: PleasureRepository
) {
    suspend operator fun invoke(title: String, description: String, category: PleasureCategory) {
        val pleasure = Pleasure(
            id = "",
            title = title,
            description = description,
            category = category,
            isEnabled = true
        )
        pleasureRepository.insert(pleasure)
    }
}
