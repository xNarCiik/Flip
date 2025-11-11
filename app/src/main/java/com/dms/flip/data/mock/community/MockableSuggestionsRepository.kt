package com.dms.flip.data.mock.community

import com.dms.flip.data.repository.community.SuggestionsRepositoryImpl
import com.dms.flip.domain.model.community.FriendSuggestion
import com.dms.flip.domain.repository.SettingsRepository
import com.dms.flip.domain.repository.community.SuggestionsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockableSuggestionsRepository @Inject constructor(
    private val real: SuggestionsRepositoryImpl,
    private val mock: MockSuggestionsRepository,
    private val settingsRepository: SettingsRepository,
) : SuggestionsRepository {

    override fun observeSuggestions(): Flow<List<FriendSuggestion>> =
        settingsRepository.useMockCommunityData.flatMapLatest { useMock ->
            if (useMock) {
                mock.observeSuggestions()
            } else {
                real.observeSuggestions()
            }
        }

    override suspend fun hideSuggestion(userId: String) {
        val delegate = if (settingsRepository.useMockCommunityData.first()) {
            mock
        } else {
            real
        }
        delegate.hideSuggestion(userId)
    }
}
