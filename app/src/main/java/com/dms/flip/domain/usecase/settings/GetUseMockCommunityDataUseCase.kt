package com.dms.flip.domain.usecase.settings

import com.dms.flip.domain.repository.SettingsRepository
import javax.inject.Inject

class GetUseMockCommunityDataUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke() = settingsRepository.useMockCommunityData
}
