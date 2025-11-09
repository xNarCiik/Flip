package com.dms.flip.domain.usecase.settings

import com.dms.flip.domain.repository.SettingsRepository
import javax.inject.Inject

class SetUseMockCommunityDataUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) =
        settingsRepository.setUseMockCommunityData(enabled)
}
