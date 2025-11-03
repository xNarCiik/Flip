package com.dms.flip.domain.repository.onboarding

import com.dms.flip.domain.model.Pleasure
import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    fun getOnboardingStatus(userId: String): Flow<Boolean>
    suspend fun initOnboardingStatus()
    suspend fun saveOnboardingStatus(username: String, avatarUrl: String?, pleasures: List<Pleasure>)
}
